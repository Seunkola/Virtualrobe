package com.virtualrobe.virtualrobe.virtualrobe_app.adapter;

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
import com.virtualrobe.virtualrobe.virtualrobe_app.model.outfit_model;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by seunk on 2/21/2018.
 */

public class OutfitAdapter extends FirestoreAdapter<OutfitAdapter.ViewHolder> {

    public interface OnFeedSelectedListener {

        void onfeedSelected(DocumentSnapshot outfit);

    }

    private OnFeedSelectedListener mListener;

    public OutfitAdapter(Query query, OnFeedSelectedListener listener) {
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

        public void bind(DocumentSnapshot snapshot, OnFeedSelectedListener mListener) {
            outfit_model outfit_model = snapshot.toObject(outfit_model.class);
            Glide.with(imageView.getContext())
                    .load(outfit_model.getImage_thumbnailbg())
                    .error(R.drawable.error_placeholder)
                    .into(imageView);

            description.setText(outfit_model.getDescribe());
        }
    }
}
