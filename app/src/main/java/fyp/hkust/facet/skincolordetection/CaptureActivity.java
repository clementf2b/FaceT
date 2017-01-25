package fyp.hkust.facet.skincolordetection;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;
import com.roger.gifloadinglibrary.GifLoadingView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;
import fyp.hkust.facet.R;
import fyp.hkust.facet.activity.MainMenuActivity;
import fyp.hkust.facet.util.FontManager;
import fyp.hkust.facet.whiteBalance.algorithms.grayWorld.GrayWorld;
import fyp.hkust.facet.whiteBalance.algorithms.histogramStretching.HistogramStretching;
import fyp.hkust.facet.whiteBalance.algorithms.improvedWP.ImprovedWP;
import id.zelory.compressor.Compressor;
import me.shaohui.advancedluban.Luban;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;

public class CaptureActivity extends AppCompatActivity implements View.OnClickListener, ToolTipView.OnToolTipViewClickedListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private Context instance;
    private Filter filter;

    private String imagePath;
    private TextView colorresult;
    private Mat demo;
    private ImageView mImgResult;
    private ProgressBar waitingCircle;
    private Intent intent;
    private ImageButton[] imageButtons;
    private TextView[] textViews;
    private String comeFromActivity;

    private Bitmap originalBitmap;
    private Bitmap compressedBitmap;

    private int scaledHeight = 0;
    private int scaledWidth = 0;
    private int taskCounter;
    private int photoQuality = 15;
    private int selectPhoto = 0;

    private Bitmap scaledBitmap;
    // add this number to count their finished order
    private ArrayList<Bitmap> convertedBitmaps;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private ArrayList<Integer> convertNumber;
    private ToolTipView mBlueToolTipView;
    private ToolTipRelativeLayout toolTipRelativeLayout;
    private int n;
    private GifLoadingView mView;
    private Handler handler; // declared before onCreate
    private Runnable myRunnable;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));

        SpannableString s = new SpannableString(getResources().getString(R.string.app_name));
        s.setSpan(new TypefaceSpan(FontManager.APP_FONT), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_capture_layout), fontType);

        verifyStoragePermissions(this);
        intent = this.getIntent();
        String path = intent.getStringExtra("path");
        if (intent.getStringExtra("activity") != null) {
            comeFromActivity = intent.getStringExtra("activity");
            photoQuality = 65;
        } else {
            comeFromActivity = "Other";
        }
        //       String color = intent.getStringExtra("color");

        imageButtons = new ImageButton[]{
                (ImageButton) findViewById(R.id.original_image),
                (ImageButton) findViewById(R.id.converted_image1),
                (ImageButton) findViewById(R.id.converted_image2),
                (ImageButton) findViewById(R.id.converted_image3),
        };

        textViews = new TextView[]{
                (TextView) findViewById(R.id.original_image_text),
                (TextView) findViewById(R.id.converted_image1_text),
                (TextView) findViewById(R.id.converted_image2_text),
                (TextView) findViewById(R.id.converted_image3_text),
        };

        mImgResult = (ImageView) findViewById(R.id.image_show);
        waitingCircle = (ProgressBar) findViewById(R.id.progressBar);

        //pop up hint
        toolTipRelativeLayout = (ToolTipRelativeLayout) findViewById(R.id.capture_activity_tooltipRelativeLayout);
        findViewById(R.id.capture_activity_redtv).setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addBlueToolTipView();
            }
        }, 1100);
        //end of hint

        File f = new File(path);
//        originalBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

        Luban.compress(getApplicationContext(), f)
                .putGear(Luban.CUSTOM_GEAR)
                .asObservable()                             // generate Observable
                .subscribe();      // subscribe the compress result

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float width = size.x * 0.7f;
        float height = size.y * 0.6f;
        mImgResult.setPadding((int) (size.x * 0.15), (int) (size.y * 0.05), (int) (size.x * 0.15), (int) (size.y * 0.05));
        if (comeFromActivity.equals("ShowCameraViewActivity")) {
            width = size.x * 0.8f;
            height = size.y * 0.8f;
            mImgResult.setPadding((int) (size.x * 0.10), (int) (size.y * 0.1), (int) (size.x * 0.10), (int) (size.y * 0.1));
        }
        Log.d(TAG, " device_size:" + width + " : " + height);
        compressedBitmap = new Compressor.Builder(CaptureActivity.this)
                .setMaxWidth(width)
                .setMaxHeight(height)
                .setQuality(photoQuality)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .build()
                .compressToBitmap(f);

        compressedBitmap = RotateBitmap(compressedBitmap, -90, comeFromActivity);
//        changeDimensions();
        //scaledBitmap = createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, false);
        //Log.d("path", path);
        taskCounter = 0;

        Random rand = new Random();
        n = rand.nextInt(11) + 1;
        //12 is the maximum and the 1 is our minimum
        try {
            /*recycling unused objects in order to
            make the memory they currently occupy available for quick reuse.*/
            System.gc();
            int id = getResources().getIdentifier("num" + n, "drawable", getPackageName());
            mView = new GifLoadingView();
            mView.setBackgroundResource(id);
            mView.show(getFragmentManager(), "");
            mView.setCancelable(false);
            mView.setDimming(true);
            mView.setRadius(1);

            final MyTask myTask1 = new MyTask(this, 1);
            myTask1.execute();
            final MyTask myTask2 = new MyTask(this, 2);
            StartAsyncTaskInParallel(myTask2);
            final MyTask myTask3 = new MyTask(this, 3);
            StartAsyncTaskInParallel(myTask3);

        } catch (Exception obj) {

        }

//        initThreads();
        // colorresult.setText(color);

        imageButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mImgResult.setImageBitmap(compressedBitmap);
                selectEffect(imageButtons[0], textViews[0]);
                selectPhoto = 0;
            }
        });

        imageButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mImgResult.setImageBitmap(convertedBitmaps.get(0));
                selectEffect(imageButtons[1], textViews[1]);
                selectPhoto = 1;
                mBlueToolTipView.setVisibility(View.GONE);
                toolTipRelativeLayout.setVisibility(View.GONE);
            }
        });

        imageButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mImgResult.setImageBitmap(convertedBitmaps.get(1));
                selectEffect(imageButtons[2], textViews[2]);
                selectPhoto = 2;
            }
        });

        imageButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mImgResult.setImageBitmap(convertedBitmaps.get(2));
                selectEffect(imageButtons[3], textViews[3]);
                selectPhoto = 3;
            }
        });
    }

    public static void verifyStoragePermissions(Activity activity) {

        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void selectEffect(ImageButton selectButton, TextView selectText) {

        for (int i = 0; i < 4; i++) {
            imageButtons[i].setElevation(0.0f);
            textViews[i].setElevation(0.0f);
            textViews[i].setTypeface(null, Typeface.NORMAL);
            textViews[i].setTextSize(12.0f);
        }
        selectButton.setElevation(20.0f);
        selectText.setElevation(20.0f);
        selectText.setTypeface(null, Typeface.BOLD);
        selectText.setTextSize(18.0f);
    }

    //adding the hints
    @Override
    public void onToolTipViewClicked(ToolTipView toolTipView) {
        if (mBlueToolTipView == toolTipView) {
            mBlueToolTipView = null;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.capture_activity_redtv) {
            if (mBlueToolTipView == null) {
                addBlueToolTipView();
            } else {
                mBlueToolTipView.remove();
                mBlueToolTipView = null;
            }

        }
    }

    private void addBlueToolTipView() {
        ToolTip toolTip = new ToolTip()
                .withText("Select a suitable white balance and click right top button")
                .withColor(getResources().getColor(R.color.colorPrimary))
                .withAnimationType(ToolTip.AnimationType.FROM_MASTER_VIEW);

        mBlueToolTipView = toolTipRelativeLayout.showToolTipForView(toolTip, findViewById(R.id.capture_activity_redtv));
        mBlueToolTipView.setOnToolTipViewClickedListener(this);
    }

    //end of the hint
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    demo = new Mat();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.whitebalance_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.apply_btn:
                Snackbar.make(this.findViewById(android.R.id.content), "Click apply button =]", Snackbar.LENGTH_SHORT).show();
                final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
                pDialog.setCancelable(true);
                pDialog.setTitleText("Are you sure?")
                        .setContentText("effect " + selectPhoto + " is the most suitable?")
                        .setConfirmText("Yes,start it!")
                        .setCancelText("No")
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                Snackbar.make(findViewById(android.R.id.content), "Cancel sweet alert", Snackbar.LENGTH_SHORT).show();
                                pDialog.dismiss();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog
                                        .setTitleText("Skin Color Detection")
                                        .setContentText("Skin Color Detection will begin")
                                        .setConfirmText("OK")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                saveImageAndIntent();
                                            }
                                        })
                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            }
                        }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void saveImageAndIntent() {
        try {
            //Write file
            String filename = "bitmap.png";
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            convertedBitmaps.get(selectPhoto).compress(Bitmap.CompressFormat.PNG, 100, stream);

            //Cleanup
            stream.close();
            convertedBitmaps.get(selectPhoto).recycle();

            //Pop intent
            Intent colorDectectionIntent = new Intent(this, ColorDetectionActivity.class);
            colorDectectionIntent.putExtra("image", filename);
            startActivity(colorDectectionIntent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle, String check) {
        Log.d(TAG + "activity compare: ", check + " : " + "ShowCameraViewActivity");
        Matrix matrix = new Matrix();
        //rotate if the phone come from taking photo
        if (check.equals("ShowCameraViewActivity")) {
            matrix.postRotate(angle);
        }
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private class MyTask extends AsyncTask<String, Integer, Integer> {
        int number;
        private String content = null;
        private boolean error = false;
        Context mContext;
        int NOTIFICATION_ID = 1;
        Notification mNotification;
        NotificationManager mNotificationManager;

        public MyTask(Context context, int target) {

            this.mContext = context;
            number = target;
            //Get the notification manager
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        }

        private void createNotification(Context context, String message) {
            Intent notificationIntent = new Intent(context, CaptureActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentIntent(intent)
                    .setPriority(PRIORITY_HIGH) //private static final PRIORITY_HIGH = 5;
                    .setContentText(message)
                    .setAutoCancel(true);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }

        @Override
        protected Integer doInBackground(String... param) {

            try {
                convertedBitmaps = new ArrayList<Bitmap>(5);
                if (!convertedBitmaps.contains(compressedBitmap)) {
                    convertedBitmaps.add(compressedBitmap);
                }

                switch (number) {
                    case 1:
                        GrayWorld grayWorld = new GrayWorld(compressedBitmap);
                        convertedBitmaps.add(grayWorld.getConvertedBitmap());
                        break;
                    case 2:
                        HistogramStretching histogramStretching = new HistogramStretching(compressedBitmap);
                        convertedBitmaps.add(histogramStretching.getConvertedBitmap());
                        break;
                    case 3:
                        ImprovedWP improvedWP = new ImprovedWP(compressedBitmap);
                        convertedBitmaps.add(improvedWP.getConvertedBitmap());
                        break;
                }
                Log.d("Finish Task", "");
                taskCounter++;
            } catch (Exception e) {
                Log.w("HTTP4:", e);
                content = e.getMessage();
                error = true;
                cancel(true);
            }
//            convertedBitmaps = new Bitmap[] {
//                    scaledBitmap,
//                    histogramStretching.getConvertedBitmap(),
//                    grayWorld.getConvertedBitmap(),
//                    improvedWP.getConvertedBitmap()
//            };
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (error) {
                createNotification(CaptureActivity.this, "Image Processing ended abnormally!");

            } else {
                Log.d("taskcounter :", taskCounter + "");
                mImgResult.setImageBitmap(compressedBitmap);
                imageButtons[0].setImageBitmap(compressedBitmap);
                selectEffect(imageButtons[0], textViews[0]);
                switch (number) {
                    case 1:
                        imageButtons[1].setImageBitmap(convertedBitmaps.get(taskCounter - 1));
                        mView.dismiss();
//                    convertNumber.add(taskCounter-1);
                        break;
                    case 2:
                        imageButtons[2].setImageBitmap(convertedBitmaps.get(taskCounter - 1));
//                    convertNumber.add(taskCounter-1);

                        break;
                    case 3:
                        imageButtons[3].setImageBitmap(convertedBitmaps.get(taskCounter - 1));
//                    convertNumber.add(taskCounter-1);
                        break;
                }
                createNotification(CaptureActivity.this, "Image processing is complete!");
                Toast.makeText(mContext, "Image processing is complete!", Toast.LENGTH_SHORT).show();
            }


//            demo= new Mat();
//            Utils.bitmapToMat(convertedBitmaps[1],demo);
//            Mat gray_demo = new Mat();
//            Imgproc.cvtColor(demo, gray_demo, Imgproc.COLOR_RGB2GRAY);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            createNotification(CaptureActivity.this, "Image is in progress");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void StartAsyncTaskInParallel(MyTask task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            task.execute();
    }


    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    public void changeDimensions() {
        // dimensions of display
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthDisplay = size.x;
        int heightDisplay = size.y;
        int widthDisplayDp = pxToDp(widthDisplay);
        int heightDisplayDp = pxToDp(heightDisplay);

        Log.i(TAG, "display width in px: " + Integer.toString(widthDisplay));
        Log.i(TAG, "display height in px: " + Integer.toString(heightDisplay));
        Log.i(TAG, "display width in dp: " + Integer.toString(widthDisplayDp));
        Log.i(TAG, "display height in dp: " + Integer.toString(heightDisplayDp));

        int widthImage = compressedBitmap.getWidth();
        int widthImageDp = pxToDp(widthImage);
        int heightImage = compressedBitmap.getHeight();
        int heightImageDp = pxToDp(heightImage);

        Log.i(TAG, "bitmap width in px: " + Integer.toString(widthImage));
        Log.i(TAG, "bitmap height in px: " + Integer.toString(heightImage));
        Log.i(TAG, "bitmap width in dp: " + Integer.toString(widthImageDp));
        Log.i(TAG, "bitmap height in dp: " + Integer.toString(heightImageDp));

        if (heightDisplay - 300 >= heightImage && widthDisplay >= widthImage) {
            scaledHeight = heightImage;
            scaledWidth = widthImage;
        } else {
            scaledHeight = heightDisplay - 300;
            double ratio = (double) scaledHeight / (double) heightImage;
            scaledWidth = (int) ((double) widthImage * ratio);
        }
        Log.i(TAG, "scaled width: " + Integer.toString(scaledWidth));
        Log.i(TAG, "scaled height: " + Integer.toString(scaledHeight));
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.ydpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

}
