package fyp.hkust.facet.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
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
import com.google.android.gms.wallet.wobs.LabelValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.util.FontManager;

public class ProductDetailActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private final static String TAG = "ProductDetailActivity";
    private int DATA_COUNT = 5;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private CircleImageView product_color_imageview_1;
    private ColorPickerDialog colorPickerDialog;
    private ExpandableTextView descTextview;

    private PieChart ratingPieChart;
    private HorizontalBarChart ratingChartBar;
    private Typeface fontType;

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

        descTextview = (ExpandableTextView) findViewById(R.id.expand_text_view);
        descTextview.setText(getString(R.string.temp_description_label));

        ratingPieChart = (PieChart) findViewById(R.id.rating_pie_chart);
        seteffect(ratingPieChart);
        setData(ratingPieChart, 4);

//        ratingChartBar = (HorizontalBarChart) findViewById(R.id.rating_chart_bar);
//        ratingChartBar.setData(getBarData());

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        Log.d(TAG + "mDatabase", mDatabase.toString());
        mDatabase.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        Log.d(TAG + "mDatabaseUsers", mDatabaseUsers.toString());
        mDatabaseUsers.keepSynced(true);

        checkUserExist();
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

    private void seteffect(PieChart colorPie) {

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
        colorPie.setCenterTextSize(60);

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
        barChart.invalidate();
        barChart.setExtraOffsets(10, 10, 10, 10);

        barChart.setContentDescription("");
        barChart.setDragDecelerationFrictionCoef(0.95f);

        barChart.setHighlightPerTapEnabled(true);

        //disable the label and description
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        barChart.setElevation(10f);
    }


    public BarData getBarData() {

        ArrayList<BarEntry> entries = new ArrayList<>();
        float overall_people = 100f;

        entries.add(new BarEntry(36/overall_people, 0));
        entries.add(new BarEntry(24/overall_people, 1));
        entries.add(new BarEntry(15/overall_people , 2));
        entries.add(new BarEntry(10/overall_people , 3));
        entries.add(new BarEntry(15/overall_people , 4));

        ArrayList<String> labels = new ArrayList<>();
        labels.add("");
        labels.add("");
        labels.add("");
        labels.add("");
        labels.add("");

        BarDataSet dataset = new BarDataSet(entries,"");
        dataset.setColors(ColorTemplate.MATERIAL_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(dataset);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(fontType);
        data.setBarWidth(0.9f);

        return data;
    }
}
