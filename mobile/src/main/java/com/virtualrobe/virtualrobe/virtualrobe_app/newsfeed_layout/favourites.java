package com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.virtualrobe.virtualrobe.virtualrobe_app.R;


public class favourites extends Fragment {
    public favourites(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favourites, container, false);
        return view;
    }
}
