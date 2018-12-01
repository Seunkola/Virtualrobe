package com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.AutoComplete;


import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.otaliastudios.autocomplete.RecyclerViewPresenter;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.FirestoreAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Brand;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Color;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ColorPresenter extends RecyclerViewPresenter<Color> {
    protected ColorAdapter adapter;
    private String string;
    private FirebaseFirestore mFirestore;
    private Query mQuery;

    public ColorPresenter(Context context) {
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
    protected RecyclerView.LayoutManager instantiateLayoutManager() {
        return new GridLayoutManager(getContext(),2);
    }

    @Override
    protected RecyclerView.Adapter instantiateAdapter() {
        mFirestore = FirebaseFirestore.getInstance();
        data_query(string);
        startListening();
        return adapter;
    }

    private void data_query(String string) {
        mQuery = mFirestore.collection("Colors")
                .orderBy("name", Query.Direction.ASCENDING);
        adapter = new ColorAdapter(mQuery);
    }

    @Override
    protected void onQuery(@Nullable CharSequence query) {
        if (TextUtils.isEmpty(query)) {
            string = "";
            Query mQuery =  mFirestore.collection("Colors")
                    .orderBy("name", Query.Direction.DESCENDING);
            this.mQuery = mQuery;
            adapter.setQuery(this.mQuery);
        }
        else {
            /*convert character to lowercase */
            char c[] = query.toString().toCharArray();
            c[0] = Character.toUpperCase(c[0]);

            /*query database*/
            string = new String(c);
            mQuery = mFirestore.collection("Colors")
                    .orderBy("name", Query.Direction.ASCENDING)
                    .whereEqualTo("name",string);
            adapter.setQuery(this.mQuery);
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

    class ColorAdapter extends FirestoreAdapter<ColorAdapter.ViewHolder> {

        public ColorAdapter(Query query) {
            super(query);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.color_dialog, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(getSnapshot(position).toObject(Color.class));
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.color)
            ImageView color_image;

            @BindView(R.id.name)
            TextView name;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            public void bind(final Color color) {
                name.setText(color.getName());
                String color_hex = color.getHex();
                if (color_hex != null) {
                    color_image.setBackgroundColor(android.graphics.Color.parseColor(color_hex));
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dispatchClick(color);
                    }
                });
            }
        }
    }
}
