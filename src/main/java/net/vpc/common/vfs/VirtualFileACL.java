/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs;

import net.vpc.common.vfs.impl.ACLPermission;

import java.util.Set;

/**
 *
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

    public void chown(String newOwner);

    public String getOwner();

    public void grantCreateFile(String profiles);

    public void grantCreateDirectory(String profiles);

    public void grantRemovePath(String profiles);

    public void grantReadFile(String profiles);

    public void grantWriteFile(String profiles);

    public void grantListDirectory(String profiles);

}
