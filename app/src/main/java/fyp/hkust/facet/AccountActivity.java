package fyp.hkust.facet;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ImageButton profilePic;
    private TextView mNameField;
    private Uri mImageUri;
    private RecyclerView recentlyMatchProductList;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageProfileImage;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private LinearLayoutManager horizontalLayoutManagaer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabase.keepSynced(true);

        profilePic = (ImageButton)findViewById(R.id.profilepic);
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
                        Intent mainIntent = new Intent(AccountActivity.this, SetupActivity.class);
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
