package com.sakthi.propertyinspector;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.sakthi.propertyinspector.data.Area;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.util.AndroidUtilities;
import com.sakthi.propertyinspector.util.AppPreference;
import com.sakthi.propertyinspector.util.FileUtil;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import io.fabric.sdk.android.Fabric;

/**
 * Created by sakthivel on 5/17/2016.
 */
public class PropertyInspector extends Application{

    private PropertyInfo mPropertyInfo;
    private AppPreference mPreference;

    public void setPropertyInfo(PropertyInfo info){
        mPropertyInfo=info;
    }
    public PropertyInfo getPropertyInfo(){
        return mPropertyInfo;
    }

    public AppPreference getPreference(){
        return mPreference;
    }

    final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

    public void onCreate(){
        super.onCreate();
        Fabric.with(this, new Crashlytics());
       mPreference=new AppPreference(getApplicationContext());
        String appDirPath=mPreference.getAppDirPath();

        if(!FileUtil.isDirFound(appDirPath))FileUtil.createDirectory(appDirPath);


        AndroidUtilities.density = getResources().getDisplayMetrics().density;
        AndroidUtilities.checkDisplaySize(this);

//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread thread, Throwable ex) {
//                Log.e("Error Message",ex.getStackTrace()[0].toString());
//                //FileUtil.saveCrashReport(getApplicationContext());
//                StringWriter stackTrace = new StringWriter();
//                ex.printStackTrace(new PrintWriter(stackTrace));
//                StringBuilder errorReport = new StringBuilder();
//                errorReport.append("************ CAUSE OF ERROR ************\n\n");
//                errorReport.append(stackTrace.toString());
//                FileUtil.saveCrashReport(getApplicationContext(),errorReport.toString());
//
//                System.exit(1);
//            }
//        });

    }


}
