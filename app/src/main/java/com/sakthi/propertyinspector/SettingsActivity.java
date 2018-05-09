package com.sakthi.propertyinspector;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.sakthi.propertyinspector.data.FTPSettings;
import com.sakthi.propertyinspector.data.PhotoData;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.repository.FTPRepository;
import com.sakthi.propertyinspector.util.AppPreference;
import com.sakthi.propertyinspector.util.FilePickerActivity;
import com.sakthi.propertyinspector.util.ParseUtil;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by sakthivel on 5/13/2016.
 */
public class SettingsActivity extends AppCompatActivity implements FTPRepository.FTPConnectionListener{


    private TextView mTextAppDir;
    private EditText mEditDefaultName, txtFTPUserName, txtFTPPassword, txtFTPHost;
    private Spinner mSpinnerQuality;


    private String[] mPictureSizes;

    private AppPreference mPreference;

    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_settings);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTextAppDir=(TextView) findViewById(R.id.valAppDirectory);
        mEditDefaultName=(EditText)findViewById(R.id.editExportName);
        mSpinnerQuality=(Spinner)findViewById(R.id.comboPhotoQuality) ;

        mPreference=((PropertyInspector)getApplication()).getPreference();
        mTextAppDir.setText(mPreference.getAppDirPath());

        txtFTPPassword = (EditText) findViewById(R.id.txtFTPPassword);
        txtFTPUserName = (EditText) findViewById(R.id.txtFTPUserName);
        txtFTPHost = (EditText) findViewById(R.id.txtFTPHost);

        findViewById(R.id.viewDirAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intnt=new Intent(SettingsActivity.this, FilePickerActivity.class);
                startActivityForResult(intnt,FilePickerActivity.REQ_PICK_DIR);
            }
        });


        if (Build.VERSION.SDK_INT >= 23){
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                listResolutions();
            }else{
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 231);
            }
        }else{
            listResolutions();
        }


        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,mPictureSizes);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerQuality.setAdapter(arrayAdapter);

        final AppPreference pref=((PropertyInspector)getApplication()).getPreference();
        final Gson gson = new Gson();
        FTPSettings ftpSettings = gson.fromJson(pref.getFTPSettings(), FTPSettings.class);
        if (ftpSettings != null) {
            txtFTPHost.setText(ftpSettings.host);
            txtFTPUserName.setText(ftpSettings.userName);
            txtFTPPassword.setText(ftpSettings.password);
        }

        findViewById(R.id.but_settings_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FTPSettings ftpSettings = new FTPSettings();
                ftpSettings.host = txtFTPHost.getText().toString().trim();
                ftpSettings.userName = txtFTPUserName.getText().toString().trim();
                ftpSettings.password = txtFTPPassword.getText().toString().trim();

                new ConnectToFTP(ftpSettings).execute();

                pref.setFTPSettings(gson.toJson(ftpSettings).toString());


            }
        });

        mPreference=((PropertyInspector)getApplication()).getPreference();
        mTextAppDir.setText(mPreference.getAppDirPath());
        if(mPreference.getPhotoQuality()!=null) mSpinnerQuality.setSelection(ParseUtil.getItemPosition(mPictureSizes,mPreference.getPhotoQuality()));
        mEditDefaultName.setText(mPreference.getExportName());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 231:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    listResolutions();
                }
                break;
        }
    }

    class ConnectToFTP extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;
        private FTPSettings ftpSettings;

        public ConnectToFTP(FTPSettings ftpSettings) {
            this.ftpSettings = ftpSettings;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(SettingsActivity.this, null, "Checking credentials to FTP", false, false);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            FTPRepository repository = new FTPRepository();
            repository.setFTPConnectionListener(SettingsActivity.this);
            repository.getConnection(ftpSettings);
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

    private void listResolutions(){
        Camera camera=Camera.open();
        Camera.Parameters params=camera.getParameters();
        List<Camera.Size> sizeList= params.getSupportedPictureSizes();
        mPictureSizes=new String[sizeList.size()];
        int indx=0;
        for(Camera.Size size:sizeList){
            mPictureSizes[indx++]=size.width+" x "+size.height;
        }
        camera.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilePickerActivity.REQ_PICK_DIR && resultCode == RESULT_OK) {
            //  String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            String path=data.getStringExtra(FilePickerActivity.PATH)+"/";
            mTextAppDir.setText(path);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){

        if(item.getItemId()==android.R.id.home){
            finish();
        }

       return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnectionFailed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingsActivity.this, "FTP Connection Failed. Please check your FTP settings.", Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    @Override
    public void onConnectionSuccess() {
        String dirPath=mTextAppDir.getText().toString();
        String size=mPictureSizes[mSpinnerQuality.getSelectedItemPosition()];
        String fileName=mEditDefaultName.getText().toString().trim();

        AppPreference pref=((PropertyInspector)getApplication()).getPreference();

        pref.setAppDirPath(dirPath);
        pref.setPhotoQuality(size);
        pref.setExportName(fileName);

        Snackbar.make(findViewById(R.id.but_settings_save),"Settings Updated",Snackbar.LENGTH_SHORT).show();
    }
}
