package com.sakthi.propertyinspector.data;

import android.util.Log;

import com.sakthi.propertyinspector.util.FileHandler;
import com.sakthi.propertyinspector.util.ParseUtil;

import java.util.ArrayList;

/**
 * Created by sakthivel on 5/27/2016.
 */
public class RoomItem {

    private int mRoomId;
    private int mInventoryId;
    private int mItemId;
    private String mDesc="";
    private String mMoreInfo="";
    private int mConditionId;
    private int mColorId;
    private String mNote="";

    private int mNewInventoryItemIndi;
    private int mIndiCondTask;
    private int mIndiDel;
    private String mCleaningNote="";
    private String mMake="";
    private String mModel="";


    private ArrayList<Question> mQuestions;
    private ArrayList<PhotoData> mPhotosList;
    private PropertyInfo.QuestionStatics mStatics;

    public RoomItem(String[] parsedData){

        mRoomId= ParseUtil.getIntFromString(parsedData[3],0);
        mInventoryId=ParseUtil.getIntFromString(parsedData[4],0);
        mItemId=ParseUtil.getIntFromString(parsedData[5],0);
        mDesc=ParseUtil.getStringFromArray(parsedData,6);
        mMoreInfo= ParseUtil.getStringFromArray(parsedData,7);
        mConditionId= ParseUtil.getIntFromString(parsedData[8],0);
        mColorId= ParseUtil.getIntFromString(parsedData[9],0);
        mNote=ParseUtil.getStringFromArray(parsedData,10);
        mNewInventoryItemIndi=ParseUtil.getIntFromString(parsedData[11],0);
        mIndiCondTask=ParseUtil.getIntFromString(parsedData[12],0);
        mIndiDel=ParseUtil.getIntFromString(parsedData[13],0);
        mCleaningNote=ParseUtil.getStringFromArray(parsedData,14);
        mMake=ParseUtil.getStringFromArray(parsedData,15);
        mModel=ParseUtil.getStringFromArray(parsedData,16);
        mQuestions=new ArrayList<>();

        mStatics=new PropertyInfo.QuestionStatics();
        mPhotosList=new ArrayList<>();

    }

    public RoomItem(int roomId){
        mRoomId=roomId;
        mQuestions=new ArrayList<>();
        mStatics=new PropertyInfo.QuestionStatics();
        mPhotosList=new ArrayList<>();
        mNewInventoryItemIndi=1;
    }

    public RoomItem(int roomId,int itemId){
        this(roomId);
        mItemId=itemId;
    }


    public RoomItem copy(){

        RoomItem roomItem=new RoomItem(mRoomId,mItemId);
        roomItem.mInventoryId=mInventoryId;
        roomItem.mDesc=new String(mDesc);
        roomItem.mMoreInfo=new String(mMoreInfo);
        roomItem.mConditionId=mConditionId;
        roomItem.mColorId=mColorId;
        roomItem.mNote=new String(mNote);
        roomItem.mIndiCondTask=mIndiCondTask;
        roomItem.mIndiDel=mIndiDel;
        roomItem.mCleaningNote=new String(mCleaningNote);
        roomItem.mMake=new String(mMake);
        roomItem.mModel=new String(mModel);

        for(Question qst:mQuestions)roomItem.addQuestion(qst.copy(true));
        roomItem.mPhotosList=mPhotosList;

        return roomItem;
    }

    public int getRoomId(){
        return mRoomId;
    }
    public int getItemId(){return mItemId;}

    public String getName(){return mDesc;}
    public String getNote(){return mNote;}
    public String getCleaningNote(){return mCleaningNote;}

    public String getMake(){return mMake;}
    public String getModel(){return mModel;}
    public String getType(){return mMoreInfo;}

    public int getConditionId(){return mConditionId;}
    public int getColorId(){return mColorId;}

    public void setInventoryId(int inventoryId){
        mInventoryId=inventoryId;
    }

    public int getInventoryId(){return mInventoryId;}

    public void setCondIndiTask(int task){
        mIndiCondTask=task;
    }
    public boolean getCondIndiTask(){return mIndiCondTask==1;}

    public boolean isItemDeleted(){
        return mIndiDel==1;
    }

    public void setItemDeleted(boolean delete){
        mIndiDel=delete?1:0;
    }

    public void setItemId(int id){
        mItemId=id;
    }

    public void setName(String desc){
        mDesc=desc;
    }

    public void setMake(String make){
        mMake=make;
    }

    public void setModel(String model){
       mModel=model;
    }

    public void setNote(String note){
        mNote=note;
    }

    public void setType(String type){
        mMoreInfo=type;
    }

    public void setCleaningNote(String cnote){
        mCleaningNote=cnote;
    }

    public void setColor(int id){
        mColorId=id;
    }

    public void setConitionId(int id){
        mConditionId=id;
    }


    public void addQuestion(Question question){
        mQuestions.add(question);
    }
    public void setQuestions(ArrayList<Question> questions){
        mQuestions.clear();
        for(Question quest:questions)mQuestions.add(quest);
    }
    public ArrayList<Question> getQuestions(){
        return mQuestions;
    }




    public String toCSV(){


       // Client ID, Property ID, Room ID, Inventory ID, Item ID, Item Description,
        // More Info, Condition, Colour, Notes, New Inventory Item Indicator,
        // Condition Task Indicator, Delete Indicator, Cleaning Note, Make, Model, Image Name

        StringBuilder builder=new StringBuilder();
        builder.append(mRoomId).append(",");
        builder.append(mInventoryId).append(",");
        builder.append(mItemId).append(",");
        builder.append(mDesc).append(",");
        builder.append(mMoreInfo).append(",");
        builder.append(mConditionId).append(",");
        builder.append(mColorId).append(",");
        builder.append(mNote).append(",");
        builder.append(mNewInventoryItemIndi).append(",");//need to add new inventory item indicator
        builder.append(mIndiCondTask).append(",");
        builder.append(mIndiDel).append(",");
        builder.append(mCleaningNote).append(",");
        builder.append(mMake).append(",");
        builder.append(mModel).append(",");
        builder.append("").append(",");//need to query user

        return builder.toString();
    }

    public String  toCSVQuestions(int keycode,String preFix){

        //04, Client ID, Property ID, Room ID, Inventory ID, Item ID, Question No, Question, Yes, No
        StringBuilder strCsv=new StringBuilder();
        for(Question quest:mQuestions){
            strCsv.append(keycode+preFix+mRoomId+","+mInventoryId+","+mItemId+","+quest.getQuestionId()+","+quest.getQuestion()+","+(quest.getAnswer()==Question.YES?1:0)+","+(quest.getAnswer()==Question.NO?1:0)+"\n");
        }
        return strCsv.toString();
    }

    public String toCSVPhotos(int keycode,String preFix){
        StringBuilder strCsv=new StringBuilder();

        for(PhotoData data:mPhotosList){
            strCsv.append(keycode+preFix+mRoomId+","+mInventoryId+","+mItemId+","+data.getName()+"\n");
        }

        return strCsv.toString();
    }

    public PropertyInfo.QuestionStatics getStatics(){

        int noOfQuest=mQuestions.size();
        int answered=0;
        boolean isGroupQuestion = false;
        for(Question quest:mQuestions){
            if(quest.isAnswered())
            {
                answered++;
            }
            isGroupQuestion = quest.isGroupQuestion();
        }
        mStatics.set(answered,noOfQuest, isGroupQuestion);

        return mStatics;
    }

    public ArrayList<PhotoData> getPhotosList(){
        return mPhotosList;
    }

    public void addItemPhoto(PhotoData photo){
        mPhotosList.add(photo);
    }

    public int getNumberOfPhotos(){return mPhotosList.size();}


    public void updateData(RoomItem item){

        mInventoryId=item.mInventoryId;
        mDesc=item.mDesc;
        mMoreInfo=item.mMoreInfo;
        mConditionId=item.mConditionId;
        mColorId=item.mColorId;
        mNote=item.mNote;
        mIndiCondTask=item.mIndiCondTask;
        mIndiDel=item.mIndiDel;
        mCleaningNote=item.mCleaningNote;
        mMake=item.mMake;
        mModel=item.mModel;

        for(Question lquest:item.mQuestions){
            for(Question uquest:mQuestions){
                if(lquest.getQuestionId()==uquest.getQuestionId()){
                    uquest.setAnswer(lquest.getAnswer());
                }
            }
        }

    }


}
