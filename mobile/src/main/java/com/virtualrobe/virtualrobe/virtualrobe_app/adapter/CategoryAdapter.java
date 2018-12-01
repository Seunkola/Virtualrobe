package com.virtualrobe.virtualrobe.virtualrobe_app.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.wardrobe_model;

import butterknife.BindView;
import butterknife.ButterKnife;


public class CategoryAdapter extends FirestoreAdapter<CategoryAdapter.ViewHolder>{

    public interface OnImageSelectedListener {

        void onfeedSelected(DocumentSnapshot category);

    }

    private OnImageSelectedListener mListener;

    public CategoryAdapter(Query query, OnImageSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.category_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.background_image)
        ImageView imageView;

        @BindView(R.id.selected)
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnImageSelectedListener listener) {
            wardrobe_model model = snapshot.toObject(wardrobe_model.class);
            Resources resources = itemView.getResources();

            /* Load images*/
            Glide.with(imageView.getContext())
                    .load(model.getImage())
                    .error(R.drawable.grey)
                    .into(imageView);

            /*onclick*/
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        checkBox.toggle();
                        listener.onfeedSelected(snapshot);
                    }
                }
            });

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onfeedSelected(snapshot);
                    }
                }
            });
        }
    }
}
