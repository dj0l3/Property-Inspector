package com.sakthi.propertyinspector;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.PropertyItem;
import com.sakthi.propertyinspector.data.Question;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.util.FileHandler;
import com.sakthi.propertyinspector.util.ParseUtil;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class ItemPickActivity extends AppCompatActivity {

    private RecyclerView mItemsView;
    private ItemListAdapter mListAdapter;


    private static ArrayList<PropertyItem> mPickedItems=new ArrayList<>();

    /*public static void setPickedQuestion(ArrayList<PropertyItem> pickedItems){
        mPickedItems.clear();
        for(PropertyItem item:pickedItems)mPickedItems.add(item);
    }*/
    public static ArrayList<PropertyItem> getPickedItems(){
        return mPickedItems;
    }

    public static final int ACTION_PICK_ROOM_BASIC=1;
    public static final int ACTION_PICK_ROOM_SPECIFIC=2;
    public static final int ACTION_PICK_ROOM_OTHER=3;
    public static final int ACTION_ADD_AREA_ITEMS=4;


    private int mAction;
    private int mRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_item_pick);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAction=getIntent().getIntExtra("ACTION",ACTION_PICK_ROOM_BASIC);
        mPickedItems.clear();

        mItemsView=(RecyclerView)findViewById(R.id.roomItemList);
        mListAdapter=new ItemListAdapter(this);

        mItemsView.setLayoutManager(new LinearLayoutManager(this));
        mItemsView.setAdapter(mListAdapter);

        PropertyInfo lPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();

        /*ArrayList<Question> questList=lPropertyInfo.getDefaultQuestions();
        for(Question quest:questList){
            //mDefQuestions.add(quest.copy());
            mListAdapter.addItem(quest.copy(false),false);
        }*/

        ArrayList<PropertyItem> mItemsList=null;
        mRoomId=getIntent().getIntExtra("ROOM_ID",-1);
        switch (mAction){

            case ACTION_PICK_ROOM_BASIC:
                setTitle("Room Basic Items");
                mItemsList=lPropertyInfo.getRoomBasicItems();
                break;
            case ACTION_PICK_ROOM_SPECIFIC:
                setTitle("Room Specific Items");
                mItemsList=lPropertyInfo.getRoomSepcificItems(mRoomId);
                break;
            case ACTION_PICK_ROOM_OTHER:
                setTitle("Room Other Items");
                mItemsList=lPropertyInfo.getRoomOtherItems(mRoomId);
                break;

        }
        ParseUtil.sortPropertyItem(mItemsList);
        mListAdapter.setListItems(mItemsList);

        findViewById(R.id.butPickItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setResult(Activity.RESULT_OK);
               // finish();

                if(mPickedItems.size()<1) {
                    Toast.makeText(getApplicationContext(),"pick at least one item",Toast.LENGTH_SHORT).show();
                    return;
                }
                Dialogs.showYesOrNoDialog(ItemPickActivity.this,"New Items","Do you need to save these items?",new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which==DialogInterface.BUTTON_POSITIVE){
                            dialog.dismiss();
                            saveItemInfo();
                        }else {
                            dialog.dismiss();
                           // finish();
                        }

                    }
                });

              /*  if(mPickedItems.size()>0) {
                    ParseUtil.sortPropertyItem(mPickedItems);
                    Intent intent = new Intent(ItemPickActivity.this, BulkNewItemActivity.class);
                    intent.putExtra("ROOM_ID",mRoomId);
                    intent.putExtra("ACTION",mAction);
                    startActivityForResult(intent,ItemListActivity.NEW_ITEM_REQ);
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),"pick at least one item",Toast.LENGTH_SHORT).show();
                }*/
            }
        });

        findViewById(R.id.butCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

    }

    public boolean isItemPicked(PropertyItem item){

        for(PropertyItem iItem:mPickedItems){
            if(iItem.getItemId()==item.getItemId())return true;
        }

        return false;
    }

    public void removeFromPick(PropertyItem item){
        for(PropertyItem pickedItem:mPickedItems){
            if(pickedItem.getItemId()==item.getItemId()){
               mPickedItems.remove(item);
                break;
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem){

        if(menuItem.getItemId()==android.R.id.home){
            finish();
            return true;
        }else return super.onOptionsItemSelected(menuItem);

    }

    private class ItemViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener,View.OnClickListener{

        private View mRootView;

        private TextView mItem;
        private RadioButton mRadioYes;
        private RadioButton mRadioNo;
        private PropertyItem mDataBinded;


        private CheckBox mIsPicked;
        private View mActionView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;

            mItem = (TextView) mRootView.findViewById(R.id.txtQuestion);
            mRadioYes = (RadioButton) mRootView.findViewById(R.id.radioButYes);
            mRadioNo = (RadioButton) mRootView.findViewById(R.id.radioButNO);
           /* mActionYes = mRootView.findViewById(R.id.actionViewYes);
            mActionNo = mRootView.findViewById(R.id.actionViewNo);*/

        /*    mRadioYes.setOnCheckedChangeListener(this);
            mRadioNo.setOnCheckedChangeListener(this);*/
            mActionView=mRootView.findViewById(R.id.listActionItem);
            mActionView.setOnClickListener(this);

            mIsPicked=(CheckBox)mRootView.findViewById(R.id.pickCheckBox);
            mIsPicked.setOnCheckedChangeListener(this);
        }

        private boolean mIsCheckedOnBind;
        public void bind(final PropertyItem data){
            mDataBinded=data;
            mItem.setText((getAdapterPosition()+1)+") "+data.getDesc());
            mIsCheckedOnBind=isItemPicked(data);
            mIsPicked.setChecked(mIsCheckedOnBind);

            /*if(mDataBinded.isAnswered()){
                if(mDataBinded.getAnswer()==Question.YES)mRadioYes.setChecked(true);
                else mRadioNo.setChecked(true);
            }*/
        }




        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


            if(mIsCheckedOnBind){
                mIsCheckedOnBind=false;
                return;
            }

            if(buttonView==mIsPicked){

                if(isChecked&&!isItemPicked(mDataBinded))mPickedItems.add(mDataBinded);
                else if(!isChecked&&isItemPicked(mDataBinded))removeFromPick(mDataBinded);

            }

        }

        @Override
        public void onClick(View v) {
            mIsPicked.setChecked(!mIsPicked.isChecked());
        }
    }


    private class ItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


        private LayoutInflater mInflater;
        private ArrayList<PropertyItem> mDataList;

        public ItemListAdapter(Context context){
            mInflater=(LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
            mDataList=new ArrayList<>();
        }

        public void setListItems(ArrayList<PropertyItem> listItems){
            for(PropertyItem item:listItems){
                mDataList.add(item);
            }
            notifyDataSetChanged();
        }

        public void addItem(PropertyItem item ,boolean update){
            mDataList.add(item);
            if(update)notifyDataSetChanged();
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view=mInflater.inflate(R.layout.list_question_pick_item,null);
            ItemViewHolder areaHolder=new ItemViewHolder(view);
            return areaHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            ItemViewHolder viewHolder=(ItemViewHolder) holder;
            viewHolder.bind(mDataList.get(position));

        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }
    }


    private void saveItemInfo(){

        new AsyncTaskWP(this,"Saving Items..."){

            @Override
            protected Object doInBackground(Object... params) {

                PropertyInfo propertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();

                for(PropertyItem propItem:mPickedItems) {

                    RoomItem roomItem=new RoomItem(mRoomId);
                    ArrayList<Question> questions=propertyInfo.getQuestionsById(propItem.getQuestionsIndx());
                    roomItem.setQuestions(questions);
                    roomItem.setName(propItem.getDesc());
                    roomItem.setItemId(propItem.getItemId());
                    propertyInfo.addNewRoomItem(roomItem);

                }

                String path=((PropertyInspector)getApplication()).getPreference().getWorkFilePath();
//                FileHandler.exportCSV(propertyInfo,path);
                int versionNumber = ((PropertyInspector)getApplication()).getPreference().getWorkFileBackupNumber();
                FileHandler.exportCSVForBackup(propertyInfo, path, versionNumber);
                return null;
            }

            public void onPostExecute(Object result){
                super.onPostExecute(result);
                setResult(RESULT_OK);
                finish();
            }

        }.execute();

    }


}
