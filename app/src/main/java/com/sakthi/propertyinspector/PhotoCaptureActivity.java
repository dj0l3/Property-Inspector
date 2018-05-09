
package com.sakthi.propertyinspector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;
import com.sakthi.propertyinspector.data.PhotoData;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.RoomItem;
import com.sakthi.propertyinspector.util.FilePickerActivity;
import com.sakthi.propertyinspector.util.FileUtil;
import com.sakthi.propertyinspector.views.RoomItemFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;


public class PhotoCaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

	private Camera camera;
	private SurfaceView surfaceView;
	private PictureCallback rawCallback;
	private ShutterCallback shutterCallback;
	private PictureCallback jpegCallback;
	private int cameraid=0;
	private GestureDetector mGestureDetector;
			/** Called when the activity is first created. */

	public int mRoomId;
	//public int mItemId;
	private int mInvId;

	private CheckBox mFlashControl;
	private PropertyInfo mPropertyInfo;

	private ViewPager mItemPager;
	private ArrayList<RoomItem> mRoomItems;
	private RoomItem mRoomItem;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		setContentView(R.layout.activity_capture_prelollipop);
		findBackFacingCamera();


		mPropertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();

		surfaceView = (SurfaceView)findViewById(R.id.surface);
		surfaceView.getHolder().addCallback(this);
		surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		rawCallback = new PictureCallback() {
			public void onPictureTaken(byte[] data, Camera camera) {
				Log.d("Log", "onPictureTaken - raw");
			}
		};


		mItemPager =(ViewPager)findViewById(R.id.itemTitlePager);

		//Handles data for jpeg picture
		shutterCallback = new ShutterCallback() {
			public void onShutter() {
				Log.i("Log", "onShutter'd");
			}
		};
		jpegCallback = new PictureCallback() {
			public void onPictureTaken(final byte[] data, Camera acamera) {

				new AsyncTaskWP(PhotoCaptureActivity.this,"Saving Image..."){

					@Override
					protected Object doInBackground(Object... params) {
						FileOutputStream outStream = null;
						try {
							String preFix=mPropertyInfo.getClientId()+"_"+mPropertyInfo.getPropertyId()+"_";
							long time= System.currentTimeMillis();
							String imageName=preFix+mRoomId+"_"+mRoomItem.getInventoryId()+"_"+mRoomItem.getItemId()+"_"+time+"_"+mRoomItem.getNumberOfPhotos()+".jpg";
							String imgFolder=((PropertyInspector)getApplication()).getPreference().getWorkDirPath()+"images/";
							String filePath=imgFolder+imageName;
							PhotoData photo=new PhotoData(time,imageName,filePath);

							FileUtil.createFile(filePath);
							File file=new File(filePath);
							outStream = new FileOutputStream(file.getPath());
							outStream.write(data);
							outStream.close();
							RoomItem roomItem=mPropertyInfo.getRoomItemByInvId(mInvId); //.getRoomItemById(mRoomId,mItemId);
							roomItem.addItemPhoto(photo);

							Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
						}
						return null;
					}

					public void onPostExecute(Object data){
						super.onPostExecute(data);
						updatePhotosTaken();
						Toast.makeText(PhotoCaptureActivity.this,"Photo Captured",Toast.LENGTH_SHORT).show();
						camera.startPreview();
					}

				}.execute();


			}
		};
		mGestureDetector=new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
			public boolean onSingleTapConfirmed(MotionEvent e){
				if(camera!=null)
					captureImage();
				return true;
			}
		});

		mRoomId=getIntent().getIntExtra("ROOM_ID",0);
		//mItemId=getIntent().getIntExtra("ITEM_ID",0);
		mInvId=getIntent().getIntExtra("INV_ID",0);

		mRoomItems=mPropertyInfo.getAreaItems(mRoomId);

		mFlashControl=(CheckBox)findViewById(R.id.checkboxFlash);
		mFlashControl.setChecked(false);
		mFlashControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				turnOnFlash(isChecked);
			}
		});


		mItemPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {

				mRoomItem=mRoomItems.get(position);
				mRoomId=mRoomItem.getRoomId();
				//mItemId=mRoomItem.getItemId();
				mInvId=mRoomItem.getInventoryId();

			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});


		ItemScrollAdapter adapter=new ItemScrollAdapter(getSupportFragmentManager(),mRoomItems);
		mItemPager.setAdapter(adapter);


		int pageIndx=0;
		int size=mRoomItems.size();
		for(int i=0;i<size;i++){
			RoomItem roomItem=mRoomItems.get(i);
			//if(roomItem.getRoomId()==mRoomId&&roomItem.getItemId()==mItemId){
			if(roomItem.getInventoryId()==mInvId){
				mRoomItem=roomItem;
				pageIndx=i;
				break;
			}
		}

		mItemPager.setCurrentItem(pageIndx);


	}

	public void updatePhotosTaken(){
		List<Fragment> fragments=(getSupportFragmentManager().getFragments());
		for(Fragment frags:fragments){
			RoomItemView frag=(RoomItemView)frags;

			if(frag!=null&&frag.getInventoryId()==mInvId){

				frag.updateData(mRoomItem.getName(),mRoomItem.getNumberOfPhotos(),getNumOfPhotos(mRoomItem));
			 }
		}
	}

	private int getNumOfPhotos(RoomItem item){
		RoomItem reportItem=RoomItemFragment.getChangedItems().get(item.getInventoryId());
		if(reportItem!=null)return mPropertyInfo.getNumberPhotosToBeTaken(reportItem);
		else return mPropertyInfo.getPropItemById(item.getItemId()).getMaxNumberOfPhotos();
	}


	public static class RoomItemView extends Fragment{

		public static Fragment newInstance(int invId,String roomItemName){
			Fragment fragment=new RoomItemView();
			Bundle args=new Bundle();
			args.putInt("InvId",invId);
			args.putString("ItemName",roomItemName);
			fragment.setArguments(args);
			return fragment;
		}


		public int getInventoryId(){
			return fInvId;
		}

		public void updateData(String name,int numOfPhotos,int maxNumOfPhotos){
			if(mNameView!=null)mNameView.setText(name+" ( "+numOfPhotos+" / "+maxNumOfPhotos+" ) ");
		}

		private int fInvId;
		private TextView mNameView;
		public View onCreateView(LayoutInflater inflater, ViewGroup group,Bundle savedState){
			return inflater.inflate(R.layout.photo_item_fragment,null);
		}

		public void onViewCreated(View view,Bundle savedState){
			super.onViewCreated(view,savedState);
			Bundle args=getArguments();
			fInvId=args.getInt("InvId");
			mNameView=(TextView)view.findViewById(R.id.roomItemName);
			mNameView.setText(args.getString("ItemName"));

		}

		public void onResume(){
			super.onResume();
		}


	}

	private class ItemScrollAdapter extends FragmentStatePagerAdapter{

		private ArrayList<RoomItem> roomItemsList;

		public ItemScrollAdapter(FragmentManager fm,ArrayList<RoomItem> itemsList) {
			super(fm);
			roomItemsList=itemsList;
		}

		//mPropertyInfo.getPropItemById(roomItem.getItemId()).getMaxNumberOfPhotos()

		@Override
		public Fragment getItem(int position) {
			RoomItem roomItem=roomItemsList.get(position);
			return RoomItemView.newInstance(roomItem.getInventoryId(),roomItem.getName()+ " ( "+roomItem.getNumberOfPhotos()+" / "+getNumOfPhotos(roomItem)+" ) ");
		}

		@Override
		public int getCount() {
			return roomItemsList.size();
		}
	}


	private void turnOnFlash(boolean on){
		if(camera==null)return;

		mFlashControl.setText("FLASH "+(on?"ON":"OFF"));
		Camera.Parameters params=camera.getParameters();
		params.setFlashMode(on?Camera.Parameters.FLASH_MODE_ON: Camera.Parameters.FLASH_MODE_OFF);
		camera.setParameters(params);
	}

	private void captureImage() {
		// TODO Auto-generated method stub
		try {
			camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}catch(RuntimeException e)
		{
			start_camera();
			camera.takePicture(shutterCallback, rawCallback, jpegCallback);
		}
	}

	private void start_camera() {
		try {
			if (Build.VERSION.SDK_INT >= 23) {
				if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
					if (camera == null) {
						camera = Camera.open(cameraid);
					}
				} else {
					Toast.makeText(PhotoCaptureActivity.this, "Please grant permission in settings to start the camera", Toast.LENGTH_LONG)
							.show();
					finish();
				}

			} else {
				if (camera == null) {
					camera = Camera.open(cameraid);
				}
			}
		} catch (Exception e) {
			Toast.makeText(PhotoCaptureActivity.this, "Could not start the camera. " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}

	public static void setCameraDisplayOrientation(Context context,
												   int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = ((Activity) context).getWindowManager()
				.getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
		Camera.Parameters params = camera.getParameters();
		params.setRotation(result);
		camera.setParameters(params);
	}

	private void stop_camera() {
		camera.stopPreview();
		camera.release();
		camera=null;
	}

	private boolean mIsSurfaceCreated;
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mIsSurfaceCreated=true;

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder){
		mIsSurfaceCreated=false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		if (holder.getSurface() == null) {
			return;
		}
		try {
			camera.stopPreview();
		} catch (Exception e) {
			Log.e("CamTestActivity", "Tried to shut down non existant preview",
					e);
		}
		// Set orientation and display size here:
		Camera.Parameters params = camera.getParameters();
	////	Camera.Size previewSize = getBestPreviewSize(params);
		Camera.Size desiredPictureSize = getDesiredPictureSize(params);
		/*if (holder != null && hasMacroFocus(params))
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);// only added in
			// API 14...
		else if (holder != null && hasAutoFocus(params)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		} else {
			Log.d("CameraPreview",
					"The holder has not been created yet or there is no autofocus");
		}*/

	//	params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
		setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK,
				camera);
		//params.setPreviewSize(previewSize.width, previewSize.height);
		params.setPictureSize(desiredPictureSize.width, desiredPictureSize.height);
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		// -
		params.set("orientation", "portrait");
		params.set("rotation", 90);

		camera.setParameters(params);
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.e("CameraPreview",
					"Camera preview displayed could not be changed to mHolder",
					e);
		}
		camera.startPreview();

	}


	@Override
	protected void onResume() {
		super.onResume();
		if(!mIsSurfaceCreated){
			start_camera();
		}

		turnOnFlash(mFlashControl.isChecked());
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(camera!=null)stop_camera();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if(mGestureDetector!=null)return mGestureDetector.onTouchEvent(event);

		return super.onTouchEvent(event);
	}
	private void findFrontFacingCamera() {

		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				cameraid = i;
				break;
			}
		}
	}
	private void findBackFacingCamera() {

		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraid = i;
				break;
			}
		}
	}

	private Camera.Size getDesiredPictureSize(Camera.Parameters params) {
		// Resolution is widthxheight
		Camera.Size result = null;
		// boolean isDesiredValue=false;
		final int minArea = 500 * 500;
		final int maxArea = 1000 * 1000;
		String storedSize=((PropertyInspector)getApplication()).getPreference().getPhotoQuality();
		List<Camera.Size> supportedSizeList = params.getSupportedPictureSizes();
		if(storedSize==null)return supportedSizeList.get(0);
		for (Camera.Size size : supportedSizeList) {

			String lsize= size.width+" x "+size.height;
			if(lsize.equals(storedSize))return size;
			/*if (size.width * size.height > minArea
					&& size.width * size.height < maxArea) {
				if (result == null)
					result = size;
				else {
					int resultArea = result.width * result.height;
					int sizeArea = size.width * size.height;
					if (resultArea < sizeArea) {
						result = size;
					}
				}
			}*/
		}
		return result;
	}

	private Camera.Size getBestPreviewSize(Camera.Parameters params) {
		Camera.Size result = null;
		ArrayList<Camera.Size> mSupportedSizes = (ArrayList<Camera.Size>) params
				.getSupportedPreviewSizes();
		for (Camera.Size size : mSupportedSizes) {

			if (result == null)
				result = size;
			else {
				int resultArea = result.width * result.height;
				int sizeArea = size.width * size.height;
				if (resultArea < sizeArea)
					result = size;
			}

		}
		Log.d("CameraPreview", "Camera Preview size set to " + result.width
				+ "X" + result.height);
		return result;
	}

	public boolean hasMacroFocus(Camera.Parameters params) {
		List<String> supportedFocusModes = params.getSupportedFocusModes();
		if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO))
			return true;
		return false;
	}

	public boolean hasAutoFocus(Camera.Parameters params) {
		List<String> focusModes = params.getSupportedFocusModes();

		if (focusModes != null
				&& focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
			return true;
		} else if (focusModes == null) {
			Log.d("CameraPreview", "Error getting autofocus mode list");
		}
		return false;
	}

	public void onBackPressed(){

		Intent result=new Intent();
		result.putExtra("ROOM_ID",mRoomId);
		result.putExtra("INV_ID",mInvId);
		setResult(RESULT_OK,result);
		finish();

	}

}