package fyp.hkust.facet.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fyp.hkust.facet.R;
import fyp.hkust.facet.model.Brand;
import fyp.hkust.facet.model.Product;
import fyp.hkust.facet.util.FontManager;

public class ProductRecommentationActivity extends AppCompatActivity {

    private static final String TAG = "ProductRecommentationActivity ";
    private static View activity_main_layout;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseBrand;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseRatings;

    private Map<String, Product> mProducts = new HashMap<String, Product>();
    private Map<String, Brand> mBrand = new HashMap<String, Brand>();
    private List<String> brandList = new ArrayList<>();
    private List<String> brandIDList = new ArrayList<>();
    private RecyclerView recommend_product_list_1, recommend_product_list_2, recommend_product_list_3, recommend_product_list_4;
    private GridLayoutManager mgr;
    private ProductAdapter mProductAdapter1, mProductAdapter2, mProductAdapter3, mProductAdapter4;
    private Map<String, Product> mFoundationProducts = new HashMap<String, Product>();
    private Map<String, Product> mBlushProducts = new HashMap<String, Product>();
    private Map<String, Product> mEyshadowProducts = new HashMap<String, Product>();
    private Map<String, Product> mLipstickProducts = new HashMap<String, Product>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_recommentation);
        setup();
    }

    private void setup() {

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Recommendation");

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_product_recommendation_layout), fontType);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabase.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mDatabaseBrand = FirebaseDatabase.getInstance().getReference().child("Brand");
        mDatabaseRatings = FirebaseDatabase.getInstance().getReference().child("Ratings");
        mDatabaseRatings.keepSynced(true);
        Log.d(TAG + "mDatabaseRatings", mDatabaseRatings.toString());

        recommend_product_list_1 = (RecyclerView) findViewById(R.id.recommend_product_list_1);
        recommend_product_list_2 = (RecyclerView) findViewById(R.id.recommend_product_list_2);
        recommend_product_list_3 = (RecyclerView) findViewById(R.id.recommend_product_list_3);
        recommend_product_list_4 = (RecyclerView) findViewById(R.id.recommend_product_list_4);
        recommend_product_list_1.setItemAnimator(new DefaultItemAnimator());
        recommend_product_list_2.setItemAnimator(new DefaultItemAnimator());
        recommend_product_list_3.setItemAnimator(new DefaultItemAnimator());
        recommend_product_list_4.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Product result = ds.getValue(Product.class);
                    if (result.getValidate() == 1) {
                        mProducts.put(ds.getKey(), result);
                        mProducts.get(ds.getKey()).setRating(Long.valueOf(0));
                        Log.d(" product " + ds.getKey(), result.toString());
                    }
                }

                mDatabaseRatings.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = 0;
                        double totalRating = 0.0;

                        for (DataSnapshot ratingDs : dataSnapshot.getChildren()) {
                            Map<String, Long> td = (HashMap<String, Long>) ratingDs.getValue();
                            List<String> keys = new ArrayList<>(td.keySet());
                            List<Long> values = new ArrayList<>(td.values());
                            for (int i = 0; i < values.size(); i++) {
                                double temp = doubleValue(values.get(i));
                                Log.d(TAG + " temp", temp + "");
                                totalRating += temp;
                                count++;
                            }
                            Log.d(" rating " + ratingDs.getKey(), ratingDs.getValue().toString());
                            mProducts.get(ratingDs.getKey()).setRating((long) (totalRating / count));
                            Log.d(" mProduct rating ", mProducts.get(ratingDs.getKey()).getRating() + "");
                        }

                        mDatabaseBrand.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                brandIDList.clear();
                                brandList.clear();
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    Brand result = ds.getValue(Brand.class);
                                    mBrand.put(ds.getKey(), result);
                                    brandIDList.add(ds.getKey());
                                    brandList.add(result.getBrand());
                                    Log.d(" brand " + ds.getKey(), result.toString());
                                }


                                //sort product
                                sortProduct();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.e(TAG, "Failed to get value.", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }

    private void sortProduct() {

        Resources res = getResources();
        final String[] categoryArray = res.getStringArray(R.array.category_type_array);
        List<Product> values = new ArrayList<>(mProducts.values());
        List<String> keys = new ArrayList<>(mProducts.keySet());

        for (int i = 0; i < keys.size(); i++) {
            if (values.get(i) != null && values.get(i).getCategory().equals(categoryArray[1])) {
                Log.d(TAG +  "1 sortProduct: ",    keys.get(i) + " "+ values.get(0).getProductName());
                mFoundationProducts.put(keys.get(i), values.get(i));
            }
            if (values.get(i) != null && values.get(i).getCategory().equals(categoryArray[2])) {
                Log.d(TAG +  "2 sortProduct: ",    keys.get(i) + " "+ values.get(0).getProductName());
                mBlushProducts.put(keys.get(i), values.get(i));
            }
            if (values.get(i) != null && values.get(i).getCategory().equals(categoryArray[3])) {
                Log.d(TAG +  "3 sortProduct: ",    keys.get(i) + " "+ values.get(0).getProductName());
                mEyshadowProducts.put(keys.get(i), values.get(i));
            }
            if (values.get(i) != null && values.get(i).getCategory().equals(categoryArray[4])) {
                Log.d(TAG +  "4 sortProduct: ",    keys.get(i) + " "+ values.get(0).getProductName());
                mLipstickProducts.put(keys.get(i), values.get(i));
            }

        }

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager3 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager4 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recommend_product_list_1.setLayoutManager(layoutManager1);
        mProductAdapter1 = new ProductAdapter(mFoundationProducts, getApplicationContext());
        recommend_product_list_1.setAdapter(mProductAdapter1);
        mProductAdapter1.notifyDataSetChanged();

        recommend_product_list_2.setLayoutManager(layoutManager2);
        mProductAdapter2 = new ProductAdapter(mBlushProducts, getApplicationContext());
        recommend_product_list_2.setAdapter(mProductAdapter2);
        mProductAdapter2.notifyDataSetChanged();

        recommend_product_list_3.setLayoutManager(layoutManager3);
        mProductAdapter3 = new ProductAdapter(mEyshadowProducts, getApplicationContext());
        recommend_product_list_3.setAdapter(mProductAdapter3);
        mProductAdapter3.notifyDataSetChanged();

        recommend_product_list_4.setLayoutManager(layoutManager4);
        mProductAdapter4 = new ProductAdapter(mLipstickProducts, getApplicationContext());
        recommend_product_list_4.setAdapter(mProductAdapter4);
        mProductAdapter4.notifyDataSetChanged();
    }

    private static double doubleValue(Object value) {
        return (value instanceof Number ? ((Number) value).doubleValue() : -1.0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //back press
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class ProductAdapter extends RecyclerView.Adapter<MainActivity.ProductViewHolder> {

        private Map<String, Product> mResultProducts = new HashMap<>();
        // Allows to remember the last item shown on screen
        private int lastPosition = -1;
        private Context context;

        public ProductAdapter(Map<String, Product> mProducts, Context c) {
            this.context = c;
            this.mResultProducts = mProducts;
            notifyDataSetChanged();
        }

        @Override
        public MainActivity.ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = getLayoutInflater().inflate(R.layout.product_row, parent, false);
            MainActivity.ProductViewHolder viewHolder = new MainActivity.ProductViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MainActivity.ProductViewHolder viewHolder, int position) {
            List<Product> values = new ArrayList<>(mResultProducts.values());
            final Product model = values.get(position);
            List<String> keys = new ArrayList<>(mResultProducts.keySet());
            final String product_id = keys.get(position);

            Log.d(TAG + " product_id", product_id);
            Log.d(TAG + " product time", model.getReleaseDate() + "");
            Log.d(TAG + " product name", model.getProductName());
            Log.d(TAG + " product category", model.getCategory());
            Log.d(TAG, "loading view " + position);

//            Log.d(TAG + " product id ", product_id);
            viewHolder.setProductName(model.getProductName());
            if (mBrand != null)
                viewHolder.setBrandName(mBrand.get(model.getBrandID()).getBrand());
            viewHolder.setImage(getApplicationContext(), model.getProductImage());
            viewHolder.setUid(model.getUid());
            if (model.getRating() == null)
                viewHolder.setRating((long) 0);
            else
                viewHolder.setRating(model.getRating());
            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent productDetailIntent = new Intent();
                    productDetailIntent.setClass(ProductRecommentationActivity.this, ProductDetailActivity.class);
                    productDetailIntent.putExtra("product_id", product_id);
                    Log.d(TAG + " product_id", product_id);
                    productDetailIntent.putExtra("colorNo", model.getColorNo());
                    Log.d(TAG + " colorNo", model.getColorNo() + "");
                    startActivity(productDetailIntent);
                }
            });
            setAnimation(viewHolder.itemView, position);
            Log.d(TAG, "finish loading view");

        }

        /**
         * Here is the key method to apply the animation
         */
        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
                animation.setDuration(500);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return mResultProducts == null ? 0 : mResultProducts.size();
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private Typeface customTypeface = Typeface.createFromAsset(itemView.getContext().getAssets(), FontManager.APP_FONT);

        public ProductViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setRating(Long rating) {
            RatingBar product_rating_bar = (RatingBar) mView.findViewById(R.id.product_rating_bar);
            product_rating_bar.setRating(rating);
            SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            boolean ratingDisplayCheck = SP.getBoolean("ratingButton", true);
            if (ratingDisplayCheck == false)
                product_rating_bar.setVisibility(View.INVISIBLE);
            Log.d(TAG + " ratingDisplayCheck", ratingDisplayCheck + "");
        }

        public void setProductName(String productName) {
            TextView product_title = (TextView) mView.findViewById(R.id.p_title);
            product_title.setText(productName);
            product_title.setTypeface(customTypeface, Typeface.BOLD);
        }

        public void setBrandName(String brand) {
            TextView product_desc = (TextView) mView.findViewById(R.id.p_desc);
            product_desc.setText(brand);
            product_desc.setTypeface(customTypeface);
        }

        public void setUid(String uid) {
            TextView product_username = (TextView) mView.findViewById(R.id.p_username);
            product_username.setText(uid);
            product_username.setTypeface(customTypeface);
        }

        public void setImage(final Context ctx, final String image) {
            final ImageView post_image = (ImageView) mView.findViewById(R.id.product_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "image loading success !");
                }

                @Override
                public void onError() {
                    Log.d(TAG, "image loading error !");
                    Picasso.with(ctx)
                            .load(image)
                            .resize(100, 100)
                            .centerCrop()
                            .into(post_image);
                }
            });
        }
    }
}
