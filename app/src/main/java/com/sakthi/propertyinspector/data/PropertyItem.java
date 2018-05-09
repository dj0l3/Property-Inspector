package com.sakthi.propertyinspector.data;

import android.util.Log;

import com.sakthi.propertyinspector.util.FileHandler;
import com.sakthi.propertyinspector.util.ParseUtil;

import java.util.ArrayList;

/**
 * Created by sakthivel on 6/6/2016.
 */
public class PropertyItem {
    // 24, Client ID, Item ID, Description, Location Indicator, Question Number Indicator (1-25), New Item
    private int mItemId;
    private String mDesc;
    private int mIsNewItem;
    private int mNumberOfPhotos;

    private ArrayList<Integer> mLocationIndicators;
    private ArrayList<Integer> mQuestions;

    public PropertyItem(String[] parsedArray){
        mQuestions=new ArrayList<>();

        mItemId= ParseUtil.getIntFromString(parsedArray[2],0);
        mDesc=ParseUtil.getStringFromArray(parsedArray,3);
        mLocationIndicators=new ArrayList<>();

        //location indicator index 4 to 20 deprecated
        //location indicator index 4 to 21 deprecated totally 18 roomItems
        for(int i=4;i<=21;i++){
            int indicator=ParseUtil.getIntFromString(parsedArray[i],0);
            mLocationIndicators.add(indicator);
        }
        //question indicator index 21 to 45 deprecated

        //question indicator index 22 to 46 deprecated totally 25 questions
        for(int i=22;i<=46;i++){
            int questNo=i-21;
            int isQuestAvail=ParseUtil.getIntFromString(parsedArray[i],0);
            if(isQuestAvail==1)mQuestions.add(questNo);
        }

        mIsNewItem=ParseUtil.getIntFromString(parsedArray[47],0);
        mNumberOfPhotos= ParseUtil.getIntFromString(parsedArray[48],0);
        Log.e("Photos:"+mNumberOfPhotos," "+parsedArray[47]);

    }

    public PropertyItem(int itemId){
        mItemId=itemId;
        mQuestions=new ArrayList<>();
    }

    public ArrayList<Integer> getQuestionsIndx(){
        return mQuestions;
    }

    public int getItemId(){return mItemId;}
    public String getDesc(){ return  mDesc;}

    public int getLocationIndicator(int indx){return mLocationIndicators.get(indx);}
    public boolean isNewItem(){return mIsNewItem==1;}

    public int getMaxNumberOfPhotos(){
        return mNumberOfPhotos;
    }

    public String toCSV(){
        StringBuilder csvText=new StringBuilder();

        csvText.append(mItemId).append(",");
        csvText.append(mDesc).append(",");
        int size=mLocationIndicators.size();
        for(int i=0;i<size;i++){
            csvText.append(mLocationIndicators.get(i)).append(",");
        }

        for(int i=1;i<=25;i++){
            boolean idFound=false;
            for(int id:mQuestions){
               if(id==i){
                    idFound=true;
                    break;
                }
            }
            csvText.append(idFound?1:0);
            csvText.append(",");
        }

        csvText.append(mIsNewItem).append(",");
        csvText.append(mNumberOfPhotos);
        csvText.append("\n");

        return csvText.toString();
    }


}
