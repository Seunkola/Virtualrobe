package com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.FirestoreAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Shopping_items;

import org.w3c.dom.Document;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SectionAdapter extends FirestoreAdapter<SectionAdapter.ViewHolder> {

    public interface ItemListener{
        void itemSelected(DocumentSnapshot snapshot);
    }
    private ItemListener listener;
    public SectionAdapter(Query query,ItemListener listener){
        super(query);
        this.listener = listener;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.section_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.background_image)
        ImageView listing_image;

        @BindView(R.id.title)
        TextView title;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.seller)
        TextView seller;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
        public void bind(final DocumentSnapshot snapshot,
                         final ItemListener listener) {
            Shopping_items items = snapshot.toObject(Shopping_items.class);

            Picasso.with(listing_image.getContext())
                    .load(items.getImage_thumbnail())
                    .placeholder(R.drawable.grey)
                    .error(R.drawable.error_placeholder)
                    .into(listing_image);

            title.setText(items.getItem_name());
            price.setText(items.getPrice());
            seller.setText(items.getMerchant_store());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.itemSelected(snapshot);
                }
            });
        }


    }
}
