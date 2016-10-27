/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs;

import java.io.IOException;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface MountableFS extends VirtualFileSystem {

    public void mount(String path, VirtualFileSystem subfs) throws IOException;

    public void umount(String path) throws IOException;
}
