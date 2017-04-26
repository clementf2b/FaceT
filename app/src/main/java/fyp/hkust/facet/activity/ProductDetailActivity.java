package fyp.hkust.facet.activity;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koushikdutta.ion.Ion;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.vatsal.imagezoomer.ImageZoomButton;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.adapter.ArrayAdapterWithIcon;
import fyp.hkust.facet.fragment.MultipleColorFragment;
import fyp.hkust.facet.model.Brand;
import fyp.hkust.facet.model.Comment;
import fyp.hkust.facet.model.ProductTypeOne;
import fyp.hkust.facet.model.ProductTypeTwo;
import fyp.hkust.facet.model.User;
import fyp.hkust.facet.util.CheckConnectivity;
import fyp.hkust.facet.util.FontManager;
import fyp.hkust.facet.util.TypefaceSpan;

public class ProductDetailActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private final static String TAG = "ProductDetailActivity";
    private static final int CAM_REQUEST = 3;
    private static final int GALLERY_REQUEST = 1;
    private int GALLERY_REQUEST_2 = 2;
    public final int[] CUSTOM_COLOR = {Color.rgb(23, 188, 247), Color.rgb(57, 197, 193),
            Color.rgb(179, 225, 215), Color.rgb(255, 226, 210), Color.rgb(255, 174, 182)};

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseComments;
    private DatabaseReference mDatabaseRatings;
    private DatabaseReference mDatabaseBrand;
    private DatabaseReference mDatabaseCommentsCurrentProduct;
    private DatabaseReference mDatabaseFavourite;
    private DatabaseReference mDatabaseNotifications;

    private ColorPickerDialog colorPickerDialog;
    private ExpandableTextView descTextview;

    private PieChart ratingPieChart;
    private HorizontalBarChart ratingChartBar;
    private Typeface fontType;

    private LinearLayout activity_product_detail_layout;
    private EmojiPopup emojiPopup;
    private EmojiEditText commentEmojiconEditText;
    private ImageView emojiButton;
    private ImageView submitCommentButton;
    private View commentView;
    private String product_id;
    private String user_image_url;
    private TextView product_name_text;
    private TextView brand_name_text;
    private TextView category_type_name_text;
    private ImageZoomButton detail_product_image;
    private CircleImageView user_profile_pic;
    private ProgressDialog mProgress;
    private Map<String, Brand> mBrand = new HashMap<String, Brand>();
    private RatingBar user_rating_bar;
    private ImageButton location_btn;
    private Button submitRatingButton;
    private ImageView insert_image_btn;
    private TextView preview_textview;
    private ImageView image_preview;
    private String captureImageFullPath = null;
    private Uri mImageUri = null;
    private StorageReference mStorageProfileImage;
    private float averageRating = 0;
    private int ratingCount = 0;
    private TextView rating_population_number_text;
    private float[] barRatingCount = new float[5];
    private RatingBar top_rating_bar;
    private TextView rating_textview;
    private RecyclerView mCommentList;
    private String current_username = "";
    // Animation
    private Animation animFadein;
    private ProgressBar product_detail_loading_indicator;
    private LikeButton likeButton;
    private String product_owner_id;
    private Long colorNo;
    private ProductTypeOne product_data_one = null;
    private ProductTypeTwo product_data_two = null;
    private RecyclerView color_recycler_view;
    private TextView more_product_color_label;
    private ImageView delete_rating;
    private List<String> colorSet = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        activity_product_detail_layout = (LinearLayout) findViewById(R.id.activity_product_detail_layout);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CheckConnectivity check = new CheckConnectivity();
        Boolean conn = check.checkNow(this.getApplicationContext());
        if (conn == true) {
            //run your normal code path here
            Log.d(TAG, "Network connected");
        } else {
            //Send a warning message to the user
            Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "No Internet Access", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_product_detail_layout), fontType);
        // load the animation
        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);

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

        //initialize
        product_id = getIntent().getExtras().getString("product_id");
        colorNo = getIntent().getExtras().getLong("colorNo");
        Log.d(TAG + " product_id", product_id);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        Log.d(TAG + "mDatabase", mDatabase.toString());
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorageProfileImage = FirebaseStorage.getInstance().getReference().child("Comment_images");

        Log.d(TAG + "mDatabaseUsers", mDatabaseUsers.toString());
        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDatabaseComments.keepSynced(true);
        Log.d(TAG + "mDatabaseComments", mDatabaseComments.toString());
        mDatabaseRatings = FirebaseDatabase.getInstance().getReference().child("Ratings");
        mDatabaseRatings.keepSynced(true);
        Log.d(TAG + "mDatabaseRatings", mDatabaseRatings.toString());
        mDatabaseFavourite = FirebaseDatabase.getInstance().getReference().child("Favourite");
        mDatabaseFavourite.keepSynced(true);
        Log.d(TAG + "mDatabaseFavourite", mDatabaseFavourite.toString());
        mDatabaseNotifications = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mDatabaseNotifications.keepSynced(true);
        Log.d(TAG + "mDatabaseRatings", mDatabaseRatings.toString());

        mDatabaseCommentsCurrentProduct = FirebaseDatabase.getInstance().getReference().child("Comments").child(product_id);
        mDatabaseCommentsCurrentProduct.keepSynced(true);
        Log.d(TAG + "mDatabaseCommentsCurrentProduct", mDatabaseCommentsCurrentProduct.toString());
        mDatabaseBrand = FirebaseDatabase.getInstance().getReference().child("Brand");

//        product_color_imageview_1 = (CircleImageView) findViewById(R.id.product_color_1);
//        product_color_imageview_1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                colorPickerDialog = ColorPickerDialog.createColorPickerDialog(ProductDetailActivity.this);
//                colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
//                    @Override
//                    public void onColorPicked(int color, String hexVal) {
//                        System.out.println("Got color: " + color);
//                        System.out.println("Got color in hex form: " + hexVal);
//                        product_color_imageview_1.setColorFilter(Color.parseColor(hexVal));
//
//                        // Make use of the picked color here
//                    }
//                });
//                colorPickerDialog.show();
//            }
//        });

        likeButton = (LikeButton) findViewById(R.id.product_star_button);
        detail_product_image = (ImageZoomButton) findViewById(R.id.detail_product_image);
        product_detail_loading_indicator = (ProgressBar) findViewById(R.id.product_detail_loading_indicator);
        rating_textview = (TextView) findViewById(R.id.rating_textview);
        top_rating_bar = (RatingBar) findViewById(R.id.top_rating_bar);
        location_btn = (ImageButton) findViewById(R.id.location_btn);
        product_name_text = (TextView) findViewById(R.id.product_name_text);
        brand_name_text = (TextView) findViewById(R.id.brand_name_text);
        category_type_name_text = (TextView) findViewById(R.id.category_type_name_text);
        more_product_color_label = (TextView) findViewById(R.id.more_product_color_label);

        descTextview = (ExpandableTextView) findViewById(R.id.expand_text_view);
        descTextview.setText(getString(R.string.temp_description_label));

        delete_rating = (ImageView) findViewById(R.id.delete_rating);
        user_profile_pic = (CircleImageView) findViewById(R.id.user_profile_pic);
        user_rating_bar = (RatingBar) findViewById(R.id.user_rating_bar);
        submitRatingButton = (Button) findViewById(R.id.submit_rating_btn);

        delete_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle("Delete Comments?");
                builder.setMessage("Confirm delete");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseRatings.child(product_id).child(mAuth.getCurrentUser().getUid()).removeValue();
                        Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "Removed to Rating", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        Toast.makeText(getApplicationContext(), "pressed ok", Toast.LENGTH_SHORT).show();
                        //refresh
                        finish();
                        startActivity(getIntent());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "pressed cancel", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });

        ratingPieChart = (PieChart) findViewById(R.id.rating_pie_chart);
        setEffectPieChart(ratingPieChart);
        ratingChartBar = (HorizontalBarChart) findViewById(R.id.rating_chart_bar);
        setEffectBarChart(ratingChartBar);
        for (int i = 0; i < 5; i++)
            barRatingCount[i] = 0f;

        rating_population_number_text = (TextView) findViewById(R.id.rating_population_number_text);
        //set real stat data

        checkRatingOrNot();
        getAverageRating();
        ratingChartBar.setData(getBarData());
        setData(ratingPieChart, averageRating);
        rating_textview.setText(averageRating + "");
        top_rating_bar.setRating(averageRating);
        rating_population_number_text.setText(ratingCount + "");
        Log.d("rating_population_number_text.setText(ratingCount)", ratingCount + "");
        ratingChartBar.invalidate();
        ratingPieChart.invalidate();

        preview_textview = (TextView) findViewById(R.id.preview_textview);
        image_preview = (ImageView) findViewById(R.id.image_preview);
        insert_image_btn = (ImageView) findViewById(R.id.insert_image_btn);
        commentView = findViewById(R.id.user_comment_edit_layout);
        emojiButton = (ImageView) findViewById(R.id.emoji_btn);
        submitCommentButton = (ImageView) findViewById(R.id.submit_btn);

        commentEmojiconEditText = (EmojiEditText) findViewById(R.id.comment_edittext);
        commentEmojiconEditText.setScroller(new Scroller(getApplicationContext()));
        commentEmojiconEditText.setVerticalScrollBarEnabled(true);
        commentEmojiconEditText.setMovementMethod(new ScrollingMovementMethod());

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                emojiPopup.toggle();
            }
        });

        location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locationIntent = new Intent(ProductDetailActivity.this, NearbyLocationActivity.class);
                locationIntent.putExtra("shop_id", "12345");
                startActivity(locationIntent);

            }
        });

        submitRatingButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                uploadRating();
                addNotification("Rating");
            }
        });

        if (mAuth.getCurrentUser() != null) {
            mDatabaseFavourite.child(product_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getKey().contains(mAuth.getCurrentUser().getUid()))
                            likeButton.setLiked(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                addFavourite();
                likeButton.setLiked(true);
                addNotification("new Notification");
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                removeFavourite();
            }
        });

        insert_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailActivity.this);
                builder.setTitle("Choose the way to get the comment image");
                builder.setIcon(R.mipmap.app_icon);
                builder.setCancelable(true);
                final String[] items = new String[]{"Photo Album", "Take Photo", "Cancel"};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ProductDetailActivity.this, items[which], Toast.LENGTH_SHORT).show();
                        switch (which) {
                            case 0: {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_REQUEST);
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
                });
                builder.setCancelable(false);
                builder.show();
            }
        });

        submitCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //upload the data to firebase
                emojiPopup.dismiss();
                uploadComment();
            }
        });

        mCommentList = (RecyclerView) findViewById(R.id.product_comment_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCommentList.setLayoutManager(layoutManager);

        checkUserExist();

        ProgressDialog dialog = ProgressDialog.show(ProductDetailActivity.this,
                "Loading Product Data", "Please wait ...", true);

        mDatabase.child(product_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && colorNo == 0) {
                    Log.i("product type two dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                    Log.i("product type two", colorNo + "");
                    product_data_two = dataSnapshot.getValue(ProductTypeTwo.class);
                    if (product_data_two.getProductName() != null) {
                        product_name_text.setText(product_data_two.getProductName());
                        SpannableString s = new SpannableString(product_data_two.getProductName());
                        s.setSpan(new TypefaceSpan(ProductDetailActivity.this, FontManager.CUSTOM_FONT), 0, s.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        getSupportActionBar().setTitle(s);
                    }
                    if (product_data_two.getBrandID() != null)
                        brand_name_text.setText(product_data_two.getBrandID());
                    if (product_data_two.getDescription() != null)
                        descTextview.setText(product_data_two.getDescription());
                    if (product_data_two.getUid() != null)
                        product_owner_id = product_data_two.getUid();
                    if (product_data_two.getCategory() != null)
                        category_type_name_text.setText(product_data_two.getCategory());

                    if (product_data_two.getColor() != null) {
                        Log.d(TAG + " color", product_data_two.getColor().get(0).toString() + " : " + product_data_two.getColor().get(0).get(0));
                        for (int i = 0; i < product_data_two.getColor().size(); i++) {
                            for (int j = 0; j < product_data_two.getColor().get(i).size(); j++) {
                                colorSet.add(product_data_two.getColor().get(i).get(j));
                            }
                        }
                        Log.d(TAG, product_data_two.getColor().toString());

                        color_recycler_view = (RecyclerView) findViewById(R.id.color_recycler_view);
                        color_recycler_view.setLayoutManager(new GridLayoutManager(getApplicationContext(), 6));//这里用线性宫格显示 类似于grid view
                        color_recycler_view.setAdapter(new RecyclerAdapter());
                        color_recycler_view.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return true;
                            }
                        });

                    }

                    if (product_data_two.getProductImage() != null && product_data_two.getProductImage().length() > 0) {
                        product_detail_loading_indicator.setVisibility(View.VISIBLE);
                        Ion.with(detail_product_image)
                                .placeholder(R.mipmap.app_icon)
                                .error(R.mipmap.app_icon)
                                .animateIn(animFadein)
                                .load(product_data_two.getProductImage());
                    }
                } else if (dataSnapshot.getValue() != null && colorNo == 1) {
                    Log.i("product type two dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                    Log.i("product type two", colorNo + "");
                    product_data_two = dataSnapshot.getValue(ProductTypeTwo.class);
                    if (product_data_two.getProductName() != null) {
                        product_name_text.setText(product_data_two.getProductName());
                        SpannableString s = new SpannableString(product_data_two.getProductName());
                        s.setSpan(new TypefaceSpan(ProductDetailActivity.this, FontManager.CUSTOM_FONT), 0, s.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        getSupportActionBar().setTitle(s);
                    }
                    if (product_data_two.getBrandID() != null)
                        brand_name_text.setText(product_data_two.getBrandID());
                    if (product_data_two.getDescription() != null)
                        descTextview.setText(product_data_two.getDescription());
                    if (product_data_two.getUid() != null)
                        product_owner_id = product_data_two.getUid();
                    if (product_data_two.getCategory() != null)
                        category_type_name_text.setText(product_data_two.getCategory());

                    if (product_data_two.getColor() != null) {
                        Log.d(TAG + " color", product_data_two.getColor().get(0).toString() + " : " + product_data_two.getColor().get(0).get(0));
                        color_recycler_view = (RecyclerView) findViewById(R.id.color_recycler_view);
                        color_recycler_view.setLayoutManager(new GridLayoutManager(getApplicationContext(), 6));//这里用线性宫格显示 类似于grid view
                        color_recycler_view.setAdapter(new RecyclerAdapter());
                        color_recycler_view.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return true;
                            }
                        });
                        more_product_color_label.setVisibility(View.VISIBLE);
                        more_product_color_label.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MultipleColorFragment multipleColorFragment = new MultipleColorFragment();//Get Fragment Instance

                                Bundle data = new Bundle();//Use bundle to pass data
//                                for(int i = 0;i<product_data_two.getColor().size();i++) {
//                                    data.putStringArrayList(i + "", product_data_two.getColor().get(i));
//                                    Log.d(TAG + " send fragment data",data.toString());
//                                }
                                data.putSerializable("color", product_data_two.getColor());
                                data.putInt("size", product_data_two.getColor().size());
                                multipleColorFragment.setArguments(data);//Finally set argument bundle to fragment
                                final FragmentManager fm = getFragmentManager();
                                multipleColorFragment.show(getFragmentManager(), "Color Set");
                            }
                        });
                    }

                    if (product_data_two.getProductImage() != null && product_data_two.getProductImage().length() > 0) {
                        product_detail_loading_indicator.setVisibility(View.VISIBLE);
                        Ion.with(detail_product_image)
                                .placeholder(R.mipmap.app_icon)
                                .error(R.mipmap.app_icon)
                                .animateIn(animFadein)
                                .load(product_data_two.getProductImage());
                    }
                }

                mDatabaseBrand.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Brand result = ds.getValue(Brand.class);
                            mBrand.put(ds.getKey(), result);
                            Log.d(" brand " + ds.getKey(), result.toString());
                        }
                        if (product_data_one != null)
                            brand_name_text.setText(mBrand.get(product_data_one.getBrandID()).getBrand());
                        if (product_data_two != null)
                            brand_name_text.setText(mBrand.get(product_data_two.getBrandID()).getBrand());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });

        dialog.dismiss();

        // username change listener
        if (mAuth.getCurrentUser() != null) {
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Log.i("user dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                        final User user_data = dataSnapshot.getValue(User.class);
                        Log.e(user_data.getName(), "User data is null!");
                        current_username = user_data.getName();
                        user_image_url = user_data.getImage();
                        Log.d(TAG + " current user", current_username);
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
        } else {
            Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "Haven't logged in", Snackbar.LENGTH_SHORT);
            snackbar.show();
        }

        setUpEmojiPopup();
        checkUserExist();
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder {

            public int currentItem;
            public de.hdodenhof.circleimageview.CircleImageView colorImage;

            public ViewHolder(View itemView) {
                super(itemView);
                colorImage = (CircleImageView) itemView.findViewById(R.id.product_color_card);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();

                        Snackbar.make(v, "Click detected on item " + position,
                                Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.show_color_layout, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            if (colorNo == 0 && colorSet != null)
                viewHolder.colorImage.setColorFilter(Color.parseColor(colorSet.get(i)));
            else if (colorNo == 1 && product_data_two.getColor() != null)
                viewHolder.colorImage.setColorFilter(Color.parseColor(product_data_two.getColor().get(0).get(i)));
        }

        @Override
        public int getItemCount() {
            if (colorNo == 0) {
                Log.d(TAG + " product_data_one size", colorSet.size() + "");
                return colorSet.size();
            } else {
                Log.d(TAG + " product_data_two size", product_data_two.getColor().get(0).size() + " ");
                return product_data_two.getColor().get(0).size();
            }
        }
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(activity_product_detail_layout)
                .setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
                    @Override
                    public void onEmojiBackspaceClicked(final View v) {
                        emojiPopup.dismiss();
//                        Log.d(TAG, "Clicked on Backspace");
                    }
                })
                .setOnEmojiClickedListener(new OnEmojiClickedListener() {
                    @Override
                    public void onEmojiClicked(final Emoji emoji) {
                        Log.d(TAG, "Clicked on emoji");
                    }
                })
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        emojiButton.setBackground(ContextCompat.getDrawable(ProductDetailActivity.this, R.drawable.ic_keyboard));
                    }
                })
                .setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
                    @Override
                    public void onKeyboardOpen(final int keyBoardHeight) {
                        Log.d(TAG, "Opened soft keyboard");
                    }
                })
                .setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        emojiButton.setBackground(ContextCompat.getDrawable(ProductDetailActivity.this, R.drawable.emoji_one_category_people));
                    }
                })
                .setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
                    @Override
                    public void onKeyboardClose() {
                        emojiPopup.dismiss();
                        Log.d(TAG, "Closed soft keyboard");
                    }
                })
                .build(commentEmojiconEditText);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.product_detail_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //back press
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            case R.id.product_apply:
                Log.d(TAG, " product apply");
                showMakeUpDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showMakeUpDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose the way to get your selfie");

        builder.setIcon(R.drawable.app_icon_100);
        builder.setCancelable(true);

        final String[] items = new String[]{"From Gallery", "Take Photo"};
        final Integer[] icons = new Integer[]{R.drawable.colorful_gallery, R.drawable.colorful_camera};
        ListAdapter adapter = new ArrayAdapterWithIcon(getApplication(), items, icons);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: {
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, GALLERY_REQUEST_2);
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

    private void addNotification(String action) {

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Adding notification ...");
        mProgress.show();

        final DatabaseReference currentNotification = mDatabaseNotifications.child(product_owner_id).push();
        currentNotification.child("action").setValue(action);
        currentNotification.child("product_id").setValue(product_id);
        currentNotification.child("product_image").setValue(product_data_two.getProductImage());
        currentNotification.child("product_name").setValue(product_data_two.getProductName());
        currentNotification.child("colorNo").setValue(colorNo);
        currentNotification.child("sender_user_id").setValue(mAuth.getCurrentUser().getUid());
        currentNotification.child("sender_username").setValue(mAuth.getCurrentUser().getUid());
        currentNotification.child("sender_image").setValue(user_image_url);
        currentNotification.child("time").setValue(getCurrentTimeInString());

        mProgress.dismiss();

    }

    private void addFavourite() {

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Adding to favourite ...");
        mProgress.show();

        mDatabaseFavourite.child(product_id).child(mAuth.getCurrentUser().getUid()).child("time").setValue(getCurrentTimeInString());
        mDatabaseFavourite.child(product_id).child(mAuth.getCurrentUser().getUid()).child("colorNo").setValue(colorNo);

        Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "Added to favourite", Snackbar.LENGTH_LONG);
        snackbar.show();

        mProgress.dismiss();
    }

    private void removeFavourite() {

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Removing to favourite ...");
        mProgress.show();

        mDatabaseFavourite.child(product_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey() == mAuth.getCurrentUser().getUid()) {
                        mDatabaseFavourite.child(product_id).child(ds.getKey()).removeValue();
                        Log.d(" remove favourite " + ds.getKey(), mAuth.getCurrentUser().getUid());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "Removed to favourite", Snackbar.LENGTH_LONG);
        snackbar.show();

        mProgress.dismiss();
    }


    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<Comment, CommentViewHolder> firebaseCommentRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(
                Comment.class,
                R.layout.comment_list_row,
                CommentViewHolder.class,
                mDatabaseComments.child(product_id)

        ) {
            @Override
            protected void populateViewHolder(CommentViewHolder viewHolder, final Comment model, int position) {

                Log.d(TAG, "loading view " + position);
                final String comment_id = getRef(position).getKey();
                final String user_id = model.getUid();
                viewHolder.setCommentTime(model.getComment_time());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                if (model.getUid_image() != null)
                    viewHolder.setUserImage(getApplicationContext(), model.getUid_image());
                if (model.getComment_image() != null)
                    viewHolder.setCommentImage(getApplicationContext(), model.getComment_image());

                viewHolder.user_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(ProductDetailActivity.this, OtheruserProfileActivity.class);
                        intent.putExtra("user_id", user_id);
                        startActivity(intent);
                    }
                });

                viewHolder.comment_username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(ProductDetailActivity.this, OtheruserProfileActivity.class);
                        intent.putExtra("user_id", user_id);//可放所有基本類別
                        startActivity(intent);
                    }
                });

                if (mAuth.getCurrentUser() != null) {
                    if (mAuth.getCurrentUser().getUid().equals(user_id)) {
                        viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                                builder.setTitle("Delete Comments?");
                                builder.setMessage("Confirm delete");
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mDatabaseComments.child(product_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue() != null) {
                                                    Map<String, Comment> temp = (HashMap<String, Comment>) dataSnapshot.getValue();
                                                    for (String key : temp.keySet()) {
                                                        if (key == comment_id) {
                                                            mDatabaseComments.child(product_id).child(comment_id).removeValue();
                                                            Log.d(" remove comment " + comment_id, key);
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
//                                        Toast.makeText(getApplicationContext(), "您按下OK按鈕", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                //設定Negative按鈕資料
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //按下按鈕時顯示快顯
//                                        Toast.makeText(getApplicationContext(), "您按下Cancel按鈕", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                builder.show();
                                return true;
                            }
                        });
                    }

//                    Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "Loaded comments successful.", Snackbar.LENGTH_LONG);
//                    snackbar.show();
                }
            }
        };

        mCommentList.setAdapter(firebaseCommentRecyclerAdapter);
        mCommentList.setFocusable(false);
        mCommentList.setNestedScrollingEnabled(false);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private Typeface customTypeface = Typeface.createFromAsset(itemView.getContext().getAssets(), FontManager.APP_FONT);
        TextView comment_username;
        CircleImageView user_image;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            comment_username = (TextView) mView.findViewById(R.id.comment_username);
            user_image = (CircleImageView) mView.findViewById(R.id.comment_profilepic);
        }

        public void setComment(String comment) {
            EmojiTextView emojicon_text_view = (EmojiTextView) mView.findViewById(R.id.emojicon_text_view);
            emojicon_text_view.setText(comment);
            emojicon_text_view.setTypeface(customTypeface);
        }

        public void setCommentTime(String date) {
            TextView comment_time = (TextView) mView.findViewById(R.id.comment_date);
            comment_time.setText(date);
            comment_time.setTypeface(customTypeface);
        }

        public void setUsername(String username) {
            comment_username.setText(username);
            comment_username.setTypeface(customTypeface);
        }

        public void setUserImage(final Context ctx, final String userImage) {
            if (userImage != null && userImage.length() > 0) {
                Log.d(TAG + "userImage", userImage);
                Picasso.with(ctx).load(userImage).networkPolicy(NetworkPolicy.OFFLINE).into(user_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "image loading success !");
                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, "image loading error !");
                        Picasso.with(ctx)
                                .load(userImage)
                                .resize(50, 50)
                                .centerCrop()
                                .into(user_image);
                    }
                });

            }
        }

        public void setCommentImage(final Context ctx, final String commentImage) {
            final ImageZoomButton comment_image = (ImageZoomButton) mView.findViewById(R.id.comment_image);
            final ProgressBar loading_indicator = (ProgressBar) mView.findViewById(R.id.loading_indicator);
            loading_indicator.setVisibility(View.GONE);
            comment_image.setVisibility(View.GONE);
            if (commentImage != null && commentImage.length() > 0) {
                loading_indicator.setVisibility(View.VISIBLE);
                Animation animFadein = AnimationUtils.loadAnimation(ctx, R.anim.fade_in);
                Ion.with(comment_image)
                        .animateIn(animFadein)
                        .error(R.mipmap.app_icon)
                        .load(commentImage);
                comment_image.setVisibility(View.VISIBLE);
            }
        }
    }

    private void checkRatingOrNot() {
        if (mAuth.getCurrentUser() != null) {
            mDatabaseRatings.child(product_id).child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Log.d(TAG + " dataSnapshot check", dataSnapshot.getValue().getClass().getName());
                        if (dataSnapshot.getValue().getClass().getName().equals("java.lang.Long")) {
                            Long temp = (Long) dataSnapshot.getValue();
                            user_rating_bar.setRating((float) temp);
                            user_rating_bar.setIsIndicator(true);
                            submitRatingButton.setVisibility(View.GONE);
                        } else if (dataSnapshot.getValue().getClass().getName().equals("java.lang.double")) {
                            double temp = (double) dataSnapshot.getValue();
                            user_rating_bar.setRating((float) temp);
                            user_rating_bar.setIsIndicator(true);
                            submitRatingButton.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.e(TAG, "Failed to get value.", error.toException());
                }
            });
        }
    }

    private void getAverageRating() {

        if (mAuth.getCurrentUser() != null) {
            //average ratings change listener
            mDatabaseRatings.child(product_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int totalRating = 0;
                    if (dataSnapshot.getValue() != null) {
                        Log.d(TAG + " dataSnapshot.getChildren()", dataSnapshot.getValue().toString());
                        Map<String, Long> td = (HashMap<String, Long>) dataSnapshot.getValue();
                        List<String> keys = new ArrayList<>(td.keySet());
                        List<Long> values = new ArrayList<>(td.values());
                        if (keys.contains(mAuth.getCurrentUser().getUid())) {
                            delete_rating.setVisibility(View.VISIBLE);
                        }
//                    Log.d(TAG + "  arraylist" , values.toString());
                        for (int i = 0; i < values.size(); i++) {
                            double temp = doubleValue(values.get(i));
                            barRatingCount = countEachRating(temp);
                            Log.d(TAG + " temp", temp + "");
                            totalRating += temp;
                        }
                        ratingCount = values.size();
                        Log.d(TAG + " total , ratingCount", totalRating + " , " + ratingCount);
                        averageRating = totalRating / ratingCount;
                        //round to 2 significant figures
                        BigDecimal bd = new BigDecimal(averageRating);
                        bd = bd.round(new MathContext(2));
                        averageRating = bd.floatValue();

                        Log.d(TAG + " average", averageRating + "");
                        ratingChartBar.setData(getBarData());
                        setData(ratingPieChart, averageRating);
                        rating_textview.setText(averageRating + "");
                        top_rating_bar.setRating(averageRating);
                        rating_population_number_text.setText(ratingCount + "");
                        Log.d("rating_population_number_text.setText(ratingCount)", ratingCount + "");
                        ratingChartBar.invalidate();
                        ratingPieChart.invalidate();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.e(TAG, "Failed to get value.", error.toException());
                }
            });
        } else {
            Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "Haven't logged in.", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private static double doubleValue(Object value) {
        return (value instanceof Number ? ((Number) value).doubleValue() : -1.0);
    }

    private float[] countEachRating(Double temp) {
        int y = temp.intValue();
        switch (y) {
            case 1:
                barRatingCount[0] += 1;
                break;
            case 2:
                barRatingCount[1] += 1;
                break;
            case 3:
                barRatingCount[2] += 1;
                break;
            case 4:
                barRatingCount[3] += 1;
                break;
            case 5:
                barRatingCount[4] += 1;
                break;
        }
        return barRatingCount;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            image_preview.setImageURI(mImageUri);
            preview_textview.setVisibility(View.VISIBLE);
            image_preview.setVisibility(View.VISIBLE);
        } else if (requestCode == CAM_REQUEST) {
            mImageUri = Uri.parse(new File(captureImageFullPath).toString());
            image_preview.setImageURI(mImageUri);
            preview_textview.setVisibility(View.VISIBLE);
            image_preview.setVisibility(View.VISIBLE);
        }

        if (requestCode == GALLERY_REQUEST_2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
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
            intent.setClass(ProductDetailActivity.this, SingleMakeupActivity.class);
            intent.putExtra("path", picturePath);
            intent.putExtra("makeupType", product_data_two.getCategory());
            if (product_data_two.getCategory().equals("Eyeshadows")) {
                intent.putExtra("colorArrayArray", product_data_two.getColor());
            } else
                intent.putStringArrayListExtra("colorArray", (ArrayList<String>) colorSet);
            //intent.putExtra("color" , "" + mBlobColorHsv);
            startActivity(intent);
        } else if (requestCode == CAM_REQUEST) {
            Intent intent = new Intent();
            intent.setClass(ProductDetailActivity.this, SingleMakeupActivity.class);
            intent.putExtra("path", captureImageFullPath);
            startActivity(intent);
        }

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

    private void uploadRating() {
        float rating = user_rating_bar.getRating();
        Log.d(TAG + " user_rating", rating + "");
        if (rating != 0.0f) {
            mProgress = new ProgressDialog(this);
            mProgress.setMessage("Uploading Rating ...");
            mProgress.show();

            mDatabaseRatings.child(product_id).child(mAuth.getCurrentUser().getUid()).setValue(rating);

            mProgress.dismiss();
        }
    }

    private void uploadComment() {
        final String commentText = commentEmojiconEditText.getText().toString();

        if (!TextUtils.isEmpty(commentText)) {
            mProgress = new ProgressDialog(this);
            mProgress.setMessage("Uploading Comments ...");
            mProgress.show();

            final DatabaseReference currentProductComment = mDatabaseComments.child(product_id).push();

            if (mImageUri != null) {
                StorageReference filepath = mStorageProfileImage.child(mImageUri.getLastPathSegment());
                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        String commentUri = taskSnapshot.getDownloadUrl().toString();

                        Log.d("currentProductComment", currentProductComment.toString());
                        currentProductComment.child("comment_time").setValue(getCurrentTimeInString());
                        currentProductComment.child("uid").setValue(mAuth.getCurrentUser().getUid());
                        currentProductComment.child("uid_image").setValue(user_image_url);
                        currentProductComment.child("username").setValue(current_username);
                        Log.d(TAG + " current_username", current_username);
                        Log.d(TAG + " uid_image", user_image_url);
                        currentProductComment.child("comment_image").setValue(commentUri);
                        Log.d(TAG + " comment_image", commentUri);
                        currentProductComment.child("comment").setValue(commentText);

                        Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "Upload comment successful.", Snackbar.LENGTH_LONG);
                        snackbar.show();

                    }
                });
            } else {
                Log.d("currentProductComment", currentProductComment.toString());
                currentProductComment.child("comment_time").setValue(getCurrentTimeInString());
                currentProductComment.child("uid").setValue(mAuth.getCurrentUser().getUid());
                currentProductComment.child("uid_image").setValue(user_image_url);
                currentProductComment.child("username").setValue(current_username);
                currentProductComment.child("comment_image").setValue("");
                currentProductComment.child("comment").setValue(commentText);
                Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "Upload comment successful.", Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            commentEmojiconEditText.clearFocus();
            commentEmojiconEditText.getText().clear();
            preview_textview.setVisibility(View.GONE);
            image_preview.setVisibility(View.GONE);
            mProgress.dismiss();
        }
    }

    private String getCurrentTimeInString() {
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH);
        Log.d(TAG + " current time", format.format(curDate));
        return format.format(curDate);
    }

    @Override
    public void onBackPressed() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
            this.finish();
        }
    }

    @Override
    protected void onStop() {
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }

        super.onStop();
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

    private void setData(PieChart colorPie, float value) {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        float result = value / 5f;
        Log.d(TAG + " result ", result + "");
        entries.add(new PieEntry(result, 0));
        entries.add(new PieEntry(1 - result, 1));
        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.

//        colorPie.setCenterTextTypeface(mTfLight);
        int centerTextColor = android.graphics.Color.argb(255, 57, 197, 193);
        colorPie.setCenterTextColor(centerTextColor);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(3f);

        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.argb(120, 57, 197, 193));
        colorPie.setCenterText(value + "");
        colorPie.setCenterTextSize(30);

        colors.add(Color.argb(100, 214, 214, 214));
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

        Log.d(TAG + "barData", barRatingCount[3] + "");
        entries.add(new BarEntry(4, barRatingCount[4]));
        entries.add(new BarEntry(3, barRatingCount[3]));
        entries.add(new BarEntry(2, barRatingCount[2]));
        entries.add(new BarEntry(1, barRatingCount[1]));
        entries.add(new BarEntry(0, barRatingCount[0]));

        BarDataSet dataset = new BarDataSet(entries, "");
        dataset.setColors(CUSTOM_COLOR);
        dataset.setDrawValues(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(dataset);

        BarData data = new BarData(dataSets);
//        data.setValueTextSize(10f);
//        data.setValueTypeface(fontType);
        data.setBarWidth(1f);

        return data;
    }

}
