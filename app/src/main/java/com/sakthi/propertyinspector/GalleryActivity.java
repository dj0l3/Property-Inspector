package com.sakthi.propertyinspector;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.sakthi.propertyinspector.data.PhotoData;
import com.sakthi.propertyinspector.data.PropertyInfo;
import com.sakthi.propertyinspector.data.RoomItem;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class GalleryActivity extends AppCompatActivity {

    private static LruCache<String, Bitmap> mMemoryCache;
    private RecyclerView mGalleyView;
    private GalleryAdapter mGalleryAdapter;


    private int mRoomId;
    private int mInvId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Items Gallery");


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int alottedMemory=maxMemory/6;

       // final int memClass = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
       // final int cacheSize = 1024 * 1024 * memClass / 8;
        if(mMemoryCache==null)mMemoryCache = new LruCache<String, Bitmap>(alottedMemory) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in bytes rather than number of items.
                return bitmap.getByteCount()/1024;
            }
        };

        mGalleyView=(RecyclerView)findViewById(R.id.galleryView);
        mGalleryAdapter=new GalleryAdapter(this);
        GridLayoutManager gridLayout=new GridLayoutManager(this,3);
        mGalleyView.addItemDecoration(new GridSpacingItemDecoration(3,6,true));
        DefaultItemAnimator itemAnimator=new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setRemoveDuration(500);
        mGalleyView.setItemAnimator(itemAnimator);

        DisplayMetrics lDispMetrics=new DisplayMetrics();
        Display lDisplay=getWindowManager().getDefaultDisplay();
        lDisplay.getMetrics(lDispMetrics);

        gridSize=(lDispMetrics.widthPixels)/3;
        mGalleyView.setAdapter(mGalleryAdapter);
        mGalleyView.setLayoutManager(gridLayout);

        mRoomId=getIntent().getIntExtra("ROOM_ID",0);
        mInvId=getIntent().getIntExtra("INV_ID",0);

        findViewById(R.id.actionTakePicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent captureIntent=new Intent(GalleryActivity.this,PhotoCaptureActivity.class);
                captureIntent.putExtra("ROOM_ID",mRoomId);
                captureIntent.putExtra("INV_ID",mInvId);
                startActivityForResult(captureIntent,100);
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if (menuItem.getItemId() == android.R.id.home) {
            Intent result=new Intent();
            result.putExtra("ROOM_ID",mRoomId);
            result.putExtra("INV_ID",mInvId);
            setResult(RESULT_OK,result);
            finish();
            return true;
        } else return super.onOptionsItemSelected(menuItem);

    }

    public void onActivityResult(int req,int res,Intent data){

        mRoomId=data.getIntExtra("ROOM_ID",mRoomId);
        mInvId=data.getIntExtra("INV_ID",mInvId);

    }


    public void onBackPressed(){

        Intent result=new Intent();
        result.putExtra("ROOM_ID",mRoomId);
        result.putExtra("INV_ID",mInvId);
        setResult(RESULT_OK,result);
        finish();

    }


    public void onResume(){
        super.onResume();
        PropertyInfo propertyInfo=((PropertyInspector)getApplication()).getPropertyInfo();
        RoomItem item=propertyInfo.getRoomItemByInvId(mInvId); //propertyInfo.getRoomItemById(mRoomId,mItemId);
        setTitle(item.getName()+" Photos");
        ArrayList<PhotoData>photos=item.getPhotosList();
        if(mGalleryAdapter.getItemCount()!=photos.size()) {
            mGalleryAdapter.removeAll();
            for (PhotoData data : photos) {
                //mGalleryAdapter.addImageItem(data);
                new BitmapWorkerTask(data).execute();
            }
            mGalleyView.scrollToPosition(photos.size()-1);
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                //outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView mImageView;

        public ViewHolder(View view){
            super(view);
            mImageView=(ImageView)view;
            mImageView.setClickable(true);
        }

        public void onBind(PhotoData data){
            final Bitmap bm = getBitmapFromMemCache(data.getImagePath());

            if (bm == null){
               // BitmapWorkerTask task = new BitmapWorkerTask(mImageView,0);
              //  task.execute(data.getImagePath(),"0");
            }else mImageView.setImageBitmap(bm);
        }

    }


    public class GalleryAdapter extends RecyclerView.Adapter<ViewHolder>{

        private ArrayList<PhotoData> mImageList;
        private Context mContext;

        public GalleryAdapter(Context contxt){
            mContext=contxt;
            mImageList=new ArrayList<>();
        }

        public void setImageItems(ArrayList<PhotoData> items){
            int size=mImageList.size();
            mImageList.clear();
            notifyItemRangeRemoved(0,size);
            for(PhotoData photo:items)mImageList.add(photo);
            notifyItemRangeInserted(0,mImageList.size());
        }

        public void addImageItem(PhotoData data){
            mImageList.add(data);
            notifyItemInserted(mImageList.size()-1);
        }

        public void removeAll(){
            int size=mImageList.size();
            mImageList.clear();
            notifyItemRangeRemoved(0,size);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            //LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewHolder holder=new ViewHolder(new ImageView(mContext));
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.onBind(mImageList.get(position));
        }

        private void setFadeAnimation(View view,boolean scrollUp) {


            TranslateAnimation animation=new TranslateAnimation(0,0,scrollUp?-100:100,0);
            //  animation.setDuration(500);
            //view.startAnimation(animation);

            AnimationSet set=new AnimationSet(true);
            set.addAnimation(new AlphaAnimation(0f, 1.0f));
            set.addAnimation(new ScaleAnimation(0.2f, 1f, 0.2f, 1f, gridSize / 2, gridSize / 2));
            // set.addAnimation(animation);

            set.setDuration(600);
            view.startAnimation(set);



        }

        @Override
        public int getItemCount() {
            return mImageList.size();
        }
    }

    int gridSize;

    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {

        Bitmap bm = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inScaled=true;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        bm = BitmapFactory.decodeFile(path, options);
        Bitmap scaledBitmap= ThumbnailUtils.extractThumbnail(bm,gridSize,gridSize,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);



        return scaledBitmap;
    }

    public int calculateInSampleSize(

            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }

        return inSampleSize;
    }


    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        /*private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Integer> typeref;*/
        private PhotoData mData;
        public BitmapWorkerTask(PhotoData data) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
           // imageViewReference = new WeakReference<ImageView>(imageView);
           // typeref=new WeakReference<Integer>(type);
            mData=data;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

                final Bitmap bitmap = decodeSampledBitmapFromUri(mData.getImagePath(),gridSize,gridSize);
                addBitmapToMemoryCache(mData.getImagePath(), bitmap);
                return bitmap;

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap!=null)mGalleryAdapter.addImageItem(mData);
           /* if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }*/
        }
    }


    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if(key==null||bitmap==null)return;
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
    public Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }



}
