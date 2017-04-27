package fyp.hkust.facet.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.model.Shop;
import fyp.hkust.facet.util.FontManager;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static org.json.JSONObject.NULL;

/**
 * Created by bentley on 17/4/2017.
 */

public class ShopLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton normal_map_button, shop_location_button, my_location_button;
    private String shop_id = null;
    private static final int RC_LOCATION_PERM = 124;
    private Shop shop;
    private List<Shop> brandShops = new ArrayList<>();
    private TextView currentShopName, currentShopAddress;
    private CircleImageView currentImage;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLocation;
    private LinearLayout bottomPanel;
    private String type;
    private int shopFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_location);
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.layout_shop_location), fontType);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        init();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    public void locationTask() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permissions, do the thing!
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location_contacts),
                    RC_LOCATION_PERM, perms);
        }
    }

    private void init() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        type = bundle.getString("type");

        switch (type) {
            case "shopItem":
                shop = (Shop) bundle.getSerializable("shop");
                break;
            case "brandShops":
                int size = bundle.getInt("size");
                Log.d("brandshops", "bundle get " + size);
                for (int i = 0; i < size; i++) {
                    String temp = "shops" + i;
                    Log.d("brandshops", "bundle get" + " " + temp);
                    brandShops.add((Shop) bundle.getSerializable(temp));
                }
                Log.d("brandshopget", "" + brandShops.size());
                break;
        }

//        normal_map_button = (ImageButton) findViewById(R.id.normal_map_button);
        shop_location_button = (ImageButton) findViewById(R.id.shop_location_button);
        my_location_button = (ImageButton) findViewById(R.id.my_location_button);
        currentShopName = (TextView) findViewById(R.id.current_shop_name);
        currentShopAddress = (TextView) findViewById(R.id.current_shop_address);
        currentImage = (CircleImageView) findViewById(R.id.current_shop_image);
        bottomPanel = (LinearLayout) findViewById(R.id.bottom_info_panel_one_shop);

        switch (type) {
            case "shopItem":
                currentShopName.setText(shop.getName());
                currentShopAddress.setText(shop.getAddress());
                Picasso.with(this).load(shop.getImage()).into(currentImage);
                break;
            case "brandShops":
                currentShopName.setText(brandShops.get(0).getName());
                currentShopAddress.setText(brandShops.get(0).getAddress());
                Picasso.with(this).load(brandShops.get(0).getImage()).into(currentImage);
                break;
        }
//        normal_map_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //normal map
//                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//            }
//        });

        statusCheck();

        shop_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomPanel.setVisibility(View.VISIBLE);
                mMap.clear();
                switch (type) {
                    case "shopItem":
                        LatLng shopLocation = new LatLng(shop.getLatitude(), shop.getLongitude());
                        Marker shopLocationMarker = mMap.addMarker(new MarkerOptions().position(shopLocation).title("Shop is here")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(shopLocation));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(shopLocation, 17), 2000, null);
                        shopLocationMarker.showInfoWindow();

                        CircleOptions circleOptions = new CircleOptions();
                        circleOptions.center(shopLocation);
                        circleOptions.radius(20);
                        circleOptions.strokeColor(Color.argb(150, 89, 214, 214));
                        mMap.addCircle(circleOptions);
                        break;
                    case "brandShops":
                        for (int i = 0; i < brandShops.size(); i++) {
                            LatLng shopLocation2 = new LatLng(brandShops.get(i).getLatitude(), brandShops.get(i).getLongitude());
                            Marker shopLocationMarker2 = mMap.addMarker(new MarkerOptions().position(shopLocation2)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            if (i == 0) {
//                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shopLocation2, 10));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(shopLocation2, 15), 2000, null);
                            }
                        }
                        LatLng myCurrentLatLng = new LatLng(brandShops.get(shopFlag).getLatitude(), brandShops.get(shopFlag).getLongitude());
                        Marker currentMarker = mMap.addMarker(new MarkerOptions().position(myCurrentLatLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                        currentMarker.setVisible(true);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLatLng, 16), 2000, null);
                        currentShopName.setText(brandShops.get(shopFlag).getName());
                        currentShopAddress.setText(brandShops.get(shopFlag).getAddress());
                        Picasso.with(getApplicationContext()).load(brandShops.get(shopFlag).getImage()).into(currentImage);
                        bottomPanel.setVisibility(View.VISIBLE);
                        if (shopFlag == (brandShops.size() - 1)) {
                            shopFlag = 0;
                        } else {
                            shopFlag = shopFlag + 1;
                        }
                        break;
                }
            }
        });

        my_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ShopLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);

                } else {
                    // Show rationale and request permission.
                    locationTask();
                }

                if (LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient).isLocationAvailable()) {
                    currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (currentLocation != null) {
                        LatLng myCurrentLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                        Marker shopLocationMarker = mMap.addMarker(new MarkerOptions().position(myCurrentLocation).title("You are here")
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 17), 2000, null);
//                        shopLocationMarker.showInfoWindow();
                        bottomPanel.setVisibility(View.GONE);
                    }
                }
            }
        });
    }


    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in place and move the camera
//        Log.d("OnMapReady", "here");
//        Log.d("OnMapReady", "type:" + type);
        switch (type) {
            case "shopItem":
                LatLng shopLocation1 = new LatLng(shop.getLatitude(), shop.getLongitude());
                Marker shopLocationMarker1 = mMap.addMarker(new MarkerOptions().position(shopLocation1)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shopLocation1, 15));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(shopLocation, 17), 2000, null);
//                Log.d("OnMapReady", "shopItem");
                break;
            case "brandShops":
                for (int i = 0; i < brandShops.size(); i++) {
                    LatLng shopLocation2 = new LatLng(brandShops.get(i).getLatitude(), brandShops.get(i).getLongitude());
                    Marker shopLocationMarker2 = mMap.addMarker(new MarkerOptions().position(shopLocation2)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    if (i == 0) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shopLocation2, 10));
                    }
                }
//                Log.d("OnMapReady", "brandShop");
                break;
        }

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        Log.d("onstart", "yes");
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        Log.d("onstop", "yes");
        super.onStop();
    }


}
