package fyp.hkust.facet.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import fyp.hkust.facet.R;
import fyp.hkust.facet.adapter.SelectColorRecyclerAdapter;
import fyp.hkust.facet.util.CallbackItemTouch;
import fyp.hkust.facet.util.MyItemTouchHelperCallback;

/**
 * Created by ClementNg on 31/3/2017.
 */

public class ColorSelectFragment extends DialogFragment implements CallbackItemTouch {

    private final String TAG = "SwapColorFragment";
    RecyclerView color_select_recyclerview;
    ArrayList<String> data;
    private SelectColorRecyclerAdapter adapter;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.color_select_fragment_layout, null);

        LinearLayoutManager llm
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        //RECYCER
        color_select_recyclerview = (RecyclerView) rootView.findViewById(R.id.color_select_recyclerview);
        color_select_recyclerview.setLayoutManager(llm);

        data = (ArrayList<String>) getArguments().getSerializable("selectedColor");
        adapter = new SelectColorRecyclerAdapter(getActivity(),data);
        Log.d(TAG + " data ", data.toString());

        color_select_recyclerview.setAdapter(new SelectColorRecyclerAdapter(this.getActivity(), data));

        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallback(this);// create MyItemTouchHelperCallback
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback); // Create ItemTouchHelper and pass with parameter the MyItemTouchHelperCallback
        touchHelper.attachToRecyclerView(color_select_recyclerview); // Attach ItemTouchHelper to RecyclerView

        builder.setTitle("Swap Color position");
        builder.setView(rootView).setPositiveButton("Ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                passData(data);
                dialog.dismiss();
            }
        });

        builder.setView(rootView).setNegativeButton("Cancel", null);

        return builder.create();
    }

    public interface OnDataPass {
        public void onDataPass(List<String> data);
    }
    OnDataPass dataPasser;

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        dataPasser = (OnDataPass) a;
    }

    public void passData(List<String> data) {
        dataPasser.onDataPass(data);
    }

    @Override
    public void itemTouchOnMove(int oldPosition, int newPosition) {
        data.add(newPosition,data.remove(oldPosition));// change position
        adapter.notifyItemMoved(oldPosition, newPosition); //notifies changes in adapter, in this case use the notifyItemMoved
    }
}