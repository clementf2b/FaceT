package fyp.hkust.facet.mMultipleColorRecycler;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import fyp.hkust.facet.R;

/**
 * Created by ClementNg on 31/3/2017.
 */

public class MultipleColorAdapter extends RecyclerView.Adapter<MultipleColorHolder> {

    private final static String TAG = " MakeupProductAdapter ";
    private final Context c;
    private ArrayList<ArrayList<String>> colorArray;


    public MultipleColorAdapter(Context c, ArrayList<ArrayList<String>> colorArray) {
        this.c = c;
        this.colorArray = colorArray;
    }

    @Override
    public MultipleColorHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.color_set_row, viewGroup, false);
        MultipleColorHolder viewHolder = new MultipleColorHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MultipleColorHolder viewHolder, int position) {
        Log.d(TAG + " onBindViewHolder position", position + "");
        Log.d(TAG + " data ", colorArray.toString());
        for (int i = 0; i < colorArray.get(position).size(); i++) {
            if (colorArray.get(position).get(i) != null) {
                Log.d(TAG + " onBindViewHolder", colorArray.get(position).get(i).toString());
                viewHolder.product_color_image[i].setColorFilter(Color.parseColor(colorArray.get(position).get(i)));
                viewHolder.product_color_image[i].setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return colorArray.size();
    }

}
