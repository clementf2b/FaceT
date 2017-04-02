package fyp.hkust.facet.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.internal.LinkedHashTreeMap;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import fyp.hkust.facet.R;
import fyp.hkust.facet.model.Product;
import fyp.hkust.facet.util.FontManager;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private static View activity_main_layout;
    private RecyclerView mProductList;
    private GridLayoutManager mgr;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String NAV_ITEM_ID = "nav_index";
    DrawerLayout drawerLayout;
    private int navItemId;
    private Toolbar toolbar;

    private Map<String, Product> mProducts = new HashMap<String, Product>();
    private int order = 1;
    private int categoryResult = 0;
    private int sort = 0;
    private Spinner filterSpinner;
    private Spinner orderSpinner;
    private static int firstTime = 0;
    private Map<String, Product> mSortedProducts = new HashMap<String, Product>();
    private ProductAdapter mProductAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main_layout = (CoordinatorLayout) findViewById(R.id.activity_main_layout);
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_main_layout), fontType);

        categoryResult = getIntent().getExtras().getInt("categoryResult");

        //start
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackground(new ColorDrawable(Color.parseColor("#00000000")));
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(MainActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
                navigateTo(menuItem);

                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });

        NavigationView sort_view = (NavigationView) findViewById(R.id.sort_navigation_view);
        sort_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(MainActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.END); /*Important Line*/
                return true;
            }
        });

        setupRightDrawer();

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (null != savedInstanceState) {
            navItemId = savedInstanceState.getInt(NAV_ITEM_ID, R.id.nav_product);
        } else {
            navItemId = R.id.nav_product;
        }

        navigateTo(view.getMenu().findItem(navItemId));
        //end

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out. Haven't logged in before");
                }
                if (firebaseAuth.getCurrentUser() != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in user_id:" + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabase.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mProductList = (RecyclerView) findViewById(R.id.product_list);
        mgr = new GridLayoutManager(this, 2);
        mProductList.setLayoutManager(mgr);

        checkUserExist();
    }

    private void setupRightDrawer() {
        View sort_main_layout = (View) findViewById(R.id.sort_main_layout);
        Button apply_btn = (Button) sort_main_layout.findViewById(R.id.apply_btn);
        Button clear_btn = (Button) sort_main_layout.findViewById(R.id.clear_btn);
        final Button acensding_btn = (Button) sort_main_layout.findViewById(R.id.acensding_btn);
        final Button decensding_btn = (Button) sort_main_layout.findViewById(R.id.decensding_btn);
        apply_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " apply");
                drawerLayout.closeDrawer(GravityCompat.END);
                setupProductList();
            }
        });
        apply_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " clear");
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        acensding_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " order = 0");
                acensding_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_with_bg));
                decensding_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_no_bg));
                acensding_btn.setTextColor(getResources().getColor(R.color.white));
                decensding_btn.setTextColor(getResources().getColor(R.color.font_color_pirmary));
                order = 0;
            }
        });
        decensding_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " order = 1");
                decensding_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_with_bg));
                acensding_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_no_bg));
                decensding_btn.setTextColor(getResources().getColor(R.color.white));
                acensding_btn.setTextColor(getResources().getColor(R.color.font_color_pirmary));
                order = 1;
            }
        });

        filterSpinner = (Spinner) findViewById(R.id.shop_filter_spinner);
        Resources res = getResources();

        final String[] category = res.getStringArray(R.array.category_type_array);
        final ArrayAdapter<CharSequence> lunchList = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.category_type_array,
                android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(lunchList);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "You choose " + category[position], Toast.LENGTH_SHORT).show();
                if(firstTime > 0) {
                    categoryResult = position;
                }
                firstTime++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        orderSpinner = (Spinner) findViewById(R.id.sort_spinner);

        final String[] sortString = res.getStringArray(R.array.sort_type_array);
        final ArrayAdapter<CharSequence> sortList = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.sort_type_array,
                android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(sortList);

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "You choose " + sortString[position], Toast.LENGTH_SHORT).show();
                sort = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {  /*Closes the Appropriate Drawer*/
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
            System.exit(0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Product result = ds.getValue(Product.class);
                    mProducts.put(ds.getKey(), result);
                    Log.d(" product " + ds.getKey(), result.toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProductAdapter = new ProductAdapter(mProducts);
        mProductList.setAdapter(mProductAdapter);
        mProductAdapter.notifyDataSetChanged();
    }

    public void setupProductList() {
        // sort by name a - z
        mSortedProducts = filterProduct(mProducts, categoryResult);
        mSortedProducts = sortByComparator(mSortedProducts, sort, order);
        mProductAdapter = new ProductAdapter(mSortedProducts);
        mProductList.setAdapter(mProductAdapter);
        mProductAdapter.notifyDataSetChanged();
    }

    private Map<String, Product> filterProduct(Map<String, Product> unsortMap, int categoryResult) {
        List<Map.Entry<String, Product>> temp = new LinkedList<Map.Entry<String, Product>>(unsortMap.entrySet());
        Resources res = getResources();
        final String[] categoryArray = res.getStringArray(R.array.category_type_array);

        if (categoryResult > 0) {
            for (int i = 0; i < temp.size(); i++) {
                if (!categoryArray[categoryResult].equals(temp.get(i).getValue().getCategory())) {
                    temp.remove(i);
                }
            }
        }

        // Maintaining insertion order with the help of LinkedList
        Map<String, Product> filteredMap = new LinkedHashMap<String, Product>();
        for (Map.Entry<String, Product> entry : temp) {
            filteredMap.put(entry.getKey(), entry.getValue());
            Log.d("Sorted ", "Map");
            Log.d(entry.getKey(), entry.getValue().toString());
        }

        return filteredMap;
    }

    private static Map<String, Product> sortByComparator(Map<String, Product> unsortMap, int sort, final int order) {

        final StringBuilder sortOperation = new StringBuilder("");

        List<Map.Entry<String, Product>> list = new LinkedList<Map.Entry<String, Product>>(unsortMap.entrySet());

        // Sorting the list based on values

        switch (sort) {
            case 0:
                sortOperation.append("Sort by release date");
                Collections.sort(list, new Comparator<Map.Entry<String, Product>>() {
                    public int compare(Map.Entry<String, Product> o1,
                                       Map.Entry<String, Product> o2) {
                        if (order == 0) {
                            // sort by name a - z
                            return o1.getValue().getReleaseDate().compareTo(o2.getValue().getReleaseDate());
                        } else {
                            // sort by name z - a
                            return o2.getValue().getReleaseDate().compareTo(o1.getValue().getReleaseDate());
                        }
                    }
                });
                break;
            case 1:
                sortOperation.append("Sort by product name");
                Collections.sort(list, new Comparator<Map.Entry<String, Product>>() {
                    public int compare(Map.Entry<String, Product> o1,
                                       Map.Entry<String, Product> o2) {
                        if (order == 0) {
                            // sort by name a - z
                            return o1.getValue().getProductName().compareTo(o2.getValue().getProductName());
                        } else {
                            // sort by name z - a
                            return o2.getValue().getProductName().compareTo(o1.getValue().getProductName());
                        }
                    }
                });
                break;
            case 2:
                sortOperation.append("Sort by category");
                Collections.sort(list, new Comparator<Map.Entry<String, Product>>() {
                    public int compare(Map.Entry<String, Product> o1,
                                       Map.Entry<String, Product> o2) {
                        if (order == 0) {
                            // sort by name a - z
                            return o1.getValue().getCategory().compareTo(o2.getValue().getCategory());
                        } else {
                            // sort by name z - a
                            return o2.getValue().getCategory().compareTo(o1.getValue().getCategory());
                        }
                    }
                });
                break;
        }

        if (order == 0) {
            // sort by name a - z
            sortOperation.append(" in ascending order");
        } else {
            // sort by name z - a
            sortOperation.append(" in descending order");
        }

        // Maintaining insertion order with the help of LinkedList
        Map<String, Product> sortedMap = new LinkedHashMap<String, Product>();
        for (Map.Entry<String, Product> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
            Log.d("Sorted ", "Map");
            Log.d(entry.getKey(), entry.getValue().toString());
        }

        Snackbar snackbar = Snackbar.make(activity_main_layout, sortOperation, Snackbar.LENGTH_SHORT);
        snackbar.show();

        return sortedMap;
    }

    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent mainIntent = new Intent(MainActivity.this, ProfileEditActivity.class);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }

        if (item.getItemId() == R.id.action_logout) {
            logout();
        }

        if (item.getItemId() == R.id.action_account) {
            startActivity(new Intent(MainActivity.this, AccountActivity.class));
        }

        if (item.getItemId() == R.id.action_setting) {
            drawerLayout.openDrawer(GravityCompat.END); /*Opens the Right Drawer*/
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateTo(MenuItem menuItem) {
        // contentView.setText(menuItem.getTitle());

        navItemId = menuItem.getItemId();
        menuItem.setChecked(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, navItemId);
    }

    private void logout() {
        mAuth.signOut();
    }

    public class ProductAdapter extends RecyclerView.Adapter<ProductViewHolder> {

        private Map<String, Product> mResultProducts = new HashMap<>();

        public ProductAdapter(Map<String, Product> mProducts) {
            this.mResultProducts = mProducts;
        }

        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = getLayoutInflater().inflate(R.layout.product_row, parent, false);
            ProductViewHolder viewHolder = new ProductViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ProductViewHolder viewHolder, int position) {
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
            viewHolder.setDescription(model.getDescription());
            viewHolder.setImage(getApplicationContext(), model.getProductImage());
            viewHolder.setUid(model.getUid());


            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent productDetailIntent = new Intent();
                    productDetailIntent.setClass(MainActivity.this, ProductDetailActivity.class);
                    productDetailIntent.putExtra("product_id", product_id);
                    Log.d(TAG + " product_id", product_id);
                    productDetailIntent.putExtra("colorNo", model.getColorNo());
                    Log.d(TAG + " colorNo", model.getColorNo() + "");
                    startActivity(productDetailIntent);
                }
            });

            Log.d(TAG, "finish loading view");

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

        public void setProductName(String productName) {
            TextView product_title = (TextView) mView.findViewById(R.id.p_title);
            product_title.setText(productName);
            product_title.setTypeface(customTypeface);
        }

        public void setDescription(String description) {
            TextView product_desc = (TextView) mView.findViewById(R.id.p_desc);
            product_desc.setText(description);
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
