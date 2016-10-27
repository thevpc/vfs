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
public interface VFileVisitor {

    /**
     * called when file is visited
     *
     * @param pathname The abstract pathname to be tested
     * @return  <code>true</code> if visitor must continue visiting
     */
    boolean visit(VFile pathname);

}
