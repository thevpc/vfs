/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.common.vfs.impl.EmptyVFS;
import net.vpc.common.vfs.impl.DefaultMountableVFS;
import net.vpc.common.vfs.impl.NativeVFS;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public final class VFS {

    public static final VirtualFileSystem NATIVE_FS = new NativeVFS();
    public static final VirtualFileSystem EMPTY_FS = new EmptyVFS();


    private VFS() {
    }

    public static VirtualFileSystem createEmptyFS() {
        return EMPTY_FS;
    }

    public static VirtualFileSystem createNativeFS() {
        return NATIVE_FS;
    }

    public static MountableFS createMountableFS(String id) {
        DefaultMountableVFS fs = new DefaultMountableVFS(id);
        return fs;
    }

    public static void copy(File inFile, File outFile, final FileFilter filter) throws IOException {
        final VirtualFileSystem nfs = NATIVE_FS;
        copy(nfs.get(inFile.getPath()), nfs.get(outFile.getPath()), filter == null ? null : new VFileFilter() {

            @Override
            public boolean accept(VFile pathname) {
                return filter.accept(new File(nfs.toNativePath(pathname.getPath())));
            }
        });
    }

    public static void copy(VFile inFile, VFile outFile, VFileFilter filter) throws IOException {
        class InOut {

            VFile inFile;
            VFile outFile;

            public InOut(VFile inFile, VFile outFile) {
                this.inFile = inFile;
                this.outFile = outFile;
            }
        }
        Stack<InOut> stack = new Stack();
        stack.push(new InOut(inFile, outFile));
        boolean root = true;
        while (!stack.isEmpty()) {
            InOut x = stack.pop();
            boolean wasRoot = root;
            if (root) {
                root = false;
            }
            if (wasRoot || filter == null || filter.accept(x.inFile)) {
                if (x.inFile.isDirectory()) {
                    if (x.outFile.exists()) {
                        //do nothing
                    } else {
                        x.outFile.mkdirs();
                    }
                    VFile[] sub = x.inFile.listFiles();
                    for (int i = sub.length - 1; i >= 0; i--) {
                        stack.push(new InOut(sub[i], x.outFile.get(sub[i].getName())));
                    }
                } else {
                    VFile f_out = x.outFile;
                    if (x.outFile.exists() && x.outFile.isDirectory()) {
                        f_out = x.outFile.get(inFile.getName());
                    }

                    if (x.inFile.exists()) {
                        if (f_out.getParentFile() != null && !f_out.getParentFile().exists()) {
                            if (!f_out.getParentFile().mkdirs()) {
                                throw new IOException("Unable to create folder " + f_out.getParentFile());
                            }
                        }
                        InputStream ins = null;
                        OutputStream outs = null;
                        try {
                            ins = x.inFile.getInputStream();
                            try {
                                outs = f_out.getOutputStream();

                                copy(ins, outs, Math.max(1014 * 1024, (int) x.inFile.length()));

                            } finally {
                                if (outs != null) {
                                    outs.close();
                                }
                            }
                        } finally {
                            if (ins != null) {
                                ins.close();
                            }
                        }
                    }

//                    copyFiles(x.in, f_out);
                }
            }
        }
    }

    public static void visit(VFile inFile, VFileVisitor visitor, VFileFilter filter) {
        Stack<VFile> stack = new Stack();
        stack.push(inFile);
        boolean root = true;
        while (!stack.isEmpty()) {
            VFile x = stack.pop();
            boolean wasRoot = root;
            if (root) {
                root = false;
            }
            if (wasRoot || filter == null || filter.accept(x)) {
                if (x.isDirectory()) {
                    if (!visitor.visit(x)) {
                        return;
                    }
                    VFile[] sub = x.listFiles();
                    for (int i = sub.length - 1; i >= 0; i--) {
                        stack.push(sub[i]);
                    }
                } else if (!visitor.visit(x)) {
                    break;
                }
            }
        }
    }

    public static void copy(InputStream inStream, OutputStream outStream, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int r = -1;
        while ((r = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, r);
        }
    }

    public void copy(File inFile, VFile outFile) throws IOException {
        InputStream fin = null;
        OutputStream fout = null;
        try {
            fin = new FileInputStream(inFile);
            fout = outFile.getOutputStream(false);
            copy(fin, fout, 2048);
        } finally {
            if (fin != null) {
                fin.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
    }

    public void copy(VFile inFile, File outFile) throws IOException {
        InputStream fin = null;
        OutputStream fout = null;
        try {
            fin = inFile.getInputStream();
            fout = new FileOutputStream(outFile, false);
            copy(fin, fout, 2048);
        } finally {
            if (fin != null) {
                fin.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
    }

    public static File copyNativeTempFile(VFile inFile) throws IOException {
        File f = File.createTempFile("tmp_", inFile.getName());
        copy(inFile, NATIVE_FS.get(f.getPath()));
        return f;
    }

    public static void copy(VFile inFile, VFile outFile) throws IOException {
        InputStream fin = null;
        OutputStream fout = null;
        try {
            fin = inFile.getInputStream();
            fout = outFile.getOutputStream(false);
            copy(fin, fout, 2048);
        } finally {
            if (fin != null) {
                fin.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
    }
}
