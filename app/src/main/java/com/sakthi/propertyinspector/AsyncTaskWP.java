package com.sakthi.propertyinspector;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Objects;

/**
 * Created by sakthivel on 6/2/2016.
 */
public abstract class AsyncTaskWP extends AsyncTask<Object,Object,Object> {

    private ProgressDialog mDialog;

    public AsyncTaskWP(Context context,String message){

        mDialog=new ProgressDialog(context);
        mDialog.setMessage(message);
        mDialog.setCancelable(false);
    }

    public void onPreExecute(){
        mDialog.show();
    }



    public void onPostExecute(Object result){
        mDialog.cancel();
    }

}
