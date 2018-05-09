package com.sakthi.propertyinspector.data;

import java.io.File;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by Satheesh on 25/06/17.
 * Sanghish IT Solutions
 * satheesh@sanghish.com
 */
public class SearchResultFile {

    public String name;
    public long lastModified;
    public String path;
    public String folderPath;
    public boolean isLocateInFTP;
    public boolean isDir;
    public boolean isAddedToDownload;
    public boolean enabled = false;
    public long fileSize;
}
