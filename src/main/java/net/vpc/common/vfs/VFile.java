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
public interface VFile {

    public VirtualFileSystem getFileSystem();

    public String getName();

    public String getPath();

    public VFile getBaseFile(String vfsId);

    public VirtualFileACL getACL();

    public String getParentPath();

    public VFile getParentFile();

    public VFileType getFileType();

    public VFile[] listFiles();

    public VFile[] listFiles(VFileFilter fileFilter);

    public boolean isFile();

    public InputStream getInputStream() throws IOException;

    public OutputStream getOutputStream() throws IOException;

    public OutputStream getOutputStream(boolean append) throws IOException;

    public boolean exists();

    public boolean mkdirs();

    public void delete() throws IOException;

    public void deleteAll() throws IOException;

    public boolean isDirectory();

    public long length();

    public long lastModified();

    public VFile get(String path);

    public void copyTo(VFile file) throws IOException;

    public void renameTo(VFile file) throws IOException;

    public boolean isParentOf(String path);

    public boolean isChildOf(String path);

    public void visit(VFileVisitor visitor, VFileFilter filter);

    public byte[] readBytes() throws IOException;

    public void writeBytes(byte[] bytes) throws IOException;

    public String probeContentType() throws IOException;

    public String probeContentType(boolean bestEffort) throws IOException;

    public FileName getFileName() throws IOException;

    public boolean isAllowedCreateChild(VFileType type, String user);

    public boolean isAllowedRemoveChild(VFileType type, String user);

    public boolean isAllowedUpdateChild(VFileType type, String user);

    public boolean isAllowedList(String user);

    public boolean isAllowedRemove(String user);

    public boolean isAllowedRead(String user);

    public boolean isAllowedWrite(String user);
}
