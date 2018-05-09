package com.sakthi.propertyinspector.data;

import com.sakthi.propertyinspector.util.ParseUtil;

/**
 * Created by sakthivel on 5/12/2016.
 */
public class Area {

    private int mClientId;
    private int mPropertyId;

    private int mRoomId;
    private String mRoomAlias;
    private int mRoomType;
    private int mRoomNo;
    private int mStatusIndx;

    private boolean mIsNewRoom;

    private PropertyInfo.QuestionStatics mInsStatic;

    public Area(String aUnparsedData){

        String[] splitted=aUnparsedData.split(",");
        mClientId= ParseUtil.getIntFromString(splitted[1],-1);
        mPropertyId= ParseUtil.getIntFromString(splitted[2],-1);
        mRoomId= ParseUtil.getIntFromString(splitted[3],-1);
        mRoomAlias=splitted[4];
        mRoomType= ParseUtil.getIntFromString(splitted[5],-1);
        mRoomNo= ParseUtil.getIntFromString(splitted[6],-1);
        mIsNewRoom=ParseUtil.getIntFromString(splitted[7],0)==1?true:false;

    }

    public Area(String[] parsedArray){

        mClientId= ParseUtil.getIntFromString(parsedArray[1],-1);
        mPropertyId= ParseUtil.getIntFromString(parsedArray[2],-1);
        mRoomId= ParseUtil.getIntFromString(parsedArray[3],-1);
        mRoomAlias=parsedArray[4];
        mRoomType= ParseUtil.getIntFromString(parsedArray[5],-1);
        mRoomNo= ParseUtil.getIntFromString(parsedArray[6],-1);
        mIsNewRoom=ParseUtil.getIntFromString(parsedArray[7],0)==1?true:false;

        mInsStatic=new PropertyInfo.QuestionStatics();
    }

    public Area(int roomId,int roomNo){
        mRoomId=roomId;
        mRoomNo=roomNo;
        mInsStatic=new PropertyInfo.QuestionStatics();
    }

    public void setRoomNo(int roomNo){
        mRoomNo=roomNo;
    }
    public void setRoomType(int typeIndx){
        mRoomType=typeIndx;
    }
    public void setStatus(int status){
        mStatusIndx=status;
    }
    public void setNewRoom(boolean newRoom){
        mIsNewRoom=newRoom;
    }

    public void setAlias(String name){
        mRoomAlias=name;
    }

    public int getRoomId(){
        return mRoomId;
    }

    public String getAliasName(){
        return mRoomAlias;
    }

    public int getRoomNo(){
        return mRoomNo;
    }

    public int getTypeIndx(){
        return mRoomType;
    }

    public int getStatusIndx(){
        return mStatusIndx;
    }

    public String toCSVArea(){
        //02, Client ID, Property ID, Room ID, Room Alias, Room Type, Room Number, New Room Indicator
        return mRoomId+","+mRoomAlias+","+mRoomType+","+mRoomNo+","+(mIsNewRoom?1:0);
    }

    public void setItemsStaticInfo(int qAnswered,int totQuestions, boolean isGroupQuestion){
        mInsStatic.set(qAnswered,totQuestions, isGroupQuestion);
    }

    public PropertyInfo.QuestionStatics getStatics(){
        return mInsStatic;
    }

}
