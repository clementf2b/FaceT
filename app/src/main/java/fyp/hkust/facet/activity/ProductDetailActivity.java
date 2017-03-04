package fyp.hkust.facet.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.Rating;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.model.Comment;
import fyp.hkust.facet.model.Product;
import fyp.hkust.facet.R;
import fyp.hkust.facet.model.User;
import fyp.hkust.facet.skincolordetection.ShowCameraViewActivity;
import fyp.hkust.facet.util.FontManager;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class ProductDetailActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private final static String TAG = "ProductDetailActivity";
    private static final int CAM_REQUEST = 3;
    private static final int GALLERY_REQUEST = 1;
    public final int[] CUSTOM_COLOR = {Color.rgb(23, 188, 247), Color.rgb(57, 197, 193),
            Color.rgb(179, 225, 215), Color.rgb(255, 226, 210), Color.rgb(255, 174, 182)};

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseComments;
    private DatabaseReference mDatabaseRatings;

    private CircleImageView product_color_imageview_1;
    private ColorPickerDialog colorPickerDialog;
    private ExpandableTextView descTextview;

    private PieChart ratingPieChart;
    private HorizontalBarChart ratingChartBar;
    private Typeface fontType;

    private LinearLayout activity_product_detail_layout;
    private EmojiconEditText commentEmojiconEditText;
    private ImageView emojiButton;
    private ImageView submitCommentButton;
    private View commentView;
    private EmojIconActions emojIcon;
    private String product_id;
    private String user_image_url;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        activity_product_detail_layout = (LinearLayout) findViewById(R.id.activity_product_detail_layout);

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

        //initialize
        product_id = getIntent().getExtras().getString("product_id");
        Log.d(TAG + " product_id", product_id);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        Log.d(TAG + "mDatabase", mDatabase.toString());
        mDatabase.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mStorageProfileImage = FirebaseStorage.getInstance().getReference().child("Comment_images");

        Log.d(TAG + "mDatabaseUsers", mDatabaseUsers.toString());
        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("Comments");
        mDatabaseComments.keepSynced(true);
        Log.d(TAG + "mDatabaseComments", mDatabaseComments.toString());
        mDatabaseRatings = FirebaseDatabase.getInstance().getReference().child("Ratings");
        mDatabaseRatings.keepSynced(true);
        Log.d(TAG + "mDatabaseRatings", mDatabaseRatings.toString());
        mDatabaseCommentsCurrentProduct = FirebaseDatabase.getInstance().getReference().child("Comments").child(product_id);
        mDatabaseCommentsCurrentProduct.keepSynced(true);
        Log.d(TAG + "mDatabaseCommentsCurrentProduct", mDatabaseCommentsCurrentProduct.toString());

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
        rating_textview = (TextView) findViewById(R.id.rating_textview);
        top_rating_bar = (RatingBar) findViewById(R.id.top_rating_bar);
        location_btn = (ImageButton) findViewById(R.id.location_btn);
        product_name_text = (TextView) findViewById(R.id.product_name_text);
        brand_name_text = (TextView) findViewById(R.id.brand_name_text);
        descTextview = (ExpandableTextView) findViewById(R.id.expand_text_view);
        descTextview.setText(getString(R.string.temp_description_label));

        user_profile_pic = (CircleImageView) findViewById(R.id.user_profile_pic);
        user_rating_bar = (RatingBar) findViewById(R.id.user_rating_bar);
        submitRatingButton = (Button) findViewById(R.id.submit_rating_btn);

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

        submitRatingButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                uploadRating();
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

        mCommentList = (RecyclerView) findViewById(R.id.product_comment_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mCommentList.setLayoutManager(layoutManager);

        checkUserExist();

        mDatabase.child(product_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.i("product dataSnapshot.getValue()", dataSnapshot.getValue().toString());
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
                    Log.i("user dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                    final User user_data = dataSnapshot.getValue(User.class);
                    Log.e(user_data.getName(), "User data is null!");
                    current_username = user_data.getName();
                    Log.d(TAG + " current user" , current_username);
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
                    user_image_url = user_data.getImage();
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
            protected void populateViewHolder(CommentViewHolder viewHolder, Comment model, int position) {

                Log.d(TAG, "loading view " + position);
                final String product_id = getRef(position).getKey();
                viewHolder.setCommentTime(model.getComment_time());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                if (model.getUid_image() != null)
                    viewHolder.setUserImage(getApplicationContext(), model.getUid_image());
                if (model.getComment_image() != null)
                    viewHolder.setCommentImage(getApplicationContext(), model.getComment_image());
//                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent productDetailIntent = new Intent();
//                        productDetailIntent.setClass(MainActivity.this,ProductDetailActivity.class);
//                        productDetailIntent.putExtra("product_id",product_id);
//                        Log.d(TAG + " product_id", product_id);
//                        startActivity(productDetailIntent);
//                    }
//                });

                Snackbar snackbar = Snackbar.make(activity_product_detail_layout, "Loaded comments successful.", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        };

        mCommentList.setAdapter(firebaseCommentRecyclerAdapter);
        mCommentList.setFocusable(false);
        mCommentList.setNestedScrollingEnabled(false);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        //        private String comment;
//        private String user_image;
//        private String uid;
//        private String comment_image;
//        private String comment_time;
        public void setComment(String comment) {
            EmojiconTextView emojicon_text_view = (EmojiconTextView) mView.findViewById(R.id.emojicon_text_view);
            emojicon_text_view.setText(comment);
        }

        public void setCommentTime(String date) {
            TextView comment_time = (TextView) mView.findViewById(R.id.comment_date);
            comment_time.setText(date);
        }

        public void setUsername(String username) {
            TextView comment_username = (TextView) mView.findViewById(R.id.comment_username);
            comment_username.setText(username);
        }

        public void setUserImage(final Context ctx, final String userImage) {
            final ImageView user_image = (ImageView) mView.findViewById(R.id.comment_profilepic);
            if (userImage != null && userImage.length() > 0) {
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
            final ImageView comment_image = (ImageView) mView.findViewById(R.id.comment_image);
            if (commentImage != null && commentImage.length() > 0) {
                Picasso.with(ctx).load(commentImage).networkPolicy(NetworkPolicy.OFFLINE).into(comment_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "image loading success !");
                    }

                    @Override
                    public void onError() {
                        Log.d(TAG, "image loading error !");
                        Picasso.with(ctx)
                                .load(commentImage)
                                .resize(100, 100)
                                .centerCrop()
                                .into(comment_image);
                    }
                });
                comment_image.setVisibility(View.VISIBLE);
            }
        }
    }

    private void checkRatingOrNot() {
        mDatabaseRatings.child(product_id).child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.d(TAG + " dataSnapshot check", dataSnapshot.getValue().getClass().getName());
                    if(dataSnapshot.getValue().getClass().getName().equals("java.lang.Long")) {
                        Long temp = (Long) dataSnapshot.getValue();
                        user_rating_bar.setRating((float) temp);
                        user_rating_bar.setIsIndicator(true);
                        submitRatingButton.setVisibility(View.GONE);
                    }
                    else if(dataSnapshot.getValue().getClass().getName().equals("java.lang.double")){
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

    private void getAverageRating() {

        //average ratings change listener
        mDatabaseRatings.child(product_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int totalRating = 0;
                if (dataSnapshot.getValue() != null) {
                    Log.d(TAG + " dataSnapshot.getChildren()", dataSnapshot.getValue().toString());
                    Map<String, Long> td = (HashMap<String, Long>) dataSnapshot.getValue();
                    List<Long> values = new ArrayList<>(td.values());
//                    Log.d(TAG + "  arraylist" , values.toString());

                    for (int i = 0; i < values.size(); i++) {
                        Long temp = values.get(i);
                        barRatingCount = countEachRating(temp);
                        Log.d(TAG + " temp", temp + "");
                        totalRating += temp;
                        ratingCount += 1;
                    }
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
    }

    private float[] countEachRating(Long temp) {
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
                        Log.d(TAG + " current_username" , current_username);
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

    private void setData(PieChart colorPie, float value) {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        float result = value / 5f;
        Log.d(TAG + " result ", result + "");
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
        colorPie.setCenterText(value + "");
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

        Log.d(TAG + "barData", barRatingCount[3] + "");
        entries.add(new BarEntry(4, barRatingCount[4]));
        entries.add(new BarEntry(3, barRatingCount[3]));
        entries.add(new BarEntry(2, barRatingCount[2]));
        entries.add(new BarEntry(1, barRatingCount[1]));
        entries.add(new BarEntry(0, barRatingCount[0]));

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
