package com.sakthi.propertyinspector.data;

import android.util.Log;

import com.sakthi.propertyinspector.util.ParseUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sakthivel on 5/11/2016.
 */
public class PropertyInfo {
    
    private String mAddress;
    private String mPostalCode;

    private int mClientId;
    private int mPropertyId;
    private int mAlarmNumber;
    private int mUniqueKeyNo;

    private Note mOwenerNote;
    private Note mPropertyNote;

    private ArrayList<Area> mAreaList;
    private ArrayList<RoomItem> mItemList;
    private ArrayList<Question> mDefQuestions;

    private HashMap<Integer,RoomType> mRoomTypes;
    private HashMap<Integer,String> mConditions;
    private HashMap<Integer,String> mColors;

    private ArrayList<PropertyItem> mPropertyItems;

    private String[] STATUS_ARRAY={"Common Area","Void Area"};

    private int mRoomNewNumber;
    private int mRoomNewId;

    public PropertyInfo (){

        mAreaList=new ArrayList<>();
        mItemList=new ArrayList<>();

        mOwenerNote=new Note();
        mPropertyNote=new Note();
        mRoomTypes=new HashMap<>();
        mColors=new HashMap<>();
        mConditions=new HashMap<>();
        mDefQuestions=new ArrayList<>();
        mPropertyItems=new ArrayList<>();

    }


    private String[] arrayFromMap(HashMap<Integer,String> map){

        int numbers=map.size();
        String[] valueArr=new String[numbers];
        Iterator<Integer> iterator=map.keySet().iterator();
        int indx=0;
        while (iterator.hasNext()){
            valueArr[indx++]=map.get(iterator.next());
        }
        Arrays.sort(valueArr);

        return valueArr;
    }

    public String[] getConditions(){
        return arrayFromMap(mConditions);
    }

    public String[] getColors(){
        return arrayFromMap(mColors);
    }

    public int getItemPosition(String[] items,String sText){
        return Arrays.binarySearch(items,sText);
    }

    public int getConditionId(String text){

        Iterator<Integer> keys=mConditions.keySet().iterator();

        while (keys.hasNext()){
            int id=keys.next();
            if(mConditions.get(id).equals(text))return id;
        }

        return -1;
    }

    public int getColorsId(String text) {

        Iterator<Integer> keys = mColors.keySet().iterator();

        while (keys.hasNext()) {
            int id = keys.next();
            if (mColors.get(id).equals(text)) return id;
        }

        return -1;
    }


    public void addCondition(String[] parsedArray){
        int id=ParseUtil.getIntFromString(parsedArray[2],0);
        String txtCond=ParseUtil.getStringFromArray(parsedArray,3);
        mConditions.put(id,txtCond);
    }

    public void addColor(String[] parsedArray){
        int id=ParseUtil.getIntFromString(parsedArray[2],0);
        String txtColor=ParseUtil.getStringFromArray(parsedArray,3);
        mColors.put(id,txtColor);
    }

    public String getCondition(int id){
        return mConditions.get(id);
    }

    public String getColor(int id){
        return mColors.get(id);
    }


    public void addArea(String[] parsedArray){

    }

    public ArrayList<Area> getAreaList(){
        return mAreaList;
    }

    public ArrayList<RoomItem> getAreaItems(int roomId){
        ArrayList<RoomItem> roomItems=new ArrayList<>();

        for(RoomItem item:mItemList)if(roomId==item.getRoomId()&&!item.isItemDeleted()){
            roomItems.add(item);
        }
            ParseUtil.sortRooItem(roomItems);
        return  roomItems;
    }

    public ArrayList<RoomItem> getAreaItemsIncludeDeleted(int roomId){
        ArrayList<RoomItem> roomItems=new ArrayList<>();

        for(RoomItem item:mItemList)if(roomId==item.getRoomId()){
            roomItems.add(item);
        }
        return  roomItems;
    }

    /*public RoomItem getRoomItemById(int roomId,int itemId){

        for(RoomItem roomItem:mItemList)if(roomId==roomItem.getRoomId()&&itemId==roomItem.getItemId())return roomItem;
        return null;

    }*/

    public RoomItem getRoomItemByInvId(int invId){

        for(RoomItem roomItem:mItemList)if(invId==roomItem.getInventoryId())return roomItem;
        return null;

    }

    public void addPropertyItem(PropertyItem item){
        mPropertyItems.add(item);
    }

    public ArrayList<PropertyItem> getPropertyItems(){
        return mPropertyItems;
    }

    public void addRoomItem(RoomItem item){
        mItemList.add(item);
    }

    public void removeRoomItem(RoomItem item){
        mItemList.remove(item);
    }

    private ArrayList<RoomItem> mNewlyAddedItems=new ArrayList<>();

    public ArrayList<RoomItem> getNewAddedItems(){
        return mNewlyAddedItems;
    }

    public void clearNewItems(){
        mNewlyAddedItems.clear();
    }

    public void addNewRoomItem(RoomItem roomItem){

        mNewlyAddedItems.clear();
        //int newItemId=0;
        int newInventoryId=0;
        for(RoomItem rItem:mItemList){
           // newItemId=Math.max(newItemId,rItem.getItemId());
            newInventoryId=Math.max(newInventoryId,rItem.getInventoryId());
        }
       // newItemId+=1;
        newInventoryId+=1;
        roomItem.setInventoryId(newInventoryId);
        //roomItem.setItemId(newItemId);
        mItemList.add(roomItem);
        mNewlyAddedItems.add(roomItem);

    }

    public void addRoomType(String[] parsedArray){

        int id=Integer.parseInt(parsedArray[2]);
        String type=ParseUtil.getStringFromArray(parsedArray,3);
        if(type.trim().length()<1)return;//type=" ";
        RoomType roomType=new RoomType(id,type);
        mRoomTypes.put(id,roomType);

    }

   /* public void addItemQuestion(Question question,int roomId,int itemId){

        RoomItem item=getRoomItemById(roomId,itemId);
        item.addQuestion(question);

    }*/

    public void addItemQuestion(Question question,int invId){

        RoomItem item=getRoomItemByInvId(invId); //getRoomItemById(roomId,itemId);
        item.addQuestion(question);

    }


    public void addDefaultQuestion(Question question){
        mDefQuestions.add(question);
    }

    public ArrayList<Question> getDefaultQuestions(){
        return mDefQuestions;
    }

    public Area getNewlyAddedArea(){
        return mAreaList.get(mAreaList.size()-1);
    }


    public int getRoomTypeIndx(String roomType){
        Iterator<Integer> iterator=mRoomTypes.keySet().iterator();
        while (iterator.hasNext()){
            RoomType type=mRoomTypes.get(iterator.next());
            if(type.getType().equals(roomType))return type.getId();
        }
        return 0;
    }


    public PropertyItem getPropItemById(int itemId){
        for(PropertyItem item:mPropertyItems)if(item.getItemId()==itemId)return item;
        return null;
    }


    public void addArea(Area aArea){
        mAreaList.add(aArea);

        if(aArea.getRoomNo()>=mRoomNewNumber){
            mRoomNewNumber=aArea.getRoomNo()+1;
        }

        if(aArea.getRoomId()>=mRoomNewId){
            mRoomNewId=aArea.getRoomId()+1;
        }
    }

    public Area getArea(int roomId){

        for(Area area:mAreaList){
            if(area.getRoomId()==roomId)return area;
        }
        return null;
    }

    public Area getNewArea(){

        Area area=new Area(mRoomNewId,mRoomNewNumber);
        area.setNewRoom(true);
        return area;

    }

    public void setBasicInfo(String[] parsedArray){
        mClientId= ParseUtil.getIntFromString(parsedArray[1],-1);
        mPropertyId= ParseUtil.getIntFromString(parsedArray[2],-1);
        mAlarmNumber= ParseUtil.getIntFromString(parsedArray[3],-1);
        mUniqueKeyNo= ParseUtil.getIntFromString(parsedArray[4],-1);
        mAddress=ParseUtil.getStringFromArray(parsedArray,5);//parsedArray[5];
        mPostalCode=ParseUtil.getStringFromArray(parsedArray,6);//parsedArray[6];
    }

    public String[] getRoomTypes(){
        String[] types=new String[mRoomTypes.size()];
        Iterator<Integer> iterator=mRoomTypes.keySet().iterator();
        int indx=0;
        while (iterator.hasNext()){
            RoomType type=mRoomTypes.get(iterator.next());
            types[indx++]=type.getType();
        }
        Arrays.sort(types);

        return types;
    }
    public String[] getStatusArray(){return STATUS_ARRAY;}

    public String getOwnerInstruction(){return mOwenerNote.getDesc();}

    public void setOwnerInst(String[] parsedArr){
       mOwenerNote.setInfo(ParseUtil.getStringFromArray(parsedArr,3),ParseUtil.getStringFromArray(parsedArr,4));
    }

    public String getPropertyInstruction(){return mPropertyNote!= null ? mPropertyNote.getDesc() : "";}
    public void setPropertyInst(String[] parsedArr){
       mPropertyNote.setInfo(ParseUtil.getStringFromArray(parsedArr,3),ParseUtil.getStringFromArray(parsedArr,4));
    }

    public String getAddress(){return mAddress;}
    public String getPostalCode(){return mPostalCode;}
    public int getAlarmNumber(){return mAlarmNumber;}
    public int getUniqueKey(){return mUniqueKeyNo;}

    public ArrayList<RoomItem> getRoomItems(){return mItemList;}

    public String getRoomType(int typeId){
        return mRoomTypes.get(typeId) != null ? mRoomTypes.get(typeId).getType() : "";
    }
    public String getRoomStatus(int statusIndx){
        return STATUS_ARRAY[statusIndx];
    }

    public boolean isAreaAvailable(String areaName){
        for(Area area:mAreaList){
            if(areaName.equals(area.getAliasName()))return true;
        }
        return false;
    }

    public int getClientId(){return mClientId;}
    public int getPropertyId(){return mPropertyId;}

    public ArrayList<Question> getQuestionsById(ArrayList<Integer> ids){

        Log.e(ids.size()+"Question Count"," "+mDefQuestions.size());
        ArrayList<Question> questions=new ArrayList<>();
        for(Question question:mDefQuestions){
            for(int id:ids){
                if(id==question.getQuestionId()){
                    questions.add(question.copy(false));
                    break;
                }
            }
        }
        return questions;

    }

    public QuestionStatics getRoomStatics(Area area) {

        ArrayList<RoomItem> roomItems=getAreaItems(area.getRoomId());

        int totQuest=0;
        int ansQuest=0;
        boolean isGroupQuestion = false;

        for(RoomItem item:roomItems){
            QuestionStatics itemStatic=item.getStatics();
            totQuest+=itemStatic.getTotalQuestions();
            ansQuest+=itemStatic.getAnsweredQCount();
            isGroupQuestion = itemStatic.isGroupQuestion();
        }

        area.setItemsStaticInfo(ansQuest,totQuest, isGroupQuestion);

        return area.getStatics();
    }

    public void getOverallStatics(QuestionStatics qStatic,PhotoStatics pStatic){

        int totQuest=0;
        int ansQuest=0;
        int totItems=0;//mItemList.size();
        int itemsCaptured=0;
        boolean isGroupQuestion = false;

        for(RoomItem item:mItemList){
            if(item.isItemDeleted())continue;
            QuestionStatics itemStatic=item.getStatics();
            totQuest+=itemStatic.getTotalQuestions();
            ansQuest+=itemStatic.getAnsweredQCount();
            isGroupQuestion = itemStatic.isGroupQuestion();
            if(item.getNumberOfPhotos()>0)itemsCaptured++;

            totItems++;
        }

        qStatic.set(ansQuest,totQuest, isGroupQuestion);
        pStatic.set(itemsCaptured,totItems);

    }


    public static abstract class Statics{

        protected int mPercCompleted;

        public abstract void copy(Statics statics);
        public abstract int percentage();
    }
    public static class PhotoStatics extends Statics{

        private int mTotalItems;
        private int mCapturedItems;



        public void set(int capturedItems,int totalItems){
            mCapturedItems=capturedItems;
            mTotalItems=totalItems;
            mPercCompleted=(int)(mCapturedItems*100.0f/mTotalItems);
        }

        @Override
        public void copy(Statics statics) {
            PhotoStatics copyFrom=(PhotoStatics)statics;
            set(copyFrom.mCapturedItems,copyFrom.mTotalItems);
        }

        @Override
        public int percentage() {
            return mPercCompleted;
        }


        public int getItemsNeedToBeCaptured(){
            return mTotalItems-mCapturedItems;
        }

    }

    public static class QuestionStatics extends Statics{

        private int mTotalQuestions;
        private int mQuestionAnswered;
        private boolean isGroupQuestion;

        public void set(int noQAnsw,int mTotalQues, boolean isGroupQuestion){
            mTotalQuestions=mTotalQues;
            mQuestionAnswered=noQAnsw;
            this.isGroupQuestion = isGroupQuestion;
//            if (isGroupQuestion && mQuestionAnswered>0){
//                mPercCompleted = 100;
//            }else{
//                if(mTotalQuestions==0)mPercCompleted=100;
//                else mPercCompleted=(int)(mQuestionAnswered*100.0f/mTotalQuestions);
//            }
            if(mTotalQuestions==0)mPercCompleted=100;
            else mPercCompleted=(int)(mQuestionAnswered*100.0f/mTotalQuestions);

        }

        public int getTotalQuestions(){return mTotalQuestions;}
        public int getAnsweredQCount(){return mQuestionAnswered;}
        public boolean isGroupQuestion(){return isGroupQuestion;}

        @Override
        public void copy(Statics statics) {
            QuestionStatics copyFrom=(QuestionStatics)statics;
            set(copyFrom.mQuestionAnswered,copyFrom.mTotalQuestions, copyFrom.isGroupQuestion);
        }

        public int percentage(){
            return mPercCompleted;
        }

    }


    public String toCSVBasicInfo(){
        return mAlarmNumber+","+mUniqueKeyNo+","+mAddress+","+mPostalCode;
    }


    public String toCSVOwnerNote(){
        return mOwenerNote.getId()+","+mOwenerNote.getDesc();
    }
    public String toCSVPropertyNote(){
        return mPropertyNote.getId()+","+mPropertyNote.getDesc();
    }

    public String toCSVConditions(int codeId){

        Iterator<Integer> iterator=mConditions.keySet().iterator();
        StringBuilder strCsv=new StringBuilder();

        while (iterator.hasNext()){
            int id=iterator.next();
            String condition=mConditions.get(id);
            strCsv.append(codeId+","+getClientId()+","+id+","+condition+"\n");
        }
        return strCsv.toString();
    }

    public String toCSVColors(int codeId){

        Iterator<Integer> iterator=mColors.keySet().iterator();
        StringBuilder strCsv=new StringBuilder();

        while (iterator.hasNext()){
            int id=iterator.next();
            String color=mColors.get(id);
            strCsv.append(codeId+","+getClientId()+","+id+","+color+"\n");
        }
        return strCsv.toString();
    }

    public String toCSVRoomType(int codeId){

        Iterator<Integer> iterator=mRoomTypes.keySet().iterator();
        StringBuilder strCsv=new StringBuilder();

        while (iterator.hasNext()){
            int id=iterator.next();
            String type=mRoomTypes.get(id).getType();
            strCsv.append(codeId+","+getClientId()+","+id+","+type+"\n");
        }
        return strCsv.toString();
    }


    public ArrayList<PropertyItem> getRoomSepcificItems(int roomID){

        ArrayList<PropertyItem> rsItems=new ArrayList<>();//room specific items
        int roomTypeId=getArea(roomID).getTypeIndx();

        for(PropertyItem item:mPropertyItems){
            if(item.getLocationIndicator(roomTypeId)==1)rsItems.add(item);
        }
        return rsItems;
    }

    public ArrayList<PropertyItem> getRoomBasicItems(){

        ArrayList<PropertyItem> brItems=new ArrayList<>();//room specific items
        //Property item location indicator is 14 then its a basic item

        for(PropertyItem item:mPropertyItems){
            if(item.getLocationIndicator(14)==1)brItems.add(item);
        }
        return brItems;
    }

    public ArrayList<PropertyItem> getRoomOtherItems(int roomID){

        ArrayList<PropertyItem> roItems=new ArrayList<>();//room other items
        int roomTypeId=getArea(roomID).getTypeIndx();

        for(PropertyItem item:mPropertyItems) {
            if (item.getLocationIndicator(roomTypeId) !=1 && item.getLocationIndicator(14) !=1)
                roItems.add(item);
        }

        return roItems;

    }


    private Question getDefQuestionById(int id){

        for(Question defQuest:mDefQuestions){
            if(id==defQuest.getQuestionId())return defQuest;
        }
        return null;

    }


    private int getExtraPhotosByQuestion(Question question){

        Question defQuest=getDefQuestionById(question.getQuestionId());

        if(question.isAnswered()&&question.getAnswer()!=defQuest.getDefAnswer()){
            return defQuest.getExtraPhotoCount();
        }else return 0;

    }

    public int getNumberPhotosToBeTaken(RoomItem roomItem){

        int condition=roomItem.getConditionId();
        if(condition==5)return 0;
        else {
            int numOfPhotos=0;
            PropertyItem propItem=getPropItemById(roomItem.getItemId());
            numOfPhotos=propItem.getMaxNumberOfPhotos();
            ArrayList<Question> itemQuestions = roomItem.getQuestions();

            for(Question question:itemQuestions){
               numOfPhotos+=getExtraPhotosByQuestion(question);
            }

            return numOfPhotos;
        }

    }


    /*public ArrayList<Item> getRoomSpecificItems(Area area)
    {
        ArrayList<Item> roomSpecificItems = new ArrayList<Item>();
        if (itemsList != null && itemsList.size() > 0)
        {
            for (Item item : itemsList)
            {
//				if (item.getLocationIndicator() >= 1 && item.getLocationIndicator() <= 13)
                if(area.getRoomTypeID() == item.getLocationIndicator())
                    roomSpecificItems.add(item);
            }
        }
        return roomSpecificItems;
    }

    public ArrayList<Item> getBasicRoomItemsList()
    {
        ArrayList<Item> basicRoomItems = new ArrayList<Item>();
        if (itemsList != null && itemsList.size() > 0) for (Item item : itemsList)
            if (item.getLocationIndicator() == 14) basicRoomItems.add(item);
        return basicRoomItems;
    }

    public ArrayList<Item> getRoomOthersItemsList(Area area)
    {
        ArrayList<Item> basicRoomItems = new ArrayList<Item>();
        if (itemsList != null && itemsList.size() > 0) for (Item item : itemsList)
            if (item.getLocationIndicator() != 14 // 14 is Basic Room
                    && area.getRoomTypeID() != item.getLocationIndicator()) // area room type ID == item location indic. >> Specific Room
                basicRoomItems.add(item);
        return basicRoomItems;
    }*/

}
