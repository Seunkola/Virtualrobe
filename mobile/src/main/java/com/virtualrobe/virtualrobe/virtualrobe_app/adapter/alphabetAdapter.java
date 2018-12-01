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
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Alphabet;

import butterknife.BindView;
import butterknife.ButterKnife;

public class alphabetAdapter extends FirestoreAdapter<alphabetAdapter.ViewHolder> {

    public interface OnAlphabetSelectedListener {

        void onalphabetSelected(DocumentSnapshot alphabet);

    }

    private OnAlphabetSelectedListener mListener;

    public alphabetAdapter(Query query, OnAlphabetSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.alphabet_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.character)
        TextView textView;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnAlphabetSelectedListener listener) {
            Alphabet alphabet = snapshot.toObject(Alphabet.class);

            textView.setText(alphabet.getCharacter());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onalphabetSelected(snapshot);
                }
            });
        }
    }
}
