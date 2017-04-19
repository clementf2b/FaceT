package fyp.hkust.facet.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.model.Shop;
import fyp.hkust.facet.util.FontManager;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class NearbyLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton normal_map_button, shop_location_button, my_location_button;
    private String shop_id = null;
    private static final int RC_LOCATION_PERM = 124;
    private GoogleApiClient mGoogleApiClient;
    private List<Shop> shopList = new ArrayList<>();
    private TextView currentShopName, currentShopAddress;
    private CircleImageView currentImage;
    private LinearLayout bottomPanel;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_location);

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.layout_nearby_location), fontType);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        init();
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

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        for (int i = 0; i < bundle.size(); i++) {
            shopList.add((Shop) bundle.getSerializable("shop" + i));
        }

        shop_location_button = (ImageButton) findViewById(R.id.shop_location_button);
        my_location_button = (ImageButton) findViewById(R.id.my_location_button);
        currentShopName = (TextView) findViewById(R.id.current_shop_name);
        currentShopAddress = (TextView) findViewById(R.id.current_shop_address);
        currentImage = (CircleImageView) findViewById(R.id.current_shop_image);
        bottomPanel = (LinearLayout) findViewById(R.id.bottom_info_panel_nearby_shop);
        bottomPanel.setVisibility(View.GONE);

        shop_location_button = (ImageButton) findViewById(R.id.shop_location_button);
        my_location_button = (ImageButton) findViewById(R.id.my_location_button);


        shop_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                List<Shop> shopWithinRange = new ArrayList<>();
                for (int i = 0; i < shopList.size(); i++) {
                    LatLng shopLocation = new LatLng(shopList.get(i).getLatitude(), shopList.get(i).getLongitude());
                    Marker shopLocationMarker = mMap.addMarker(new MarkerOptions().position(shopLocation)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    shopLocationMarker.setVisible(false);
                    Location target = new Location("target");
                    target.setLatitude(shopList.get(i).getLatitude());
                    target.setLongitude(shopList.get(i).getLongitude());
                    Log.d("before_compare_location", ""+currentLocation.distanceTo(target));
                    if(currentLocation.distanceTo(target) < 5000 ) {
                        shopLocationMarker.setVisible(true);
                        shopWithinRange.add(shopList.get(i));
                    }
                }
                LatLng myCurrentLatLng = new LatLng(shopWithinRange.get(2).getLatitude(), shopWithinRange.get(2).getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLatLng, 13), 2000, null);
                currentShopName.setText(shopWithinRange.get(2).getName());
                currentShopAddress.setText(shopWithinRange.get(2).getAddress());
                Picasso.with(getApplicationContext()).load(shopWithinRange.get(2).getImage()).into(currentImage);
                bottomPanel.setVisibility(View.VISIBLE);
            }
        });

        my_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomPanel.setVisibility(View.GONE);
                locateYourself();
            }
        });

//        locateYourself();
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

    public void locateYourself() {
        statusCheck();
        if (ContextCompat.checkSelfPermission(NearbyLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocation, 17), 2000, null);
                bottomPanel.setVisibility(View.GONE);
            }
        }
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
        LatLng shopLocation = new LatLng(22.28552, 114.15769);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(shopLocation, 10));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(shopLocation, 9), 2000, null);
        // Add a marker in place and move the camera
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

//        shop_location_button.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View v) {
//        bottomPanel.setVisibility(View.VISIBLE);
//
//        LatLng shopLocation = new LatLng(shop.getLatitude(), shop.getLongitude());
//        Marker shopLocationMarker = mMap.addMarker(new MarkerOptions().position(shopLocation).title("Shop is here")
//        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
////                mMap.moveCamera(CameraUpdateFactory.newLatLng(shopLocation));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(shopLocation, 17), 2000, null);
//        shopLocationMarker.showInfoWindow();
//
//        CircleOptions circleOptions = new CircleOptions();
//        circleOptions.center(shopLocation);
//        circleOptions.radius(20);
//        circleOptions.strokeColor(Color.argb(150, 89, 214, 214));
//        mMap.addCircle(circleOptions);
//        }
//        });

