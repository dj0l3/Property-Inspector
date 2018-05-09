package com.sakthi.propertyinspector.data;

import android.util.Log;

/**
 * Created by sakthivel on 5/23/2016.
 */
public class RoomType {

    private int mId;
    private String mType;

    public RoomType(int id,String type){
        mId=id;
        mType=type;
        Log.e("Room Type",mId+" : "+mType);
    }

    public int getId(){
        return mId;
    }

    public String getType(){
        return mType;
    }

}
