package com.sakthi.propertyinspector;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by sakthivel on 6/2/2016.
 */
public class Dialogs {

    public static void showInfoDialog(Context contxt,String title,String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(contxt);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                dialog.dismiss();
            }
        });
       /* builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog
            }
        });*/
        AlertDialog dialog = builder.create();
        dialog.show();


    }


    public static void showYesOrNoDialog(Context contxt,String title,String message,DialogInterface.OnClickListener clickListener){

        AlertDialog.Builder builder = new AlertDialog.Builder(contxt);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Yes",clickListener);
        builder.setNegativeButton("No", clickListener);

        AlertDialog dialog = builder.create();
        dialog.show();


    }
}
