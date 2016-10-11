package fyp.hkust.facet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class CaptureActivity extends AppCompatActivity {

    private TextView colorresult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        Intent intent = this.getIntent();
        String path = intent.getStringExtra("path");
        String color = intent.getStringExtra("color");

        File f = new File(path);
        ImageView mImgView1 = (ImageView)findViewById(R.id.imageView);
        Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
        mImgView1.setImageBitmap(bmp);

        Log.d("path" , path);

        colorresult = (TextView) findViewById(R.id.textView2);
        colorresult.setText(color);
    }
}
