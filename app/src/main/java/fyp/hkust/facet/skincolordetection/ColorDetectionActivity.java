package fyp.hkust.facet.skincolordetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.roger.gifloadinglibrary.GifLoadingView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fyp.hkust.facet.R;

import static android.graphics.Bitmap.createScaledBitmap;

public class ColorDetectionActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private static final String TAG = "ColorDetectionActivity";
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;

    private Bitmap originalBitmap;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private File mCascadeFile;
    private File                   mHaarCascadeEyeFile;
    private CascadeClassifier mJavaDetector;

    private CascadeClassifier      mJavaEyeDetector;

    private TextView color_result_text;
    private ImageView o_image, gray_image;

    private int scaledHeight = 0;
    private int scaledWidth = 0;

    private Bitmap scaledBitmap;
    private Bitmap convertedBitmap;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private boolean              mIsColorSelected = false;
    private Mat mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;

    private int face_middle_x;
    private int face_middle_y;

    int avg_cb = 120;//YCbCr顏色空間膚色cb的平均值
    int avg_cr = 155;//YCbCr顏色空間膚色cr的平均值
    int skinRange = 22;//YCbCr顏色空間膚色的範圍

    private GifLoadingView mColorDetectView;
    private int n;
    private Bitmap bmp;
    private Typeface mTfLight;
    protected static final int STOP = 0x10000;

    private PieChart rPieChart,gPieChart,bPieChart;

    public ColorDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_detection);

        // Load native library after(!) OpenCV initialization
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        }

        color_result_text = (TextView) findViewById(R.id.color_result_text);
        rPieChart = (PieChart) findViewById(R.id.r_chart);
        gPieChart = (PieChart) findViewById(R.id.g_chart);
        bPieChart = (PieChart) findViewById(R.id.b_chart);

        color_result_text.setVisibility(View.INVISIBLE);

//        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

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
            mJavaDetector.load( mCascadeFile.getAbsolutePath() );
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

        bmp = null;
        String filename = getIntent().getStringExtra("image");
        try {
            FileInputStream is = this.openFileInput(filename);
            bmp = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
//            originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test2);
            //get the image from intent
            Random rand = new Random();
            n = rand.nextInt(11) + 1;
            //12 is the maximum and the 1 is our minimum
            /*recycling unused objects in order to
            make the memory they currently occupy available for quick reuse.*/
            System.gc();
            int id = getResources().getIdentifier("num" + n, "drawable", getPackageName());
            mColorDetectView = new GifLoadingView();
            mColorDetectView.setBackgroundResource(id);
            mColorDetectView.show(getFragmentManager(), "");
            mColorDetectView.setCancelable(false);
            mColorDetectView.setDimming(true);
            mColorDetectView.setRadius(1);

            originalBitmap = bmp;
            changeDimensions();
            scaledBitmap = createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, false);
            convertedBitmap = scaledBitmap;
            gray_image = (ImageView) findViewById(R.id.gray_image);

            // o_image.setImageBitmap(scaledBitmap);
            gray_image.setImageBitmap(convertedBitmap);

            Mat demo = new Mat();
            Utils.bitmapToMat(convertedBitmap,demo);
            Mat gray_demo = new Mat();
            Imgproc.cvtColor(demo, gray_demo, Imgproc.COLOR_RGB2GRAY);

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
                Log.d(TAG," yo1");
            }
            else {
                Log.e(TAG, "Detection method is not selected!");
            }

            Log.d(TAG," yo2");
            Rect[] facesArray = faces.toArray();
            Log.d(TAG," yo3: " + facesArray.length);
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(demo, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
                face_middle_x = (int)facesArray[i].tl().x + facesArray[i].width/2;
                face_middle_y = (int)facesArray[i].tl().y + facesArray[i].height/2;
                Log.d("face middle : " ,face_middle_x +"," + face_middle_y);
                Log.d(TAG, "faces array " + String.valueOf(i));

            }

            // detect the skin color area and turn it into white
//        Mat ycbcr_image = new Mat();
//                    Imgproc.cvtColor(demo, ycbcr_image, Imgproc.COLOR_RGB2YCrCb);
//                    double[] ycbcr_colors;
//                    double[] black= {0,0,0};
//                    double[] white= {255,255,255};
//                    for( int i = 0; i < ycbcr_image.height(); i++ )
//                        for( int j = 0; j < ycbcr_image.rows(); j++ )
//                        {
//                            ycbcr_colors = ycbcr_image.get(i,j);
//                            if((ycbcr_colors[2] > avg_cb-skinRange && ycbcr_colors[2] < avg_cb+skinRange) &&
//                                    (ycbcr_colors[1]> avg_cr-skinRange && ycbcr_colors[1] < avg_cr+skinRange))
//                                ycbcr_image.put(i, j,white);
//                            else
//                                ycbcr_image.put(i, j, black);
//                        }
//
//        ycbcr_image.copyTo(demo);


            //blob detection
            mDetector = new ColorBlobDetector();
            mSpectrum = new Mat();
            mBlobColorRgba = new Scalar(255);
            mBlobColorHsv = new Scalar(255);
            SPECTRUM_SIZE = new Size(200, 64);
            CONTOUR_COLOR = new Scalar(255,0,0,255);

            int cols = demo.cols();
            int rows = demo.rows();

            int xOffset = (originalBitmap.getWidth() - cols) / 2;
            int yOffset = (originalBitmap.getHeight() - rows) / 2;

//        int x = (int)event.getX() - xOffset;
//        int y = (int)event.getY() - yOffset;

//the middle point of the face detection
            int x = (int)face_middle_x;
            int y = (int)face_middle_y;
            Imgproc.circle(demo,new org.opencv.core.Point(x,y),10,FACE_RECT_COLOR);
            Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
            Log.i(TAG, "Offset coordinates: (" + xOffset + ", " + yOffset + ")");

            Rect touchedRect = new Rect();

            touchedRect.x = (x>4) ? x-4 : 0;
            touchedRect.y = (y>4) ? y-4 : 0;

            touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
            touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

            Mat touchedRegionRgba = demo.submat(touchedRect);

            Mat touchedRegionHsv = new Mat();
            Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

            // Calculate average color of touched region
            mBlobColorHsv = Core.sumElems(touchedRegionHsv);
            int pointCount = touchedRect.width*touchedRect.height;
            for (int i = 0; i < mBlobColorHsv.val.length; i++)
                mBlobColorHsv.val[i] /= pointCount;

            //Add a Toast to display the HSV color
            Toast.makeText(this, "HSV = " + mBlobColorHsv , Toast.LENGTH_SHORT).show();
            mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

            //Add a Toast to display the HSV color
            Log.i(TAG, "HSV = " + mBlobColorHsv.val[0] + " , " + mBlobColorHsv.val[1] + " , "+ mBlobColorHsv.val[2] + "");
            // show the average color in textview
            color_result_text.setText("HSV = [ " + mBlobColorHsv.val[0] + " , " + mBlobColorHsv.val[1] + " , "+ mBlobColorHsv.val[2] + " ]");
            Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                    ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

            mDetector.setHsvColor(mBlobColorHsv);

            Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

            mIsColorSelected = true;

            touchedRegionRgba.release();
            touchedRegionHsv.release();

            if (mIsColorSelected) {
                mDetector.process(demo);
                List<MatOfPoint> contours = mDetector.getContours();
                Log.e(TAG, "Contours count: " + contours.size());
                Imgproc.drawContours(demo, contours, -1, CONTOUR_COLOR);

                Mat colorLabel = demo.submat(4, 68, 4, 68);
                colorLabel.setTo(mBlobColorRgba);

                Mat spectrumLabel = demo.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
                mSpectrum.copyTo(spectrumLabel);
            }
            Utils.matToBitmap(demo,convertedBitmap);
            gray_image.setImageBitmap(convertedBitmap);

            seteffect(rPieChart);
            seteffect(gPieChart);
            seteffect(bPieChart);

            setData(rPieChart,0,(int) mBlobColorRgba.val[0]);
            setData(gPieChart,1,(int) mBlobColorRgba.val[1]);
            setData(bPieChart,2,(int) mBlobColorRgba.val[2]);
            //end of the color detection and dismiss the progress bar
            mColorDetectView.dismiss();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void seteffect(PieChart colorPie) {

        colorPie.animateXY(2000, 2000 ,Easing.EasingOption.EaseInBounce, Easing.EasingOption.EaseInBounce);
        colorPie.invalidate();
        colorPie.setDrawHoleEnabled(true);
        colorPie.setExtraOffsets(10, 10, 10, 10);

        colorPie.setContentDescription("");
        colorPie.setDragDecelerationFrictionCoef(0.95f);
        colorPie.setDrawCenterText(true);

        colorPie.setRotationAngle(0);
        // enable rotation of the chart by touch
        colorPie.setRotationEnabled(true);
        colorPie.setHighlightPerTapEnabled(true);
        colorPie.setDrawHoleEnabled(true);
        colorPie.setHoleColor(Color.WHITE);

        //disable the label and description
        colorPie.getDescription().setEnabled(false);
        colorPie.getLegend().setEnabled(false);

        colorPie.setTransparentCircleColor(Color.WHITE);
        colorPie.setTransparentCircleAlpha(80);
        colorPie.setElevation(10f);
        colorPie.setHoleRadius(60f);
        colorPie.setTransparentCircleRadius(61f);

        // add a selection listener
        colorPie.setOnChartValueSelectedListener(this);

    }

    private void setData(PieChart colorPie,int order,int color) {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        float colorValue = color/255f;
        entries.add(new PieEntry(colorValue, 0));
        entries.add(new PieEntry(1-colorValue ,1));
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.

//        colorPie.setCenterTextTypeface(mTfLight);
        colorPie.setCenterTextColor(ColorTemplate.getHoloBlue());

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(3f);

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.clear();
        switch (order)
        {
            case 0:
                colors.add(Color.argb(100,color, 0, 0));
                colorPie.setCenterText("Red");
                break;
            case 1:
                colors.add(Color.argb(100,0,color, 0));
                colorPie.setCenterText("Green");
                break;
            case 2:
                colors.add(Color.argb(100,0, 0,color));
                colorPie.setCenterText("Blue");
                break;
        }
        colors.add(Color.argb(80, 214, 214, 214));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(0f);
        data.setValueTextColor(Color.WHITE);
        colorPie.setData(data);

        // undo all highlights
        colorPie.highlightValues(null);

        colorPie.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
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

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }
}
