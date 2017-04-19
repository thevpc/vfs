/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs;

import net.vpc.common.vfs.impl.ACLPermission;

import java.util.Set;

/**
 * @author taha.bensalah@gmail.com
 */
public interface VirtualFileACL {

    public ACLPermission getAllowedCreateChildPermission(VFileType type, String user);

    public ACLPermission getAllowedRemoveChildPermission(VFileType type, String user);

    public ACLPermission getAllowedUpdateChildPermission(VFileType type, String user);

    public ACLPermission getAllowedListPermission(String user);

    public ACLPermission getAllowedRemovePermission(String user);

    public ACLPermission getAllowedReadPermission(String user);

    public ACLPermission getAllowedWritePermission(String user);

    public VirtualFileACL getDefaultFileACL();

    public VirtualFileACL getDefaultFolderACL();

    public boolean isReadOnly();

    public String getProperty(String name);

    public void setProperty(String name, String value);

    public Set<String> getPropertyNames();

    public void setOwner(String newOwner);

    public String getOwner();

    public void setPermissionCreateFile(String profiles);

    public void setPermissionCreateDirectory(String profiles);

    public void setPermissionRemoveFile(String profiles);

    public void setPermissionRemoveDirectory(String profiles);

    public void setPermissionReadFile(String profiles);

    public void setPermissionWriteFile(String profiles);

    public void setPermissionListDirectory(String profiles);
    public void setPermissionRemove(String profiles) ;
    public String getPermissionRemove() ;

    public boolean isPropagateOwner();

    public void setPropagateOwner(boolean value);

    public boolean isPropagateACL();

    public void setPropagateACL(boolean value);

    public String getPermissionReadFile();

    public String getPermissionWriteFile();

    public String getPermissionRemoveFile();

    public String getPermissionRemoveDirectory();

    public String getPermissionListDirectory();

    public String getPermissionCreateDirectory();

    public String getPermissionCreateFile();

}
