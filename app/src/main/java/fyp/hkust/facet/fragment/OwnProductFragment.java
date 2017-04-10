package fyp.hkust.facet.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fyp.hkust.facet.R;
import fyp.hkust.facet.activity.MainActivity;
import fyp.hkust.facet.activity.ProductDetailActivity;
import fyp.hkust.facet.activity.ProfileActivity;
import fyp.hkust.facet.activity.ProfileEditActivity;
import fyp.hkust.facet.model.Product;
import fyp.hkust.facet.util.FontManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class OwnProductFragment extends Fragment {

    private static final String TAG = "OwnProductFragment";
    //No Scroll
    public static final int SCROLL_STATE_IDLE = 0;
    //User is scrolling
    public static final int SCROLL_STATE_DRAGGING = 1;
    //Auto scrolling
    public static final int SCROLL_STATE_SETTLING = 2;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private static final String NAV_ITEM_ID = "nav_index";
    private ImageButton profilePic;
    private TextView mOwnNameField;
    private Uri mImageUri;
    private RecyclerView mOwnProductList;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageProfileImage;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private GridLayoutManager mgr;
    private FragmentActivity context;
    private ownProductAdapter mOwnProductAdapter;
    private Map<String, Product> mOwnProducts = new HashMap<>();

    public OwnProductFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_own_product, container, false);

        Typeface fontType = FontManager.getTypeface(getContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(view.findViewById(R.id.fragment_own_product_layout), fontType);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabase.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mOwnProductList = (RecyclerView) view.findViewById(R.id.ownproductlist);
        mgr = new GridLayoutManager(getContext(), 3);
        mOwnProductList.setLayoutManager(mgr);

        checkUserExist();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        context = getActivity();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mAuth.getCurrentUser() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Product result = ds.getValue(Product.class);
                        if (result.getUid() == mAuth.getCurrentUser().getUid()) {
                            mOwnProducts.put(ds.getKey(), result);
                            Log.d(" product " + ds.getKey(), result.toString());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mOwnProductAdapter = new ownProductAdapter(mOwnProducts);
        mgr.setAutoMeasureEnabled(true);
        mOwnProductList.setAdapter(mOwnProductAdapter);
    }

    public class ownProductAdapter extends RecyclerView.Adapter<OwnProductViewHolder> {

        private Map<String, Product> mOwnProducts = new HashMap<>();

        public ownProductAdapter(Map<String, Product> mProducts) {
            this.mOwnProducts = mProducts;
        }

        @Override
        public OwnProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.own_product_list_row, parent, false);
            OwnProductViewHolder viewHolder = new OwnProductViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(OwnProductViewHolder viewHolder, int position) {
            List<Product> values = new ArrayList<>(mOwnProducts.values());
            final Product model = values.get(position);
            List<String> keys = new ArrayList<>(mOwnProducts.keySet());
            final String product_id = keys.get(position);

            Log.d(TAG + " product_id", product_id);
            Log.d(TAG + " product time", model.getReleaseDate() + "");
            Log.d(TAG + " product name", model.getProductName());
            Log.d(TAG + " product category", model.getCategory());

            Log.d(TAG, "loading view " + position);

            viewHolder.setImage(getContext(), model.getProductImage());

            viewHolder.mOwnProductView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent productDetailIntent = new Intent();
                    productDetailIntent.setClass(getActivity(), ProductDetailActivity.class);
                    productDetailIntent.putExtra("product_id", product_id);
                    Log.d(TAG + " product_id", product_id);
                    productDetailIntent.putExtra("colorNo", model.getColorNo());
                    Log.d(TAG + " colorNo", model.getColorNo() + "");
                    startActivity(productDetailIntent);
                }
            });

            Log.d(TAG, "finish loading view");

        }

        @Override
        public int getItemCount() {
            return mOwnProducts.size();
        }
    }

    public static class OwnProductViewHolder extends RecyclerView.ViewHolder {

        View mOwnProductView;

        public OwnProductViewHolder(View itemView) {
            super(itemView);
            mOwnProductView = itemView;
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

        public void setImage(final Context ctx, final String image) {
            final ImageView own_post_image = (ImageView) mOwnProductView.findViewById(R.id.own_product_image);
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


    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null) {
            final String user_id = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        Intent mainIntent = new Intent(getActivity(), ProfileEditActivity.class);
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

}
