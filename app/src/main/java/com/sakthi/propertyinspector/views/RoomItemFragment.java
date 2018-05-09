package com.sakthi.propertyinspector.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sakthi.propertyinspector.GalleryActivity;
import com.sakthi.propertyinspector.PhotoCaptureActivity;
import com.sakthi.propertyinspector.PropertyInspector;
import com.sakthi.propertyinspector.R;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.Question;
import com.sakthi.propertyinspector.data.RoomItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by sakthivel on 6/9/2016.
 */
public class RoomItemFragment extends Fragment {


    private static HashMap<Integer,RoomItem> mapItems=new HashMap<>();
    public static HashMap<Integer,RoomItem> getChangedItems(){
        return mapItems;
    }
    public static void clear(){
        mapItems.clear();
    }

    public static RoomItemFragment newInstance(int roomId,int invId){

        RoomItemFragment roomItemFragment=new RoomItemFragment();
        Bundle args=new Bundle();
        args.putInt("ROOM_ID",roomId);
      //  args.putInt("ITEM_ID",itemId);
        args.putInt("INV_ID",invId);
        roomItemFragment.setArguments(args);

        return roomItemFragment;
    }

    private Spinner mConditionSpinner;
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
    //private int mItemId;
    private int mInvId;

    private RoomItem mRoomItem;
    private String[] mArrConditions;
    private String[] mArrColors;

    private Context mContext;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedState){
        super.onCreateView(inflater,container,savedState);

        Bundle args=getArguments();

        mRoomId=args.getInt("ROOM_ID");
        //mItemId=args.getInt("ITEM_ID");
        mInvId=args.getInt("INV_ID");
        mContext=getActivity();

        return inflater.inflate(R.layout.content_report,null);

    }


    @Override
    public void onViewCreated(View view,Bundle savedInstanceState) {

        super.onViewCreated(view,savedInstanceState);

        mPropertyInfo=((PropertyInspector)getActivity().getApplication()).getPropertyInfo();
        mArrConditions=mPropertyInfo.getConditions();
        mArrColors=mPropertyInfo.getColors();

        mConditionSpinner=(Spinner)view.findViewById(R.id.reportSpinnerCond);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item,mPropertyInfo.getConditions());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mConditionSpinner.setAdapter(arrayAdapter);

        mColorSpinner=(Spinner)view.findViewById(R.id.reportSpinnerColor);
        arrayAdapter=new ArrayAdapter(mContext,android.R.layout.simple_spinner_item,mPropertyInfo.getColors());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mColorSpinner.setAdapter(arrayAdapter);


        mQuestionsView=(RecyclerView)view.findViewById(R.id.itemQuestionsList);
        mListAdapter=new QuestListAdapter(mContext);

        mQuestionsView.setLayoutManager(new LinearLayoutManager(mContext));
        mQuestionsView.setAdapter(mListAdapter);

        mEditMake=(TextInputEditText)view.findViewById(R.id.reportEditMake);
        mEditModel=(TextInputEditText)view.findViewById(R.id.reportEditModel);
        mEditType=(TextInputEditText)view.findViewById(R.id.reportEditType);

        mEditNote=(TextInputEditText)view.findViewById(R.id.reportNote);
        mEditCNote=(TextInputEditText)view.findViewById(R.id.reportCleaningNote);

        mIsConditionTaskAdded=(CheckBox)view.findViewById(R.id.chkConditionTask);


        if(mRoomId!=-1&&mInvId!=-1){
            mRoomItem=mapItems.get(mInvId);

            if(mRoomItem==null) {
                Log.e("On Room Create"+mInvId,""+mapItems.size());
               // mRoomItem=mPropertyInfo.getRoomItemById(mRoomId,mItemId).copy();
                mRoomItem=mPropertyInfo.getRoomItemByInvId(mInvId).copy();
                mapItems.put(mRoomItem.getInventoryId(),mRoomItem);
            }

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

        mGallery=(TextView)view.findViewById(R.id.lblGallery);
        view.findViewById(R.id.actionCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent captureIntent=new Intent(mContext,PhotoCaptureActivity.class);
                captureIntent.putExtra("ROOM_ID",mRoomId);
                //captureIntent.putExtra("ITEM_ID",mItemId);
                captureIntent.putExtra("INV_ID",mInvId);
                startActivityForResult(captureIntent,100);
            }
        });

        view.findViewById(R.id.actionGallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent captureIntent=new Intent(mContext,GalleryActivity.class);
                captureIntent.putExtra("ROOM_ID",mRoomId);
               //captureIntent.putExtra("ITEM_ID",mItemId);
                captureIntent.putExtra("INV_ID",mInvId);
                startActivityForResult(captureIntent,100);
            }
        });


        mConditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                            int conditionId=mPropertyInfo.getConditionId(mArrConditions[position]);
                                                            mRoomItem.setConitionId(conditionId);
                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> parent) {

                                                        }
                                                    });

       /* mPhotos=(Button)findViewById(R.id.butPhotos);
        mPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent captureIntent=new Intent(ReportActivity.this,GalleryActivity.class);
                captureIntent.putExtra("ROOM_ID",mRoomId);
                captureIntent.putExtra("ITEM_ID",mItemId);
                startActivity(captureIntent);
            }
        });*/

    }




    public void onViewStateRestored(Bundle state){
        super.onViewStateRestored(state);

        //Toast.makeText(mContext,"State Restored:"+mRoomItem.getName(), Toast.LENGTH_SHORT).show();

    }

    public void onSaveInstanceState(Bundle savedState){
        super.onSaveInstanceState(savedState);
        updateData();


       // Toast.makeText(mContext,"On Saved State:"+mRoomItem.getName(), Toast.LENGTH_SHORT).show();
        //savedState.putString("Make",mEditMake.getText().toString().trim());
        //savedState.putString("Make",mEditMake.getText().toString().trim());


    }



    public void updateData(){
        String make=mEditMake.getText().toString().trim();
        String model=mEditModel.getText().toString().trim();
        String type=mEditType.getText().toString().trim();

        int conditionId=mPropertyInfo.getConditionId(mArrConditions[mConditionSpinner.getSelectedItemPosition()]);
        int colorId=mPropertyInfo.getColorsId(mArrColors[mColorSpinner.getSelectedItemPosition()]);

        String note=mEditNote.getText().toString().trim();
        String cNote=mEditCNote.getText().toString().trim();

        mRoomItem.setMake(make);
        mRoomItem.setModel(model);
        mRoomItem.setType(type);

        mRoomItem.setNote(note);
        mRoomItem.setCleaningNote(cNote);

        mRoomItem.setConitionId(conditionId);
        mRoomItem.setColor(colorId);

        mRoomItem.setCondIndiTask(mIsConditionTaskAdded.isChecked()?1:0);
    }

    public void onResume(){
        super.onResume();
        mGallery.setText("Gallery ("+mRoomItem.getNumberOfPhotos()+")");
    }

    public void onPause() {
        super.onPause();
    }

    public void onAttach(Context context){
        super.onAttach(context);
    }



    public RoomItem getRoomItem(){return  mRoomItem;}


    public void saveItems(){

        Log.e("Updating Room",mRoomItem.getName());
        updateData();


    }

    public String getTitle(){
        return mRoomItem.getName();
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
           /* mActionYes = mRootView.findViewById(R.id.actionViewYes);
            mActionNo = mRootView.findViewById(R.id.actionViewNo);*/

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

    public void onDetach(){
        super.onDetach();

    }


    private class QuestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{


        private LayoutInflater mInflater;
        private ArrayList<Question> mDataList;

        public QuestListAdapter(Context context){
            mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    }


    public void onDestroy(){
        super.onDestroy();
    }


}
