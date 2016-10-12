package fyp.hkust.facet;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import fyp.hkust.facet.whiteBalance.algorithms.WhitePatch.WhitePatch;
import fyp.hkust.facet.whiteBalance.algorithms.grayWorld.GrayWorld;
import fyp.hkust.facet.whiteBalance.algorithms.histogramStretching.HistogramStretching;
import fyp.hkust.facet.whiteBalance.algorithms.improvedWP.ImprovedWP;

import static android.graphics.Bitmap.createScaledBitmap;

public class CaptureActivity extends AppCompatActivity {

    private Context instance;
    private Filter filter;

    private String imagePath;
    private TextView colorresult;
    private ImageButton mImgButton1;
    private ImageView mImgResult;
    private final String TAG = "ConvertedPhotos";
    private ProgressBar waitingCircle;

    private ImageButton[] imageButtons;

    private Bitmap originalBitmap;

    private int scaledHeight = 0;
    private int scaledWidth = 0;

    private Bitmap scaledBitmap;
    private Bitmap convertedBitmap;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private ProgressDialog barProgressDialog;

    private Handler mUI_Handler = new Handler();
    private Handler mThreadHandler;
    private HandlerThread mThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        Intent intent = this.getIntent();
        String path = intent.getStringExtra("path");
        String color = intent.getStringExtra("color");

        barProgressDialog = new ProgressDialog(this);

        imageButtons = new ImageButton[]{
                (ImageButton) findViewById(R.id.original_image),
                (ImageButton) findViewById(R.id.converted_image1),
                (ImageButton) findViewById(R.id.converted_image2),
                (ImageButton) findViewById(R.id.converted_image3),
                (ImageButton) findViewById(R.id.converted_image4)
        };

        mImgResult = (ImageView) findViewById(R.id.image_show);
        waitingCircle = (ProgressBar) findViewById(R.id.progressBar);
        mImgButton1 = (ImageButton) findViewById(R.id.image_result);

        File f = new File(path);
        originalBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
        changeDimensions();

        //聘請一個特約工人，有其經紀人派遣其工人做事 (另起一個有Handler的Thread)

        mThread = new HandlerThread("name");

        //讓Worker待命，等待其工作 (開啟Thread)

        mThread.start();

        //找到特約工人的經紀人，這樣才能派遣工作 (找到Thread上的Handler)

        mThreadHandler=new Handler(mThread.getLooper());

        //請經紀人指派工作名稱 r，給工人做

        mThreadHandler.post(r1);

        mImgButton1.setImageBitmap(scaledBitmap);
        mImgResult.setImageBitmap(convertedBitmap);
        imageButtons[0].setImageBitmap(scaledBitmap);
        imageButtons[1].setImageBitmap(convertedBitmap);

        barProgressDialog.dismiss();

        Mat demo = new Mat();
        Utils.bitmapToMat(convertedBitmap,demo);
        Mat gray_demo = new Mat();
        Imgproc.cvtColor(demo, gray_demo, Imgproc.COLOR_RGB2GRAY);


        colorresult = (TextView) findViewById(R.id.textView2);
        colorresult.setText(color);
    }

    //工作名稱 r1 的工作內容

    private Runnable r1=new Runnable () {

        public void run() {

            // TODO Auto-generated method stub

            //.............................

            //做了很多事

            scaledBitmap = createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, false);

            GrayWorld grayWorldS = new GrayWorld(scaledBitmap);
            convertedBitmap = grayWorldS.getConvertedBitmap();
            //Log.d("path", path);
            //請經紀人指派工作名稱 r，給工人做
            mUI_Handler.post(r2);
        }

    };


    //工作名稱 r2 的工作內容

    private Runnable r2=new Runnable () {

        public void run() {

            // TODO Auto-generated method stub
            barProgressDialog.setTitle("Image Processing ...");
            barProgressDialog.setMessage("Image in progress ...");
            barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
            barProgressDialog.show();
            //.............................
            //顯示畫面的動作
        }

    };

    protected void onDestroy() {

        super.onDestroy();

        //移除工人上的工作

        if (mThreadHandler != null) {

            mThreadHandler.removeCallbacks(r1);

        }

        //解聘工人 (關閉Thread)

        if (mThread != null) {

            mThread.quit();

        }

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

        int widthImage = originalBitmap.getWidth();
        int widthImageDp = pxToDp(widthImage);
        int heightImage = originalBitmap.getHeight();
        int heightImageDp = pxToDp(heightImage);

        Log.i(TAG, "bitmap width in px: " + Integer.toString(widthImage));
        Log.i(TAG, "bitmap height in px: " + Integer.toString(heightImage));
        Log.i(TAG, "bitmap width in dp: " + Integer.toString(widthImageDp));
        Log.i(TAG, "bitmap height in dp: " + Integer.toString(heightImageDp));

        if(heightDisplay - 300 >= heightImage && widthDisplay >= widthImage) {
            scaledHeight = heightImage;
            scaledWidth = widthImage;
        } else {
            scaledHeight = heightDisplay - 300;
            double ratio = (double)scaledHeight / (double)heightImage;
            scaledWidth = (int)((double)widthImage * ratio);
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
