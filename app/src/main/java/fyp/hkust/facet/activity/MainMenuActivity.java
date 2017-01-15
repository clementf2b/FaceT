package fyp.hkust.facet.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import fyp.hkust.facet.R;
import fyp.hkust.facet.skincolordetection.CaptureActivity;
import fyp.hkust.facet.skincolordetection.ShowCameraViewActivity;

public class MainMenuActivity extends AppCompatActivity {

    private static final String TAG = MainMenuActivity.class.getSimpleName();
    private ImageButton shoppingBtn;
    private ImageButton photoCameraBtn;
    private ImageButton accountBtn;
    private ImageButton storeBtn;

    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        photoCameraBtn = (ImageButton) findViewById(R.id.photo_camera);
        shoppingBtn = (ImageButton) findViewById(R.id.shopping_button);
        accountBtn = (ImageButton) findViewById(R.id.account_button);
        storeBtn = (ImageButton) findViewById(R.id.store_button);

        photoCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
                builder.setTitle("Choose the way to get your selfie");
                builder.setIcon(R.mipmap.app_icon);
                final String[] items = new String[]{"Photo Album", "Take Photo"};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainMenuActivity.this, items[which], Toast.LENGTH_SHORT).show();
                        switch(which)
                        {
                            case 0:
                            {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
                                break;
                            }
                            case 1:
                            {
                                Intent cameraViewIntent = new Intent(MainMenuActivity.this, ShowCameraViewActivity.class);
//                cameraViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(cameraViewIntent);
                                break;
                            }

                        }
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        });

        shoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        accountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent accountIntent = new Intent(MainMenuActivity.this, MainActivity.class);
//                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(accountIntent);

            }
        });

        storeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent storeIntent = new Intent(MainMenuActivity.this, SwapFaceActivity.class);
//                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(storeIntent);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
            Log.d(TAG + "Path:" , imagePath);
            Intent intent = new Intent();
            intent.setClass(MainMenuActivity.this,CaptureActivity.class);
            intent.putExtra("path",imagePath);
            //intent.putExtra("color" , "" + mBlobColorHsv);
            startActivity(intent);
        }
    }

}
