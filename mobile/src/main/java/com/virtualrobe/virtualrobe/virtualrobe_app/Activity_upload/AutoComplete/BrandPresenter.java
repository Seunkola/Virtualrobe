package com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.AutoComplete;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.otaliastudios.autocomplete.RecyclerViewPresenter;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.FirestoreAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Brand;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BrandPresenter extends RecyclerViewPresenter<Brand> {
    protected BrandAdapter adapter;
    private String string;
    private FirebaseFirestore mFirestore;
    private Query mQuery;

    public BrandPresenter(Context context) {
        super(context);
    }

    @Override
    protected PopupDimensions getPopupDimensions() {
        PopupDimensions dims = new PopupDimensions();
        dims.width = 600;
        dims.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        return dims;
    }

    @Override
    protected RecyclerView.Adapter instantiateAdapter() {
        mFirestore = FirebaseFirestore.getInstance();
        data_query(string);
        return adapter;
    }

    @Override
    protected void onQuery(@Nullable CharSequence query) {
        if (TextUtils.isEmpty(query)) {
        }
        else {
            startListening();
            /*convert character to lowercase */
            char c[] = query.toString().toCharArray();
            c[0] = Character.toUpperCase(c[0]);

            /*query database*/
           /* string = new String(c);
            Query mQuery =  mFirestore.collection("Brands")
                    .orderBy("name", Query.Direction.DESCENDING)
                    .whereEqualTo("name",string);
            this.mQuery = mQuery;
            adapter.setQuery(this.mQuery);*/
        }
    }

    public void startListening(){
        if (adapter!=null){
            adapter.startListening();
        }
    }

    public void stopListening(){
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private void data_query(final String query){
        mQuery = mFirestore.collection("Brands")
                    .orderBy("name", Query.Direction.ASCENDING);
        adapter = new BrandAdapter(mQuery);
    }

    class BrandAdapter extends FirestoreAdapter<BrandAdapter.ViewHolder>{

        public BrandAdapter(Query query) {
            super(query);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.brand_dialog, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(getSnapshot(position).toObject(Brand.class));
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.brandname)
            TextView brandname;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bind(final Brand brand) {
                brandname.setText(brand.getName());
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dispatchClick(brand);
                    }
                });
            }
        }
    }
}
