package net.vpc.common.vfs;

/**
 * Created by vpc on 1/1/17.
 */
public interface VFileNameGenerator {
    public String generateFileName(String baseName, int index);
}
