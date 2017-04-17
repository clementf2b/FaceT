package fyp.hkust.facet.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import fyp.hkust.facet.R;
import fyp.hkust.facet.adapter.ShopsAdapter;
import fyp.hkust.facet.model.Shop;

/**
 * Created by bentley on 3/4/2017.
 */

public class KowloonFragment extends Fragment {

    private List<Shop> shopList = new ArrayList<>();;
    private RecyclerView recyclerView;
    private ShopsAdapter mAdapter;
    private View view;
    private String TAG = this.getClass().getSimpleName();

    public KowloonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_kowloon, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_kowloon);

        mAdapter = new ShopsAdapter(shopList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        return view;
    }

    public void setShopList (List<Shop> shopList_kl){
        shopList = shopList_kl;
        Log.d("FragmentKL_shopList_kl", ""+shopList_kl.size());
        Log.d("FragmentKL_shopList", ""+shopList.size());
        mAdapter = new ShopsAdapter(shopList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();
    }
//
//    String shopName, String shopAddress, String shopDistrict, String shopImage, Double lantitide, Double longtitude
//    private void prepareShopData() {
//        Shop shop = new Shop ("Sasa 1", "10 A, 123 Building, Llm Garden, ABCD Street, Hang Hau", "Mong Kok", "kl",
//                    "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcTTZ6kP7VzNmI3nSl7AZW0AI02EsrDaHg6CPPiFJoEXIJXvAJb2",
//                    22.337586, 114.265288);
//        shopList.add(shop);
//
//        shop = new Shop ("Sasa 2", "10 A, 123 Building, Llm Garden, ABCD Street, Hang Hau", "Mong Kok", "kl",
//                "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcTTZ6kP7VzNmI3nSl7AZW0AI02EsrDaHg6CPPiFJoEXIJXvAJb2",
//                22.337586, 114.265288);
//        shopList.add(shop);
//
//        shop = new Shop ("Sasa 3", "10 A, 123 Building, Llm Garden, ABCD Street, Hang Hau", "Mong Kok", "kl",
//                "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcTTZ6kP7VzNmI3nSl7AZW0AI02EsrDaHg6CPPiFJoEXIJXvAJb2",
//                22.337586, 114.265288);
//        shopList.add(shop);
//
//        shop = new Shop ("Sasa 4", "10 A, 123 Building, Llm Garden, ABCD Street, Hang Hau", "Mong Kok", "kl",
//                "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcTTZ6kP7VzNmI3nSl7AZW0AI02EsrDaHg6CPPiFJoEXIJXvAJb2",
//                22.337586, 114.265288);
//        shopList.add(shop);
//
//        Log.d("shop_kow", shopList.size()+"");
//
//        mAdapter.notifyDataSetChanged();
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
