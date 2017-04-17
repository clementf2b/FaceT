package fyp.hkust.facet.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import fyp.hkust.facet.R;
import fyp.hkust.facet.adapter.ArrayAdapterWithIcon;
import fyp.hkust.facet.notificationservice.MyService;
import fyp.hkust.facet.skincolordetection.CaptureActivity;
import fyp.hkust.facet.skincolordetection.ShowCameraViewActivity;
import fyp.hkust.facet.util.FontManager;

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = MainMenuActivity.class.getSimpleName();
    private static final int CAM_REQUEST = 3;
    // Storage Permissions
    private static String[] PERMISSIONS_REQ = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int REQUEST_CODE_PERMISSION = 2;
    private View activity_main_menu_layout;
    private ImageButton shoppingBtn;
    private ImageButton photoCameraBtn;
    private ImageButton accountBtn;
    private ImageButton storeBtn;

    private static final int GALLERY_REQUEST = 1;
    private static int OVERLAY_PERMISSION_REQ_CODE = 3;
    private ImageButton favBtn;
    private int buttonNumber = 0;
    private String captureImageFullPath = null;
    private boolean doubleBackToExitPressedOnce = false;
    private TextView main_menu_title;
    private RelativeLayout product_layout, account_layout, store_layout, fav_layout, photo_camera_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//        String strUserName = SP.getString("username", "NA");
//        Log.d(TAG + " username", strUserName);
        boolean notificationCheck = SP.getBoolean("notificationReceiveButton", true);
        Log.d(TAG + " notificationReceiveButton", notificationCheck + "");

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        activity_main_menu_layout = (RelativeLayout) findViewById(R.id.activity_main_menu_layout);
        photo_camera_layout = (RelativeLayout) findViewById(R.id.photo_camera_layout);
        fav_layout = (RelativeLayout) findViewById(R.id.fav_layout);
        store_layout = (RelativeLayout) findViewById(R.id.store_layout);
        account_layout = (RelativeLayout) findViewById(R.id.account_layout);
        product_layout = (RelativeLayout) findViewById(R.id.product_layout);

        Typeface titleFontType = FontManager.getTypeface(getApplicationContext(), FontManager.ROOT + FontManager.TITLE_FONT);
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_main_menu_layout), fontType);

        main_menu_title = (TextView) findViewById(R.id.main_menu_title);
        main_menu_title.setTypeface(titleFontType);
        photoCameraBtn = (ImageButton) findViewById(R.id.photo_camera);
        shoppingBtn = (ImageButton) findViewById(R.id.shopping_button);
        accountBtn = (ImageButton) findViewById(R.id.account_button);
        storeBtn = (ImageButton) findViewById(R.id.store_button);
        favBtn = (ImageButton) findViewById(R.id.fav_button);

        YoYo.with(Techniques.SlideInDown).duration(800).playOn(findViewById(R.id.photo_camera_layout));
        YoYo.with(Techniques.SlideInDown).duration(1300).playOn(findViewById(R.id.fav_layout));
        YoYo.with(Techniques.SlideInDown).duration(1800).playOn(findViewById(R.id.store_layout));
        YoYo.with(Techniques.SlideInDown).duration(2300).playOn(findViewById(R.id.account_layout));
        YoYo.with(Techniques.SlideInDown).duration(2800).playOn(findViewById(R.id.product_layout));

        photo_camera_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showAlertDialog();
            }
        });

        product_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(MainMenuActivity.this, MainActivity.class);
//                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(Intent);
            }
        });

        account_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent accountIntent = new Intent(MainMenuActivity.this, ProfileActivity.class);
//                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(accountIntent);

            }
        });

        fav_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMakeUpDialog();
            }
        });

        store_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent storeIntent = new Intent(MainMenuActivity.this, ShopListActivity.class);
//                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(storeIntent);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this.getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
        }

        if (!isMyServiceRunning(MyService.class) && notificationCheck != false)
            startService(new Intent(this, MyService.class));

        if (notificationCheck == false)
            stopService(new Intent(this, MyService.class));
    }

    @Override
    public void onBackPressed() {
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

    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose the way to get your selfie");

        builder.setIcon(R.mipmap.app_icon);
        builder.setCancelable(true);

        final String[] items = new String[]{"From Gallery", "Take Photo"};
        final Integer[] icons = new Integer[]{R.mipmap.app_icon, R.mipmap.app_icon};
        ListAdapter adapter = new ArrayAdapterWithIcon(getApplication(), items, icons);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: {
                        buttonNumber = 1;
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, GALLERY_REQUEST);
                        break;
                    }
                    case 1: {
                        Intent cameraViewIntent = new Intent(MainMenuActivity.this, ShowCameraViewActivity.class);
//                cameraViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(cameraViewIntent);
                        break;
                    }
                }

            }
        }).show();
    }

    private void showMakeUpDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose the way to get your selfie");

        builder.setIcon(R.mipmap.app_icon);
        builder.setCancelable(true);

        final String[] items = new String[]{"From Gallery", "Take Photo"};
        final Integer[] icons = new Integer[]{R.mipmap.app_icon, R.mipmap.app_icon};
        ListAdapter adapter = new ArrayAdapterWithIcon(getApplication(), items, icons);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: {
                        buttonNumber = 2;
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, GALLERY_REQUEST);
                        break;
                    }
                    case 1: {
                        Intent cameraViewIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File file = getFile();
                        cameraViewIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                        startActivityForResult(cameraViewIntent, CAM_REQUEST);
                        break;
                    }
                }

            }
        }).show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private File getFile() {

        File folder = new File("sdcard/FaceT");

        if (!folder.exists()) {
            folder.mkdir();
        }

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        captureImageFullPath = folder + "/makeup_" + currentDateTimeString;
        File imageFile = new File(captureImageFullPath);
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this.getApplicationContext())) {
                    Snackbar snackbar = Snackbar
                            .make(activity_main_menu_layout, "CameraActivity\", \"SYSTEM_ALERT_WINDOW, permission not granted...", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            }
        }

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri pickedImage = data.getData();
            Log.d(TAG, "selected!!!" + " : " + pickedImage.getPath());
            // Let's read picked image path using content resolver
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(pickedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Log.d(TAG + "Path:", picturePath);
            Intent intent = new Intent();
            if (buttonNumber == 1)
                intent.setClass(MainMenuActivity.this, CaptureActivity.class);
            else if (buttonNumber == 2)
                intent.setClass(MainMenuActivity.this, ColorizeFaceActivity.class);
            intent.putExtra("path", picturePath);
            //intent.putExtra("color" , "" + mBlobColorHsv);
            startActivity(intent);
        } else if (requestCode == CAM_REQUEST) {
            Intent intent = new Intent();
            intent.setClass(MainMenuActivity.this, ColorizeFaceActivity.class);
            intent.putExtra("path", captureImageFullPath);
            startActivity(intent);
        }

    }

    /**
     * Checks if the app has permission to write to device storage or open camera
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
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
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}

