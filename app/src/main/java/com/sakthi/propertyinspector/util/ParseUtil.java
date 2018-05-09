package com.sakthi.propertyinspector.util;

import com.sakthi.propertyinspector.data.PropertyItem;
import com.sakthi.propertyinspector.data.RoomItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sakthivel on 5/12/2016.
 */
public class ParseUtil {

    public static int getIntFromString(String value,int defValue){

        try {
            return Integer.parseInt(value.trim());
        }catch (Exception e){}

        return defValue;
    }

    public static String getStringFromArray(String[] array,int indx){

        try {
            return array[indx].trim();
        }catch (Exception e){}

        return "";
    }

    public static int getItemPosition(String[] items,String sText){
        int size=items.length;
        for(int i=0;i<size;i++){
            if(items[i].equals(sText))return i;
        }
        return 0;
    }

   /* public  static int getPageIndx(ArrayList<RoomItem> items,int itemId){

        int num= items.size();
        for (int i=0;i<num;i++){
            if(items.get(i).getItemId()==itemId)return i;
        }

        return 0;
    }*/

    public static void sortRooItem(List<RoomItem> items){

        Comparator<RoomItem> itemComparator=new Comparator<RoomItem>() {
            @Override
            public int compare(RoomItem lhs, RoomItem rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        };

        Collections.sort(items,itemComparator);

    }

    public static void sortPropertyItem(List<PropertyItem> items){

        Comparator<PropertyItem> itemComparator=new Comparator<PropertyItem>() {
            @Override
            public int compare(PropertyItem lhs, PropertyItem rhs) {
                return lhs.getDesc().compareToIgnoreCase(rhs.getDesc());
            }
        };

        Collections.sort(items,itemComparator);

    }

}
