package com.sakthi.propertyinspector.data;

/**
 * Created by sakthivel on 6/3/2016.
 */
public class PhotoData {

    private long mPhotoId;
    private String mImageName;
    private String mImagePath;


    public PhotoData(long id,String imageName,String imagePath){
        mPhotoId=id;
        mImageName=imageName;
        mImagePath=imagePath;
    }

    public long getId(){
        return mPhotoId;
    }

    public String getName(){return mImageName;}

    public String getImagePath(){return mImagePath;}

}
