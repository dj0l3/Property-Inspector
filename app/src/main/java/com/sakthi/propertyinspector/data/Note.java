package com.sakthi.propertyinspector.data;

/**
 * Created by sakthivel on 5/18/2016.
 */
public class Note {

    private String mNoteId;
    private String mNoteDesc=" ";


    public Note(){}

    public Note(String id,String note){
      setInfo(id,note);
    }

    public void setInfo(String id,String note){
        mNoteId=id;
        mNoteDesc=note;
    }

    public String getId(){return mNoteId;}
    public String getDesc(){return mNoteDesc;}

}
