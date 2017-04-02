package fyp.hkust.facet.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.model.User;
import fyp.hkust.facet.util.CustomTypeFaceSpan;
import fyp.hkust.facet.util.FontManager;

public class CategoryActivity extends AppCompatActivity {

    private static final String TAG = "CategoryActivity";
    private static final String NAV_ITEM_ID = "nav_index";
    private NavigationView view;
    DrawerLayout drawerLayout;
    TextView contentView;
    private int navItemId;

    private RippleView ripple_all;
    private RippleView ripple_eyes;
    private RippleView ripple_face;
    private RippleView ripple_lips;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        //start layout animation
//        delayAction(0, Techniques.SlideInLeft, 500, R.id.category_All);
//        delayAction(500, Techniques.SlideInLeft, 500, R.id.category_Face);
//        delayAction(1000, Techniques.SlideInLeft, 500, R.id.category_Eyes);
//        delayAction(1500, Techniques.SlideInLeft, 500, R.id.category_Lips);
        //end layout animation
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_category_layout), fontType);

        //start
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackground(new ColorDrawable(Color.parseColor("#00000000")));
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        view = (NavigationView) findViewById(R.id.navigation_view);
        applyCustomFontToWholeMenu();
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(CategoryActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
                navigateTo(menuItem);

                drawerLayout.closeDrawers();

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
                    builder.setMessage("You need to login before using this.");
                    builder.setTitle("Warning");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent loginIntent = new Intent(CategoryActivity.this, LoginActivity.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(loginIntent);
                            // User is signed out
                            Log.d(TAG, "onAuthStateChanged:signed_out. Haven't logged in before");
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
                if (firebaseAuth.getCurrentUser() != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in user_id:" + firebaseAuth.getCurrentUser().getUid());
                }
            }
        };

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        //get user data
        setupNavHeader();
        setup();
        checkUserExist();
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

    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent mainIntent = new Intent(CategoryActivity.this, ProfileEditActivity.class);
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

    private void setup() {
        ripple_all = (RippleView) findViewById(R.id.ripple_all);
        ripple_eyes = (RippleView) findViewById(R.id.ripple_eyes);
        ripple_face = (RippleView) findViewById(R.id.ripple_face);
        ripple_lips = (RippleView) findViewById(R.id.ripple_lips);

        ripple_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "click All");
                Intent categoryAllIntent = new Intent(CategoryActivity.this, MainActivity.class);
                categoryAllIntent.putExtra("categoryResult", 0);
//                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(categoryAllIntent);
            }
        });

        ripple_eyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "click eyes");
                Intent categoryFaceIntent = new Intent(CategoryActivity.this, MainActivity.class);
//                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                categoryFaceIntent.putExtra("categoryResult", 1);
                startActivity(categoryFaceIntent);
            }
        });

        ripple_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "click face");
                Intent categoryEyesIntent = new Intent(CategoryActivity.this, MainActivity.class);
                categoryEyesIntent.putExtra("categoryResult", 2);
//                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(categoryEyesIntent);

            }
        });

        ripple_lips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "click lips");
                Intent categoryLipsIntent = new Intent(CategoryActivity.this, MainActivity.class);
                categoryLipsIntent.putExtra("categoryResult", 3);
//                accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(categoryLipsIntent);
            }
        });

    }

    private void delayAction(int time, final Techniques action, final int duration, final int viewId) {
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


    private void navigateTo(MenuItem menuItem) {
        // contentView.setText(menuItem.getTitle());

        navItemId = menuItem.getItemId();
        //Check to see which item was being clicked and perform appropriate action
        switch (navItemId) {
            //Replacing the main content with ContentFragment Which is our Inbox View;
            case R.id.nav_virtual_makeup:
                navItemId = 0;
                break;
            case R.id.nav_product:
                navItemId = 1;
                break;
            case R.id.nav_store_location:
                navItemId = 2;
                break;
            case R.id.nav_profile:
                navItemId = 3;
                break;
            case R.id.nav_setting:
//                startActivity(new Intent(AccountActivity.this, CategoryActivity.class));
                break;
            case R.id.navigation_view:
                break;
            default:
                navItemId = 0;
        }
        Snackbar.make(drawerLayout, navItemId + " Clicked", Snackbar.LENGTH_SHORT).show();
        menuItem.setChecked(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, navItemId);
    }

}
