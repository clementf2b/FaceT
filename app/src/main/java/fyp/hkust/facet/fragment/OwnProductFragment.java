package fyp.hkust.facet.fragment;


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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import fyp.hkust.facet.R;
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

        FirebaseRecyclerAdapter<Product, ProfileActivity.OwnProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProfileActivity.OwnProductViewHolder>(

                Product.class,
                R.layout.own_product_list_row,
                ProfileActivity.OwnProductViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(ProfileActivity.OwnProductViewHolder own_viewHolder, Product model, int position) {

//                own_viewHolder.setTitle(model.getTitle());
//                own_viewHolder.setDesc(model.getDesc());
                own_viewHolder.setImage(getContext(), model.getImage());
//                own_viewHolder.setUsername(model.getUsername());
            }
        };

        mgr.setAutoMeasureEnabled(true);
        mOwnProductList.setAdapter(firebaseRecyclerAdapter);

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
