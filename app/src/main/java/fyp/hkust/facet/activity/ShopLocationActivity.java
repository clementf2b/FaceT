package fyp.hkust.facet.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fyp.hkust.facet.R;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ShopLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton normal_map_button, shop_location_button, my_location_button;
    private String shop_id = null;
    private static final int RC_LOCATION_PERM = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_location);
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

        Intent intent = this.getIntent();
        shop_id = intent.getStringExtra("shop_id");

        normal_map_button = (ImageButton) findViewById(R.id.normal_map_button);
        shop_location_button = (ImageButton) findViewById(R.id.shop_location_button);
        my_location_button = (ImageButton) findViewById(R.id.my_location_button);

        normal_map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //normal map
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        shop_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LatLng shopLocation = new LatLng(22.337586, 114.265288);
                Marker shopLocationMarker = mMap.addMarker(new MarkerOptions().position(shopLocation).title("Shop is here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(shopLocation));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(shopLocation));
                shopLocationMarker.showInfoWindow();

                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(shopLocation);
                circleOptions.radius(800);
                mMap.addCircle(circleOptions);
            }
        });

        my_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ShopLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                } else {
                    // Show rationale and request permission.
                    locationTask();
                }
            }
        });
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
        LatLng shopLocation = new LatLng(22.337586, 114.265288);
        Marker shopLocationMarker = mMap.addMarker(new MarkerOptions().position(shopLocation).title("Shop is here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(shopLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(shopLocation));
        shopLocationMarker.showInfoWindow();

        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(shopLocation);
        circleOptions.radius(800);
        mMap.addCircle(circleOptions);
    }
}
