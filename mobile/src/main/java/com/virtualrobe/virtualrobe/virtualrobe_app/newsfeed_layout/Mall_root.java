package com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.virtualrobe.virtualrobe.virtualrobe_app.R;

public class Mall_root extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    /* Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.mall_root_fragment, container, false);

        try {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.flContent, new shopping_mall());
            transaction.commit();
        }
        catch (Exception e){

        }

        return view;
    }
}
