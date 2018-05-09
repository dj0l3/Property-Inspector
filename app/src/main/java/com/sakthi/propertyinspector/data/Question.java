package com.sakthi.propertyinspector.data;

import com.sakthi.propertyinspector.util.ParseUtil;

/**
 * Created by sakthivel on 5/30/2016.
 */
public class Question {

    public static final int YES=1;
    public static final int NO=0;

    private int mQuestionId;
    private String mQuestion;
    private int mAnswer;
    private boolean isGroupQuestion;


    private int mDefValue;
    private int mExtraPhotoCount;

    public Question(String[] parsedData){

        mQuestionId= ParseUtil.getIntFromString(parsedData[6],0);
        mQuestion=ParseUtil.getStringFromArray(parsedData,7);

        int yesAnswer=ParseUtil.getIntFromString(parsedData[8],0);
        int noAnswer=ParseUtil.getIntFromString(parsedData[9],0);

        mAnswer=((yesAnswer|noAnswer)==0?-1:(yesAnswer==1)?1:0);
        isGroupQuestion = parsedData.length == 10 && ParseUtil.getIntFromString(parsedData[9], 0) > 0;

    }

    private Question(){}

    public Question(int questionId,String quest,int defValue,int photoExtra, boolean isGroupQuestion){

        mDefValue=defValue;
        mQuestionId= questionId;
        mQuestion=quest;
        mExtraPhotoCount=photoExtra;
        this.isGroupQuestion = isGroupQuestion;
    }

    public Question copy(boolean isUpdatedAnswer){

        Question quest=new Question();
        quest.mQuestionId=mQuestionId;
        quest.mQuestion=mQuestion;
        quest.mAnswer=isUpdatedAnswer?mAnswer:-1;
        quest.isGroupQuestion = isGroupQuestion;

        return quest;
    }

    public int getQuestionId(){
        return mQuestionId;
    }

    public String getQuestion(){
        return mQuestion;
    }

    public void setQuestion(String question){
        mQuestion=question;
    }

    public boolean isAnswered(){return mAnswer!=-1;}

    public int getExtraPhotoCount(){return mExtraPhotoCount;}
    public int getDefAnswer(){return mDefValue;}

    public int getAnswer(){return mAnswer;}
    public void setAnswer(int answer){
        mAnswer=answer;
    }


    public String  toCSV(){
        return mQuestionId+","+mDefValue+","+mQuestion+","+mExtraPhotoCount+"\n";
    }

    public boolean isGroupQuestion(){return isGroupQuestion;}

}
