package fyp.hkust.facet.adapter;

/**
 * Created by bentley on 5/4/2017.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.model.Shop;

public class ShopsAdapter extends RecyclerView.Adapter<ShopsAdapter.ShopViewHolder> {

    private List<Shop> shopList;

    public class ShopViewHolder extends RecyclerView.ViewHolder {
        public TextView shopName, shopAddress, shopDistrict;
        public CircleImageView shopImage;
        public View view;

        public ShopViewHolder(View view) {
            super(view);
            this.view = view;
            shopName = (TextView) view.findViewById(R.id.shop_name);
            shopAddress = (TextView) view.findViewById(R.id.shop_address);
            shopDistrict = (TextView) view.findViewById(R.id.shop_district);
            shopImage = (CircleImageView) view.findViewById(R.id.shop_image);
        }

        public void setShopImage(String image)
        {
            Picasso.with(view.getContext()).load(image).into(shopImage);
        }
    }


    public ShopsAdapter(List<Shop> shopList) {
        this.shopList = shopList;
    }

    @Override
    public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shoplist_row, parent, false);

        return new ShopViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ShopViewHolder holder, int position) {

        Shop shop = shopList.get(position);
        holder.shopName.setText(shop.getShopName());
        holder.shopAddress.setText(shop.getShopAddress());
        holder.shopDistrict.setText(shop.getShopDistrict());
        holder.setShopImage(shop.getShopImage());
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }
}