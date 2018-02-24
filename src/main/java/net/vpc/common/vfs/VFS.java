/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs;


import net.vpc.common.vfs.impl.*;

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

    public static ListFS createListFS(String id) {
        VFolderVFS fs = new VFolderVFS(id);
        return fs;
    }




}
