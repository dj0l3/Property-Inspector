package com.sakthi.propertyinspector;

import android.app.Activity;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.sakthi.propertyinspector.data.Area;
import com.sakthi.propertyinspector.data.InspectedFiles;
import com.sakthi.propertyinspector.data.PhotoData;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.util.AndroidUtilities;
import com.sakthi.propertyinspector.util.AppPreference;
import com.sakthi.propertyinspector.util.FileHandler;
import com.sakthi.propertyinspector.util.FileUtil;
import com.sakthi.propertyinspector.util.ParseUtil;
import com.sakthi.propertyinspector.views.SwipeToRemoveHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import io.fabric.sdk.android.Fabric;

/**
 * Created by sakthivel on 5/27/2016.
 */
public class ItemListActivity extends AppCompatActivity{

    private PropertyInfo mPropertyInfo;
    private ItemListAdapter mListAdapter;
    private RecyclerView mRecyclerView;

    public static final int NEW_ITEM_REQ=341;

    private int mRoomId;
    private Area mArea;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_area);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView toolBarImg=(ImageView)findViewById(R.id.toolbarImage);
        toolBarImg.setImageResource(R.drawable.itemlist2);

        mPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();
        mRoomId=getIntent().getIntExtra("ROOM_ID",-1);

        if (mPropertyInfo == null) {
            return;
        }

        mArea=mPropertyInfo.getArea(mRoomId);

        setTitle(mArea.getAliasName()+" Items");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent=new Intent(ItemListActivity.this,BulkNewItemActivity.class);
                intent.putExtra("ROOM_ID",mRoomId);
                startActivityForResult(intent,NEW_ITEM_REQ);
            }
        });


        mRecyclerView=(RecyclerView)findViewById(R.id.listViewArea);
        mListAdapter=new ItemListAdapter(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mListAdapter);

        ArrayList<RoomItem> items=mPropertyInfo.getAreaItems(mRoomId);
        //ParseUtil.sortRooItem(items);

        mListAdapter.setListItems(items);

        SwipeToRemoveHelper swipeHelper=new SwipeToRemoveHelper(mListAdapter);
        ItemTouchHelper touchHelper=new ItemTouchHelper(swipeHelper);
        touchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setItemAnimator(AndroidUtilities.getItemAnimator());

       /* ArrayList<RoomItem> items=mPropertyInfo.getAreaItems(mRoomId);

        for(RoomItem item:items){
            Log.e(item.getName()," "+item.getQuestions().size());
        }*/

    }

    public void onActivityResult(int req,int res,Intent data){

       /* if(req==NEW_ITEM_REQ&&res== Activity.RESULT_OK){
           // mListAdapter.addItem(mPropertyInfo.getNewlyAddedArea(),true);
            //Snackbar.make(mRecyclerView,"Added New Area",Snackbar.LENGTH_SHORT).show();
            ArrayList<RoomItem> items=mPropertyInfo.getNewAddedItems();
            for(RoomItem item:items)mListAdapter.addItem(item,true);
        }*/
    }

    public void onResume(){
        super.onResume();
        if(mPropertyInfo != null && mPropertyInfo.getNewAddedItems().size()>0){
            mPropertyInfo.clearNewItems();
            mListAdapter.removeAll();
            mListAdapter.setListItems(mPropertyInfo.getAreaItems(mRoomId));
            finish();

        }else if (mListAdapter != null) mListAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.item_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id==R.id.action_room_specific){
            Intent intent=new Intent(this,ItemPickActivity.class);
            intent.putExtra("ROOM_ID",mRoomId);
            intent.putExtra("ACTION",ItemPickActivity.ACTION_PICK_ROOM_SPECIFIC);
            startActivity(intent);
        }else if(id==R.id.action_room_basic){

            Intent intent=new Intent(this,ItemPickActivity.class);
            intent.putExtra("ROOM_ID",mRoomId);
            intent.putExtra("ACTION",ItemPickActivity.ACTION_PICK_ROOM_BASIC);
            startActivity(intent);

        }else if(id==R.id.action_room_othes){

            Intent intent=new Intent(this,ItemPickActivity.class);
            intent.putExtra("ROOM_ID",mRoomId);
            intent.putExtra("ACTION",ItemPickActivity.ACTION_PICK_ROOM_OTHER);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private View mRootView;

        private TextView mQty;
        private TextView mName;
        private TextView mAnsPerc;
        private TextView mPhotos;
        private TextView mColor;
        private TextView mCondition;
        private TextView mNote;

        public ItemViewHolder(View itemView) {

            super(itemView);
            mRootView = itemView;

            mQty = (TextView) mRootView.findViewById(R.id.itemListQty);
            mName = (TextView) mRootView.findViewById(R.id.itemListDesc);
            mAnsPerc = (TextView) mRootView.findViewById(R.id.itemListInspected);
            mPhotos = (TextView) mRootView.findViewById(R.id.itemListPhotos);
            mColor = (TextView) mRootView.findViewById(R.id.itemListColor);
            mCondition = (TextView) mRootView.findViewById(R.id.itemListCondition);
            mNote = (TextView) mRootView.findViewById(R.id.itemListNote);
            mRootView.setClickable(true);

        }


        public void bind(final RoomItem roomItem){

            mName.setText(roomItem.getName());
            mNote.setText((roomItem.getNote().trim().length()==0)?"Items note unavailable":"Note: "+roomItem.getNote());
            mPhotos.setText("Photos : "+roomItem.getNumberOfPhotos());

            mAnsPerc.setText("Inspected : "+roomItem.getStatics().percentage()+"%");
            mQty.setText("Qty : 1");

            mColor.setText(mPropertyInfo.getColor(roomItem.getColorId()));
            mCondition.setText(mPropertyInfo.getCondition(roomItem.getConditionId()));


            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(ItemListActivity.this,ReportActivity.class);
                    Log.e("Id",""+roomItem.getInventoryId());
                    intent.putExtra("ROOM_ID",mRoomId);
                    intent.putExtra("INV_ID",roomItem.getInventoryId());
                    intent.putExtra("ITEM_ID",roomItem.getItemId());
                    startActivity(intent);
                }
            });

        }

    }


    private class ItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SwipeToRemoveHelper.SwipeToRemoveCallback{


        private LayoutInflater mInflater;
        private ArrayList<RoomItem> mDataList;

        private Context mContext;

        public ItemListAdapter(Context context){
            mContext=context;
            mInflater=(LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
            mDataList=new ArrayList<>();
        }



        public void setListItems(ArrayList<RoomItem> listItems){
            for(RoomItem item:listItems){
                mDataList.add(item);
            }
            notifyDataSetChanged();
        }

        public void addItem(RoomItem item ,boolean update){
            mDataList.add(item);
            if(update)notifyDataSetChanged();
        }

        public void removeAll(){
            mDataList.clear();
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view=mInflater.inflate(R.layout.object_list_item2,null);
            ItemViewHolder itemHolder=new ItemViewHolder(view);
            return itemHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            ItemViewHolder viewHolder=(ItemViewHolder)holder;
            viewHolder.bind(mDataList.get(position));

        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        @Override
        public void onItemRemoved(final int position) {

            new AsyncTaskWP(mContext,"Deleting....."){

                public void onPreExecute(){
                    mDataList.get(position).setItemDeleted(true);
                }
                @Override
                protected Object doInBackground(Object... params) {

                /*    ArrayList<PhotoData> photos=mDataList.get(position).getPhotosList();
                    for(PhotoData data:photos){
                        FileUtil.deleteFile(data.getImagePath());
                    }*/

                    String path=((PropertyInspector)getApplication()).getPreference().getWorkFilePath();
//                    FileHandler.exportCSV(mPropertyInfo,path);
                    int versionNumber = ((PropertyInspector)getApplication()).getPreference().getWorkFileBackupNumber();
                    FileHandler.exportCSVForBackup(mPropertyInfo, path, versionNumber);
                    return null;
                }
                public void onPostExecute(Object result){
                    //super.onPostExecute(result);
                   // mPropertyInfo.removeRoomItem(mDataList.get(position));
                    mDataList.remove(position);
                    notifyItemRemoved(position);
                }
            }.execute();




        }
    }

    @Override
    public void onBackPressed() {
        saveItemInfo();
    }

    private void saveItemInfo(){
        new AsyncTaskWP(this,"Saving Property Information...."){
            @Override
            protected Object doInBackground(Object... params) {

                PropertyInfo mPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();
                String path=((PropertyInspector)getApplication()).getPreference().getWorkFilePath();
//                FileHandler.exportCSV(mPropertyInfo,path);
                int versionNumber = ((PropertyInspector)getApplication()).getPreference().getWorkFileBackupNumber();
                FileHandler.exportCSVForBackup(mPropertyInfo, path, versionNumber + 1);
                ((PropertyInspector)getApplication()).getPreference().setWorkFileBackupNumber(versionNumber + 1);
                Gson gson = new Gson();
                AppPreference preference = new AppPreference(ItemListActivity.this);
                String strInspectedFiles = preference.getInspectedProperty();

                try {
                    JSONArray jsonArray;
                    if (strInspectedFiles != null && strInspectedFiles.length()>0) {
                        jsonArray = new JSONArray(strInspectedFiles);
                    }else {
                        jsonArray = new JSONArray();
                    }
                    int existingPosition = -1;
                    for (int i=0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        InspectedFiles existingProperty = gson.fromJson(jsonObject.toString(), InspectedFiles
                                .class);
                        if (existingProperty.propertyInfo.getPropertyId() == mPropertyInfo.getPropertyId()){
                            existingPosition = i;
                            break;
                        }
                    }
                    InspectedFiles inspectedFile = new InspectedFiles();
                    inspectedFile.propertyInfo = mPropertyInfo;
                    inspectedFile.filePath = path;
                    JSONObject propertyObject = new JSONObject(gson.toJson(inspectedFile));

                    if (existingPosition > 0){
                        jsonArray.put(existingPosition, propertyObject);
                    }else{
                        jsonArray.put(propertyObject);
                    }
                    Log.d("TAG", "jsonArray: "+jsonArray);
                    preference.setInspectedProperty(jsonArray.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("TAG", "doInBackground: "+path);
                return null;

            }

            public void onPostExecute(Object result){
                super.onPostExecute(result);
                finish();
            }

        }.execute();
    }
}
