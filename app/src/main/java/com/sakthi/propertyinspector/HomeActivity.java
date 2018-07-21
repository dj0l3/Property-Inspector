package com.sakthi.propertyinspector;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.sakthi.propertyinspector.data.FTPSettings;
import com.sakthi.propertyinspector.data.PhotoData;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.util.AppPreference;
import com.sakthi.propertyinspector.util.FileHandler;
import com.sakthi.propertyinspector.util.FilePickerActivity;
import com.sakthi.propertyinspector.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {


    private Button mButOpen;
    private Button mButContinue;
    private Button mButInstance;
    private Button mButDeleteAllInspectedFiled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestPermission();

        AppPreference preference = new AppPreference(this);
        String ftpSettings = preference.getFTPSettings();
        if (TextUtils.isEmpty(ftpSettings)) {
            FTPSettings settings = new FTPSettings();
            settings.host = "ftp.crb115.co.uk";
            settings.userName = "upcond@crb115.co.uk";
            settings.password = "up115cond";
            preference.setFTPSettings(new Gson().toJson(settings));
        }

        mButContinue = (Button) findViewById(R.id.butContinueInspection);
        mButInstance = (Button) findViewById(R.id.butPickInstance);
        mButOpen = (Button) findViewById(R.id.butOpenInspection);
        mButDeleteAllInspectedFiled = (Button) findViewById(R.id.butDeleteAllInspectedFiles);

        mButContinue.setOnClickListener(this);
        mButInstance.setOnClickListener(this);
        mButOpen.setOnClickListener(this);
        mButDeleteAllInspectedFiled.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {


            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);


           /* final Intent chooserIntent = new Intent(this, DirectoryChooserActivity.class);

            final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                    .newDirectoryName("DirChooserSample")
                    .allowReadOnlyDirectory(true)
                    .allowNewDirectoryNameModification(true)
                    .build();

            chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);

            // REQUEST_DIRECTORY is a constant integer to identify the request, e.g. 0
            startActivityForResult(chooserIntent, 1);*/

            return true;
        }/*else if(id==R.id.action_propFile){

            openFolder();
            return true;
        }*/


        return super.onOptionsItemSelected(item);
    }


    public void openFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                + "/");
        intent.setDataAndType(uri, "text/csv");
        startActivity(Intent.createChooser(intent, "Pick Instance File"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FilePickerActivity.REQ_PICK_DIR && resultCode == RESULT_OK) {
            //  String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            String path = data.getStringExtra(FilePickerActivity.PATH);
            Snackbar.make(mButInstance, path, Snackbar.LENGTH_LONG).show();

        } else if (requestCode == FilePickerActivity.REQ_PICK_FILE && resultCode == RESULT_OK) {

            final String path = data.getStringExtra(FilePickerActivity.PATH);
            final AppPreference pref = ((PropertyInspector) getApplication()).getPreference();

            if (!FileUtil.isValidPropertyFile(path)) {
                Snackbar.make(mButInstance, "Pick Valid Property File", Snackbar.LENGTH_LONG).show();
                return;
            }


            if (mActionId == R.id.butOpenInspection) {
                String workDir = FileUtil.getParentDirectory(path);
                pref.setWorkDirPath(workDir);
                pref.setWorkFilePath(path);
                loadPropertyFile(path, false);
                return;
            }


            final String[] workFile = FileUtil.isInspectedAlready(path, pref.getAppDirPath(), pref.getExportName());
            if (workFile != null) {
                Dialogs.showYesOrNoDialog(this, "Resume Property Inspection?", "press \"yes\" to resume inspection or press \"no\" to start new inspection.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            resumePropertyInspection(path, workFile);
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            initNewPropertyInspection(path, pref.getAppDirPath(), pref.getExportName());
                        }
                    }
                });

            } else initNewPropertyInspection(path, pref.getAppDirPath(), pref.getExportName());


        }
    }

    private void resumePropertyInspection(String versionFilePath, String[] workFile) {
        // workFile[0] => Work dir path
        // workFile[1] => Base file path
        try {
            AppPreference pref = ((PropertyInspector) getApplication()).getPreference();
            pref.setWorkDirPath(workFile[0]);
            pref.setWorkFilePath(workFile[1]);
            FileUtil.copyVersionToBase(versionFilePath, workFile[1]);
            FileUtil.removeVersionFiles(workFile[0], workFile[1]);
            loadPropertyFile(workFile[1], false);
        } catch (Exception e) {
            Snackbar.make(mButContinue, "Error while resuming inspection", Snackbar.LENGTH_LONG).show();
        }
    }

    private void initNewPropertyInspection(String path, String appPath, String exportName) {
        try {
            String[] workFileInfo = FileUtil.initFilesForInspection(path, appPath, exportName);
            AppPreference appPref = ((PropertyInspector) getApplication()).getPreference();
            appPref.setWorkDirPath(workFileInfo[0]);
            appPref.setWorkFilePath(workFileInfo[1]);
            loadPropertyFile(path, true);
        } catch (Exception e) {
            Snackbar.make(mButInstance, e.getMessage(), Snackbar.LENGTH_LONG).show();
            // Dialogs.showAlertDialog(this,"Error",e.getMessage());
        }
    }


    private void loadPropertyFile(final String path, final boolean newFile) {


        final ProgressDialog lProgressDialog = new ProgressDialog(this);
        lProgressDialog.setTitle("Please Wait");
        lProgressDialog.setMessage("Loading property file....");
        lProgressDialog.setCancelable(false);

        new AsyncTask<Object, Object, Object>() {

            public void onPreExecute() {
                lProgressDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] params) {
                try {

                    AppPreference appPref = ((PropertyInspector) getApplication()).getPreference();
                    String workingDir = appPref.getWorkDirPath();
                    String workingFile = appPref.getWorkFilePath();


                    PropertyInfo info;

                    if (!newFile) {
                        ((PropertyInspector)getApplication()).getPreference().setWorkFileBackupNumber(0);
                        info = new FileHandler().importCSV(path);
                    } else {
                        info = new FileHandler().importCSV(path);
//                        FileHandler.exportCSV(info, workingFile);
                        ((PropertyInspector)getApplication()).getPreference().setWorkFileBackupNumber(0);
                        int versionNumber = ((PropertyInspector)getApplication()).getPreference().getWorkFileBackupNumber();
                        FileHandler.exportCSVForBackup(info, workingFile, versionNumber);
                    }

                   /* if(path!=null){
                        info=new FileHandler().importCSV(path);
                        FileHandler.exportCSV(info,workingFile);
                    }else {
                        info=new FileHandler().importCSV(workingFile);
                    }*/

                    ((PropertyInspector) getApplication()).setPropertyInfo(info);
                    String imageDir = workingDir + "images/";

                    if (newFile) {

                        File dirImage = new File(imageDir);
                        File[] imageFiles = dirImage.listFiles();
                        for (File file : imageFiles) file.delete();

                        return info;

                    }

                    ArrayList<RoomItem> roomItems = info.getRoomItems();
                    String prefix = info.getClientId() + "_" + info.getPropertyId() + "_";
                    for (RoomItem item : roomItems) {
                        if (item.isItemDeleted()) continue;

                        String filter = prefix + item.getRoomId() + "_" + item.getInventoryId() + "_" + item.getItemId();
                        File[] files = FileUtil.getImageFiles(imageDir, filter);
                        for (File file : files) {
                            Log.e(item.getRoomId() + ":" + item.getItemId(), file.getName());
                            item.addItemPhoto(new PhotoData(1, file.getName(), file.getPath()));
                        }
                    }

                    return info;
                } catch (Exception e) {
                    return e.getMessage();
                }

            }

            public void onPostExecute(Object result) {
                lProgressDialog.cancel();

                if (result != null && result instanceof PropertyInfo) {
                    Intent intnt = new Intent(HomeActivity.this, PropertyInfoActivity.class);
                    startActivity(intnt);
                } else if (result != null) {
                    Snackbar.make(mButInstance, result.toString(), Snackbar.LENGTH_LONG).show();
                }
            }

        }.execute();

    }


    public void onResume() {
        super.onResume();

        AppPreference appPref = ((PropertyInspector) getApplication()).getPreference();
        findViewById(R.id.butContinueInspection).setVisibility(appPref.getWorkFilePath() == null ? View.GONE : View.VISIBLE);
    }


    private int mActionId;

    @Override
    public void onClick(View v) {

        mActionId = v.getId();

        if (mActionId == R.id.butPickInstance) {
            Intent intnt = new Intent(this, FilePickerActivity.class);
            intnt.putExtra(FilePickerActivity.REQ_MODE, FilePickerActivity.REQ_PICK_FILE);
            startActivityForResult(intnt, FilePickerActivity.REQ_PICK_FILE);
            ListCompletedInspections.StaticButton.setPressedFalse();

        } else if (mActionId == R.id.butContinueInspection) {
            String versionFilePath = ((PropertyInspector) getApplication()).getPreference().getWorkFilePath();
            String[] workFile = FileUtil.getDirAndBaseFilePath(versionFilePath);
            resumePropertyInspection(versionFilePath, workFile);
            //loadPropertyFile(((PropertyInspector) getApplication()).getPreference().getWorkFilePath(), false);
        } else if (mActionId == R.id.butOpenInspection) {
//            Intent intnt=new Intent(this, FilePickerActivity.class);
//            intnt.putExtra(FilePickerActivity.REQ_MODE,FilePickerActivity.REQ_PICK_FILE);
//            intnt.putExtra(FilePickerActivity.PICK_ROOT_DIR,((PropertyInspector)getApplication()).getPreference().getAppDirPath());
//            startActivityForResult(intnt,FilePickerActivity.REQ_PICK_FILE);
            Intent intnt = new Intent(this, ListCompletedInspections.class);
            startActivity(intnt);
        } else if (mActionId == R.id.butDeleteAllInspectedFiles) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_title_delete);
            //add pass to dialog
            builder.setMessage(R.string.dialog_message_delete_all_inspected_files);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        int deletedFilesCount = FileUtil.clearAllInspectionFolders(getApplicationContext());
                        Toast.makeText(HomeActivity.this, getString(R.string.message_files_deleted, deletedFilesCount),
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(HomeActivity.this, getString(R.string.error_deleting_files, e.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }

                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }
    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Toast.makeText(FilePickerActivity.this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can save image .");
                } else {
                    Log.e("value", "Permission Denied, You cannot save image.");
                }
                break;
        }
    }
}
