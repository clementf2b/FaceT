package fyp.hkust.facet.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ClementNg on 12/3/2017.
 */

public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

    private List<Integer> images;

    public ArrayAdapterWithIcon(Context context, List<String> items, List<Integer> images) {
        super(context, android.R.layout.select_dialog_item, items);
        this.images = images;
    }

    public ArrayAdapterWithIcon(Context context, String[] items, Integer[] images) {
        super(context, android.R.layout.select_dialog_item, items);
        this.images = Arrays.asList(images);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);

        textView.setTextColor(Color.BLACK);
        textView.setTextSize(17f);
        textView.setPadding(10, 10, 10, 10);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images.get(position), 0, 0, 0);
            textView.setCompoundDrawablePadding(30);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
            textView.setCompoundDrawablePadding(30);
        }
        textView.setCompoundDrawablePadding(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
        return view;
    }

}
