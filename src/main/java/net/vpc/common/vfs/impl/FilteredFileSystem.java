/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VirtualFileACL;
import java.util.ArrayList;
import net.vpc.common.vfs.VFSSecurityManager;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileFilter;
import net.vpc.common.vfs.VirtualFileSystem;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class FilteredFileSystem extends AbstractDelegateVirtualFileSystem {

    private VirtualFileSystem fs;
    private VFileFilter filter;

    public FilteredFileSystem(String id,VirtualFileSystem fs, VFileFilter filter) {
        super(id);
        this.fs = fs;
        this.filter = filter;
        if (filter == null) {
            throw new NullPointerException("Filter could not be null");
        }
    }

    @Override
    public VFSSecurityManager getSecurityManager() {
        return fs.getSecurityManager();
    }

    @Override
    public VFile get(String path) {
        return super.get(path);
    }

    @Override
    public VFile getDelegate(String f) {
        VFile ff = fs.get(f);
        if (ff == null) {
            return null;
        }
        if (filter.accept(ff)) {
            return new DefaultFile(ff.getPath(), this);
        }
        return null;
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
    public VFile[] listFiles(String path, VFileFilter fileFilter) {
        return filter(fs.listFiles(path));
    }

    @Override
    public VFile[] getRoots() {
        VFile[] r = filter(fs.getRoots());
        if (r.length > 0) {
            return r;
        }
        return new VFile[]{new DefaultFile("/", this)};
    }

    private VFile[] filter(VFile[] o) {
        ArrayList<VFile> r = new ArrayList<>();
        for (VFile o1 : o) {
            if (filter.accept(o1)) {
                r.add(o1);
            }
        }
        return r.toArray(new VFile[r.size()]);
    }

    @Override
    public String toString() {
        return "FilteredFS{" + "fs=" + fs + ", filter=" + filter + '}';
    }

}
