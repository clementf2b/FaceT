package fyp.hkust.facet.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.adapter.ViewPagerAdapter;
import fyp.hkust.facet.fragment.FavouriteProductFragment;
import fyp.hkust.facet.fragment.OwnProductFragment;
import fyp.hkust.facet.model.Notification;
import fyp.hkust.facet.model.User;
import fyp.hkust.facet.util.FontManager;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private CircleImageView profilePic;
    private TextView mOwnNameField;
    private Button btnEdit;
    private static final String NAV_ITEM_ID = "nav_index";
    DrawerLayout drawerLayout;
    private int navItemId;
    private Toolbar toolbar;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseNotifications;
    private StorageReference mStorageProfileImage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private TextView profile_email;
    private com.melnykov.fab.FloatingActionButton add_product_fab;
    private RecyclerView mNotificaitonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.activity_profile), fontType);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        changeTabsFont();


        //start
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackground(new ColorDrawable(Color.parseColor("#00000000")));
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(ProfileActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
                navigateTo(menuItem);

                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });
        NavigationView notification_view = (NavigationView) findViewById(R.id.notification_navigation_view);
        notification_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(ProfileActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();

                drawerLayout.closeDrawer(GravityCompat.END); /*Important Line*/

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
            navItemId = R.id.nav_profile;
        }

        navigateTo(view.getMenu().findItem(navItemId));
        //end
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(ProfileActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabaseNotifications = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mDatabaseNotifications.keepSynced(true);

        profilePic = (CircleImageView) findViewById(R.id.profile_picture);
        mOwnNameField = (TextView) findViewById(R.id.profile_username);
        profile_email = (TextView) findViewById(R.id.profile_email);
        btnEdit = (Button) findViewById(R.id.btn_edit);

        View notification_bottom_layout = (View)findViewById(R.id.notification_bottom_layout);
        mNotificaitonList = (RecyclerView) notification_bottom_layout.findViewById(R.id.notification_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mNotificaitonList.setLayoutManager(layoutManager);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                startActivity(intent);
            }
        });

        add_product_fab = (com.melnykov.fab.FloatingActionButton) findViewById(R.id.add_product_fab);
        add_product_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, PostActivity.class);
                startActivity(intent);
            }
        });

        checkUserExist();
        if(mAuth.getCurrentUser()!=null) {
            setupUserData();
            getUserData();
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    add_product_fab.show();
                } else
                    add_product_fab.hide();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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
                        Intent mainIntent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
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

    private void getUserData() {
        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null)
                {
                    Log.i("dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                    final User user_data = dataSnapshot.getValue(User.class);
                    Log.e(user_data.getName(), "User data is null!");
                    mOwnNameField.setText(user_data.getName());
                    profile_email.setText(mAuth.getCurrentUser().getEmail());

                    Picasso.with(getApplicationContext()).load(user_data.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(profilePic, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext())
                                    .load(user_data.getImage())
                                    .centerCrop()
                                    .fit()
                                    .into(profilePic);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setupUserData() {

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    Log.i("dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                    final User user_data = dataSnapshot.getValue(User.class);
                    Log.e(user_data.getName(), "User data is null!");
                    mOwnNameField.setText(user_data.getName());
                    profile_email.setText(user_data.getEmail());
                    Picasso.with(getApplicationContext()).load(user_data.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(profilePic, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext())
                                    .load(user_data.getImage())
                                    .centerCrop()
                                    .fit()
                                    .into(profilePic);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OwnProductFragment(), getResources().getString(R.string.own_product_fragment_text));
        adapter.addFragment(new FavouriteProductFragment(), getResources().getString(R.string.favourite_product_fragment_text));
        viewPager.setAdapter(adapter);
    }

    private void changeTabsFont() {
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(fontType, Typeface.NORMAL);
                }
            }
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_notification) {
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

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        if(mAuth.getCurrentUser()!=null) {
            final DatabaseReference currentUserNotification = mDatabaseNotifications.child(mAuth.getCurrentUser().getUid());
            FirebaseRecyclerAdapter<Notification, NotificationViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notification, NotificationViewHolder>(

                    Notification.class,
                    R.layout.notification_row,
                    NotificationViewHolder.class,
                    currentUserNotification

            ) {
                @Override
                protected void populateViewHolder(NotificationViewHolder viewHolder, final Notification model, int position) {

                    Log.d(TAG, "loading view " + position);
                    Log.d(TAG, model.getSender_username());
                    final String product_id = getRef(position).getKey();
                    viewHolder.setAction(model.getProduct_name(), model.getAction(), model.getSender_username());
                    viewHolder.setTime(model.getTime());
                    viewHolder.setImage(getApplicationContext(), model.getProduct_image());

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent productDetailIntent = new Intent();
                            productDetailIntent.setClass(ProfileActivity.this, ProductDetailActivity.class);
                            productDetailIntent.putExtra("product_id", product_id);
                            Log.d(TAG + " product_id", product_id);
                            productDetailIntent.putExtra("colorNo", model.getColorNo());
                            Log.d(TAG + " colorNo", model.getColorNo()+"");
                            startActivity(productDetailIntent);
                        }
                    });

                    Log.d(TAG, "finish loading view");
                }
            };

            Log.d(TAG, " Notification : " + mDatabaseNotifications.child(mAuth.getCurrentUser().getUid()));
            mNotificaitonList.setAdapter(firebaseRecyclerAdapter);
        }
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private Typeface customTypeface = Typeface.createFromAsset(itemView.getContext().getAssets(), FontManager.APP_FONT);

        public NotificationViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setAction(String productName, String action, String senderUsername) {
            TextView action_textview = (TextView) mView.findViewById(R.id.action_textview);
            Log.d("setAction", productName + " " + action + " " + senderUsername);
            action_textview.setText(productName + " " + action + " " + senderUsername);
            action_textview.setTypeface(customTypeface);
        }

        public void setTime(String time) {
            TextView time_textview = (TextView) mView.findViewById(R.id.time_textview);
            time_textview.setText(time);
            time_textview.setTypeface(customTypeface);
        }

        public void setImage(final Context ctx, final String image) {
            final ImageView product_image = (ImageView) mView.findViewById(R.id.product_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(product_image, new Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "image loading success !");
                }

                @Override
                public void onError() {
                    Log.d(TAG, "image loading error !");
                    Picasso.with(ctx)
                            .load(image)
                            .resize(90, 90)
                            .centerCrop()
                            .into(product_image);
                }
            });
        }
    }
}
