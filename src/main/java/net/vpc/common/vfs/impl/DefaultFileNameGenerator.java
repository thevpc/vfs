package net.vpc.common.vfs.impl;

import net.vpc.common.vfs.VFileNameGenerator;

/**
 * Created by vpc on 1/1/17.
 */
public class DefaultFileNameGenerator implements VFileNameGenerator{
    public static final VFileNameGenerator INSTANCE=new DefaultFileNameGenerator();
    @Override
    public String generateFileName(String baseName, int index) {
        if(index<=1){
            return baseName;
        }
        return baseName+" "+index;
    }
}
