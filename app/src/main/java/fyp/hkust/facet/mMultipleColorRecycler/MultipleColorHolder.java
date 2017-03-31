package fyp.hkust.facet.mMultipleColorRecycler;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;

/**
 * Created by ClementNg on 31/3/2017.
 */

public class MultipleColorHolder extends RecyclerView.ViewHolder {

    public int currentItem;
    public CircleImageView[] product_color_image = new CircleImageView[8];

    public MultipleColorHolder(View itemView) {
        super(itemView);

        product_color_image[0] = (CircleImageView) itemView.findViewById(R.id.product_color_image1);
        product_color_image[1] = (CircleImageView) itemView.findViewById(R.id.product_color_image2);
        product_color_image[2] = (CircleImageView) itemView.findViewById(R.id.product_color_image3);
        product_color_image[3] = (CircleImageView) itemView.findViewById(R.id.product_color_image4);
        product_color_image[4] = (CircleImageView) itemView.findViewById(R.id.product_color_image5);
        product_color_image[5] = (CircleImageView) itemView.findViewById(R.id.product_color_image6);
        product_color_image[6] = (CircleImageView) itemView.findViewById(R.id.product_color_image7);
        product_color_image[7] = (CircleImageView) itemView.findViewById(R.id.product_color_image8);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();

                Snackbar.make(v, "Click detected on item " + position,
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
