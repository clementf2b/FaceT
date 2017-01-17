package fyp.hkust.facet.activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import fyp.hkust.facet.R;
import fyp.hkust.facet.util.FontManager;

public class CategoryActivity extends AppCompatActivity {

    private static final String TAG = "CategoryActivity";
    private static final String NAV_ITEM_ID = "nav_index";
    DrawerLayout drawerLayout;
    TextView contentView;
    private int navItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        //start layout animation
        delayAction(0,Techniques.SlideInLeft,500,R.id.category_All);
        delayAction(500,Techniques.SlideInLeft,500,R.id.category_Face);
        delayAction(1000,Techniques.SlideInLeft,500,R.id.category_Eyes);
        delayAction(1500,Techniques.SlideInLeft,500,R.id.category_Lips);
        //end layout animation
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_category_layout), fontType);

        //start

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackground(new ColorDrawable(Color.parseColor("#00000000")) );
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(CategoryActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
                navigateTo(menuItem);

                drawerLayout.closeDrawers();

                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer){
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

        if(null != savedInstanceState){
            navItemId = savedInstanceState.getInt(NAV_ITEM_ID, R.id.nav_camera);
        }
        else{
            navItemId = R.id.nav_camera;
        }

        navigateTo(view.getMenu().findItem(navItemId));

        //end
    }

    private void delayAction(int time,final Techniques action,final int duration, final int viewId)
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after time
                YoYo.with(action)
                        .duration(duration)
                        .playOn(findViewById(viewId));
                findViewById(viewId).setVisibility(View.VISIBLE);
            }
        }, time);
    }

    private void navigateTo(MenuItem menuItem){
        // contentView.setText(menuItem.getTitle());

        navItemId = menuItem.getItemId();
        menuItem.setChecked(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, navItemId);
    }

}
