/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.vpc.common.vfs.*;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public abstract class AbstractDelegateVirtualFileSystem extends AbstractVirtualFileSystem {
    private VFileDelegateSecurityManager _secu=new VFileDelegateSecurityManager(
            new VFileDelegate() {
                @Override
                public VFile getDelegate(String path) {
                    return AbstractDelegateVirtualFileSystem.this.getDelegate(path);
                }
            }
    );

    public AbstractDelegateVirtualFileSystem(String id) {
        super(id);
    }

    public abstract VFile getDelegate(String f);

    @Override
    public void delete(String path) throws IOException {
        VFile f = getDelegate(path);
        if (f == null) {
            throw new IOException("Not Found " + path);
        }
        f.getFileSystem().delete(f.getPath());
    }

    @Override
    public void deleteAll(String path) throws IOException {
        VFile f = getDelegate(path);
        if (f == null) {
            throw new IOException("Not Found " + path);
        }
        f.getFileSystem().deleteAll(f.getPath());
    }

    @Override
    public boolean exists(String path) {
        VFile f = getDelegate(path);
        if (f == null) {
            if(path.equals("/")){
                //root alwas exists
                return true;
            }
            return false;
        }
        return f.getFileSystem().exists(f.getPath());
    }

    @Override
    public InputStream getInputStream(String path) throws IOException {
        VFile f = getDelegate(path);
        if (f == null) {
            return null;
        }
        return f.getFileSystem().getInputStream(f.getPath());
    }

    @Override
    public final OutputStream getOutputStream(String path) throws IOException {
        return getOutputStream(path, false);
    }

    @Override
    public OutputStream getOutputStream(String path, boolean append) throws IOException {
        VFile f = getDelegate(path);
        if (f == null) {
            return null;
        }
        return f.getFileSystem().getOutputStream(f.getPath(), append);
    }

    @Override
    public boolean isDirectory(String path) {
        if (path.equals("/")) {
            return true;
        }
        VFile f = getDelegate(path);
        if (f == null) {
            return false;
        }
        return f.getFileSystem().isDirectory(f.getPath());
    }

    @Override
    public long lastModified(String path) {
        VFile f = getDelegate(path);
        if (f == null) {
            return 0;
        }
        return f.getFileSystem().lastModified(f.getPath());
    }

    @Override
    public long length(String path) {
        VFile f = getDelegate(path);
        if (f == null) {
            return 0;
        }
        return f.getFileSystem().length(f.getPath());
    }

    @Override
    public boolean mkdir(String path) {
        VFile f = getDelegate(path);
        if (f == null) {
            return false;
        }
        return f.getFileSystem().mkdir(f.getPath());
    }

    @Override
    public boolean mkdirs(String path) {
        VFile f = getDelegate(path);
        if (f == null) {
            return false;
        }
        return f.getFileSystem().mkdirs(f.getPath());
    }

    @Override
    public boolean isFile(String path) {
        VFile f = getDelegate(path);
        if (f == null) {
            return false;
        }
        return f.getFileSystem().isFile(f.getPath());
    }

    @Override
    public VFileType getFileType(String path) {
        VFile f = getDelegate(path);
        if (f == null) {
            return null;
        }
        return f.getFileSystem().getFileType(f.getPath());
    }
    @Override
    public VFile[] listFiles(final String path, final VFileFilter fileFilter) {
        VFile f = getDelegate(path);
        if (f == null) {
            return null;
        }
        final String basePath = f.getPath();
        VFile[] vFiles = f.listFiles(
                fileFilter == null ? null :
                        new VFileFilter() {
                            @Override
                            public boolean accept(VFile pathname) {
                                String allPath = pathname.getPath();
                                String newPath = path + allPath.substring(basePath.length());
                                DefaultFile newFile = new DefaultFile(newPath, AbstractDelegateVirtualFileSystem.this);
                                return (fileFilter.accept(newFile));
                            }
                        });
        for (int i = 0; i < vFiles.length; i++) {
            String allPath = vFiles[i].getPath();
            String newPath = path + allPath.substring(basePath.length());
            DefaultFile newFile = new DefaultFile(newPath, AbstractDelegateVirtualFileSystem.this);
            vFiles[i]=newFile;
        }
        return vFiles;
    }

    @Override
    public VFile getBase(String path, String vfsId) {
        if (vfsId == null || vfsId.length() == 0 || vfsId.equalsIgnoreCase(getId())) {
            return get(path);
        }
        VFile delegate = getDelegate(path);
        if(delegate!=null){
            return delegate.getBaseFile(vfsId);
        }
        return null;
    }

    @Override
    public VFSSecurityManager getSecurityManager() {
        return _secu;
    }

    @Override
    public VirtualFileACL getACL(String path) {
        VFile delegate = getDelegate(path);
        if(delegate==null) {
            if ("/".equals(path)) {
                return DefaultVirtualFileACL.READ_ONLY;
            }
            return null;
        }
        return delegate.getFileSystem().getACL(delegate.getPath());
    }
}
