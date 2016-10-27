/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import java.io.File;
import java.io.IOException;
import net.vpc.common.vfs.VFile;

/**
 *
 * @author taha.bensalah@gmail.com
 */
@Deprecated
public class FolderVFS extends NativeVFS {

    private String prefix;
    private String normalizedPrefix;

    public FolderVFS(String prefix) {
        if (prefix != null) {
            if (prefix.length() > 0 && !prefix.equals("/") && prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            if (prefix.length() > 0 && !prefix.equals("\\") && prefix.endsWith("\\")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
        }
        this.prefix = prefix;
        this.normalizedPrefix=prefix.replace("\\", "/");
    }

    @Override
    public String toVirtualPath(String jpath) {
        if (prefix == null) {
            return jpath.replace('\\', '/');
        }
        jpath=jpath.replace('\\', '/');
        if (jpath.equals(normalizedPrefix)) {
            return "/";
        }
        if (jpath.startsWith(normalizedPrefix + "/")) {
            return jpath.substring(normalizedPrefix.length());
        }
        return null;
    }

    @Override
    public String toNativePath(String vpath) {
        if (vpath == null) {
            return vpath;
        }
        if (vpath.equals("/")) {
            return prefix;
        }
        //should handle .. and .
        return prefix + "/" + vpath.replace("/", System.getProperty("file.separator"));
    }

    @Override
    public VFile createTempFile(String prefix, String suffix, String folder) {
        try {
            File root = (folder == null) ? null : new File(toNativePath(folder));
            if (root == null) {
                root = this.prefix == null ? null : new File(prefix, "tmp");
            }
            File f = File.createTempFile(prefix, suffix, root);
            return toVFile(f);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
}
