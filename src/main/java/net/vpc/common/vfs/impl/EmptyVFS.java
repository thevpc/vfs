/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VirtualFileACL;
import net.vpc.common.vfs.VFSSecurityManager;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileFilter;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class EmptyVFS extends AbstractDelegateVirtualFileSystem {

    public EmptyVFS() {
        super("EmptyFS");
    }

    @Override
    public VFile getDelegate(String f) {
        return null;
    }

    @Override
    public VFile[] getRoots() {
        return new VFile[]{new DefaultFile("/", this)};
    }

    @Override
    public VFile[] listFiles(String path, VFileFilter fileFilter) {
        if (path.equals("/")) {
            return new VFile[0];
        }
        return new VFile[0];
    }

    @Override
    public VFile getBase(String path, String vfsId) {
        if (vfsId == null || vfsId.length() == 0 || vfsId.equalsIgnoreCase(getId())) {
            return get(path);
        }
        return null;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public VFSSecurityManager getSecurityManager() {
        return DefaultVFSSecurityManager.INSTANCE;
    }

    @Override
    public VirtualFileACL getACL(String path) {
        return DefaultVirtualFileACL.READ_ONLY;
    }

}
