package com.sakthi.propertyinspector;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.sakthi.propertyinspector.data.Area;
import com.sakthi.propertyinspector.data.FTPSettings;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.Question;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.repository.FTPRepository;
import com.sakthi.propertyinspector.util.AppPreference;
import com.sakthi.propertyinspector.util.FileHandler;
import com.sakthi.propertyinspector.util.FileUtil;
import com.sakthi.propertyinspector.util.ParseUtil;
import com.sakthi.propertyinspector.views.RoomItemFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class ReportActivity extends AppCompatActivity {

    private ViewPager mItemPager;
    private RoomItemFragmentAdapter mAdapter;
    private PropertyInfo mPropertyInfo;

    private int mRoomId;
    //private int mItemId;
    private int mInvId;

    private RoomItem mRoomItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mItemPager=(ViewPager)findViewById(R.id.viewPagerItems);
        mRoomId=getIntent().getIntExtra("ROOM_ID",-1);
        //mItemId=getIntent().getIntExtra("ITEM_ID",-1);
        mInvId=getIntent().getIntExtra("INV_ID",-1);
        Log.e("Id",""+mInvId);


        mPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();
        mAdapter=new RoomItemFragmentAdapter(getSupportFragmentManager(),mPropertyInfo.getAreaItems(mRoomId));
        mItemPager.setAdapter(mAdapter);

        setCurrentRoom();

        mItemPager.setOffscreenPageLimit(1);
        mItemPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {


              /*  int count=getSupportFragmentManager().getBackStackEntryCount()-1;
                String name=getSupportFragmentManager().getBackStackEntryAt(count).getName();

                ((RoomItemFragment)getSupportFragmentManager().findFragmentByTag(name)).update();*/
                setTitle(mAdapter.getItemData(position).getName());
                saveOnPageScroll();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        RoomItemFragment.clear();

    }

    private void setCurrentRoom(){

        if (mPropertyInfo != null){
            for (int i=0; i<mPropertyInfo.getRoomItems().size(); i++){
                RoomItem rmItem = mPropertyInfo.getRoomItems().get(i);
                if (rmItem.getRoomId() == mRoomId){
                    mRoomItem = rmItem;
                }
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else return super.onOptionsItemSelected(menuItem);
    }


    private boolean mIsInitiating;
    public void onResume(){
        super.onResume();

        int pageIndx=0;
        int size=mAdapter.getCount();
        for(int i=0;i<size;i++){
            RoomItem roomItem=mAdapter.getItemData(i);
            //if(roomItem.getItemId()==mItemId){//deprecated
            if(roomItem.getInventoryId()==mInvId){
                pageIndx=i;
                break;
            }
        }
       mIsInitiating=true;
        setTitle(mAdapter.getItemData(pageIndx).getName());
        mItemPager.setCurrentItem(pageIndx);

    }


    public void onActivityResult(int req,int res,Intent data){
        if (data != null && data.getExtras() != null) {
            mRoomId=data.getIntExtra("ROOM_ID",mRoomId);
            mInvId=data.getIntExtra("INV_ID",mInvId);
        }

        //mItemId=data.getIntExtra("ITEM_ID",mItemId);

    }



    public static class RoomItemFragmentAdapter extends FragmentStatePagerAdapter{

        private ArrayList<RoomItem> mItems;

        public RoomItemFragmentAdapter(FragmentManager fm,ArrayList<RoomItem> aItems) {
            super(fm);
            mItems=aItems;
        }

        /*public RoomItemFragmentAdapter(ArrayList<RoomItem> aItems){
            mItems=aItems;
        }*/

        @Override
        public Fragment getItem(int position) {
            RoomItem item=mItems.get(position);
            return RoomItemFragment.newInstance(item.getRoomId(),item.getInventoryId());
        }

        public RoomItem getItemData(int position){
            return mItems.get(position);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }
    }


  /*  private Spinner mConditionSpinner;
    private Spinner mColorSpinner;

    private PropertyInfo mPropertyInfo;
    private RecyclerView mQuestionsView;
    private QuestListAdapter mListAdapter;

    private TextInputEditText mEditMake;
    private TextInputEditText mEditModel;
    private TextInputEditText mEditType;

    private TextInputEditText mEditNote;
    private TextInputEditText mEditCNote;
    private CheckBox mIsConditionTaskAdded;

   // private Button mPhotos;
      private TextView mGallery;

    private int mRoomId;
    private int mItemId;

    private RoomItem mRoomItem;
    private String[] mArrConditions;
    private String[] mArrColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();
        mArrConditions=mPropertyInfo.getConditions();
        mArrColors=mPropertyInfo.getColors();

        mConditionSpinner=(Spinner)findViewById(R.id.reportSpinnerCond);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,mPropertyInfo.getConditions());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mConditionSpinner.setAdapter(arrayAdapter);

        mColorSpinner=(Spinner)findViewById(R.id.reportSpinnerColor);
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_spinner_item,mPropertyInfo.getColors());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mColorSpinner.setAdapter(arrayAdapter);


        mQuestionsView=(RecyclerView)findViewById(R.id.itemQuestionsList);
        mListAdapter=new QuestListAdapter(this);

        mQuestionsView.setLayoutManager(new LinearLayoutManager(this));
        mQuestionsView.setAdapter(mListAdapter);

        mEditMake=(TextInputEditText)findViewById(R.id.reportEditMake);
        mEditModel=(TextInputEditText)findViewById(R.id.reportEditModel);
        mEditType=(TextInputEditText)findViewById(R.id.reportEditType);

        mEditNote=(TextInputEditText)findViewById(R.id.reportNote);
        mEditCNote=(TextInputEditText)findViewById(R.id.reportCleaningNote);

        mIsConditionTaskAdded=(CheckBox)findViewById(R.id.chkConditionTask);

        mRoomId=getIntent().getIntExtra("ROOM_ID",-1);
        mItemId=getIntent().getIntExtra("ITEM_ID",-1);


        if(mRoomId!=-1&&mItemId!=-1){
            mRoomItem=mPropertyInfo.getRoomItemById(mRoomId,mItemId);
            setTitle(mRoomItem.getName() +" Report");
            mListAdapter.setListItems(mRoomItem.getQuestions());

            mEditMake.setText(mRoomItem.getMake());
            mEditModel.setText(mRoomItem.getModel());
            mEditType.setText(mRoomItem.getType());

            mEditNote.setText(mRoomItem.getNote());
            mEditCNote.setText(mRoomItem.getCleaningNote());

            mColorSpinner.setSelection(mPropertyInfo.getItemPosition(mArrColors,mPropertyInfo.getColor(mRoomItem.getColorId())));
            mConditionSpinner.setSelection(mPropertyInfo.getItemPosition(mArrConditions,mPropertyInfo.getCondition(mRoomItem.getConditionId())));
            mIsConditionTaskAdded.setChecked(mRoomItem.getCondIndiTask());
        }

        mGallery=(TextView)findViewById(R.id.lblGallery);
        findViewById(R.id.actionCamera).setOnClickListener(new View.OnClickListener() {
                                                               @Override
                                                               public void onClick(View v) {
                                                                   Intent captureIntent=new Intent(ReportActivity.this,PhotoCaptureActivity.class);
                                                                   captureIntent.putExtra("ROOM_ID",mRoomId);
                                                                   captureIntent.putExtra("ITEM_ID",mItemId);
                                                                   startActivity(captureIntent);
                                                               }
                                                           });

        findViewById(R.id.actionGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent captureIntent=new Intent(ReportActivity.this,GalleryActivity.class);
                captureIntent.putExtra("ROOM_ID",mRoomId);
                captureIntent.putExtra("ITEM_ID",mItemId);
                startActivity(captureIntent);
            }
        });


       *//* mPhotos=(Button)findViewById(R.id.butPhotos);
        mPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent captureIntent=new Intent(ReportActivity.this,GalleryActivity.class);
                captureIntent.putExtra("ROOM_ID",mRoomId);
                captureIntent.putExtra("ITEM_ID",mItemId);
                startActivity(captureIntent);
            }
        });*//*

    }

    public void onResume(){
        super.onResume();
        mGallery.setText("Photos ("+mRoomItem.getNumberOfPhotos()+")");
    }

    private class QuestViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener{

        private View mRootView;

        private TextView mQuestion;
        private RadioButton mRadioYes;
        private RadioButton mRadioNo;

        private Question mDataBinded;

        public QuestViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;

            mQuestion = (TextView) mRootView.findViewById(R.id.txtQuestion);
            mRadioYes = (RadioButton) mRootView.findViewById(R.id.radioButYes);
            mRadioNo = (RadioButton) mRootView.findViewById(R.id.radioButNO);
           *//* mActionYes = mRootView.findViewById(R.id.actionViewYes);
            mActionNo = mRootView.findViewById(R.id.actionViewNo);*//*

            mRadioYes.setOnCheckedChangeListener(this);
            mRadioNo.setOnCheckedChangeListener(this);
        }

        public void bind(final Question data){
            mDataBinded=data;
            mQuestion.setText(data.getQuestion()+"?");

            if(mDataBinded.isAnswered()){
                if(mDataBinded.getAnswer()==Question.YES)mRadioYes.setChecked(true);
                else mRadioNo.setChecked(true);
            }
        }


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if(!isChecked)return;

            if(buttonView==mRadioYes){
                mDataBinded.setAnswer(Question.YES);
            }else if(buttonView==mRadioNo){
                mDataBinded.setAnswer(Question.NO);
            }

        }

    }


    private class QuestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


        private LayoutInflater mInflater;
        private ArrayList<Question> mDataList;

        public QuestListAdapter(Context context){
            mInflater=(LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
            mDataList=new ArrayList<>();
        }

        public void setListItems(ArrayList<Question> listItems){
            for(Question itemQuest:listItems){
                mDataList.add(itemQuest);
            }
            notifyDataSetChanged();
        }

        public void addItem(Question itemQuestion ,boolean update){
            mDataList.add(itemQuestion);
            if(update)notifyDataSetChanged();
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view=mInflater.inflate(R.layout.item_list_question,null);
            QuestViewHolder areaHolder=new QuestViewHolder(view);
            return areaHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            QuestViewHolder viewHolder=(QuestViewHolder) holder;
            viewHolder.bind(mDataList.get(position));

        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }
    }*/


   // private boolean mIsChanged;
    public void onBackPressed(){
        //if(mIsChanged){
            Dialogs.showYesOrNoDialog(this,"Changes done","Do you need to save changes?",new DialogInterface.OnClickListener(){

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

    private void saveOnPageScroll(){

        if(mIsInitiating){
            mIsInitiating=false;
            return;
        }

        new AsyncTaskWP(this,""){
            public void onPreExecute(){

                List<Fragment> fragments=(getSupportFragmentManager().getFragments());
                for(Fragment frags:fragments){
                    RoomItemFragment frag=(RoomItemFragment)frags;
                    if(frag==null||frag.getRoomItem()==null)continue;
                    frag.updateData();
                }
            }

            @Override
            protected Object doInBackground(Object... params) {

                synchronized (objectLock) {
                    ArrayList<RoomItem> allItems = mPropertyInfo.getRoomItems();
                    Map<Integer, RoomItem> mapItems = RoomItemFragment.getChangedItems();

                    Iterator<Map.Entry<Integer, RoomItem>> entries = mapItems.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry<Integer, RoomItem> entry = entries.next();
                        RoomItem local = entry.getValue();
                        Log.e("Item Found",""+local.getItemId());
                        ArrayList<Question> questions=local.getQuestions();
                       /* for (Question question:questions){
                            Log.e("Question"+question.getAnswer(),question.getQuestionId()+":"+question.getQuestion());
                        };*/
                        for (RoomItem item : allItems) {
                            if(item.getInventoryId()==local.getInventoryId()){
                                item.updateData(local);
                            }
                            /*if (item.getItemId() == local.getItemId() && item.getRoomId() == local.getRoomId()) {
                                item.updateData(local);
                            }*/
                        }
                    }
                    String path = ((PropertyInspector) getApplication()).getPreference().getWorkFilePath();
//                    FileHandler.exportCSV(mPropertyInfo, path);
                    int versionNumber = ((PropertyInspector)getApplication()).getPreference().getWorkFileBackupNumber();
                    FileHandler.exportCSVForBackup(mPropertyInfo, path, versionNumber);
                }
                return null;
            }

            public void onPostExecute(Object result){
            }

        }.execute();

    }

    private final  Object objectLock=new Object();

    private void saveItemInfo(){
        new AsyncTaskWP(this,"Saving Item Details....."){


            public void onPreExecute(){
                super.onPreExecute();


                List<Fragment> fragments=(getSupportFragmentManager().getFragments());
                for(Fragment frags:fragments){
                    RoomItemFragment frag=(RoomItemFragment)frags;
                    if(frag==null||frag.getRoomItem()==null)continue;
                    // if(frag.getRoomItem().getItemId()==mAdapter.getItemData(position).getItemId()){
                        frag.updateData();
                    // }
                }

                ArrayList<RoomItem> allItems = mPropertyInfo.getRoomItems();
                HashMap<Integer,RoomItem> mapItems=RoomItemFragment.getChangedItems();

                Iterator<Map.Entry<Integer, RoomItem>> entries = mapItems.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry<Integer, RoomItem> entry = entries.next();
                    RoomItem local = entry.getValue();
                     //Log.e("Item Found",""+local.getItemId());
                     ArrayList<Question> questions=local.getQuestions();
                    /* for (Question question:questions){
                        // Log.e("Question"+question.getAnswer(),question.getQuestionId()+":"+question.getQuestion());
                     };*/
                     for (RoomItem item : allItems){
                         if(item.getInventoryId()==local.getInventoryId()){
                             item.updateData(local);
                         }
                     }
                }
            }

            @Override
            protected Object doInBackground(Object... params) {

                String path=((PropertyInspector)getApplication()).getPreference().getWorkFilePath();
                int versionNumber = ((PropertyInspector)getApplication()).getPreference().getWorkFileBackupNumber();
                FileHandler.exportCSVForBackup(mPropertyInfo, path, versionNumber);
//                FileHandler.exportCSV(mPropertyInfo,path);
                return null;

            }

            public void onPostExecute(Object result){
                super.onPostExecute(result);
                finish();
            }

        }.execute();
    }

    public void onDestroy(){
        RoomItemFragment.clear();
        super.onDestroy();
    }


}
