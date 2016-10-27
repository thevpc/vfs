/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface VFSSecurityManager {

    public boolean isAllowedCreateChild(String path, VFileType type, String user);

    public boolean isAllowedRemoveChild(String path, VFileType type, String user);

    public boolean isAllowedUpdateChild(String path, VFileType type, String user);

    public boolean isAllowedList(String path, String user);

    public boolean isAllowedRemove(String path, String user);

    public boolean isAllowedRead(String path, String user);

    public boolean isAllowedWrite(String path, String user);
}
