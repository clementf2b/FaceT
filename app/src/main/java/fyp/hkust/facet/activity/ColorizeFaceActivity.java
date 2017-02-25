package fyp.hkust.facet.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.skincolordetection.CaptureActivity;
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

    private PinchImageView imageView = null;
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
    private int[][] color;
    private Intent intent;
    private HandlerThread mThread;
    private Handler mThreadHandler;
    private String path;
    private LinearLayout makeup_select_layout;


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

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float width = size.x;
        float height = size.y;

        final File f = new File(path);
//        originalBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());

        Luban.compress(getApplicationContext(), f)
                .putGear(Luban.CUSTOM_GEAR)
                .asObservable()                             // generate Observable
                .subscribe();      // subscribe the compress result

        originalImg = new Compressor.Builder(ColorizeFaceActivity.this)
                .setMaxWidth(width)
                .setMaxHeight(height)
                .setQuality(80)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .build()
                .compressToBitmap(f);

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

                if(checkExpend == true) {
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
                else if(checkExpend == false) {

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

        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(landmarkDetection);

        imageView = (PinchImageView) this.findViewById(R.id.imageView1);
        imageView.setImageBitmap(originalImg);

        drawbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                temp = originalImg;
                connectLipLine();
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
                temp = originalImg;
                addExtraPoint();
                detectRegion2();
            }
        });

    }

    //工作名稱 r1 的工作內容

    private Runnable landmarkDetection = new Runnable() {

        public void run() {
            // TODO Auto-generated method stub
            progressDialog = new ProgressDialog(ColorizeFaceActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Please Wait..");
            progressDialog.setMessage("Getting landmark result ...");
            progressDialog.show();

            FaceppDetect faceppDetect = new FaceppDetect();
            faceppDetect.setDetectCallback(new DetectCallback() {

                public void detectResult(JSONObject rst) {

                    Log.v(TAG, rst.toString());
                    //create a new canvas
                    Bitmap temp = Bitmap.createBitmap(originalImg.getWidth(), originalImg.getHeight(), originalImg.getConfig());
                    Canvas canvas = new Canvas(temp);
                    canvas.drawBitmap(temp, new Matrix(), null);

                    try {
                        //get 83 point
                        //get the landmark
                        JSONObject jsonObject = rst.getJSONArray("result").getJSONObject(0).getJSONObject("landmark");
                        //Log.v("landmark_result",jsonObject.);
                        float x, y;
                        Iterator<String> keys = jsonObject.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            landmark_pt_label.add(key);
                            Log.v("key", key);
                        }
                        // Get size and display.
                        final int count = landmark_pt_label.size();
                        Log.v("Count ", count + "");

                        // Loop through elements.
                        for (int i = 0; i < count; i++) {

                            x = (float) jsonObject.getJSONObject(landmark_pt_label.get(i)).getDouble("x");
                            y = (float) jsonObject.getJSONObject(landmark_pt_label.get(i)).getDouble("y");
                            // store the value of landmark result

                            x = x / 100 * originalImg.getWidth();
                            y = y / 100 * originalImg.getHeight();

                            landmark_pt_x.add(x);
                            landmark_pt_y.add(y);

                            Log.d(TAG + " i = " + i + " " + landmark_pt_label.get(i), "x : " + x + "  landmark_x: " + landmark_pt_x.get(i));
//                                drawpoint(x, y, bitmap, canvas);
                            j++;
                        }
                        j = 37;
                        //save new image
//                        originalImg = temp;

                        ColorizeFaceActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                //show the image
                                imageView.setImageBitmap(originalImg);
                                Log.d(TAG, "Finished " + count + " points!");
                                progressDialog.dismiss();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        ColorizeFaceActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Log.e(TAG, "Error.");
                            }
                        });
                    }
                }
            });
            faceppDetect.detect(originalImg);
        }
    };

    private void detectRegion2() {

        Bitmap bmpTemp = originalImg.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmpTemp, mRgbMat);
        Imgproc.cvtColor(mRgbMat, mHsvMat, Imgproc.COLOR_RGB2HSV_FULL, channelCount);
        for (int i = 0; i < extra_landmark_pt_x.size(); i++) {
            extra_landmark_pt_y.set(i, extra_landmark_pt_y.get(i) - 5);
            do {
//
                if (mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[0] > 0 &&
                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[0] < 45 &&
                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[1] > 0.23 * 255 &&
                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[1] < 0.68 * 255 &&
                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[2] > 0.25 * 255 &&
                        mHsvMat.get(Math.round(extra_landmark_pt_x.get(i)), Math.round(extra_landmark_pt_y.get(i)))[2] < 0.8 * 255
                        ) {
                    extra_landmark_pt_y.set(i, extra_landmark_pt_y.get(i) - 5);
                } else
                    break;
            } while (true);
        }

        Bitmap bitmap = Bitmap.createBitmap(originalImg.getWidth(), originalImg.getHeight(), originalImg.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, new Matrix(), null);

        Paint paint = new Paint();
//        int baseColor = 0x55D3996B;
//        paint.setColor(baseColor);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
        paint.setColor(Color.BLUE);
//        paint.setARGB(20,253,243,244);
        paint.setStrokeWidth(Math.max(originalImg.getWidth(), originalImg.getHeight()) / 100f);
        paint.setAntiAlias(true);
        paint.setDither(true);

        //设置混合模式

        int sc = canvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        Path path = new Path();

        // Left lower lips
        path.reset();
        path.moveTo(landmark_pt_x.get(1), landmark_pt_y.get(1));
        for (int i = 2; i < 10; i++)
            path.lineTo(landmark_pt_x.get(i), landmark_pt_y.get(i));
        path.lineTo(landmark_pt_x.get(0), landmark_pt_y.get(0));

        for (int i = 18; i > 9; i--)
            path.lineTo(landmark_pt_x.get(i), landmark_pt_y.get(i));

        for (int i = extra_landmark_pt_x.size() - 1; i >= 0; i--)
            path.lineTo(extra_landmark_pt_x.get(i), extra_landmark_pt_y.get(i));

//        for(int i = 0;i<extra_landmark_pt_x.size();i++)
//            canvas.drawCircle(extra_landmark_pt_x.get(i), extra_landmark_pt_y.get(i), 2, paint);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        //remove lip part
        path.reset();
        path.moveTo(landmark_pt_x.get(37), landmark_pt_y.get(37));

        path.cubicTo(landmark_pt_x.get(40), landmark_pt_y.get(40),
                landmark_pt_x.get(41), landmark_pt_y.get(41),
                landmark_pt_x.get(38), landmark_pt_y.get(38));
        path.cubicTo(
                landmark_pt_x.get(43), landmark_pt_y.get(43),
                landmark_pt_x.get(44), landmark_pt_y.get(44),
                landmark_pt_x.get(46), landmark_pt_y.get(46)
        );
        path.quadTo(
                landmark_pt_x.get(42), landmark_pt_y.get(42),
                landmark_pt_x.get(45), landmark_pt_y.get(45));
        path.quadTo(
                landmark_pt_x.get(39), landmark_pt_y.get(39),
                landmark_pt_x.get(37), landmark_pt_y.get(37));
        path.close();
        canvas.drawPath(path, paint);

        path.reset();
        path.moveTo(landmark_pt_x.get(37), landmark_pt_y.get(37));

        path.cubicTo(landmark_pt_x.get(49), landmark_pt_y.get(49),
                landmark_pt_x.get(48), landmark_pt_y.get(48),
                landmark_pt_x.get(54), landmark_pt_y.get(54));
        path.cubicTo(landmark_pt_x.get(51), landmark_pt_y.get(51),
                landmark_pt_x.get(52), landmark_pt_y.get(52),
                landmark_pt_x.get(46), landmark_pt_y.get(46));
        path.quadTo(
                landmark_pt_x.get(53), landmark_pt_y.get(53),
                landmark_pt_x.get(47), landmark_pt_y.get(47));
        path.quadTo(
                landmark_pt_x.get(50), landmark_pt_y.get(50),
                landmark_pt_x.get(37), landmark_pt_y.get(37));
        path.close();
        canvas.drawPath(path, paint);

        //left_eyebrow
        path.reset();
        path.moveTo(landmark_pt_x.get(33), landmark_pt_y.get(33));
        path.quadTo(
                landmark_pt_x.get(32), landmark_pt_y.get(32),
                landmark_pt_x.get(31), landmark_pt_y.get(31));
        path.quadTo(
                landmark_pt_x.get(30), landmark_pt_y.get(30),
                landmark_pt_x.get(29), landmark_pt_y.get(29));
        path.quadTo(
                landmark_pt_x.get(34), landmark_pt_y.get(34),
                landmark_pt_x.get(35), landmark_pt_y.get(35));
        path.quadTo(
                landmark_pt_x.get(36), landmark_pt_y.get(36),
                landmark_pt_x.get(33), landmark_pt_y.get(33));
        path.close();
        canvas.drawPath(path, paint);

        //right_eyebrow
        path.reset();
        path.moveTo(landmark_pt_x.get(75), landmark_pt_y.get(75));
        path.quadTo(
                landmark_pt_x.get(76), landmark_pt_y.get(76),
                landmark_pt_x.get(77), landmark_pt_y.get(77));
        path.quadTo(
                landmark_pt_x.get(78), landmark_pt_y.get(78),
                landmark_pt_x.get(79), landmark_pt_y.get(79));
        path.quadTo(
                landmark_pt_x.get(82), landmark_pt_y.get(82),
                landmark_pt_x.get(81), landmark_pt_y.get(81));
        path.quadTo(
                landmark_pt_x.get(80), landmark_pt_y.get(80),
                landmark_pt_x.get(75), landmark_pt_y.get(75));
        path.close();
        canvas.drawPath(path, paint);

        // left eye
        path.reset();
        //left_eye_left_corner
        path.moveTo(landmark_pt_x.get(21), landmark_pt_y.get(21));
        path.quadTo(
                //left_eye_lower_left_quarter
                landmark_pt_x.get(22), landmark_pt_y.get(22),
                //left_eye_bottom
                landmark_pt_x.get(19), landmark_pt_y.get(19));
        path.quadTo(
                //left_eye_lower_right_quarter
                landmark_pt_x.get(23), landmark_pt_y.get(23),
                //left_eye_right_corner
                landmark_pt_x.get(25), landmark_pt_y.get(25));
        path.quadTo(
                //left_eye_upper_right_quarter
                landmark_pt_x.get(28), landmark_pt_y.get(28),
                //left_eye_top
                landmark_pt_x.get(26), landmark_pt_y.get(26));
        path.quadTo(
                //left_eye_upper_left_quarter
                landmark_pt_x.get(27), landmark_pt_y.get(27),
                //left_eye_left_corner
                landmark_pt_x.get(21), landmark_pt_y.get(21));
        path.close();
        canvas.drawPath(path, paint);

        // right eye
        path.reset();
        //right_eye_left_corner
        path.moveTo(landmark_pt_x.get(67), landmark_pt_y.get(67));
        path.quadTo(
                //right_eye_lower_left_quarter
                landmark_pt_x.get(68), landmark_pt_y.get(67),
                //right_eye_bottom
                landmark_pt_x.get(65), landmark_pt_y.get(65));
        path.quadTo(
                //right_eye_lower_right_quarter
                landmark_pt_x.get(69), landmark_pt_y.get(69),
                //right_eye_right_corner
                landmark_pt_x.get(71), landmark_pt_y.get(71));
        path.quadTo(
                //right_eye_upper_right_quarter
                landmark_pt_x.get(74), landmark_pt_y.get(74),
                //right_eye_top
                landmark_pt_x.get(72), landmark_pt_y.get(72));
        path.quadTo(
                //right_eye_upper_left_quarter
                landmark_pt_x.get(73), landmark_pt_y.get(73),
                //right_eye_left_corner
                landmark_pt_x.get(67), landmark_pt_y.get(67));
        path.close();
        canvas.drawPath(path, paint);

        //src
        // 再绘制src源图
//        drawCanvas.drawBitmap(originalImg, 0, 0, mPaint);

        //清除混合模式
        paint.setXfermode(null);
        //还原画布
        canvas.restoreToCount(sc);

        //get the color of each pixel
        color = new int[bitmap.getWidth()][bitmap.getHeight()];
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                color[x][y] = originalImg.getPixel(x, y);
                if (bitmap.getPixel(x, y) == Color.BLUE) {
                    color[x][y] = originalImg.getPixel(x, y);
                    float[] hsv = new float[3];
                    Color.RGBToHSV(Color.red(color[x][y]), Color.green(color[x][y]), Color.blue(color[x][y]), hsv);
                    hsv[0] = 18;
                    color[x][y] = Color.HSVToColor(hsv);
                }
                bitmap.setPixel(x, y, color[x][y]);
            }
        }

        imageView.setImageBitmap(bitmap);
        Log.d("color", mHsvMat.get(Math.round(extra_landmark_pt_x.get(0)), Math.round(extra_landmark_pt_y.get(0)))[0] + " " +
                mHsvMat.get(Math.round(extra_landmark_pt_x.get(0)), Math.round(extra_landmark_pt_y.get(0)))[1] + " " +
                mHsvMat.get(Math.round(extra_landmark_pt_x.get(0)), Math.round(extra_landmark_pt_y.get(0)))[2]);

// HSV back to BGR
        Imgproc.cvtColor(mHsvMat, mRgbMat, Imgproc.COLOR_HSV2BGR, channelCount);

    }

    private void addExtraPoint() {
//        //contour_left1 i = 1
//        extra_landmark_pt_x.add(landmark_pt_x.get(1));
//        extra_landmark_pt_y.add(landmark_pt_y.get(1));
//        //contour_right1 i = 10
//        extra_landmark_pt_x.add(landmark_pt_x.get(10));
//        extra_landmark_pt_y.add(landmark_pt_y.get(10));
        // left_eyebrow_upper
        extra_landmark_pt_x.add(landmark_pt_x.get(34));
        extra_landmark_pt_y.add(landmark_pt_y.get(34));
        extra_landmark_pt_x.add(landmark_pt_x.get(35));
        extra_landmark_pt_y.add(landmark_pt_y.get(35));
        extra_landmark_pt_x.add(landmark_pt_x.get(36));
        extra_landmark_pt_y.add(landmark_pt_y.get(36));
        //right_eyebrow_upper
        extra_landmark_pt_x.add(landmark_pt_x.get(80));
        extra_landmark_pt_y.add(landmark_pt_y.get(80));
        extra_landmark_pt_x.add(landmark_pt_x.get(81));
        extra_landmark_pt_y.add(landmark_pt_y.get(81));
        extra_landmark_pt_x.add(landmark_pt_x.get(82));
        extra_landmark_pt_y.add(landmark_pt_y.get(82));

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
        int color1 = 0xCC6b4491;
        int color2 = 0xCC34ac32;
        int sc = drawCanvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);

        mPaint.setShader(new LinearGradient(landmark_pt_x.get(21) - 10f, landmark_pt_y.get(21), landmark_pt_x.get(25), landmark_pt_y.get(25), color1, color2, Shader.TileMode.CLAMP));
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
        path.moveTo(landmark_pt_x.get(21) - 10f, landmark_pt_y.get(21));

        path.quadTo(
                //left_eye_top + a distance
                landmark_pt_x.get(26), landmark_pt_y.get(26) - 30f,

                //left_eye_right_corner
                landmark_pt_x.get(25) + 5f, landmark_pt_y.get(25) - 5f);

        path.quadTo(
                //left_eye_upper_right_quarter
                landmark_pt_x.get(28), landmark_pt_y.get(28) - 5f,
                //left_eye_top
                landmark_pt_x.get(26), landmark_pt_y.get(26) - 2f);

        path.quadTo(
                //left_eye_upper_left_quarter
                landmark_pt_x.get(27), landmark_pt_y.get(27) - 5f,
                //left_eye_left_corner
                landmark_pt_x.get(21) - 2f, landmark_pt_y.get(21) - 5f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        // right eye
        mPaint.setShader(new LinearGradient(landmark_pt_x.get(71) + 10f, landmark_pt_y.get(71), landmark_pt_x.get(67), landmark_pt_y.get(67), color1, color2, Shader.TileMode.CLAMP));

        path.reset();
        //right_eye_right_corner
        path.moveTo(landmark_pt_x.get(71) + 10f, landmark_pt_y.get(71));

        path.quadTo(
                //right_eye_top + a distance
                landmark_pt_x.get(72), landmark_pt_y.get(72) - 30f,

                //right_eye_left_corner
                landmark_pt_x.get(67) - 5f, landmark_pt_y.get(67) - 5f);

        path.quadTo(
                //right_eye_upper_left_quarter
                landmark_pt_x.get(73), landmark_pt_y.get(73) - 5f,
                //right_eye_top
                landmark_pt_x.get(72), landmark_pt_y.get(72) - 2f);

        path.quadTo(
                //right_eye_upper_right_quarter
                landmark_pt_x.get(74), landmark_pt_y.get(74) - 5f,
                //right_eye_right_corner
                landmark_pt_x.get(71), landmark_pt_y.get(71) - 5f);

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

    private void drawRouge() {

        Canvas drawCanvas = new Canvas(temp);
        Paint mPaint = new Paint();

        int rougeColor = 0x10FA0005;
        mPaint.setColor(rougeColor);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(1f);
        int sc = drawCanvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        mPaint.setMaskFilter(new BlurMaskFilter(7, BlurMaskFilter.Blur.NORMAL));

        //nose_contour_left2 - contour_left3
        float rouge_left_x = landmark_pt_x.get(56) - (landmark_pt_x.get(56) - landmark_pt_x.get(3)) / 2f;
        float rouge_left_y = landmark_pt_y.get(56);
        float slope = ((landmark_pt_y.get(56) - landmark_pt_y.get(60)) / (landmark_pt_x.get(56) - landmark_pt_x.get(60)));
        rouge_left_y = ((rouge_left_x - landmark_pt_x.get(56)) * slope) + landmark_pt_x.get(56);
        drawCanvas.drawCircle(rouge_left_x, rouge_left_y, 30f, mPaint);
//        nose_contour_right2 - contour_right3
        float rouge_right_x = landmark_pt_x.get(60) + (landmark_pt_x.get(12) - landmark_pt_x.get(60)) / 2f;
        float rouge_right_y = landmark_pt_y.get(60);
        rouge_right_y = ((rouge_right_x - landmark_pt_x.get(60)) * slope) + landmark_pt_y.get(60);
        drawCanvas.drawCircle(rouge_right_x, rouge_right_y, 30f, mPaint);

        //设置混合模式
        mPaint.setXfermode(mXfermode);

        //清除混合模式
        mPaint.setXfermode(null);
        //还原画布
        drawCanvas.restoreToCount(sc);
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
        if (mThreadHandler != null) {
            mThreadHandler.removeCallbacks(landmarkDetection);
        }

        if (mThread != null) {
            mThread.quit();
        }
    }

    private class FaceppDetect {
        DetectCallback callback = null;

        public void setDetectCallback(DetectCallback detectCallback) {
            callback = detectCallback;
        }

        public void detect(final Bitmap image) {

            new Thread(new Runnable() {

                public void run() {
                    HttpRequests httpRequests = new HttpRequests(
                            getResources().getString(R.string.facepp_API_Key),
                            getResources().getString(R.string.facepp_API_Secret),
                            true,
                            false);
                    //Log.v(TAG, "image size : " + img.getWidth() + " " + img.getHeight());

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    float scale = Math.min(1, Math.min(600f / originalImg.getWidth(), 600f / originalImg.getHeight()));
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);

                    Bitmap imgSmall = Bitmap.createBitmap(originalImg, 0, 0, originalImg.getWidth(), originalImg.getHeight(), matrix, false);
                    //Log.v(TAG, "imgSmall size : " + imgSmall.getWidth() + " " + imgSmall.getHeight());

                    imgSmall.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] array = stream.toByteArray();

                    try {
                        //detect
                        JSONObject result = httpRequests.detectionDetect(new PostParameters().setImg(array));
                        getFaceid(result);
                        result = httpRequests.detectionLandmark(new PostParameters().setFaceId(face_id));
                        Log.d("json_result", result.toString());
                        //finished , then call the callback function
                        if (callback != null) {
                            callback.detectResult(result);
                        }
                    } catch (FaceppParseException e) {
                        e.printStackTrace();
                        ColorizeFaceActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Log.e(TAG, "Network error.");
                            }
                        });
                    }
                }
            }).start();

        }
    }


    interface DetectCallback {
        void detectResult(JSONObject rst);
    }
}
