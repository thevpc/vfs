/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VirtualFileACL;
import java.util.ArrayList;
import java.util.List;
import net.vpc.common.vfs.VFSSecurityManager;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileFilter;
import net.vpc.common.vfs.VFileType;
import net.vpc.common.vfs.VirtualFileSystem;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class SubVFS extends AbstractDelegateVirtualFileSystem {

    private String prefix;
    private VirtualFileSystem fs;
    private List<String> prefixParts;
    VFSSecurityManagerImpl sm;

    public SubVFS(String id, VirtualFileSystem fs, String prefix) {
        super(id);
        this.fs = fs;
        this.prefix = (prefix == null || prefix.trim().isEmpty()) ? null : normalizeVirtualPath(prefix);
        this.prefixParts = this.prefix == null ? new ArrayList<String>() : VFSUtils.toPathParts(this.prefix, true);
        this.sm = new VFSSecurityManagerImpl();
    }

    @Override
    public VFile getDelegate(String f) {
        if (f.equals("/")) {
            return fs.get(prefix);
        } else {
            return fs.get(prefix, normalizeVirtualPath(f));
        }
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

//    @Override
//    public VirtualFileACL getACL(String path) {
//        VFile t = getDelegate(path);
//        return t == null ? null : t.getACL();
//    }

    @Override
    public VFSSecurityManager getSecurityManager() {
        return sm;
    }

    @Override
    public VFile[] listFiles(String path, VFileFilter fileFilter) {
        VFile f = getDelegate(path);
        if (f == null) {
            return new VFile[0];
        }
        VFile[] d = f.getFileSystem().listFiles(f.getPath());
        ArrayList<VFile> r = new ArrayList<>();
        for (VFile d1 : d) {
            List<String> pp = VFSUtils.toPathParts(d1.getPath(), true);
            for (int i = 0; i < prefixParts.size(); i++) {
                pp.remove(0);
            }
            VFile ff = get(toPathString(pp));
            if (fileFilter == null || fileFilter.accept(ff)) {
                r.add(ff);
            }
        }
        return r.toArray(new VFile[r.size()]);
    }

    @Override
    public VFile[] getRoots() {
        return new VFile[]{new DefaultFile("/", this)};
    }

    @Override
    public String toString() {
        return "SubFS{" + prefix + " @ " + fs + '}';
    }

    private class VFSSecurityManagerImpl implements VFSSecurityManager {

        VFSSecurityManager bsm;

        public VFSSecurityManagerImpl() {
            bsm = fs.getSecurityManager();
        }

        @Override
        public boolean isAllowedCreateChild(String path, VFileType type, String user) {
            VFile p = getDelegate(path);
            if (p == null) {
                return false;
            }
            return bsm.isAllowedCreateChild(p.getPath(), type, user);
        }

        @Override
        public boolean isAllowedRemoveChild(String path, VFileType type, String user) {
            VFile p = getDelegate(path);
            if (p == null) {
                return false;
            }
            return bsm.isAllowedRemoveChild(p.getPath(), type, user);
        }

        @Override
        public boolean isAllowedUpdateChild(String path, VFileType type, String user) {
            VFile p = getDelegate(path);
            if (p == null) {
                return false;
            }
            return bsm.isAllowedUpdateChild(p.getPath(), type, user);
        }

        @Override
        public boolean isAllowedList(String path, String user) {
            VFile p = getDelegate(path);
            if (p == null) {
                return false;
            }
            return bsm.isAllowedList(p.getPath(), user);
        }

        @Override
        public boolean isAllowedRemove(String path, String user) {
            VFile p = getDelegate(path);
            if (p == null) {
                return false;
            }
            return bsm.isAllowedRemove(p.getPath(), user);
        }

        @Override
        public boolean isAllowedRead(String path, String user) {
            VFile p = getDelegate(path);
            if (p == null) {
                return false;
            }
            return bsm.isAllowedRead(p.getPath(), user);
        }

        @Override
        public boolean isAllowedWrite(String path, String user) {
            VFile p = getDelegate(path);
            if (p == null) {
                return false;
            }
            return bsm.isAllowedWrite(p.getPath(), user);
        }
    }

}
