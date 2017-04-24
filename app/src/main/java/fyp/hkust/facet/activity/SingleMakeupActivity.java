package fyp.hkust.facet.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.util.FontManager;
import fyp.hkust.facet.util.PinchImageView;
import fyp.hkust.facet.util.TypefaceSpan;
import id.zelory.compressor.Compressor;
import me.shaohui.advancedluban.Luban;

public class SingleMakeupActivity extends AppCompatActivity {

    private static final String TAG = SingleMakeupActivity.class.getSimpleName();

    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private Bitmap basicImg = null;
    private PinchImageView imageView = null;
    private Bitmap originalImg = null;
    private Bitmap temp = null, temp2 = null, bitmap = null;

    private List<Float> landmark_pt_x = new ArrayList<>();
    private List<Float> landmark_pt_y = new ArrayList<>();
    private List<Float> extra_landmark_pt_x = new ArrayList<>();
    private List<Float> extra_landmark_pt_y = new ArrayList<>();

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private ProgressDialog progressDialog;
    private Mat mRgbMat;
    private Mat mHsvMat;
    private Mat mMaskMat;
    private int channelCount = 3;
    private Mat mDilatedMat;
    private Mat hierarchy;

    private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

    private HandlerThread mThread;
    private Handler mThreadHandler;
    private int[] tempArray;
    private int[] bitmapArray;
    private int[] originalArray;

    private int newWidth;
    private int newHeight;
    FaceDet mFaceDet;

    private ImageButton eyeshadow_method1, eyeshadow_method2, eyeshadow_method3, eyeshadow_method4;
    private LinearLayout eyeshadow_method_layout;

    private PorterDuff.Mode mPorterDuffMode = PorterDuff.Mode.OVERLAY;
    private Xfermode mXfermode = new PorterDuffXfermode(mPorterDuffMode);

    private Button compare_button;

    private String makeupType = "";
    private int categoryResult = 1;
    private int methodNumber = 1;
    private List<String> colorArray = new ArrayList<>();
    private boolean doubleBackToExitPressedOnce = false;
    private Intent intent;
    private String path;
    private LinearLayout rouge_alpha_select;
    private SeekBar alpha_seekBar;
    private int alphaValueRouge = 10;
    private ArrayList<String> savedEyeshadowColor = new ArrayList<>();
    private ArrayList<ArrayList<String>> data = new ArrayList<>();
    private RecyclerView makeup_color_list;
    private RelativeLayout single_makeup_layout;

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


    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        //rotate if the phone come from taking photo
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_makeup);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set up photo first
        intent = this.getIntent();
        path = intent.getStringExtra("path");
        makeupType = intent.getStringExtra("makeupType");

        SpannableString s = new SpannableString(makeupType);
        s.setSpan(new TypefaceSpan(SingleMakeupActivity.this, FontManager.CUSTOM_FONT), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);

//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float width = size.x;
        float height = size.y;
        Log.d(TAG + "screen ", width + " : " + height);

        imageView = (PinchImageView) this.findViewById(R.id.imageView1);
        android.view.ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = (int) (width * 0.7);
        layoutParams.height = (int) (height * 0.7);
        imageView.setLayoutParams(layoutParams);

        final File f = new File(path);
//        originalBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

        Luban.compress(getApplicationContext(), f)
                .putGear(Luban.CUSTOM_GEAR)
                .asObservable()                             // generate Observable
                .subscribe();      // subscribe the compress result

        basicImg = new Compressor.Builder(SingleMakeupActivity.this)
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

        basicImg = originalImg.copy(bitmapConfig, true);
        temp = originalImg.copy(bitmapConfig, true);
        bitmap = originalImg.copy(bitmapConfig, true);

        setup();

        compare_button = (Button) findViewById(R.id.compare_button);
        compare_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        //=====Write down your Finger Pressed code here
                        //clear imageview
                        imageView.setImageBitmap(null);
                        imageView.setImageBitmap(basicImg);
//                        Toast.makeText(getApplication(), "long touch image", Toast.LENGTH_SHORT).show();
                        return true;

                    case MotionEvent.ACTION_UP:
                        imageView.setImageBitmap(temp);
                        //=====Write down you code Finger Released code here
                        return true;
                }
                return false;
            }
        });

        progressDialog = new ProgressDialog(SingleMakeupActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please Wait..");
        progressDialog.setMessage("Getting landmark result ...");
        progressDialog.show();

        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(landmarkDetection);

        if (makeupType.equals("Foundation")) {
            categoryResult = 1;
            colorArray = getIntent().getStringArrayListExtra("colorArray");
            makeup_color_list.setAdapter(new ColorRecyclerAdapter());
            Log.d(TAG + "  colorArray size", colorArray.size() + " " + colorArray.toString());

        } else if (makeupType.equals("Brush")) {
            categoryResult = 2;
            colorArray = getIntent().getStringArrayListExtra("colorArray");
            makeup_color_list.setAdapter(new ColorRecyclerAdapter());
            Log.d(TAG + "  colorArray size", colorArray.size() + " " + colorArray.toString());
        } else if (makeupType.equals("Eyeshadows")) {
            categoryResult = 3;
            data = (ArrayList<ArrayList<String>>) getIntent().getSerializableExtra("colorArrayArray");
            Log.d(TAG + " data ", data.toString());
            makeup_color_list.setAdapter(new MultipleColorAdapter(SingleMakeupActivity.this, data));

            if (data != null && data.get(0).size() == 1) {
                eyeshadow_method_layout.setBackgroundColor(Color.WHITE);
                eyeshadow_method1.setBackgroundResource(R.drawable.eyeshadow1c1);
                eyeshadow_method2.setBackgroundResource(R.drawable.eyeshadow1c2);
                eyeshadow_method3.setBackgroundResource(R.drawable.eyeshadow1c3);
                eyeshadow_method4.setBackgroundResource(R.drawable.eyeshadow1c4);
            } else if (data != null && data.get(0).size() == 2) {
                eyeshadow_method_layout.setBackgroundColor(Color.WHITE);
                eyeshadow_method1.setBackgroundResource(R.drawable.eyeshadow2c1);
                eyeshadow_method2.setBackgroundResource(R.drawable.eyeshadow2c2);
                eyeshadow_method3.setBackgroundResource(R.drawable.eyeshadow2c3);
                eyeshadow_method4.setBackgroundResource(R.drawable.eyeshadow2c4);
            } else if (data != null && data.get(0).size() == 3) {
                eyeshadow_method_layout.setBackgroundColor(Color.WHITE);
                eyeshadow_method1.setBackgroundResource(R.drawable.eyeshadow3c1);
                eyeshadow_method2.setBackgroundResource(R.drawable.eyeshadow3c2);
                eyeshadow_method3.setBackgroundResource(R.drawable.eyeshadow3c3);
                eyeshadow_method4.setBackgroundResource(R.drawable.eyeshadow3c4);
            } else if (data != null && data.get(0).size() == 4) {
                eyeshadow_method_layout.setBackgroundColor(Color.WHITE);
                eyeshadow_method1.setBackgroundResource(R.drawable.eyeshadow1s);
                eyeshadow_method2.setBackgroundResource(R.drawable.eyeshadow2s);
                eyeshadow_method3.setBackgroundResource(R.drawable.eyeshadow3s);
                eyeshadow_method4.setBackgroundResource(R.drawable.eyeshadow4s);
            }
        } else if (makeupType.equals("Lipsticks")) {
            categoryResult = 4;
            colorArray = getIntent().getStringArrayListExtra("colorArray");
            makeup_color_list.setAdapter(new ColorRecyclerAdapter());
            Log.d(TAG + "  colorArray size", colorArray.size() + " " + colorArray.toString());
        }
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

                        final ArrayList<Point> landmarks = ret.getFaceLandmarks();
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
                            Log.d(TAG, count + " " + pointX + " : " + pointY);
                            count++;
//                            drawpoint(pointX, pointY, temp, canvas);
                        }
                        Log.d(TAG + " added landmark", count + "");
                        detectRegion();
                        originalImg = temp.copy(temp.getConfig(), true);
                        temp2 = temp.copy(temp.getConfig(), true);
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
                        imageView.setImageBitmap(temp);
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
        bm.recycle();
        return resizedBitmap;
    }

    private void detectRegion() {
        Bitmap bmpTemp = basicImg.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmpTemp, mRgbMat);
        Imgproc.cvtColor(mRgbMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL, channelCount);

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
        paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        paint.setPathEffect(new CornerPathEffect(70));
        path.reset();

        path.moveTo(landmark_pt_x.get(48), landmark_pt_y.get(48));
        path.lineTo(landmark_pt_x.get(49), landmark_pt_y.get(49));
        path.lineTo(landmark_pt_x.get(50), landmark_pt_y.get(50));
        path.lineTo(landmark_pt_x.get(51), landmark_pt_y.get(51));
        path.lineTo(landmark_pt_x.get(52), landmark_pt_y.get(52));
        path.lineTo(landmark_pt_x.get(53), landmark_pt_y.get(53));
        path.lineTo(landmark_pt_x.get(54), landmark_pt_y.get(54));

        path.lineTo(landmark_pt_x.get(64), landmark_pt_y.get(64));
        path.cubicTo(
                landmark_pt_x.get(63), landmark_pt_y.get(63),
                landmark_pt_x.get(62), landmark_pt_y.get(62),
                landmark_pt_x.get(61), landmark_pt_y.get(61)
        );
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

        path.lineTo(landmark_pt_x.get(64), landmark_pt_y.get(64));

        path.cubicTo(
                landmark_pt_x.get(65), landmark_pt_y.get(65),
                landmark_pt_x.get(66), landmark_pt_y.get(66),
                landmark_pt_x.get(67), landmark_pt_y.get(67)
        );

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
        originalArray = new int[temp.getWidth() * temp.getHeight()];
        tempArray = new int[temp.getWidth() * temp.getHeight()];

        temp.getPixels(originalArray, 0, temp.getWidth(), 0, 0, temp.getWidth(), temp.getHeight());
        bitmap.getPixels(bitmapArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        temp.getPixels(tempArray, 0, temp.getWidth(), 0, 0, temp.getWidth(), temp.getHeight());
    }

    private void setup() {
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.single_makeup_layout), fontType);
        single_makeup_layout = (RelativeLayout) findViewById(R.id.single_makeup_layout);
        //toolbar button

        makeup_color_list = (RecyclerView) findViewById(R.id.makeup_color_list);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        makeup_color_list.setLayoutManager(llm);
        makeup_color_list.setItemAnimator(new DefaultItemAnimator());

        eyeshadow_method_layout = (LinearLayout) findViewById(R.id.eyeshadow_method_layout);
        rouge_alpha_select = (LinearLayout) findViewById(R.id.rouge_alpha_select);
        alpha_seekBar = (SeekBar) findViewById(R.id.alpha_seekBar);

        eyeshadow_method1 = (ImageButton) findViewById(R.id.eyeshadow_method1);
        eyeshadow_method1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodNumber = 1;
                new LoadingMakeupAsyncTask().execute();
            }
        });
        eyeshadow_method2 = (ImageButton) findViewById(R.id.eyeshadow_method2);
        eyeshadow_method2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodNumber = 2;
                new LoadingMakeupAsyncTask().execute();
            }
        });
        eyeshadow_method3 = (ImageButton) findViewById(R.id.eyeshadow_method3);
        eyeshadow_method3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodNumber = 3;
                new LoadingMakeupAsyncTask().execute();
            }
        });
        eyeshadow_method4 = (ImageButton) findViewById(R.id.eyeshadow_method4);
        eyeshadow_method4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodNumber = 4;
                new LoadingMakeupAsyncTask().execute();
            }
        });
    }

    private void setupFoundation(String foundationColor, Bitmap getBitmap) {

        try {
            int color = stringColorRGBToARGB(foundationColor, 150, 0, 0, 0);
            float[] foundationHSV = new float[3];
            Color.colorToHSV(color, foundationHSV);

            for (int x = 0; x < bitmap.getWidth(); x++) {
                for (int y = 0; y < bitmap.getHeight(); y++) {
                    if (bitmapArray[x + y * bitmap.getWidth()] == Color.BLUE) {
                        float[] hsv = new float[3];
                        Color.RGBToHSV(Color.red(originalArray[x + y * bitmap.getWidth()]), Color.green(originalArray[x + y * bitmap.getWidth()]), Color.blue(originalArray[x + y * bitmap.getWidth()]), hsv);
//                        Log.d(TAG, "face[" + x + " , " + y + "] : " + hsv[0] + " , " + hsv[1] + " , " + hsv[2] + " ] " + foundationHSV[0] + " " + foundationHSV[1] + " " + foundationHSV[2]);
                        if (foundationHSV[0] > 28f) {
                            hsv[0] = (hsv[0] + foundationHSV[0]) / 2.2f;
                        } else
                            hsv[0] = foundationHSV[0];

                        if (hsv[1] > foundationHSV[1]) {
                            hsv[1] = foundationHSV[1];
                            Log.d(TAG, "face[" + x + " , " + y + "] : " + hsv[0] + " , " + hsv[1] + " , " + hsv[2] + " ] " + foundationHSV[0] + " " + foundationHSV[1] + " " + foundationHSV[2]);
                        }

//                        hsv[1] = hsv[1] + Math.abs(foundationHSV[1] - hsv[1]);
//                        Log.d(TAG + " hsv[0] = foundationHSV[0] ", hsv[0] + " " + foundationHSV[0]);
                        tempArray[x + y * bitmap.getWidth()] = Color.HSVToColor(hsv);
                    }
                }
            }
            getBitmap.setPixels(tempArray, 0, getBitmap.getWidth(), 0, 0, getBitmap.getWidth(), getBitmap.getHeight());
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

//        Log.d("color", mHsvMat.get(Math.round(extra_landmark_pt_x.get(0)), Math.round(extra_landmark_pt_y.get(0)))[0] + " " +
//                mHsvMat.get(Math.round(extra_landmark_pt_x.get(0)), Math.round(extra_landmark_pt_y.get(0)))[1] + " " +
//                mHsvMat.get(Math.round(extra_landmark_pt_x.get(0)), Math.round(extra_landmark_pt_y.get(0)))[2]);

// HSV back to BGR
//        Imgproc.cvtColor(mHsvMat, mRgbMat, Imgproc.COLOR_HSV2BGR, channelCount);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public void changeLipColor(String lipstickColor, Bitmap getBitmap) {

        int color = stringColorRGBToARGB(lipstickColor, 255, 0, 0, 0);
        float[] lipstickHSV = new float[3];
        Color.colorToHSV(color, lipstickHSV);

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (bitmapArray[x + y * bitmap.getWidth()] == Color.GREEN) {
                    float[] hsv = new float[3];
                    Color.RGBToHSV(Color.red(tempArray[x + y * bitmap.getWidth()]), Color.green(tempArray[x + y * bitmap.getWidth()]), Color.blue(tempArray[x + y * bitmap.getWidth()]), hsv);
                    hsv[0] = lipstickHSV[0];
                    hsv[1] = hsv[1] + Math.abs((lipstickHSV[1] - hsv[1]) / 5f);
                    Log.d(TAG + " hsv , colorhsv", hsv[0] + " : " + hsv[1] + " : " + hsv[2] + " , " + lipstickHSV[0] + " : " + lipstickHSV[1] + " : " + lipstickHSV[2]);
                    tempArray[x + y * bitmap.getWidth()] = Color.HSVToColor(hsv);
                }
            }
        }
        getBitmap.setPixels(tempArray, 0, getBitmap.getWidth(), 0, 0, getBitmap.getWidth(), getBitmap.getHeight());
    }

    public static int mixTwoColors(int color1, int color2, float amount) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        final byte BLUE_CHANNEL = 0;

        final float inverseAmount = 1.0f - amount;

        int a = ((int) (((float) (color1 >> ALPHA_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> ALPHA_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int r = ((int) (((float) (color1 >> RED_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> RED_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int g = ((int) (((float) (color1 >> GREEN_CHANNEL & 0xff) * amount) +
                ((float) (color2 >> GREEN_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int b = ((int) (((float) (color1 & 0xff) * amount) +
                ((float) (color2 & 0xff) * inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
    }

    private int stringColorRGBToARGB(String hexColor, int valueA, int valueR, int valueG, int valueB) {
        //get color from db
//        int color = Color.parseColor("#FF783928");
        int color1 = Color.parseColor(hexColor);
        int b = (color1) & 0xFF;
        int g = (color1 >> 8) & 0xFF;
        int r = (color1 >> 16) & 0xFF;
        int a = valueA;
        Log.d(TAG + " stringColorRGBToARGB a , r ,g ,b ", a + " " + r + " " + g + " " + b + "");

        if (r + valueR > 255)
            r = 255;
        else if (r + valueR < 0)
            r = 0;
        else
            r = r + valueR;

        if (g + valueG > 255)
            g = 255;
        else if (g + valueG < 0)
            g = 0;
        else
            g = g + valueG;

        if (b + valueB > 255)
            b = 255;
        else if (b + valueB < 0)
            b = 0;
        else
            b = b + valueB;

        int color2 = a << 24 | r << 16 | g << 8 | b << 0;
//        Log.d(TAG + " color1_color2 ", color1 + " : " + color2);
        return color2;
    }

    private int stringColorToARGB(String hexColor, int valueA, int valueR, int valueG, int valueB) {
        //get color from db
//        int color = Color.parseColor("#FF783928");
        int color1 = Color.parseColor(hexColor);
        int b = (color1) & 0xFF;
        int g = (color1 >> 8) & 0xFF;
        int r = (color1 >> 16) & 0xFF;
        int a = (color1 >> 24) & 0xFF;
        Log.d(TAG + "stringColorToARGB a , r ,g ,b ", a + " " + r + " " + g + " " + b + "");

        if (a + valueA > 255)
            a = 255;
        else if (a + valueA < 0)
            a = 0;
        else
            a = a + valueA;

        if (r + valueR > 255)
            r = 255;
        else if (r + valueR < 0)
            r = 0;
        else
            r = r + valueR;

        if (g + valueG > 255)
            g = 255;
        else if (g + valueG < 0)
            g = 0;
        else
            g = g + valueG;

        if (b + valueB > 255)
            b = 255;
        else if (b + valueB < 0)
            b = 0;
        else
            b = b + valueB;

        int color2 = a << 24 | r << 16 | g << 8 | b << 0;
        Log.d(TAG + "stringColorToARGB2 a , r ,g ,b ", a + " " + r + " " + g + " " + b + "");
        Log.d(TAG + " color1_color2 ", color1 + " : " + color2);
        return color2;
    }

    private void drawEyeShadowWithFourColorMethod1(Bitmap getBitmap) {
        Canvas drawCanvas = new Canvas(getBitmap);
        Paint mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(50f);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mPaint.setPathEffect(new CornerPathEffect(70));
        mPaint.setXfermode(mXfermode);

        int size = colorArray.size();
        int color1 = 0, color2 = 0, color3 = 0, color4 = 0;
        if (size == 1) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(0), 0, 60, 60, 60);
            color3 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(0), 0, 60, 60, 60);
            mPaint.setMaskFilter(new BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL));
        } else if (size == 3) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(2), 0, 50, 50, 50);
            mPaint.setMaskFilter(new BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL));
        }
        if (size == 4) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(3), 0, 0, 0, 0);
            mPaint.setMaskFilter(new BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL));
        }

//        int sc = drawCanvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);
//        int mixColor = mixTwoColors(color1,color2,0.5f);
//        Log.d(TAG, mixColor + "");
//        mPaint.setColor(mixColor);

        mPaint.setShader(new LinearGradient(
                landmark_pt_x.get(36), landmark_pt_y.get(36) - 7f,
                landmark_pt_x.get(39), landmark_pt_y.get(39) - 7f,
                color1,
                color2,
                Shader.TileMode.CLAMP));

//       mPaint.setShader(new RadialGradient(
//               landmark_pt_x.get(21) + (landmark_pt_x.get(25) - landmark_pt_x.get(21) / 2), landmark_pt_y.get(21), (landmark_pt_x.get(25) - landmark_pt_x.get(21) / 2),
//                color1, color2,
//                Shader.TileMode.CLAMP));


        float widthEyeShadow = (landmark_pt_y.get(37) - landmark_pt_y.get(19)) / 2.2f;
        Log.d(TAG + " widthEyeShadow ", " " + widthEyeShadow);

        Path path = new Path();
        // left eye
        path.reset();
//        Float left_middle_eye_eyebrow_y = (landmark_pt_y.get(21) - landmark_pt_y.get(31) )/2;
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(36) - 15f, landmark_pt_y.get(36) - 5f);

        path.cubicTo(
                //left_eye_top_left + a distance
                landmark_pt_x.get(37) + 1f, landmark_pt_y.get(37) - widthEyeShadow,
                //left_eye_top_right + a distance
                landmark_pt_x.get(38) + 1f, landmark_pt_y.get(38) - widthEyeShadow,
                //left_eye_right_corner
                landmark_pt_x.get(39) + 7f, landmark_pt_y.get(39) - 1f);

        path.lineTo(landmark_pt_x.get(39) + 6f, landmark_pt_y.get(39));

        path.cubicTo(
                //left_eye_top_left
                landmark_pt_x.get(38) + 1f, landmark_pt_y.get(38) - 10f,
                //left_eye_top_right
                landmark_pt_x.get(37) + 1f, landmark_pt_y.get(37) - 8f,
                //left_eye_left_corner
                landmark_pt_x.get(36), landmark_pt_y.get(36) - 9f);

        path.lineTo(landmark_pt_x.get(36) - 15f, landmark_pt_y.get(36) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // right eye
//        4 colors
//        LinearGradient linearGradient = new LinearGradient(0, 0, width, height,
//                new int[] {
//                        0xFF1e5799,
//                        0xFF207cca,
//                        0xFF2989d8,
//                        0xFF207cca }, //substitute the correct colors for these
//                new float[] {
//                        0, 0.40f, 0.60f, 1 },
//                Shader.TileMode.REPEAT);
        mPaint.setShader(new LinearGradient(
                landmark_pt_x.get(45), landmark_pt_y.get(45) - 7f,
                landmark_pt_x.get(42), landmark_pt_y.get(42) - 7f,
                color1,
                color2,
                Shader.TileMode.CLAMP));

        path.reset();
        //right_eye_right_corner
        path.moveTo(landmark_pt_x.get(45) + 15f, landmark_pt_y.get(45) - 5f);

        path.cubicTo(
                //right_eye_top_right + a distance
                landmark_pt_x.get(44) - 1f, landmark_pt_y.get(44) - widthEyeShadow,
                //right_eye_top_left + a distance
                landmark_pt_x.get(43) - 1f, landmark_pt_y.get(43) - widthEyeShadow,
                //right_eye_left_corner
                landmark_pt_x.get(42) - 7f, landmark_pt_y.get(42) - 1f);

        path.lineTo(landmark_pt_x.get(42) - 6f, landmark_pt_y.get(42));

        path.cubicTo(
                //right_eye_top_left
                landmark_pt_x.get(43) - 1f, landmark_pt_y.get(43) - 10f,
                //right_eye_top_right
                landmark_pt_x.get(44) - 1f, landmark_pt_y.get(44) - 8f,
                //right_eye_right_corner
                landmark_pt_x.get(45), landmark_pt_y.get(45) - 9f);

        path.lineTo(landmark_pt_x.get(45) + 15f, landmark_pt_y.get(45) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // down eye shadow

        mPaint.setShader(
                new LinearGradient(landmark_pt_x.get(36), landmark_pt_y.get(36) + 10f,
                        landmark_pt_x.get(39), landmark_pt_y.get(39) + 10f,
                        color3, color4,
                        Shader.TileMode.CLAMP));

//        mPaint.setMaskFilter(new BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL));

        widthEyeShadow = (landmark_pt_y.get(37) - landmark_pt_y.get(19)) / 3f;
        Log.d(TAG + " widthEyeShadow ", " " + widthEyeShadow);

        // left eye
        path.reset();
//        Float left_middle_eye_eyebrow_y = (landmark_pt_y.get(21) - landmark_pt_y.get(31) )/2;
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) + 4f);
        path.lineTo(landmark_pt_x.get(41) - 5f, landmark_pt_y.get(41) + 7f);
        path.lineTo(landmark_pt_x.get(40) - 5f, landmark_pt_y.get(40) + 7f);
        path.lineTo(landmark_pt_x.get(39) + 10f, landmark_pt_y.get(39) + 4f);
        path.lineTo(landmark_pt_x.get(40) - 5f, landmark_pt_y.get(40) + 4f);
        path.lineTo(landmark_pt_x.get(41) - 5f, landmark_pt_y.get(41) + 4f);
        path.lineTo(landmark_pt_x.get(36) - 5f, landmark_pt_y.get(36) + 4f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // right eye
//        4 colors
//        LinearGradient linearGradient = new LinearGradient(0, 0, width, height,
//                new int[] {
//                        0xFF1e5799,
//                        0xFF207cca,
//                        0xFF2989d8,
//                        0xFF207cca }, //substitute the correct colors for these
//                new float[] {
//                        0, 0.40f, 0.60f, 1 },
//                Shader.TileMode.REPEAT);
        mPaint.setShader(
                new LinearGradient(
                        landmark_pt_x.get(45), landmark_pt_y.get(45) + 10f,
                        landmark_pt_x.get(42), landmark_pt_y.get(42) + 10f,
                        color3, color4,
                        Shader.TileMode.CLAMP));
        mPaint.setMaskFilter(new BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL));

        path.reset();
        //right_eye_right_corner
        path.moveTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) + 4f);
        path.lineTo(landmark_pt_x.get(46) + 5f, landmark_pt_y.get(46) + 7f);
        path.lineTo(landmark_pt_x.get(47) + 5f, landmark_pt_y.get(47) + 7f);
        path.lineTo(landmark_pt_x.get(42) - 10f, landmark_pt_y.get(42) + 4f);
        path.lineTo(landmark_pt_x.get(47) + 5f, landmark_pt_y.get(47) + 4f);
        path.lineTo(landmark_pt_x.get(46) + 5f, landmark_pt_y.get(46) + 4f);
        path.lineTo(landmark_pt_x.get(45) + 5f, landmark_pt_y.get(45) + 4f);

        path.close();
        drawCanvas.drawPath(path, mPaint);
    }

    private void drawEyeShadowWithFourColorMethod2(Bitmap getBitmap) {
        Canvas drawCanvas = new Canvas(getBitmap);
        Paint mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(50f);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mPaint.setPathEffect(new CornerPathEffect(70));
        mPaint.setXfermode(mXfermode);

        int size = colorArray.size();
        int color1 = 0, color2 = 0, color3 = 0, color4 = 0;
        if (size == 1) {
            color2 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(0), -100, 30, 30, 30);
            color4 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
        } else if (size == 2) {
            color1 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
        } else if (size == 3) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
        } else if (size == 4) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(3), 0, 0, 0, 0);
        }
//        1.
        mPaint.setColor(color1);
        mPaint.setMaskFilter(new BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL));

        float widthEyeShadow = (landmark_pt_y.get(37) - landmark_pt_y.get(19)) / 2.2f;
        Log.d(TAG + " widthEyeShadow ", " " + widthEyeShadow);

        Path path = new Path();
        // left eye
        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(36) - 15f, landmark_pt_y.get(36) - 5f);

        path.cubicTo(
                //left_eye_top_left + a distance
                landmark_pt_x.get(37) + 1f, landmark_pt_y.get(37) - widthEyeShadow,
                //left_eye_top_right + a distance
                landmark_pt_x.get(38) + 1f, landmark_pt_y.get(38) - widthEyeShadow,
                //left_eye_right_corner
                landmark_pt_x.get(39) + 7f, landmark_pt_y.get(39) - 1f);

        path.lineTo(landmark_pt_x.get(39) + 6f, landmark_pt_y.get(39));

        path.cubicTo(
                //left_eye_top_left
                landmark_pt_x.get(38) + 1f, landmark_pt_y.get(38) - 10f,
                //left_eye_top_right
                landmark_pt_x.get(37) + 1f, landmark_pt_y.get(37) - 8f,
                //left_eye_left_corner
                landmark_pt_x.get(36), landmark_pt_y.get(36) - 9f);

        path.lineTo(landmark_pt_x.get(36) - 15f, landmark_pt_y.get(36) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        //right_eye_right_corner
        path.moveTo(landmark_pt_x.get(45) + 15f, landmark_pt_y.get(45) - 5f);

        path.cubicTo(
                //right_eye_top_right + a distance
                landmark_pt_x.get(44) + 1f, landmark_pt_y.get(44) - widthEyeShadow,
                //right_eye_top_left + a distance
                landmark_pt_x.get(43) + 1f, landmark_pt_y.get(43) - widthEyeShadow,
                //right_eye_left_corner
                landmark_pt_x.get(42) - 7f, landmark_pt_y.get(42) - 1f);

        path.lineTo(landmark_pt_x.get(42) - 6f, landmark_pt_y.get(42));

        path.cubicTo(
                //right_eye_top_left
                landmark_pt_x.get(43) + 1f, landmark_pt_y.get(43) - 10f,
                //right_eye_top_right
                landmark_pt_x.get(44) + 1f, landmark_pt_y.get(44) - 8f,
                //right_eye_right_corner
                landmark_pt_x.get(45), landmark_pt_y.get(45) - 9f);

        path.lineTo(landmark_pt_x.get(45) + 15f, landmark_pt_y.get(45) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // 2.
        mPaint.setColor(color2);
        mPaint.setStrokeJoin(Paint.Join.MITER);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.SQUARE);
//        mPaint.setMaskFilter(new BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL));
        // left eye
        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 5f);
        path.lineTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 7f);
        path.lineTo(landmark_pt_x.get(36) - 17f, landmark_pt_y.get(36) - 7f);
        path.lineTo(landmark_pt_x.get(41) - 10f, landmark_pt_y.get(41) + 7f);
        path.lineTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) - 5f);
        path.lineTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(44) + 10f, landmark_pt_y.get(44) - 5f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 7f);
        path.lineTo(landmark_pt_x.get(45) + 17f, landmark_pt_y.get(45) - 7f);
        path.lineTo(landmark_pt_x.get(46) + 10f, landmark_pt_y.get(46) + 7f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 5f);
        path.lineTo(landmark_pt_x.get(44) + 10f, landmark_pt_y.get(44) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // 3.
        mPaint.setColor(color3);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);
//        mPaint.setMaskFilter(new BlurMaskFilter(1f, BlurMaskFilter.Blur.NORMAL));
        path.reset();

        path.moveTo(landmark_pt_x.get(36) - 1f, landmark_pt_y.get(36) - 4f);
        path.lineTo(landmark_pt_x.get(37) - 1f, landmark_pt_y.get(37) - 4f);
        path.lineTo(landmark_pt_x.get(38) - 1f, landmark_pt_y.get(38) - 4f);
        path.lineTo(landmark_pt_x.get(39) - 1f, landmark_pt_y.get(39) - 4f);
        path.lineTo(landmark_pt_x.get(39) - 1f, landmark_pt_y.get(39) - 5.5f);
        path.lineTo(landmark_pt_x.get(38) - 1f, landmark_pt_y.get(38) - 6.5f);
        path.lineTo(landmark_pt_x.get(37) - 1f, landmark_pt_y.get(37) - 6.5f);
        path.lineTo(landmark_pt_x.get(36) - 1f, landmark_pt_y.get(36) - 6.5f);

        path.moveTo(landmark_pt_x.get(45) + 1f, landmark_pt_y.get(45) - 4f);
        path.lineTo(landmark_pt_x.get(44) + 1f, landmark_pt_y.get(44) - 4f);
        path.lineTo(landmark_pt_x.get(43) + 1f, landmark_pt_y.get(43) - 4f);
        path.lineTo(landmark_pt_x.get(42) + 1f, landmark_pt_y.get(42) - 4f);
        path.lineTo(landmark_pt_x.get(42) + 1f, landmark_pt_y.get(42) - 5.5f);
        path.lineTo(landmark_pt_x.get(43) + 1f, landmark_pt_y.get(43) - 6.5f);
        path.lineTo(landmark_pt_x.get(44) + 1f, landmark_pt_y.get(44) - 6.5f);
        path.lineTo(landmark_pt_x.get(45) + 1f, landmark_pt_y.get(45) - 6.5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);
        //设置混合模式

        // 3.
        mPaint.setColor(color3);
//        mPaint.setMaskFilter(new BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL));
        // left eye
        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 5f);
        path.lineTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 7f);
        path.lineTo(landmark_pt_x.get(36) - 17f, landmark_pt_y.get(36) - 7f);
        path.lineTo(landmark_pt_x.get(41) - 10f, landmark_pt_y.get(41) + 7f);
        path.lineTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) - 5f);
        path.lineTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(44) + 10f, landmark_pt_y.get(44) - 5f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 7f);
        path.lineTo(landmark_pt_x.get(45) + 17f, landmark_pt_y.get(45) - 7f);
        path.lineTo(landmark_pt_x.get(46) + 10f, landmark_pt_y.get(46) + 7f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 5f);
        path.lineTo(landmark_pt_x.get(44) + 10f, landmark_pt_y.get(44) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

//        down eye shadow
//        4.
        mPaint.setColor(color4);
//        mPaint.setMaskFilter(new BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL));

//        widthEyeShadow = (landmark_pt_y.get(37) - landmark_pt_y.get(19)) / 3.5f;
        Log.d(TAG + " down widthEyeShadow ", " " + widthEyeShadow);
        // left eye
        path.reset();
//        Float left_middle_eye_eyebrow_y = (landmark_pt_y.get(21) - landmark_pt_y.get(31) )/2;
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) + 5f);

        path.cubicTo(
                //left_eye_top_left + a distance
                landmark_pt_x.get(41) + 1f, landmark_pt_y.get(41) + 6f,
                //left_eye_top_right + a distance
                landmark_pt_x.get(40) + 1f, landmark_pt_y.get(40) + 6f,
                //left_eye_right_corner
                landmark_pt_x.get(39) + 12f, landmark_pt_y.get(39) + 3f);

//        path.lineTo(landmark_pt_x.get(39) + 13f, landmark_pt_y.get(39));

        path.cubicTo(
                //left_eye_top_left
                landmark_pt_x.get(40) + 1f, landmark_pt_y.get(40) + 5f,
                //left_eye_top_right
                landmark_pt_x.get(41) + 1f, landmark_pt_y.get(41) + 4f,
                //left_eye_left_corner
                landmark_pt_x.get(36), landmark_pt_y.get(36) + 4f);

        path.lineTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) + 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        //right_eye_right_corner
        path.moveTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) + 5f);

        path.cubicTo(
                //right_eye_top_right + a distance
                landmark_pt_x.get(46) - 1f, landmark_pt_y.get(46) + 6f,
                //right_eye_top_left + a distance
                landmark_pt_x.get(47) - 1f, landmark_pt_y.get(47) + 6f,
                //right_eye_left_corner
                landmark_pt_x.get(42) - 12f, landmark_pt_y.get(42) + 3f);

//        path.lineTo(landmark_pt_x.get(42) - 13f, landmark_pt_y.get(42));

        path.cubicTo(
                //right_eye_top_left
                landmark_pt_x.get(47) - 1f, landmark_pt_y.get(47) + 5f,
                //right_eye_top_right
                landmark_pt_x.get(46) - 1f, landmark_pt_y.get(46) + 4f,
                //right_eye_right_corner
                landmark_pt_x.get(45), landmark_pt_y.get(45) + 4f);

        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) + 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);
    }


    //    http://yuanx2liang.pixnet.net/blog/post/274719413-阿元師四色眼影無敵必勝畫法
    private void drawEyeShadowWithFourColorMethod3(Bitmap getBitmap) {
        Canvas drawCanvas = new Canvas(getBitmap);
        Paint mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(50f);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mPaint.setPathEffect(new CornerPathEffect(70));
        mPaint.setXfermode(mXfermode);

        int size = colorArray.size();
        int color1 = 0, color2 = 0, color3 = 0, color4 = 0, shapeColor = 0;
        if (size == 2) {
            shapeColor = stringColorToARGB(colorArray.get(1), -60, 0, 0, 0);
            color1 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(0), 0, -40, -40, -40);
            color4 = stringColorToARGB(colorArray.get(0), 0, -20, -20, -20);
        }
        if (size == 3) {
            shapeColor = stringColorToARGB(colorArray.get(0), -60, 0, 0, 0);
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
        } else if (size == 4) {
            shapeColor = stringColorToARGB(colorArray.get(0), -120, 0, 0, 0);
            color1 = stringColorToARGB(colorArray.get(0), -80, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), -80, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(2), -80, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(3), -80, 0, 0, 0);
        }

//        1.
        mPaint.setMaskFilter(new BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL));

        float widthEyeShadow = (landmark_pt_y.get(37) - landmark_pt_y.get(19)) / 2.2f;
        Log.d(TAG + " widthEyeShadow ", " " + widthEyeShadow);

        Path path = new Path();
        // left eye

        mPaint.setColor(color1);

        path.moveTo(landmark_pt_x.get(39) + 13f, landmark_pt_y.get(39));
        path.lineTo(landmark_pt_x.get(38) + 3f, landmark_pt_y.get(38) - 3f);
        path.lineTo(landmark_pt_x.get(38) + 3f, landmark_pt_y.get(38) - 12f);
        path.lineTo(landmark_pt_x.get(39) + 13f, landmark_pt_y.get(39));

        path.moveTo(landmark_pt_x.get(42) - 13f, landmark_pt_y.get(42));
        path.lineTo(landmark_pt_x.get(43) - 5f, landmark_pt_y.get(43) - 3f);
        path.lineTo(landmark_pt_x.get(43) - 5f, landmark_pt_y.get(43) - 12f);
        path.lineTo(landmark_pt_x.get(42) - 13f, landmark_pt_y.get(42));

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        mPaint.setColor(color2);

        path.moveTo(landmark_pt_x.get(38) + 5f, landmark_pt_y.get(38) - 5f);
        path.lineTo(landmark_pt_x.get(37), landmark_pt_y.get(37) - 5f);
        path.lineTo(landmark_pt_x.get(37), landmark_pt_y.get(37) - 10f);
        path.lineTo(landmark_pt_x.get(38) + 5f, landmark_pt_y.get(37) - 10f);
        path.lineTo(landmark_pt_x.get(38) + 5f, landmark_pt_y.get(37) - 5f);

        path.moveTo(landmark_pt_x.get(43) - 5f, landmark_pt_y.get(38) - 5f);
        path.lineTo(landmark_pt_x.get(44), landmark_pt_y.get(37) - 6f);
        path.lineTo(landmark_pt_x.get(44), landmark_pt_y.get(37) - 11f);
        path.lineTo(landmark_pt_x.get(43) - 5f, landmark_pt_y.get(37) - 10f);
        path.lineTo(landmark_pt_x.get(43) - 5f, landmark_pt_y.get(37) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        mPaint.setColor(color2);

        path.moveTo(landmark_pt_x.get(37), landmark_pt_y.get(37) - 5f);
        path.lineTo(landmark_pt_x.get(36) - 7f, landmark_pt_y.get(36) - 7f);
        path.lineTo(landmark_pt_x.get(36) - 8f, landmark_pt_y.get(36) - 12f);
        path.lineTo(landmark_pt_x.get(37), landmark_pt_y.get(37) - 9f);


        path.moveTo(landmark_pt_x.get(44), landmark_pt_y.get(44) - 4f);
        path.lineTo(landmark_pt_x.get(45) + 7f, landmark_pt_y.get(45) - 7f);
        path.lineTo(landmark_pt_x.get(45) + 12f, landmark_pt_y.get(45) - 12f);
        path.lineTo(landmark_pt_x.get(44), landmark_pt_y.get(44) - 8f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // down eye shadow
        //        3.
        mPaint.setColor(color2);
//        mPaint.setMaskFilter(new BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL));
        // left eye
        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(37) - 7f, landmark_pt_y.get(37) - 3f);
        path.lineTo(landmark_pt_x.get(37) - 7f, landmark_pt_y.get(37) - 6f);
        path.lineTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) - 6f);
        path.lineTo(landmark_pt_x.get(41) - 7f, landmark_pt_y.get(41) + 6f);
        path.lineTo(landmark_pt_x.get(36) - 7f, landmark_pt_y.get(36) - 3f);
        path.lineTo(landmark_pt_x.get(37) - 7f, landmark_pt_y.get(37) - 3f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(44) + 7f, landmark_pt_y.get(44) - 3f);
        path.lineTo(landmark_pt_x.get(45) + 7f, landmark_pt_y.get(45) - 6f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 6f);
        path.lineTo(landmark_pt_x.get(46) + 7f, landmark_pt_y.get(46) + 6f);
        path.lineTo(landmark_pt_x.get(45) + 7f, landmark_pt_y.get(45) - 3f);
        path.lineTo(landmark_pt_x.get(44) + 7f, landmark_pt_y.get(44) - 3f);

        path.close();
        drawCanvas.drawPath(path, mPaint);
//        mPaint.setMaskFilter(new BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL));
        // left eye
        path.reset();
        mPaint.setColor(color3);
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 3f);
        path.lineTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 6f);
        path.lineTo(landmark_pt_x.get(36) - 15f, landmark_pt_y.get(36) - 6f);
        path.lineTo(landmark_pt_x.get(41) - 10f, landmark_pt_y.get(41) + 6f);
        path.lineTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) - 3f);
        path.lineTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 3f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(44) + 10f, landmark_pt_y.get(44) - 3f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 6f);
        path.lineTo(landmark_pt_x.get(45) + 15f, landmark_pt_y.get(45) - 6f);
        path.lineTo(landmark_pt_x.get(46) + 10f, landmark_pt_y.get(46) + 6f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 3f);
        path.lineTo(landmark_pt_x.get(44) + 10f, landmark_pt_y.get(44) - 3f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

//        down eye shadow
//        4.
        mPaint.setColor(color4);
//        mPaint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));

//        widthEyeShadow = (landmark_pt_y.get(37) - landmark_pt_y.get(19)) / 3.5f;
        Log.d(TAG + " down widthEyeShadow ", " " + widthEyeShadow);
        // left eye
        path.reset();
//        Float left_middle_eye_eyebrow_y = (landmark_pt_y.get(21) - landmark_pt_y.get(31) )/2;
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(41), landmark_pt_y.get(41) + 6f);

        path.cubicTo(
                //left_eye_top_left + a distance
                landmark_pt_x.get(41) + 1f, landmark_pt_y.get(41) + 8f,
                //left_eye_top_right + a distance
                landmark_pt_x.get(40) + 1f, landmark_pt_y.get(40) + 6f,
                //left_eye_right_corner
                landmark_pt_x.get(39) + 12f, landmark_pt_y.get(39) + 3f);

//        path.lineTo(landmark_pt_x.get(39) + 13f, landmark_pt_y.get(39));

        path.cubicTo(
                //left_eye_top_left
                landmark_pt_x.get(40) + 1f, landmark_pt_y.get(40) + 5f,
                //left_eye_top_right
                landmark_pt_x.get(41) + 1f, landmark_pt_y.get(41) + 4f,
                //left_eye_left_corner
                landmark_pt_x.get(36) - 5f, landmark_pt_y.get(36) + 3f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        //right_eye_right_corner
        path.moveTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) + 6f);

        path.cubicTo(
                //right_eye_top_right + a distance
                landmark_pt_x.get(46) - 1f, landmark_pt_y.get(46) + 8f,
                //right_eye_top_left + a distance
                landmark_pt_x.get(47) - 1f, landmark_pt_y.get(47) + 6f,
                //right_eye_left_corner
                landmark_pt_x.get(42) - 12f, landmark_pt_y.get(42) + 3f);

//        path.lineTo(landmark_pt_x.get(42) - 13f, landmark_pt_y.get(42));

        path.cubicTo(
                //right_eye_top_left
                landmark_pt_x.get(47) - 1f, landmark_pt_y.get(47) + 5f,
                //right_eye_top_right
                landmark_pt_x.get(46) - 1f, landmark_pt_y.get(46) + 4f,
                //right_eye_right_corner
                landmark_pt_x.get(45), landmark_pt_y.get(45) + 3f);

//        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) + 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);


        //清除混合模式
        mPaint.setXfermode(null);
    }

    //    http://liz.tw/maybelline-eyelash-2014/
    private void drawEyeShadowWithFourColorMethod4(Bitmap getBitmap) {
        Canvas drawCanvas = new Canvas(getBitmap);
        Paint mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(50f);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mPaint.setPathEffect(new CornerPathEffect(100));
        mPaint.setXfermode(mXfermode);

        int size = colorArray.size();
        int color1 = 0, color2 = 0, color3 = 0, color4 = 0, shapeColor = 0;
        if (size == 1) {
            color2 = stringColorToARGB(colorArray.get(0), -150, 20, 20, 20);
            color3 = stringColorToARGB(colorArray.get(0), -160, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(0), -170, 0, 0, 0);
            mPaint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
        } else if (size == 2) {
            color2 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            shapeColor = stringColorToARGB(colorArray.get(1), -60, 0, 0, 0);
            mPaint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
        } else if (size == 3) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(2), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            shapeColor = stringColorToARGB(colorArray.get(0), -60, 0, 0, 0);
            mPaint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
        } else {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(colorArray.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(colorArray.get(3), 0, 0, 0, 0);
            shapeColor = stringColorToARGB(colorArray.get(0), -50, 0, 0, 0);
            mPaint.setMaskFilter(new BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL));
        }
//        1.
        mPaint.setColor(color1);

        Path path = new Path();

        path.moveTo(landmark_pt_x.get(39) + 13f, landmark_pt_y.get(39));
        path.lineTo(landmark_pt_x.get(38), landmark_pt_y.get(38) - 12f);
        path.lineTo(landmark_pt_x.get(37), landmark_pt_y.get(37) - 12f);
        path.lineTo(landmark_pt_x.get(36) - 13f, landmark_pt_y.get(36) - 14f);
        path.lineTo(landmark_pt_x.get(36) - 8f, landmark_pt_y.get(36) - 9f);
        path.lineTo(landmark_pt_x.get(37), landmark_pt_y.get(37) - 9f);
        path.lineTo(landmark_pt_x.get(38), landmark_pt_y.get(37) - 9f);
        path.lineTo(landmark_pt_x.get(39) + 9f, landmark_pt_y.get(39));
        path.lineTo(landmark_pt_x.get(39) + 13f, landmark_pt_y.get(39));

        path.moveTo(landmark_pt_x.get(42) - 13f, landmark_pt_y.get(42));
        path.lineTo(landmark_pt_x.get(43), landmark_pt_y.get(43) - 12f);
        path.lineTo(landmark_pt_x.get(44), landmark_pt_y.get(44) - 12f);
        path.lineTo(landmark_pt_x.get(45) + 14f, landmark_pt_y.get(45) - 14f);
        path.lineTo(landmark_pt_x.get(45) + 9f, landmark_pt_y.get(45) - 9f);
        path.lineTo(landmark_pt_x.get(44), landmark_pt_y.get(44) - 9f);
        path.lineTo(landmark_pt_x.get(43), landmark_pt_y.get(43) - 9f);
        path.lineTo(landmark_pt_x.get(42) - 9f, landmark_pt_y.get(42));
        path.lineTo(landmark_pt_x.get(42) - 13f, landmark_pt_y.get(42));

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();

        mPaint.setColor(shapeColor);
        //打影
        path.moveTo(landmark_pt_x.get(17) + 5f, landmark_pt_y.get(17) + 3f);
        path.lineTo(landmark_pt_x.get(18) + 5f, landmark_pt_y.get(18) + 13f);
        path.lineTo(landmark_pt_x.get(19) + 5f, landmark_pt_y.get(19) + 16f);
        path.lineTo(landmark_pt_x.get(18) + 5f, landmark_pt_y.get(18) + 16f);
        path.lineTo(landmark_pt_x.get(17) + 5f, landmark_pt_y.get(17) + 6f);

        path.moveTo(landmark_pt_x.get(26) - 5f, landmark_pt_y.get(26) + 3f);
        path.lineTo(landmark_pt_x.get(25) - 5f, landmark_pt_y.get(25) + 13f);
        path.lineTo(landmark_pt_x.get(24) - 5f, landmark_pt_y.get(24) + 16f);
        path.lineTo(landmark_pt_x.get(25) - 5f, landmark_pt_y.get(25) + 16f);
        path.lineTo(landmark_pt_x.get(26) - 5f, landmark_pt_y.get(26) + 6f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

//        2.
        mPaint.setColor(color2);
        path.reset();
        // left eye
        path.moveTo(landmark_pt_x.get(39) + 9f, landmark_pt_y.get(39));
        path.lineTo(landmark_pt_x.get(38), landmark_pt_y.get(38) - 9f);
        path.lineTo(landmark_pt_x.get(37), landmark_pt_y.get(37) - 9f);
        path.lineTo(landmark_pt_x.get(36) - 9f, landmark_pt_y.get(36) - 9f);
        path.lineTo(landmark_pt_x.get(36) - 6f, landmark_pt_y.get(36) - 6f);
        path.lineTo(landmark_pt_x.get(37), landmark_pt_y.get(37) - 5f);
        path.lineTo(landmark_pt_x.get(38), landmark_pt_y.get(37) - 5f);
        path.lineTo(landmark_pt_x.get(39) + 6f, landmark_pt_y.get(39));
        path.lineTo(landmark_pt_x.get(39) + 9f, landmark_pt_y.get(39));

        path.moveTo(landmark_pt_x.get(42) - 9f, landmark_pt_y.get(42));
        path.lineTo(landmark_pt_x.get(43), landmark_pt_y.get(43) - 9f);
        path.lineTo(landmark_pt_x.get(44), landmark_pt_y.get(44) - 9f);
        path.lineTo(landmark_pt_x.get(45) + 9f, landmark_pt_y.get(45) - 9f);
        path.lineTo(landmark_pt_x.get(45) + 6f, landmark_pt_y.get(45) - 6f);
        path.lineTo(landmark_pt_x.get(44), landmark_pt_y.get(44) - 5f);
        path.lineTo(landmark_pt_x.get(43), landmark_pt_y.get(43) - 5f);
        path.lineTo(landmark_pt_x.get(42) - 6f, landmark_pt_y.get(42));
        path.lineTo(landmark_pt_x.get(42) - 9f, landmark_pt_y.get(42));

        path.close();
        drawCanvas.drawPath(path, mPaint);
//3.
        mPaint.setColor(color2);
        path.reset();
        // left eye
        path.moveTo(landmark_pt_x.get(36) - 9f, landmark_pt_y.get(36));
        path.lineTo(landmark_pt_x.get(41) + 3f, landmark_pt_y.get(41) + 9f);
        path.lineTo(landmark_pt_x.get(40) + 3f, landmark_pt_y.get(40) + 9f);
        path.lineTo(landmark_pt_x.get(40) + 3f, landmark_pt_y.get(40) + 7f);
        path.lineTo(landmark_pt_x.get(41) + 3f, landmark_pt_y.get(41) + 7f);
        path.lineTo(landmark_pt_x.get(36) + 6f, landmark_pt_y.get(36));
        path.lineTo(landmark_pt_x.get(36) - 7f, landmark_pt_y.get(36));
        path.lineTo(landmark_pt_x.get(36) - 9f, landmark_pt_y.get(36));

        path.moveTo(landmark_pt_x.get(45) + 9f, landmark_pt_y.get(45));
        path.lineTo(landmark_pt_x.get(46) - 3f, landmark_pt_y.get(46) + 9f);
        path.lineTo(landmark_pt_x.get(47) - 3f, landmark_pt_y.get(47) + 9f);
        path.lineTo(landmark_pt_x.get(47) - 3f, landmark_pt_y.get(47) + 7f);
        path.lineTo(landmark_pt_x.get(46) - 3f, landmark_pt_y.get(46) + 7f);
        path.lineTo(landmark_pt_x.get(45) - 6f, landmark_pt_y.get(45));
        path.lineTo(landmark_pt_x.get(45) + 7f, landmark_pt_y.get(45));
        path.lineTo(landmark_pt_x.get(45) + 9f, landmark_pt_y.get(45));

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        mPaint.setColor(color2);
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(38) - 5f, landmark_pt_y.get(38) - 3f);
        path.lineTo(landmark_pt_x.get(37) - 9f, landmark_pt_y.get(37) - 3f);
        path.lineTo(landmark_pt_x.get(37) - 9f, landmark_pt_y.get(37) - 6f);
        path.lineTo(landmark_pt_x.get(36) - 14f, landmark_pt_y.get(36) - 6f);
        path.lineTo(landmark_pt_x.get(41) - 9f, landmark_pt_y.get(41) + 6f);
        path.lineTo(landmark_pt_x.get(36) - 9f, landmark_pt_y.get(36) - 3f);
        path.lineTo(landmark_pt_x.get(37) - 9f, landmark_pt_y.get(37) - 3f);
        path.lineTo(landmark_pt_x.get(38) - 5f, landmark_pt_y.get(38) - 3f);

        path.moveTo(landmark_pt_x.get(43) + 5f, landmark_pt_y.get(43) - 3f);
        path.lineTo(landmark_pt_x.get(44) + 9f, landmark_pt_y.get(44) - 3f);
        path.lineTo(landmark_pt_x.get(45) + 9f, landmark_pt_y.get(45) - 6f);
        path.lineTo(landmark_pt_x.get(45) + 14f, landmark_pt_y.get(45) - 6f);
        path.lineTo(landmark_pt_x.get(46) + 9f, landmark_pt_y.get(46) + 6f);
        path.lineTo(landmark_pt_x.get(45) + 9f, landmark_pt_y.get(45) - 3f);
        path.lineTo(landmark_pt_x.get(44) + 9f, landmark_pt_y.get(44) - 3f);
        path.lineTo(landmark_pt_x.get(43) + 5f, landmark_pt_y.get(43) - 3f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        mPaint.setColor(color3);
        mPaint.setStrokeJoin(Paint.Join.MITER);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.SQUARE);      // set the paint cap to round too

        path.moveTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 5f);
        path.lineTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 8f);
        path.lineTo(landmark_pt_x.get(36) - 17f, landmark_pt_y.get(36) - 8f);
        path.lineTo(landmark_pt_x.get(41) - 10f, landmark_pt_y.get(41) + 8f);
        path.lineTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) - 5f);
        path.lineTo(landmark_pt_x.get(37) - 10f, landmark_pt_y.get(37) - 5f);

        path.moveTo(landmark_pt_x.get(44) + 10f, landmark_pt_y.get(44) - 5f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 8f);
        path.lineTo(landmark_pt_x.get(45) + 17f, landmark_pt_y.get(45) - 8f);
        path.lineTo(landmark_pt_x.get(46) + 10f, landmark_pt_y.get(46) + 8f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 5f);
        path.lineTo(landmark_pt_x.get(44) + 10f, landmark_pt_y.get(44) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

//        down eye shadow
//        4.
        mPaint.setColor(color4);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too

        path.reset();
        path.moveTo(landmark_pt_x.get(41), landmark_pt_y.get(41) + 6f);
        path.lineTo(landmark_pt_x.get(40), landmark_pt_y.get(40) + 10f);
        path.lineTo(landmark_pt_x.get(39) + 10f, landmark_pt_y.get(39));
        path.lineTo(landmark_pt_x.get(40), landmark_pt_y.get(40) + 6f);
        path.lineTo(landmark_pt_x.get(41), landmark_pt_y.get(41) + 6f);

        path.moveTo(landmark_pt_x.get(46), landmark_pt_y.get(47) + 6f);
        path.lineTo(landmark_pt_x.get(47), landmark_pt_y.get(46) + 10f);
        path.lineTo(landmark_pt_x.get(42) - 10f, landmark_pt_y.get(45));
        path.lineTo(landmark_pt_x.get(47), landmark_pt_y.get(46) + 6f);
        path.lineTo(landmark_pt_x.get(46), landmark_pt_y.get(47) + 6f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

    }

    private void drawEyeShadowWithTwoColorMethod1(Bitmap getBitmap) {

        Canvas drawCanvas = new Canvas(getBitmap);
        Paint mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(50f);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mPaint.setPathEffect(new CornerPathEffect(70));
        mPaint.setXfermode(mXfermode);
        mPaint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));

        int size = colorArray.size();
        int color1 = 0, color2 = 0;
        if (size == 1) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(0), 0, 60, 60, 60);
        } else if (size == 2) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
        }

        mPaint.setShader(new LinearGradient(
                landmark_pt_x.get(36), landmark_pt_y.get(36) - 8f,
                landmark_pt_x.get(39), landmark_pt_y.get(39) - 8f,
                color1,
                color2,
                Shader.TileMode.CLAMP));

//       mPaint.setShader(new RadialGradient(
//               landmark_pt_x.get(21) + (landmark_pt_x.get(25) - landmark_pt_x.get(21) / 2), landmark_pt_y.get(21), (landmark_pt_x.get(25) - landmark_pt_x.get(21) / 2),
//                color1, color2,
//                Shader.TileMode.CLAMP));


        float widthEyeShadow = (landmark_pt_y.get(37) - landmark_pt_y.get(19)) / 2.2f;
        Log.d(TAG + " widthEyeShadow ", " " + widthEyeShadow);

        Path path = new Path();
        // left eye
        path.reset();

        path.moveTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) - 7f);
        path.lineTo(landmark_pt_x.get(37) - 6f, landmark_pt_y.get(37) - 11f);
        path.lineTo(landmark_pt_x.get(38) - 6f, landmark_pt_y.get(38) - 11f);
        path.lineTo(landmark_pt_x.get(39) + 7f, landmark_pt_y.get(39) - 5f);

        path.lineTo(landmark_pt_x.get(38) - 6f, landmark_pt_y.get(38) - 5f);
        path.lineTo(landmark_pt_x.get(37) - 6f, landmark_pt_y.get(37) - 5f);
        path.lineTo(landmark_pt_x.get(36) - 7f, landmark_pt_y.get(36) - 5f);
        path.lineTo(landmark_pt_x.get(36) - 10f, landmark_pt_y.get(36) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // right eye
//        4 colors
//        LinearGradient linearGradient = new LinearGradient(0, 0, width, height,
//                new int[] {
//                        0xFF1e5799,
//                        0xFF207cca,
//                        0xFF2989d8,
//                        0xFF207cca }, //substitute the correct colors for these
//                new float[] {
//                        0, 0.40f, 0.60f, 1 },
//                Shader.TileMode.REPEAT);
        mPaint.setShader(new LinearGradient(
                landmark_pt_x.get(45), landmark_pt_y.get(45) - 8f,
                landmark_pt_x.get(42), landmark_pt_y.get(42) - 8f,
                color1,
                color2,
                Shader.TileMode.CLAMP));

        path.reset();
        //right_eye_right_corner
        path.moveTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 7f);
        path.lineTo(landmark_pt_x.get(44) + 6f, landmark_pt_y.get(44) - 11f);
        path.lineTo(landmark_pt_x.get(43) + 6f, landmark_pt_y.get(43) - 11f);
        path.lineTo(landmark_pt_x.get(42) - 7f, landmark_pt_y.get(42) - 5f);

        path.lineTo(landmark_pt_x.get(43) + 6f, landmark_pt_y.get(43) - 5f);
        path.lineTo(landmark_pt_x.get(44) + 6f, landmark_pt_y.get(44) - 5f);
        path.lineTo(landmark_pt_x.get(45) + 7f, landmark_pt_y.get(45) - 5f);
        path.lineTo(landmark_pt_x.get(45) + 10f, landmark_pt_y.get(45) - 5f);


        path.close();
        drawCanvas.drawPath(path, mPaint);
    }

    private void drawEyeShadowWithTwoColorMethod2(Bitmap getBitmap) {

        Canvas drawCanvas = new Canvas(getBitmap);
        Paint mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(50f);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mPaint.setPathEffect(new CornerPathEffect(70));
        mPaint.setXfermode(mXfermode);

        int size = colorArray.size();
        int color1 = 0, color2 = 0;
        if (size == 1) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
        } else if (size == 2) {
            color1 = stringColorToARGB(colorArray.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(colorArray.get(1), 0, 0, 0, 0);
        }

//        int sc = drawCanvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);
//        int mixColor = mixTwoColors(color1,color2,0.5f);
//        Log.d(TAG, mixColor + "");
//        mPaint.setColor(mixColor);

        mPaint.setShader(new LinearGradient(
                landmark_pt_x.get(36), landmark_pt_y.get(36) - 10f,
                landmark_pt_x.get(39), landmark_pt_y.get(39) - 10f,
                color1,
                color1,
                Shader.TileMode.CLAMP));

//       mPaint.setShader(new RadialGradient(
//               landmark_pt_x.get(21) + (landmark_pt_x.get(25) - landmark_pt_x.get(21) / 2), landmark_pt_y.get(21), (landmark_pt_x.get(25) - landmark_pt_x.get(21) / 2),
//                color1, color2,
//                Shader.TileMode.CLAMP));
        mPaint.setMaskFilter(new BlurMaskFilter(6, BlurMaskFilter.Blur.NORMAL));

        float widthEyeShadow = (landmark_pt_y.get(37) - landmark_pt_y.get(19)) / 3f;
        Log.d(TAG + " widthEyeShadow ", " " + widthEyeShadow);

        Path path = new Path();
        // left eye
        path.reset();
//        Float left_middle_eye_eyebrow_y = (landmark_pt_y.get(21) - landmark_pt_y.get(31) )/2;
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(36) - 25f, landmark_pt_y.get(36) - 5f);

        path.cubicTo(
                //left_eye_top_left + a distance
                landmark_pt_x.get(37) + 1f, landmark_pt_y.get(37) - widthEyeShadow,
                //left_eye_top_right + a distance
                landmark_pt_x.get(38) + 1f, landmark_pt_y.get(38) - widthEyeShadow,
                //left_eye_right_corner
                landmark_pt_x.get(39) + 16f, landmark_pt_y.get(39) - 3f);

        path.lineTo(landmark_pt_x.get(39) + 14f, landmark_pt_y.get(39));

        path.cubicTo(
                //left_eye_top_left
                landmark_pt_x.get(38) + 1f, landmark_pt_y.get(38) - 10f,
                //left_eye_top_right
                landmark_pt_x.get(37) + 1f, landmark_pt_y.get(37) - 9f,
                //left_eye_left_corner
                landmark_pt_x.get(36), landmark_pt_y.get(36) - 9f);

        path.lineTo(landmark_pt_x.get(36) - 25f, landmark_pt_y.get(36) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // right eye
//        4 colors
//        LinearGradient linearGradient = new LinearGradient(0, 0, width, height,
//                new int[] {
//                        0xFF1e5799,
//                        0xFF207cca,
//                        0xFF2989d8,
//                        0xFF207cca }, //substitute the correct colors for these
//                new float[] {
//                        0, 0.40f, 0.60f, 1 },
//                Shader.TileMode.REPEAT);
        mPaint.setShader(new LinearGradient(
                landmark_pt_x.get(45), landmark_pt_y.get(45) - 15f,
                landmark_pt_x.get(42), landmark_pt_y.get(42) - 15f,
                color1,
                color1,
                Shader.TileMode.CLAMP));
        mPaint.setMaskFilter(new BlurMaskFilter(6, BlurMaskFilter.Blur.NORMAL));

        path.reset();
        //right_eye_right_corner
        path.moveTo(landmark_pt_x.get(45) + 25f, landmark_pt_y.get(45) - 5f);

        path.cubicTo(
                //right_eye_top_right + a distance
                landmark_pt_x.get(44) - 1f, landmark_pt_y.get(44) - widthEyeShadow,
                //right_eye_top_left + a distance
                landmark_pt_x.get(43) - 1f, landmark_pt_y.get(43) - widthEyeShadow,
                //right_eye_left_corner
                landmark_pt_x.get(42) - 16f, landmark_pt_y.get(42) - 3f);

        path.lineTo(landmark_pt_x.get(42) - 14f, landmark_pt_y.get(42));

        path.cubicTo(
                //right_eye_top_left
                landmark_pt_x.get(43) - 1f, landmark_pt_y.get(43) - 10f,
                //right_eye_top_right
                landmark_pt_x.get(44) - 1f, landmark_pt_y.get(44) - 9f,
                //right_eye_right_corner
                landmark_pt_x.get(45), landmark_pt_y.get(45) - 9f);

        path.lineTo(landmark_pt_x.get(45) + 25f, landmark_pt_y.get(45) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);
    }

    private void drawRouge(String blushColor, Bitmap getBitmap) {

        Canvas drawCanvas = new Canvas(getBitmap);
        Paint mPaint = new Paint();

        int rougeColor = stringColorRGBToARGB(blushColor, 35 + alphaValueRouge, 0, 0, 0);

        mPaint.setColor(rougeColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mPaint.setPathEffect(new CornerPathEffect(70));
        mPaint.setXfermode(mXfermode);
        mPaint.setStrokeWidth(1f);

        int sc = drawCanvas.saveLayer(0, 0, getBitmap.getWidth(), getBitmap.getHeight(), null, Canvas.ALL_SAVE_FLAG);
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
        //设置混合模式
        mPaint.setXfermode(mXfermode);
        //清除混合模式
        mPaint.setXfermode(null);
        //还原画布
        drawCanvas.restoreToCount(sc);
        drawCanvas.setBitmap(getBitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    class LoadingMakeupAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... position) {
            switch (categoryResult) {
                case 1:
                    temp = temp2.copy(temp2.getConfig(), true);
                    setupFoundation(colorArray.get(0), temp);
                    break;
                case 2:
                    temp = temp2.copy(temp2.getConfig(), true);
                    drawRouge(colorArray.get(0), temp);
                    break;
                case 3:
                    temp = temp2.copy(temp.getConfig(), true);
                    eyeshadowMethodSelect();
                    break;
                case 4:
                    temp = temp2.copy(temp2.getConfig(), true);
                    changeLipColor(colorArray.get(0), temp);
//                    lipLayer();
                    break;
            }
            return null;
        }

        private void eyeshadowMethodSelect() {
            if (colorArray.size() == 1) {
                switch (methodNumber) {
                    case 1:
                        drawEyeShadowWithFourColorMethod1(temp2);
                        break;
                    case 2:
                        drawEyeShadowWithTwoColorMethod1(temp2);
                        break;
                    case 3:
                        drawEyeShadowWithFourColorMethod2(temp2);
                        break;
                    case 4:
                        drawEyeShadowWithFourColorMethod4(temp2);
                        break;
                }
            } else if (colorArray.size() == 2) {
                switch (methodNumber) {
                    case 1:
                        drawEyeShadowWithTwoColorMethod1(temp2);
                        break;
                    case 2:
                        drawEyeShadowWithFourColorMethod2(temp2);
                        break;
                    case 3:
                        drawEyeShadowWithFourColorMethod3(temp2);
                        break;
                    case 4:
                        drawEyeShadowWithFourColorMethod4(temp2);
                        break;
                }
            } else if (colorArray.size() == 3) {
                switch (methodNumber) {
                    case 1:
                        drawEyeShadowWithFourColorMethod1(temp2);
                        break;
                    case 2:
                        drawEyeShadowWithFourColorMethod2(temp2);
                        break;
                    case 3:
                        drawEyeShadowWithFourColorMethod3(temp2);
                        break;
                    case 4:
                        drawEyeShadowWithFourColorMethod4(temp2);
                        break;
                }
            }
            if (colorArray.size() == 4) {
                switch (methodNumber) {
                    case 1:
                        drawEyeShadowWithFourColorMethod1(temp2);
                        break;
                    case 2:
                        drawEyeShadowWithFourColorMethod2(temp2);
                        break;
                    case 3:
                        drawEyeShadowWithFourColorMethod3(temp2);
                        break;
                    case 4:
                        drawEyeShadowWithFourColorMethod4(temp2);
                        break;
                }
            }

            Log.d(TAG, " method number : " + methodNumber + " data.size(): " + data.size());
            temp = temp2.copy(temp2.getConfig(), true);
            temp2 = basicImg.copy(temp.getConfig(), true);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SingleMakeupActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Please Wait..");
            Resources res = getResources();
            final String[] categoryArray = res.getStringArray(R.array.category_type_array);
            progressDialog.setMessage("Setting up " + categoryArray[categoryResult] + " ...");
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            imageView.setImageBitmap(temp);
        }
    }

    public class MultipleColorAdapter extends RecyclerView.Adapter<MultipleColorHolder> {

        private final static String TAG = " MakeupProductAdapter ";
        private final Context c;
        public int selectedColorItemPosition = 0;
        public ArrayList<ArrayList<String>> multipleColor = new ArrayList<>();


        public MultipleColorAdapter(Context c, ArrayList<ArrayList<String>> multipleColor) {
            this.c = c;
            this.multipleColor = multipleColor;
        }

        @Override
        public MultipleColorHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.makeup_product_color_layout, viewGroup, false);
            MultipleColorHolder viewHolder = new MultipleColorHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final MultipleColorHolder viewHolder, final int position) {
            Log.d(TAG + " onBindViewHolder position", position + "");
            Log.d(TAG + " data ", data.toString());
            for (int i = 0; i < data.get(position).size(); i++) {
                if (multipleColor.get(position).get(i) != null) {
                    final int sdk = android.os.Build.VERSION.SDK_INT;
                    if (position == selectedColorItemPosition) {
                        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            viewHolder.select_color_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.square_rounded_border));
                        } else {
                            viewHolder.select_color_layout.setBackground(getResources().getDrawable(R.drawable.square_rounded_border));
                        }
                    } else {
                        viewHolder.select_color_layout.setBackgroundResource(0);
                    }
                    Log.d(TAG + " onBindViewHolder", data.get(position).get(i).toString());
                    viewHolder.makeup_product_color_image[i].setColorFilter(Color.parseColor(data.get(position).get(i)));
                    viewHolder.makeup_product_color_image[i].setVisibility(View.VISIBLE);
                }
            }

            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedColorItemPosition = position;
                    notifyDataSetChanged();

                    Log.d(TAG, " selectedColorItemPosition :" + selectedColorItemPosition);

                    colorArray.clear();
                    colorArray = new ArrayList<>();
                    for (int i = 0; i < data.get(position).size(); i++)
                        colorArray.add(data.get(position).get(i));

                    if (categoryResult == 3) {
                        eyeshadow_method_layout.setVisibility(View.VISIBLE);
                    }

//                    drawEyeShadowWithOneColorMethod1(multipleColor);
                    Snackbar.make(v, "Click detected on item " + position + " : " + data.get(position).toString(),
                            Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

    }

    public class MultipleColorHolder extends RecyclerView.ViewHolder {

        private LinearLayout select_color_layout;
        View mView;
        public int currentItem;
        public CircleImageView[] makeup_product_color_image = new CircleImageView[8];
        public CardView makeup_product_color_card;

        public MultipleColorHolder(View itemView) {
            super(itemView);
            mView = itemView;
            makeup_product_color_card = (CardView) itemView.findViewById(R.id.makeup_product_color_card);
            makeup_product_color_card.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            select_color_layout = (LinearLayout) itemView.findViewById(R.id.select_color_layout);

            makeup_product_color_image[0] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image1);
            makeup_product_color_image[1] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image2);
            makeup_product_color_image[2] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image3);
            makeup_product_color_image[3] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image4);
            makeup_product_color_image[4] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image5);
            makeup_product_color_image[5] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image6);
            makeup_product_color_image[6] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image7);
            makeup_product_color_image[7] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image8);
        }
    }

    public class ColorRecyclerAdapter extends RecyclerView.Adapter<ColorRecyclerAdapter.ColorViewHolder> {

        public int selectedColorItemPosition = 0;

        class ColorViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout select_color_layout;
            View mView;
            public int currentItem;
            public de.hdodenhof.circleimageview.CircleImageView colorImage;

            public ColorViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
                colorImage = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image1);
                select_color_layout = (LinearLayout) itemView.findViewById(R.id.select_color_layout);
            }
        }

        @Override
        public ColorRecyclerAdapter.ColorViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.makeup_product_color_layout, viewGroup, false);
            ColorRecyclerAdapter.ColorViewHolder viewHolder = new ColorRecyclerAdapter.ColorViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ColorRecyclerAdapter.ColorViewHolder viewHolder, final int position) {
            viewHolder.colorImage.setColorFilter(Color.parseColor(colorArray.get(position)));
            final int sdk = android.os.Build.VERSION.SDK_INT;
            viewHolder.select_color_layout.setBackgroundResource(0);
            if (position == selectedColorItemPosition) {
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.select_color_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_border));
                } else {
                    viewHolder.select_color_layout.setBackground(getResources().getDrawable(R.drawable.circle_border));
                }
            }

            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedColorItemPosition = position;
                    viewHolder.select_color_layout.setBackgroundResource(0);
                    notifyDataSetChanged();
                    new LoadingMakeupAsyncTask().execute(new Integer(position));
                    Snackbar.make(v, "Click detected on item " + position + " : " + colorArray.get(position).toString(),
                            Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();

                    if (categoryResult == 2) {
                        rouge_alpha_select.setVisibility(View.VISIBLE);
                        alpha_seekBar.setProgress(10);
                    }
                    alpha_seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                            alphaValueRouge = progress;
                            Log.d(TAG, " " + progress + "/" + seekBar.getMax());
                            new LoadingMakeupAsyncTask().execute(new Integer(position));
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            Log.d(TAG + " product_size", colorArray.size() + "");
            return colorArray.size();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //back press
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
