package fyp.hkust.facet.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
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

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.adapter.ArrayAdapterWithIcon;
import fyp.hkust.facet.fragment.HongKongIslandFragment;
import fyp.hkust.facet.fragment.KowloonFragment;
import fyp.hkust.facet.fragment.NewTerritoriesFragment;
import fyp.hkust.facet.model.Brand;
import fyp.hkust.facet.model.Shop;
import fyp.hkust.facet.model.User;
import fyp.hkust.facet.skincolordetection.ShowCameraViewActivity;
import fyp.hkust.facet.util.CustomTypeFaceSpan;
import fyp.hkust.facet.util.FontManager;

public class ShopListActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private static final int CAM_REQUEST = 3;
    private int buttonNumber = 0;
    private String captureImageFullPath;

    private static final String NAV_ITEM_ID = "nav_index";
    DrawerLayout drawerLayout;
    private int navItemId;
    private Toolbar toolbar;
    private NavigationView view;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<Shop> shopList = new ArrayList<>();
    private List<Shop> shopList_hk = new ArrayList<>();
    private List<Shop> shopList_kl = new ArrayList<>();
    private List<Shop> shopList_nt = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseShop, mDatabaseBrand, mDatabaseDistrict;
    private Map<String, Brand> mBrand = new HashMap<String, Brand>();
    private Map<String, String> mDistrict = new HashMap<>();
    private String TAG = this.getClass().getSimpleName();
    private HongKongIslandFragment fragmentHK = new HongKongIslandFragment();
    private KowloonFragment fragmentKL = new KowloonFragment();
    private NewTerritoriesFragment fragmentNT = new NewTerritoriesFragment();
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_shop_layout), fontType);

        //start
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackground(new ColorDrawable(Color.parseColor("#00000000")));
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        view = (NavigationView) findViewById(R.id.navigation_view);

        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(ShopListActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
                navigateTo(menuItem);

                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });
        applyCustomFontToWholeMenu();
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_facet_match:
                        navItemId = 0;
                        showAlertDialog();
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_virtual_makeup:
                        navItemId = 1;
                        showMakeUpDialog();
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_product:
                        navItemId = 2;
                        startActivity(new Intent(ShopListActivity.this, MainActivity.class));
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_store_location:
                        navItemId = 3;
                        startActivity(new Intent(ShopListActivity.this, ShopListActivity.class));
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_profile:
                        navItemId = 4;
                        startActivity(new Intent(ShopListActivity.this, ProfileActivity.class));
                        menuItem.setChecked(true);
                        break;
                    case R.id.nav_setting:
                        navItemId = 5;
                        startActivity(new Intent(ShopListActivity.this, SettingsActivity.class));
                        menuItem.setChecked(true);
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
            navItemId = savedInstanceState.getInt(NAV_ITEM_ID, R.id.nav_profile);
        } else {
            navItemId = R.id.nav_store_location;
        }

        navigateTo(view.getMenu().findItem(navItemId));
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        //end

        prepareShopData();
        setupNavHeader();

        viewPager = (ViewPager) findViewById(R.id.viewpager_shop_list);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs_shop_list);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void prepareShopData() {
        mDatabaseShop = FirebaseDatabase.getInstance().getReference().child("Shop");
        mDatabaseBrand = FirebaseDatabase.getInstance().getReference().child("Brand");
        mDatabaseDistrict = FirebaseDatabase.getInstance().getReference().child("District");

        mDatabaseBrand.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                shopList.clear();
                shopList_hk.clear();
                shopList_kl.clear();
                shopList_nt.clear();
                mBrand.clear();
                mDistrict.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Brand brand = ds.getValue(Brand.class);
                    mBrand.put(ds.getKey(), brand);
                }
                mDatabaseDistrict.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot area : dataSnapshot.getChildren()) {
                            for (DataSnapshot district_key : area.getChildren()) {
                                mDistrict.put(district_key.getKey(), district_key.getValue().toString());
                            }
                        }
                        mDatabaseShop.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                Log.d("data", dataSnapshot.getValue().toString());

                                //first layer: BrandID
                                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//                                    Log.v("key", childDataSnapshot.getKey());//displays the key for the node
                                    List<Shop> temp = new ArrayList<>();
                                    temp.clear();
                                    int count = 0;
                                    //second layer: Shop's object
                                    for (DataSnapshot ds : childDataSnapshot.getChildren()) {
//                                        Log.v("value", ds.getValue().toString());   //gives the value for given keyname
                                        Shop shop = ds.getValue(Shop.class);
                                        temp.add(shop);
                                        temp.get(count).setBrandID(childDataSnapshot.getKey());
                                        temp.get(count).setImage(mBrand.get(childDataSnapshot.getKey()).getImage());
                                        temp.get(count).setDistrict(mDistrict.get(temp.get(count).getDistrict()));
                                        shopList.add(temp.get(count));
                                        count++;
                                        Log.d("SHOPLIST", "" + shopList.size());
                                    }
                                }

                                for (int i = 0; i < shopList.size(); i++) {
                                    Log.d("Area", shopList.get(i).getArea());
                                    switch (shopList.get(i).getArea()) {
                                        case "hki":
                                            shopList_hk.add(shopList.get(i));
                                            break;
                                        case "kl":
                                            shopList_kl.add(shopList.get(i));
                                            break;
                                        case "nt":
                                            shopList_nt.add(shopList.get(i));
                                        default:
                                            break;
                                    }
                                }
                                fragmentHK.setShopList(shopList_hk);
                                fragmentKL.setShopList(shopList_kl);
                                fragmentNT.setShopList(shopList_nt);

//                                Log.d("shopList_hk", ""+ shopList_hk.size());
//                                Log.d("shopList_wl", ""+ shopList_kl.size());
//                                Log.d("shopList_nt", ""+ shopList_nt.size());
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.w("Error", "Failed to read shop value.", error.toException());
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Error", "Failed to read brand value.", error.toException());
            }
        });
//        Log.d("shop_hk", shopList.size() + "");

    }

    public void findNearbyStore(View view) {
        boolean status = statusCheck();
        if (status) {
            Intent intent = new Intent(this, NearbyLocationActivity.class);
            Bundle bundle = new Bundle();
            for (int i = 0; i < shopList.size(); i++) {
                bundle.putSerializable("shop" + i, shopList.get(i));
            }
            Log.d("BUNDLE", "" + bundle.size());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public boolean statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(fragmentHK, getString(R.string.tab_hkisland));
        adapter.addFragment(fragmentKL, getString(R.string.tab_kowloon));
        adapter.addFragment(fragmentNT, getString(R.string.tab_nTerritories));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void navigateTo(MenuItem menuItem) {
        // contentView.setText(menuItem.getTitle());

        navItemId = menuItem.getItemId();
        menuItem.setChecked(true);
    }

    private void setupNavHeader() {

        View header = view.getHeaderView(0);
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        final TextView usernameHeader = (TextView) header.findViewById(R.id.username_header);
        final TextView emailHeader = (TextView) header.findViewById(R.id.email_header);
        usernameHeader.setTypeface(fontType);
        emailHeader.setTypeface(fontType);
        final CircleImageView headerphoto = (CircleImageView) header.findViewById(R.id.profile_image);
        headerphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accountIntent = new Intent(ShopListActivity.this, ProfileActivity.class);
                accountIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(accountIntent);
            }
        });

        if (mAuth.getCurrentUser() != null) {
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final User user_data = dataSnapshot.getValue(User.class);
                    if (user_data != null) {

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

    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose the way to get your selfie");

        builder.setIcon(R.drawable.app_icon_100);
        builder.setCancelable(true);

        final String[] items = new String[]{"From Gallery", "Take Photo"};
        final Integer[] icons = new Integer[]{R.drawable.colorful_gallery_s, R.drawable.colorful_camera_s};
        ListAdapter adapter = new ArrayAdapterWithIcon(getApplication(), items, icons);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: {
                        buttonNumber = 1;
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, GALLERY_REQUEST);
                        break;
                    }
                    case 1: {
                        Intent cameraViewIntent = new Intent(ShopListActivity.this, ShowCameraViewActivity.class);
//                cameraViewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(cameraViewIntent);
                        break;
                    }
                }

            }
        }).show();
    }

    private void showMakeUpDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose the way to get your selfie");

        builder.setIcon(R.drawable.app_icon_100);
        builder.setCancelable(true);

        final String[] items = new String[]{"From Gallery", "Take Photo"};
        final Integer[] icons = new Integer[]{R.drawable.colorful_gallery_s, R.drawable.colorful_camera_s};
        ListAdapter adapter = new ArrayAdapterWithIcon(getApplication(), items, icons);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0: {
                        buttonNumber = 2;
                        Intent intent = new Intent(
                                Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, GALLERY_REQUEST);
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
}
