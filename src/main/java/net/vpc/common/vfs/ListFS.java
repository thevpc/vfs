package net.vpc.common.vfs;

import java.util.NoSuchElementException;

/**
 * Created by vpc on 1/1/17.
 */
public interface ListFS extends VirtualFileSystem {
    /**
     * convenient way to add new file to the folder even if the file name already exists
     *
     * @param name          preferred file name
     * @param file          file or directory to add to the list
     * @param nameGenerator Name Generator used to generate new name. If null, default one will be used
     * @return new Name
     */
    public String addOrRename(String name, VFile file, VFileNameGenerator nameGenerator);

    /**
     * 
     * @param name file name
     * @param file file to add
     * @throws IllegalArgumentException if the file already exists
     */
    public void add(String name, VFile file) throws IllegalArgumentException;

    /**
     * 
     * @param name
     * @throws NoSuchElementException if the name does not exist
     */
    public void remove(String name) throws NoSuchElementException;
}
