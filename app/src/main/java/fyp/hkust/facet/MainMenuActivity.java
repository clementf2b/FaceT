package fyp.hkust.facet;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import fyp.hkust.facet.skincolordetection.ShowCameraViewActivity;

public class MainMenuActivity extends AppCompatActivity {

    private ImageButton shoppingBtn;
    private ImageButton photoCameraBtn;
    private ImageButton accountBtn;

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

        photoCameraBtn = (ImageButton)findViewById(R.id.photo_camera);
        shoppingBtn = (ImageButton)findViewById(R.id.shopping_button);
        accountBtn = (ImageButton) findViewById(R.id.account_button);

        photoCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cameraViewIntent = new Intent(MainMenuActivity.this,ShowCameraViewActivity.class);
                cameraViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(cameraViewIntent);
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

                Intent accountIntent = new Intent(MainMenuActivity.this,MainActivity.class);
                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(accountIntent);

            }
        });

    }
}
