package fyp.hkust.facet.fragment;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fyp.hkust.facet.R;
import fyp.hkust.facet.util.FontManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatchedProductFragment extends Fragment {


    public MatchedProductFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_matched_product, container, false);

        Typeface fontType = FontManager.getTypeface(getContext(), FontManager.APP_FONT);
        FontManager.markAsIconContainer(view.findViewById(R.id.fragment_favourite_layout), fontType);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
    }

}
