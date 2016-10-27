/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.vfs.impl;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

/**
 * @author taha.bensalah@gmail.com
 */
public class ContentTypeUtils {
    private static MimetypesFileTypeMap CONTENT_TYPES_MAP = new MimetypesFileTypeMap();

    public static String probeContentType(String fileName) {
        String contentType = null;
        try {
            contentType = CONTENT_TYPES_MAP.getContentType(fileName);
        } catch (Exception ex) {
            //ignore
        }
        String fileNameLower = fileName.toLowerCase();
        if (contentType == null || "application/octet-stream".equals(contentType)) {
            if (fileNameLower.endsWith(".txt")) {
                contentType = "text/plain";
            } else if (fileNameLower.endsWith(".html")) {
                contentType = "text/html";
            } else if (fileNameLower.endsWith(".xml")) {
                contentType = "text/xml";
            } else if (fileNameLower.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (fileNameLower.endsWith(".css")) {
                contentType = "text/css";
            } else if (fileNameLower.endsWith(".js")) {
                contentType = "text/javascript";
            } else {
                contentType = "application/octet-stream";

            }
        }
        return contentType;
    }

    public static String probeContentType(File file) {
        String contentType = null;
        try {
            contentType = Files.probeContentType(file.toPath());
        } catch (IOException ex) {
            //ignore
        }
        if (contentType == null || "application/octet-stream".equals(contentType)) {
            return probeContentType(file.getName());
        }
        return contentType;
    }

    public static String probeContentType(URL url) {
        try {
            if (url.getProtocol().equals("file")) {
                File f;
                f = new File(url.toURI());
                return probeContentType(f);
            }
            File temp = File.createTempFile("t", "r");
            Files.copy(url.openStream(), temp.toPath());
            String ct = Files.probeContentType(temp.toPath());
            temp.delete();
            return ct;
        } catch (Exception ex) {
            return null;
        }
    }
}
