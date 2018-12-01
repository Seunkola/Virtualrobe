package com.virtualrobe.virtualrobe.virtualrobe_app.Search.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.FirestoreAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.outfit_model;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyOutfitsRecyclerViewAdapter extends FirestoreAdapter<MyOutfitsRecyclerViewAdapter.ViewHolder> {
    public interface OnFeedSelectedListener {

        void onfeedSelected(DocumentSnapshot user);

    }

    private OnFeedSelectedListener mListener;

    public MyOutfitsRecyclerViewAdapter(Query query, OnFeedSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.fragment_outfits, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.picture)
        ImageView imageView;

        @BindView(R.id.description)
        TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnFeedSelectedListener listener) {
            outfit_model model= snapshot.toObject(outfit_model.class);

            Glide.with(imageView.getContext())
                    .load(model.getImage_thumbnailsm())
                    .error(R.drawable.error_placeholder)
                    .into(imageView);

            description.setText(model.getDescribe());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onfeedSelected(snapshot);
                }
            });
        }
    }
}
