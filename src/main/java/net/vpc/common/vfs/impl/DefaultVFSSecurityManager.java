/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VFSSecurityManager;
import net.vpc.common.vfs.VFileType;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class DefaultVFSSecurityManager implements VFSSecurityManager {

    public static final VFSSecurityManager INSTANCE = new DefaultVFSSecurityManager();

    @Override
    public boolean isAllowedCreateChild(String path, VFileType type, String user) {
        return true;
    }

    @Override
    public boolean isAllowedRemoveChild(String path, VFileType type, String user) {
        return true;
    }

    @Override
    public boolean isAllowedUpdateChild(String path, VFileType type, String user) {
        return true;
    }

    @Override
    public boolean isAllowedList(String path, String user) {
        return true;
    }

    @Override
    public boolean isAllowedRemove(String path, String user) {
        return true;
    }

    @Override
    public boolean isAllowedRead(String path, String user) {
        return true;
    }

    @Override
    public boolean isAllowedWrite(String path, String user) {
        return true;
    }

}
