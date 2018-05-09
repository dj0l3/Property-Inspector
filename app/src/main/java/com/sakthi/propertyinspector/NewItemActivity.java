package com.sakthi.propertyinspector;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.PropertyItem;
import com.sakthi.propertyinspector.data.Question;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.util.FileHandler;

import java.util.ArrayList;
import java.util.Comparator;

import io.fabric.sdk.android.Fabric;

public class NewItemActivity extends AppCompatActivity {


    private Spinner mConditionSpinner;
    private Spinner mColorSpinner;
    private Spinner mDescSpinner;

    private PropertyInfo mPropertyInfo;
    private RecyclerView mQuestionsView;
    private QuestListAdapter mListAdapter;

    private TextInputEditText mEditMake;
    private TextInputEditText mEditModel;
    private TextInputEditText mEditType;

    private TextInputEditText mEditNote;
    private TextInputEditText mEditCNote;

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
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_new_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("New Items");

        findViewById(R.id.layoutDesc).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutConditionTask).setVisibility(View.GONE);


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

        mDescSpinner=(Spinner)findViewById(R.id.spinnerItemsDesc);
        ItemDescAdapter descAdapter=new ItemDescAdapter(this,android.R.layout.simple_spinner_item,mPropertyInfo.getPropertyItems());
        descAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        descAdapter.sort(new Comparator<PropertyItem>() {
                             @Override
                             public int compare(PropertyItem lhs, PropertyItem rhs) {
                                 return lhs.getDesc().compareToIgnoreCase(rhs.getDesc());
                             }
                         });
                mDescSpinner.setAdapter(descAdapter);

        mQuestionsView=(RecyclerView)findViewById(R.id.itemQuestionsList);
        mListAdapter=new QuestListAdapter(this);

        mQuestionsView.setLayoutManager(new LinearLayoutManager(this));
        mQuestionsView.setAdapter(mListAdapter);

        mEditMake=(TextInputEditText)findViewById(R.id.reportEditMake);
        mEditModel=(TextInputEditText)findViewById(R.id.reportEditModel);
        mEditType=(TextInputEditText)findViewById(R.id.reportEditType);

        mEditNote=(TextInputEditText)findViewById(R.id.reportNote);
        mEditCNote=(TextInputEditText)findViewById(R.id.reportCleaningNote);

        mRoomId=getIntent().getIntExtra("ROOM_ID",-1);
        mRoomItem=new RoomItem(mRoomId);

       // mRoomId=getIntent().getIntExtra("ROOM_ID",-1);
        //smItemId=getIntent().getIntExtra("ITEM_ID",-1);


        if(mRoomId!=-1&&mItemId!=-1){
           /* mRoomItem=mPropertyInfo.getRoomItemById(mRoomId,mItemId);
            setTitle(mRoomItem.getName() +" Report");
            mListAdapter.setListItems(mRoomItem.getQuestions());

            mEditMake.setText(mRoomItem.getMake());
            mEditModel.setText(mRoomItem.getModel());
            mEditType.setText(mRoomItem.getType());

            mEditNote.setText(mRoomItem.getNote());
            mEditCNote.setText(mRoomItem.getCleaningNote());

            mColorSpinner.setSelection(mPropertyInfo.getItemPosition(mArrColors,mPropertyInfo.getColor(mRoomItem.getColorId())));
            mConditionSpinner.setSelection(mPropertyInfo.getItemPosition(mArrConditions,mPropertyInfo.getCondition(mRoomItem.getConditionId())));*/
        }

        mGallery=(TextView)findViewById(R.id.lblGallery);
       /* findViewById(R.id.actionCamera).setOnClickListener(new View.OnClickListener() {
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
        */

        PropertyItem item=(PropertyItem)mDescSpinner.getSelectedItem();
        if (item != null) {
            ArrayList<Question> questions=mPropertyInfo.getQuestionsById(item.getQuestionsIndx());
            mListAdapter.setListItems(questions);
        }

        mDescSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PropertyItem item=(PropertyItem)mDescSpinner.getSelectedItem();
                if (item != null) {
                    ArrayList<Question> questions=mPropertyInfo.getQuestionsById(item.getQuestionsIndx());
                    mListAdapter.setListItems(questions);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        findViewById(R.id.textAddQuestion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionPickActivity.setPickedQuestion(mListAdapter.getQuestItems());
                Intent intent=new Intent(NewItemActivity.this,QuestionPickActivity.class);
                startActivityForResult(intent,REQ_PICK_QUESTIONS);
            }
        });

    }

    public static final int REQ_PICK_QUESTIONS=123;

    public void onActivityResult(int req,int res,Intent data){
        if(res== Activity.RESULT_OK&&req==REQ_PICK_QUESTIONS){

            ArrayList<Question> pickedQuestions=QuestionPickActivity.getPickedQuestions();
            ArrayList<Question> adapterQuestion=mListAdapter.getQuestItems();

            int size=pickedQuestions.size();
            int nsize=adapterQuestion.size();

            mListAdapter.setListItems(pickedQuestions);

           /* for(Question qst:pickedQuestions){
                if(!adapterQuestion.contains(qst))mListAdapter.addItem(qst,true);
            }*/

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menuSaveItem) {
            saveItemInfo();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    private class ItemDescAdapter extends ArrayAdapter<PropertyItem>{

        public ItemDescAdapter(Context context, int resource,ArrayList<PropertyItem> items) {
            super(context, resource,items);
        }


        public View getView(int position, View convertView, ViewGroup parent){

            TextView view=(TextView)super.getView(position,convertView,parent);
            view.setText(getItem(position).getDesc());

            return view;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent){

            TextView view=(TextView)super.getDropDownView(position,convertView,parent);
            view.setText(getItem(position).getDesc());

            return view;
        }

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
            removeAll();
            for(Question itemQuest:listItems){
                mDataList.add(itemQuest);
            }
            notifyDataSetChanged();
        }


        public ArrayList<Question> getQuestItems(){
            return mDataList;
        }

        public void removeAll(){
            int size=mDataList.size();
            if(size==0)return;
            mDataList.clear();
            notifyItemRangeRemoved(0,size);
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

    // private boolean mIsChanged;
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
        new AsyncTaskWP(this,"Saving Item Details....."){


            public void onPreExecute(){
                super.onPreExecute();

                String desc=((PropertyItem)mDescSpinner.getSelectedItem()).getDesc();
                String make=mEditMake.getText().toString().trim();
                String model=mEditModel.getText().toString().trim();
                String type=mEditType.getText().toString().trim();

                int conditionId=mPropertyInfo.getConditionId(mArrConditions[mConditionSpinner.getSelectedItemPosition()]);
                int colorId=mPropertyInfo.getColorsId(mArrColors[mColorSpinner.getSelectedItemPosition()]);

                String note=mEditNote.getText().toString().trim();
                String cNote=mEditCNote.getText().toString().trim();


                mRoomItem.setName(desc);
                mRoomItem.setMake(make);
                mRoomItem.setModel(model);
                mRoomItem.setType(type);

                mRoomItem.setNote(note);
                mRoomItem.setCleaningNote(cNote);

                mRoomItem.setConitionId(conditionId);
                mRoomItem.setColor(colorId);


                ArrayList<Question>questions=mListAdapter.getQuestItems();
                for(Question qst:questions)mRoomItem.addQuestion(qst);

                mPropertyInfo.addNewRoomItem(mRoomItem);

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

}

