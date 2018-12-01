package com.virtualrobe.virtualrobe.virtualrobe_app.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.outfit_model;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class feedAdapter extends FirestoreAdapter<feedAdapter.ViewHolder> {

    public interface OnFeedSelectedListener {

        void onfeedSelected(DocumentSnapshot outfit);

    }

    private OnFeedSelectedListener mListener;

    public feedAdapter(Query query, OnFeedSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.newsfeed_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_image)
        CircleImageView user_image;

        @BindView(R.id.user_name)
        TextView user_name;

        @BindView(R.id.timestamp)
        RelativeTimeTextView timestamp;

        @BindView(R.id.outfit_image)
        ImageView outfit_image;

        @BindView(R.id.restaurant_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.restaurant_item_num_ratings)
        TextView numRatingsView;

        @BindView(R.id.share)
        Button share;

        @BindView(R.id.u_description)
        TextView username_description;

        @BindView(R.id.description)
        TextView description;

        @BindView(R.id.view_comments)
        Button viewReviews;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnFeedSelectedListener listener) {

            outfit_model outfit_model = snapshot.toObject(outfit_model.class);
            Resources resources = itemView.getResources();

            /* Load images*/
            Glide.with(user_image.getContext())
                    .load(outfit_model.getUser_profileimg())
                    .error(R.drawable.default_profile)
                    .into(user_image);

            Glide.with(outfit_image.getContext())
                    .load(outfit_model.getImage_thumbnailbg())
                    .error(R.drawable.grey)
                    .into(outfit_image);

            if (outfit_model.getuser() != null) {
                user_name.setText(outfit_model.getuser());
            }
            if (outfit_model.getNewsfeed_time() != 0) {
                long timestamps = ((long) outfit_model.getNewsfeed_time()) * 1000L;
                timestamp.setReferenceTime(timestamps);
            }
            if (outfit_model.getuser() != null && outfit_model.getDescribe() != null) {
                    username_description.setText(outfit_model.getuser());
                    description.setText(outfit_model.getDescribe());
            }
            ratingBar.setRating((float) outfit_model.getAvgRating());
            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                    outfit_model.getNumRatings()));

            // Click listener
            viewReviews.setOnClickListener(new View.OnClickListener() {
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

