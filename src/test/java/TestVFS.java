import net.vpc.common.vfs.VFS;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VirtualFileSystem;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author taha.bensalah@gmail.com on 7/6/16.
 */
public class TestVFS {
    public static void main(String[] args) {
        try {
            VirtualFileSystem subfs = VFS.createNativeFS().subfs("C:\\work\\apache-tomcat-8.5.6\\bin");
            for (VFile listFile : subfs.listFiles("/")) {
                System.out.println(listFile);
            }
//            MountableFS mfs = VFS.createMountableFS("?");
//            mfs.mount("/", createNativeFS().subfs("/home/vpc/acm/folder1"));
//            mfs.mount("/f3", createNativeFS().subfs("/home/vpc/acm/folder3"));
//            for (VFile f : mfs.listFiles("/")) {
//
//                System.out.println((f.isDirectory()?"D":"F")+" "+(f.exists()?" ":"?")+" "+f.getName()+" "+f);
//            }
//            System.out.println(mfs.get("/f3/f4/A.class").exists());
//            System.out.println(mfs.get("/folder2").exists());
//            System.out.println(mfs.get("/folder2/MonPremierExemple2.java").exists());

//            VirtualFileSystem n = createNativeFS().filter(new VFileFilter() {
//
//                @Override
//                public boolean accept(VFile pathname) {
//                    return pathname.getPath().equals("/home") || pathname.isChildOf("/home");
//                }
//            });
//            final VFile[] t = n.listFiles("/etc");
//            for (VFile t1 : t) {
//                System.out.println(t1.getPath() + " : " + (t1.exists() ? "exists" : "does not exist") + " : " + t1.length() + " : " + t1.lastModified());
//            }
        } catch (Exception ex) {
            Logger.getLogger(VFS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
