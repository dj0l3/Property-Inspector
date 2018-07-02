package com.sakthi.propertyinspector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.sakthi.propertyinspector.data.Area;
import com.sakthi.propertyinspector.data.InspectedFiles;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.util.AppPreference;
import com.sakthi.propertyinspector.util.FileHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class AreaActivity extends AppCompatActivity {

    private PropertyInfo mPropertyInfo;
    private AreaListAdapter mListAdapter;
    private RecyclerView mRecyclerView;

    public static final int NEW_AREA_REQ=0x1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_area);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Rooms");
        mPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent=new Intent(AreaActivity.this,NewAreaActivity.class);
                startActivityForResult(intent,NEW_AREA_REQ);
            }
        });


        mRecyclerView=(RecyclerView)findViewById(R.id.listViewArea);
        mListAdapter=new AreaListAdapter(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mListAdapter);

        if (mPropertyInfo != null) {
            mListAdapter.setListItems(mPropertyInfo.getAreaList());
        }
    }

    public void onActivityResult(int req,int res,Intent data){

        if(req==NEW_AREA_REQ&&res==Activity.RESULT_OK){
            mListAdapter.addItem(mPropertyInfo.getNewlyAddedArea(),true);
            Snackbar.make(mRecyclerView,"Added New Area",Snackbar.LENGTH_SHORT).show();
        }

    }


    private class AreaViewHolder extends RecyclerView.ViewHolder{

        private View mRootView;

        private TextView mRoomNo;
        private TextView mName;
        private TextView mAnsPerc;
        private TextView mRoomType;
        private TextView mStatus;

        public AreaViewHolder(View itemView) {
            super(itemView);
            mRootView=itemView;



            mRoomNo=(TextView)mRootView.findViewById(R.id.textAreaRoomNo);
            mName=(TextView)mRootView.findViewById(R.id.textRoomAlias);
            mAnsPerc=(TextView)mRootView.findViewById(R.id.textQuestAnsweredPerc);
            mRoomType=(TextView)mRootView.findViewById(R.id.textRoomType);
            mStatus=(TextView)mRootView.findViewById(R.id.textRoomStatus);

            mRootView.setClickable(true);
        }

        public void bind(final Area data,int bindPosition){

            mRoomNo.setText(""+data.getRoomNo());
            mName.setText(data.getAliasName().toUpperCase());
//            Log.e("Room Type"+mPropertyInfo.getRoomType(data.getTypeIndx()),data.getAliasName().toUpperCase());
            mRoomType.setText("Type : "+mPropertyInfo.getRoomType(data.getTypeIndx()));
            mStatus.setText("Status : "+mPropertyInfo.getRoomStatus(data.getStatusIndx()));

            if (!ListCompletedInspections.StaticButton.isOpenSelectedFilePressed) {
                if (mPropertyInfo.getRoomStatics(data).percentage() == 100
                        && !data.getStatics().isAreaEntered()) {

                    if (mPropertyInfo.getRoomStatics(data).getAnsweredQCount() != 0) {
                        mAnsPerc.setText(Integer.toString(mPropertyInfo.getRoomStatics(data).getAnsweredQCount() * 100 / mPropertyInfo.getRoomStatics(data).getTotalQuestions())+ "%");
                    } else {
                        mAnsPerc.setText("0%");
                    }
                } else {
                    if (mPropertyInfo.getAreaItems(data.getRoomId()).size() == 0) {
                        mAnsPerc.setText(mPropertyInfo.getRoomStatics(data).percentage() + "%");
                    } else {
                        mAnsPerc.setText(Integer.toString(mPropertyInfo.getRoomStatics(data).getAnsweredQCount() * 100 / mPropertyInfo.getRoomStatics(data).getTotalQuestions())+ "%");
                    }
                }
            } else {
                mAnsPerc.setText(mPropertyInfo.getRoomStatics(data).percentage() + "%");
            }

            mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent=new Intent(AreaActivity.this,ItemListActivity.class);
                    intent.putExtra("ROOM_ID",data.getRoomId());
                    startActivity(intent);
                    data.getStatics().setAreaEntered(true);
                    //Snackbar.make(mRecyclerView,"Exception thrown when parsing items for "+data.getAliasName(),Snackbar.LENGTH_LONG).show();
                }
            });

        }

    }


    private class AreaListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


        private LayoutInflater mInflater;
        private ArrayList<Area> mDataList;

        public AreaListAdapter(Context context){
            mInflater=(LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
            mDataList=new ArrayList<>();
        }



        public void setListItems(ArrayList<Area> listItems){
            for(Area area:listItems){
                mDataList.add(area);
            }
            notifyDataSetChanged();
        }

        public void addItem(Area area ,boolean update){
            mDataList.add(area);
            if(update)notifyDataSetChanged();
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view=mInflater.inflate(R.layout.area_list_item,null);
            AreaViewHolder areaHolder=new AreaViewHolder(view);
            return areaHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            AreaViewHolder viewHolder=(AreaViewHolder)holder;
            viewHolder.bind(mDataList.get(position),position);

        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

    }

    public void onResume(){
        super.onResume();
        mListAdapter.notifyDataSetChanged();
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

                Gson gson = new Gson();
                AppPreference preference = new AppPreference(AreaActivity.this);
                int versionNumber = preference.getWorkFileBackupNumber();
                FileHandler.exportCSVForBackup(mPropertyInfo, path, versionNumber);
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
