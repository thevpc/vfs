/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VirtualFileACL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.vpc.common.vfs.MountableFS;
import net.vpc.common.vfs.VFSSecurityManager;
import net.vpc.common.vfs.VFile;
import net.vpc.common.vfs.VFileFilter;
import net.vpc.common.vfs.VFileType;
import net.vpc.common.vfs.VirtualFileSystem;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class DefaultMountableVFS extends AbstractDelegateVirtualFileSystem implements MountableFS {

    private List<MountPoint> mounts = new ArrayList<MountPoint>();
    private Map<String, MountPoint> mountsByFirstPath = new LinkedHashMap<String, MountPoint>();
    private VFSSecurityManagerImpl sm = new VFSSecurityManagerImpl();

    public DefaultMountableVFS(String id) {
        super(id);
    }

    @Override
    public void mount(String path, VirtualFileSystem subfs) throws IOException {
        path = normalizeVirtualPath(path);
        List<String> p = VFSUtils.toPathParts(path, true);
        String parent = null;
        if (p.size() > 0) {
            parent = toPathString(p, 1, p.size());
            VFile pp = get(parent);
            if (pp == null || !pp.isDirectory() || !pp.exists()) {
                throw new IOException("Unable to mount " + path + ". Parent path not found " + parent);
            }
            MountPoint mp = new MountPoint(path, parent, subfs, VFSUtils.toPathParts(path, true));
            mounts.add(0, mp);
            mountsByFirstPath.put(p.get(0), mp);
        } else {
            MountPoint mp = new MountPoint("/", "/", subfs, VFSUtils.toPathParts(path, true));
            mounts.add(0, mp);
            mountsByFirstPath.put("/", mp);
            //mount at root!
        }
    }

    @Override
    public VFSSecurityManager getSecurityManager() {
        return sm;
    }

    @Override
    public VFile[] listFiles(String path, VFileFilter fileFilter) {
        path = normalizeVirtualPath(path);

        ArrayList<VFile> r = new ArrayList<>();
        if ("/".equals(path)) {
            LinkedHashMap<String, VFile> rrr = new LinkedHashMap<>();
            MountPoint rmp = mountsByFirstPath.get("/");
            if (rmp != null) {
                for (VFile lf : rmp.subfs.listFiles("/")) {
                    rrr.put(lf.getName(), get("/" + lf.getName()));
                }
            }
            for (Map.Entry<String, MountPoint> entry : mountsByFirstPath.entrySet()) {
                if (!entry.getKey().equals("/")) {
                    rrr.put(entry.getKey(), get("/" + entry.getKey()));
                }
            }

            return rrr.values().toArray(new VFile[rrr.size()]);
        }

        MountPointAndFile info = getMountPointAndFile(path);
        if (info == null) {
            if ("/".equals(path)) {
                //check mount on root
                for (MountPoint mount : mounts) {
                    if (mount.parentPath != null && mount.parentPath.equals(path)) {
                        r.add(get(mount.getPath()));
                    }
                }
            }
            return r.toArray(new VFile[r.size()]);
        }
        VFile[] d = info.file.listFiles();
        for (VFile d1 : d) {
            VFile ff = get(info.mountPoint.getPath(), d1.getPath());
            if (fileFilter == null || fileFilter.accept(ff)) {
                r.add(ff);
            }
        }
        for (MountPoint mount : mounts) {
            if (mount.parentPath != null && mount.parentPath.equals(path)) {
                r.add(get(mount.getPath()));
            }
        }
        return r.toArray(new VFile[r.size()]);
    }

    public void umount(String path) throws IOException {
        path = normalizeVirtualPath(path);
        for (int i = 0; i < mounts.size(); i++) {
            MountPoint m = mounts.get(i);
            if (m.getPath().equals(path)) {
                mounts.remove(i);
                return;
            }
        }
        throw new IllegalArgumentException("Mount point not found " + path);
    }

    @Override
    public boolean exists(String path) {
        if ("/".equals(path)) {
            return true;
        }
        return super.exists(path);
    }

    @Override
    public VFile getDelegate(String f) {
        MountPointAndFile ff = getMountPointAndFile(f);
        if (ff == null) {
            return null;
        }
        return ff.file;
    }

    @Override
    public VFile getBase(String path, String vfsId) {
        if (vfsId == null || vfsId.length() == 0 || vfsId.equalsIgnoreCase(getId())) {
            return get(path);
        }
        VFile t = getDelegate(path);
        if (t != null) {
            return t.getBaseFile(vfsId);
        }
        return null;
    }

    protected MountPointAndFile getMountPointAndFile(String f) {
        List<String> p0 = VFSUtils.toPathParts(f, true);
//        List<MountPoint> mounts2 = new ArrayList<>(mounts);
        if (p0.isEmpty()) {
            MountPoint y2 = mountsByFirstPath.get("/");
            if (y2 != null) {
                return new MountPointAndFile(y2, y2.getSubfs().get("/"));
            }
            return null;
//            //look for root
//            for (int j = 0; j < mounts2.size(); j++) {
//                MountPoint mm = mounts2.get(j);
//                if (mm.getPathParts().size() == 0) {
//                    return new MountPointAndFile(mm, mm.getSubfs().get("/"));
//                }
//            }
        }
        MountPoint y2 = mountsByFirstPath.get(p0.get(0));
        if (y2 != null) {
            return new MountPointAndFile(y2, y2.getSubfs().get(toPathString(p0, 1, p0.size())));
        }
        y2 = mountsByFirstPath.get("/");
        if (y2 != null) {
            return new MountPointAndFile(y2, y2.getSubfs().get(toPathString(p0, 0, p0.size())));
        }
//        for (int i = 0; i < p0.size(); i++) {
//            if (mounts2.size() == 1) {
//                return new MountPointAndFile(
//                        mounts2.get(0),
//                        mounts2.get(0).getSubfs().get(toPathString(p0, i, p0.size()))
//                );
//            }
//            String y = p0.get(i);
//            for (int j = mounts2.size() - 1; j >= 0; j--) {
//                MountPoint mm = mounts2.get(j);
//                if (i < mm.getPathParts().size()) {
//                    String x = mm.getPathParts().get(i);
//                    if (x.equals(y)) {
//                        //ok
//                    } else {
//                        mounts2.remove(j);
//                    }
//                } else {
//                    mounts2.remove(j);
//                }
//            }
//        }
//        if (mounts2.size() == 1) {
//            return new MountPointAndFile(
//                    mounts2.get(0),
//                    mounts2.get(0).getSubfs().get("/")
//            );
//        }
        return null;
    }

    @Override
    public VFile[] getRoots() {
        return new VFile[]{new DefaultFile("/", this)};
    }

    protected static class MountPointAndFile {

        MountPoint mountPoint;
        VFile file;

        public MountPointAndFile(MountPoint mountPoint, VFile file) {
            this.mountPoint = mountPoint;
            this.file = file;
        }

    }

    @Override
    public String toString() {
        return "MountableFS{" + mounts + '}';
    }

    public static class MountPoint {

        private String parentPath;
        private String path;
        private List<String> pathParts;
        private VirtualFileSystem subfs;

        public MountPoint(String path, String parentPath, VirtualFileSystem subfs, List<String> pathParts) {
            this.path = path;
            this.subfs = subfs;
            this.pathParts = pathParts;
            this.parentPath = parentPath;
        }

        public String getPath() {
            return path;
        }

        public VirtualFileSystem getSubfs() {
            return subfs;
        }

        public List<String> getPathParts() {
            return pathParts;
        }

        @Override
        public String toString() {
            return "MountPoint{" + path + "=>" + subfs + '}';
        }

    }

    private class VFSSecurityManagerImpl implements VFSSecurityManager {

        public VFSSecurityManagerImpl() {
        }

        @Override
        public boolean isAllowedCreateChild(String path, VFileType type, String user) {
            MountPointAndFile d = getMountPointAndFile(path);
            if (d == null) {
                return false;
            }
            return d.mountPoint.subfs.getSecurityManager().isAllowedCreateChild(d.file.getPath(), type, user);
        }

        @Override
        public boolean isAllowedRemoveChild(String path, VFileType type, String user) {
            MountPointAndFile d = getMountPointAndFile(path);
            if (d == null) {
                return false;
            }
            return d.mountPoint.subfs.getSecurityManager().isAllowedRemoveChild(d.file.getPath(), type, user);
        }

        @Override
        public boolean isAllowedUpdateChild(String path, VFileType type, String user) {
            MountPointAndFile d = getMountPointAndFile(path);
            if (d == null) {
                return false;
            }
            return d.mountPoint.subfs.getSecurityManager().isAllowedUpdateChild(d.file.getPath(), type, user);
        }

        @Override
        public boolean isAllowedList(String path, String user) {
            MountPointAndFile d = getMountPointAndFile(path);
            if (d == null) {
                return false;
            }
            return d.mountPoint.subfs.getSecurityManager().isAllowedList(d.file.getPath(), user);
        }

        @Override
        public boolean isAllowedRemove(String path, String user) {
            MountPointAndFile d = getMountPointAndFile(path);
            if (d == null) {
                return false;
            }
            return d.mountPoint.subfs.getSecurityManager().isAllowedRemove(d.file.getPath(), user);
        }

        @Override
        public boolean isAllowedRead(String path, String user) {
            MountPointAndFile d = getMountPointAndFile(path);
            if (d == null) {
                return false;
            }
            return d.mountPoint.subfs.getSecurityManager().isAllowedRead(d.file.getPath(), user);
        }

        @Override
        public boolean isAllowedWrite(String path, String user) {
            MountPointAndFile d = getMountPointAndFile(path);
            if (d == null) {
                return false;
            }
            return d.mountPoint.subfs.getSecurityManager().isAllowedWrite(d.file.getPath(), user);
        }
    }

//    @Override
//    public VirtualFileACL getACL(String path) {
//        VFile t = getDelegate(path);
//        if (t == null) {
//            if ("/".equals(path)) {
//                return DefaultVirtualFileACL.READ_ONLY;
//            }
//        }
//        return t == null ? null : t.getACL();
//    }
}
