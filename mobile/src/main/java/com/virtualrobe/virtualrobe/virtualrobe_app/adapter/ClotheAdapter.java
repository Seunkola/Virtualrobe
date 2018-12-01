package com.virtualrobe.virtualrobe.virtualrobe_app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Clothes;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClotheAdapter extends FirestoreAdapter<ClotheAdapter.ViewHolder> {

    public interface OnFeedSelectedListener {

        void onfeedSelected(DocumentSnapshot outfit);

    }

    private OnFeedSelectedListener mListener;

    public ClotheAdapter(Query query, OnFeedSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.singe_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.picture)
        SquareImageView imageView;

        @BindView(R.id.description)
        TextView textView;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnFeedSelectedListener listener) {
            Clothes clothes = snapshot.toObject(Clothes.class);

            Glide.with(imageView.getContext())
                    .load(clothes.getThumbnail())
                    .error(R.drawable.error_placeholder)
                    .into(imageView);

            textView.setText(clothes.getBrand());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onfeedSelected(snapshot);
                }
            });
        }
    }

}
