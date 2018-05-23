package com.jack.imagepickorcapture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.jack.imagepickorcapture.adapter.PhotoUploadGalleryAdapter;
import com.jack.imagepickorcapture.adapter.PhotoUploadViewPagerAdapter;
import com.jack.imagepickorcapture.helper.MarshmellowUtility;
import com.jack.imagepickorcapture.helper.PhotoUploadingTaskNew;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;


public class PhotoUpload extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;

    // directory name to store captured image
    private static final String IMAGE_DIRECTORY_NAME = "Allevents";
    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private final String TAG = "Photo upload activity";
    private String event_id, eventname;
    private int selected_item = 0;
    private Uri fileUri; // file url to store image/video

    private PhotoUploadGalleryAdapter mGalleryAdapter;
    private ArrayList<Image> mSelectedImages;
    private GridView gridView;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int id = 12424;
    private ViewPager viewPager;
    private PhotoUploadViewPagerAdapter pagerAdapterCaption;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_upload_view);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        SetupToolBar();
        eventname = "Meet and Greet Shirley Setia";
        event_id = "176229133007306";

        viewPager = (ViewPager) findViewById(R.id.photo_upload_pager);
        gridView = (GridView) findViewById(R.id.grid);

        viewPager.setPageMargin(10);
        mSelectedImages = new ArrayList<>();
        pagerAdapterCaption = new PhotoUploadViewPagerAdapter(PhotoUpload.this, mSelectedImages);
        mGalleryAdapter = new PhotoUploadGalleryAdapter(getApplicationContext(), mSelectedImages);
        gridView.setAdapter(mGalleryAdapter);
        viewPager.setAdapter(pagerAdapterCaption);

        boolean isCapture = getIntent().getBooleanExtra("isCapture", false);
        if (isCapture) {
            captureImage();
        } else {
            OpenImageSelector(8, Constants.REQUEST_CODE);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected_item = position;
                viewPager.setCurrentItem(position);
                hideSoftKeyboard();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                hideSoftKeyboard();
            }

            @Override
            public void onPageSelected(int position) {
                selected_item = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @SuppressLint("RestrictedApi")
    private void SetupToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);

        if (mToolbar != null) {
            mToolbar.setTitle("Photo upload");
            mToolbar.setNavigationIcon(R.drawable.ic_launcher_background);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }
    }

    public PendingIntent getPendingAction() {
        Intent yesReceive = new Intent();
        yesReceive.setAction("YES");
        return PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void CallUploadListner() {
        new PhotoUploadingTaskNew(event_id,
                "jatinmandanka@gmail.com", mSelectedImages, new OnPhotoUpload() {

            @Override
            public void onUploadStart(String msg,
                                      String Uri) {
                Random rand = new Random();
                id = rand.nextInt(2000);
                // Displays the progress bar for the first time.
                mBuilder.setProgress(mSelectedImages.size(), 0, false);
                mNotifyManager.notify(id, mBuilder.build());
            }

            @Override
            public void onUploadProgressChange(int percentage) {
                try {
                    mBuilder.setProgress(100, percentage, false);
                    // Displays the progress bar for the first time.
                    mNotifyManager.notify(0, mBuilder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onUploadSelect(int position) {
                //NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_action_content_clear, "Cancel", getPendingAction()).build();
                mBuilder.setProgress(mSelectedImages.size(), position, false)
                        .setContentText("Upload in progress.. (" + position + " of " + (mSelectedImages.size() + ")"));
                //mBuilder.addAction(action);
                mNotifyManager.notify(id, mBuilder.build());
            }

            @Override
            public void onCompleate(boolean iserror, String errormsg) {
                mBuilder.setContentTitle("Photo upload complete").setContentText("Tap for view photos.");
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://allevents.in/e/" + event_id));

                    PendingIntent imtent = PendingIntent.getActivity(getApplicationContext(), 0, i, PendingIntent.FLAG_ONE_SHOT);

                    mBuilder.setProgress(0, 0, false).setContentIntent(imtent).setAutoCancel(true);
                    mNotifyManager.notify(id, mBuilder.build());
                    PhotoUpload.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancleSelect(int position) {
                System.out
                        .println("=====================gallerylistview cancel called  " + position);
            }
        });
    }

   /* private void showRemoveImageDialog(final int position) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        //builder.setTitle("Dialog");
        builder.setMessage(R.string.photo_upload_remove_msg);
        builder.setPositiveButton("remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mSelectedImages.size() != 1) {
                    mSelectedImages.remove(position);
                    mGalleryAdapter.notifyDataSetChanged();
                    pagerAdapterCaption.notifyDataSetChanged();
                } else {
                    PhotoUpload.this.finish();
                }
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                Button bt_pos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                bt_pos.setTransformationMethod(null);
                bt_pos.setTextColor(ContextCompat.getColor(PhotoUpload.this, R.color.colorAERed));

                Button bt_neg = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                bt_neg.setTransformationMethod(null);
            }
        });
        dialog.show();
    }*/

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MarshmellowUtility.MY_PERMISSIONS_REQUEST_PERMISSIONS:
                try {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        captureImage();
                    } /*else {
                        //code for deny
                    }*/
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @SuppressLint({"InlinedApi", "NewApi"})
    private void selectPic() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PhotoUpload.this);

        final String[] items = new String[]{"Take a Photo",
                "Upload From Gallery"};
        builder.setCancelable(true);
        builder.setTitle("Select Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    int result = MarshmellowUtility.checkPermission(PhotoUpload.this, Manifest.permission.CAMERA);
                    if (result == 0 || result == 1 || result == 3) {
                        captureImage();
                    } else if (result == 4) {
                        Toast.makeText(PhotoUpload.this, "App requires phone camera access to take photo. You can modify app permissions from Permissions > Camera > Turn On.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    OpenImageSelector((Constants.DEFAULT_LIMIT - mSelectedImages.size()), Constants.REQUEST_ADD_MORE_CODE);
                }
            }
        });
        builder.create().show();
    }

    private void OpenImageSelector(int i, int resultcode) {
        Intent intent = new Intent(PhotoUpload.this, AlbumSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, i);
        startActivityForResult(intent, resultcode);
    }

    /*
     * Capturing Camera Image will lauch camera app requrest image capture
     */
    private void captureImage() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                File f = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                fileUri = Uri.fromFile(f);
                Uri contentUri = FileProvider.getUriForFile(PhotoUpload.this, BuildConfig.APPLICATION_ID + ".provider", f);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
            } else {
                fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            }
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        } catch (Exception anfe) {
            Toast.makeText(PhotoUpload.this,
                    "Oops!the image can't be captured", Toast.LENGTH_LONG)
                    .show();
            PhotoUpload.this.finish();
        }
    }

    /*
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        if (fileUri != null) outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        if (savedInstanceState != null) fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Receiving activity result method will be called after closing the camera
     */
    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        /*LOGD(TAG, "====== onactivity result request code = " + requestCode
                + "  result code = " + resultCode);*/
        try {
            if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {

                if (resultCode == RESULT_OK) {
                    previewCapturedImage();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(PhotoUpload.this,
                            "You cancelled image upload", Toast.LENGTH_SHORT)
                            .show();
                    PhotoUpload.this.finish();
                } else {
                    Toast.makeText(PhotoUpload.this,
                            "Couldn't Upload image. Try again.", Toast.LENGTH_SHORT)
                            .show();
                }
            } else if (requestCode == Constants.REQUEST_CODE) {
                //for Multiple Image Selected
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    if (images.size() > 0) {
                        mSelectedImages.clear();
                        mSelectedImages.addAll(images);
                        mGalleryAdapter.notifyDataSetChanged();
                        pagerAdapterCaption.notifyDataSetChanged();
                        gridView.setVisibility(View.VISIBLE);
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // user cancelled recording
                    if (mSelectedImages.size() == 0) {
                        Toast.makeText(PhotoUpload.this,
                                "You cancelled image upload", Toast.LENGTH_SHORT)
                                .show();
                    }
                    PhotoUpload.this.finish();
                }
            } else if (requestCode == Constants.REQUEST_ADD_MORE_CODE) {
                //for Multiple Image Selected
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    mSelectedImages.addAll(images);
                    mGalleryAdapter.notifyDataSetChanged();
                    pagerAdapterCaption.notifyDataSetChanged();

                    gridView.setVisibility(View.VISIBLE);
                } else if (resultCode == RESULT_CANCELED) {
                    // user cancelled recording
                    if (mSelectedImages.size() == 0) {
                        Toast.makeText(PhotoUpload.this,
                                "You cancelled image upload", Toast.LENGTH_SHORT)
                                .show();
                    }
                } else {
                    // failed to record video
                    Toast.makeText(getApplicationContext(),
                            "Couldn't Upload image. Try again.", Toast.LENGTH_SHORT)
                            .show();
                }
            } else if (requestCode == Constants.REQUEST_REPLACE_CODE) {
                // for Replace Image selected
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);
                    mSelectedImages.set(selected_item, images.get(0));
                    mGalleryAdapter.notifyDataSetChanged();
                    pagerAdapterCaption.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gridView.setAdapter(null);
    }

    private void launchUploadActivity(boolean isImage, String path) {
        Intent i = new Intent(PhotoUpload.this, UploadActivity.class);
        i.putExtra("filePath", path);
        i.putExtra("isImage", isImage);
        startActivity(i);
    }

    public void onClickUpload(View view) {
        //if (AllEventUtilitys.isNetworkAvailable(PhotoUpload.this)) {
        if (mSelectedImages.size() != 0) {
            launchUploadActivity(true, mSelectedImages.get(0).path);
            this.finish();
            /*NotificationChannel notificationChannel = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                String channelId = "JACK_APP";
                CharSequence channelName = "Jack APP";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, channelName, importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(notificationChannel);
                mBuilder = new NotificationCompat.Builder(PhotoUpload.this, channelId);
            } else {
                mBuilder = new NotificationCompat.Builder(PhotoUpload.this);
            }

            mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder.setContentTitle("Uploading photos").setOngoing(true)
                    .setContentText("Photo upload in progress.. (0 of " + (mSelectedImages.size() + ")"))
                    .setSmallIcon(R.drawable.ic_event_photo_upload);

            //inform to listner start upload pics
            CallUploadListner();
            Toast.makeText(PhotoUpload.this,
                    "Uploading started...", Toast.LENGTH_SHORT)
                    .show();
            PhotoUpload.this.finish();*/
        } else {
            PhotoUpload.this.finish();
        }

        //}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (mSelectedImages.size() > 1) {
                    mSelectedImages.remove(selected_item);
                    mGalleryAdapter.notifyDataSetChanged();
                    pagerAdapterCaption.notifyDataSetChanged();
                } else {
                    PhotoUpload.this.finish();
                }
                break;
            case R.id.action_replace:
                OpenImageSelector(1, Constants.REQUEST_REPLACE_CODE);
                break;
            case R.id.action_add_more:
                if (mSelectedImages.size() >= 8) {
                    Toast.makeText(
                            getApplicationContext(),
                            String.format(getString(R.string.limit_exceeded_m), 8),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    selectPic();
                }
                break;
            case android.R.id.home:
                PhotoUpload.this.finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.menu_photo_upload_actionmd, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*
         * Display image from a path to ImageView
         */
    private void previewCapturedImage() {
        try {
            //add this image into arraylist
            Image img = new Image(56454, " ", fileUri.getPath(), true);
            mSelectedImages.add(img);

            mGalleryAdapter.notifyDataSetChanged();
            pagerAdapterCaption.notifyDataSetChanged();
            gridView.setVisibility(View.VISIBLE);

        } catch (NullPointerException e) {
            Log.e(TAG, "Could not preview captured image");
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            System.gc();
        } catch (Exception e) {
            Log.e(TAG, "Could not preview captured image");
        }
    }

    /*
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        if (getOutputMediaFile(type) != null) {
            return Uri.fromFile(getOutputMediaFile(type));
        } else {
            return null;
        }

    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @SuppressLint("NewApi")
    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                //Log.d(IMAGE_DIRECTORY_NAME, "photo upload Oops! Failed create "   + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

}
