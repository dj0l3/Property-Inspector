package com.sakthi.propertyinspector.repository;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.sakthi.propertyinspector.data.FTPSettings;
import com.sakthi.propertyinspector.data.SearchResultFile;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class FTPRepository {

    private static final String TAG = "FTPRepository";
    private FTPConnectionListener listener;

    public FTPRepository() {
    }

    private ArrayList<SearchResultFile> filesToBeDownload;
    private int downloadPosition = 0;

    FTPClient downloadFTPConnection = null;

    public FTPRepository(ArrayList<SearchResultFile> filesToBeDownload) {
        this.filesToBeDownload = filesToBeDownload;
    }

    public void setFTPConnectionListener(FTPConnectionListener listener) {
        this.listener = listener;
    }

    public FTPClient getConnection(FTPSettings ftpSettings) {
        FTPClient ftpClient = new FTPClient();
        if (ftpSettings != null) {
            try {
                if (ftpClient == null) {
                    ftpClient = new FTPClient();
                }
                if (!ftpClient.isConnected()) {
                    ftpClient.connect(ftpSettings.host, 21);
                    if (ftpClient.login(ftpSettings.userName, ftpSettings.password)) {
                        if (listener != null) {
                            listener.onConnectionSuccess();
                        }

                    } else {
                        if (listener != null) {
                            listener.onConnectionFailed();
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "getConnection: " + e.getLocalizedMessage());
                if (listener != null) {
                    listener.onConnectionFailed();
                }
            }
        }else{
            if (listener != null) {
                listener.onConnectionFailed();
            }
        }
        return ftpClient;
    }


    public void createFolder(String path, String name) {
        String fullPath = Environment.getExternalStorageDirectory().toString() + path + "/" + name;

        File folder = new File(fullPath);

        if (!folder.exists()) {
            folder.mkdir();
        }
    }


    public void uploadFile(FTPSettings ftpSettings, File file, int pid) {

        try {
            if (fileUploadFTP == null) {
                fileUploadFTP = this.getConnection(ftpSettings);
                fileUploadFTP.setFileType(FTP.BINARY_FILE_TYPE);
                fileUploadFTP.enterLocalPassiveMode();
            }

            boolean isTlsExists = fileUploadFTP.changeWorkingDirectory("/inventory");
            if (!isTlsExists) {
                fileUploadFTP.makeDirectory("inventory");
            }
            boolean isPIDExists = fileUploadFTP.changeWorkingDirectory("/inventory/" + pid);
            if (!isPIDExists) {
                fileUploadFTP.changeWorkingDirectory("/inventory");
                fileUploadFTP.makeDirectory(String.valueOf(pid));
            }

            boolean isImageExists = fileUploadFTP.changeWorkingDirectory("/inventory/" + pid + "/images");
            if (!isImageExists) {
                fileUploadFTP.changeWorkingDirectory("/inventory/" + pid);
                fileUploadFTP.makeDirectory("images");
            }
            fileUploadFTP.changeWorkingDirectory("/inventory/" + pid + "/images");
            FileInputStream fis = new FileInputStream(file);
            fileUploadFTP.setBufferSize(1024 * 1024);
            fileUploadFTP.setConnectTimeout(20000);
            boolean success = fileUploadFTP.storeFile(file.getName(), fis);
            if (success){
                if (uploadListener != null) {
                    uploadListener.uploadFileCompleted();
                }
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    FTPClient fileUploadFTP = null;

    public void uploadCSVFile(FTPSettings ftpSettings, File file, int pid) {

        try {
            if (fileUploadFTP == null) {
                fileUploadFTP = this.getConnection(ftpSettings);
                fileUploadFTP.setFileType(FTP.BINARY_FILE_TYPE);
                fileUploadFTP.enterLocalPassiveMode();
            }

            boolean isTlsExists = fileUploadFTP.changeWorkingDirectory("/inventory");
            if (!isTlsExists) {
                fileUploadFTP.makeDirectory("inventory");
            }
            boolean isPIDExists = fileUploadFTP.changeWorkingDirectory("/inventory/" + pid);
            if (!isPIDExists) {
                fileUploadFTP.changeWorkingDirectory("/inventory");
                fileUploadFTP.makeDirectory(String.valueOf(pid));
            }
            FileInputStream fis = new FileInputStream(file);
            fileUploadFTP.setBufferSize(1024 * 1024);
            fileUploadFTP.setConnectTimeout(20000);
            boolean success = fileUploadFTP.storeFile(file.getName(), fis);
            if (success){
                if (uploadListener != null) {
                    uploadListener.uploadFileCompleted();
                }
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Boolean downloadAndSaveFile(FTPSettings ftpSettings, String filename, String filePath, File localFile)
            throws IOException {
        FTPClient ftp = null;

        try {
            ftp = this.getConnection(ftpSettings);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            ftp.changeWorkingDirectory(filePath);

            OutputStream outputStream = null;
            boolean success = false;
            InputStream inputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(
                        localFile));
               // success = ftp.retrieveFile(filename, outputStream);
                inputStream = ftp.retrieveFileStream(filename);
                byte[] bytesArray = new byte[4096];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                    outputStream.write(bytesArray, 0, bytesRead);
                }

                success = ftp.completePendingCommand();
                if (success) {
                    System.out.println("File #2 has been downloaded successfully.");
                    searchListener.downloadFileCompleted(null);
                }
                outputStream.close();

            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            return success;
        } finally {
            if (ftp != null) {
                ftp.logout();
                ftp.disconnect();
            }
        }
    }

    public Boolean downloadAndSaveFile(FTPSettings ftpSettings, String rootFodlerPath, SearchResultFile resultFile)
            throws IOException {

//        SearchResultFile resultFile = filesToBeDownload.get(downloadPosition);

        try {
            if (downloadFTPConnection == null) {
                downloadFTPConnection = this.getConnection(ftpSettings);
                downloadFTPConnection.setFileType(FTP.BINARY_FILE_TYPE);
                downloadFTPConnection.enterLocalPassiveMode();
            }

            downloadFTPConnection.changeWorkingDirectory(resultFile.folderPath);

            OutputStream outputStream = null;
            boolean success = false;
            InputStream inputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(
                        new File(rootFodlerPath+"/"+resultFile.name)));
                // success = ftp.retrieveFile(filename, outputStream);
                try {
                    inputStream = downloadFTPConnection.retrieveFileStream(resultFile.name);
                    byte[] bytesArray = new byte[4096];
                    int bytesRead = -1;
                    while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                        outputStream.write(bytesArray, 0, bytesRead);
                    }
                    success = downloadFTPConnection.completePendingCommand();
                }catch (Exception e){
                    success = true;
                }
                if (success) {
                    System.out.println("File #2 has been downloaded successfully.");
                    searchListener.downloadFileCompleted(null);
//                    if (downloadPosition < filesToBeDownload.size()-1){
                        downloadPosition++;
//                        downloadAndSaveFile(ftpSettings, rootFodlerPath);
//                    }
                }
                outputStream.close();

            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            return success;
        } finally {
            if (downloadFTPConnection != null && downloadPosition == filesToBeDownload.size() -1) {
                downloadFTPConnection.logout();
                downloadFTPConnection.disconnect();
            }
        }
    }

    FTPClient ftpSearch = null;


    public void searchFIleInFTP(FTPSettings ftpSettings, final String filenameToSearch, String currentPath)
            throws IOException {
        ArrayList<SearchResultFile> searchResultsLis = new ArrayList<>();
        try {
            ftpSearch = this.getConnection(ftpSettings);
            ftpSearch.setFileType(FTP.BINARY_FILE_TYPE);
            ftpSearch.enterLocalPassiveMode();

            ftpSearch.changeWorkingDirectory(currentPath);

            FTPFile[] result = ftpSearch.listFiles(currentPath);

            if (result != null && result.length > 0) {
                for (int i=0; i<result.length; i++) {
                    FTPFile aFile = result[i];
                    if (!aFile.getName().equals("..") && !aFile.getName().equals(".")){
                        SearchResultFile resultFile = new SearchResultFile();
                        resultFile.name = aFile.getName();
                        resultFile.lastModified = aFile.getTimestamp().getTime().getTime();
                        resultFile.path = (currentPath.endsWith("/")
                                ? currentPath + aFile.getName()
                                : currentPath + "/" + aFile.getName());

                        resultFile.folderPath = (currentPath.endsWith("/")
                                ? currentPath + aFile.getName()
                                : currentPath);
                        resultFile.isDir = aFile.getType() == FTPFile.DIRECTORY_TYPE;
                        resultFile.isLocateInFTP = true;

                        resultFile.fileSize = aFile.getSize();
                        searchResultsLis.add(resultFile);
                    }

                }
            }
            if (searchListener != null) {
                searchListener.searchCompleted(searchResultsLis);
            }
        } finally {
            closeFTPSearchConnection();
        }
    }

    public void closeFTPSearchConnection() {
        try {
            if (ftpSearch != null) {
                ftpSearch.logout();
                ftpSearch.disconnect();
            }
        }catch (Exception ex){

        }
    }

    public void closeFTPUploadConnection() {
        try {
            if (fileUploadFTP != null) {
                fileUploadFTP.logout();
                fileUploadFTP.disconnect();
            }
        }catch (Exception ex){

        }
    }

    public interface FTPConnectionListener {
        void onConnectionFailed();

        void onConnectionSuccess();
    }

    public interface FTPSearchListener{
        void searchCompleted(ArrayList<SearchResultFile> searchResultFiles);

        void downloadFileCompleted(String filePath);
    }

    public interface FTPUploadListener{
        void uploadFileCompleted();
    }

    private FTPSearchListener searchListener;

    private FTPUploadListener uploadListener;

    public void setSearchResultsLis(FTPSearchListener listener) {
        this.searchListener = listener;
    }

    public void setFTPUploadListener(FTPUploadListener uploadListener){
        this.uploadListener = uploadListener;
    }
}
