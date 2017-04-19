/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VirtualFileACL;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.common.vfs.VFSSecurityManager;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileFilter;
import net.vpc.common.vfs.VFileType;
import net.vpc.common.vfs.VirtualFileSystem;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public abstract class FileACLVirtualFileSystem extends AbstractDelegateVirtualFileSystem {

    private static final String EXT = ".vacl";
    private VirtualFileSystem baseFS;
    private VFSSecurityManager securityManager;

    private class ACLDelegateSecurityManager implements VFSSecurityManager {

        @Override
        public boolean isAllowedCreateChild(String path, VFileType type, String user) {
            VirtualFileACL acl = getACL(path);
            if (acl != null) {
                ACLPermission p = acl.getAllowedCreateChildPermission(type, user);
                if(p!=ACLPermission.DEFAULT){
                    return p==ACLPermission.GRANT;
                }
            }
            return false;
        }

        @Override
        public boolean isAllowedRemoveChild(String path, VFileType type, String user) {
            VirtualFileACL acl = getACL(path);
            if (acl != null) {
                ACLPermission p = acl.getAllowedRemoveChildPermission(type, user);
                if(p!=ACLPermission.DEFAULT){
                    return p==ACLPermission.GRANT;
                }
            }
            return false;
        }

        @Override
        public boolean isAllowedUpdateChild(String path, VFileType type, String user) {
            VirtualFileACL acl = getACL(path);
            if (acl != null) {
                ACLPermission p = acl.getAllowedUpdateChildPermission(type, user);
                if(p!=ACLPermission.DEFAULT){
                    return p==ACLPermission.GRANT;
                }
            }
            return false;
        }

        @Override
        public boolean isAllowedList(String path, String user) {
            VirtualFileACL acl = getACL(path);
            if (acl != null) {
                ACLPermission p = acl.getAllowedListPermission(user);
                if(p!=ACLPermission.DEFAULT){
                    return p==ACLPermission.GRANT;
                }
            }
            return true;
        }

        @Override
        public boolean isAllowedRemove(String path, String user) {
            VirtualFileACL acl = getACL(path);
            if (acl != null) {
                ACLPermission p = acl.getAllowedRemovePermission(user);
                if(p!=ACLPermission.DEFAULT){
                    return p==ACLPermission.GRANT;
                }
            }
            return false;
        }

        @Override
        public boolean isAllowedRead(String path, String user) {
            VirtualFileACL acl = getACL(path);
            if (acl != null) {
                ACLPermission p = acl.getAllowedReadPermission(user);
                if(p!=ACLPermission.DEFAULT){
                    return p==ACLPermission.GRANT;
                }
            }
            return true;
        }

        @Override
        public boolean isAllowedWrite(String path, String user) {
            VirtualFileACL acl = getACL(path);
            if (acl != null) {
                ACLPermission p = acl.getAllowedWritePermission(user);
                if(p!=ACLPermission.DEFAULT){
                    return p==ACLPermission.GRANT;
                }
            }
            return false;
        }

    }

    public FileACLVirtualFileSystem(String id, VirtualFileSystem baseFS) {
        super(id);
        this.baseFS = baseFS;
        this.securityManager = (new ACLDelegateSecurityManager());
    }

    @Override
    public VFile getDelegate(String f) {
        if (f.endsWith(EXT)) {
            return null;
        }
        return baseFS.get(f);
    }

    @Override
    public VFile getBase(String path, String vfsId) {
        if (vfsId == null || vfsId.length() == 0 || vfsId.equalsIgnoreCase(getId())) {
            return get(path);
        }
        VFile t = getDelegate(path);
        if (t != null) {
            return t.getBaseFile(vfsId);
        }
        return null;
    }

    private String getACLPath(String path) {
        VFile f = baseFS.get(path);
        if (f == null) {
            return null;
        }
        if (f.isFile()) {
            return f.getPath() + EXT;
        }
        if (f.isDirectory()) {
            return f.getPath() + "/" + EXT;
        }
        return null;
    }

    public void storeACL(String path, SerializableVirtualFileACL acl) {
        String aclPath = getACLPath(path);
        if (aclPath != null) {
            if (acl == null) {
                try {
                    baseFS.delete(aclPath);
                } catch (IOException ex) {
                    Logger.getLogger(FileACLVirtualFileSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                byte[] bytes = acl.toBytes();
                try {
                    baseFS.writeBytes(aclPath, bytes);
                } catch (IOException ex) {
                    Logger.getLogger(FileACLVirtualFileSystem.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @Override
    public SerializableVirtualFileACL getACL(String path) {
        String aclPath = getACLPath(path);
        if (aclPath != null && baseFS.exists(aclPath)) {
            try {
                byte[] b = baseFS.readBytes(aclPath);
                if (b != null && b.length > 0) {
                    return loadACL(path, b);
                }
            } catch (IOException ex) {
                Logger.getLogger(FileACLVirtualFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return loadACL(path, null);
    }

    public VirtualFileACL getExistingACL(String path) {
        String aclPath = getACLPath(path);
        if (aclPath != null && baseFS.exists(aclPath)) {
            try {
                byte[] b = baseFS.readBytes(aclPath);
                if (b != null && b.length > 0) {
                    return loadACL(path, b);
                }
            } catch (IOException ex) {
                Logger.getLogger(FileACLVirtualFileSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public VFile[] getRoots() {
        VFile[] baseRoots = baseFS.getRoots();
        VFile[] all = new VFile[baseRoots.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = new DefaultFile(baseRoots[i].getPath(), this);
        }
        return all;
    }

    @Override
    public VFile[] listFiles(String path, final VFileFilter fileFilter) {
        if (!getSecurityManager().isAllowedList(path, null)) {
            throw new RuntimeException("List Directory Not Allowed at " + path);
        }
        VFile[] baseFiles = baseFS.listFiles(path, new VFileFilter() {

            @Override
            public boolean accept(VFile pathname) {
                if (pathname.getPath().endsWith(EXT)) {
                    return false;
                }
                if (!getSecurityManager().isAllowedList(pathname.getPath(), null)) {
                    return false;
                }
                if (fileFilter != null && !fileFilter.accept(pathname)) {
                    return false;
                }
                return true;
            }
        });
        VFile[] all = new VFile[baseFiles.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = new DefaultFile(baseFiles[i].getPath(), this);
        }
        return all;
    }

    public VirtualFileSystem getBaseFS() {
        return baseFS;
    }

    @Override
    public void delete(String path) throws IOException {
//        VFileType type = getFileType(path);
//        VFile pp = getParentFile(path);
//        if (pp != null) {
//            if (!getSecurityManager().getAllowedRemoveChildPermission(pp.getPath(), type, null)) {
//                throw new RuntimeException("Not Allowed");
//            }
//        }
        if (!getSecurityManager().isAllowedRemove(path, null)) {
            throw new RuntimeException("Not Allowed");
        }
        super.delete(path);
    }

    @Override
    public boolean mkdirs(String path) {
        VFile t = baseFS.get(path);
        String lastName = null;
        while (t != null && !t.exists()) {
            lastName = t.getName();
            t = t.getParentFile();
        }
        if (t != null && lastName != null) {
//            if (!getSecurityManager().isAllowedCreateChild(t.getParentPath(), VFileType.DIRECTORY, null)) {
//                throw new RuntimeException("Not Allowed");
//            }
            if (!getSecurityManager().isAllowedCreateChild(t.getPath(), VFileType.DIRECTORY, null)) {
                throw new RuntimeException("Create Directory Not Allowed at " + t.getPath() + " . Unable to create " + path);
            }
        }
        boolean r = super.mkdirs(path);
        VirtualFileACL a = getExistingACL(path);
        if (a == null && t != null) {
            storeACL(path, (SerializableVirtualFileACL) getACL(t.getPath()).getDefaultFolderACL());
        }
        return r;
    }

    @Override
    public boolean mkdir(String path) {
        VFile t = baseFS.get(path);
        String lastName = null;
        while (t != null && !t.exists()) {
            lastName = t.getName();
            t = t.getParentFile();
        }
        if (t != null && lastName != null) {
//            if (!getSecurityManager().isAllowedCreateChild(t.getParentPath(), VFileType.DIRECTORY, null)) {
//                throw new RuntimeException("Not Allowed");
//            }
            if (!getSecurityManager().isAllowedCreateChild(t.getPath(), VFileType.DIRECTORY, null)) {
                throw new RuntimeException("Create Directory Not Allowed at " + t.getPath() + " . Unable to create " + path);
            }
        }
        boolean r = super.mkdir(path);
        VirtualFileACL a = getExistingACL(path);
        if (a == null && t != null) {
            storeACL(path, (SerializableVirtualFileACL) getACL(t.getPath()).getDefaultFolderACL());
        }
        return r;
    }

    @Override
    public OutputStream getOutputStream(String path, boolean append) throws IOException {
        if (baseFS.exists(path)) {
//            VFile pp = getParentFile(path);
//            if (pp != null) {
//                if (!getSecurityManager().isAllowedCreateChild(pp.getPath(), VFileType.FILE, null)) {
//                    throw new RuntimeException("Not Allowed");
//                }
//            }
            if (!getSecurityManager().isAllowedWrite(path, null)) {
                throw new RuntimeException("Write Not Allowed at " + path);
            }
        } else {
            VFile f = baseFS.get(path);
//            if (!getSecurityManager().getAllowedUpdateChildPermission(getParentFile(path).getPath(), VFileType.FILE, null)) {
//                throw new RuntimeException("Not Allowed");
//            }
            if (!getSecurityManager().isAllowedCreateChild(f.getParentFile().getPath(),VFileType.FILE, null)) {
                throw new RuntimeException("Write Not Allowed at " + f.getParentFile().getPath());
            }
            VirtualFileACL a0= getExistingACL(f.getPath());
            if (a0==null ) {
                VirtualFileACL a = getExistingACL(f.getParentPath());
                if (a != null) {
                    storeACL(path, (SerializableVirtualFileACL) a.getDefaultFileACL());
                }
            }
        }
        return super.getOutputStream(path, append);
    }

    @Override
    public InputStream getInputStream(String path) throws IOException {
        if (!getSecurityManager().isAllowedRead(path, null)) {
            throw new RuntimeException("Read Not Allowed at " + path);
        }
        return super.getInputStream(path);
    }

    @Override
    public VFSSecurityManager getSecurityManager() {
        return securityManager;
    }

    protected SerializableVirtualFileACL loadACL(String path, byte[] bytes) {
        try {
            Properties p = new Properties();
            if (bytes != null) {
                p.load(new ByteArrayInputStream(bytes));
                return new FileACL(path, p, this);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileACLVirtualFileSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new FileACL(path, null, this);
    }

    public abstract ACLPermission userMatchesProfileFilter(String login, String profile);

    /**
     * true if the current session has admin rights
     * @return true if the current session has admin rights
     */
    public abstract boolean isAdmin();

    /**
     * login of the current session user. When user is impersonated, the impersonated user should be returned.
     * @return login of the current session user
     */
    public abstract String getUserLogin();
}
