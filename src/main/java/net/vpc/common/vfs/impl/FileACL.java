/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VirtualFileACL;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileType;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class FileACL implements SerializableVirtualFileACL {

    private static final Logger log = Logger.getLogger(FileACL.class.getName());
    private Properties p;
    private String path;
    private FileACLVirtualFileSystem outer;

    public FileACL(String path, Properties p, FileACLVirtualFileSystem outer) {
        this.outer = outer;
        this.p = p;
        this.path = path;
    }

    protected void set(String prop, String val) {
        if (p == null) {
            p=new Properties();
        }
        if (val == null) {
            val = "";
        }
        if (val.length() == 0) {
            p.remove(prop);
        } else {
            p.put(prop, val);
        }
    }

    public String getUser(String login) {
        if (isEmpty(login)) {
            String login2 = outer.getUserLogin();
            if (!isEmpty(login2)) {
                return login2;
            }
        }
        return login;
    }

    public ACLPermission isAllowed(String action, String login) {
        if (outer.isAdmin()) {
            return ACLPermission.GRANT;
        }
        //            String login0=login;
        login = getUser(login);
        if (p == null) {
            return ACLPermission.DEFAULT;
        }
        if (isEmpty(login) || getOwner().equals(login)) {
            return ACLPermission.GRANT;
        }
        String allowedProfiles = p.getProperty(action);
        if (isEmpty(allowedProfiles)) {
            return ACLPermission.DEFAULT;
        }
        if (allowedProfiles.trim().equals("*")) {
            return ACLPermission.GRANT;
        }
        return outer.userMatchesProfileFilter(login, allowedProfiles);
    }

    public String getOwner() {
        String owner = p == null ? null : p.getProperty("Owner");
        if((owner == null || owner.trim().isEmpty())) {
            VFile cf = outer.get(path);
            VFile pp = cf.getParentFile();
            if (pp != null) {
                VirtualFileACL pacl = pp.getACL();
                return pacl.getOwner();
            }
        }
        return owner == null ? "" : owner;
    }

    @Override
    public ACLPermission getAllowedCreateChildPermission(VFileType type, String user) {
        String typeSuffix = type == VFileType.FILE ? "File" : type == VFileType.DIRECTORY ? "Directory" : "";
        return isAllowed("CreateChild" + typeSuffix, user);
    }

    @Override
    public ACLPermission getAllowedRemoveChildPermission(VFileType type, String user) {
        String typeSuffix = type == VFileType.FILE ? "File" : type == VFileType.DIRECTORY ? "Directory" : "";
        return isAllowed("RemoveChild" + typeSuffix, user);
    }

    @Override
    public ACLPermission getAllowedUpdateChildPermission(VFileType type, String user) {
        String typeSuffix = type == VFileType.FILE ? "File" : type == VFileType.DIRECTORY ? "Directory" : "";
        return isAllowed("UpdateChild" + typeSuffix, user);
    }

    @Override
    public ACLPermission getAllowedRemovePermission(String user) {
        VFile cf = outer.get(path);
        ACLPermission curr = isAllowed("Remove", user);
        if(curr!=ACLPermission.DEFAULT){
            return curr;
        }
        if(cf.isFile()){
            VFile pp = cf.getParentFile();
            if (pp != null) {
                VirtualFileACL pacl = pp.getACL();
                if (pacl != null) {
                    curr = pacl.getAllowedRemoveChildPermission(cf.getFileType(), user);
                    if(curr!=ACLPermission.DEFAULT){
                        return curr;
                    }
                }
            }
        }
        return ACLPermission.DEFAULT;
    }

    @Override
    public ACLPermission getAllowedReadPermission(String user) {
        VFile cf = outer.get(path);
        ACLPermission curr = isAllowed("Read", user);
        if(curr!=ACLPermission.DEFAULT){
            return curr;
        }
        return ACLPermission.DEFAULT;
    }

    @Override
    public ACLPermission getAllowedWritePermission(String user) {
        VFile cf = outer.get(path);
        ACLPermission curr = isAllowed("Write", user);
        if(curr!=ACLPermission.DEFAULT){
            return curr;
        }
        if(cf.isFile()){
            VFile pp = cf.getParentFile();
            if (pp != null) {
                VirtualFileACL pacl = pp.getACL();
                if (pacl != null) {
                    curr = pacl.getAllowedUpdateChildPermission(cf.getFileType(), user);
                    if(curr!=ACLPermission.DEFAULT){
                        return curr;
                    }
                }
            }
        }
        return ACLPermission.DEFAULT;
    }

    @Override
    public ACLPermission getAllowedListPermission(String user) {

        return isAllowed("List", user);
    }

    @Override
    public byte[] toBytes() {
        try {
            ByteArrayOutputStream s = new ByteArrayOutputStream();
            if (p != null) {
                p.store(s, "Virtual File System ACL");
            }
            return s.toByteArray();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        return new byte[0];
    }

    @Override
    public VirtualFileACL getDefaultFileACL() {
        Properties p2 = new Properties();
        p2.setProperty("Owner", getUser(getOwner()));
        p2.setProperty("ReadFile", "*");
        return new FileACL(null, p2, outer);
    }

    @Override
    public VirtualFileACL getDefaultFolderACL() {
        Properties p2 = new Properties();
        p2.setProperty("Owner", getUser(getOwner()));
        p2.setProperty("ListDirectory", "*");
        return new FileACL(null, p2, outer);
    }

    public void chown(String newOwner) {
        setACLProperty("Owner", newOwner);
    }

    public void grantCreateFile(String profiles) {
        setACLProperty("CreateFile", profiles);
    }

    public void grantCreateDirectory(String profiles) {
        setACLProperty("CreateDirectory", profiles);
    }

    public void grantRemovePath(String profiles) {
        setACLProperty("RemovePath", profiles);
    }

    public void grantReadFile(String profiles) {
        setACLProperty("ReadFile", profiles);
    }

    public void grantWriteFile(String profiles) {
        setACLProperty("WriteFile", profiles);
    }

    public void grantListDirectory(String profiles) {
        setACLProperty("ListDirectory", profiles);
    }

    protected void setACLProperty(String property, String value) {
        try {
            if (outer.isAdmin()
                    || getOwner().equals(outer.getUserLogin())) {
                set(property, value);
                outer.storeACL(path, this);
            }
        } catch (Exception e) {
            log.log(Level.FINER, "Error", e);
            //ignore
        }
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    protected String getACLProperty(String property) {
        if (p == null) {
            return null;
        }
        return p.getProperty(property);
    }

    @Override
    public String getProperty(String name) {
        if (p == null) {
            return null;
        }
        return p.getProperty("$" + name);
    }

    @Override
    public Set<String> getPropertyNames() {
        if (p == null) {
            return Collections.EMPTY_SET;
        }
        HashSet<String> all = new HashSet<>();
        for (Object o : p.keySet()) {
            String s = (String) o;
            if (s.startsWith("$")) {
                all.add(s.substring(1));
            }
        }
        return all;
    }

    @Override
    public void setProperty(String name, String value) {
        setACLProperty("$" + name, value);
    }

    private static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

}
