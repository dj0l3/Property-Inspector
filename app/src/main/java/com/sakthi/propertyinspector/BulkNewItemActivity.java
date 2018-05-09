package com.sakthi.propertyinspector;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.PropertyItem;
import com.sakthi.propertyinspector.data.Question;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.util.FileHandler;
import com.sakthi.propertyinspector.views.NewItemFragment;
import com.sakthi.propertyinspector.views.RoomItemFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class BulkNewItemActivity extends AppCompatActivity {


    private ViewPager mItemPager;
    private FloatingActionButton mAddItem;
   // private ItemFragmentAdapter mAdapter;
    private ItemFragmentStateAdapter mStateAdapter;



    public static ArrayList<PropertyItem> mDescItems;
    private int mRoomID;
    private int mAction;

    private PropertyInfo mPropertyInfo;

    private ArrayList<Integer> uniqueIdList;
    private int mUniqueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_bulk_new_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uniqueIdList=new ArrayList<>();
        mUniqueId=1;


        mAddItem= (FloatingActionButton) findViewById(R.id.addItem);
        mAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStateAdapter.addItem(new PropertyItem(-1),true);
                mItemPager.setCurrentItem(mStateAdapter.getCount()-1);
                /*mAdapter.addFragment(NewItemFragment.newInstance(-1,mUniqueId++,mRoomID),true);
                mItemPager.setCurrentItem(mAdapter.getCount()-1);*/
            }
        });

        mItemPager=(ViewPager)findViewById(R.id.newItemsPager);
        /*mAdapter=new ItemFragmentAdapter(getSupportFragmentManager());
        mItemPager.setAdapter(mAdapter);*/

        mStateAdapter=new ItemFragmentStateAdapter(getSupportFragmentManager());
        mItemPager.setAdapter(mStateAdapter);



        mRoomID=getIntent().getIntExtra("ROOM_ID",-1);
        mAction=getIntent().getIntExtra("ACTION",ItemPickActivity.ACTION_ADD_AREA_ITEMS);

        mPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();
        switch (mAction){
            case ItemPickActivity.ACTION_PICK_ROOM_SPECIFIC:
                setTitle("Add Room Specific Items");
                mDescItems=mPropertyInfo.getRoomSepcificItems(mRoomID);
                break;
            case ItemPickActivity.ACTION_PICK_ROOM_BASIC:
                setTitle("Add Room Basic Items");
                mDescItems=mPropertyInfo.getRoomBasicItems();
                break;
            case ItemPickActivity.ACTION_PICK_ROOM_OTHER:
                setTitle("Add Room Other Items");
                mDescItems=mPropertyInfo.getRoomOtherItems(mRoomID);
                break;
            case ItemPickActivity.ACTION_ADD_AREA_ITEMS:
                setTitle("Add Room Items");
                mDescItems=mPropertyInfo.getPropertyItems();
                break;
        }

        if(mAction!=ItemPickActivity.ACTION_ADD_AREA_ITEMS) {
            ArrayList<PropertyItem> pickedItems = ItemPickActivity.getPickedItems();
            for (PropertyItem item : pickedItems) {
                /*NewItemFragment fragment=NewItemFragment.newInstance(item.getItemId(),mUniqueId++,mRoomID);
                mAdapter.addFragment(fragment,false);*/
                mStateAdapter.addItem(item,false);

            }
            mStateAdapter.notifyDataSetChanged();
            //mAdapter.notifyDataSetChanged();
        }else{
           /* NewItemFragment fragment=NewItemFragment.newInstance(-1,mUniqueId++,mRoomID);
            mAdapter.addFragment(fragment,true);*/
            mStateAdapter.addItem(mDescItems.get(0),true);

        }


        //mItemPager.setCurrentItem(mAdapter.getCount()-1);
        mItemPager.setCurrentItem(mStateAdapter.getCount()-1);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else return super.onOptionsItemSelected(menuItem);
    }

    private int getFakeItemId(int position){
        int size=uniqueIdList.size();
        if(position<size){
            return uniqueIdList.get(position);
        }else {
            mUniqueId+=1;
            uniqueIdList.add(mUniqueId);
            return mUniqueId;
        }
    }

    private void romoveFakeItemId(int position){

        if(position<uniqueIdList.size()){
            uniqueIdList.remove(position);
        }

    }

    private void addRoomItemToFragment(int fakeId,PropertyItem item){

        RoomItem roomItem=new RoomItem(mRoomID);
        roomItem.setName(item.getDesc());
        roomItem.setItemId(item.getItemId());

        Log.e("New Item Id",""+item.getItemId());

        NewItemFragment.getChangedItems().put(fakeId,roomItem);


        ArrayList<Question> questions=mPropertyInfo.getQuestionsById(item.getQuestionsIndx());
        roomItem.setQuestions(questions);
                //NewItemFragment.getChangedItems().get(fakeId);
        //


    }


    public  class ItemFragmentStateAdapter extends FragmentStatePagerAdapter {

        private ArrayList<PropertyItem> mItems;

        public ItemFragmentStateAdapter(FragmentManager fm) {
            super(fm);
            mItems = new ArrayList<>();
        }

        public void addItem(PropertyItem item,boolean update){
            int position=mItems.size();
            mItems.add(item);
            addRoomItemToFragment(getFakeItemId(position),item);
            if(update)notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            PropertyItem item = mItems.get(position);
            return NewItemFragment.newInstance(item.getItemId(),getFakeItemId(position),mRoomID);
        }

        public PropertyItem getItemData(int position) {
            return mItems.get(position);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }
    }

    public static class ItemFragmentAdapter extends FragmentPagerAdapter{

        private ArrayList<NewItemFragment> mItemFrags;

        public ItemFragmentAdapter(FragmentManager fm) {
            super(fm);
            mItemFrags=new ArrayList<>();
        }


        public void addFragment(NewItemFragment fragment,boolean update){
            mItemFrags.add(fragment);
            if(update)notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            return mItemFrags.get(position);
        }

        @Override
        public int getCount() {
            return mItemFrags.size();
        }

    }

    public void onBackPressed(){
        //if(mIsChanged){
        Dialogs.showYesOrNoDialog(this,"New Item","Do you need to add this item?",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==DialogInterface.BUTTON_POSITIVE){
                    dialog.dismiss();
                    saveItemInfo();
                }else {
                    dialog.dismiss();
                    finish();
                }

            }
        });
        // }else super.onBackPressed();

    }


    private void saveItemInfo(){
        new AsyncTaskWP(this,"Updating New Room Items....."){


            public void onPreExecute(){
                super.onPreExecute();
                /*ArrayList<NewItemFragment> fragments=mAdapter.mItemFrags;
                for(NewItemFragment fragment:fragments){
                    RoomItem roomItem=fragment.getRoomItem();
                    mPropertyInfo.addNewRoomItem(roomItem);
                }*/

                List<Fragment> fragments=(getSupportFragmentManager().getFragments());
                for(Fragment frags:fragments){
                    NewItemFragment frag=(NewItemFragment)frags;
                    if(frag==null)continue;
                    RoomItem item=frag.getRoomItem();
                }

                HashMap<Integer,RoomItem> mapItems=NewItemFragment.getChangedItems();

                Iterator<Integer> key=mapItems.keySet().iterator();
                while(key.hasNext()){
                    int itemId=key.next();
                    RoomItem newRoomItem=mapItems.get(itemId);
                    mPropertyInfo.addNewRoomItem(newRoomItem);
                }

            }

            @Override
            protected Object doInBackground(Object... params) {

                String path=((PropertyInspector)getApplication()).getPreference().getWorkFilePath();
//                FileHandler.exportCSV(mPropertyInfo,path);
                int versionNumber = ((PropertyInspector)getApplication()).getPreference().getWorkFileBackupNumber();
                FileHandler.exportCSVForBackup(mPropertyInfo, path, versionNumber);
                return null;

            }

            public void onPostExecute(Object result){
                super.onPostExecute(result);
                setResult(Activity.RESULT_OK);
                finish();
            }

        }.execute();
    }

    public void onDestroy(){
        NewItemFragment.clear();
        super.onDestroy();
    }

}
