/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface VirtualFileSystem {

    public String getId();

    public String toNativePath(String vpath);

    public String toVirtualPath(String jpath);

    VFile get(String parent, String path);

    VFile get(String parent);

    VFile getBase(String path, String vfsId);

    long lastModified(String path);

    long length(String path);

    VFile getParentFile(String path);

    boolean exists(String path);

    InputStream getInputStream(String path) throws IOException;

    OutputStream getOutputStream(String path) throws IOException;

    OutputStream getOutputStream(String path, boolean append) throws IOException;

    boolean mkdir(String path);

    boolean mkdirs(String path);

    void write(String path, InputStream stream) throws IOException;

    VFile[] listFiles(String path);

    VFile[] listFiles(String path, VFileFilter fileFilter);

    VFile[] getRoots();

    public boolean isFile(String path);

    public VFileType getFileType(String path);

    public boolean isDirectory(String path);

    VFile createTempFile(String prefix, String suffix, String folder);

    public void delete(String path) throws IOException;

    public void deleteAll(String path) throws IOException;

    public void renameTo(String path, VFile file) throws IOException;

    public void copyTo(String path, VFile file) throws IOException;

    public boolean isParentOf(String parent, String child);

    /**
     * creates a sub fs with random generated fsId
     * @param path
     * @return 
     */
    public VirtualFileSystem subfs(String path);

    public VirtualFileSystem subfs(String path, String fsId);

    public VirtualFileSystem filter(VFileFilter fileFilter);

    public VirtualFileSystem filter(VFileFilter fileFilter, String fsId);

    public byte[] readBytes(String path) throws IOException;

    public void writeBytes(String path, byte[] bytes) throws IOException;

    public String probeContentType(String path, boolean bestEffort) throws IOException;

    public VFSSecurityManager getSecurityManager();

    public VirtualFileACL getACL(String path);
}
