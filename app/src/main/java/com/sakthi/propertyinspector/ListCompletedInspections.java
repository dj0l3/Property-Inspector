package com.sakthi.propertyinspector;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sakthi.propertyinspector.adapter.ListInspectedFilesAdapter;
import com.sakthi.propertyinspector.data.FTPSettings;
import com.sakthi.propertyinspector.data.InspectedFiles;
import com.sakthi.propertyinspector.data.PhotoData;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.repository.FTPRepository;
import com.sakthi.propertyinspector.util.AppPreference;
import com.sakthi.propertyinspector.util.FileHandler;
import com.sakthi.propertyinspector.util.FileUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Satheesh on 12/06/17.
 * Sanghish IT Solutions
 * satheesh@sanghish.com
 */
public class ListCompletedInspections extends AppCompatActivity implements View.OnClickListener, FTPRepository.FTPConnectionListener,
        FTPRepository.FTPUploadListener {

    private ListInspectedFilesAdapter adapter;
    private LinearLayout lnrRoot;
    private TextView lblNoInspectedFiles;
    private RecyclerView recyclerView;
    private Snackbar snackFTPFailed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_completed_inspection);

        lnrRoot = (LinearLayout) findViewById(R.id.lnrRoot);
        lblNoInspectedFiles = (TextView) findViewById(R.id.lblNoInspectedFiles);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        snackFTPFailed = Snackbar.make(lnrRoot, "FTP Connection Failed. Please check your FTP settings.", Snackbar.LENGTH_LONG);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadFileLists();
    }

    private boolean isFilePresent(String path) {
        boolean exists;

        File file = new File(path);
        exists = file.exists();

        if (!exists) {
            file = new File(path);
            exists = file.exists();
        }

        return exists;
    }

    private void loadFileLists() {
        Gson gson = new Gson();
        AppPreference preference = new AppPreference(this);
        String inspectedFiles = preference.getInspectedProperty();
        ArrayList<InspectedFiles> propertyInfoArrayList = new ArrayList<>();
        try {
            JSONArray jsonArray;
            if (inspectedFiles != null && inspectedFiles.length() > 0) {
                jsonArray = new JSONArray(inspectedFiles);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    InspectedFiles existingProperty = gson.fromJson(jsonObject.toString(), InspectedFiles
                            .class);

                    if (isFilePresent(existingProperty.filePath)) {
                        if (propertyInfoArrayList.size() == 0) {
                            propertyInfoArrayList.add(existingProperty);
                        } else {
                            boolean alreadyExist = false;
                            for (InspectedFiles f : propertyInfoArrayList) {
                                if (f.filePath.equals(existingProperty.filePath)) {
                                    alreadyExist = true;
                                }
                            }

                            if (!alreadyExist) {
                                propertyInfoArrayList.add(existingProperty);
                            }
                        }

                        new FindUploadedFiles(existingProperty).execute();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new ListInspectedFilesAdapter(propertyInfoArrayList, this, this);
        recyclerView.setAdapter(adapter);
        if (propertyInfoArrayList != null && propertyInfoArrayList.size() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            lblNoInspectedFiles.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            lblNoInspectedFiles.setVisibility(View.VISIBLE);
        }
    }

    public void refreshRecyclerView() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        InspectedFiles inspectedFiles = null;
        switch (view.getId()) {
            case R.id.btnOpenInspectedFiles:
                inspectedFiles = (InspectedFiles) view.getTag(R.id.lblAddress);
                if (inspectedFiles != null) {
                    final String path = inspectedFiles.filePath;
                    final AppPreference pref = ((PropertyInspector) getApplication()).getPreference();

                    if (!FileUtil.isValidPropertyFile(path)) {
                        Snackbar.make(lnrRoot, "Pick Valid Property File", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    String workDir = FileUtil.getParentDirectory(path);
                    pref.setWorkDirPath(workDir);
                    pref.setWorkFilePath(path);
                    loadPropertyFile(path, false);
                    new StaticButton();

                }
                break;
            case R.id.btnUploadToFTP:
                if (isConnectedWifi()) {
                    inspectedFiles = (InspectedFiles) view.getTag(R.id.lblUniqueKey);
                    int position = (int) view.getTag(R.id.btnOpenInspectedFiles);
                    if (inspectedFiles != null) {
                        new UploadImagesFTP(inspectedFiles, position).execute();
                    }
                } else {
                    Toast.makeText(this, "Internet is not connected with WIFI network. Please connect through wifi and try to upload the " +
                            "files.", Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    private boolean isConnectedWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            // Do whatever
            return true;
        }
        return false;
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
                        info = new FileHandler().importCSV(path);
                    } else {
                        info = new FileHandler().importCSV(path);
//                        FileHandler.exportCSV(info,workingFile);
                        int versionNumber = ((PropertyInspector) getApplication()).getPreference().getWorkFileBackupNumber();
                        FileHandler.exportCSVForBackup(info, path, versionNumber);
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
                    Intent intnt = new Intent(ListCompletedInspections.this, PropertyInfoActivity.class);
                    startActivity(intnt);
                } else if (result != null) {
                    Snackbar.make(lnrRoot, result.toString(), Snackbar.LENGTH_LONG).show();
                }
            }

        }.execute();

    }

    @Override
    public void onConnectionFailed() {
        isFtpUploadSuccess = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (snackFTPFailed != null && !snackFTPFailed.isShown()) {
                    snackFTPFailed.show();
                }
            }
        });
    }

    @Override
    public void onConnectionSuccess() {
        isFtpUploadSuccess = true;
    }

    private ProgressDialog lUploadProgressDialog;
    private int fileUploadedCount;

    @Override
    public void uploadFileCompleted() {
        fileUploadedCount++;
        if (lUploadProgressDialog != null) {
            lUploadProgressDialog.setProgress(fileUploadedCount);
        }

        isFtpUploadSuccess = true;
    }

    boolean isFtpUploadSuccess;

    class UploadImagesFTP extends AsyncTask<Void, Void, Void> {


        private InspectedFiles inspectedFiles;
        private int position;
        private FTPRepository repository;
        private AppPreference pref;

        public UploadImagesFTP(InspectedFiles inspectedFiles, int position) {
            this.inspectedFiles = inspectedFiles;
            this.position = position;
            pref = ((PropertyInspector) getApplication()).getPreference();
            repository = new FTPRepository();
            repository.setFTPConnectionListener(ListCompletedInspections.this);
            repository.setFTPUploadListener(ListCompletedInspections.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            int filesToUploaded = 1;
            for (int i = 0; i < inspectedFiles.propertyInfo.getRoomItems().size(); i++) {
                RoomItem roomItem = inspectedFiles.propertyInfo.getRoomItems().get(i);
                for (int j = 0; j < roomItem.getPhotosList().size(); j++) {
                    filesToUploaded++;
                }
            }
            lUploadProgressDialog = new ProgressDialog(ListCompletedInspections.this);
            lUploadProgressDialog.setTitle("Please Wait");
            lUploadProgressDialog.setMessage("Uploading file to FTP....");
            lUploadProgressDialog.setCancelable(false);
            lUploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            lUploadProgressDialog.setProgress(fileUploadedCount);
            lUploadProgressDialog.setMax(filesToUploaded);
            lUploadProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... voids) {

            FTPSettings ftpSettings = new Gson().fromJson(pref.getFTPSettings(), FTPSettings.class);
            repository.uploadCSVFile(ftpSettings, new File(inspectedFiles.filePath),
                    inspectedFiles.propertyInfo.getPropertyId());
            for (int i = 0; i < inspectedFiles.propertyInfo.getRoomItems().size(); i++) {
                RoomItem roomItem = inspectedFiles.propertyInfo.getRoomItems().get(i);
                for (int j = 0; j < roomItem.getPhotosList().size(); j++) {
                    PhotoData photoData = roomItem.getPhotosList().get(j);
                    repository.uploadFile(ftpSettings, new File(photoData.getImagePath()),
                            inspectedFiles.propertyInfo.getPropertyId());

                    if (!repository.uploadFile(ftpSettings, new File(photoData.getImagePath()),
                            inspectedFiles.propertyInfo.getPropertyId())) {
                        isFtpUploadSuccess = false;
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (lUploadProgressDialog != null) {
                lUploadProgressDialog.dismiss();
            }
            if (repository != null) {
                repository.closeFTPUploadConnection();
            }
            if (isFtpUploadSuccess) {
                String inspectedFiles = pref.getInspectedProperty();

                try {
                    JSONArray jsonArray;
                    JSONArray newItemsArray = new JSONArray();
                    if (inspectedFiles != null && inspectedFiles.length() > 0) {
                        jsonArray = new JSONArray(inspectedFiles);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            if (i != position) {
                                newItemsArray.put(jsonArray.get(i));
                            }
                        }
                    }
                    pref.setInspectedProperty(newItemsArray.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                loadFileLists();
            } else {
                new android.app.AlertDialog.Builder(ListCompletedInspections.this)
                        .setTitle("Some files failed to upload on FTP")
                        .setMessage("Do you want to resume uploading?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new UploadImagesFTP(inspectedFiles, position).execute();
                    }
                }).setNegativeButton("No", null).show();
            }

        }

    }

    class FindUploadedFiles extends AsyncTask<Void, Void, Void> {

        private InspectedFiles inspectedFiles;
        final ProgressDialog dialog = new ProgressDialog(ListCompletedInspections.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog.setTitle("Please Wait");
            dialog.setMessage("Loading data....");
            dialog.setCancelable(false);
            dialog.show();
        }

        public FindUploadedFiles(InspectedFiles inspectedFiles) {
            this.inspectedFiles = inspectedFiles;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AppPreference pref = ((PropertyInspector) getApplication()).getPreference();
            FTPRepository repository = new FTPRepository();
            repository.setFTPConnectionListener(ListCompletedInspections.this);
            repository.setFTPUploadListener(ListCompletedInspections.this);
            try {
                FTPSettings ftpSettings = new Gson().fromJson(pref.getFTPSettings(), FTPSettings.class);
                inspectedFiles.setUploadedPhotos(repository.numberOfUploadedPictures(ftpSettings, inspectedFiles.propertyInfo.getPropertyId()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                repository.closeFTPUploadConnection();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            refreshRecyclerView();

            dialog.dismiss();
        }
    }

    public static class StaticButton {
        public static boolean isOpenSelectedFilePressed;

        public StaticButton() {
            isOpenSelectedFilePressed = true;
        }

        public static void setPressedFalse() {
            isOpenSelectedFilePressed = false;
        }
    }

}
