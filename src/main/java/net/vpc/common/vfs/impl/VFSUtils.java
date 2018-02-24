package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/1/17.
 */
public class VFSUtils {
    public static String toPath(List<String> path) {
        if(path.size()==0){
            return "/";
        }
        StringBuilder sb=new StringBuilder();
        for (String s : path) {
            sb.append("/").append(s);
        }
        return sb.toString();
    }

    public static List<String> toPathParts(String path, boolean compact) {
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

    public static String wildcardToRegex(String pattern) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder("^");
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '!':
                case '$':
                case '{':
                case '}':
                case '+':
                case '[':
                case ']':
                {
                    sb.append('\\').append(c);
                    break;
                }
                case '?': {
                    sb.append("[^/\\\\]");
                    break;
                }
                case '*': {
                    if (i + 1 < cc.length && cc[i + 1] == '*') {
                        i++;
                        sb.append(".*");
                    } else {
                        sb.append("[^/\\\\]*");
                    }
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        sb.append('$');
        return sb.toString();
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

    public static void visit(VFile inFile, final String path, VFileVisitor visitor, VFileFilter filter) {
        class Val{
            VFile file;
            String[] next;

            public Val(VFile file, String[] next) {
                this.file = file;
                this.next = next;
            }
        }

        Stack<Val> stack = new Stack();
        List<String> strings = VFSUtils.toPathParts(path, true);
        stack.push(new Val(
                inFile
                , strings.toArray(new String[strings.size()])
        ));
        boolean root = true;
        while (!stack.isEmpty()) {
            Val x = stack.pop();
            if (root) {
                root = false;
            }
            if(x.next.length==0){
                if(filter==null || filter.accept(x.file)) {
                    if(!visitor.visit(x.file)){
                        break;
                    }
                }
            }else{
                if(x.file.isDirectory()) {
                    strings = new ArrayList<>(Arrays.asList(x.next));
                    String first = strings.remove(0);
                    String[] next = strings.toArray(new String[strings.size()]);
                    if (first.contains("**")) {
                            final Pattern pattern = Pattern.compile(VFSUtils.wildcardToRegex(first));
                            final String basePath = x.file.getPath();
                            x.file.visit(new VFileVisitor() {
                                @Override
                                public boolean visit(VFile pathname) {
//                            basePath.
                                    String path2 = pathname.getPath().substring(basePath.length() + 1);
                                    return pattern.matcher(path2).matches();
                                }
                            }, filter);
                    } else if (first.contains("*")) {
                        final Pattern pattern = Pattern.compile(VFSUtils.wildcardToRegex(first));
                            for (VFile child : x.file.listFiles(new VFileFilter() {
                                @Override
                                public boolean accept(VFile pathname) {
                                    return pattern.matcher(pathname.getName()).matches();
                                }
                            })) {
                                stack.push(new Val(child, next));
                            }
                    } else {
                            VFile vFile = x.file.get(first);
                            if (vFile.exists()) {
                                stack.push(new Val(vFile, next));
                            }
                    }
                }
            }
        }
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
    public static void copy(File inFile, VFile outFile, FileFilter filter) throws IOException {
        class InOut {

            File inFile;
            VFile outFile;

            public InOut(File inFile, VFile outFile) {
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
                    File[] sub = x.inFile.listFiles();
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
                            ins = new FileInputStream(x.inFile);
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


    public static void copy(InputStream inStream, OutputStream outStream, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int r = -1;
        while ((r = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, r);
        }
    }

    public static void copy(File inFile, VFile outFile) throws IOException {
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

    public static void copy(VFile inFile, File outFile) throws IOException {
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

//    public static void copy(File inFile, File outFile, final FileFilter filter) throws IOException {
//        final VirtualFileSystem nfs = VFS.NATIVE_FS;
//        copy(nfs.get(inFile.getPath()), nfs.get(outFile.getPath()), filter == null ? null : new VFileFilter() {
//
//            @Override
//            public boolean accept(VFile pathname) {
//                return filter.accept(new File(nfs.toNativePath(pathname.getPath())));
//            }
//        });
//    }

    public static File copyNativeTempFile(VFile inFile) throws IOException {
        File f = File.createTempFile("tmp_", inFile.getName());
        VFSUtils.copy(inFile, VFS.NATIVE_FS.get(f.getPath()));
        return f;
    }


}
