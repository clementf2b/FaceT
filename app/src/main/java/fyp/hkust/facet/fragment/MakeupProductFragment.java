package fyp.hkust.facet.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.activity.ProductDetailActivity;
import fyp.hkust.facet.model.ProductTypeTwo;
import fyp.hkust.facet.util.FontManager;

/**
 * Created by ClementNg on 31/3/2017.
 */
public class MakeupProductFragment extends DialogFragment {

    private final String TAG = "MakeupProductFragment";
    RecyclerView rv;
    private DatabaseReference mDatabase;
    private Map<String, ProductTypeTwo> mAppliedProducts = new HashMap<>();
    private String selectedFoundationID, selectedBrushID, selectedEyeshadowID, selectedLipstickID;
    private int foundationColorPostion, brushColorPostion, eyeshadowColorPostion, lipstickColorPostion;
    private ImageView before_imageview;
    private ImageView after_imageview;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.makeup_product_fragment_layout, null);

        if (getArguments().getString("selectedFoundationID") != null) {
            selectedFoundationID = getArguments().getString("selectedFoundationID");
            foundationColorPostion = getArguments().getInt("foundationColorPosition");
            Log.d(TAG + " f ", selectedFoundationID + " " + foundationColorPostion);
        }
        if (getArguments().getString("selectedBrushID") != null) {
            selectedBrushID = getArguments().getString("selectedBrushID");
            brushColorPostion = getArguments().getInt("brushColorPosition");
            Log.d(TAG + " b ", selectedBrushID + " " + brushColorPostion);
        }
        if (getArguments().getString("selectedEyeshadowID") != null) {
            selectedEyeshadowID = getArguments().getString("selectedEyeshadowID");
            eyeshadowColorPostion = getArguments().getInt("eyeshadowColorPosition");
            Log.d(TAG + " e ", selectedEyeshadowID + " " + eyeshadowColorPostion);
        }
        if (getArguments().getString("selectedLipstickID") != null) {
            selectedLipstickID = getArguments().getString("selectedLipstickID");
            lipstickColorPostion = getArguments().getInt("lipstickColorPosition");
            Log.d(TAG + " l ", selectedLipstickID + " " + lipstickColorPostion);
        }

        getDatabaseProductData();

        before_imageview = (ImageView) rootView.findViewById(R.id.before_imageview);
        after_imageview = (ImageView) rootView.findViewById(R.id.after_imageview);
        Bitmap basicImg = getArguments().getParcelable("basicImg");
        Bitmap temp = getArguments().getParcelable("temp");
        before_imageview.setImageBitmap(basicImg);
        after_imageview.setImageBitmap(temp);

        Log.i("before height:", basicImg.getHeight() +"");
        Log.i("after height:", temp.getHeight() +"");

        rv = (RecyclerView) rootView.findViewById(R.id.makeup_product_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);

        rv.setAdapter(new MakeupProductAdapter(mAppliedProducts, this.getActivity()));
        builder.setTitle("Applied Products");

        //make dialog full screen
        Dialog d = builder.setView(rootView).setNegativeButton("Cancel", null).create();
        // (That new View is just there to have something inside the dialog that can grow big enough to cover the whole screen.)

        d.show();

        return d;
    }

    public void getDatabaseProductData() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Product");
        mDatabase.keepSynced(true);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(selectedFoundationID) || ds.getKey().equals(selectedBrushID) || ds.getKey().equals(selectedEyeshadowID) || ds.getKey().equals(selectedLipstickID)) {
                        ProductTypeTwo result = ds.getValue(ProductTypeTwo.class);
                        mAppliedProducts.put(ds.getKey(), result);
                        Log.d(" product key ", ds.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
    }

    public class MakeupProductAdapter extends RecyclerView.Adapter<MakeupProductViewHolder> {

        private Map<String, ProductTypeTwo> mMakeupProducts = new HashMap<>();
        // Allows to remember the last item shown on screen
        private int lastPosition = -1;
        private Context context;

        public MakeupProductAdapter(Map<String, ProductTypeTwo> mProducts, Context c) {
            this.context = c;
            this.mMakeupProducts = mProducts;
            notifyDataSetChanged();
        }

        @Override
        public MakeupProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.makeup_apply_product_row, parent, false);
            MakeupProductViewHolder viewHolder = new MakeupProductViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(MakeupProductViewHolder viewHolder, int position) {
            List<ProductTypeTwo> values = new ArrayList<>(mMakeupProducts.values());
            final ProductTypeTwo model = values.get(position);
            List<String> keys = new ArrayList<>(mMakeupProducts.keySet());
            final String product_id = keys.get(position);

            Log.d(TAG + " product_id", product_id);
            Log.d(TAG + " product time", model.getReleaseDate() + "");
            Log.d(TAG + " product name", model.getProductName());
            Log.d(TAG + " product category", model.getCategory());
            Log.d(TAG + " product getcolor", model.getColor().toString());
            Log.d(TAG, "loading view " + position);

//            Log.d(TAG + " product id ", product_id);
            viewHolder.setProductName(model.getProductName());
            viewHolder.setImage(getActivity(), model.getProductImage());

            if (model.getCategory().equals("Foundation")) {
                viewHolder.setProduct_color_image(model.getColor().get(foundationColorPostion));
            } else if (model.getCategory().equals("Brush")) {
                viewHolder.setProduct_color_image(model.getColor().get(brushColorPostion));
            } else if (model.getCategory().equals("Eyeshadows")) {
                viewHolder.setProduct_color_image(model.getColor().get(eyeshadowColorPostion));
            } else if (model.getCategory().equals("Lipsticks")) {
                viewHolder.setProduct_color_image(model.getColor().get(lipstickColorPostion));
            }

            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
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

            setAnimation(viewHolder.itemView, position);
            Log.d(TAG, "finish loading view");
        }

        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
                animation.setDuration(500);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return mMakeupProducts == null ? 0 : mMakeupProducts.size();
        }
    }

    public class MakeupProductViewHolder extends RecyclerView.ViewHolder {

        View mView;
        private Typeface customTypeface = Typeface.createFromAsset(itemView.getContext().getAssets(), FontManager.APP_FONT);

        public CircleImageView[] product_color_image = new CircleImageView[8];

        public MakeupProductViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProduct_color_image(ArrayList<String> colorList) {
            product_color_image[0] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image1);
            product_color_image[1] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image2);
            product_color_image[2] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image3);
            product_color_image[3] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image4);
            product_color_image[4] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image5);
            product_color_image[5] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image6);
            product_color_image[6] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image7);
            product_color_image[7] = (CircleImageView) itemView.findViewById(R.id.makeup_product_color_image8);

            Log.d(TAG + " data ", colorList.toString());
            for (int i = 0; i < colorList.size(); i++) {
                if (colorList.get(i) != null) {
                    Log.d(TAG + " onBindViewHolder", colorList.get(i).toString());
                    product_color_image[i].setColorFilter(Color.parseColor(colorList.get(i)));
                    product_color_image[i].setVisibility(View.VISIBLE);
                }
            }
        }

        public void setProductName(String productName) {
            TextView product_title = (TextView) mView.findViewById(R.id.makeup_apply_product_name);
            product_title.setText(productName);
            product_title.setTypeface(customTypeface, Typeface.BOLD);
        }

        public void setImage(final Context ctx, final String image) {
            final ImageView makeup_apply_product_image = (ImageView) mView.findViewById(R.id.makeup_apply_product_image);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(makeup_apply_product_image, new Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "image loading success !");
                }

                @Override
                public void onError() {
                    Log.d(TAG, "image loading error !");
                    Picasso.with(ctx)
                            .load(image)
                            .resize(100, 100)
                            .centerCrop()
                            .into(makeup_apply_product_image);
                }
            });
        }
    }
}