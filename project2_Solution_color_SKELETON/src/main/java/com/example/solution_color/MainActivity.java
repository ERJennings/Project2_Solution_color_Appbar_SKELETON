package com.example.solution_color;


import android.Manifest;
import android.content.Intent;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.library.bitmap_utilities.BitMap_Helpers;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener {

    //these are constants and objects that I used, use them if you wish
    private static final String DEBUG_TAG = "CartoonActivity";
    private static final String ORIGINAL_FILE = "origfile.png";
    private static final String PROCESSED_FILE = "procfile.png";

    private static final int TAKE_PICTURE = 1;
    private static final double SCALE_FROM_0_TO_255 = 2.55;
    private static final int DEFAULT_COLOR_PERCENT = 3;
    private static final int DEFAULT_BW_PERCENT = 15;

    //preferences
    private int saturation = DEFAULT_COLOR_PERCENT;
    private int bwPercent = DEFAULT_BW_PERCENT;
    private String shareSubject;
    private String shareText;

    //where images go
    private String originalImagePath;   //where orig image is
    private String processedImagePath;  //where processed image is
    private Uri outputFileUri;          //tells camera app where to store image

    //used to measure screen size
    int screenheight;
    int screenwidth;

    private ImageView myImage;

    //these guys will hog space
    Bitmap bmpOriginal;                 //original image
    Bitmap bmpThresholded;              //the black and white version of original image
    Bitmap bmpThresholdedColor;         //the colorized version of the black and white image

    //TODO manage all the permissions you need
    //private static final int PERMISSION_REQUEST_CAMERA = 0;
    //private static final int PERMISSION_REQUEST_READ = 0;
    //private static final int PERMISSION_REQUEST_WRITE = 0;
    private static final String[] PERMISSION_LIST = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int PERMISSION_REQUEST_ALL = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO be sure to set up the appbar in the activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //dont display these
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FloatingActionButton fab = findViewById(R.id.buttonTakePicture);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO manage this, mindful of permissions

            }
        });

        //get the default image
        myImage = (ImageView) findViewById(R.id.imageView1);


        //TODO manage the preferences and the shared preference listenes
        // TODO and get the values already there getPrefValues(settings);
        //TODO use getPrefValues(SharedPreferences settings)

        // Fetch screen height and width,
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        screenheight = metrics.heightPixels;
        screenwidth = metrics.widthPixels;

        setUpFileSystem();
    }

    private void setImage() {
        //prefer to display processed image if available
        bmpThresholded = Camera_Helpers.loadAndScaleImage(processedImagePath, screenheight, screenwidth);
        if (bmpThresholded != null) {
            myImage.setImageBitmap(bmpThresholded);
            Log.d(DEBUG_TAG, "setImage: myImage.setImageBitmap(bmpThresholded) set");
            return;
        }

        //otherwise fall back to unprocessd photo
        bmpOriginal = Camera_Helpers.loadAndScaleImage(originalImagePath, screenheight, screenwidth);
        if (bmpOriginal != null) {
            myImage.setImageBitmap(bmpOriginal);
            Log.d(DEBUG_TAG, "setImage: myImage.setImageBitmap(bmpOriginal) set");
            return;
        }

        //worst case get from default image
        //save this for restoring
        bmpOriginal = BitMap_Helpers.copyBitmap(myImage.getDrawable());
        Log.d(DEBUG_TAG, "setImage: bmpOriginal copied");
    }

    //TODO use this to set the following member preferences whenever preferences are changed.
    //TODO Please ensure that this function is called by your preference change listener
    private void getPrefValues(SharedPreferences settings) {
        //TODO should track shareSubject, shareText, saturation, bwPercent
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    private void setUpFileSystem(){
        //TODO do we have needed permissions?
        //TODO if not then dont proceed

        //get some paths
        // Create the File where the photo should go
        File photoFile = createImageFile(ORIGINAL_FILE);
        originalImagePath = photoFile.getAbsolutePath();

        File processedfile = createImageFile(PROCESSED_FILE);
        processedImagePath=processedfile.getAbsolutePath();

        //worst case get from default image
        //save this for restoring
        if (bmpOriginal == null)
            bmpOriginal = BitMap_Helpers.copyBitmap(myImage.getDrawable());

        setImage();
    }

    //TODO manage creating a file to store camera image in
    //TODO where photo is stored
    private File createImageFile(final String fn) {
        //TODO fill in
        return null;
    }

    //DUMP for students
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // permissions

    /***
     * callback from requestPermissions
     * @param permsRequestCode  user defined code passed to requestpermissions used to identify what callback is coming in
     * @param permissions       list of permissions requested
     * @param grantResults      //results of those requests
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        //TODO fill in

        //See for reference: onRequestPermissionsResult in RuntimePermissionsBasic

        if (permsRequestCode == PERMISSION_REQUEST_ALL) {
            for (String permissionCheck : permissions) {

                //START CAMERA PERMISSIONS
                if (permissionCheck.equals(Manifest.permission.CAMERA)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.cameraAccess, Snackbar.LENGTH_SHORT).show();
                    }
                    else {
                        Snackbar.make(findViewById(android.R.id.content), R.string.noCameraAccess, Snackbar.LENGTH_SHORT).show();
                    }
                }
                //END CAMERA PERMISSIONS

                //START READ PERMISSIONS
                if (permissionCheck.equals(Manifest.permission.CAMERA)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.readAccess, Snackbar.LENGTH_SHORT).show();
                    }
                    else {
                        Snackbar.make(findViewById(android.R.id.content), R.string.noReadAccess, Snackbar.LENGTH_SHORT).show();
                    }
                }
                //END READ PERMISSIONS

                //START WRITE PERMISSIONS
                if (permissionCheck.equals(Manifest.permission.CAMERA)) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Snackbar.make(findViewById(android.R.id.content), R.string.writeAccess, Snackbar.LENGTH_SHORT).show();
                    }
                    else {
                        Snackbar.make(findViewById(android.R.id.content), R.string.noWriteAccess, Snackbar.LENGTH_SHORT).show();
                    }
                }
                //END WRITE PERMISSIONS
            }
        }
    }

    //DUMP for students
    /**
     * Verify that the specific list of permisions requested have been granted, otherwise ask for
     * these permissions.  Note this is coarse in that I assumme I need them all
     */
    private boolean verifyPermissions() {
        //TODO fill in

        //See for reference: requestCameraPermission and showCameraPreview in PermissionDemo

        boolean verified = true;

        for (String permissionCheck : PERMISSION_LIST) {

            verified = true;

            if (ActivityCompat.checkSelfPermission(this, permissionCheck) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, PERMISSION_LIST, PERMISSION_REQUEST_ALL);
                verified = false;

            }

            return verified;

        }

        //and return false until they are granted
        //return false;

        //verified = true;
        return verified;
    }


    //take a picture and store it on external storage
    public void doTakePicture() {
        //TODO verify that app has permission to use camera

        //TODO manage launching intent to take a picture

    }

    //TODO manage return from camera and other activities
    // TODO handle edge cases as well (no pic taken)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO get photo
        //TODO set the myImage equal to the camera image returned
        //TODO tell scanner to pic up this unaltered image
        //TODO save anything needed for later

    }

    /**
     * delete original and processed images, then rescan media paths to pick up that they are gone.
     */
    private void doReset() {
        //TODO verify that app has permission to use file system
        //do we have needed permissions?
        if (!verifyPermissions()) {
            return;
        }
        //delete the files
        Camera_Helpers.delSavedImage(originalImagePath);
        Camera_Helpers.delSavedImage(processedImagePath);
        bmpThresholded = null;
        bmpOriginal = null;

        myImage.setImageResource(R.drawable.gutters);
        myImage.setScaleType(ImageView.ScaleType.FIT_CENTER);//what the hell? why both
        myImage.setScaleType(ImageView.ScaleType.FIT_XY);

        //worst case get from default image
        //save this for restoring
        bmpOriginal = BitMap_Helpers.copyBitmap(myImage.getDrawable());

        //TODO make media scanner pick up that images are gone

    }

    public void doSketch() {
        //TODO verify that app has permission to use file system
        //do we have needed permissions?
        if (!verifyPermissions()) {
            return;
        }

        //sketchify the image
        if (bmpOriginal == null){
            Log.e(DEBUG_TAG, "doSketch: bmpOriginal = null");
            return;
        }
        bmpThresholded = BitMap_Helpers.thresholdBmp(bmpOriginal, bwPercent);

        //set image
        myImage.setImageBitmap(bmpThresholded);

        //save to file for possible email
        Camera_Helpers.saveProcessedImage(bmpThresholded, processedImagePath);
        scanSavedMediaFile(processedImagePath);
    }

    public void doColorize() {
        //TODO verify that app has permission to use file system
        //do we have needed permissions?
        if (!verifyPermissions()) {
            return;
        }

        //colorize the image
        if (bmpOriginal == null){
            Log.e(DEBUG_TAG, "doColorize: bmpOriginal = null");
            return;
        }
        //if not thresholded yet then do nothing
        if (bmpThresholded == null){
            Log.e(DEBUG_TAG, "doColorize: bmpThresholded not thresholded yet");
            return;
        }

        //otherwise color the bitmap
        bmpThresholdedColor = BitMap_Helpers.colorBmp(bmpOriginal, saturation);

        //takes the thresholded image and overlays it over the color one
        //so edges are well defined
        BitMap_Helpers.merge(bmpThresholdedColor, bmpThresholded);

        //set background to new image
        myImage.setImageBitmap(bmpThresholdedColor);

        //save to file for possible email
        Camera_Helpers.saveProcessedImage(bmpThresholdedColor, processedImagePath);
        scanSavedMediaFile(processedImagePath);
    }

    public void doShare() {
        //TODO verify that app has permission to use file system
        //do we have needed permissions?
        if (!verifyPermissions()) {
            return;
        }

        //TODO share the processed image with appropriate subject, text and file URI
        //TODO the subject and text should come from the preferences set in the Settings Activity

    }

    //TODO set this up
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO handle all of the appbar button clicks

        return true;
    }

    //TODO set up pref changes
    @Override
    public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
        //TODO reload prefs at this point
    }

    /**
     * Notifies the OS to index the new image, so it shows up in Gallery.
     * see https://www.programcreek.com/java-api-examples/index.php?api=android.media.MediaScannerConnection
     */
    private void scanSavedMediaFile( final String path) {
        // silly array hack so closure can reference scannerConnection[0] before it's created
        final MediaScannerConnection[] scannerConnection = new MediaScannerConnection[1];
        try {
            MediaScannerConnection.MediaScannerConnectionClient scannerClient = new MediaScannerConnection.MediaScannerConnectionClient() {
                public void onMediaScannerConnected() {
                    scannerConnection[0].scanFile(path, null);
                }

                @Override
                public void onScanCompleted(String path, Uri uri) {

                }

            };
            scannerConnection[0] = new MediaScannerConnection(this, scannerClient);
            scannerConnection[0].connect();
        } catch (Exception ignored) {
        }
    }
}

