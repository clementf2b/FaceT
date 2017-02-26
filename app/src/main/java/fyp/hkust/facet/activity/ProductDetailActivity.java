package fyp.hkust.facet.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Scroller;
import android.widget.TextView;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.vatsal.imagezoomer.ImageZoomButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.model.Product;
import fyp.hkust.facet.R;
import fyp.hkust.facet.model.User;
import fyp.hkust.facet.skincolordetection.ShowCameraViewActivity;
import fyp.hkust.facet.util.FontManager;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ProductDetailActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private final static String TAG = "ProductDetailActivity";
    public final int[] CUSTOM_COLOR = {Color.rgb(23, 188, 247), Color.rgb(57, 197, 193),
            Color.rgb(179, 225, 215), Color.rgb(255, 226, 210), Color.rgb(255, 174, 182)};

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseComments;

    private CircleImageView product_color_imageview_1;
    private ColorPickerDialog colorPickerDialog;
    private ExpandableTextView descTextview;

    private PieChart ratingPieChart;
    private HorizontalBarChart ratingChartBar;
    private Typeface fontType;

    private EmojiconEditText commentEmojiconEditText;
    private ImageView emojiButton;
    private ImageView submitCommentButton;
    private View commentView;
    private EmojIconActions emojIcon;
    private String product_id;
    private TextView product_name_text;
    private TextView brand_name_text;
    private Product backUp_product_data;
    private ImageZoomButton detail_product_image;
    private CircleImageView user_profile_pic;
    private ProgressBar loading_indicator;
    private ProgressDialog mProgress;
    private DatabaseReference mDatabaseCommentsCurrentProduct;
    private RatingBar user_rating_bar;
    private ImageButton location_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_product_detail_layout), fontType);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(ProductDetailActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        product_color_imageview_1 = (CircleImageView) findViewById(R.id.product_color_1);
        product_color_imageview_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerDialog = ColorPickerDialog.createColorPickerDialog(ProductDetailActivity.this);
                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color, String hexVal) {
                        System.out.println("Got color: " + color);
                        System.out.println("Got color in hex form: " + hexVal);
                        product_color_imageview_1.setColorFilter(Color.parseColor(hexVal));

                        // Make use of the picked color here
                    }
                });
                colorPickerDialog.show();
            }
        });

        detail_product_image = (ImageZoomButton) findViewById(R.id.detail_product_image);
        location_btn = (ImageButton) findViewById(R.id.location_btn);
        product_name_text = (TextView) findViewById(R.id.product_name_text);
        brand_name_text = (TextView) findViewById(R.id.brand_name_text);
        descTextview = (ExpandableTextView) findViewById(R.id.expand_text_view);
        descTextview.setText(getString(R.string.temp_description_label));

        user_profile_pic = (CircleImageView) findViewById(R.id.user_profile_pic);
        user_rating_bar = (RatingBar) findViewById(R.id.user_rating_bar);
        ratingPieChart = (PieChart) findViewById(R.id.rating_pie_chart);
        setEffectPieChart(ratingPieChart);
        setData(ratingPieChart, 4);

        ratingChartBar = (HorizontalBarChart) findViewById(R.id.rating_chart_bar);
        setEffectBarChart(ratingChartBar);
        ratingChartBar.setData(getBarData());

        commentView = findViewById(R.id.user_comment_edit_layout);
        emojiButton = (ImageView) findViewById(R.id.emoji_btn);
        submitCommentButton = (ImageView) findViewById(R.id.submit_btn);

        commentEmojiconEditText = (EmojiconEditText) findViewById(R.id.comment_edittext);
        commentEmojiconEditText.setScroller(new Scroller(getApplicationContext()));
        commentEmojiconEditText.setVerticalScrollBarEnabled(true);
        commentEmojiconEditText.setMovementMethod(new ScrollingMovementMethod());

        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationIntent = new Intent(ProductDetailActivity.this, ShopLocationActivity.class);
                locationIntent.putExtra("shop_id", "12345");
                startActivity(locationIntent);

            }
        });

        emojIcon = new EmojIconActions(this, commentView, commentEmojiconEditText, emojiButton);
        emojIcon.ShowEmojIcon();
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e("Keyboard", "open");
            }

            @Override
            public void onKeyboardClose() {
                Log.e("Keyboard", "close");
            }
        });

        submitCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upload the data to firebase
                uploadComment();
            }
        });

        product_id = getIntent().getExtras().getString("product_id");
        Log.d(TAG + " product_id", product_id);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        Log.d(TAG + "mDatabase", mDatabase.toString());
        mDatabase.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        Log.d(TAG + "mDatabaseUsers", mDatabaseUsers.toString());
        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDatabaseComments.keepSynced(true);
        Log.d(TAG + "mDatabaseComments", mDatabaseComments.toString());
        mDatabaseCommentsCurrentProduct = FirebaseDatabase.getInstance().getReference().child("Comments").child(product_id);
        mDatabaseCommentsCurrentProduct.keepSynced(true);
        Log.d(TAG + "mDatabaseCommentsCurrentProduct", mDatabaseCommentsCurrentProduct.toString());

        mDatabase.child(product_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.i("dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                    final Product product_data = dataSnapshot.getValue(Product.class);
                    backUp_product_data = product_data;
                    if (product_data.getTitle() != null)
                        product_name_text.setText(product_data.getTitle());
                    if (product_data.getBrand() != null)
                        brand_name_text.setText(product_data.getBrand());
                    if (product_data.getDesc() != null)
                        descTextview.setText(product_data.getDesc());
                    Picasso.with(getApplicationContext()).load(product_data.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(detail_product_image, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Display display = getWindowManager().getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            int width = size.x;
                            int height = size.y;
                            Picasso.with(getApplicationContext())
                                    .load(product_data.getImage())
                                    .into(detail_product_image);

                            detail_product_image.invalidate();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });

        // username change listener
        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.i("dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                    final User user_data = dataSnapshot.getValue(User.class);
                    Log.e(user_data.getName(), "User data is null!");
                    Picasso.with(getApplicationContext()).load(user_data.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(user_profile_pic, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext())
                                    .load(user_data.getImage())
                                    .fit()
                                    .centerCrop()
                                    .into(user_profile_pic);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read username value.", error.toException());
            }
        });

        checkUserExist();
    }

    private void uploadRating() {
        float rating = user_rating_bar.getRating();

        if (rating != 0.0f) {
            mProgress = new ProgressDialog(this);
            mProgress.setMessage("Uploading Rating ...");
            mProgress.show();

            DatabaseReference currentProductComment = mDatabaseComments.child(product_id).push();
            Log.d("currentProductComment", currentProductComment.toString());

            mProgress.dismiss();
        }
    }

    private void uploadComment() {
        String commentText = commentEmojiconEditText.getText().toString();

        if (!TextUtils.isEmpty(commentText)) {
            mProgress = new ProgressDialog(this);
            mProgress.setMessage("Uploading Comments ...");
            mProgress.show();

            DatabaseReference currentProductComment = mDatabaseComments.child(product_id).push();
            Log.d("currentProductComment", currentProductComment.toString());
            currentProductComment.child("comment_time").setValue(getCurrentTimeInString());
            currentProductComment.child("uid").setValue(mAuth.getCurrentUser().getUid());
            currentProductComment.child("comment").setValue(commentText);

            mProgress.dismiss();
        }
    }

    private String getCurrentTimeInString() {
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        Log.d(TAG + " current time", format.format(curDate));
        return format.format(curDate);
    }

    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            Log.d(TAG + "user_id", user_id);

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent mainIntent = new Intent(ProductDetailActivity.this, ProfileEditActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void setEffectPieChart(PieChart colorPie) {

        colorPie.animateXY(1500, 1500, Easing.EasingOption.EaseInBounce, Easing.EasingOption.EaseInBounce);
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
        colorPie.getLegend().setFormSize(10f);
        colorPie.getLegend().setFormToTextSpace(5f);

        //disable the label and description
        colorPie.getDescription().setEnabled(false);
        colorPie.getLegend().setEnabled(false);

        colorPie.setTransparentCircleColor(Color.WHITE);
        colorPie.setTransparentCircleAlpha(80);
        colorPie.setElevation(10f);
        colorPie.setHoleRadius(90f);
        colorPie.setTransparentCircleRadius(61f);

        // add a selection listener
        colorPie.setOnChartValueSelectedListener(this);

    }

    private void setData(PieChart colorPie, int value) {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        float result = value / 5f;
        entries.add(new PieEntry(result, 0));
        entries.add(new PieEntry(1 - result, 1));
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.

//        colorPie.setCenterTextTypeface(mTfLight);
        colorPie.setCenterTextColor(ColorTemplate.getHoloBlue());

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(3f);

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.argb(100, 0, 185, 245));
        colorPie.setCenterText(value + ".0");
        colorPie.setCenterTextSize(30);

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

    private void setEffectBarChart(HorizontalBarChart barChart) {

        barChart.animateXY(1500, 1500, Easing.EasingOption.EaseInBounce, Easing.EasingOption.EaseInBounce);

        barChart.setContentDescription("");
        barChart.setDragDecelerationFrictionCoef(0.5f);

        barChart.setHighlightPerTapEnabled(false);

        //disable the label and description
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        barChart.setElevation(10f);
        barChart.setFitBars(true);

        // enable touch gestures
        barChart.setTouchEnabled(false);

        // enable scaling and dragging
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);

        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisLeft().setDrawLabels(false); // no axis labels
        barChart.getAxisLeft().setDrawAxisLine(false); // no axis line
        barChart.getAxisLeft().setDrawGridLines(false); // no grid lines
//        barChart.getAxisLeft().setSpaceTop(0);
//        barChart.getAxisLeft().setSpaceBottom(0);
//        barChart.getAxisRight().setAxisMinimum(0);
        barChart.getXAxis().setEnabled(false);

        barChart.getAxisRight().setEnabled(false);

        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);

        barChart.highlightValues(null);
        barChart.getAxisRight().setAxisLineWidth(0);
        barChart.getAxisRight().setSpaceMax(0f);
        barChart.invalidate();
    }


    public BarData getBarData() {

        ArrayList<BarEntry> entries = new ArrayList<>();
        float overall_people = 100f;

        entries.add(new BarEntry(4, 36f));
        entries.add(new BarEntry(3, 24f));
        entries.add(new BarEntry(2, 25f));
        entries.add(new BarEntry(1, 5f));
        entries.add(new BarEntry(0, 10f));

        BarDataSet dataset = new BarDataSet(entries, "");
        dataset.setColors(CUSTOM_COLOR);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(dataset);

        BarData data = new BarData(dataSets);
//        data.setValueTextSize(10f);
//        data.setValueTypeface(fontType);
        data.setBarWidth(1f);

        return data;
    }
}
