package fyp.hkust.facet.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.swipe.library.Swipe;
import com.github.pwittchen.swipe.library.SwipeEvent;
import com.github.pwittchen.swipe.library.SwipeListener;
import com.melnykov.fab.FloatingActionButton;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;

import fyp.hkust.facet.util.PinchImageView;

import id.zelory.compressor.Compressor;
import me.shaohui.advancedluban.Luban;

/**
 * A simple demo, get a picture form your phone<br />
 * Use the facepp api to detect<br />
 * Find all face on the picture, and mark them out.
 *
 * @author moon5ckq
 */
public class ColorizeFaceActivity extends AppCompatActivity {

    private static final String TAG = ColorizeFaceActivity.class.getSimpleName();

    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private PinchImageView imageView = null;
    private Bitmap basicImg = null;
    private Bitmap originalImg = null;
    private Bitmap temp = null;
    private FloatingActionButton drawbtn;
    private ImageButton show_hide_layout_button;
    private CircleImageView face_button, eye_button, rouge_button, lip_button;
    private boolean checkExpend = true;

    private String face_id = null;
    private List<String> landmark_pt_label = new ArrayList<>();
    private List<Float> landmark_pt_x = new ArrayList<>();
    private List<Float> landmark_pt_y = new ArrayList<>();
    private List<Float> extra_landmark_pt_x = new ArrayList<>();
    private List<Float> extra_landmark_pt_y = new ArrayList<>();
    private int j = 0;
    private int counter = 40;
    //    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.SCREEN;
//    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.SRC_OVER;
    //    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.XOR;
//    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.ADD;
    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.OVERLAY;
    private Xfermode mXfermode = new PorterDuffXfermode(mPorterDuffMode);

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ProgressDialog progressDialog;
    private FloatingActionButton facebtn;
    private Mat mRgbMat;
    private Mat mHsvMat;
    private Mat mMaskMat;
    private int channelCount = 3;
    private Mat mDilatedMat;
    private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

    private Mat hierarchy;
    private int iLineThickness = 5;
    private org.opencv.core.Scalar colorGreen = new Scalar(0, 255, 0, 0);
    private static double mMinContourArea = 0.30;
    private List<MatOfPoint> mMaxContours = new ArrayList<MatOfPoint>();
    private Intent intent;
    private String path;
    private LinearLayout makeup_select_layout;
    private Swipe swipe;
    private Bitmap bitmap;
    private HandlerThread mThread;
    private Handler mThreadHandler;
    private int[] tempArray;
    private int[] bitmapArray;

    private int newWidth;
    private int newHeight;
    FaceDet mFaceDet;

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    mRgbMat = new Mat();
                    mHsvMat = new Mat();
                    mMaskMat = new Mat();
                    mDilatedMat = new Mat();
                    hierarchy = new Mat();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        remove status bar
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_colorize_face);

        verifyStoragePermissions(this);

        //set up photo first
        intent = this.getIntent();
        path = intent.getStringExtra("path");

//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
        float width = 1080;
        float height = 1380;
        Log.d(TAG + "screen ", width + " : " + height);

        imageView = (PinchImageView) this.findViewById(R.id.imageView1);

        final File f = new File(path);
//        originalBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

        Luban.compress(getApplicationContext(), f)
                .putGear(Luban.CUSTOM_GEAR)
                .asObservable()                             // generate Observable
                .subscribe();      // subscribe the compress result

        basicImg = new Compressor.Builder(ColorizeFaceActivity.this)
                .setMaxWidth(width)
                .setMaxHeight(height)
                .setQuality(80)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .build()
                .compressToBitmap(f);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        originalImg = BitmapFactory.decodeFile(path, options);
        android.graphics.Bitmap.Config bitmapConfig = originalImg.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        originalImg = originalImg.copy(bitmapConfig, true);

        temp = originalImg.copy(bitmapConfig, true);
        bitmap = originalImg.copy(bitmapConfig, true);

        makeup_select_layout = (LinearLayout) findViewById(R.id.makeup_select_layout);
        face_button = (CircleImageView) this.findViewById(R.id.face_button);
        eye_button = (CircleImageView) this.findViewById(R.id.eye_button);
        rouge_button = (CircleImageView) this.findViewById(R.id.rouge_button);
        lip_button = (CircleImageView) this.findViewById(R.id.lip_button);

        show_hide_layout_button = (ImageButton) this.findViewById(R.id.show_hide_layout_button);
        drawbtn = (FloatingActionButton) this.findViewById(R.id.drawbtn);
        facebtn = (FloatingActionButton) this.findViewById(R.id.facebtn);

        show_hide_layout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkExpend == true) {
                    makeup_select_layout.animate()
                            .translationY(0)
                            .alpha(0.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    makeup_select_layout.setVisibility(View.GONE);
                                }
                            });

                    show_hide_layout_button.setImageResource(R.mipmap.ic_expand_more_black_24dp);
                    checkExpend = false;
                } else if (checkExpend == false) {
                    makeup_select_layout.animate()
                            .translationY(1)
                            .alpha(1.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    makeup_select_layout.setVisibility(View.VISIBLE);
                                }
                            });

                    show_hide_layout_button.setImageResource(R.mipmap.ic_expand_less_black_24dp);
                    checkExpend = true;
                }

            }
        });

        swipe = new Swipe();
        swipe.addListener(new SwipeListener() {
            @Override
            public void onSwipingLeft(final MotionEvent event) {
            }

            @Override
            public void onSwipedLeft(final MotionEvent event) {
            }

            @Override
            public void onSwipingRight(final MotionEvent event) {
            }

            @Override
            public void onSwipedRight(MotionEvent event) {

            }

            @Override
            public void onSwipingUp(MotionEvent event) {

            }

            @Override
            public void onSwipedUp(final MotionEvent event) {
//                Log.d(TAG, "SWIPED_UP");
                makeup_select_layout.animate()
                        .translationY(1)
                        .alpha(1.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationEnd(animation);
                                makeup_select_layout.setVisibility(View.VISIBLE);
                            }
                        });

                show_hide_layout_button.setImageResource(R.mipmap.ic_expand_less_black_24dp);
                checkExpend = true;

            }

            @Override
            public void onSwipingDown(final MotionEvent event) {
//                Log.d(TAG, "SWIPING_DOWN");
            }

            @Override
            public void onSwipedDown(final MotionEvent event) {
//                Log.d(TAG, "SWIPED_DOWN");
                makeup_select_layout.animate()
                        .translationY(0)
                        .alpha(0.0f)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                makeup_select_layout.setVisibility(View.GONE);
                            }
                        });

                show_hide_layout_button.setImageResource(R.mipmap.ic_expand_more_black_24dp);
                checkExpend = false;
            }
        });


        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        //=====Write down your Finger Pressed code here
                        //clear imageview
                        imageView.setImageBitmap(null);
                        imageView.setImageBitmap(originalImg);
                        Toast.makeText(getApplication(), "long touch image", Toast.LENGTH_SHORT).show();
                        return true;

                    case MotionEvent.ACTION_UP:
                        imageView.setImageBitmap(temp);
                        //=====Write down you code Finger Released code here
                        return true;
                }
                return false;
            }
        });

        progressDialog = new ProgressDialog(ColorizeFaceActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Getting landmark result ...");
        progressDialog.show();

        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(landmarkDetection);

        drawbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                connectLipLine();
                changeLipColor();
                drawEyeShadow();
                drawRouge();
                //mouth_lower_lip_left_contour 1- 3 : 39 - 41
//                drawLocation(temp, drawCanvas);
                Log.d(TAG + " point", j + " ");
                j++;
                imageView.setImageBitmap(temp);
            }
        });

        facebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                detectRegion();
                detectRegion2();
                imageView.setImageBitmap(null);
                imageView.setImageBitmap(temp);
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        swipe.dispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    private Runnable landmarkDetection = new Runnable() {

        public void run() {
            // TODO Auto-generated method stu
            try {
//                Canvas canvas = new Canvas(temp);
//                canvas.drawBitmap(originalImg, new Matrix(), null);

                Canvas canvas = new Canvas(temp);
                canvas.drawBitmap(temp, new Matrix(), null);

                if (mFaceDet == null) {
                    mFaceDet = new FaceDet(Constants.getFaceShapeModelPath());
                }
                List<VisionDetRet> faceList = mFaceDet.detect(path);
                if (faceList.size() > 0) {
                    for (VisionDetRet ret : faceList) {
                        float width = 1080;
                        // By ratio scale
                        float aspectRatio = originalImg.getWidth() / (float) originalImg.getHeight();
                        float resizeRatio = 1;
                        final int MAX_SIZE = 1;
                        newWidth = 1080;
                        newHeight = 1380;
                        newHeight = Math.round(newWidth / aspectRatio);

                        if (originalImg.getWidth() > MAX_SIZE && originalImg.getHeight() > MAX_SIZE) {
                            originalImg = getResizedBitmap(originalImg, newWidth, newHeight);
                            resizeRatio = (float) originalImg.getWidth() / (float) width;
                            Log.d("resizeRatio ", resizeRatio + "");
                        }

                        ArrayList<Point> landmarks = ret.getFaceLandmarks();
                        int count = 0;
                        for (Point point : landmarks) {
                            float pointX = (point.x * resizeRatio);
                            float pointY = (point.y * resizeRatio);
                            if (count == 2 || count == 3 || count == 4 || count == 5 || count == 6) {
                                landmark_pt_x.add(pointX + 6f);
                                landmark_pt_y.add(pointY - 6f);
                                Log.d(TAG, count + " " + pointX + " : " + pointY);
                            } else {
                                landmark_pt_x.add(pointX);
                                landmark_pt_y.add(pointY);
                            }
                            count++;

                            drawpoint(pointX, pointY, temp, canvas);
                        }
                        Log.d(TAG + " added landmark", count + "");

                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "No face", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(originalImg);
                        progressDialog.dismiss();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    };

    protected Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        return resizedBitmap;
    }

    private void detectRegion2() {

        Bitmap bmpTemp = basicImg.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmpTemp, mRgbMat);
        Imgproc.cvtColor(mRgbMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL, channelCount);

//        for (int i = 0; i < extra_landmark_pt_x.size(); i++) {
//            extra_landmark_pt_y.set(i, extra_landmark_pt_y.get(i) - 5);
//            do {
////
//                if (mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[0] > 0 &&
//                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[0] < 45 &&
//                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[1] > 0.23 * 255 &&
//                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[1] < 0.68 * 255 &&
//                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[2] > 0.25 * 255 &&
//                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[2] < 0.8 * 255
//                        ) {
//                    extra_landmark_pt_y.set(i, extra_landmark_pt_y.get(i) - 5);
//                } else
//                    break;
//            } while (true);
//        }

        try {
            // load cascade file from application resources
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            String xmlDataFileName = "lbpcascade_frontalface.xml";
            mCascadeFile = new File(cascadeDir, xmlDataFileName);
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // eyes detect
            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            //must add this line
            mJavaDetector.load(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }

        Mat gray_demo = new Mat();
        Imgproc.cvtColor(mRgbMat, gray_demo, Imgproc.COLOR_RGB2GRAY);

        if (mAbsoluteFaceSize == 0) {
            int height = gray_demo.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();
        if (mJavaDetector != null) {
            mJavaDetector.detectMultiScale(gray_demo, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
            Log.d(TAG, " yo1");
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Log.d(TAG, faces.toString());
        org.opencv.core.Rect[] facesArray = faces.toArray();
        Log.d(TAG + "facesArray[0].height ", facesArray[0].tl().x + " " + facesArray[0].tl().y);
        float extra = (float) facesArray[0].tl().y;
        if (extra - 30.0f < 1.0f) {
            extra = 0.0f;
        } else if (extra - 30.0f > 1.0f)
            extra = extra - 30.0f;
        // left_eyebrow_upper
        extra_landmark_pt_x.add(landmark_pt_x.get(17) + 3f);
        extra_landmark_pt_y.add(extra + 3f);
        extra_landmark_pt_x.add(landmark_pt_x.get(19) + 3f);
        extra_landmark_pt_y.add(extra + 3f);
        extra_landmark_pt_x.add(landmark_pt_x.get(21) + 3f);
        extra_landmark_pt_y.add(extra + 3f);

        extra_landmark_pt_x.add(landmark_pt_x.get(0));
        extra_landmark_pt_y.add(extra);

        //right_eyebrow_upper
        extra_landmark_pt_x.add(landmark_pt_x.get(22) - 3f);
        extra_landmark_pt_y.add(extra + 3f);
        extra_landmark_pt_x.add(landmark_pt_x.get(24) - 3f);
        extra_landmark_pt_y.add(extra + 3f);
        extra_landmark_pt_x.add(landmark_pt_x.get(26) - 3f);
        extra_landmark_pt_y.add(extra + 3f);
        Log.d(TAG + "facesArray[0].height ", facesArray[0].tl().x + " " + extra);

//        //bilateralFilter
//        Utils.bitmapToMat(bitmap, mRgbMat);
//        Imgproc.cvtColor(mRgbMat, mRgbMat, Imgproc.COLOR_BGR2RGB);
//        Mat dstMat = new Mat(mRgbMat.size(), mRgbMat.type());
//        Imgproc.bilateralFilter(mRgbMat, dstMat, 10, 50, 0);
//        Imgproc.cvtColor(dstMat, mRgbMat, Imgproc.COLOR_RGBA2BGR);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, new Matrix(), null);

        Paint paint = new Paint();
//        int baseColor = 0x55D3996B;
//        paint.setColor(baseColor);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
        paint.setColor(Color.BLUE);
        paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        paint.setPathEffect(new CornerPathEffect(80));
//        paint.setARGB(20,253,243,244);
        paint.setStrokeWidth(Math.max(originalImg.getWidth(), originalImg.getHeight()) / 100f);
        paint.setAntiAlias(true);
        paint.setDither(true);

        //设置混合模式

        int sc = canvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        Path path = new Path();
// face area
        path.reset();
        path.moveTo(landmark_pt_x.get(0), landmark_pt_y.get(0));

        for (int i = 1; i < 16; i++) {
            path.lineTo(landmark_pt_x.get(i), landmark_pt_y.get(i));
        }

        for (int i = 6; i > 0; i--)
            path.lineTo(extra_landmark_pt_x.get(i), extra_landmark_pt_y.get(i));
        Log.d(TAG, extra_landmark_pt_x.get(0) + " : " + extra_landmark_pt_y.get(0));

        path.lineTo(landmark_pt_x.get(0), landmark_pt_y.get(0));

        path.close();
        canvas.drawPath(path, paint);

        //change lip part to green
        paint.setColor(Color.GREEN);
        path.reset();
        path.moveTo(landmark_pt_x.get(48), landmark_pt_y.get(48));

        for (int i = 49; i < 55; i++)
            path.lineTo(landmark_pt_x.get(i), landmark_pt_y.get(i));

        path.lineTo(landmark_pt_x.get(64), landmark_pt_y.get(64));
        path.lineTo(landmark_pt_x.get(63), landmark_pt_y.get(63));
        path.lineTo(landmark_pt_x.get(62), landmark_pt_y.get(62));
        path.lineTo(landmark_pt_x.get(60), landmark_pt_y.get(60));
        path.lineTo(landmark_pt_x.get(48), landmark_pt_y.get(48));

        path.close();
        canvas.drawPath(path, paint);

        path.reset();
        path.moveTo(landmark_pt_x.get(48), landmark_pt_y.get(48));
        path.lineTo(landmark_pt_x.get(59), landmark_pt_y.get(59));
        path.lineTo(landmark_pt_x.get(58), landmark_pt_y.get(58));
        path.lineTo(landmark_pt_x.get(57), landmark_pt_y.get(57));
        path.lineTo(landmark_pt_x.get(56), landmark_pt_y.get(56));
        path.lineTo(landmark_pt_x.get(55), landmark_pt_y.get(55));
        path.lineTo(landmark_pt_x.get(54), landmark_pt_y.get(54));

        for (int i = 64; i < 68; i++)
            path.lineTo(landmark_pt_x.get(i), landmark_pt_y.get(i));

        path.lineTo(landmark_pt_x.get(60), landmark_pt_y.get(60));
        path.lineTo(landmark_pt_x.get(48), landmark_pt_y.get(48));

        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(50f);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        //left_eyebrow
//        path.reset();
//        path.moveTo(landmark_pt_x.get(33), landmark_pt_y.get(33));
//        path.quadTo(
//                landmark_pt_x.get(32), landmark_pt_y.get(32),
//                landmark_pt_x.get(31), landmark_pt_y.get(31));
//        path.quadTo(
//                landmark_pt_x.get(30), landmark_pt_y.get(30),
//                landmark_pt_x.get(29), landmark_pt_y.get(29));
//        path.quadTo(
//                landmark_pt_x.get(34), landmark_pt_y.get(34),
//                landmark_pt_x.get(35), landmark_pt_y.get(35));
//        path.quadTo(
//                landmark_pt_x.get(36), landmark_pt_y.get(36),
//                landmark_pt_x.get(33), landmark_pt_y.get(33));
//        path.close();
//        canvas.drawPath(path, paint);
//
//        //right_eyebrow
//        path.reset();
//        path.moveTo(landmark_pt_x.get(75), landmark_pt_y.get(75));
//        path.quadTo(
//                landmark_pt_x.get(76), landmark_pt_y.get(76),
//                landmark_pt_x.get(77), landmark_pt_y.get(77));
//        path.quadTo(
//                landmark_pt_x.get(78), landmark_pt_y.get(78),
//                landmark_pt_x.get(79), landmark_pt_y.get(79));
//        path.quadTo(
//                landmark_pt_x.get(82), landmark_pt_y.get(82),
//                landmark_pt_x.get(81), landmark_pt_y.get(81));
//        path.quadTo(
//                landmark_pt_x.get(80), landmark_pt_y.get(80),
//                landmark_pt_x.get(75), landmark_pt_y.get(75));
//        path.close();
//        canvas.drawPath(path, paint);

        // left eye
        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(36), landmark_pt_y.get(36));
        path.cubicTo(
                //left_eye_lower_left_quarter
                landmark_pt_x.get(37), landmark_pt_y.get(37),
                //left_eye_bottom
                landmark_pt_x.get(38), landmark_pt_y.get(38),
                landmark_pt_x.get(39), landmark_pt_y.get(39));
        path.cubicTo(
                //left_eye_lower_right_quarter
                landmark_pt_x.get(40), landmark_pt_y.get(40),
                //left_eye_right_corner
                landmark_pt_x.get(41), landmark_pt_y.get(41),
                landmark_pt_x.get(36), landmark_pt_y.get(36));
        path.close();
        canvas.drawPath(path, paint);

        // right eye
        path.reset();
        //right_eye_left_corner
        path.moveTo(landmark_pt_x.get(42), landmark_pt_y.get(42));
        path.cubicTo(
                //left_eye_lower_left_quarter
                landmark_pt_x.get(43), landmark_pt_y.get(43),
                //left_eye_bottom
                landmark_pt_x.get(44), landmark_pt_y.get(44),
                landmark_pt_x.get(45), landmark_pt_y.get(45));
        path.cubicTo(
                //left_eye_lower_right_quarter
                landmark_pt_x.get(46), landmark_pt_y.get(46),
                //left_eye_right_corner
                landmark_pt_x.get(47), landmark_pt_y.get(47),
                landmark_pt_x.get(42), landmark_pt_y.get(42));
        path.close();
        canvas.drawPath(path, paint);

        //src
        // 再绘制src源图
//        drawCanvas.drawBitmap(originalImg, 0, 0, mPaint);
        //清除混合模式
        paint.setXfermode(null);
        //还原画布
        canvas.restoreToCount(sc);


        bitmapArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        tempArray = new int[temp.getWidth() * temp.getHeight()];

        bitmap.getPixels(bitmapArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        temp.getPixels(tempArray, 0, temp.getWidth(), 0, 0, temp.getWidth(), temp.getHeight());

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (bitmapArray[x + y * bitmap.getWidth()] == Color.BLUE) {
                    float[] hsv = new float[3];
                    Color.RGBToHSV(Color.red(tempArray[x + y * bitmap.getWidth()]), Color.green(tempArray[x + y * bitmap.getWidth()]), Color.blue(tempArray[x + y * bitmap.getWidth()]), hsv);
                    hsv[0] = 26;
                    tempArray[x + y * bitmap.getWidth()] = Color.HSVToColor(hsv);
                }
            }
        }

        temp.setPixels(tempArray, 0, temp.getWidth(), 0, 0, temp.getWidth(), temp.getHeight());

        Log.d("color", mHsvMat.get(Math.round(extra_landmark_pt_x.get(0)), Math.round(extra_landmark_pt_y.get(0)))[0] + " " +
                mHsvMat.get(Math.round(extra_landmark_pt_x.get(0)), Math.round(extra_landmark_pt_y.get(0)))[1] + " " +
                mHsvMat.get(Math.round(extra_landmark_pt_x.get(0)), Math.round(extra_landmark_pt_y.get(0)))[2]);

// HSV back to BGR
        Imgproc.cvtColor(mHsvMat, mRgbMat, Imgproc.COLOR_HSV2BGR, channelCount);

    }

    private void detectRegion() {

        //get the image from gallery and change it into bitmap
        Bitmap bmpTemp = originalImg.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmpTemp, mRgbMat);
        Imgproc.cvtColor(mRgbMat, mHsvMat, Imgproc.COLOR_RGB2HSV, channelCount);
//        http://matmidia.org/sibgrapi2009/media/posters/59928.pdf
//        The skin in channel H is characterized by values between 0 and 50,
//        in the channel S from 0.23 to 0.68 for Asian and Caucasian ethnics.
        Scalar lowerThreshold = new Scalar(0, 0.20 * 255, 70);
        Scalar upperThreshold = new Scalar(30, 0.80 * 255, 200);
        Core.inRange(mHsvMat, lowerThreshold, upperThreshold, mMaskMat);
//        Imgproc.dilate(mMaskMat, mDilatedMat, new Mat());

        Imgproc.findContours(mMaskMat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find max contour area
        double maxArea = 0;
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            if (area > maxArea)
                maxArea = area;
        }

        // Filter contours by area and resize to fit the original image size
        mMaxContours.clear();
        each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint contour = each.next();
            if (Imgproc.contourArea(contour) > mMinContourArea * maxArea) {
//                Core.multiply(contour, new Scalar(4,4), contour);
                mMaxContours.add(contour);
            }
        }

        Imgproc.drawContours(mRgbMat, mMaxContours, 0, colorGreen, 0);
        Log.d(TAG + " contours", contours.size() + "");
        Log.d(TAG + " contours", mMaxContours.size() + "");
        Log.d(TAG + " contours row col", mMaxContours.get(0).toList().toString());
        Log.d(TAG + " contours ", mMaxContours.get(0).toString());
        counter++;

//        Imgproc.drawContours(mRgbMat, contours,counter, colorGreen);

//        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
//            if (contours.size() > 100)  // Minimum size allowed for consideration
//            {
//                Imgproc.drawContours(mRgbMat, contours, contourIdx, colorGreen, iLineThickness);
//                Log.d(TAG + " contours " , contours.get(contourIdx).toString());
//            }
//        }

        // convert to bitmap:
        Bitmap bm = Bitmap.createBitmap(mRgbMat.cols(), mRgbMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mMaskMat, bm);

        // find the imageview and draw it!
        imageView.setImageBitmap(bm);
    }


    public void drawpoint(float x, float y, Bitmap bitmap, Canvas canvas) {

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(Math.max(originalImg.getWidth(), originalImg.getHeight()) / 100f);
        paint.setAntiAlias(true);
        paint.setDither(true);
        canvas.drawCircle(x, y, 2, paint);
//        canvas.drawText(j+"",x, y, paint);
    }

    public void drawLocation(Bitmap bitmap, Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeWidth(Math.max(originalImg.getWidth(), originalImg.getHeight()) / 100f);
        canvas.drawCircle(landmark_pt_x.get(j), landmark_pt_y.get(j), 2, paint);
    }


    public void changeLipColor() {
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (bitmapArray[x + y * bitmap.getWidth()] == Color.GREEN) {
                    float[] hsv = new float[3];
                    Color.RGBToHSV(Color.red(tempArray[x + y * bitmap.getWidth()]), Color.green(tempArray[x + y * bitmap.getWidth()]), Color.blue(tempArray[x + y * bitmap.getWidth()]), hsv);
                    hsv[0] = 350;
                    tempArray[x + y * bitmap.getWidth()] = Color.HSVToColor(hsv);
                }
            }
        }
        temp.setPixels(tempArray, 0, temp.getWidth(), 0, 0, temp.getWidth(), temp.getHeight());

    }


    public void connectLipLine() {

        Canvas drawCanvas = new Canvas(temp);
//
//        drawCanvas.drawColor(Color.WHITE);
        Paint mPaint = new Paint();

        int lipColor = 0x5CFA0005;
        mPaint.setColor(lipColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(0.01f);
//        drawCanvas.drawColor(Color.WHITE);
        int sc = drawCanvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);
//        canvas.drawCircle(landmark_pt_x.get(start + 1), landmark_pt_y.get(start + 1), 2, mPaint);
//        canvas.drawCircle(landmark_pt.get(start+2)[0], landmark_pt.get(start+2)[1], 2, mPaint);

//        mPaint.setColorFilter(new PorterDuffColorFilter(lipColor, PorterDuff.Mode.OVERLAY));

        //dst
        Path path = new Path();

        // Left lower lips
        path.reset();
        path.moveTo(landmark_pt_x.get(37), landmark_pt_y.get(37));

        path.cubicTo(landmark_pt_x.get(40), landmark_pt_y.get(40),
                landmark_pt_x.get(41), landmark_pt_y.get(41),
                // bottom lip point
                landmark_pt_x.get(38), landmark_pt_y.get(38));

        // bottom right lip point
        path.cubicTo(
                // bottom lip point
                landmark_pt_x.get(43), landmark_pt_y.get(43),
                landmark_pt_x.get(44), landmark_pt_y.get(44),
                landmark_pt_x.get(46), landmark_pt_y.get(46)
        );

        //inside lower
        //start from right to middle
        path.quadTo(
                landmark_pt_x.get(42), landmark_pt_y.get(42),
                landmark_pt_x.get(45), landmark_pt_y.get(45));

        //inside lower
        //start from middle to left
        path.quadTo(
                landmark_pt_x.get(39), landmark_pt_y.get(39),
                landmark_pt_x.get(37), landmark_pt_y.get(37));

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        // top left lip point
        path.moveTo(landmark_pt_x.get(37), landmark_pt_y.get(37));

        path.cubicTo(landmark_pt_x.get(49), landmark_pt_y.get(49),
                landmark_pt_x.get(48), landmark_pt_y.get(48),
                //top middle point
                landmark_pt_x.get(54), landmark_pt_y.get(54));

        //top right lip point
        path.cubicTo(landmark_pt_x.get(51), landmark_pt_y.get(51),
                landmark_pt_x.get(52), landmark_pt_y.get(52),
                landmark_pt_x.get(46), landmark_pt_y.get(46));

        //inside upper
        //start from right to middle
        path.quadTo(
                landmark_pt_x.get(53), landmark_pt_y.get(53),
                landmark_pt_x.get(47), landmark_pt_y.get(47));

        //inside upper
        //start from middle to left
        path.quadTo(
                landmark_pt_x.get(50), landmark_pt_y.get(50),
                landmark_pt_x.get(37), landmark_pt_y.get(37));

        path.close();
        drawCanvas.drawPath(path, mPaint);

        //设置混合模式
        mPaint.setXfermode(mXfermode);

        //src
        // 再绘制src源图
//        drawCanvas.drawBitmap(originalImg, 0, 0, mPaint);

        //清除混合模式
        mPaint.setXfermode(null);
        //还原画布
        drawCanvas.restoreToCount(sc);

    }

    private void drawEyeShadow() {
        Canvas drawCanvas = new Canvas(temp);
//        drawCanvas.drawColor(Color.WHITE);
        Paint mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(50f);
//        drawCanvas.drawColor(Color.WHITE);
        int color1 = 0xDD6b4491;
        int color2 = 0xDD34ac32;
//        int sc = drawCanvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        mPaint.setShader(
                new LinearGradient(landmark_pt_x.get(36) - 25f, landmark_pt_y.get(36) - 20f,
                        landmark_pt_x.get(39) + 20f, landmark_pt_y.get(39) - 8f,
                        color1, color2,
                        Shader.TileMode.CLAMP));

//       mPaint.setShader(new RadialGradient(
//               landmark_pt_x.get(21) + (landmark_pt_x.get(25) - landmark_pt_x.get(21) / 2), landmark_pt_y.get(21), (landmark_pt_x.get(25) - landmark_pt_x.get(21) / 2),
//                color1, color2,
//                Shader.TileMode.CLAMP));
        mPaint.setMaskFilter(new BlurMaskFilter(7, BlurMaskFilter.Blur.NORMAL));
        Path path = new Path();
        // left eye
        path.reset();
//        Float left_middle_eye_eyebrow_y = (landmark_pt_y.get(21) - landmark_pt_y.get(31) )/2;
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(36) - 25f, landmark_pt_y.get(36) - 4f);

        path.cubicTo(
                //left_eye_top_left + a distance
                landmark_pt_x.get(37), landmark_pt_y.get(37) - 20f,
                //left_eye_top_right + a distance
                landmark_pt_x.get(38), landmark_pt_y.get(38) - 20f,
                //left_eye_right_corner
                landmark_pt_x.get(39) + 10f, landmark_pt_y.get(39) + 5f);

        path.cubicTo(
                //left_eye_top_left
                landmark_pt_x.get(38), landmark_pt_y.get(38) - 11f,
                //left_eye_top_right
                landmark_pt_x.get(37), landmark_pt_y.get(37) - 9f,
                //left_eye_left_corner
                landmark_pt_x.get(36) - 5f, landmark_pt_y.get(36) - 8f);

        path.lineTo(landmark_pt_x.get(36) - 25f, landmark_pt_y.get(36) - 4f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // right eye
        mPaint.setShader(
                new LinearGradient(landmark_pt_x.get(45) + 25f, landmark_pt_y.get(45) - 20f,
                        landmark_pt_x.get(42) - 20f, landmark_pt_y.get(42) - 4f,
                        color1, color2,
                        Shader.TileMode.CLAMP));

        path.reset();
        //right_eye_right_corner
        path.moveTo(landmark_pt_x.get(45) + 25f, landmark_pt_y.get(45) - 4f);

        path.cubicTo(
                //right_eye_top_right + a distance
                landmark_pt_x.get(44), landmark_pt_y.get(44) - 20f,
                //right_eye_top_left + a distance
                landmark_pt_x.get(43), landmark_pt_y.get(43) - 20f,
                //right_eye_left_corner
                landmark_pt_x.get(42) - 10f, landmark_pt_y.get(42) - 5f);

        path.cubicTo(
                //right_eye_top_left
                landmark_pt_x.get(43), landmark_pt_y.get(43) - 11f,
                //right_eye_top_right
                landmark_pt_x.get(44), landmark_pt_y.get(44) - 9f,
                //right_eye_right_corner
                landmark_pt_x.get(45) + 5f, landmark_pt_y.get(45) - 8f);

        path.lineTo(landmark_pt_x.get(45) + 25f, landmark_pt_y.get(45) - 4f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        //设置混合模式
        mPaint.setXfermode(mXfermode);

        //src
        // 再绘制src源图
//        drawCanvas.drawBitmap(originalImg, 0, 0, mPaint);

        //清除混合模式
        mPaint.setXfermode(null);
        //还原画布
//        drawCanvas.restoreToCount(sc);
    }

    private int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    private void drawRouge() {

        Canvas drawCanvas = new Canvas(temp);
        Paint mPaint = new Paint();

        int rougeColor = 0x10FA0005;
        mPaint.setColor(rougeColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mPaint.setPathEffect(new CornerPathEffect(50));
        mPaint.setStrokeWidth(1f);
        int sc = drawCanvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        mPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));

        //nose_contour_left2 - contour_left3
        float rouge_left_x = landmark_pt_x.get(30) - (landmark_pt_x.get(30) - landmark_pt_x.get(2)) / 2f;
        float rouge_left_y = landmark_pt_y.get(31);
        float slope = ((landmark_pt_y.get(31) - landmark_pt_y.get(35)) / (landmark_pt_x.get(31) - landmark_pt_x.get(35)));
        rouge_left_y = ((rouge_left_x - landmark_pt_x.get(31)) * slope) + landmark_pt_y.get(31);
//        drawCanvas.drawCircle(rouge_left_x, rouge_left_y, 30f, mPaint);
//        nose_contour_right2 - contour_right3
        float rouge_right_x = landmark_pt_x.get(30) + (landmark_pt_x.get(14) - landmark_pt_x.get(30)) / 2f;
        float rouge_right_y = landmark_pt_y.get(35);
        rouge_right_y = ((rouge_right_x - landmark_pt_x.get(35)) * slope) + landmark_pt_y.get(35);

        Path path = new Path();
        path.reset();
        //draw left rouge
//        contour_left3
        path.moveTo(landmark_pt_x.get(2) + 3f, landmark_pt_y.get(2));
        path.cubicTo(landmark_pt_x.get(3) + 3f, landmark_pt_y.get(3),
                landmark_pt_x.get(4) + 3f, landmark_pt_y.get(4),
                landmark_pt_x.get(5) + 3f, landmark_pt_y.get(5) - 4f
        );
        path.lineTo(rouge_left_x, rouge_left_y);
        path.lineTo(landmark_pt_x.get(2) + 3f, landmark_pt_y.get(2));

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        //draw right rouge
//        contour_right3
        path.moveTo(landmark_pt_x.get(14) - 3f, landmark_pt_y.get(14));
        path.cubicTo(landmark_pt_x.get(13) - 3f, landmark_pt_y.get(13),
                landmark_pt_x.get(12) - 3f, landmark_pt_y.get(12),
                landmark_pt_x.get(11) - 3f, landmark_pt_y.get(11) - 4f
        );

        path.lineTo(rouge_right_x, rouge_right_y);
        path.lineTo(landmark_pt_x.get(14) - 3f, landmark_pt_y.get(14));

        drawCanvas.drawPath(path, mPaint);

//        Log.d(TAG + "" , landmark_pt_y.get(56) + " " + landmark_pt_x.get(56) +  " : " +landmark_pt_y.get(60) + " " +  landmark_pt_x.get(60));
//        Log.d(TAG + " rouge " , rouge_left_x + " " + rouge_left_y + " : " + rouge_right_x + " " + rouge_right_y);
//        drawCanvas.drawCircle(rouge_right_x, rouge_right_y, 30f, mPaint);

        //设置混合模式
        mPaint.setXfermode(mXfermode);
        //清除混合模式
        mPaint.setXfermode(null);
        //还原画布
        drawCanvas.restoreToCount(sc);
        drawCanvas.setBitmap(temp);
    }


    public void getFaceid(JSONObject result) {
        try {
            face_id = result.getJSONArray("face").getJSONObject(0).getString("face_id");
        } catch (JSONException e) {
            e.printStackTrace();
            ColorizeFaceActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.d(TAG + " error", " Error.");
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}