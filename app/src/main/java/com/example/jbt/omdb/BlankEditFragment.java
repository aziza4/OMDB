package com.example.jbt.omdb;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class BlankEditFragment extends Fragment {


    public BlankEditFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_blank_edit, container, false);
        Utility.setEditFragBackgroundColor(getActivity(), view);
        return view;
    }

}
