package com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.TypefaceSpan;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.ColorAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.alphabetAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Alphabet;
import com.virtualrobe.virtualrobe.virtualrobe_app.Filter.color_alphabetical_filter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class fragment_select_cloth_color extends Fragment implements alphabetAdapter.OnAlphabetSelectedListener, ColorAdapter.OnColorSelectedListener {
    private String category_name;

    @BindView(R.id.page_text)
    TextView page_Header;

    @BindView(R.id.text_current_search)
    TextView mCurrentSearchView;

    @BindView(R.id.text_current_sort_by)
    TextView mCurrentSortByView;

    @BindView(R.id.recycler_color)
    RecyclerView recyclerView_Color;

    @BindView(R.id.recycler_alphabet)
    RecyclerView recyclerView_Alphabet;

    @BindView(R.id.view_empty)
    LinearLayout mEmptyView;

    private FirebaseFirestore mFirestore;
    private alphabetAdapter alphabetAdapter;
    private ColorAdapter colorAdapter;
    GridLayoutManager layoutManager, layoutManager_color;

    Query query;
    private Query color_query;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_cloth_color, container, false);
        ButterKnife.bind(this,view);

        /*get category name and set header title*/
        Bundle extras = getArguments();
        if (extras != null) {
            category_name = extras.getString("Category_name");
            if (category_name!=null && !category_name.isEmpty()){
                String header_text = "Select " + category_name + "Color";
                /*Set header title and font type*/
                SpannableString s = new SpannableString(header_text);
                s.setSpan(new TypefaceSpan(getActivity(), "LobsterTwo-Bold.ttf"), 0, s.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                page_Header.setText(s);
            }
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        //Initialize Alphabet Filter
        initAlphabet();

        //Initialize Colors
        initColors();
        initRecyclerview();

        return view;
    }

    private void initColors() {
        // GET colors
        color_query = mFirestore.collection("Colors")
                .orderBy("name",Query.Direction.ASCENDING);

    }

    private void initRecyclerview() {
        if (color_query!=null){
            colorAdapter = new ColorAdapter(color_query,this){
                @Override
                protected void onDataChanged() {
                    if (getItemCount() == 0) {
                        recyclerView_Color.setVisibility(View.GONE);
                        mEmptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView_Color.setVisibility(View.VISIBLE);
                        mEmptyView.setVisibility(View.GONE);
                    }
                }
            };
        }

        layoutManager_color = new GridLayoutManager(getContext(),4);
        recyclerView_Color.setLayoutManager(layoutManager_color);
        recyclerView_Color.setAdapter(colorAdapter);
    }

    private void initAlphabet() {

        // Get Alphabets
        Query iteems_Query = mFirestore.collection("Alphabets")
                .orderBy("Character", Query.Direction.ASCENDING);

        // RecyclerView
        alphabetAdapter = new alphabetAdapter(iteems_Query,this){
            @Override
            protected void onDataChanged() {
            }
        };

        layoutManager = new GridLayoutManager(getActivity(),12);
        recyclerView_Alphabet.setLayoutManager(layoutManager);
        recyclerView_Alphabet.setAdapter(alphabetAdapter);
    }


    @Override
    public void onalphabetSelected(DocumentSnapshot alphabet) {
        Alphabet model_alphabet = alphabet.toObject(Alphabet.class);

        // Update the query
        color_alphabetical_filter filter = new color_alphabetical_filter("Colors",
                model_alphabet.getCharacter(),mFirestore);

        color_query = filter.filter_by_alphabets();
        colorAdapter.setQuery(color_query);
    }

    @Override
    public void oncolorSelected(DocumentSnapshot color) {

    }

    @Override
    public void onStart() {
        super.onStart();
        colorAdapter.startListening();
        alphabetAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        colorAdapter.stopListening();
        alphabetAdapter.stopListening();

    }

}
