package com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.Adapter;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.ClotheAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.FirestoreAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Clothes;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.SquareImageView;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Selection_Adapter extends FirestoreAdapter<Selection_Adapter.ViewHolder> {

    private static int lastCheckedPosition = -1;

public interface OnFeedSelectedListener {

    void onfeedSelected(DocumentSnapshot cloth);

}

    private Selection_Adapter.OnFeedSelectedListener mListener;

    public Selection_Adapter(Query query, Selection_Adapter.OnFeedSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public Selection_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new Selection_Adapter.ViewHolder(inflater.inflate(R.layout.cloth_selection_list, parent, false));
    }

    @Override
    public void onBindViewHolder(Selection_Adapter.ViewHolder holder, int position) {
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
                     final Selection_Adapter.OnFeedSelectedListener listener) {
        final Clothes clothes = snapshot.toObject(Clothes.class);

        Glide.with(imageView.getContext())
                .load(clothes.getThumbnail())
                .error(R.drawable.error_placeholder)
                .into(imageView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    checkBox.setChecked(true);
                    listener.onfeedSelected(snapshot);
                    clothes.setSelected(true);
                }
            }
        });
    }
}

}
