package com.example.jbt.omdb;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class BlankEditFragment extends Fragment {


    public BlankEditFragment() { }


    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View viewRoot = inflater.inflate(R.layout.fragment_blank_edit, container, false);

        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper(getActivity());
        boolean isTabletMode = sharedPrefHelper.getTabletMode();

        if (isTabletMode) {
            View rootLayout = viewRoot.findViewById(R.id.editFragLayout);
            rootLayout.setBackgroundColor(getResources().getColor(R.color.edit_background));
        }

        return viewRoot;
    }

}
