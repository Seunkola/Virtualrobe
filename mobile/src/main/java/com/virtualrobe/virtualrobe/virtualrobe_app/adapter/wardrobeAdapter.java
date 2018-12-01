package com.virtualrobe.virtualrobe.virtualrobe_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.Clothes_Activity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.wardrobe_model;

import butterknife.BindView;
import butterknife.ButterKnife;

public class wardrobeAdapter extends FirestoreAdapter<wardrobeAdapter.ViewHolder> {
    public Context context;
    public String user_id;

    public wardrobeAdapter(Query query, String user_id){
        super(query);
        this.user_id = user_id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.wardrobe_list, parent, false));
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position).toObject(wardrobe_model.class),context,getSnapshot(position),user_id);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.background_image)
        ImageView background_image;

        @BindView(R.id.category_name)
        TextView category_name;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final wardrobe_model model, final Context context, final DocumentSnapshot snapshot, final String user_id) {
            Glide.with(background_image.getContext())
                    .load(model.getImage())
                    .error(R.drawable.grey)
                    .into(background_image);

            if (model.getImage_name()!=null){
                category_name.setText(model.getImage_name());
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context,Clothes_Activity.class);
                    intent.putExtra("Category",model.getImage_name());
                    intent.putExtra("Category_ID",snapshot.getId());
                    intent.putExtra("User_ID",user_id);
                    context.startActivity(intent);
                }
            });
        }
    }
}
