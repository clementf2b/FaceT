package fyp.hkust.facet.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.Face;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.provider.BigImageCardProvider;
import com.roger.gifloadinglibrary.GifLoadingView;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfFloat6;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Subdiv2D;
import org.opencv.photo.Photo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import fyp.hkust.facet.R;
import fyp.hkust.facet.util.FontManager;
import fyp.hkust.facet.util.FileUtils;
import fyp.hkust.facet.util.ImageUtils;
import hugo.weaving.DebugLog;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.INTER_LINEAR;

public class SwapFaceActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMG = 1;
    private static final int RESULT_LOAD_IMG2 = 2;
    private static final int REQUEST_CODE_PERMISSION = 2;

    private static final String TAG = "MainActivity";

    // Storage Permissions
    private static String[] PERMISSIONS_REQ = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    protected String mTestImgPath;
    // UI

    protected ImageView mDetectResultImage;
    protected ImageView mDetectResultImage2;
    protected FloatingActionButton mFabActionBt;

    FaceDet mFaceDet;
    protected Bitmap bitmapResult;
    protected Mat mRgb;
    private double[] rValue;
    private double[] gValue;
    private double[] bValue;
    private FloatingActionButton mgetcolorbtn;
    private SeekBar mRseekBar;
    private SeekBar mGseekBar;
    private SeekBar mBseekBar;
    private Mat C;
    private double[] temp;
    private List<Point> landmarks;
    private List<Point> landmarks2;
    private int counter = 1;
    private String imgPathResult1, imgPathResult2;
    private Bitmap bitmapResult2;
    private FloatingActionButton mFabSwapFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swap_face);

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_swap_face_layout), fontType);

        mFabSwapFace = (FloatingActionButton) findViewById(R.id.swapfacebtn);
        mFabActionBt = (FloatingActionButton) findViewById(R.id.fab);
        mDetectResultImage = (ImageView) findViewById(R.id.detect_result_image);
        mDetectResultImage2 = (ImageView) findViewById(R.id.detect_result_image2);
        mgetcolorbtn = (FloatingActionButton) findViewById(R.id.getcolorbtn);
        mRseekBar = (SeekBar) findViewById(R.id.rseekBar);
        mGseekBar = (SeekBar) findViewById(R.id.gseekBar);
        mBseekBar = (SeekBar) findViewById(R.id.bseekBar);

        mFabActionBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SwapFaceActivity.this, "Pick one image", Toast.LENGTH_SHORT).show();
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);

                runDetectAsync2();

            }
        });

        mgetcolorbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getRGBValue();
            }
        });

        mFabSwapFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faceSwap();
            }
        });

        mRseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
//                seekBarValue.setText(String.valueOf(progress));
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do something here,
                //if you want to do anything at the start of
                // touching the seekbar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Display the value in textview
                C.convertTo(C, CvType.CV_8UC4);
                for (int i = 0; i < mRgb.total(); i++)
                    temp[i] = (double) progress;
                Log.d(TAG, temp[0] + " " + progress);

                C.put(0, 0, temp);
                Bitmap bmp = Bitmap.createBitmap(C.cols(), C.rows(),
                        Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(C, bmp);
                mDetectResultImage.setImageBitmap(bmp);
                Log.d("mRseekBar", progress + "/" + seekBar.getMax());
            }
        });

        mGseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do something here,
                //if you want to do anything at the start of
                // touching the seekbar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Display the value in textview

                C.convertTo(C, CvType.CV_8UC4);
                for (int i = (int) mRgb.total(); i < mRgb.total() * 2; i++)
                    temp[i] = (double) progress;
                C.put(0, 0, temp);
                Bitmap bmp = Bitmap.createBitmap(C.cols(), C.rows(),
                        Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(C, bmp);
                mDetectResultImage.setImageBitmap(bmp);
                Log.d("mGseekBar", progress + "/" + seekBar.getMax());
            }
        });

        mBseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do something here,
                //if you want to do anything at the start of
                // touching the seekbar
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Display the value in textview

                Log.d("mBseekBar", progress + "/" + seekBar.getMax());
                C.convertTo(C, CvType.CV_8UC4);
                for (int i = (int) mRgb.total() * 2; i < mRgb.total() * 3; i++)
                    temp[i] = (double) progress;
                C.put(0, 0, temp);
                Bitmap bmp = Bitmap.createBitmap(C.cols(), C.rows(),
                        Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(C, bmp);
                mDetectResultImage.setImageBitmap(bmp);
            }
        });

        // Just use hugo to print log
        isExternalStorageWritable();
        isExternalStorageReadable();

        // For API 23+ you need to request the read/write permissions even if they are already in your manifest.
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion >= Build.VERSION_CODES.M) {
            verifyPermissions(this);
        }


    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    break;
                default:
                    super.onManagerConnected(status);
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

    public void getRGBValue() {
        try {

            long start = System.currentTimeMillis();
            mRgb = new Mat(bitmapResult.getHeight(), bitmapResult.getHeight(), CvType.CV_64FC3);
            Utils.bitmapToMat(bitmapResult, mRgb);

            //method one to get color
            C = mRgb.clone();
            mRgb.convertTo(mRgb, CvType.CV_64FC3); // New line added.
//            total() gives you total number of pixels in the image and channel() gives you number of channels
            int size = (int) (mRgb.total() * mRgb.channels());
            temp = new double[size]; // use double[] instead of byte[]
            mRgb.get(0, 0, temp);
            Log.d(TAG, "get the rValue");
            rValue = new double[(int) mRgb.total()];
            gValue = new double[(int) mRgb.total()];
            bValue = new double[(int) mRgb.total()];

            for (int i = 0; i < mRgb.total(); i++) {
//                temp[i] = (temp[i] / 2);  // no more casting required.
//                Log.d("Color: ", temp[i] +" ");
                rValue[i] = temp[i];
//                Log.d("rValue:",rValue[i]  + " " + temp[i]);
            }
            Log.d(TAG, "get the gValue");
            for (int i = (int) mRgb.total(); i < mRgb.total() * 2; i++)
                gValue[i - (int) mRgb.total()] = temp[i];
            Log.d(TAG, "get the bValue");
            for (int i = (int) mRgb.total() * 2; i < mRgb.total() * 3; i++)
                bValue[i - (int) mRgb.total() * 2] = temp[i];
            Log.d(TAG, "finish");
            // set the value and place the photo
//            C.put(0, 0, temp);
//            Bitmap bmp = Bitmap.createBitmap(C.cols(), C.rows(),
//                    Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(C, bmp);
//            mDetectResultImage.setImageBitmap(bmp);

            //method 2 to get RGB value
//            for (int a = 0; a < mRgb.rows(); a++) {
//                for (int b = 0; b < mRgb.cols(); b++) {
//                    //returns an array of all the channels values at (x, y), each channel in a different place.
//                    //Mat.get(x, y);
//                    //- sets the channel values at (x, y) to value.
//                    //Mat.put(x, y, value)
//                    rgbValue = mRgb.get(a, b);
//                    Log.i("", "red:"+rgbValue[0]+"green:"+rgbValue[1]+"blue:"+rgbValue[2]);
////                image.put(a, b, new double[]{255, 255, 0});//sets the pixel to yellow
//                }
//            }
            long runTime = System.currentTimeMillis() - start;
            Log.d("Total run time", runTime * 1000 + " Secs");
        } catch (Exception e) {

        }

    }

    /**
     * Checks if the app has permission to write to device storage or open camera
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    @DebugLog
    private static boolean verifyPermissions(Activity activity) {
        // Check if we have write permission
        int write_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read_persmission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera_permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (write_permission != PackageManager.PERMISSION_GRANTED ||
                read_persmission != PackageManager.PERMISSION_GRANTED ||
                camera_permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_REQ,
                    REQUEST_CODE_PERMISSION
            );
            return false;
        } else {
            return true;
        }
    }

    /* Checks if external storage is available for read and write */
    @DebugLog
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    @DebugLog
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @DebugLog
    protected void demoStaticImage() {
        if (mTestImgPath != null) {
            Log.d(TAG, "demoStaticImage() launch a task to det");
            runDetectAsync(mTestImgPath);
        } else {
            Log.d(TAG, "demoStaticImage() mTestImgPath is null, go to gallery");
            Toast.makeText(SwapFaceActivity.this, "Pick an image to run algorithms", Toast.LENGTH_SHORT).show();
            // Create intent to Open Image applications like Gallery, Google Photos
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            Toast.makeText(SwapFaceActivity.this, "Demo using static images", Toast.LENGTH_SHORT).show();
            demoStaticImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
                Random rand = new Random();
                int n = rand.nextInt(11) + 1;
                System.gc();
                int id = getResources().getIdentifier("num" + n, "drawable", getPackageName());
                GifLoadingView mView = new GifLoadingView();
                mView.setBackgroundResource(id);
                mView.show(getFragmentManager(), "");
                mView.setCancelable(false);
                mView.setDimming(true);


                // Get the Image from data
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mTestImgPath = cursor.getString(columnIndex);
                cursor.close();
                if (mTestImgPath != null) {
                    runDetectAsync(mTestImgPath);
                    imgPathResult1 = mTestImgPath;
                    Toast.makeText(this, "Img Path:" + mTestImgPath, Toast.LENGTH_SHORT).show();
                }
                mView.dismiss();

            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }


    // ==========================================================
    // Tasks inner class
    // ==========================================================
    private ProgressDialog mDialog;

    @NonNull
    protected void runDetectAsync(@NonNull String imgPath) {
        showDiaglog();

        final String targetPath = Constants.getFaceShapeModelPath();
        if (!new File(targetPath).exists()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SwapFaceActivity.this, "Copy landmark model to " + targetPath, Toast.LENGTH_SHORT).show();
                }
            });
            FileUtils.copyFileFromRawToOthers(getApplicationContext(), R.raw.shape_predictor_68_face_landmarks, targetPath);
        }
        // Init
        if (mFaceDet == null) {
            mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
        }

        Log.d(TAG, "Image path: " + imgPath);
        //face square point
        List<VisionDetRet> faceList = mFaceDet.detect(imgPath);
        mDetectResultImage.setImageBitmap(drawRect(imgPath, faceList, Color.GREEN, 1));
        dismissDialog();
    }

    @NonNull
    protected void runDetectAsync2() {
        showDiaglog();

        mDetectResultImage2.setImageDrawable(getResources().getDrawable(R.drawable.hillary_clinton));
        final String targetPath = Constants.getFaceShapeModelPath();
        if (!new File(targetPath).exists()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SwapFaceActivity.this, "Copy landmark model to " + targetPath, Toast.LENGTH_SHORT).show();
                }
            });
            FileUtils.copyFileFromRawToOthers(getApplicationContext(), R.raw.shape_predictor_68_face_landmarks, targetPath);
        }
        // Init
        if (mFaceDet == null) {
            mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
        }
        Bitmap bitmap = ((BitmapDrawable) mDetectResultImage2.getDrawable()).getBitmap();


//        Log.d(TAG, "Image path: " + imgPath);
        //face square point
        List<VisionDetRet> faceList = mFaceDet.detect(bitmap);
        //Write file
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg";
        File f = new File(path);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }


        mDetectResultImage2.setImageBitmap(drawRect(path, faceList, Color.GREEN, 2));
        dismissDialog();
    }


    @UiThread
    protected void showDiaglog() {
        mDialog = ProgressDialog.show(SwapFaceActivity.this, "Wait", "Person and face detection", true);
    }

    @UiThread
    protected void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    @DebugLog
    protected Bitmap drawRect(String path, List<VisionDetRet> results, int color, int number) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bm = BitmapFactory.decodeFile(path, options);
        android.graphics.Bitmap.Config bitmapConfig = bm.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bm = bm.copy(bitmapConfig, true);
        int width = bm.getWidth();
        int height = bm.getHeight();
        // By ratio scale
        float aspectRatio = bm.getWidth() / (float) bm.getHeight();

        final int MAX_SIZE = 512;
        int newWidth = MAX_SIZE;
        int newHeight = MAX_SIZE;
        float resizeRatio = 1;
        newHeight = Math.round(newWidth / aspectRatio);
        if (bm.getWidth() > MAX_SIZE && bm.getHeight() > MAX_SIZE) {
            Log.d(TAG, "Resize Bitmap");
            bm = getResizedBitmap(bm, newWidth, newHeight);
            resizeRatio = (float) bm.getWidth() / (float) width;
            Log.d(TAG, "resizeRatio " + resizeRatio);
        }

        // Create canvas to draw
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        // Loop result list
        for (VisionDetRet ret : results) {
            Rect bounds = new Rect();
            bounds.left = (int) (ret.getLeft() * resizeRatio);
            bounds.top = (int) (ret.getTop() * resizeRatio);
            bounds.right = (int) (ret.getRight() * resizeRatio);
            bounds.bottom = (int) (ret.getBottom() * resizeRatio);
            canvas.drawRect(bounds, paint);
            // Get landmark
            if (number == 1) {
                landmarks = ret.getFaceLandmarks();
                for (Point point : landmarks) {
                    int pointX = (int) (point.x * resizeRatio);
                    int pointY = (int) (point.y * resizeRatio);
                    canvas.drawCircle(pointX, pointY, 2, paint);
                    Log.d("LandMark Result: ", pointX + "," + pointY);
                }
            } else if (number == 2) {
                landmarks2 = ret.getFaceLandmarks();
                for (Point point : landmarks2) {
                    int pointX = (int) (point.x * resizeRatio);
                    int pointY = (int) (point.y * resizeRatio);
                    canvas.drawCircle(pointX, pointY, 2, paint);
                    Log.d("LandMark Result 2: ", pointX + "," + pointY);
                }
            }
        }

        if (number == 1)
            bitmapResult = bm;
        else if (number == 2)
            bitmapResult2 = bm;

        return bm;
    }

    @DebugLog
    protected Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        return resizedBitmap;
    }

    private Bitmap JPGtoRGB888(Bitmap img) {
        Bitmap result = null;

        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];

//        get jpeg pixels, each int is the color value of one pixel
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

//        create bitmap in appropriate format
        result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

//        Set RGB pixels
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());

        return result;
    }

    private void faceSwap() {
        //convert Mat to float data type
        //original face sharp
        Bitmap bmp1 = JPGtoRGB888(bitmapResult);
        Mat sourceImage1 = new Mat();
        Utils.bitmapToMat(bmp1, sourceImage1);
        sourceImage1.convertTo(sourceImage1, CvType.CV_32F);
        MatOfPoint points1 = new MatOfPoint();

        Vector<org.opencv.core.Point> temp1 = new Vector<>();
        for (int i = 0;i<landmarks.size();i++) {
            temp1.add(new org.opencv.core.Point(landmarks.get(i).x, landmarks.get(i).y));
            Log.d("temp 1 vs land 1:", temp1.get(i).x + " : " + temp1.get(i).y + " | " + landmarks.get(i).x + " : " + landmarks.get(i).y);
        }
        points1.fromList(temp1);
//        skin color
        Bitmap bmp2 = JPGtoRGB888(bitmapResult2);
        Mat sourceImage2 = new Mat();
        Utils.bitmapToMat(bmp2, sourceImage2);
        sourceImage2.convertTo(sourceImage2, CvType.CV_32F);
        MatOfPoint points2 = new MatOfPoint();
        // Find convex hull
        Vector<org.opencv.core.Point> temp2 = new Vector<>();
        for (int i = 0;i<landmarks2.size();i++) {
            temp2.add(new org.opencv.core.Point(landmarks2.get(i).x, landmarks2.get(i).y));
//            Log.d("temp 2 vs land 2:", temp2.get(i).x + " : " + temp2.get(i).y + " | " + landmarks2.get(i).x + " : " + landmarks2.get(i).y);
        }
        points2.fromList(temp2);

        Vector<org.opencv.core.Point> hull1 = new Vector<>();
        Vector<org.opencv.core.Point> hull2 = new Vector<>();
        MatOfInt hullIndex = new MatOfInt();

        Imgproc.convexHull(points1, hullIndex, false);
        int[] hullList = hullIndex.toArray();
        Canvas canvas = new Canvas(bmp1);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < hullList.length; i++) {
            hull1.add(points1.toList().get(hullIndex.toList().get(i)));
            canvas.drawCircle((float)hull1.get(i).x,(float)hull1.get(i).y, 2, paint);
            Log.d("hull1 value : ",hull1.get(i).x + " : " + hull1.get(i).y );
            hull2.add(points2.toList().get(hullIndex.toList().get(i)));
        }
        mDetectResultImage.setImageBitmap(bmp1);
        // Find delaunay triangulation for points on the convex hull
        Vector<Vector<Integer>> dt = null;
        org.opencv.core.Rect rect = new org.opencv.core.Rect(0, 0, sourceImage2.cols(),  sourceImage2.rows());
//        calculateDelaunayTriangles(rect, hull2, dt);
//        Photo.seamlessClone();

    }
    // Calculate Delaunay triangles for set of points
// Returns the vector of indices of 3 points for each triangle
//    static void calculateDelaunayTriangles(org.opencv.core.Rect rect, Vector<org.opencv.core.Point> points, Vector<Vector<Integer> > delaunayTri){
//
//        // Create an instance of Subdiv2D
//        Subdiv2D subdiv = new Subdiv2D(rect);
//
//        // Insert points into subdiv
//        for(int i = 0;i < points.size();i++)
//        subdiv.insert(points.get(i));
//
//        MatOfFloat6 triangleList = null;
//        subdiv.getTriangleList(triangleList);
//        Vector<MatOfPoint2f> pt = new Vector<>(3);
//        MatOfInt ind = new MatOfInt(3);
//
//        for( int i = 0; i < triangleList.size(); i++ )
//        {
//            MatOfFloat6 t = triangleList[i];
//            pt[0] = new Vector<MatOfPoint2f>(t[0], t[1]);
//            pt[1] = new Vector<MatOfPoint2f>(t[2], t[3]);
//            pt[2] = new Vector<MatOfPoint2f>(t[4], t[5 ]);
//
//            if ( rect.contains(pt[0]) && rect.contains(pt[1]) && rect.contains(pt[2])){
//                for(int j = 0; j < 3; j++)
//                    for(int k = 0; k < points.size(); k++)
//                        if(Math.abs(pt[j].x - points[k].x) < 1.0 && abs(pt[j].y - points[k].y) < 1)
//                            ind[j] = k;
//
//                delaunayTri.push_back(ind);
//            }
//        }
//
//    }
}
