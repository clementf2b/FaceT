package fyp.hkust.facet;

import android.os.Bundle;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
//AppCompatActivity
public class ShowCameraViewActivity extends Activity implements CvCameraViewListener2, View.OnTouchListener {

    private static final String    TAG                 = "AutoCam::MainActivity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemCameraId;
    private MenuItem               mItemExit;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private File                   mHaarCascadeEyeFile;
    private CascadeClassifier      mJavaDetector;

    private CascadeClassifier      mJavaEyeDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    //private CameraBridgeViewBase   mOpenCvCameraView;
    private CameraView            mOpenCvCameraView;

    public ShowCameraViewActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_show_camera_view);
        Log.i(TAG, "called onCreate");
        Camera.Size resolution = null;
        mOpenCvCameraView = (CameraView) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        //mOpenCvCameraView.setResolution(resolution);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex( CameraBridgeViewBase.CAMERA_ID_FRONT);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int i, int i1) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame cvCameraViewFrame) {

        mRgba = cvCameraViewFrame.rgba();
        mGray = cvCameraViewFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null) {
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

            }
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
            Log.d(TAG, "faces array " + String.valueOf(i));

        }
        if(facesArray.length > 0) {
            //Thread t = getBaseContext().getMainLooper().getThread();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_S");
                    String currentDateandTime = sdf.format(new Date());
                    String saveDir = Environment.getExternalStorageDirectory().getPath() + "/DCIM/OCV/FDSave";
                    File dirCheck = new File(saveDir);
                    if(!dirCheck.exists()) {
                        dirCheck.mkdirs();
                    }
                    String fileName = saveDir + "/" + currentDateandTime + ".jpg";
                    try {
                        mOpenCvCameraView.takePicture(fileName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            t.start();
        }

        return mRgba;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemCameraId = menu.add("Front");
        mItemExit = menu.add("Exit");
        mItemExit.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if(item == mItemCameraId) {
            changeCamera();
        } else if(item == mItemExit) {
            finish();
        }
        return true;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.i(TAG, "onTouch event");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateandTime = sdf.format(new Date());
        String saveDir = Environment.getExternalStorageDirectory().getPath() + "/DCIM/OCV/TouchSave";
        File dirCheck = new File(saveDir);
        if(!dirCheck.exists()) {
            dirCheck.mkdirs();
        }
        String fileName = saveDir + "/touch_picture_" + currentDateandTime + ".jpg";
        try {
            mOpenCvCameraView.takePicture(fileName);

            Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }


    private boolean usingFront = true;
    private void changeCamera() {
        try {
            mOpenCvCameraView.disableView();
            if(usingFront) {
                mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
                mItemCameraId.setTitle("Back");
            } else {
                mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
                mItemCameraId.setTitle("Front");
            }
            usingFront = !usingFront;
            mOpenCvCameraView.enableView();
            //onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //System.loadLibrary("detection_based_tracker");

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
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        cascadeDir.delete();

                        InputStream is2 = getResources().openRawResource(R.raw.haarcascade_eye);
                        File haarcascadeDir = getDir("haarcascade", Context.MODE_PRIVATE);
                        String xmlDFName = "haarcascade_eye.xml";
                        mHaarCascadeEyeFile = new File(haarcascadeDir, xmlDFName);
                        FileOutputStream os2 = new FileOutputStream(mHaarCascadeEyeFile);
                        byte[] buffer2 = new byte[4096];
                        int bytesRead2;
                        while((bytesRead2 = is2.read(buffer)) != -1) {
                            os2.write(buffer, 0, bytesRead2);
                        }
                        is2.close();
                        os2.close();
                        mJavaEyeDetector = new CascadeClassifier(mHaarCascadeEyeFile.getAbsolutePath());
                        if(mJavaEyeDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaEyeDetector = null;
                        } else {
                            Log.i(TAG, "Loaded cascade classifier from " + mHaarCascadeEyeFile.getAbsolutePath());
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(ShowCameraViewActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}
