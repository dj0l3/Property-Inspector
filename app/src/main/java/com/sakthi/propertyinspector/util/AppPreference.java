package com.sakthi.propertyinspector.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 * Created by sakthivel on 5/6/2016.
 */
public class AppPreference {

    //private static final String

    private SharedPreferences mSharedPreference;

    private static final String KEY_APP_DIR = "APP_DIR";
    private static final String KEY_EXPORT_NAME = "EXPORT_NAME";
    private static final String KEY_PHOTO_QUALITY = "PHOTO_QUALITY";

    private static final String KEY_REPORTER_NAME = "REPORTER_NAME";
    private static final String KEY_TENANT_NAME = "TENANT_NAME";
    private static final String KEY_WORK_DIR_PATH = "WORKING_DIR_PATH";
    private static final String KEY_WORK_FILE_PATH = "WORKING_FILE_PATH";

    private final String DEFAULT_EXPORT_NAME = "ce02";
    private final String DEFAULT_DIR_PATH;
    private static final String KEY_FTP_SETTINGS = "KEY_FTP_SETTINGS";
    private static final String KEY_INSPECTED_PROPERTY = "KEY_INSPECTED_PROPERTY";
    private final String KEY_WORK_FILE_NAME = "WORKING_FILE_PATH";

    private final String KEY_WORK_FILE_BACKUP_NUMBER = "WORK_FILE_BACKUP_NUMBER";

    private static final String KEY_LATEST_WORK_FILE_PATH = "LATEST_WORKING_FILE_PATH";
    private final String KEY_LATEST_WORK_FILE_NAME = "LATEST_WORKING_FILE_PATH";


    public AppPreference(Context contxt) {
        mSharedPreference = contxt.getSharedPreferences("PropertyInspector", Context.MODE_PRIVATE);
        DEFAULT_DIR_PATH = Environment.getExternalStorageDirectory().getPath() + "/inventory/";
    }


    public String getAppDirPath() {

        return mSharedPreference.getString(KEY_APP_DIR, DEFAULT_DIR_PATH
        );
    }

    public String getExportName() {
        return mSharedPreference.getString(KEY_EXPORT_NAME, DEFAULT_EXPORT_NAME);
    }

    public String getPhotoQuality() {
        return mSharedPreference.getString(KEY_PHOTO_QUALITY, null);
    }


    public void setAppDirPath(String path) {
        mSharedPreference.edit().putString(KEY_APP_DIR, path).commit();
    }

    public void setExportName(String name) {
        mSharedPreference.edit().putString(KEY_EXPORT_NAME, name).commit();
    }

    public void setPhotoQuality(String quality) {
        mSharedPreference.edit().putString(KEY_PHOTO_QUALITY, quality).commit();
    }

    public void setReporterName(String name) {
        mSharedPreference.edit().putString(KEY_REPORTER_NAME, name).commit();
    }

    public void setTenantName(String name) {
        mSharedPreference.edit().putString(KEY_TENANT_NAME, name).commit();
    }

    public String getReporterName() {
        return mSharedPreference.getString(KEY_REPORTER_NAME, "");
    }

    public String getTenantName() {
        return mSharedPreference.getString(KEY_TENANT_NAME, "");
    }


    public void setWorkFilePath(String path) {
        mSharedPreference.edit().putString(KEY_WORK_FILE_PATH, path).commit();
    }

    public void setWorkDirPath(String path) {
        mSharedPreference.edit().putString(KEY_WORK_DIR_PATH, path).commit();
    }

    public String getWorkDirPath() {
        return mSharedPreference.getString(KEY_WORK_DIR_PATH, null);
    }

    public String getWorkFilePath() {
        return mSharedPreference.getString(KEY_WORK_FILE_PATH, null);
    }

    public String getLatestWorkFilePath() {
        return mSharedPreference.getString(KEY_LATEST_WORK_FILE_PATH, null);
    }

    public void setLatestWorkFilePath(String path) {
        mSharedPreference.edit().putString(KEY_LATEST_WORK_FILE_PATH, path).commit();
    }

    public String getLatestWorkFileName() {
        return mSharedPreference.getString(KEY_LATEST_WORK_FILE_NAME, null);
    }

    public void setLatestWorkFileName(String path) {
        mSharedPreference.edit().putString(KEY_LATEST_WORK_FILE_NAME, path).commit();
    }

    public void setWorkFileBackupNumber(int number) {
        mSharedPreference.edit().putInt(KEY_WORK_FILE_BACKUP_NUMBER, number).commit();
    }

    public int getWorkFileBackupNumber() {
        return mSharedPreference.getInt(KEY_WORK_FILE_BACKUP_NUMBER, 0);
    }


    public void setFTPSettings(String settings) {
        mSharedPreference.edit().putString(KEY_FTP_SETTINGS, settings).commit();
    }

    public String getFTPSettings() {
        return mSharedPreference.getString(KEY_FTP_SETTINGS, null);
    }

    public void setInspectedProperty(String settings) {
        mSharedPreference.edit().putString(KEY_INSPECTED_PROPERTY, settings).commit();
    }

    public String getInspectedProperty() {
        return mSharedPreference.getString(KEY_INSPECTED_PROPERTY, null);
    }

    public void setWorkFileName(String name) {
        mSharedPreference.edit().putString(KEY_WORK_FILE_NAME, name).commit();
    }

    public String getWorkFileName() {
        return mSharedPreference.getString(KEY_WORK_FILE_NAME, null);
    }


}
