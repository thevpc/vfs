/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.vpc.common.vfs.VFile;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class VZipUtils {

    public static void zip(VFile targetFile, VZipOptions options, VFile... source) throws IOException {
        if (options == null || options.getTempFileSystem() == null) {
            throw new IllegalArgumentException("Missing Options");
        }
        boolean tempFile = false;
        if (options.isTempFile()) {
            //ignore
        }
        VFile f = targetFile;
        if (tempFile) {
            f = options.getTempFileSystem().createTempFile("zip", ".zip", null);
        }
        ZipOutputStream zip = null;
        OutputStream fW = null;
        HashSet<String> excludedPaths = new HashSet<String>();
        excludedPaths.add(targetFile.getPath());
        excludedPaths.add(f.getPath());
        try {
            fW = f.getOutputStream();
            try {
                zip = new ZipOutputStream(fW);
                if (options.isSkipRoots()) {
                    for (VFile srcFileName : source) {
                        if (srcFileName.isDirectory()) {
                            for (VFile file : srcFileName.listFiles()) {
                                add("", file, zip, excludedPaths);
                            }
                        } else {
                            add("", srcFileName, zip, excludedPaths);
                        }
                    }
                } else {
                    for (VFile srcFileName : source) {
                        add("", srcFileName, zip, excludedPaths);
                    }
                }
            } finally {
                if (zip != null) {
                    zip.close();
                }
            }
        } finally {
            if (fW != null) {
                fW.close();
            }
        }
        if (options.isTempFile()) {
            boolean okRenamed = false;
            try {
                f.renameTo(targetFile);
                okRenamed = true;
            } catch (Exception ex) {
                Logger.getLogger(VZipUtils.class.getName()).log(Level.SEVERE, "Unable to rename " + f.getPath(), ex);
            }
            if (!okRenamed) {
                f.copyTo(targetFile);
            }
        }
    }

//    private static void zipDir(String dirName, String nameZipFile) throws IOException {
//        ZipOutputStream zip = null;
//        FileOutputStream fW = null;
//        fW = new FileOutputStream(nameZipFile);
//        zip = new ZipOutputStream(fW);
//        addFolderToZip("", dirName, zip);
//        zip.close();
//        fW.close();
//    }
    private static void add(String path, VFile srcFolder, ZipOutputStream zip, Set<String> excludedPaths) throws IOException {
        if (excludedPaths.contains(srcFolder.getPath())) {
            return;
        }
        if (srcFolder.isDirectory()) {
            addFolderToZip(path, srcFolder, zip, excludedPaths);
        } else {
            addFileToZip(path, srcFolder, zip, false, excludedPaths);
        }
    }

    private static void addFolderToZip(String path, VFile srcFolder, ZipOutputStream zip, Set<String> excludedPaths) throws IOException {
        if (excludedPaths.contains(srcFolder.getPath())) {
            return;
        }
        if (srcFolder.listFiles().length == 0) {
            addFileToZip(path, srcFolder, zip, true, excludedPaths);
        } else {
            for (VFile f : srcFolder.listFiles()) {
                if (path.equals("")) {
                    addFileToZip(srcFolder.getName(), f, zip, false, excludedPaths);
                } else {
                    addFileToZip(concatPath(path, srcFolder.getName()), f, zip, false, excludedPaths);
                }
            }
        }
    }

    private static String concatPath(String a, String b) {
        if (a.endsWith("/")) {
            if (b.startsWith("/")) {
                return a + b.substring(1);
            } else {
                return a + b;
            }
        } else {
            if (b.startsWith("/")) {
                return a + b;
            } else {
                return a + "/" + b;
            }
        }
    }

    private static void addFileToZip(String path, VFile srcFile, ZipOutputStream zip, boolean flag, Set<String> excludedPaths) throws IOException {
        if (excludedPaths.contains(srcFile.getPath())) {
            return;
        }
//        VFile folder = new File(srcFile);
        String pathPrefix = path;
        if (!pathPrefix.endsWith("/")) {
            pathPrefix = pathPrefix + "/";
        }
        if (!pathPrefix.startsWith("/")) {
            pathPrefix = "/" + pathPrefix;
        }
        if (flag) {
//            System.out.println("[FOLDER ]" + pathPrefix + folder.getName());
            zip.putNextEntry(new ZipEntry(pathPrefix + srcFile.getName() + "/"));
        } else {
            if (srcFile.isDirectory()) {
                addFolderToZip(pathPrefix, srcFile, zip, excludedPaths);
            } else {
//                System.out.println("[FILE  ]" + pathPrefix + folder.getName() + " - " + srcFile);
                byte[] buf = new byte[1024];
                int len;
                InputStream in = srcFile.getInputStream();
                zip.putNextEntry(new ZipEntry(pathPrefix + srcFile.getName()));
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
            }
        }
    }
}
