package fyp.hkust.facet.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.util.MyItemTouchHelperCallback;

/**
 * Created by ClementNg on 12/4/2017.
 */

public class SelectColorRecyclerAdapter extends
        RecyclerView.Adapter<SelectColorRecyclerAdapter.SelectColorViewHolder>
        implements MyItemTouchHelperCallback.ItemTouchHelperAdapter {

    private final Context c;
    public List<String> selectedColorSet = new ArrayList<>();

    public SelectColorRecyclerAdapter(Context c, List<String> data) {
        this.c = c;
        this.selectedColorSet = data;
    }

    class SelectColorViewHolder extends RecyclerView.ViewHolder {

        public int currentItem;
        public de.hdodenhof.circleimageview.CircleImageView colorImage;

        public SelectColorViewHolder(View itemView) {
            super(itemView);
            colorImage = (CircleImageView) itemView.findViewById(R.id.order_product_color_card);

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

    @Override
    public SelectColorViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.show_color_layout, viewGroup, false);
        SelectColorViewHolder viewHolder = new SelectColorViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(SelectColorViewHolder viewHolder, int i) {
        viewHolder.colorImage.setColorFilter(Color.parseColor(selectedColorSet.get(i)));
    }

    @Override
    public int getItemCount() {

        return selectedColorSet.size();
    }

    @Override
    public void onItemDismiss(int position) {
        selectedColorSet.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(selectedColorSet, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(selectedColorSet, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }
}