package fyp.hkust.facet.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fyp.hkust.facet.R;
import fyp.hkust.facet.adapter.ShopsAdapter;
import fyp.hkust.facet.model.Brand;
import fyp.hkust.facet.model.Shop;

/**
 * Created by bentley on 3/4/2017.
 */

public class HongKongIslandFragment extends Fragment {

    private List<Shop> shopList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ShopsAdapter mAdapter;
    private View view;
    private String TAG = this.getClass().getSimpleName();

    public HongKongIslandFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_hong_kong_island, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_hkIsland);
        mAdapter = new ShopsAdapter(shopList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        return view;
    }

    public void setShopList (List<Shop> shopList_hk){
        shopList = shopList_hk;
        Log.d("FragmentHK_shopList_hk", ""+shopList_hk.size());
        Log.d("FragmentHK_shopList", ""+shopList.size());
        mAdapter = new ShopsAdapter(shopList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();
    }

//    private void prepareShopData() {
//
//        mDatabaseShop = FirebaseDatabase.getInstance().getReference().child("Shop");
//        mDatabaseBrand = FirebaseDatabase.getInstance().getReference().child("Brand");
//
//        mDatabaseBrand.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                shopList.clear();
//                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                    Brand brand = ds.getValue(Brand.class);
//                    mBrand.put(ds.getKey(), brand);
//                    Log.d("mbrand", "" + mBrand.size());
//                }
//
//                mDatabaseShop.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Log.d("data", dataSnapshot.getValue().toString());
//
//
//                        //first layer: BrandID
//                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//                            Log.v("key", childDataSnapshot.getKey());//displays the key for the node
//
//                            List<Shop> temp = new ArrayList<>();
//                            temp.clear();
//                            int count = 0;
//                            //second layer: Shop's object
//                            for (DataSnapshot ds : childDataSnapshot.getChildren()) {
//                                Log.v("value", ds.getValue().toString());   //gives the value for given keyname
//
//                                Shop shop = ds.getValue(Shop.class);
//                                temp.add(shop);
//                                temp.get(count).setBrandID(childDataSnapshot.getKey());
//                                temp.get(count).setImage(mBrand.get(childDataSnapshot.getKey()).getImage());
//                                shopList.add(temp.get(count));
//                                count++;
//                                Log.d("SHOPLIST", "" + shopList.size());
//
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError error) {
//                        // Failed to read value
//                        Log.w("Error", "Failed to read shop value.", error.toException());
//                    }
//                });
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                Log.w("Error", "Failed to read brand value.", error.toException());
//            }
//        });
//
//        Log.d("shop_hk", shopList.size() + "");
//
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, " onDestroy");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, " onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, " onResume");
    }
}
