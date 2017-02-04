package fyp.hkust.facet.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
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
import android.widget.ImageButton;
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
import fyp.hkust.facet.Product;
import fyp.hkust.facet.R;
import fyp.hkust.facet.User;
import fyp.hkust.facet.util.FontManager;

public class AccountActivity extends AppCompatActivity{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String NAV_ITEM_ID = "nav_index";
    private CircleImageView profilePic;
    private TextView mNameField;
    private Uri mImageUri;
    private RecyclerView recentlyMatchProductList;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageProfileImage;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private LinearLayoutManager horizontalLayoutManagaer;

    DrawerLayout drawerLayout;
    TextView contentView;
    private int navItemId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //start
        Typeface fontType = FontManager.getTypeface(getApplicationContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(findViewById(R.id.account_main_layout), fontType);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackground(new ColorDrawable(Color.parseColor("#00000000")) );
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(AccountActivity.this, menuItem.getTitle() + " pressed", Toast.LENGTH_LONG).show();
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

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabase.keepSynced(true);

        profilePic = (CircleImageView)findViewById(R.id.profilepic);
        mNameField = (TextView) findViewById(R.id.accountnamefield);

        final String user_id = mAuth.getCurrentUser().getUid();
        mDatabaseUsers.keepSynced(true);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    Intent loginIntent = new Intent(AccountActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        // username change listener
        mDatabaseUsers.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.e("dataSnapshot.getValue()", dataSnapshot.getValue().toString());
                final User user = dataSnapshot.getValue(User.class);
                Log.e(user.getName(), "User data is null!");
                mNameField.setText(user.getName());
                Picasso.with(getApplicationContext()).load(user.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(profilePic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        Picasso.with(getApplicationContext())
                                .load(user.getImage())
                                .resize(150, 150)
                                .centerCrop()
                                .into(profilePic);
                    }
                });

                // Check for null
                if (user == null) {
                    Log.e(TAG, "User data is null!");
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read username value.", error.toException());
            }
        });


        //Recently Match list

        recentlyMatchProductList = (RecyclerView) findViewById(R.id.recentMatchProductList);
        horizontalLayoutManagaer = new LinearLayoutManager(AccountActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recentlyMatchProductList.setLayoutManager(horizontalLayoutManagaer);

        checkUserExist();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateTo(MenuItem menuItem){
       // contentView.setText(menuItem.getTitle());

        navItemId = menuItem.getItemId();
        //Check to see which item was being clicked and perform appropriate action
        switch (navItemId) {
            //Replacing the main content with ContentFragment Which is our Inbox View;
            case R.id.nav_camera:
                navItemId = 0;
                break;
            case R.id.nav_gallery:
                navItemId = 1;
                break;
            case R.id.nav_manage:
                navItemId = 2;
                break;
            case R.id.nav_send:
                navItemId = 3;
                break;
            case R.id.nav_share:
                navItemId = 4;
                break;
            case R.id.nav_slideshow:
                startActivity(new Intent(AccountActivity.this, CategoryActivity.class));
                break;
            case R.id.navigation_view:
                break;
            default:
                navItemId = 0;
        }
        Snackbar.make(drawerLayout, navItemId + " Clicked" ,Snackbar.LENGTH_SHORT).show();
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

        FirebaseRecyclerAdapter<Product,AccountActivity.RecenlyMatchProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, AccountActivity.RecenlyMatchProductViewHolder>(

                Product.class,
                R.layout.recently_match_product_row,
                AccountActivity.RecenlyMatchProductViewHolder.class,
                mDatabase.limitToLast(10)
        ) {
            @Override
            protected void populateViewHolder(AccountActivity.RecenlyMatchProductViewHolder rm_viewHolder, Product model, int position) {

                rm_viewHolder.setTitle(model.getTitle());
                rm_viewHolder.setDesc(model.getDesc());
                rm_viewHolder.setImage(getApplicationContext(),model.getImage());
                rm_viewHolder.setUsername(model.getUsername());
            }
        };

        recentlyMatchProductList.setAdapter(firebaseRecyclerAdapter);
        recentlyMatchProductList.setNestedScrollingEnabled(false);
        recentlyMatchProductList.isScrollbarFadingEnabled();
    }

    public static class RecenlyMatchProductViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public RecenlyMatchProductViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title)
        {
            TextView rm_product_title = (TextView) mView.findViewById(R.id.rm_p_title);
            rm_product_title.setText(title);
        }

        public void setDesc(String desc)
        {
            TextView rm_product_desc = (TextView) mView.findViewById(R.id.rm_p_desc);
            rm_product_desc.setText(desc);
        }

        public void setUsername(String username)
        {
            TextView rm_product_username = (TextView) mView.findViewById(R.id.rm_p_username);
            rm_product_username.setText(username);
        }

        public void setImage(final Context ctx, final  String image)
        {
            final ImageView rm_post_image = (ImageView) mView.findViewById(R.id.rm_product_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(rm_post_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(ctx)
                            .load(image)
                            .resize(100, 100)
                            .centerCrop()
                            .into(rm_post_image);
                }
            });

        }
    }

    private void checkUserExist() {

        if(mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent mainIntent = new Intent(AccountActivity.this, ProfileEditActivity.class);
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

    private void logout()
    {
        mAuth.signOut();
    }

}
