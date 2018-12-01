package com.virtualrobe.virtualrobe.virtualrobe_app.help;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.virtualrobe.virtualrobe.virtualrobe_app.MainActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.Start_up_Page;


public class TermsAndPolicy extends Fragment {
    Button privacy,terms;
    ScrollView pricay_scroll,terms_scroll;
    int count_privacy = 1;
    int count_terms = 1;
    RelativeLayout accept;
    Button accept_button;

    public TermsAndPolicy(){}
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.terms_policy, container, false);
        privacy = (Button)view.findViewById(R.id.btn_privacy);
        terms = (Button)view.findViewById(R.id.btn_terms);
        pricay_scroll = (ScrollView)view.findViewById(R.id.privacy_contents);
        terms_scroll = (ScrollView)view.findViewById(R.id.terms_contents);
        accept = (RelativeLayout)view.findViewById(R.id.accept_layout);
        accept_button = (Button)view.findViewById(R.id.accept);

        Bundle args = getArguments();
        if (args!=null && args.containsKey("welcome")){
            accept.setVisibility(View.VISIBLE);
        }

        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pricay_scroll.getVisibility() == View.GONE){
                    pricay_scroll.setVisibility(View.VISIBLE);
                }
                else {
                    pricay_scroll.setVisibility(View.GONE);
                }

            }
        });

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (terms_scroll.getVisibility()==View.GONE){
                    terms_scroll.setVisibility(View.VISIBLE);
                }
                else {
                    terms_scroll.setVisibility(View.GONE);
                }
            }
        });

        accept_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
            }
        });

        return view;
    }

    public void accept(){
        Intent next = new Intent(getActivity(), Start_up_Page.class);
        startActivity(next);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }


}
