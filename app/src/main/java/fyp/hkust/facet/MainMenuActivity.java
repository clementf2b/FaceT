package fyp.hkust.facet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainMenuActivity extends AppCompatActivity {

    private ImageButton shoppingBtn;
    private ImageButton photoCameraBtn;
    private ImageButton accountBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

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
