package fyp.hkust.facet.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import fyp.hkust.facet.R;
import fyp.hkust.facet.mMultipleColorRecycler.MultipleColorAdapter;
import fyp.hkust.facet.mMultipleColorRecycler.MultipleColorHolder;

/**
 * Created by ClementNg on 31/3/2017.
 */

public class MultipleColorFragment extends DialogFragment {

    private final String TAG = "MultipleColorFragment";
    RecyclerView rv;
    ArrayList<ArrayList<String>> data;


    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.multiple_color_fragment_layout, null);

        //RECYCER
        rv = (RecyclerView) rootView.findViewById(R.id.multiple_color_recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));


        data = (ArrayList<ArrayList<String>>) getArguments().getSerializable("color");
        Log.d(TAG + " data ", data.toString());

        rv.setAdapter(new MultipleColorAdapter(this.getActivity(),data));

        builder.setTitle("Color Set");
        builder.setView(rootView).setNegativeButton("Cancel", null);

        return builder.create();
    }

}