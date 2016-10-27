/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.vpc.common.vfs.VFS;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileFilter;
import net.vpc.common.vfs.VirtualFileSystem;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public abstract class AbstractVirtualFileSystem implements VirtualFileSystem {

    private String id;

    public AbstractVirtualFileSystem(String id) {
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public String toPathString(List<String> r, int fromIndex, int toIndex) {
        return toPathString(r.subList(fromIndex, toIndex));
    }

    @Override
    public boolean isParentOf(String parent, String child) {
        List<String> p = toPathParts(parent, true);
        List<String> c = toPathParts(child, true);
        if (c.size() > p.size()) {
            for (int i = 0; i < p.size(); i++) {
                if (!p.get(i).equals(c.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public String toPathString(List<String> r) {
        if (r.isEmpty()) {
            return "/";
        }
        StringBuilder sb = new StringBuilder();
        for (String n : r) {
            sb.append("/").append(n);
        }
        return sb.toString();
    }

    public String normalizeVirtualPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Invalid Path");
        }
//        if (!path.startsWith("/")||!path.startsWith("\\")) {
//            throw new IllegalArgumentException("Invalid Path");
//        }

        List<String> r = toPathParts(path, true);
        return toPathString(r);
    }

    public List<String> toPathParts(String path, boolean compact) {
        List<String> r = new ArrayList<String>();
        for (String i : path.split("/|\\\\")) {
            if (i.length() > 0) {
                if (compact) {
                    if (i.equals(".")) {
                        //do nothing
                    } else if (i.equals("..")) {
                        if(r.size()>0) {
                            r.remove(r.size() - 1);
                        }
                    } else {
                        r.add(i);
                    }
                } else {
                    r.add(i);
                }
            }
        }
        return r;
    }

    @Override
    public VFile getParentFile(String file) {
        if (file == null) {
            return null;
        }
        String path = file;
        List<String> r = toPathParts(path, true);
        if (r.isEmpty()) {
            return null;
        }
        r.remove(r.size() - 1);
        if (r.isEmpty()) {
            return new DefaultFile("/", this);
        }
        StringBuilder sb = new StringBuilder();
        for (String n : r) {
            sb.append("/").append(n);
        }
        return new DefaultFile(sb.toString(), this);
    }

    @Override
    public VFile get(String path) {
        return new DefaultFile(normalizeVirtualPath(path), this);
    }

    @Override
    public void write(String path, InputStream stream) throws IOException {
        OutputStream c = null;
        try {
            c = getOutputStream(path);
            VFS.copy(stream, c, 2048);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    public VFile[] listFiles(String path) {
        return listFiles(path, null);
    }

    public File toNativeFile(String vpath) {
        String p = toNativePath(vpath);
        return p == null ? null : new File(p);
    }

    public String toNativePath(String vpath) {
        if (File.listRoots().length > 1) {
            if (vpath.equals("/")) {
                return System.getProperty("file.separator");
            }
            if (vpath.startsWith("/")) {
                vpath = vpath.substring(1);
            }
        }
        return vpath.replace("/", System.getProperty("file.separator"));
    }

    public String toVirtualPath(String jpath) {
        String fs = System.getProperty("file.separator");
        String p = jpath.replace(fs, "/");
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        return p;
    }

    @Override
    public VFile createTempFile(String prefix, String suffix, String folder) {
        if (folder == null) {
            folder = "/tmp";
        }
        VFile tmp = get(folder);
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
        Random r = new Random();
        int count = 0;
        while (count < 1000) {
            int i = Math.abs(r.nextInt());
            VFile p = tmp.get(prefix + i + suffix);
            if (!p.exists()) {
                return p;
            }
            count++;
        }
        throw new IllegalArgumentException("Unable to create temp file");
    }

    @Override
    public void renameTo(String path, VFile file) throws IOException {
        VFS.copy(get(path), file);
        delete(path);
    }

    @Override
    public void copyTo(String path, VFile file) throws IOException {
        VFS.copy(get(path), file);
    }

    @Override
    public VFile get(String parent, String path) {
        if (parent == null) {
            return get(path);
        }
        if (path == null) {
            return get(parent);
        }
        return get(parent + "/" + path);
    }

    @Override
    public VirtualFileSystem subfs(String path) {
        return subfs(path, null);
    }

    @Override
    public VirtualFileSystem subfs(String path, String fsId) {
        return new SubVFS(fsId, this, path);
    }

    /**
     *
     * @param fileFilter
     * @return
     */
    @Override
    public VirtualFileSystem filter(VFileFilter fileFilter) {
        return filter(fileFilter, null);
    }

    @Override
    public VirtualFileSystem filter(VFileFilter fileFilter, String fsId) {
        if (fileFilter == null) {
            return this;
        }
        return new FilteredFileSystem(fsId, this, fileFilter);
    }

    @Override
    public byte[] readBytes(String path) throws IOException {
        InputStream ios = get(path).getInputStream();
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        } finally {
            try {
                if (ous != null) {
                    ous.close();
                }
            } catch (IOException e) {
            }

            try {
                if (ios != null) {
                    ios.close();
                }
            } catch (IOException e) {
            }
        }
        if (ous == null) {
            throw new IOException();
        }
        return ous.toByteArray();
    }

    @Override
    public void writeBytes(String path, byte[] bytes) throws IOException {
        OutputStream o = null;
        try {
            o = getOutputStream(path);
            o.write(bytes);
        } finally {
            if (o != null) {
                o.close();
            }
        }
    }

    @Override
    public String probeContentType(String path, boolean bestEffort) throws IOException {
        return ContentTypeUtils.probeContentType(get(path).getName());
    }

    @Override
    public void deleteAll(String path) throws IOException {
        VFile f = get(path);
        if (f.isFile()) {
            delete(path);
        } else if (f.isDirectory()) {
            nativeDeleteAll(f);
        } else {
            throw new IOException("Unsupported");
        }
    }

    private void nativeDeleteAll(VFile head) throws IOException {
        if (head == null) {
            return;
        }
        LinkedList<VFile> stack = new LinkedList<VFile>();
        stack.push(head);

        while (!stack.isEmpty()) {
            VFile next = stack.peek();
            VFile[] children = next.listFiles();
            if (children == null) {
                children = new VFile[0];
            }
            boolean finishedSubtrees = false;
            for (VFile c : children) {
                if (c == head) {
                    finishedSubtrees = true;
                    break;
                }
            }
            boolean isLeaf = children.length == 0;
            if (finishedSubtrees || isLeaf) {
                stack.pop();
                next.delete();
                head = next;
            } else {
                for (VFile c : children) {
                    stack.push(c);
                }
            }
        }
    }
}
