/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VirtualFileACL;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import net.vpc.common.vfs.FileName;
import net.vpc.common.vfs.VFS;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileFilter;
import net.vpc.common.vfs.VFileType;
import net.vpc.common.vfs.VFileVisitor;
import net.vpc.common.vfs.VirtualFileSystem;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class DefaultFile implements VFile {

    private String name;
    private String path;
    private VirtualFileSystem fs;

    public DefaultFile(String path, VirtualFileSystem fs) {
        this.fs = fs;
        this.path = path;
        int index = path.lastIndexOf('/');
        if (index < 0) {
            name = path;
        } else {
            name = path.substring(index + 1);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getParentPath() {
        VFile p = getParentFile();
        return p == null ? null : p.getName();
    }

    @Override
    public VFile getParentFile() {
        return fs.getParentFile(getPath());
    }

    @Override
    public boolean isFile() {
        return fs.isFile(getPath());
    }

    @Override
    public boolean isDirectory() {
        return fs.isDirectory(getPath());
    }

    @Override
    public VFileType getFileType() {
        return fs.getFileType(getPath());
    }

    @Override
    public VFile[] listFiles() {
        return fs.listFiles(getPath());
    }

    @Override
    public boolean mkdirs() {
        return fs.mkdirs(getPath());
    }

    @Override
    public void delete() throws IOException {
        fs.delete(getPath());
    }

    @Override
    public void deleteAll() throws IOException {
        fs.deleteAll(getPath());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fs.getInputStream(getPath());
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return fs.getOutputStream(getPath());
    }

    @Override
    public OutputStream getOutputStream(boolean append) throws IOException {
        return fs.getOutputStream(getPath(), append);
    }

    @Override
    public VFile[] listFiles(VFileFilter fileFilter) {
        return fs.listFiles(getPath(), fileFilter);
    }

    @Override
    public VFile get(String path) {
        return fs.get(getPath(), path);
    }

    @Override
    public long length() {
        return fs.length(getPath());
    }

    @Override
    public long lastModified() {
        return fs.lastModified(getPath());
    }

    @Override
    public VirtualFileSystem getFileSystem() {
        return fs;
    }

    @Override
    public boolean exists() {
        return fs.exists(getPath());
    }

    @Override
    public void renameTo(VFile file) throws IOException {
        fs.renameTo(getPath(), file);
    }

    @Override
    public void copyTo(VFile file) throws IOException {
        fs.copyTo(getPath(), file);
    }

    @Override
    public boolean isParentOf(String path) {
        return fs.isParentOf(getPath(), path);
    }

    @Override
    public boolean isChildOf(String path) {
        return fs.isParentOf(path, getPath());
    }

    @Override
    public void visit(VFileVisitor visitor, VFileFilter filter) {
        VFS.visit(this, visitor, filter);
    }

    @Override
    public byte[] readBytes() throws IOException {
        return fs.readBytes(path);
    }

    @Override
    public void writeBytes(byte[] bytes) throws IOException {
        fs.writeBytes(path, bytes);
    }

    @Override
    public String probeContentType() throws IOException {
        return fs.probeContentType(path, false);
    }

    @Override
    public String probeContentType(boolean bestEffort) throws IOException {
        return fs.probeContentType(path, bestEffort);
    }

    @Override
    public String toString() {
        return "File{" + "path=" + path + ", fs=" + fs + '}';
    }

    @Override
    public FileName getFileName() {
        String[] s1 = splitName(getName(), true);
        String[] s2 = splitName(getName(), false);
        return new FileName(s1[0], s1[1], s2[0], s2[1]);
    }

    private String[] splitName(String baseName, boolean longExtension) {
        if (baseName != null) {
            int dot = longExtension ? baseName.indexOf('.') : baseName.lastIndexOf('.');
            if (dot < 0) {
                return new String[]{baseName, null};
            } else if (dot == 0) {
                return new String[]{"", baseName};
            } else if (dot == baseName.length() - 1) {
                return new String[]{baseName, ""};
            } else {
                return new String[]{
                    baseName.substring(0, dot),
                    baseName.substring(dot + 1)
                };
            }
        }
        return new String[]{null, null};
    }

    @Override
    public VirtualFileACL getACL() {
        return getFileSystem().getACL(path);
    }

    @Override
    public VFile getBaseFile(String vfsId) {
        return fs.getBase(path,vfsId);
    }

    @Override
    public boolean isAllowedCreateChild(VFileType type, String user) {
        return getFileSystem().getSecurityManager().isAllowedCreateChild(getPath(),type, user);
    }

    @Override
    public boolean isAllowedRemoveChild(VFileType type, String user) {
        return getFileSystem().getSecurityManager().isAllowedRemoveChild(getPath(), type, user);
    }

    @Override
    public boolean isAllowedUpdateChild(VFileType type, String user) {
        return getFileSystem().getSecurityManager().isAllowedUpdateChild(getPath(), type, user);
    }

    @Override
    public boolean isAllowedList(String user) {
        return getFileSystem().getSecurityManager().isAllowedList(getPath(), user);
    }

    @Override
    public boolean isAllowedRemove(String user) {
        return getFileSystem().getSecurityManager().isAllowedRemove(getPath(), user);
    }

    @Override
    public boolean isAllowedRead(String user) {
        return getFileSystem().getSecurityManager().isAllowedRead(getPath(), user);
    }

    @Override
    public boolean isAllowedWrite(String user) {
        return getFileSystem().getSecurityManager().isAllowedWrite(getPath(),user);
    }
}
