package com.virtualrobe.virtualrobe.virtualrobe_app.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Color;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ColorAdapter extends FirestoreAdapter<ColorAdapter.ViewHolder> {

    public interface OnColorSelectedListener {

        void oncolorSelected(DocumentSnapshot color);

    }

    private OnColorSelectedListener mListener;

    public ColorAdapter(Query query, OnColorSelectedListener listener) {
        super(query);
        mListener = listener;
    }


    @NonNull
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
                         final OnColorSelectedListener listener) {
            Color color = snapshot.toObject(Color.class);

            String color_hex = color.getHex();
            if (color_hex != null && !color_hex.isEmpty()) {
                imageView.setBackgroundColor(android.graphics.Color.parseColor(color_hex));
            }

            textView.setText(color.getName());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.oncolorSelected(snapshot);
                }
            });
        }
    }
}
