/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VirtualFileSystem;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class VZipOptions {
    private boolean skipRoots=false;
    private boolean tempFile=false;
    private VirtualFileSystem tempFileSystem;

    public VZipOptions() {
    }

    public VirtualFileSystem getTempFileSystem() {
        return tempFileSystem;
    }

    public VZipOptions setTempFileSystem(VirtualFileSystem fileSystem) {
        this.tempFileSystem = fileSystem;
        return this;
    }

    
    public boolean isSkipRoots() {
        return skipRoots;
    }

    public VZipOptions setSkipRoots(boolean skipRoots) {
        this.skipRoots = skipRoots;
        return this;
    }

    public boolean isTempFile() {
        return tempFile;
    }

    public VZipOptions setTempFile(boolean tempFile) {
        this.tempFile = tempFile;
        return this;
    }
    
    
}
