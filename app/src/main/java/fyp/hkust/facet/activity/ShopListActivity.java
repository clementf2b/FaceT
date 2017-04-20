package fyp.hkust.facet.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fyp.hkust.facet.R;
import fyp.hkust.facet.fragment.HongKongIslandFragment;
import fyp.hkust.facet.fragment.KowloonFragment;
import fyp.hkust.facet.fragment.NewTerritoriesFragment;
import fyp.hkust.facet.model.Brand;
import fyp.hkust.facet.model.Shop;
import fyp.hkust.facet.util.FontManager;

public class ShopListActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private List<Shop> shopList = new ArrayList<>();
    private List<Shop> shopList_hk = new ArrayList<>();
    private List<Shop> shopList_kl = new ArrayList<>();
    private List<Shop> shopList_nt = new ArrayList<>();
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

        prepareShopData();

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
            for(int i = 0; i < shopList.size(); i++ ) {
                bundle.putSerializable("shop" + i, shopList.get(i));
            }
            Log.d("BUNDLE",""+bundle.size());
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
}
