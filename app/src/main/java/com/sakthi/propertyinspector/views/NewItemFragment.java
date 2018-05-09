package com.sakthi.propertyinspector.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.sakthi.propertyinspector.BulkNewItemActivity;
import com.sakthi.propertyinspector.PropertyInspector;
import com.sakthi.propertyinspector.QuestionPickActivity;
import com.sakthi.propertyinspector.R;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.PropertyItem;
import com.sakthi.propertyinspector.data.Question;
import com.sakthi.propertyinspector.data.RoomItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by sakthivel on 6/16/2016.
 */
public class NewItemFragment extends Fragment {


    private static HashMap<Integer,RoomItem> mapItems=new HashMap<>();
    public static HashMap<Integer,RoomItem> getChangedItems(){
        return mapItems;
    }
    public static void clear(){
        mapItems.clear();
    }

    public static NewItemFragment newInstance(int propertyItemId,int fakeItemId,int roomId){

        NewItemFragment itemFrag=new NewItemFragment();
        Bundle args=new Bundle();
        args.putInt("Property_Item_Id",propertyItemId);
        args.putInt("Fake_Item_Id",fakeItemId);
        args.putInt("ROOM_ID",roomId);
        itemFrag.setArguments(args);

        return itemFrag;
    }


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

    private int mRoomId;
    private int mFakeItemId;
    private int mPropertyItemId;

    private RoomItem mRoomItem;
    private String[] mArrConditions;
    private String[] mArrColors;


    public View onCreateView(LayoutInflater inflater, ViewGroup group,Bundle savedState){

        View view=inflater.inflate(R.layout.content_new_item,null);

        view.findViewById(R.id.layoutDesc).setVisibility(View.VISIBLE);
        view.findViewById(R.id.layoutConditionTask).setVisibility(View.GONE);

        return view;
    }

    public void onViewCreated(View iView,Bundle savedState){

        super.onViewCreated(iView,savedState);

        mPropertyInfo=((PropertyInspector)getActivity().getApplication()).getPropertyInfo();
        mArrConditions=mPropertyInfo.getConditions();
        mArrColors=mPropertyInfo.getColors();

        mConditionSpinner=(Spinner)iView.findViewById(R.id.reportSpinnerCond);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,mPropertyInfo.getConditions());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mConditionSpinner.setAdapter(arrayAdapter);

        mColorSpinner=(Spinner)iView.findViewById(R.id.reportSpinnerColor);
        arrayAdapter=new ArrayAdapter(getActivity(),android.R.layout.simple_spinner_item,mPropertyInfo.getColors());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mColorSpinner.setAdapter(arrayAdapter);

        mDescSpinner=(Spinner)iView.findViewById(R.id.spinnerItemsDesc);
        ItemDescAdapter descAdapter=new ItemDescAdapter(getActivity(),android.R.layout.simple_spinner_item, BulkNewItemActivity.mDescItems);
        descAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        descAdapter.sort(new Comparator<PropertyItem>() {
            @Override
            public int compare(PropertyItem lhs, PropertyItem rhs) {
                return lhs.getDesc().compareToIgnoreCase(rhs.getDesc());
            }
        });
        mDescSpinner.setAdapter(descAdapter);

        mQuestionsView=(RecyclerView)iView.findViewById(R.id.itemQuestionsList);
        mListAdapter=new QuestListAdapter(getActivity());

        mQuestionsView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mQuestionsView.setAdapter(mListAdapter);

        mEditMake=(TextInputEditText)iView.findViewById(R.id.reportEditMake);
        mEditModel=(TextInputEditText)iView.findViewById(R.id.reportEditModel);
        mEditType=(TextInputEditText)iView.findViewById(R.id.reportEditType);

        mEditNote=(TextInputEditText)iView.findViewById(R.id.reportNote);
        mEditCNote=(TextInputEditText)iView.findViewById(R.id.reportCleaningNote);


        Bundle args=getArguments();
        mPropertyItemId=args.getInt("Property_Item_Id");
        mFakeItemId=args.getInt("Fake_Item_Id");
        mRoomId=args.getInt("ROOM_ID");

        if(mPropertyItemId!=-1){

            for(PropertyItem item:BulkNewItemActivity.mDescItems){
                if(item.getItemId()==mPropertyItemId){
                   mDescSpinner.setSelection(descAdapter.getPosition(item));
                }
            }
        }

        PropertyItem item=(PropertyItem)mDescSpinner.getSelectedItem();
        if (item != null) {
            ArrayList<Question> questions=mPropertyInfo.getQuestionsById(item.getQuestionsIndx());
            mListAdapter.setListItems(questions);
        }

        mRoomItem=mapItems.get(mFakeItemId);

        mDescSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PropertyItem item=(PropertyItem)mDescSpinner.getSelectedItem();
                if (item != null) {
                    mRoomItem.setItemId(item.getItemId());
                    ArrayList<Question> questions=mPropertyInfo.getQuestionsById(item.getQuestionsIndx());
                    mListAdapter.setListItems(questions);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        iView.findViewById(R.id.textAddQuestion).setVisibility(View.GONE); /*.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionPickActivity.setPickedQuestion(mListAdapter.getQuestItems());
                Intent intent=new Intent(NewItemActivity.this,QuestionPickActivity.class);
                startActivityForResult(intent,REQ_PICK_QUESTIONS);
            }
        });*/


    }




    private class QuestViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

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

        public void bind(final Question data) {
            mDataBinded = data;
            mQuestion.setText(data.getQuestion() + "?");

            if (mDataBinded.isAnswered()) {
                if (mDataBinded.getAnswer() == Question.YES) mRadioYes.setChecked(true);
                else mRadioNo.setChecked(true);
            }
        }


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (!isChecked) return;

            if (buttonView == mRadioYes) {
                mDataBinded.setAnswer(Question.YES);
            } else if (buttonView == mRadioNo) {
                mDataBinded.setAnswer(Question.NO);
            }

        }
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

    private class QuestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private LayoutInflater mInflater;
        private ArrayList<Question> mDataList;

        public QuestListAdapter(Context context){
            mInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    public RoomItem getRoomItem(){
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
        mRoomItem.setQuestions(questions);

        return mRoomItem;

    }




    public void onSaveInstanceState(Bundle outState){

       super.onSaveInstanceState(outState);
        getRoomItem();

       /* String desc=((PropertyItem)mDescSpinner.getSelectedItem()).getDesc();
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
        mRoomItem.setQuestions(questions);*/

    }


}
