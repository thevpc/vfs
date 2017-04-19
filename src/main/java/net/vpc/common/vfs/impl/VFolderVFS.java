/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.ListFS;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileFilter;
import net.vpc.common.vfs.VFileNameGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author taha.bensalah@gmail.com
 */
public class VFolderVFS extends AbstractDelegateVirtualFileSystem implements ListFS{

    private LinkedHashMap<String, VFile> children = new LinkedHashMap<>();

    public VFolderVFS(String id) {
        super(id);
    }

    @Override
    public String addOrRename(String name, VFile file, VFileNameGenerator nameGenerator) {
        if (name == null) {
            throw new NullPointerException("File Name is null");
        }
        if (file == null) {
            throw new NullPointerException("File is null");
        }
        String validName=name;
        if(nameGenerator==null){
            nameGenerator=DefaultFileNameGenerator.INSTANCE;
        }
        int index=1;
        while(true){
            validName=nameGenerator.generateFileName(name,index);
            if(!children.containsKey(validName)){
                break;
            }
            index++;
        }
        children.put(validName, file);
        return validName;
    }

    public void add(String name, VFile file) {
        if (name == null) {
            throw new NullPointerException("File Name is null");
        }
        if (file == null) {
            throw new NullPointerException("File is null");
        }
        if (children.containsKey(name)) {
            throw new IllegalArgumentException("File Name already exists");
        }
        children.put(name, file);
    }

    @Override
    public void remove(String name) {
        children.remove(name);

    }
    public VFile[] listFiles(final String path, final VFileFilter fileFilter) {
        if("/".equals(path)){
            List<VFile> all=new ArrayList<>(children.size());
            for (String s : children.keySet()) {
                DefaultFile f = new DefaultFile("/"+s, this);
                if(fileFilter==null || fileFilter.accept(f)) {
                    all.add(f);
                }
            }
            return all.toArray(new VFile[all.size()]);
        }
        return super.listFiles(path,fileFilter);
    }
    //    @Override
//    public String toVirtualPath(String jpath) {
//        if (prefix == null) {
//            return jpath.replace('\\', '/');
//        }
//        jpath=jpath.replace('\\', '/');
//        if (jpath.equals(normalizedPrefix)) {
//            return "/";
//        }
//        if (jpath.startsWith(normalizedPrefix + "/")) {
//            return jpath.substring(normalizedPrefix.length());
//        }
//        return null;
//    }
//
//    @Override
//    public String toNativePath(String vpath) {
//        if (vpath == null) {
//            return vpath;
//        }
//        if (vpath.equals("/")) {
//            return prefix;
//        }
//        //should handle .. and .
//        return prefix + "/" + vpath.replace("/", System.getProperty("file.separator"));
//    }
//
//    @Override
//    public VFile createTempFile(String prefix, String suffix, String folder) {
//        try {
//            File root = (folder == null) ? null : new File(toNativePath(folder));
//            if (root == null) {
//                root = this.prefix == null ? null : new File(prefix, "tmp");
//            }
//            File f = File.createTempFile(prefix, suffix, root);
//            return toVFile(f);
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//    }

    public VFile getDelegate(String path) {
        List<String> parts = VFSUtils.toPathParts(path, true);
        if (parts.size() == 0) {
            return null;
        }
        String s = parts.get(0);
        VFile r = children.get(s);
        if (r == null) {
            return null;
        } else {
            parts.remove(0);
            return r.get(VFSUtils.toPath(parts));
        }
    }

    @Override
    public String toString() {
        return "VFolderVFS{"+getId()+"}";
    }
}
