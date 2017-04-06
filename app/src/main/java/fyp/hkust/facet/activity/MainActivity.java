package fyp.hkust.facet.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.model.Brand;
import fyp.hkust.facet.model.Product;
import fyp.hkust.facet.model.User;
import fyp.hkust.facet.util.CheckConnectivity;
import fyp.hkust.facet.util.CustomTypeFaceSpan;
import fyp.hkust.facet.util.FontManager;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private static View activity_main_layout;
    private RecyclerView mProductList;
    private GridLayoutManager mgr;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseBrand;
    private DatabaseReference mDatabaseUsers;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String NAV_ITEM_ID = "nav_index";
    DrawerLayout drawerLayout;
    private int navItemId;
    private Toolbar toolbar;
    private NavigationView view;
    private Map<String, Product> mProducts = new HashMap<String, Product>();
    private Map<String, Brand> mBrand = new HashMap<String, Brand>();
    private List<String> brandList = new ArrayList<>();
    private int order = 1;
    private int categoryResult = 0;
    private int sort = 0;
    private Spinner filterSpinner;
    private Spinner orderSpinner;
    private int brandResult = 0;
    private static int firstTime = 0;
    private Map<String, Product> mSortedProducts = new HashMap<String, Product>();
    private ProductAdapter mProductAdapter;
    private ProgressDialog dialog;
    private TabLayout tabLayout;
    private SearchView searchView;
    private MenuItem mMenuItem;
    private int[] tabIcons = {
            R.drawable.foundation,
            R.drawable.blush,
            R.drawable.eyeshadow,
            R.drawable.lipstick
    };
    private List<String> brandIDList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity_main_layout = (CoordinatorLayout) findViewById(R.id.activity_main_layout);

        CheckConnectivity check = new CheckConnectivity();
        Boolean conn = check.checkNow(this.getApplicationContext());
        if (conn == true) {
            //run your normal code path here
            Log.d(TAG, "Network connected");
        } else {
            //Send a warning message to the user
            Snackbar snackbar = Snackbar.make(activity_main_layout, "No Internet Access", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        //start
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        Typeface titleFontType = FontManager.getTypeface(getApplicationContext(), FontManager.ROOT + FontManager.TITLE_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_main_layout), fontType);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackground(new ColorDrawable(Color.parseColor("#00000000")));
        setSupportActionBar(toolbar);

        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setTypeface(titleFontType);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        view = (NavigationView) findViewById(R.id.navigation_view);
        applyCustomFontToWholeMenu();

        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(MainActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
                navigateTo(menuItem);

                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_virtual_makeup:
                        navItemId = 0;
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_product:
                        navItemId = 1;
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_store_location:
                        navItemId = 2;
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_profile:
                        navItemId = 3;
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_setting:
                        navItemId = 4;
                        menuItem.setChecked(true);
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                return true;
            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        Resources res = getResources();
        final String[] category = res.getStringArray(R.array.category_type_array);
        tabLayout.addTab(tabLayout.newTab().setText(category[0]));
        tabLayout.addTab(tabLayout.newTab().setText(category[1]));
        tabLayout.addTab(tabLayout.newTab().setText(category[2]));
        tabLayout.addTab(tabLayout.newTab().setText(category[3]));
        tabLayout.addTab(tabLayout.newTab().setText(category[4]));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tabLayout.getSelectedTabPosition() == 0) {
                    categoryResult = 0;
                    setupProductList();
                    Toast.makeText(MainActivity.this, "Tab " + tabLayout.getSelectedTabPosition(), Toast.LENGTH_SHORT).show();
                } else if (tabLayout.getSelectedTabPosition() == 1) {
                    categoryResult = 1;
                    setupProductList();
                    Toast.makeText(MainActivity.this, "Tab " + tabLayout.getSelectedTabPosition(), Toast.LENGTH_SHORT).show();
                } else if (tabLayout.getSelectedTabPosition() == 2) {
                    categoryResult = 2;
                    setupProductList();
                    Toast.makeText(MainActivity.this, "Tab " + tabLayout.getSelectedTabPosition(), Toast.LENGTH_SHORT).show();
                } else if (tabLayout.getSelectedTabPosition() == 3) {
                    categoryResult = 3;
                    setupProductList();
                    Toast.makeText(MainActivity.this, "Tab " + tabLayout.getSelectedTabPosition(), Toast.LENGTH_SHORT).show();
                } else if (tabLayout.getSelectedTabPosition() == 4) {
                    categoryResult = 4;
                    setupProductList();
                    Toast.makeText(MainActivity.this, "Tab " + tabLayout.getSelectedTabPosition(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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
        mDatabaseBrand = FirebaseDatabase.getInstance().getReference().child("Brand");

        mProductList = (RecyclerView) findViewById(R.id.product_list);
        mgr = new GridLayoutManager(this, 2);
        mProductList.setLayoutManager(mgr);

        checkUserExist();
        setupNavHeader();
    }

    private void applyCustomFontToWholeMenu() {
        Menu m = view.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);
//            //for applying a font to subMenu ...
//            SubMenu subMenu = mi.getSubMenu();
//            if (subMenu!=null && subMenu.size() >0 ) {
//                for (int j=0; j <subMenu.size();j++) {
//                    MenuItem subMenuItem = subMenu.getItem(j);
//                    applyFontToMenuItem(subMenuItem);
//                }
//            }
            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypeFaceSpan("", fontType), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    private void setupNavHeader() {

        if (mAuth.getCurrentUser() != null) {
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final User user_data = dataSnapshot.getValue(User.class);
                    if (user_data != null) {
                        View header = view.getHeaderView(0);

                        TextView usernameHeader = (TextView) header.findViewById(R.id.username_header);
                        TextView emailHeader = (TextView) header.findViewById(R.id.email_header);
                        final CircleImageView headerphoto = (CircleImageView) header.findViewById(R.id.profile_image);
                        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
                        usernameHeader.setTypeface(fontType);
                        emailHeader.setTypeface(fontType);

                        usernameHeader.setText(user_data.getName());
                        if (user_data.getEmail() != null && user_data.getEmail().length() > 0)
                            emailHeader.setText(user_data.getEmail());
                        else if (user_data.getEmail() == null || user_data.getEmail().length() == 0)
                            emailHeader.setText(mAuth.getCurrentUser().getEmail());

                        Picasso.with(getApplicationContext()).load(user_data.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(headerphoto, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Picasso.with(getApplicationContext())
                                        .load(user_data.getImage())
                                        .centerCrop()
                                        .fit()
                                        .into(headerphoto);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
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
                setupProductList();
                Log.d(TAG, " apply");
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " clear");
                categoryResult = 0;
                order = 1;
                sort = 0;
                setupProductList();
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        });
        acensding_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " order = 0");
                decensding_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_with_bg));
                acensding_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_no_bg));
                decensding_btn.setTextColor(getResources().getColor(R.color.white));
                acensding_btn.setTextColor(getResources().getColor(R.color.font_color_pirmary));
                order = 0;
            }
        });
        decensding_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " order = 1");
                acensding_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_with_bg));
                decensding_btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_button_no_bg));
                acensding_btn.setTextColor(getResources().getColor(R.color.white));
                decensding_btn.setTextColor(getResources().getColor(R.color.font_color_pirmary));
                order = 1;
            }
        });

        Resources res = getResources();
        orderSpinner = (Spinner) findViewById(R.id.sort_spinner);

        final String[] sortString = res.getStringArray(R.array.sort_type_array);
        final ArrayAdapter<CharSequence> sortList = ArrayAdapter.createFromResource(MainActivity.this,
                R.array.sort_type_array,
                android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setSelection(order);
        orderSpinner.setAdapter(sortList);

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView tmpView = (TextView) orderSpinner.getSelectedView().findViewById(android.R.id.text1);
                tmpView.setTextColor(Color.WHITE);
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
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {  /*Closes the Appropriate Drawer*/
            drawerLayout.closeDrawer(GravityCompat.END);
        }
        else
        {
            this.finish();
        }

        if (searchView != null && !searchView.isIconified()) {
            MenuItemCompat.collapseActionView(mMenuItem);
            return;
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

                            Log.d(TAG, "going to filter");
                            mSortedProducts = filterProduct(mProducts, categoryResult);
                            mProductAdapter = new ProductAdapter(mSortedProducts, getApplicationContext());
                            mProductList.setAdapter(mProductAdapter);
                            mProductAdapter.notifyDataSetChanged();


                            filterSpinner = (Spinner) findViewById(R.id.shop_filter_spinner);

                            final ArrayAdapter<String> categoryList = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, brandList);
                            filterSpinner.setSelection(brandResult);
                            filterSpinner.setAdapter(categoryList);

                            filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    TextView tmpView = (TextView) filterSpinner.getSelectedView().findViewById(android.R.id.text1);
                                    tmpView.setTextColor(Color.WHITE);
                                    Toast.makeText(MainActivity.this, "You choose " + brandList.get(position), Toast.LENGTH_SHORT).show();
                                    if (firstTime > 0) {
                                        brandResult = position;
                                    }
                                    firstTime++;
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }

    public void setupProductList() {
        // sort by name a - z
        if (mProducts.size() > 0) {
            mSortedProducts = filterProduct(mProducts, categoryResult);

            if (mSortedProducts.size() > 0)
                mSortedProducts = filterBrand(mSortedProducts, brandResult);
            if (mSortedProducts.size() > 0)
                mSortedProducts = sortByComparator(mSortedProducts, sort, order);

            mProductAdapter = new ProductAdapter(mSortedProducts, getApplicationContext());
            mProductList.setAdapter(mProductAdapter);
            mProductAdapter.notifyDataSetChanged();
        }
    }

    private Map<String, Product> filterBrand(Map<String, Product> unsortMap, int brandResult) {
        List<Map.Entry<String, Product>> temp = new LinkedList<Map.Entry<String, Product>>(unsortMap.entrySet());

        //compare temp
        List<Map.Entry<String, Product>> temp2 = new LinkedList<Map.Entry<String, Product>>(unsortMap.entrySet());

        int tempSize = temp2.size();
        List<String> removeList = new ArrayList<>();

        if (brandResult > 0) {
            for (int i = 0; i < tempSize; i++) {
                if (!brandIDList.get(brandResult).equals(temp2.get(i).getValue().getBrandID())) {
                    Log.d(TAG + " remove : " + temp2.get(i).getKey(), brandIDList.get(categoryResult) + " : " + temp2.get(i).getValue().getBrandID());
                    removeList.add(temp2.get(i).getKey());
                }
            }
        }
        Log.d("Filtered ", "Map");
        // Maintaining insertion order with the help of LinkedList
        Map<String, Product> filteredMap = new LinkedHashMap<String, Product>();
        for (Map.Entry<String, Product> entry : temp) {
            if (!removeList.contains(entry.getKey())) {
                filteredMap.put(entry.getKey(), entry.getValue());
                Log.d(entry.getKey(), entry.getValue().getProductName() + " : " + entry.getValue().getBrandID());
            }
        }

        return filteredMap;
    }

    private Map<String, Product> filterProduct(Map<String, Product> unsortMap, int categoryResult) {
        List<Map.Entry<String, Product>> temp = new LinkedList<Map.Entry<String, Product>>(unsortMap.entrySet());
        //compare temp
        List<Map.Entry<String, Product>> temp2 = new LinkedList<Map.Entry<String, Product>>(unsortMap.entrySet());

        int tempSize = temp2.size();
        List<String> removeList = new ArrayList<>();
        Resources res = getResources();
        final String[] categoryArray = res.getStringArray(R.array.category_type_array);

        if (categoryResult > 0) {
            for (int i = 0; i < tempSize; i++) {
                if (!categoryArray[categoryResult].equals(temp2.get(i).getValue().getCategory())) {
                    Log.d(TAG + " remove : " + temp2.get(i).getKey(), categoryArray[categoryResult] + " : " + temp2.get(i).getValue().getCategory());
                    removeList.add(temp2.get(i).getKey());
                }
            }
        }
        Log.d("Filtered ", "Map");
        // Maintaining insertion order with the help of LinkedList
        Map<String, Product> filteredMap = new LinkedHashMap<String, Product>();
        for (Map.Entry<String, Product> entry : temp) {
            if (!removeList.contains(entry.getKey())) {
                filteredMap.put(entry.getKey(), entry.getValue());
                Log.d(entry.getKey(), entry.getValue().getProductName() + " : " + entry.getValue().getCategory());
            }
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

    final private android.support.v7.widget.SearchView.OnQueryTextListener queryListener = new android.support.v7.widget.SearchView.OnQueryTextListener() {

        @Override
        public boolean onQueryTextChange(String newText) {
            Map<String, Product> temp = new HashMap<>(mSortedProducts);
            for (Iterator<Map.Entry<String, Product>> i = temp.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<String, Product> e = i.next();
                Product v = e.getValue();
                if (!v.getProductName().contains(newText))
                    i.remove();
            }
            mProductAdapter = new ProductAdapter(temp, MainActivity.this);
            mProductList.setAdapter(mProductAdapter);
            mProductAdapter.notifyDataSetChanged();
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            searchView.setFocusable(true);
            Log.d(TAG, "submit:" + query);
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        mMenuItem = menu.findItem(R.id.action_search);
        try {
            searchView = (SearchView) MenuItemCompat.getActionView(mMenuItem);
            searchView.setOnQueryTextListener(queryListener);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_refine) {
            drawerLayout.openDrawer(GravityCompat.END); /*Opens the Right Drawer*/
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
        // Allows to remember the last item shown on screen
        private int lastPosition = -1;
        private Context context;

        public ProductAdapter(Map<String, Product> mProducts, Context c) {
            this.context = c;
            this.mResultProducts = mProducts;
            notifyDataSetChanged();
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
            if (mBrand != null)
                viewHolder.setBrandName(mBrand.get(model.getBrandID()).getBrand());
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
