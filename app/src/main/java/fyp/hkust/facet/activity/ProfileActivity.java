package fyp.hkust.facet.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import fyp.hkust.facet.fragment.MatchedProductFragment;
import fyp.hkust.facet.fragment.OwnProductFragment;
import fyp.hkust.facet.util.FontManager;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private TabLayout tabLayout;
    private ViewPager viewPager;

    private static final String NAV_ITEM_ID = "nav_index";
    private CircleImageView profilePic;
    private TextView mOwnNameField;
    private Button btnEdit;
    private Uri mImageUri;
    private RecyclerView recentlyMatchProductList;

    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageProfileImage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

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

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabase.keepSynced(true);

        profilePic = (CircleImageView)findViewById(R.id.profile_picture);
        mOwnNameField = (TextView) findViewById(R.id.profile_username);
        btnEdit = (Button) findViewById(R.id.btn_edit);

        mDatabaseUsers.keepSynced(true);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null)
                {
                    Intent loginIntent = new Intent(ProfileActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                startActivity(intent);
            }
        });

        checkUserExist();
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
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OwnProductFragment(), getResources().getString(R.string.own_product_fragment_text));
        adapter.addFragment(new MatchedProductFragment(), getResources().getString(R.string.match_product_fragment_text));
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

    public static class OwnProductViewHolder extends RecyclerView.ViewHolder{

        View mView;


        public OwnProductViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

//        public void setTitle(String title)
//        {
//            TextView own_product_title = (TextView) mView.findViewById(R.id.own_p_title);
//            own_product_title.setText(title);
//        }
//
//        public void setDesc(String desc)
//        {
//            TextView own_product_desc = (TextView) mView.findViewById(R.id.own_p_desc);
//            own_product_desc.setText(desc);
//        }
//
//        public void setUsername(String username)
//        {
//            TextView own_product_username = (TextView) mView.findViewById(R.id.own_p_username);
//            own_product_username.setText(username);
//        }

        public void setImage(final Context ctx, final  String image)
        {
            final ImageView own_post_image = (ImageView) mView.findViewById(R.id.own_product_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(own_post_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(ctx)
                            .load(image)
                            .fit()
                            .centerCrop()
                            .into(own_post_image);
                }
            });

        }
    }


}
