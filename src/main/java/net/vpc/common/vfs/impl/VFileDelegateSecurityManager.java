package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VFSSecurityManager;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileType;

/**
 * Created by vpc on 1/1/17.
 */
public class VFileDelegateSecurityManager implements VFSSecurityManager {
    private VFileDelegate delegate;

    public VFileDelegateSecurityManager(VFileDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isAllowedCreateChild(String path, VFileType type, String user) {
        VFile p = getDelegate(path);
        if (p == null) {
            return false;
        }
        return p.getFileSystem().getSecurityManager().isAllowedCreateChild(p.getPath(), type, user);
    }

    private VFile getDelegate(String path) {
        return null;
    }

    @Override
    public boolean isAllowedRemoveChild(String path, VFileType type, String user) {
        VFile p = getDelegate(path);
        if (p == null) {
            return false;
        }
        return p.getFileSystem().getSecurityManager().isAllowedRemoveChild(p.getPath(), type, user);
    }

    @Override
    public boolean isAllowedUpdateChild(String path, VFileType type, String user) {
        VFile p = getDelegate(path);
        if (p == null) {
            return false;
        }
        return p.getFileSystem().getSecurityManager().isAllowedUpdateChild(p.getPath(), type, user);
    }

    @Override
    public boolean isAllowedList(String path, String user) {
        VFile p = getDelegate(path);
        if (p == null) {
            return false;
        }
        return p.getFileSystem().getSecurityManager().isAllowedList(p.getPath(), user);
    }

    @Override
    public boolean isAllowedRemove(String path, String user) {
        VFile p = getDelegate(path);
        if (p == null) {
            return false;
        }
        return p.getFileSystem().getSecurityManager().isAllowedRemove(p.getPath(), user);
    }

    @Override
    public boolean isAllowedRead(String path, String user) {
        VFile p = getDelegate(path);
        if (p == null) {
            return false;
        }
        return p.getFileSystem().getSecurityManager().isAllowedRead(p.getPath(), user);
    }

    @Override
    public boolean isAllowedWrite(String path, String user) {
        VFile p = getDelegate(path);
        if (p == null) {
            return false;
        }
        return p.getFileSystem().getSecurityManager().isAllowedWrite(p.getPath(), user);
    }
}
