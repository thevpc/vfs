/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class FileName {

    private final String simpleName;
    private final String longName;
    private final String shortExtension;
    private final String longExtension;

    public FileName(String simpleName, String longName, String shortExtension, String longExtension) {
        this.simpleName = simpleName;
        this.longName = longName;
        this.shortExtension = shortExtension;
        this.longExtension = longExtension;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getShortExtension() {
        return shortExtension;
    }

    public String getLongExtension() {
        return longExtension;
    }

    public boolean hasName() {
        return simpleName == null || simpleName.isEmpty();
    }

    public boolean hasExtension() {
        return shortExtension == null || shortExtension.isEmpty();
    }

    public String getLongName() {
        return longName;
    }

}
