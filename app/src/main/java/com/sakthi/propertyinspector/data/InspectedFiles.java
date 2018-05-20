package com.sakthi.propertyinspector.data;

/**
 * Created by Satheesh on 12/06/17.
 * Sanghish IT Solutions
 * satheesh@sanghish.com
 */
public class InspectedFiles {

    public PropertyInfo propertyInfo;
    public String filePath;
    private int uploadedPhotos;

    public void setUploadedPhotos(int photos) {
        uploadedPhotos = photos;
    }

    public int getUploadedPhotos() {
        return uploadedPhotos;
    }
}
