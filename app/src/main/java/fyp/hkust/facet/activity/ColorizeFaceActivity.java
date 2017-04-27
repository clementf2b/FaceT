package fyp.hkust.facet.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.swipe.library.Swipe;
import com.github.pwittchen.swipe.library.SwipeListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.taishi.flipprogressdialog.FlipProgressDialog;
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
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.fragment.ColorSelectFragment;
import fyp.hkust.facet.fragment.MakeupProductFragment;
import fyp.hkust.facet.model.ProductTypeTwo;
import fyp.hkust.facet.util.FontManager;
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
public class ColorizeFaceActivity extends AppCompatActivity implements ColorSelectFragment.OnDataPass {

    private static final String TAG = ColorizeFaceActivity.class.getSimpleName();

    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private Bitmap basicImg = null;
    private PinchImageView imageView = null;
    private Bitmap originalImg = null;
    private Bitmap temp = null, temp2 = null, bitmap = null;
    private ImageButton show_hide_layout_button;
    private Button foundation_button, eyeshadow_button, blush_button, lipstick_button;
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
    private PorterDuff.Mode mPorterDuffScreenMode = PorterDuff.Mode.SCREEN;
    private Xfermode mXferScreenmode = new PorterDuffXfermode(mPorterDuffScreenMode);
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //    private ProgressDialog progressDialog;
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
    private HandlerThread mThread;
    private Handler mThreadHandler;
    private int[] tempArray;
    private int[] bitmapArray;
    private int[] originalArray;

    private int newWidth;
    private int newHeight;
    FaceDet mFaceDet;
    private RelativeLayout foundation_layout, blush_layout, eyeshadow_layout, lipstick_layout;
    private RecyclerView makeup_product_list, makeup_color_list;
    private int categoryResult;
    private int stepCount = 0;
    private DatabaseReference mDatabase;
    private Map<String, ProductTypeTwo> mProducts = new HashMap<>();
    private Map<String, ProductTypeTwo> mSortedProducts = new HashMap<>();
    private ProductAdapter mProductAdapter;
    private Long colorNo;
    private String selectedProductID;
    private Button compare_button;
    private List<String> colorSet = new ArrayList<>();
    private Boolean firstTime = true;
    private String selectedFoundationID, selectedBrushID, selectedEyeshadowID, selectedLipstickID;
    private String selectedFoundationColor, selectedBrushColor, selectedLipstickColor;
    private int colorPosition;
    private ImageButton eyeshadow_method1, eyeshadow_method2, eyeshadow_method3, eyeshadow_method4;
    private LinearLayout eyeshadow_method_layout;
    // number,bitmap
    private Map<Integer, Bitmap> saveBitmap = new HashMap<>();
    // number,color position
    private Map<String, Integer> saveBitmapColorList = new HashMap<>();
    // number,(ID,category)
    private Map<Integer, List<String>> saveBitmapList = new HashMap<>();
    // category , number
    private Map<String, Integer> saveMakeupList = new HashMap<>();
    private int makeupCount = 0;
    private ImageButton back_button, undo_button, redo_button, save_button, apply_list_button;
    private RelativeLayout activity_colorize_face_layout;
    private ArrayList<String> savedEyeshadowColor = new ArrayList<>();
    private int methodNumber = 1;
    private boolean doubleBackToExitPressedOnce = false;
    private LinearLayout rouge_alpha_select;
    private SeekBar alpha_seekBar;
    private int alphaValueRouge = 10;
    private LinearLayout makeup_color_layout;
    private ImageButton makeup_color_arror_left, makeup_color_arror_right;
    private FlipProgressDialog fpd;

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

        basicImg = originalImg.copy(bitmapConfig, true);
        temp = originalImg.copy(bitmapConfig, true);
        bitmap = originalImg.copy(bitmapConfig, true);

        databaseGetData();
        setup();

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
                switch (stepCount) {
                    case 0:
                        // type select
                        makeup_select_layout.animate()
                                .translationY(0)
                                .alpha(1.0f)
                                .start();
                        makeup_select_layout.setVisibility(View.VISIBLE);
                        makeup_product_list.setVisibility(View.GONE);
                        makeup_color_layout.setVisibility(View.GONE);
                        eyeshadow_method_layout.setVisibility(View.GONE);
                        break;
                    case 1:
                        //product select
                        makeup_product_list.animate()
                                .translationY(0)
                                .alpha(1.0f)
                                .start();
                        makeup_select_layout.setVisibility(View.GONE);
                        makeup_product_list.setVisibility(View.VISIBLE);
                        makeup_color_layout.setVisibility(View.GONE);
                        eyeshadow_method_layout.setVisibility(View.GONE);
                        break;
                    case 2:
                        //color select layout
                        makeup_color_list.animate()
                                .translationY(0)
                                .alpha(1.0f)
                                .start();
                        makeup_select_layout.setVisibility(View.GONE);
                        makeup_product_list.setVisibility(View.GONE);
                        makeup_color_layout.setVisibility(View.VISIBLE);
                        if (categoryResult == 3)
                            eyeshadow_method_layout.setVisibility(View.VISIBLE);
                        else
                            eyeshadow_method_layout.setVisibility(View.GONE);
                        break;
                }

                Log.d(TAG, stepCount + "");
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
                        .start();
                makeup_select_layout.setVisibility(View.GONE);
                makeup_product_list.setVisibility(View.GONE);
                makeup_color_layout.setVisibility(View.GONE);
                eyeshadow_method_layout.setVisibility(View.GONE);
                rouge_alpha_select.setVisibility(View.GONE);
                show_hide_layout_button.setImageResource(R.mipmap.ic_expand_more_black_24dp);
                checkExpend = false;
            }
        });

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

        fpd = new FlipProgressDialog();
        List<Integer> imageList = new ArrayList<Integer>();
        imageList.add(R.drawable.app_icon_100);
        fpd.setImageList(imageList);                              // *Set a imageList* [Have to. Transparent background png recommended]
        fpd.setCanceledOnTouchOutside(false);// If true, the dialog will be dismissed when user touch outside of the dialog. If false, the dialog won't be dismissed.
        fpd.setImageMargin(10);
        fpd.setMinAlpha(1.0f);                                    // Set an alpha when flipping ratation start and end
        fpd.setMaxAlpha(1.0f);
        fpd.setDimAmount(80.0f);
        fpd.setOrientation("rotationY");                          // Set a flipping rotation
        fpd.setDuration(1500);
        fpd.setImageSize(170);
        fpd.setStartAngle(0.0f);                                  // Set an angle when flipping ratation start
        fpd.setEndAngle(360.0f);
        fpd.setBackgroundColor(Color.parseColor("#3b393d"));     // Set a background color of dialog
        fpd.setBackgroundAlpha(0.8f);
        fpd.setCornerRadius(10);
        fpd.show(getFragmentManager(),"Loading ...");

//        progressDialog = new ProgressDialog(ColorizeFaceActivity.this);
//        progressDialog.setCancelable(false);
//        progressDialog.setTitle("Please Wait..");
//        progressDialog.setMessage("Getting landmark result ...");
//        progressDialog.show();

        mThread = new HandlerThread("name");
        mThread.start();
        mThreadHandler = new Handler(mThread.getLooper());
        mThreadHandler.post(landmarkDetection);
    }

    private void setup() {
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.makeup_select_layout), fontType);
        activity_colorize_face_layout = (RelativeLayout) findViewById(R.id.activity_colorize_face_layout);
        //toolbar button
        back_button = (ImageButton) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        undo_button = (ImageButton) findViewById(R.id.undo_button);
        undo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (makeupCount - 1 > 0) {
                    makeupCount--;
                    temp = saveBitmap.get(makeupCount).copy(temp.getConfig(), true);
                    imageView.setImageBitmap(temp);
                    Log.d(TAG + " undo ", saveBitmap.size() + " " + makeupCount);
                }
            }
        });

        redo_button = (ImageButton) findViewById(R.id.redo_button);
        redo_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (makeupCount + 1 < saveBitmap.size()) {
                    makeupCount++;
                    temp = saveBitmap.get(makeupCount).copy(temp.getConfig(), true);
                    imageView.setImageBitmap(temp);
                    Log.d(TAG + " redo ", saveBitmap.size() + " " + makeupCount);
                }
            }
        });

        save_button = (ImageButton) findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View item = LayoutInflater.from(ColorizeFaceActivity.this).inflate(R.layout.save_image_layout, null);
                final EditText editText = (EditText) item.findViewById(R.id.save_edittext);
                String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
                editText.setText("MakeupImage_" + timeStamp);
                ImageView save_imageview = (ImageView) item.findViewById(R.id.save_imageview);
                save_imageview.setImageBitmap(temp);
                new AlertDialog.Builder(ColorizeFaceActivity.this)
                        .setTitle("Set Image Name")
                        .setView(item)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storeImage(temp, editText.getText().toString().trim());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        apply_list_button = (ImageButton) findViewById(R.id.apply_list_button);
        apply_list_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MakeupProductFragment multipleColorFragment = new MakeupProductFragment();//Get Fragment Instance
                Resources res = getResources();
                final String[] categoryArray = res.getStringArray(R.array.category_type_array);
                Bundle data = new Bundle();
                if (selectedFoundationID != null && saveBitmapColorList.get(categoryArray[1]) != null) {
                    data.putString("selectedFoundationID", selectedFoundationID);
                    data.putInt("foundationColorPosition", saveBitmapColorList.get(categoryArray[1]).intValue());
                }
                if (selectedBrushID != null && saveBitmapColorList.get(categoryArray[2]) != null) {
                    data.putString("selectedBrushID", selectedBrushID);
                    data.putInt("blushColorPosition", saveBitmapColorList.get(categoryArray[2]).intValue());
                    Log.d(TAG + "selectedBrushID", selectedBrushID + " " + saveBitmapColorList.get(categoryArray[2]).intValue());
                }
                if (selectedEyeshadowID != null && saveBitmapColorList.get(categoryArray[3]) != null) {
                    data.putString("selectedEyeshadowID", selectedEyeshadowID);
                    data.putInt("eyeshadowColorPosition", saveBitmapColorList.get(categoryArray[3]).intValue());
                }
                if (selectedLipstickID != null && saveBitmapColorList.get(categoryArray[4]) != null) {
                    data.putString("selectedLipstickID", selectedLipstickID);
                    data.putInt("lipstickColorPosition", saveBitmapColorList.get(categoryArray[4]).intValue());
                }
                data.putParcelable("temp", temp);
                data.putParcelable("basicImg", basicImg);
                multipleColorFragment.setArguments(data);//Finally set argument bundle to fragment
                final FragmentManager fm = getFragmentManager();
                multipleColorFragment.show(getFragmentManager(), "Apply Makeup List");
            }
        });
        //bottom
        makeup_color_list = (RecyclerView) findViewById(R.id.makeup_color_list);
        final LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        makeup_color_list.setLayoutManager(llm);
        makeup_color_list.setItemAnimator(new DefaultItemAnimator());

        makeup_color_layout = (LinearLayout) findViewById(R.id.makeup_color_layout);
        makeup_color_arror_left = (ImageButton) findViewById(R.id.makeup_color_arror_left);
        makeup_color_arror_right = (ImageButton) findViewById(R.id.makeup_color_arror_right);
        makeup_color_arror_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeup_color_list.scrollToPosition(0);
            }
        });
        makeup_color_arror_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeup_color_list.scrollToPosition(makeup_color_list.getAdapter().getItemCount() - 1);
            }
        });
        makeup_color_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
//                RecyclerView.canScrollVertically(1)的值表示是否能向下滚动，false表示已经滚动到底部
//                RecyclerView.canScrollVertically(-1)的值表示是否能向上滚动，false表示已经滚动到顶部
                if (!recyclerView.canScrollHorizontally(-1)) {
//                    Toast.makeText(ColorizeFaceActivity.this, "At the Left of the page", Toast.LENGTH_SHORT).show();
                    makeup_color_arror_left.setVisibility(View.INVISIBLE);
                } else if (recyclerView.canScrollHorizontally(-1))
                    makeup_color_arror_left.setVisibility(View.VISIBLE);

                if (!recyclerView.canScrollHorizontally(1)) {
//                    Toast.makeText(ColorizeFaceActivity.this, "At the bottom of the page", Toast.LENGTH_SHORT).show();
                    makeup_color_arror_right.setVisibility(View.INVISIBLE);
                } else if (recyclerView.canScrollHorizontally(1))
                    makeup_color_arror_right.setVisibility(View.VISIBLE);

                Log.d(TAG + " dx , dy ", dx + " , " + dy);
            }

        });

        makeup_product_list = (RecyclerView) findViewById(R.id.makeup_product_list);
        LinearLayoutManager llm2 = new LinearLayoutManager(this);
        llm2.setOrientation(LinearLayoutManager.HORIZONTAL);
        makeup_product_list.setLayoutManager(llm2);
        makeup_product_list.setItemAnimator(new DefaultItemAnimator());

        makeup_select_layout = (LinearLayout) findViewById(R.id.makeup_select_layout);
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

        foundation_button = (Button) this.findViewById(R.id.foundation_button);
        foundation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryResult = 1;
                setupProductAdapter();
                viewControl(makeup_product_list, makeup_color_layout, makeup_select_layout, 1);
                eyeshadow_method_layout.setVisibility(View.GONE);
            }
        });

        blush_button = (Button) this.findViewById(R.id.blush_button);
        blush_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryResult = 2;
                setupProductAdapter();
                viewControl(makeup_product_list, makeup_color_layout, makeup_select_layout, 1);
                eyeshadow_method_layout.setVisibility(View.GONE);
            }
        });

        eyeshadow_button = (Button) this.findViewById(R.id.eyeshadow_button);
        eyeshadow_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryResult = 3;
                setupProductAdapter();
                viewControl(makeup_product_list, makeup_color_layout, makeup_select_layout, 1);
                eyeshadow_method_layout.setVisibility(View.VISIBLE);
            }
        });

        lipstick_button = (Button) this.findViewById(R.id.lipstick_button);
        lipstick_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryResult = 4;
                setupProductAdapter();
                viewControl(makeup_product_list, makeup_color_layout, makeup_select_layout, 1);
                eyeshadow_method_layout.setVisibility(View.GONE);
            }
        });

        show_hide_layout_button = (ImageButton) this.findViewById(R.id.show_hide_layout_button);
        viewControl(makeup_select_layout, makeup_product_list, makeup_color_layout, 0);

        show_hide_layout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (stepCount) {
                    case 0:
                        // type select
                        viewControl(makeup_select_layout, makeup_product_list, makeup_color_layout, 0);
                        rouge_alpha_select.setVisibility(View.GONE);
                        break;
                    case 1:
                        //product select
                        viewControl(makeup_select_layout, makeup_product_list, makeup_color_layout, 0);
                        rouge_alpha_select.setVisibility(View.GONE);
                        break;
                    case 2:
                        //color select layout
                        viewControl(makeup_product_list, makeup_color_layout, makeup_select_layout, 1);
                        rouge_alpha_select.setVisibility(View.GONE);
                        break;
                }
            }
        });

    }

    private void viewControl(View view1, View view2, View view3, int step) {
        view1.setVisibility(View.VISIBLE);
        view2.setVisibility(View.GONE);
        view3.setVisibility(View.GONE);
        eyeshadow_method_layout.setVisibility(View.GONE);
        rouge_alpha_select.setVisibility(View.GONE);
        stepCount = step;
    }

    private void setupProductAdapter() {
        if (mProducts.size() > 0) {
            mSortedProducts = filterProduct(mProducts, categoryResult);

            mProductAdapter = new ProductAdapter(mSortedProducts, getApplicationContext());
            makeup_product_list.setAdapter(mProductAdapter);
            mProductAdapter.notifyDataSetChanged();
        }
    }

    private void databaseGetData() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabase.keepSynced(true);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ProductTypeTwo result = ds.getValue(ProductTypeTwo.class);
                    if (result.getValidate() == 1) {
                        mProducts.put(ds.getKey(), result);
                        Log.d(" product " + ds.getKey(), result.toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
                            dialog("Oops", "Your photo is not good enough to detect face.");

                        }
                    });
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(temp);
                        fpd.dismiss();
//                        progressDialog.dismiss();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    };

    protected void dialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ColorizeFaceActivity.this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setIcon(R.drawable.ic_error_black_24px);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ColorizeFaceActivity.this.finish();
            }
        });
        builder.create().show();
    }


    protected Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
        bm.recycle();
        return resizedBitmap;
    }

    private void lipLayer() {
        Canvas drawCanvas = new Canvas(temp);
        Paint mPaint = new Paint();
        mPaint.setXfermode(mXfermode);

        int rougeLayer = 0xEEFAFAFA;
        mPaint.setColor(rougeLayer);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        mPaint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        mPaint.setPathEffect(new CornerPathEffect(50));
        mPaint.setStrokeWidth(1f);

        int sc = drawCanvas.saveLayer(0, 0, temp.getWidth(), temp.getHeight(), null, Canvas.ALL_SAVE_FLAG);
        mPaint.setMaskFilter(new BlurMaskFilter(60f, BlurMaskFilter.Blur.OUTER));

        Path path = new Path();
        path.reset();
        path.moveTo(landmark_pt_x.get(48), landmark_pt_y.get(48));

        for (int i = 49; i < 55; i++)
            path.lineTo(landmark_pt_x.get(i), landmark_pt_y.get(i) + 2f);

        path.lineTo(landmark_pt_x.get(64), landmark_pt_y.get(64) - 2f);
        path.lineTo(landmark_pt_x.get(63), landmark_pt_y.get(63) - 2f);
        path.lineTo(landmark_pt_x.get(62), landmark_pt_y.get(62) - 2f);
        path.lineTo(landmark_pt_x.get(60), landmark_pt_y.get(60) - 2f);
        path.lineTo(landmark_pt_x.get(48), landmark_pt_y.get(48) - 2f);

        path.close();
        drawCanvas.drawPath(path, mPaint);

        path.reset();
        path.moveTo(landmark_pt_x.get(48), landmark_pt_y.get(48));
        path.lineTo(landmark_pt_x.get(59), landmark_pt_y.get(59) + 2f);
        path.lineTo(landmark_pt_x.get(58), landmark_pt_y.get(58) + 2f);
        path.lineTo(landmark_pt_x.get(57), landmark_pt_y.get(57) + 2f);
        path.lineTo(landmark_pt_x.get(56), landmark_pt_y.get(56) + 2f);
        path.lineTo(landmark_pt_x.get(55), landmark_pt_y.get(55) + 2f);
        path.lineTo(landmark_pt_x.get(54), landmark_pt_y.get(54) + 2f);

        for (int i = 64; i < 68; i++)
            path.lineTo(landmark_pt_x.get(i), landmark_pt_y.get(i) - 2f);

        path.lineTo(landmark_pt_x.get(60), landmark_pt_y.get(60) - 2f);
        path.lineTo(landmark_pt_x.get(48), landmark_pt_y.get(48) - 2f);

        path.close();
        drawCanvas.drawPath(path, mPaint);
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

    private void setupFoundation(String foundationColor, Bitmap getBitmap) {

        try {
            selectedFoundationColor = foundationColor;
            Log.d(TAG + " selectedFoundationColor ", selectedFoundationColor);
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
                            hsv[0] = (hsv[0] + foundationHSV[0]) / 2f;
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
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
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

    public void changeLipColor(String lipstickColor, Bitmap getBitmap) {

        selectedLipstickColor = lipstickColor;
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

        int size = savedEyeshadowColor.size();
        int color1 = 0, color2 = 0, color3 = 0, color4 = 0;
        if (size == 1) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 60, 60, 60);
            color3 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 60, 60, 60);
            mPaint.setMaskFilter(new BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL));
        } else if (size == 3) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(2), 0, 50, 50, 50);
            mPaint.setMaskFilter(new BlurMaskFilter(2.5f, BlurMaskFilter.Blur.NORMAL));
        }
        if (size == 4) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(3), 0, 0, 0, 0);
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

        int size = savedEyeshadowColor.size();
        int color1 = 0, color2 = 0, color3 = 0, color4 = 0;
        if (size == 1) {
            color2 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(0), -100, 30, 30, 30);
            color4 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
        } else if (size == 2) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
        } else if (size == 3) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
        } else if (size == 4) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(3), 0, 0, 0, 0);
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

        int size = savedEyeshadowColor.size();
        int color1 = 0, color2 = 0, color3 = 0, color4 = 0, shapeColor = 0;
        if (size == 2) {
            shapeColor = stringColorToARGB(savedEyeshadowColor.get(1), -60, 0, 0, 0);
            color1 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(0), 0, -40, -40, -40);
            color4 = stringColorToARGB(savedEyeshadowColor.get(0), 0, -20, -20, -20);
        }
        if (size == 3) {
            shapeColor = stringColorToARGB(savedEyeshadowColor.get(0), -60, 0, 0, 0);
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
        } else if (size == 4) {
            shapeColor = stringColorToARGB(savedEyeshadowColor.get(0), -120, 0, 0, 0);
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), -80, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), -80, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(2), -80, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(3), -80, 0, 0, 0);
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

        int size = savedEyeshadowColor.size();
        int color1 = 0, color2 = 0, color3 = 0, color4 = 0, shapeColor = 0;
        if (size == 1) {
            color2 = stringColorToARGB(savedEyeshadowColor.get(0), -150, 20, 20, 20);
            color3 = stringColorToARGB(savedEyeshadowColor.get(0), -160, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(0), -170, 0, 0, 0);
            mPaint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
        } else if (size == 2) {
            color2 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            shapeColor = stringColorToARGB(savedEyeshadowColor.get(1), -60, 0, 0, 0);
            mPaint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
        } else if (size == 3) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(2), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            shapeColor = stringColorToARGB(savedEyeshadowColor.get(0), -60, 0, 0, 0);
            mPaint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
        } else {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
            color3 = stringColorToARGB(savedEyeshadowColor.get(2), 0, 0, 0, 0);
            color4 = stringColorToARGB(savedEyeshadowColor.get(3), 0, 0, 0, 0);
            shapeColor = stringColorToARGB(savedEyeshadowColor.get(0), -50, 0, 0, 0);
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

        int size = savedEyeshadowColor.size();
        int color1 = 0, color2 = 0;
        if (size == 1) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 60, 60, 60);
        } else if (size == 2) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
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

        int size = savedEyeshadowColor.size();
        int color1 = 0, color2 = 0;
        if (size == 1) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
        } else if (size == 2) {
            color1 = stringColorToARGB(savedEyeshadowColor.get(0), 0, 0, 0, 0);
            color2 = stringColorToARGB(savedEyeshadowColor.get(1), 0, 0, 0, 0);
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

        selectedBrushColor = blushColor;
        Log.d(TAG + " selectedBlushColor ", selectedBrushColor);
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

    @Override
    protected void onPause() {
        super.onPause();
//        progressDialog.dismiss();
    }

    protected void onDestroy() {
        super.onDestroy();
//        progressDialog.dismiss();
    }


    private Map<String, ProductTypeTwo> filterProduct(Map<String, ProductTypeTwo> unsortMap, int categoryResult) {
        List<Map.Entry<String, ProductTypeTwo>> temp = new LinkedList<Map.Entry<String, ProductTypeTwo>>(unsortMap.entrySet());
        //compare temp
        List<Map.Entry<String, ProductTypeTwo>> temp2 = new LinkedList<Map.Entry<String, ProductTypeTwo>>(unsortMap.entrySet());

        int tempSize = temp2.size();
        List<String> removeList = new ArrayList<>();
        Resources res = getResources();
        final String[] categoryArray = res.getStringArray(R.array.category_type_array);

        if (categoryResult >= 0) {
            for (int i = 0; i < tempSize; i++) {
                if (!categoryArray[categoryResult].equals(temp2.get(i).getValue().getCategory())) {
                    Log.d(TAG + " remove : " + temp2.get(i).getKey(), categoryArray[categoryResult] + " : " + temp2.get(i).getValue().getCategory());
                    removeList.add(temp2.get(i).getKey());
                }
            }
        }
        Log.d("Filtered ", "Map");
        // Maintaining insertion order with the help of LinkedList
        Map<String, ProductTypeTwo> filteredMap = new LinkedHashMap<>();
        for (Map.Entry<String, ProductTypeTwo> entry : temp) {
            if (!removeList.contains(entry.getKey())) {
                filteredMap.put(entry.getKey(), entry.getValue());
                Log.d(entry.getKey(), entry.getValue().getProductName() + " : " + entry.getValue().getCategory());
            }
        }

        return filteredMap;
    }

    public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {

        private Map<String, ProductTypeTwo> mResultProducts = new HashMap<>();
        // Allows to remember the last item shown on screen
        private int lastPosition = -1;
        private Context context;

        public ProductAdapter(Map<String, ProductTypeTwo> mProducts, Context c) {
            this.context = c;
            this.mResultProducts = mProducts;
            notifyDataSetChanged();
        }

        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = getLayoutInflater().inflate(R.layout.makeup_product_row, parent, false);
            ProductViewHolder viewHolder = new ProductViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ProductViewHolder viewHolder, final int position) {
            List<ProductTypeTwo> values = new ArrayList<>(mResultProducts.values());
            final ProductTypeTwo model = values.get(position);
            final List<String> keys = new ArrayList<>(mResultProducts.keySet());
            final String product_id = keys.get(position);

            Log.d(TAG + " product_id", product_id);
            Log.d(TAG + " product name", model.getProductName());

            Log.d(TAG, "loading view " + position);

//            Log.d(TAG + " product id ", product_id);
            viewHolder.setProductName(model.getProductName());
            viewHolder.setImage(getApplicationContext(), model.getProductImage());
//            viewHolder.setUid(model.getUid());

            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Click product_id = " + product_id + " colorNo = " + colorNo);
                    colorNo = model.getColorNo();
                    selectedProductID = keys.get(position);
                    switch (categoryResult) {
                        case 1:
                            selectedFoundationID = product_id;
                            break;
                        case 2:
                            selectedBrushID = product_id;
                            break;
                        case 3:
                            selectedEyeshadowID = product_id;
                            break;
                        case 4:
                            selectedLipstickID = product_id;
                            break;
                    }
                    if (colorNo == 0 && categoryResult != 3) {
                        colorSet.clear();
                        for (int i = 0; i < mSortedProducts.get(selectedProductID).getColor().size(); i++) {
                            for (int j = 0; j < mSortedProducts.get(selectedProductID).getColor().get(i).size(); j++) {
                                colorSet.add(mSortedProducts.get(selectedProductID).getColor().get(i).get(j));
                            }
                        }
                        makeup_color_list.setAdapter(new ColorRecyclerAdapter());
                        mProductAdapter.notifyDataSetChanged();
                        viewControl(makeup_color_layout, makeup_select_layout, makeup_select_layout, 2);
                        Log.d(TAG, mSortedProducts.get(selectedProductID).getColor().toString());
                    } else {
                        makeup_color_list.setAdapter(new MultipleColorAdapter(ColorizeFaceActivity.this, model.getColor()));
                        viewControl(makeup_color_layout, makeup_select_layout, makeup_select_layout, 2);
                    }
                    makeup_color_layout.setVisibility(View.VISIBLE);
                    makeup_select_layout.setVisibility(View.GONE);
                    makeup_product_list.setVisibility(View.GONE);
                    eyeshadow_method_layout.setVisibility(View.GONE);
                    stepCount = 2;
                }
            });
            setAnimation(viewHolder.itemView, position);
            Log.d(TAG, "finish loading view");

        }

        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
                animation.setDuration(500);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return mResultProducts == null ? 0 : mResultProducts.size();
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private Typeface customTypeface = Typeface.createFromAsset(itemView.getContext().getAssets(), FontManager.APP_FONT);

        public ProductViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProductName(String productName) {
            TextView makeup_product_name = (TextView) mView.findViewById(R.id.makeup_product_name);
            makeup_product_name.setText(productName);
            makeup_product_name.setTypeface(customTypeface, Typeface.BOLD);
        }

        public void setImage(final Context ctx, final String image) {
            final ImageView makeup_product_image = (ImageView) mView.findViewById(R.id.makeup_product_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(makeup_product_image, new Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "image loading success !");
                }

                @Override
                public void onError() {
                    Log.d(TAG, "image loading error !");
                    Picasso.with(ctx)
                            .load(image)
                            .resize(100, 100)
                            .centerCrop()
                            .into(makeup_product_image);
                }
            });
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
            viewHolder.colorImage.setColorFilter(Color.parseColor(colorSet.get(position)));
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
//                    Snackbar.make(v, "Click detected on item " + position + " : " + colorSet.get(position).toString(),
//                            Snackbar.LENGTH_SHORT)
//                            .setAction("Action", null).show();

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
            Log.d(TAG + " product_size", colorSet.size() + "");
            return colorSet.size();
        }
    }

    private void errorDialogEvent(String title, String message) {
        new AlertDialog.Builder(ColorizeFaceActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    class LoadingMakeupAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... position) {
            switch (categoryResult) {
                case 1:
                    colorPosition = position[0];
                    setupFoundation(colorSet.get(position[0]), temp);
                    break;
                case 2:
                    colorPosition = position[0];
                    if (selectedFoundationColor != null)
                        setupFoundation(selectedFoundationColor, temp2);

                    if (selectedLipstickColor != null) {
                        changeLipColor(selectedLipstickColor, temp2);
//                        lipLayer();
                    }

                    if (savedEyeshadowColor != null)
                        eyeshadowMethodSelect();

                    drawRouge(colorSet.get(position[0]), temp);
                    break;
                case 3:
                    if (selectedFoundationColor != null)
                        setupFoundation(selectedFoundationColor, temp2);

                    if (selectedBrushColor != null)
                        drawRouge(selectedBrushColor, temp2);

                    if (selectedLipstickColor != null) {
                        changeLipColor(selectedLipstickColor, temp2);
//                        lipLayer();
                    }

                    eyeshadowMethodSelect();
                    break;
                case 4:
                    colorPosition = position[0];
                    changeLipColor(colorSet.get(position[0]), temp);
//                    lipLayer();
                    break;
            }
            return null;
        }

        private void eyeshadowMethodSelect() {
            if (savedEyeshadowColor.size() == 1) {
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
            } else if (savedEyeshadowColor.size() == 2) {
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
            } else if (savedEyeshadowColor.size() == 3) {
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
            if (savedEyeshadowColor.size() == 4) {
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

            Log.d(TAG, " method number : " + methodNumber + " selectedColor.size(): " + savedEyeshadowColor.size());
            temp = temp2.copy(temp2.getConfig(), true);
            temp2 = basicImg.copy(temp.getConfig(), true);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            fpd = new FlipProgressDialog();
            List<Integer> imageList = new ArrayList<Integer>();
            imageList.add(R.drawable.app_icon_100);
            fpd.setImageList(imageList);                              // *Set a imageList* [Have to. Transparent background png recommended]
            fpd.setCanceledOnTouchOutside(false);// If true, the dialog will be dismissed when user touch outside of the dialog. If false, the dialog won't be dismissed.
            fpd.setImageMargin(10);
            fpd.setMinAlpha(1.0f);                                    // Set an alpha when flipping ratation start and end
            fpd.setMaxAlpha(1.0f);
            fpd.setDimAmount(80.0f);
            fpd.setOrientation("rotationY");                          // Set a flipping rotation
            fpd.setDuration(1500);
            fpd.setImageSize(170);
            fpd.setStartAngle(0.0f);                                  // Set an angle when flipping ratation start
            fpd.setEndAngle(360.0f);
            fpd.setBackgroundColor(Color.parseColor("#3b393d"));     // Set a background color of dialog
            fpd.setBackgroundAlpha(0.8f);
            fpd.setCornerRadius(10);
            fpd.show(getFragmentManager(), "Loading ...");
//            progressDialog = new ProgressDialog(ColorizeFaceActivity.this);
//            progressDialog.setCancelable(false);
//            progressDialog.setTitle("Please Wait..");
//            Resources res = getResources();
//            final String[] categoryArray = res.getStringArray(R.array.category_type_array);
//            progressDialog.setMessage("Setting up " + categoryArray[categoryResult] + " ...");
//            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
//            progressDialog.dismiss();
            fpd.dismiss();
//             number,bitmap
//            private Map<Integer,Bitmap> saveBitmap = new HashMap<>();
//             number,(color,ID,category)
//            private Map<Integer,List<String>> saveBitmapList = new HashMap<>();
//             category , bitmap number
//            private Map<String,Integer> saveMakeupList = new HashMap<>();
            ++makeupCount;
            List<String> data = new ArrayList<>();
            data.clear();
            Resources res = getResources();
            final String[] categoryArray = res.getStringArray(R.array.category_type_array);
            saveBitmap.put(makeupCount, temp);
            data.add(categoryArray[categoryResult]);
            switch (categoryResult) {
                case 1:
                    data.add(selectedFoundationID);
                    saveMakeupList.put(categoryArray[categoryResult], makeupCount);
                    break;
                case 2:
                    data.add(selectedBrushID);
                    saveMakeupList.put(categoryArray[categoryResult], makeupCount);
                    break;
                case 3:
                    data.add(selectedEyeshadowID);
                    saveMakeupList.put(categoryArray[categoryResult], makeupCount);
                    break;
                case 4:
                    data.add(selectedLipstickID);
                    saveMakeupList.put(categoryArray[categoryResult], makeupCount);
                    break;
            }
            saveBitmapColorList.put(categoryArray[categoryResult], colorPosition);
            saveBitmapList.put(makeupCount, data);
            Log.d(TAG + " saveMakeupList.size : saveBitmap.size ", saveMakeupList.size() + " : " + saveBitmap.size());
            Log.d(TAG + " saveBitmapList.get(makeupCount) ", saveBitmapList.get(makeupCount).get(0) + " " + saveBitmapList.get(makeupCount).get(1));
            imageView.setImageBitmap(temp);

        }
    }

    public class MultipleColorAdapter extends RecyclerView.Adapter<MultipleColorHolder> {

        private final static String TAG = " MakeupProductAdapter ";
        private final Context c;
        public int selectedColorItemPosition = 0;
        public ArrayList<ArrayList<String>> colorArray;


        public MultipleColorAdapter(Context c, ArrayList<ArrayList<String>> colorArray) {
            this.c = c;
            this.colorArray = colorArray;
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
            Log.d(TAG + " data ", colorArray.toString());
            for (int i = 0; i < colorArray.get(position).size(); i++) {
                if (colorArray.get(position).get(i) != null) {
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
                    Log.d(TAG + " onBindViewHolder", colorArray.get(position).get(i).toString());
                    viewHolder.makeup_product_color_image[i].setColorFilter(Color.parseColor(colorArray.get(position).get(i)));
                    viewHolder.makeup_product_color_image[i].setVisibility(View.VISIBLE);
                }
            }
            if (colorArray != null && colorArray.get(0).size() == 1) {
                eyeshadow_method_layout.setBackgroundColor(Color.WHITE);
                eyeshadow_method1.setBackgroundResource(R.drawable.eyeshadow1c1);
                eyeshadow_method2.setBackgroundResource(R.drawable.eyeshadow1c2);
                eyeshadow_method3.setBackgroundResource(R.drawable.eyeshadow1c3);
                eyeshadow_method4.setBackgroundResource(R.drawable.eyeshadow1c4);
            } else if (colorArray != null && colorArray.get(0).size() == 2) {
                eyeshadow_method_layout.setBackgroundColor(Color.WHITE);
                eyeshadow_method1.setBackgroundResource(R.drawable.eyeshadow2c1);
                eyeshadow_method2.setBackgroundResource(R.drawable.eyeshadow2c2);
                eyeshadow_method3.setBackgroundResource(R.drawable.eyeshadow2c3);
                eyeshadow_method4.setBackgroundResource(R.drawable.eyeshadow2c4);
            } else if (colorArray != null && colorArray.get(0).size() == 3) {
                eyeshadow_method_layout.setBackgroundColor(Color.WHITE);
                eyeshadow_method1.setBackgroundResource(R.drawable.eyeshadow3c1);
                eyeshadow_method2.setBackgroundResource(R.drawable.eyeshadow3c2);
                eyeshadow_method3.setBackgroundResource(R.drawable.eyeshadow3c3);
                eyeshadow_method4.setBackgroundResource(R.drawable.eyeshadow3c4);
            } else if (colorArray != null && colorArray.get(0).size() == 4) {
                eyeshadow_method_layout.setBackgroundColor(Color.WHITE);
                eyeshadow_method1.setBackgroundResource(R.drawable.eyeshadow1s);
                eyeshadow_method2.setBackgroundResource(R.drawable.eyeshadow2s);
                eyeshadow_method3.setBackgroundResource(R.drawable.eyeshadow3s);
                eyeshadow_method4.setBackgroundResource(R.drawable.eyeshadow4s);
            }


            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedColorItemPosition = position;
                    viewHolder.select_color_layout.setBackgroundResource(0);
                    notifyDataSetChanged();

                    Log.d(TAG, " selectedColorItemPosition :" + selectedColorItemPosition);

                    savedEyeshadowColor.clear();
                    savedEyeshadowColor = new ArrayList<>();
                    for (int i = 0; i < colorArray.get(position).size(); i++)
                        savedEyeshadowColor.add(colorArray.get(position).get(i));

                    if (categoryResult == 3) {
                        eyeshadow_method_layout.setVisibility(View.VISIBLE);
                    }

//                    drawEyeShadowWithOneColorMethod1(multipleColor);
//                    Snackbar.make(v, "Click detected on item " + position + " : " + colorArray.get(position).toString(),
//                            Snackbar.LENGTH_SHORT)
//                            .setAction("Action", null).show();

                    //new一個intent物件，並指定Activity切換的class

//                    ColorSelectFragment colorSelectFragment  = new ColorSelectFragment();//Get Fragment Instance
//                    Bundle data = new Bundle();
//                    data.putSerializable("selectedColor", savedEyeshadowColor);
//                    colorSelectFragment .setArguments(data);//Finally set argument bundle to fragment
//                    final FragmentManager fm = getFragmentManager();
//                    colorSelectFragment .show(getFragmentManager(), "Select Color");
                }
            });

        }

        @Override
        public int getItemCount() {
            return colorArray.size();
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

    private void storeImage(Bitmap image, String imageName) {
        FileOutputStream fOut;
        try {
            File dir = new File("/sdcard/faceT/");
            if (!dir.exists()) {
                dir.mkdir();
            }
            String mImageName = imageName;
            String tmp = "/sdcard/faceT/" + mImageName + ".jpg";
            fOut = new FileOutputStream(tmp);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            Snackbar snackbar = Snackbar.make(activity_colorize_face_layout, "Save bitmap suceesfullly in " + tmp, Snackbar.LENGTH_SHORT);
            snackbar.show();
            try {
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataPass(List<String> data) {
        savedEyeshadowColor.clear();
        savedEyeshadowColor = new ArrayList<>();
        for (int i = 0; i < data.size(); i++)
            savedEyeshadowColor.add(data.get(i));
        Log.d("LOG", "hello " + savedEyeshadowColor.toString());
    }
}