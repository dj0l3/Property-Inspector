package com.sakthi.propertyinspector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.Question;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class QuestionPickActivity extends AppCompatActivity {

    private RecyclerView mQuestionsView;
    private QuestListAdapter mListAdapter;


    private static ArrayList<Question> mPickedQuestion=new ArrayList<>();


    public static void setPickedQuestion(ArrayList<Question> pickedQuestions){
        mPickedQuestion.clear();
        for(Question quest:pickedQuestions)mPickedQuestion.add(quest);
    }

    public static ArrayList<Question> getPickedQuestions(){
        return mPickedQuestion;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_question_pick);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Pick Questions");

        mQuestionsView=(RecyclerView)findViewById(R.id.questionPicker);
        mListAdapter=new QuestListAdapter(this);

        mQuestionsView.setLayoutManager(new LinearLayoutManager(this));
        mQuestionsView.setAdapter(mListAdapter);

        PropertyInfo lPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();

        ArrayList<Question> questList=lPropertyInfo.getDefaultQuestions();
        for(Question quest:questList){
            //mDefQuestions.add(quest.copy());
            mListAdapter.addItem(quest.copy(false),false);
        }

        findViewById(R.id.butAddQuestion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK);
                finish();
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

    public boolean isQuestionPicked(Question question){

        for(Question quest:mPickedQuestion){
            if(quest.getQuestionId()==question.getQuestionId())return true;
        }

        return false;
    }

    public void removeFromPick(Question quest){
        for(Question pickedQuest:mPickedQuestion){
            if(pickedQuest.getQuestionId()==quest.getQuestionId()){
                mPickedQuestion.remove(pickedQuest);
                break;
            }
        }
    }

    private class QuestViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener{

        private View mRootView;

        private TextView mQuestion;
        private RadioButton mRadioYes;
        private RadioButton mRadioNo;
        private Question mDataBinded;


        private CheckBox mIsPicked;


        public QuestViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;

            mQuestion = (TextView) mRootView.findViewById(R.id.txtQuestion);
            mRadioYes = (RadioButton) mRootView.findViewById(R.id.radioButYes);
            mRadioNo = (RadioButton) mRootView.findViewById(R.id.radioButNO);
           /* mActionYes = mRootView.findViewById(R.id.actionViewYes);
            mActionNo = mRootView.findViewById(R.id.actionViewNo);*/

        /*    mRadioYes.setOnCheckedChangeListener(this);
            mRadioNo.setOnCheckedChangeListener(this);*/
            mIsPicked=(CheckBox)mRootView.findViewById(R.id.pickCheckBox);
            mIsPicked.setOnCheckedChangeListener(this);
        }

        private boolean mIsCheckedOnBind;
        public void bind(final Question data){
            mDataBinded=data;
            mQuestion.setText((getAdapterPosition()+1)+") "+data.getQuestion()+"?");
            mIsCheckedOnBind=isQuestionPicked(data);
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

                if(isChecked&&!isQuestionPicked(mDataBinded))mPickedQuestion.add(mDataBinded);
                else if(!isChecked&&isQuestionPicked(mDataBinded))removeFromPick(mDataBinded);

            }


           /* if(!isChecked)return;

            if(buttonView==mRadioYes){
                mDataBinded.setAnswer(Question.YES);
            }else if(buttonView==mRadioNo){
                mDataBinded.setAnswer(Question.NO);
            }*/

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
            View view=mInflater.inflate(R.layout.list_question_pick_item,null);
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

}
