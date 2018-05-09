package com.sakthi.propertyinspector;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.sakthi.propertyinspector.data.Area;
import com.sakthi.propertyinspector.data.FTPSettings;
import com.sakthi.propertyinspector.data.InspectedFiles;
import com.sakthi.propertyinspector.data.PhotoData;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.repository.FTPRepository;
import com.sakthi.propertyinspector.util.AppPreference;
import com.sakthi.propertyinspector.util.FileHandler;
import com.sakthi.propertyinspector.util.ParseUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

/**
 * Created by sakthivel on 5/16/2016.
 */
public class PropertyInfoActivity extends AppCompatActivity {

    private TextView mTextAddress;
    private TextView mTextPostal;
    private TextView mTextAlaram;
    private TextView mTextUnique;

    private TextView mNoteProperty;
    private TextView mNoteOwner;

    private EditText mReportername;
    private EditText mTenantName;

    private TextView mAreaView;
    private PropertyInfo mPropInfo;

    private TextView mAnsPerc;
    private TextView mNoQAnsw;
    private TextView mTotalQues;

    private TextView mPhotoPerc;
    private TextView mItemsLeftToCaptured;

    private PropertyInfo.QuestionStatics mInsStatics;
    private PropertyInfo.PhotoStatics mPhotoStatics;

    private Button btnDone;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_basic_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTextAddress = (TextView) findViewById(R.id.textAddress);
        mTextPostal = (TextView) findViewById(R.id.textPostalCode);
        mTextAlaram = (TextView) findViewById(R.id.textAlarmNo);
        mTextUnique = (TextView) findViewById(R.id.textUniqueKey);

        mNoteOwner = (TextView) findViewById(R.id.valOwnerInst);
        mNoteProperty = (TextView) findViewById(R.id.valPropertyInst);

        mPropInfo = ((PropertyInspector) getApplication()).getPropertyInfo();

        if (mPropInfo != null) {
            mTextAddress.setText(mPropInfo.getAddress());
            mTextPostal.setText(mPropInfo.getPostalCode());
            mTextAlaram.setText(String.valueOf(mPropInfo.getAlarmNumber()));
            mTextUnique.setText(String.valueOf(mPropInfo.getUniqueKey()));

            mNoteOwner.setText(mPropInfo.getOwnerInstruction());
            mNoteProperty.setText(mPropInfo.getPropertyInstruction());
        }

        mNoteOwner.setClickable(true);
        mNoteProperty.setClickable(true);

        findViewById(R.id.btnUploadToFTP).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPropInfo != null) {
                    new UploadImagesFTP().execute();
                }
            }
        });


        mNoteProperty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        mAreaView = (TextView) findViewById(R.id.textAreasInfo);


        findViewById(R.id.textAreaAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PropertyInfoActivity.this, AreaActivity.class);
                startActivity(intent);
            }
        });


        mReportername = (EditText) findViewById(R.id.editReporter);
        mTenantName = (EditText) findViewById(R.id.editTenant);

        final AppPreference pref = new AppPreference(getApplicationContext());

        mReportername.setText(pref.getReporterName());
        mTenantName.setText(pref.getTenantName());

        btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTaskWP asyncTask = new AsyncTaskWP(PropertyInfoActivity.this, "Generating final csv") {
                    ProgressDialog dialog;
                    @Override
                    public void onPreExecute() {
                        super.onPreExecute();
                        dialog = ProgressDialog.show(PropertyInfoActivity.this, null, "Generating final csv", false, false);
                    }

                    @Override
                    protected Object doInBackground(Object... params) {
                        PropertyInfo mPropertyInfo = ((PropertyInspector) getApplication()).getPropertyInfo();
                        String path = ((PropertyInspector) getApplication()).getPreference().getWorkFilePath();
                        FileHandler.exportCSV(mPropertyInfo, path);
                        return null;
                    }

                    public void onPostExecute(Object result) {
                        super.onPostExecute(result);
                        if (dialog != null && dialog.isShowing()) {
                            dialog.cancel();
                        }
                    }

                };

                asyncTask.execute();
            }
        });

        mReportername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                pref.setReporterName(s.toString());
            }
        });

        mTenantName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                pref.setTenantName(s.toString());
            }
        });

        mAnsPerc = (TextView) findViewById(R.id.textQAnsPerc);
        mNoQAnsw = (TextView) findViewById(R.id.textQAnswered);
        mTotalQues = (TextView) findViewById(R.id.textQTotal);

        mPhotoPerc = (TextView) findViewById(R.id.textCapturePerc);
        mItemsLeftToCaptured = (TextView) findViewById(R.id.textItemWOPhotos);

        mInsStatics = new PropertyInfo.QuestionStatics();
        mPhotoStatics = new PropertyInfo.PhotoStatics();

    }

    class UploadImagesFTP extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(PropertyInfoActivity.this, null, "Uploading images to FTP", false, false);

        }

        @Override
        protected Void doInBackground(Void... voids) {

            AppPreference pref = ((PropertyInspector) getApplication()).getPreference();
            FTPRepository repository = new FTPRepository();
            FTPSettings ftpSettings = new Gson().fromJson(pref.getFTPSettings(), FTPSettings.class);
            for (int i = 0; i < mPropInfo.getRoomItems().size(); i++) {
                RoomItem roomItem = mPropInfo.getRoomItems().get(i);
                for (int j = 0; j < roomItem.getPhotosList().size(); j++) {
                    PhotoData photoData = roomItem.getPhotosList().get(j);
                    repository.uploadFile(ftpSettings, new File(photoData.getImagePath()),
                            mPropInfo.getPropertyId());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }


    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else return super.onOptionsItemSelected(menuItem);
    }

    public void onResume() {
        super.onResume();
        refreshContent();

    }


    private void refreshContent() {

        AsyncTaskWP asyncTask = new AsyncTaskWP(this, "Refreshing") {
            @Override
            protected Object doInBackground(Object... params) {

               /* int totalQuestion=0;
                int ansQuestion=0;

                ArrayList<Area> areas=mPropInfo.getAreaList();

                for(Area area:areas){
                    PropertyInfo.QuestionStatics statics=mPropInfo.getRoomStatics(area);
                    totalQuestion+=statics.getTotalQuestions();
                    ansQuestion+=statics.getAnsweredQCount();
                }*/
                if (mPropInfo != null) {
                    mPropInfo.getOverallStatics(mInsStatics, mPhotoStatics);
                }
                return null;
            }

            public void onPostExecute(Object result) {
                super.onPostExecute(result);

                if (mPropInfo != null) {
                    int numberOfArea = mPropInfo.getAreaList().size();
                    mAreaView.setText("" + numberOfArea);
                }
                if (mInsStatics != null) {
                    mAnsPerc.setText(mInsStatics.percentage() + "%");
                    mTotalQues.setText("" + mInsStatics.getTotalQuestions());
                    mNoQAnsw.setText("" + mInsStatics.getAnsweredQCount());
                }
                if (mPhotoStatics != null) {
                    mPhotoPerc.setText(mPhotoStatics.percentage() + "%");
                    mItemsLeftToCaptured.setText("" + mPhotoStatics.getItemsNeedToBeCaptured());
                }


            }

        };

        asyncTask.execute();

    }

    public void onBackPressed() {
        Dialogs.showYesOrNoDialog(this, "", "Do you need to save changes?", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (which == DialogInterface.BUTTON_POSITIVE) {
                    dialog.dismiss();
                    saveItemInfo();
                } else {
                    dialog.dismiss();
                    finish();
                }

            }
        });
    }

    private void saveItemInfo() {
        new AsyncTaskWP(this, "Saving Property Information....") {
            @Override
            protected Object doInBackground(Object... params) {

                PropertyInfo mPropertyInfo = ((PropertyInspector) getApplication()).getPropertyInfo();
                String path = ((PropertyInspector) getApplication()).getPreference().getWorkFilePath();
//                FileHandler.exportCSV(mPropertyInfo, path);

                Gson gson = new Gson();
                AppPreference preference = new AppPreference(PropertyInfoActivity.this);
                int versionNumber = preference.getWorkFileBackupNumber();
                FileHandler.exportCSVForBackup(mPropertyInfo, path, versionNumber);
                String strInspectedFiles = preference.getInspectedProperty();

                try {
                    JSONArray jsonArray;
                    if (strInspectedFiles != null && strInspectedFiles.length() > 0) {
                        jsonArray = new JSONArray(strInspectedFiles);
                    } else {
                        jsonArray = new JSONArray();
                    }
                    int existingPosition = -1;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        InspectedFiles existingProperty = gson.fromJson(jsonObject.toString(), InspectedFiles
                                .class);
                        if (existingProperty.propertyInfo.getPropertyId() == mPropertyInfo.getPropertyId()) {
                            existingPosition = i;
                            break;
                        }
                    }
                    InspectedFiles inspectedFile = new InspectedFiles();
                    inspectedFile.propertyInfo = mPropertyInfo;
                    inspectedFile.filePath = path;
                    JSONObject propertyObject = new JSONObject(gson.toJson(inspectedFile));

                    if (existingPosition > 0) {
                        jsonArray.put(existingPosition, propertyObject);
                    } else {
                        jsonArray.put(propertyObject);
                    }
                    Log.d("TAG", "jsonArray: " + jsonArray);
                    preference.setInspectedProperty(jsonArray.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("TAG", "doInBackground: " + path);
                return null;

            }

            public void onPostExecute(Object result) {
                super.onPostExecute(result);
                finish();
            }

        }.execute();
    }


}

