package fyp.hkust.facet.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import fyp.hkust.facet.activity.ProductDetailActivity;
import fyp.hkust.facet.activity.ProfileEditActivity;
import fyp.hkust.facet.model.Favourite;
import fyp.hkust.facet.model.Product;
import fyp.hkust.facet.util.FontManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavouriteProductFragment extends Fragment {

    private static final String TAG = "FavouriteProductFragment";
    private RecyclerView mFavouriteProductList;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseFavourite;
    private DatabaseReference mDatabase;
    private StorageReference mStorageProfileImage;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private GridLayoutManager mgr;
    private FragmentActivity context;
    private FavouriteProductAdapter mFavouriteProductAdapter;
    private Map<String, Product> mFavouriteProducts = new HashMap<>();
    private List<String> mFavouriteList = new ArrayList<>();

    public FavouriteProductFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_favourite_product, container, false);

        Typeface fontType = FontManager.getTypeface(getContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(view.findViewById(R.id.fragment_favourite_layout), fontType);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabase.keepSynced(true);
        mDatabaseFavourite = FirebaseDatabase.getInstance().getReference().child("Favourite");
        mDatabaseFavourite.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mFavouriteProductList = (RecyclerView) view.findViewById(R.id.favouriteproductlist);
        mgr = new GridLayoutManager(getContext(), 3);
        mFavouriteProductList.setLayoutManager(mgr);

        checkUserExist();
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        context = getActivity();

        mDatabaseFavourite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //go into product_id(key)
                if(mAuth.getCurrentUser() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d(TAG, ds.toString());
                        //go into user_id(key)
                        for (DataSnapshot ds2 : ds.getChildren()) {
                            if (ds2.getKey().equals(mAuth.getCurrentUser().getUid())) {
                                //grab the product id
                                mFavouriteList.add(ds.getKey());
                            }
                        }
                    }

                    mDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Product result = ds.getValue(Product.class);
                                if (mFavouriteList.contains(ds.getKey())) {
                                    mFavouriteProducts.put(ds.getKey(), result);
                                    Log.d(TAG + " product " + ds.getKey(), result.getProductName() + " , " + result.getUid() + " : " + mAuth.getCurrentUser().getUid());
                                }
                            }
                            mFavouriteProductAdapter = new FavouriteProductAdapter(mFavouriteProducts);
                            mgr.setAutoMeasureEnabled(true);
                            mFavouriteProductList.setAdapter(mFavouriteProductAdapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
    }

    public class FavouriteProductAdapter extends RecyclerView.Adapter<FavouriteProductViewHolder> {

        private Map<String, Product> mFavouriteProducts = new HashMap<>();

        public FavouriteProductAdapter(Map<String, Product> mProducts) {
            this.mFavouriteProducts = mProducts;
        }

        @Override
        public FavouriteProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.favoutire_product_list_row, parent, false);
            FavouriteProductViewHolder viewHolder = new FavouriteProductViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FavouriteProductViewHolder viewHolder, int position) {
            List<Product> values = new ArrayList<>(mFavouriteProducts.values());
            final Product model = values.get(position);
            List<String> keys = new ArrayList<>(mFavouriteProducts.keySet());
            final String product_id = keys.get(position);

            Log.d(TAG + " product_id", product_id);
            Log.d(TAG + " product time", model.getReleaseDate() + "");
            Log.d(TAG + " product name", model.getProductName());
            Log.d(TAG + " product category", model.getCategory());

            Log.d(TAG, "loading view " + position);

            viewHolder.setImage(getContext(), model.getProductImage());

            viewHolder.mFavouriteProductView.setOnClickListener(new View.OnClickListener() {
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
            return mFavouriteProducts.size();
        }
    }

    public static class FavouriteProductViewHolder extends RecyclerView.ViewHolder {

        View mFavouriteProductView;

        public FavouriteProductViewHolder(View itemView) {
            super(itemView);
            this.mFavouriteProductView = itemView;
        }

        public void setImage(final Context ctx, final String image) {
            final ImageView favourite_post_image = (ImageView) mFavouriteProductView.findViewById(R.id.favourite_product_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(favourite_post_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx)
                            .load(image)
                            .fit()
                            .centerCrop()
                            .into(favourite_post_image);
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
