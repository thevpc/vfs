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

    public ACLPermission getPermission(String action, String login) {
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
        String typeSuffix = type == VFileType.FILE ? "File" : type == VFileType.DIRECTORY ? "Directory" : "Unknown";
        return getPermission("Create" + typeSuffix, user);
    }

    @Override
    public ACLPermission getAllowedRemoveChildPermission(VFileType type, String user) {
        String typeSuffix = type == VFileType.FILE ? "File" : type == VFileType.DIRECTORY ? "Directory" : "Unknown";
        return getPermission("Remove" + typeSuffix, user);
    }

    @Override
    public ACLPermission getAllowedUpdateChildPermission(VFileType type, String user) {
        String typeSuffix = type == VFileType.FILE ? "File" : type == VFileType.DIRECTORY ? "Directory" : "Unknown";
        return getPermission("Update" + typeSuffix, user);
    }

    @Override
    public ACLPermission getAllowedRemovePermission(String user) {
        VFile cf = outer.get(path);
        ACLPermission fcurr = getPermission("Remove", user);
        if(fcurr!=ACLPermission.DEFAULT){
            if(fcurr==ACLPermission.DENY){
                return fcurr;
            }
        }
        ACLPermission curr=null;
        //file allow here
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
        return fcurr;
    }

    @Override
    public ACLPermission getAllowedReadPermission(String user) {
        VFile cf = outer.get(path);
        ACLPermission curr = getPermission("Read", user);
        if(curr!=ACLPermission.DEFAULT){
            return curr;
        }
        return ACLPermission.DEFAULT;
    }

    @Override
    public ACLPermission getAllowedWritePermission(String user) {
        VFile cf = outer.get(path);
        ACLPermission curr = getPermission("Write", user);
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

        return getPermission("List", user);
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
        if(isPropagateOwner()) {
            p2.setProperty("Owner", getUser(getOwner()));
        }else{
            p2.setProperty("Owner", getUser(null));
        }
        if(p.containsKey("ReadFile")){
            p2.setProperty("ReadFile", p.getProperty("ReadFile"));
        }else {
            p2.setProperty("ReadFile", "*");
        }
        if(p.containsKey("WriteFile")){
            p2.setProperty("WriteFile", p.getProperty("WriteFile"));
        }else {
            p2.setProperty("WriteFile", "*");
        }
        return new FileACL(null, p2, outer);
    }

    @Override
    public VirtualFileACL getDefaultFolderACL() {
        Properties p2 = new Properties();
        //will inherit all rights
        if(isPropagateACL()) {
            p2.putAll(this.p);
        }
        if(p2.containsKey("ListDirectory")) {
            p2.setProperty("ListDirectory", "*");
        }
        if(isPropagateOwner()) {
            p2.setProperty("Owner", getUser(getOwner()));
        }else{
            p2.setProperty("Owner", getUser(null));
        }
        return new FileACL(null, p2, outer);
    }

    public void setOwner(String newOwner) {
        setACLProperty("Owner", newOwner);
    }

    public void setPermissionCreateFile(String profiles) {
        setACLProperty("CreateFile", profiles);
    }

    public void setPermissionCreateDirectory(String profiles) {
        setACLProperty("CreateDirectory", profiles);
    }

    public void setPermissionRemoveFile(String profiles) {
        setACLProperty("RemoveFile", profiles);
    }

    public void setPermissionRemove(String profiles) {
        setACLProperty("Remove", profiles);
    }

    public void setPermissionRemoveDirectory(String profiles) {
        setACLProperty("RemoveDirectory", profiles);
    }

    public String getPermissionReadFile(){
        return getACLProperty("ReadFile");
    }

    public String getPermissionRemove(){
        return getACLProperty("Remove");
    }

    public String getPermissionWriteFile(){
        return getACLProperty("WriteFile");
    }

    public String getPermissionListDirectory(){
        return getACLProperty("ListDirectory");
    }

    public String getPermissionCreateDirectory(){
        return getACLProperty("CreateDirectory");
    }

    public String getPermissionCreateFile(){
        return getACLProperty("CreateFile");
    }

    public String getPermissionRemoveFile(){
        return getACLProperty("RemoveFile");
    }

    public String getPermissionRemoveDirectory(){
        return getACLProperty("RemoveDirectory");
    }

    public void setPermissionReadFile(String profiles) {
        setACLProperty("ReadFile", profiles);
    }

    public void setPermissionWriteFile(String profiles) {
        setACLProperty("WriteFile", profiles);
    }

    public void setPermissionListDirectory(String profiles) {
        setACLProperty("ListDirectory", profiles);
    }

    protected void setACLProperty(String property, String value) {
        try {
            if (outer.isAdmin()
                    || getOwner().equals(outer.getUserLogin())) {
                set(property, value);
                outer.storeACL(path, this);
                return;
            }
        } catch (Exception e) {
            log.log(Level.FINER, "Error", e);
            //ignore
        }
        throw new SecurityException("ACL update not allowed for "+path);
    }

    @Override
    public boolean isReadOnly() {
        return !outer.isAdmin()
                && !getOwner().equals(outer.getUserLogin());
    }

    public boolean isPropagateOwner() {
        return Boolean.valueOf(getACLProperty("PropagateOwner"));
    }

    public void setPropagateOwner(boolean value) {
        setACLProperty("PropagateOwner",value?"true":null);
    }

    public boolean isPropagateACL() {
        return Boolean.valueOf(getACLProperty("PropagateACL"));
    }

    public void setPropagateACL(boolean value) {
        setACLProperty("PropagateACL",value?"true":null);
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
