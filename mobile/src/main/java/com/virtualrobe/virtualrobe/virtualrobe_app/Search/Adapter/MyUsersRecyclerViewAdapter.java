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
import com.virtualrobe.virtualrobe.virtualrobe_app.model.user_model;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MyUsersRecyclerViewAdapter extends FirestoreAdapter<MyUsersRecyclerViewAdapter.ViewHolder> {

    public interface OnFeedSelectedListener {

        void onfeedSelected(DocumentSnapshot user);

    }

    private OnFeedSelectedListener mListener;

    public MyUsersRecyclerViewAdapter(Query query, OnFeedSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.fragment_users_search, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image)
        ImageView imageView;

        @BindView(R.id.content)
        TextView username;

        @BindView(R.id.fullname)
        TextView fullname;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnFeedSelectedListener listener) {
            user_model User = snapshot.toObject(user_model.class);

            Glide.with(imageView.getContext())
                    .load(User.getProfilePic_thumbnail())
                    .error(R.drawable.default_profile)
                    .into(imageView);

            username.setText(User.getUsername());
            fullname.setText(User.getFullname());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onfeedSelected(snapshot);
                }
            });
        }
    }
}
