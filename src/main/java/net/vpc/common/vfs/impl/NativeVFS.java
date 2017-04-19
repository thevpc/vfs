/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VirtualFileACL;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import net.vpc.common.vfs.VFS;
import net.vpc.common.vfs.VFSSecurityManager;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileFilter;
import net.vpc.common.vfs.VFileType;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class NativeVFS extends AbstractVirtualFileSystem {

    public NativeVFS() {
        super("NativeFS");
    }

    public NativeVFS(String id) {
        super(id);
    }

    protected VFile toVFile(File file) {
        if (file == null) {
            return null;
        }
        return new DefaultFile(toVirtualPath(file.getPath()), this);
    }

    @Override
    public VFile get(String path) {
        String jpath = toNativePath(path);
        if (jpath == null) {
            return null;
        }
        return toVFile(new File(jpath));
    }

     @Override
    public VFile getBase(String path, String vfsId) {
        if (vfsId == null || vfsId.length() == 0 || vfsId.equalsIgnoreCase(getId())) {
            return get(path);
        }
        return null;
    }


    protected File toNativeFile(VFile file) {
        String p = null;
        if (file != null) {
            p = toNativePath(file.getPath());
        }
        return p == null ? null : new File(p);
    }

    protected File toValidJFile(String file) {
        File f = toNativeFile(file);
        if (f == null) {
            toNativeFile(file);
            throw new NoSuchElementException("File Not Found " + file);
        }
        return f;
    }

    @Override
    public long lastModified(String path) {
        File f = toNativeFile(path);
        return f == null ? 0 : f.lastModified();
    }

    @Override
    public long length(String path) {
        File f = toNativeFile(path);
        return f == null ? 0 : f.length();
    }

    @Override
    public VFile getParentFile(String path) {
        File f = toNativeFile(path);
        if (f == null || path.equals("/")) {
            return null;
        }
        for (File listRoot : File.listRoots()) {
            if (listRoot.equals(f)) {
                return new DefaultFile("/", this);
            }
        }
        return toVFile(f.getParentFile());
    }

    @Override
    public boolean exists(String path) {
        File f = toNativeFile(path);
        return f != null && f.exists();
    }

    @Override
    public InputStream getInputStream(String path) throws FileNotFoundException {
        return new FileInputStream(toNativeFile(path));
    }

    @Override
    public OutputStream getOutputStream(String path, boolean append) throws IOException {
        return new FileOutputStream(toValidJFile(path), append);
    }

    @Override
    public boolean mkdir(String path) {
        return toValidJFile(path).mkdir();
    }

    @Override
    public boolean mkdirs(String path) {
        return toValidJFile(path).mkdirs();
    }

    @Override
    public OutputStream getOutputStream(String path) throws IOException {
        return getOutputStream(path, false);
    }

    @Override
    public boolean isFile(String file) {
        return toValidJFile(file).isFile();
    }

    @Override
    public boolean isDirectory(String file) {
        return toValidJFile(file).isDirectory();
    }

    @Override
    public VFileType getFileType(String path) {
        File ff = toValidJFile(path);
        return ff.isDirectory() ? VFileType.DIRECTORY : ff.isFile() ? VFileType.FILE : VFileType.UNKNOWN;
    }

    @Override
    public void write(String file, InputStream content) throws IOException {
        File f = toValidJFile(file);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f, false);
            VFSUtils.copy(content, fos, 2048);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    @Override
    public VFile[] listFiles(String path) {
        if (path.equals("/")) {
            File[] roots = File.listRoots();
            if (roots != null) {
                VFile[] all = new VFile[roots.length];
                for (int i = 0; i < all.length; i++) {
                    all[i] = toVFile(roots[i]);
                }
                return all;
            }
        }
        File jf = toNativeFile(path);
        if (jf != null) {
            File[] roots = jf.listFiles();
            if (roots != null) {
                VFile[] all = new VFile[roots.length];
                for (int i = 0; i < all.length; i++) {
                    all[i] = toVFile(roots[i]);
                }
                return all;
            }
        }
        return null;
    }

    @Override
    public VFile[] listFiles(String path, final VFileFilter fileFilter) {
        File jf = toNativeFile(path);
        if (jf != null) {
            File[] roots = jf.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    VFile vf = toVFile(pathname);
                    if (vf == null) {
                        return false;
                    }
                    if (fileFilter != null) {
                        return fileFilter.accept(vf);
                    }
                    return true;
                }
            });
            if (roots != null) {
                VFile[] all = new VFile[roots.length];
                for (int i = 0; i < all.length; i++) {
                    all[i] = toVFile(roots[i]);
                }
                return all;
            }
        }
        return new VFile[0];
    }

    @Override
    public VFile[] getRoots() {
        File[] roots = File.listRoots();
        VFile[] all = new VFile[roots.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = toVFile(roots[i]);
        }
        return all;
    }

    @Override
    public VFile createTempFile(String prefix, String suffix, String folder) {
        try {
            final File root = (folder == null) ? null : new File(toNativePath(folder));
            File f = File.createTempFile(prefix, suffix, root);
            return toVFile(f);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(String path) throws IOException {
        new File(toNativePath(path)).delete();
    }

    @Override
    public void renameTo(String path, VFile file) throws IOException {
        if (file.getFileSystem() == this) {
            toNativeFile(path).renameTo(toNativeFile(file));
        } else {
            VFSUtils.copy(get(path), file);
            delete(path);
        }
    }

    @Override
    public VFSSecurityManager getSecurityManager() {
        return DefaultVFSSecurityManager.INSTANCE;
    }

    @Override
    public String toString() {
        return "NativeVFS";
    }

    @Override
    public VirtualFileACL getACL(String path) {
        return DefaultVirtualFileACL.READ_WRITE;
    }

}
