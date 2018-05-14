package com.sakthi.propertyinspector.util;

import android.util.Log;

import com.sakthi.propertyinspector.data.Area;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.PropertyItem;
import com.sakthi.propertyinspector.data.Question;
import com.sakthi.propertyinspector.data.RoomItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by sakthivel on 5/13/2016.
 */
public class FileHandler {

    public enum CSVInfoKey {

        KEY_BASICINFO(01), KEY_AREA(02), KEY_ITEM(03), KEY_QUESTION(04), KEY_OWNERINST(05), KEY_PROPERTYINST(06), KEY_CONDITION(21), KEY_COLOR(22), KEY_ROOMTYPE(23), KEY_PROPERTY_ITEM(24), KEY_DEF_QUESTIONS(25),
        KEY_PHOTO(101);
        /* KEY_AREA,KEY_ITEM,KEY_QUESTIONS,KEY_COLORS,KEY_ROOM_TYPE*/

        int keyCode;

        CSVInfoKey(int value) {
            keyCode = value;
        }

        public boolean isKeyEqual(int key) {
            return key == keyCode;
        }
    }

    public class InvalildPropertyFileException extends Exception {

    }


    public PropertyInfo importCSV(String filePath) {

        File file = new File(filePath);

        try {
            BufferedReader lReader = new BufferedReader(new FileReader(file));
            String line = null;
            PropertyInfo lPropertyInfo = new PropertyInfo();

            int lineNum = 1;

            while ((line = lReader.readLine()) != null) {
                //Log.e("Line Num:"+(lineNum++),line);
                if (lineNum > 175) {
                    Log.d("TAG", "importCSV: " + lineNum);
                }
                String[] parsedData = line.split(",");
                int identifier = ParseUtil.getIntFromString(parsedData[0], -100);

                // if(identifier==-100)throw new InvalildPropertyFileException();

                if (CSVInfoKey.KEY_BASICINFO.isKeyEqual(identifier)) {
                    //    Log.e(identifier+"Content:","Is BasicInfo");
                    lPropertyInfo.setBasicInfo(parsedData);
                } else if (CSVInfoKey.KEY_AREA.isKeyEqual(identifier)) {
                    Area area = new Area(parsedData);
                    lPropertyInfo.addArea(area);
                    // Log.e(identifier+"Content:","Is Area");
                } else if (CSVInfoKey.KEY_OWNERINST.isKeyEqual(identifier)) {
                    lPropertyInfo.setOwnerInst(parsedData);
                } else if (CSVInfoKey.KEY_PROPERTYINST.isKeyEqual(identifier)) {
                    lPropertyInfo.setPropertyInst(parsedData);
                } else if (CSVInfoKey.KEY_ROOMTYPE.isKeyEqual(identifier)) {
                    lPropertyInfo.addRoomType(parsedData);
                } else if (CSVInfoKey.KEY_ITEM.isKeyEqual(identifier)) {
                    RoomItem item = new RoomItem(parsedData);
                    lPropertyInfo.addRoomItem(item);

                } else if (CSVInfoKey.KEY_CONDITION.isKeyEqual(identifier)) {
                    lPropertyInfo.addCondition(parsedData);
                } else if (CSVInfoKey.KEY_COLOR.isKeyEqual(identifier)) {
                    lPropertyInfo.addColor(parsedData);
                } else if (CSVInfoKey.KEY_QUESTION.isKeyEqual(identifier)) {

                    Question question = new Question(parsedData);
                    int roomId = ParseUtil.getIntFromString(parsedData[3], 0);
                    int invId = ParseUtil.getIntFromString(parsedData[4], 0);
                    int itemId = ParseUtil.getIntFromString(parsedData[5], 0);
                    Log.e("Question" + (lineNum++), line);
                    Log.e("IDS", roomId + " : " + itemId);

                    lPropertyInfo.addItemQuestion(question, invId);//roomId,itemId);

                } else if (CSVInfoKey.KEY_DEF_QUESTIONS.isKeyEqual(identifier)) {

                    int questionId = ParseUtil.getIntFromString(parsedData[2], 0);
                    String questTex = ParseUtil.getStringFromArray(parsedData, 4);
                    int defQuest = ParseUtil.getIntFromString(parsedData[3], 0);
                    int extraPhotos = ParseUtil.getIntFromString(parsedData[5], 0);
                    boolean isGroupQuestion = parsedData.length == 7 && ParseUtil.getIntFromString(parsedData[6], 0) > 0;

                    Question question = new Question(questionId, questTex, defQuest, extraPhotos, isGroupQuestion);
                    lPropertyInfo.addDefaultQuestion(question);

                } else if (CSVInfoKey.KEY_PROPERTY_ITEM.isKeyEqual(identifier)) {
                    PropertyItem item = new PropertyItem(parsedData);
                    lPropertyInfo.addPropertyItem(item);
                }

            }

            return lPropertyInfo;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void exportCSV(PropertyInfo mPropertyInfo, String outputPath) {


        //write area info
        //write area items info
        //write question items info
        //write owner note info
        //write client note
        //write conditions
        //write colors
        //write questions

        int clientID = mPropertyInfo.getClientId();
        int propertyID = mPropertyInfo.getPropertyId();

        String preFix = "," + clientID + "," + propertyID + ",";

        Log.e("Exporting Files", preFix);

        File outputFile = new File(outputPath);
        FileOutputStream osstream = null;

        try {
            osstream = new FileOutputStream(outputFile);
            //write basic info
            osstream.write(formatToCSV(CSVInfoKey.KEY_BASICINFO.keyCode, preFix, mPropertyInfo.toCSVBasicInfo()));
            //write area info
            ArrayList<Area> areas = mPropertyInfo.getAreaList();
            for (Area area : areas) {
                //write room info
                osstream.write(formatToCSV(CSVInfoKey.KEY_AREA.keyCode, preFix, area.toCSVArea()));
                //write room item info
                ArrayList<RoomItem> items = mPropertyInfo.getAreaItemsIncludeDeleted(area.getRoomId());
                for (RoomItem item : items) {
                    //write items info
                    osstream.write(formatToCSV(CSVInfoKey.KEY_ITEM.keyCode, preFix, item.toCSV()));
                    //write questions info
                    osstream.write(item.toCSVQuestions(CSVInfoKey.KEY_QUESTION.keyCode, preFix).getBytes());
                    //write photo informations
                    osstream.write(item.toCSVPhotos(CSVInfoKey.KEY_PHOTO.keyCode, preFix).getBytes());
                }
            }

            //write owner note
            osstream.write(formatToCSV(CSVInfoKey.KEY_OWNERINST.keyCode, preFix, mPropertyInfo.toCSVOwnerNote()));

            //write property note
            osstream.write(formatToCSV(CSVInfoKey.KEY_PROPERTYINST.keyCode, preFix, mPropertyInfo.toCSVPropertyNote()));

            //write condition csv
            String conditionCSV = mPropertyInfo.toCSVConditions(CSVInfoKey.KEY_CONDITION.keyCode);
            osstream.write(conditionCSV.getBytes());

            //write color csv
            String colorCSV = mPropertyInfo.toCSVColors(CSVInfoKey.KEY_COLOR.keyCode);
            osstream.write(colorCSV.getBytes());
            //write roomtype csv
            String roomTypeCSV = mPropertyInfo.toCSVRoomType(CSVInfoKey.KEY_ROOMTYPE.keyCode);
            osstream.write(roomTypeCSV.getBytes());


            //write property infoitem
            ArrayList<PropertyItem> propItem = mPropertyInfo.getPropertyItems();
            for (PropertyItem item : propItem) {
                osstream.write((CSVInfoKey.KEY_PROPERTY_ITEM.keyCode + "," + mPropertyInfo.getClientId() + "," + item.toCSV()).getBytes());
            }

            //write default questions
            ArrayList<Question> questions = mPropertyInfo.getDefaultQuestions();
            for (Question quest : questions) {
                osstream.write((CSVInfoKey.KEY_DEF_QUESTIONS.keyCode + "," + mPropertyInfo.getClientId() + "," + quest.toCSV()).getBytes());
            }

            osstream.flush();
            Log.e("Exporting Files", "Write Completed");
            osstream.close();
        } catch (FileNotFoundException e) {
            Log.e("Write Failed", e.getMessage());
        } catch (IOException e) {
            Log.e("Write Failed", e.getMessage());
        } finally {
            if (osstream != null) {
                try {
                    osstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void exportCSVForBackup(PropertyInfo mPropertyInfo, String outputPath, int versionNumber) {


        //write area info
        //write area items info
        //write question items info
        //write owner note info
        //write client note
        //write conditions
        //write colors
        //write questions

        if (mPropertyInfo == null) {
            Log.e("Exporting files", "No property info data");
            return;
        }

        int clientID = mPropertyInfo.getClientId();
        int propertyID = mPropertyInfo.getPropertyId();

        String preFix = "," + clientID + "," + propertyID + ",";

        Log.e("Exporting Files", preFix);
        String newFileName = outputPath.split(".csv")[0];
        newFileName = newFileName + "_" + (versionNumber) + ".csv";

        File outputFile = new File(outputPath);
        FileOutputStream osstream = null;

        try {
            osstream = new FileOutputStream(outputFile);
            //write basic info
            osstream.write(formatToCSV(CSVInfoKey.KEY_BASICINFO.keyCode, preFix, mPropertyInfo.toCSVBasicInfo()));
            //write area info
            ArrayList<Area> areas = mPropertyInfo.getAreaList();
            for (Area area : areas) {
                //write room info
                osstream.write(formatToCSV(CSVInfoKey.KEY_AREA.keyCode, preFix, area.toCSVArea()));
                //write room item info
                ArrayList<RoomItem> items = mPropertyInfo.getAreaItemsIncludeDeleted(area.getRoomId());
                for (RoomItem item : items) {
                    //write items info
                    osstream.write(formatToCSV(CSVInfoKey.KEY_ITEM.keyCode, preFix, item.toCSV()));
                    //write questions info
                    osstream.write(item.toCSVQuestions(CSVInfoKey.KEY_QUESTION.keyCode, preFix).getBytes());
                    //write photo informations
                    osstream.write(item.toCSVPhotos(CSVInfoKey.KEY_PHOTO.keyCode, preFix).getBytes());
                }
            }

            //write owner note
            osstream.write(formatToCSV(CSVInfoKey.KEY_OWNERINST.keyCode, preFix, mPropertyInfo.toCSVOwnerNote()));

            //write property note
            osstream.write(formatToCSV(CSVInfoKey.KEY_PROPERTYINST.keyCode, preFix, mPropertyInfo.toCSVPropertyNote()));

            //write condition csv
            String conditionCSV = mPropertyInfo.toCSVConditions(CSVInfoKey.KEY_CONDITION.keyCode);
            osstream.write(conditionCSV.getBytes());

            //write color csv
            String colorCSV = mPropertyInfo.toCSVColors(CSVInfoKey.KEY_COLOR.keyCode);
            osstream.write(colorCSV.getBytes());
            //write roomtype csv
            String roomTypeCSV = mPropertyInfo.toCSVRoomType(CSVInfoKey.KEY_ROOMTYPE.keyCode);
            osstream.write(roomTypeCSV.getBytes());


            //write property infoitem
            ArrayList<PropertyItem> propItem = mPropertyInfo.getPropertyItems();
            for (PropertyItem item : propItem) {
                osstream.write((CSVInfoKey.KEY_PROPERTY_ITEM.keyCode + "," + mPropertyInfo.getClientId() + "," + item.toCSV()).getBytes());
            }

            //write default questions
            ArrayList<Question> questions = mPropertyInfo.getDefaultQuestions();
            for (Question quest : questions) {
                osstream.write((CSVInfoKey.KEY_DEF_QUESTIONS.keyCode + "," + mPropertyInfo.getClientId() + "," + quest.toCSV()).getBytes());
            }

            osstream.flush();
            Log.e("Exporting Files", "Write Completed");
            osstream.close();
        } catch (FileNotFoundException e) {
            Log.e("Write Failed", e.getMessage());
        } catch (IOException e) {
            Log.e("Write Failed", e.getMessage());
        } finally {
            if (osstream != null) {
                try {
                    osstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private static byte[] formatToCSV(int identifier, String propIDs, String content) {
        String text = identifier + propIDs + content + "\n";
        return text.getBytes();
    }

}
