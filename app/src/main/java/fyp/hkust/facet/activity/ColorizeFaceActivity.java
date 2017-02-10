package fyp.hkust.facet.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import fyp.hkust.facet.util.PinchImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.google.android.gms.plus.model.people.Person;
import com.melnykov.fab.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import fyp.hkust.facet.R;

/**
 * A simple demo, get a picture form your phone<br />
 * Use the facepp api to detect<br />
 * Find all face on the picture, and mark them out.
 *
 * @author moon5ckq
 */
public class ColorizeFaceActivity extends AppCompatActivity {

    final private static String TAG = "MainActivity";
    final private int PICTURE_CHOOSE = 1;

    private PinchImageView imageView = null;
    private Bitmap img = null;
    private Button buttonDetect = null;
    private FloatingActionButton drawbtn;
    private TextView textView = null;
    private String face_id = null;
    private List<String> landmark_pt_label = new ArrayList<>();
    private List<Float> landmark_pt_x = new ArrayList<>();
    private List<Float> landmark_pt_y = new ArrayList<>();
    private float[] temp_pt = new float[2];
    private int j = 0;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private ProgressDialog progressDialog;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorize_face);

        Button button = (Button) this.findViewById(R.id.button1);
        Button lankmark_button = (Button) this.findViewById(R.id.landmark);
        drawbtn = (FloatingActionButton) this.findViewById(R.id.drawbtn);

        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                //get a picture form your phone
                Toast.makeText(ColorizeFaceActivity.this, "Pick one image", Toast.LENGTH_SHORT).show();

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                verifyStoragePermissions(ColorizeFaceActivity.this);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICTURE_CHOOSE);
            }
        });

        textView = (TextView) this.findViewById(R.id.textView1);

        buttonDetect = (Button) this.findViewById(R.id.button2);
        buttonDetect.setVisibility(View.INVISIBLE);
        buttonDetect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {

                textView.setText("Waiting ...");

                progressDialog = new ProgressDialog(ColorizeFaceActivity.this);
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Please Wait..");
                progressDialog.setMessage("Getting landmark result ...");
                progressDialog.show();

                // To Dismiss progress dialog
                //progressDialog.dismiss();

                FaceppDetect faceppDetect = new FaceppDetect();
                faceppDetect.setDetectCallback(new DetectCallback() {

                    public void detectResult(JSONObject rst) {
                        Log.v(TAG, rst.toString());
                        //create a new canvas
                        Bitmap bitmap = Bitmap.createBitmap(img.getWidth(), img.getHeight(), img.getConfig());
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(img, new Matrix(), null);

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

                                x = x / 100 * img.getWidth();
                                y = y / 100 * img.getHeight();

                                landmark_pt_x.add(x);
                                landmark_pt_y.add(y);

                                Log.d(TAG + " " + landmark_pt_label.get(i), "x : " + x + "  landmark_x: " + landmark_pt_x.get(i));

                                drawpoint(x, y, bitmap, canvas);
                                j++;
                            }

                            j=37;
                            //save new image
                            img = bitmap;

                            ColorizeFaceActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    //show the image

                                    imageView.setImageBitmap(img);
                                    textView.setText("Finished " + count + " points!");

                                    progressDialog.dismiss();
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ColorizeFaceActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    textView.setText("Error.");
                                }
                            });
                        }
                    }
                });
                faceppDetect.detect(img);
            }
        });

        imageView = (PinchImageView) this.findViewById(R.id.imageView1);
        imageView.setImageBitmap(img);

        drawbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Canvas drawCanvas = new Canvas(img);
                Bitmap temp = img;
                connectLipLine(39, 41, temp, drawCanvas );
                //mouth_lower_lip_left_contour 1- 3 : 39 - 41
//                drawLocation(temp, drawCanvas);
                textView.setText("point " + j);
                j++;
                imageView.setImageBitmap(temp);

            }
        });


    }


    public void drawpoint(float x, float y, Bitmap bitmap, Canvas canvas) {

        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(Math.max(img.getWidth(), img.getHeight()) / 100f);
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
        paint.setStrokeWidth(Math.max(img.getWidth(), img.getHeight()) / 100f);
        canvas.drawCircle(landmark_pt_x.get(j), landmark_pt_y.get(j), 2, paint);
    }


    public void connectLipLine(int start, int end, Bitmap bitmap, Canvas canvas) {
        Paint mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeWidth(1f);
//        canvas.drawCircle(landmark_pt_x.get(start + 1), landmark_pt_y.get(start + 1), 2, mPaint);
//        canvas.drawCircle(landmark_pt.get(start+2)[0], landmark_pt.get(start+2)[1], 2, mPaint);

        Path path = new Path();

        // Left lower lips
                path.reset();
        path.moveTo(landmark_pt_x.get(37), landmark_pt_y.get(37));
        Log.d(TAG + " connectLipLine", landmark_pt_label.get(39) + " end: " + landmark_pt_label.get(41));
        path.cubicTo(landmark_pt_x.get(40), landmark_pt_y.get(40),
                landmark_pt_x.get(41), landmark_pt_y.get(41),
                // bottom lip point
                landmark_pt_x.get(38), landmark_pt_y.get(38));

        Log.d(TAG, landmark_pt_x.get(40) + " " + landmark_pt_y.get(40) + " : " + landmark_pt_x.get(41) + " " + landmark_pt_y.get(41));
        canvas.drawPath(path, mPaint);

        path.reset();
        // bottom right lip point
        path.moveTo(landmark_pt_x.get(46), landmark_pt_y.get(46));
        Log.d(TAG + " connectLipLine", landmark_pt_label.get(39) + " end: " + landmark_pt_label.get(41));
        // Left lower lips
        path.cubicTo(landmark_pt_x.get(43), landmark_pt_y.get(43),
                landmark_pt_x.get(44), landmark_pt_y.get(44),
                // bottom lip point
                landmark_pt_x.get(38), landmark_pt_y.get(38));
        canvas.drawPath(path, mPaint);

        path.reset();
        // top left lip point
        path.moveTo(landmark_pt_x.get(37), landmark_pt_y.get(37));
        Log.d(TAG + " connectLipLine", landmark_pt_label.get(39) + " end: " + landmark_pt_label.get(41));

        path.cubicTo(landmark_pt_x.get(49), landmark_pt_y.get(49),
                landmark_pt_x.get(48), landmark_pt_y.get(48),
        //top middle point
                landmark_pt_x.get(54), landmark_pt_y.get(54));

        canvas.drawPath(path, mPaint);

        path.reset();
         //top right lip point
        path.moveTo(landmark_pt_x.get(46), landmark_pt_y.get(46));
        Log.d(TAG + " connectLipLine", landmark_pt_label.get(39) + " end: " + landmark_pt_label.get(41));

        path.cubicTo(landmark_pt_x.get(52), landmark_pt_y.get(52),
                landmark_pt_x.get(51), landmark_pt_y.get(51),
                landmark_pt_x.get(54), landmark_pt_y.get(54));

        canvas.drawPath(path, mPaint);

        //inside lower
        //start from left
        path.reset();
        path.moveTo(landmark_pt_x.get(37), landmark_pt_y.get(37));
        path.quadTo(
                landmark_pt_x.get(39), landmark_pt_y.get(39),
                landmark_pt_x.get(45), landmark_pt_y.get(45));

        canvas.drawPath(path, mPaint);

        //inside lower
        //start from right
        path.reset();
        path.moveTo(landmark_pt_x.get(46), landmark_pt_y.get(46));
        path.quadTo(
                landmark_pt_x.get(42), landmark_pt_y.get(42),
                landmark_pt_x.get(45), landmark_pt_y.get(45));

        canvas.drawPath(path, mPaint);

        //inside upper
        //start from left
        path.reset();
        path.moveTo(landmark_pt_x.get(37), landmark_pt_y.get(37));
        path.quadTo(
                landmark_pt_x.get(50), landmark_pt_y.get(50),
                landmark_pt_x.get(47), landmark_pt_y.get(47));

        canvas.drawPath(path, mPaint);

        //inside upper
        //start from right
        path.reset();
        path.moveTo(landmark_pt_x.get(46), landmark_pt_y.get(46));
        path.quadTo(
                landmark_pt_x.get(53), landmark_pt_y.get(53),
                landmark_pt_x.get(47), landmark_pt_y.get(47));

        canvas.drawPath(path, mPaint);

    }

    public void getFaceid(JSONObject result) {
        try {
            face_id = result.getJSONArray("face").getJSONObject(0).getString("face_id");
        } catch (JSONException e) {
            e.printStackTrace();
            ColorizeFaceActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    textView.setText("Error.");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //the image picker callback
        if (requestCode == PICTURE_CHOOSE) {
            if (intent != null) {
                //The Android api ~~~
                //Log.d(TAG, "idButSelPic Photopicker: " + intent.getDataString());
                Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                String fileSrc = cursor.getString(idx);
                //Log.d(TAG, "Picture:" + fileSrc);

                //just read size
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                img = BitmapFactory.decodeFile(fileSrc, options);

                //scale size to read
                options.inSampleSize = Math.max(1, (int) Math.ceil(Math.max((double) options.outWidth / 1024f, (double) options.outHeight / 1024f)));
                options.inJustDecodeBounds = false;
                img = BitmapFactory.decodeFile(fileSrc, options);
                textView.setText("Clik Detect. ==>");

                imageView.setImageBitmap(img);
                buttonDetect.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "idButSelPic Photopicker canceled");
            }
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
                    float scale = Math.min(1, Math.min(600f / img.getWidth(), 600f / img.getHeight()));
                    Matrix matrix = new Matrix();
                    matrix.postScale(scale, scale);

                    Bitmap imgSmall = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, false);
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
                                textView.setText("Network error.");
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
