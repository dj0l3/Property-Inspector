package com.sakthi.propertyinspector.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sakthi.propertyinspector.ListCompletedInspections;
import com.sakthi.propertyinspector.PropertyInspector;
import com.sakthi.propertyinspector.R;
import com.sakthi.propertyinspector.data.FTPSettings;
import com.sakthi.propertyinspector.data.SearchResultFile;
import com.sakthi.propertyinspector.repository.FTPRepository;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;


public class FilePickerActivity extends AppCompatActivity implements FTPRepository.FTPConnectionListener, FTPRepository.FTPSearchListener{

    private Toolbar toolbar;
    private FragmentManager fragmentManager = null;
    private FragmentTransaction fragmentTransaction = null;
    private DirectoryFragment mDirectoryFragment;


    public static final String PATH="PATH";
    public static final String REQ_MODE="REQ_MODE";
    public static final String PICK_ROOT_DIR="ROOT_DIR";

    public static final int REQ_PICK_DIR=0x1243;
    public static final int REQ_PICK_FILE=0x1256;

    public static final int PICK_FROM_SPECIFIC_DIR=1;

    private int mReqMode;
    private String mRootDir;
    private boolean searchTlsPressed = false;
    //private Button mButChoose;

    private static final String TAG = "FilePickerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Directory");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mReqMode=getIntent().getIntExtra(REQ_MODE,REQ_PICK_DIR);
        mRootDir=getIntent().getStringExtra(PICK_ROOT_DIR);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        mDirectoryFragment = new DirectoryFragment();
        mDirectoryFragment.setOption(mReqMode);
        mDirectoryFragment.setDefaultRoot(mRootDir);

        mDirectoryFragment.setDelegate(new DirectoryFragment.DocumentSelectActivityDelegate() {

            @Override
            public void startDocumentSelectActivity() {

            }

            @Override
            public void didSelectFiles(DirectoryFragment activity,
                                       ArrayList<String> files) {
                Intent intent=new Intent();
                intent.putExtra(PATH,files.get(0));
                setResult(Activity.RESULT_OK,intent);
                finish();
            }

            @Override
            public void updateToolBarName(String name) {
                toolbar.setTitle(name);

            }
        });
        fragmentTransaction.add(R.id.fragment_container, mDirectoryFragment, "" + mDirectoryFragment.toString());
        fragmentTransaction.commit();


        /*findViewById(R.id.butPickCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                setResult(Activity.RESULT_CANCELED,intent);
                finish();
            }
        });


       mButChoose= (Button) findViewById(R.id.butChoose);
        mButChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        String hint=mReqMode==REQ_PICK_DIR?" * Press & Hold to choose directory" : " * Pick property csv file";
        TextView hintView= (TextView) findViewById(R.id.hintFileBrowser);
        hintView.setText(hint);

        final EditText txtSearch = (EditText) findViewById(R.id.txtSearch);
        findViewById(R.id.btnSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtSearch != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);
                }
                searchFileName = txtSearch.getText().toString().trim();
                if (TextUtils.isEmpty(searchFileName)){
                    txtSearch.setError("Please enter file name to search.");
                    return;
                }
                if (Build.VERSION.SDK_INT >= 23){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        new FileSearchTask(false).execute();
                    }else {
                        Toast.makeText(FilePickerActivity.this, "Please grant permission in settings to search", Toast.LENGTH_LONG).show();
                    }

                }else{
                    new FileSearchTask(false).execute();
                }

            }
        });

        findViewById(R.id.btnSearchTls).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTlsPressed = true;
                if (txtSearch != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);
                }
                searchFileName = txtSearch.getText().toString().trim();
                if (TextUtils.isEmpty(searchFileName)){
                    txtSearch.setError("Please enter file name to search.");
                    return;
                }
                new FileSearchTask(true).execute();

            }
        });

        findViewById(R.id.downloadedFilesBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTlsPressed = false;
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(txtSearch.getWindowToken(), 0);
                txtSearch.setText("0");
                txtSearch.setSelection(txtSearch.getText().length());
                new FileSearchTask(true).execute();
            }
        });

    }

    private ArrayList<SearchResultFile> listFiles = new ArrayList<>();

    public ArrayList<SearchResultFile> findFiles(File dir, String name) {
        File[] children = dir.listFiles();

        if (children != null) {
            Log.d(TAG, "Folder Name: "+dir.getAbsolutePath());

            for(File child : children) {
                if(child.isDirectory()) {
                    findFiles(child, name);
                } else {
                    Log.d(TAG, "findFiles: "+child.getName()+" == "+child.getAbsolutePath());
                    if(child.getName().contains(name) && child.getName().endsWith(".csv")) {
                        SearchResultFile resultFile = new SearchResultFile();
                        resultFile.name = child.getName();
                        resultFile.path = child.getAbsolutePath();
                        resultFile.isLocateInFTP = false;
                        resultFile.fileSize = child.length();
                        listFiles.add(resultFile);
                    }
                }
            }
        }

        return listFiles;
    }

    @Override
    public void onConnectionFailed() {

    }

    @Override
    public void onConnectionSuccess() {

    }

    @Override
    public void searchCompleted(ArrayList<SearchResultFile> searchResultFiles) {
        if (searchResultFiles != null && searchResultFiles.size()>0) {
            for (SearchResultFile resultFile : searchResultFiles){
                if (resultFile.isDir){
                    doSearchInFTPDir(resultFile.path);
                }else if (resultFile.name.contains(searchFileName) && resultFile.name.endsWith(".csv")){
                    listFiles.add(resultFile);
                }
            }
        }
    }

    String searchFileName = "";
    AppPreference pref;
    FTPRepository repository;
    FTPSettings ftpSettings;

    class FileSearchTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog lProgressDialog;
        private boolean searchFTP;


        public FileSearchTask(boolean searchFTP) {
            this.searchFTP = searchFTP;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lProgressDialog = new ProgressDialog(FilePickerActivity.this);
            lProgressDialog.setTitle("Please Wait");
            lProgressDialog.setMessage("Searching file....");
            lProgressDialog.setCancelable(false);
            lProgressDialog.show();

            pref = ((PropertyInspector)getApplication()).getPreference();
            repository = new FTPRepository();
            ftpSettings = new Gson().fromJson(pref.getFTPSettings(), FTPSettings.class);

        }

        @Override
        protected Void doInBackground(Void... voids) {
            listFiles = new ArrayList<>();
            if (!searchFTP){
                String[] paths = getExternalStorageDirectories();
                Log.d(TAG, "onPostExecute: "+paths.length);
                for (int i=0; i<paths.length+1; i++){
                    File folderFile;
                    if (i == 0 ){
                        folderFile = Environment.getExternalStorageDirectory();
                    }else{
                        folderFile = new File(paths[i-1]);
                    }
                    findFiles(folderFile, searchFileName);
                }
            }else{
                repository.setFTPConnectionListener(FilePickerActivity.this);
                repository.setSearchResultsLis(FilePickerActivity.this);
                try {
                    repository.searchFIleInFTP(ftpSettings, searchFileName, "/csv");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute: "+listFiles.size());

            if (lProgressDialog != null && lProgressDialog.isShowing()){
                lProgressDialog.dismiss();
            }

            if (searchTlsPressed) {
                if (listFiles != null && listFiles.size() > 0) {
                    showSearchResults(listFiles);

                    filesToDownloadFromFTP.clear();
                    lastDownloadedFiles.clear();

                    for (SearchResultFile f : listFiles) {
                        if (!isFilePresent(f)) {
                            f.isAddedToDownload = true;
                            filesToDownloadFromFTP.add(f);
                            f.enabled = true;
                        }
                    }
                } else {
                    Toast.makeText(FilePickerActivity.this, "Could not find any file name consists of " + searchFileName, Toast.LENGTH_LONG)
                            .show();
                }
            } else {
                if (listFiles != null && listFiles.size() > 0) {
                    for (SearchResultFile f : listFiles) {
                        if (isFilePresentAndSameDateModified(f)) {
                            f.isAddedToDownload = true;
                            f.enabled = true;
                        }
                    }

                    showDownloadedSearchResults(listFiles);
                } else {
                    Toast.makeText(FilePickerActivity.this, "Could not find any file name consists of " + searchFileName, Toast.LENGTH_LONG)
                            .show();
                }
            }

        }
    }

    private boolean isFilePresent(SearchResultFile f) {
        boolean exists;
        String path = Environment.getExternalStorageDirectory() + "/tls/csv/" + f.name;

        File file = new File(path);
        exists = file.exists();

        if (!exists) {
            path = getApplicationContext().getFilesDir().getAbsolutePath() + "/csv/" + f.name;
            file = new File(path);
            exists = file.exists();
        }

        return exists;
    }

    private boolean isFilePresentAndSameDateModified(SearchResultFile f) {
        boolean exists;
        String path = Environment.getExternalStorageDirectory() + "/tls/csv/" + f.name;

        File file = new File(path);
        exists = file.exists();

        if (file.lastModified() != 0) {
            if (f.lastModified > file.lastModified()) {
                return true;
            }
        }

        return false;
    }

    private void doSearchInFTPDir(String path){
        try {
            repository.searchFIleInFTP(ftpSettings, searchFileName, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Dialog dialogSearchResult;
    private SearchResultsAdapter adapter;
    private CheckBox chkSelectAll;

    private void showSearchResults(final ArrayList<SearchResultFile> files){
        filesToDownloadFromFTP = new ArrayList<>();
        dialogSearchResult = new Dialog(FilePickerActivity.this);
        dialogSearchResult.setContentView(R.layout.dialog_list_search_results);
        final RecyclerView recyclerView = (RecyclerView) dialogSearchResult.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        chkSelectAll = (CheckBox) dialogSearchResult.findViewById(R.id.chkSelectAll);

        if (files != null && files.size() > 0){
            if (files.get(0).isLocateInFTP){
                dialogSearchResult.findViewById(R.id.btnDownload).setVisibility(View.VISIBLE);
                chkSelectAll.setVisibility(View.VISIBLE);
            }else {
                dialogSearchResult.findViewById(R.id.btnDownload).setVisibility(View.GONE);
                chkSelectAll.setVisibility(View.GONE);
            }
        }else{
            dialogSearchResult.findViewById(R.id.btnDownload).setVisibility(View.GONE);
            chkSelectAll.setVisibility(View.GONE);
        }

        chkSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    filesToDownloadFromFTP.clear();
                    for (int i=0; i<listFiles.size(); i++){
                        SearchResultFile resultFile = listFiles.get(i);
                        if (resultFile.enabled) {
                            resultFile.isAddedToDownload = true;
                            listFiles.set(i, resultFile);
                            filesToDownloadFromFTP.add(resultFile);
                        }
                    }

                }else{
                    filesToDownloadFromFTP.clear();
                    for (int i=0; i<listFiles.size(); i++){
                        SearchResultFile resultFile = listFiles.get(i);
                        if (resultFile.enabled) {
                            resultFile.isAddedToDownload = false;
                            listFiles.set(i, resultFile);
                        }
                    }
                }

                adapter.refreshItem(listFiles);
            }
        });

        dialogSearchResult.findViewById(R.id.btnDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSearchResult.dismiss();
                if (filesToDownloadFromFTP.size() > 0){
                    new DownloadFileTask().execute();
                }else{
                    Toast.makeText(FilePickerActivity.this, "Select the files to be download.", Toast.LENGTH_LONG).show();
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(FilePickerActivity.this));
        if (adapter != null) {
            adapter.refreshItem(listFiles);
        }else{
            adapter = new SearchResultsAdapter(files);
        }

        recyclerView.setAdapter(adapter);
        dialogSearchResult.setCancelable(true);
        dialogSearchResult.setTitle("Search Results");
        dialogSearchResult.show();
    }

    private void showDownloadedSearchResults(final ArrayList<SearchResultFile> files){
        filesToDownloadFromFTP = new ArrayList<>();
        dialogSearchResult = new Dialog(FilePickerActivity.this);
        dialogSearchResult.setContentView(R.layout.dialog_list_search_results);
        RecyclerView recyclerView = (RecyclerView) dialogSearchResult.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        chkSelectAll = (CheckBox) dialogSearchResult.findViewById(R.id.chkSelectAll);

        if (files != null && files.size() > 0){
            if (files.get(0).isLocateInFTP){
                dialogSearchResult.findViewById(R.id.btnDownload).setVisibility(View.GONE);
                chkSelectAll.setVisibility(View.GONE);
            }else {
                dialogSearchResult.findViewById(R.id.btnDownload).setVisibility(View.GONE);
                chkSelectAll.setVisibility(View.GONE);
            }
        }else{
            dialogSearchResult.findViewById(R.id.btnDownload).setVisibility(View.GONE);
            chkSelectAll.setVisibility(View.GONE);
        }

        chkSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    filesToDownloadFromFTP.clear();
                    for (int i=0; i<listFiles.size(); i++){
                        SearchResultFile resultFile = listFiles.get(i);
                        resultFile.isAddedToDownload = true;
                        listFiles.set(i, resultFile);
                        filesToDownloadFromFTP.add(resultFile);
                    }

                }else{
                    filesToDownloadFromFTP.clear();
                    for (int i=0; i<listFiles.size(); i++){
                        SearchResultFile resultFile = listFiles.get(i);
                        resultFile.isAddedToDownload = false;
                        listFiles.set(i, resultFile);
                    }
                }

                adapter.refreshItem(listFiles);
            }
        });

        dialogSearchResult.findViewById(R.id.btnDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogSearchResult.dismiss();
                if (filesToDownloadFromFTP.size() > 0){
                    new DownloadFileTask().execute();
                }else{
                    Toast.makeText(FilePickerActivity.this, "Select the files to be download.", Toast.LENGTH_LONG).show();
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(FilePickerActivity.this));
        if (adapter != null) {
            adapter.refreshItem(listFiles);
        }else{
            adapter = new SearchResultsAdapter(files);
        }

        recyclerView.setAdapter(adapter);
        dialogSearchResult.setCancelable(true);
        dialogSearchResult.setTitle("New and updated files");
        dialogSearchResult.show();
    }


    @Override
    protected void onDestroy() {
        mDirectoryFragment.onFragmentDestroy();
        super.onDestroy();
    }

   /* @Override
    public void onBackPressed() {
        if (mDirectoryFragment.onBackPressed_()) {
            super.onBackPressed();
        }
    }*/



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(mDirectoryFragment.onBackPressed_())finish();
               // Or what ever action you want here.
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<SearchResultFile> filesToDownloadFromFTP;
    private ArrayList<SearchResultFile> lastDownloadedFiles = new ArrayList<>();

    private boolean isDownloadFiled = false;

    class SearchResultViewHolder extends RecyclerView.ViewHolder{

        private TextView lblFileName;
        private TextView lblFilePath;
        private TextView lblFileSize;
        private LinearLayout lnrRoot;
        private ImageView imgDownload;
        private CheckBox chkSelectFile;

        public SearchResultViewHolder(View itemView) {
            super(itemView);
            lblFileName = (TextView) itemView.findViewById(R.id.lblFileName);
            lblFilePath = (TextView) itemView.findViewById(R.id.lblFilePathe);
            lblFileSize = (TextView) itemView.findViewById(R.id.lblFileSize);
            lnrRoot = (LinearLayout) itemView.findViewById(R.id.lnrSearchRoot);
            imgDownload = (ImageView) itemView.findViewById(R.id.imgDownload);
            chkSelectFile = (CheckBox) itemView.findViewById(R.id.chkSelectFile);
        }

        public void onBind(SearchResultFile f, int position){
            if (f != null) {
                lblFileName.setText(f.name);
                lblFilePath.setText(f.path);
                lblFileSize.setText(FileUtil.humanReadableByteCount(f.fileSize));
                lnrRoot.setTag(f);
//                imgDownload.setVisibility(f.isLocateInFTP ? View.VISIBLE : View.GONE);
                chkSelectFile.setVisibility(f.isLocateInFTP ? View.VISIBLE : View.GONE);
                chkSelectFile.setTag(R.id.lnrSearchRoot, f);
                chkSelectFile.setTag(R.id.imgDownload, position);
                chkSelectFile.setChecked(f.isAddedToDownload);
                chkSelectFile.setEnabled(f.enabled);
                chkSelectFile.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        SearchResultFile resultFile = (SearchResultFile) compoundButton.getTag(R.id.lnrSearchRoot);
                        int position = (int) compoundButton.getTag(R.id.imgDownload);
                        if (isChecked){
                            if (!filesToDownloadFromFTP.contains(resultFile)){
                                filesToDownloadFromFTP.add(resultFile);
                                resultFile.isAddedToDownload = true;
                            }
                        }else{
                            if (filesToDownloadFromFTP.contains(resultFile)){
                                filesToDownloadFromFTP.remove(resultFile);
                                resultFile.isAddedToDownload = false;
                                chkSelectAll.setChecked(false);
                            }
                        }
                        listFiles.set(position, resultFile);
                    }
                });
                lnrRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SearchResultFile selectedFile = (SearchResultFile) view.getTag();
                        if (selectedFile != null){
                            if (selectedFile.isLocateInFTP){
//                                String filePath = Environment.getExternalStorageDirectory() + "/"+selectedFile.name;
//                                new DownloadFileTask(filePath, selectedFile.name).execute();
                            }else{
                                ((EditText)findViewById(R.id.txtSearch)).setText(selectedFile.name);
                                if (dialogSearchResult != null && dialogSearchResult.isShowing()) {
                                    dialogSearchResult.dismiss();
                                }
                                Intent intent=new Intent();
                                intent.putExtra(PATH, selectedFile.path);
                                setResult(Activity.RESULT_OK,intent);
                                finish();
                            }
                        }
                    }
                });

            }
        }
    }

    @Override
    public void downloadFileCompleted(String filePath) {
        fileDownloadedCount++;
        if (lDownloadProgressDialog != null) {
            lDownloadProgressDialog.setProgress(fileDownloadedCount);
        }
    }

    class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultViewHolder>{

        private ArrayList<SearchResultFile> files;
        private LayoutInflater inflater;

        public SearchResultsAdapter(ArrayList<SearchResultFile> files) {
            this.files = files;
            inflater = LayoutInflater.from(FilePickerActivity.this);
        }

        private void refreshItem(ArrayList<SearchResultFile> files){
            this.files = files;
            notifyDataSetChanged();
        }

        @Override
        public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.view_holder_search_results, parent, false);
            return new SearchResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SearchResultViewHolder holder, int position) {
            holder.onBind(files.get(position), position);
        }

        @Override
        public int getItemCount() {
            return files != null ? files.size() : 0;
        }
    }

    public String[] getExternalStorageDirectories() {

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = getExternalFilesDirs(null);
            if (externalDirs != null) {

                for (File file : externalDirs) {
                    if (file == null || file.getPath() == null) continue;

                    String path = file.getPath().split("/Android")[0];

                    boolean addPath = false;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        addPath = Environment.isExternalStorageRemovable(file);
                    } else {
                        addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                    }

                    if (addPath) {
                        results.add(path);
                    }
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d("log tag", results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    Log.d("log tag", results.get(i)+" might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }

    int fileDownloadedCount = 0;
    ProgressDialog lDownloadProgressDialog;

    class DownloadFileTask extends AsyncTask<Void, Integer, Void>{

        private String rootFolderPath;
        private FTPRepository downloadRepository;

        public DownloadFileTask() {
            lDownloadProgressDialog = new ProgressDialog(FilePickerActivity.this);
            lDownloadProgressDialog.setTitle("Please Wait");
            lDownloadProgressDialog.setMessage("Downloading file from FTP....");
            lDownloadProgressDialog.setCancelable(false);
            lDownloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            lDownloadProgressDialog.setProgress(fileDownloadedCount);
            lDownloadProgressDialog.setMax(filesToDownloadFromFTP.size());
            lDownloadProgressDialog.show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fileDownloadedCount = 0;
            downloadRepository = new FTPRepository(filesToDownloadFromFTP);
            downloadRepository.setSearchResultsLis(FilePickerActivity.this);

            rootFolderPath = Environment.getExternalStorageDirectory() + "/tls/csv";
            File f = new File(rootFolderPath);
            if (!f.exists()){
                f.mkdirs();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        //
        private void showFilesThatFailedToDownload(final ArrayList<SearchResultFile> files){
            filesToDownloadFromFTP = new ArrayList<>();
            dialogSearchResult = new Dialog(FilePickerActivity.this);
            dialogSearchResult.setContentView(R.layout.dialog_list_search_results);
            RecyclerView recyclerView = (RecyclerView) dialogSearchResult.findViewById(R.id.recyclerView);
            recyclerView.setHasFixedSize(true);

            chkSelectAll = (CheckBox) dialogSearchResult.findViewById(R.id.chkSelectAll);

            if (files != null && files.size() > 0){
                if (files.get(0).isLocateInFTP){
                    dialogSearchResult.findViewById(R.id.btnDownload).setVisibility(View.VISIBLE);
                    chkSelectAll.setVisibility(View.GONE);
                }else {
                    dialogSearchResult.findViewById(R.id.btnDownload).setVisibility(View.GONE);
                    chkSelectAll.setVisibility(View.GONE);
                }
            }else{
                dialogSearchResult.findViewById(R.id.btnDownload).setVisibility(View.GONE);
                chkSelectAll.setVisibility(View.GONE);
            }

            dialogSearchResult.findViewById(R.id.btnDownload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogSearchResult.dismiss();
                    if (files != null) {
                        filesToDownloadFromFTP.addAll(files);
                        if (filesToDownloadFromFTP.size() > 0) {
                            new DownloadFileTask().execute();
                        } else {
                            Toast.makeText(FilePickerActivity.this, "Select the files to be download.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(FilePickerActivity.this));
            if (adapter != null) {
                listFiles.clear();
                listFiles.addAll(files);
                adapter.refreshItem(listFiles);
            }else{
                adapter = new SearchResultsAdapter(files);
            }

            recyclerView.setAdapter(adapter);
            dialogSearchResult.setCancelable(true);
            dialogSearchResult.setTitle("Please download again");
            dialogSearchResult.show();
        }
        //

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                for (int i=0; i<filesToDownloadFromFTP.size(); i++){
                    downloadRepository.downloadAndSaveFile(ftpSettings, rootFolderPath, filesToDownloadFromFTP.get(i));
                }
            } catch (IOException e) {
                e.printStackTrace();
                isDownloadFiled = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (lDownloadProgressDialog != null && lDownloadProgressDialog.isShowing()) {
                lDownloadProgressDialog.dismiss();
            }

            if (isDownloadFiled) {
                ArrayList<SearchResultFile> failedToDownload = new ArrayList<>();
                for (SearchResultFile notDownloaded : filesToDownloadFromFTP) {
                    if (!isFilePresent(notDownloaded)) {
                        notDownloaded.isAddedToDownload = true;
                        failedToDownload.add(notDownloaded);
                    } else {
                        notDownloaded.isAddedToDownload = false;
                    }
                }

                if (failedToDownload.size() > 0) {
                    Toast.makeText(FilePickerActivity.this, "Some files filed to download", Toast.LENGTH_LONG).show();
                    showFilesThatFailedToDownload(failedToDownload);
                }
            }
        }
    }

}
