package com.sakthi.propertyinspector.util;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class AndroidUtilities {

	public static float density = 1;
	public static Point displaySize = new Point();
/*
	static {
		density = ApplicationLoader.applicationContext.getResources()
				.getDisplayMetrics().density;
		checkDisplaySize();
	}*/

	public static int dp(float value) {
		return (int) Math.ceil(density * value);
	}

	public static float dpf2(float value) {
		return density * value;
	}

	public static void checkDisplaySize(Context contxt) {
		try {
			WindowManager manager = (WindowManager) contxt.getSystemService(Context.WINDOW_SERVICE);
			if (manager != null) {
				Display display = manager.getDefaultDisplay();
				if (display != null) {
					if (android.os.Build.VERSION.SDK_INT < 13) {
						displaySize
								.set(display.getWidth(), display.getHeight());
					} else {
						display.getSize(displaySize);
					}
					Log.e("tmessages", "display size = " + displaySize.x+ " " + displaySize.y);
				}
			}
		} catch (Exception e) {
			Log.e("tmessages", e.toString());
		}
	}


	public static RecyclerView.ItemAnimator getItemAnimator(){
		DefaultItemAnimator itemAnimator=new DefaultItemAnimator();
		itemAnimator.setAddDuration(250);
		itemAnimator.setRemoveDuration(250);
		return  itemAnimator;
	}



}
