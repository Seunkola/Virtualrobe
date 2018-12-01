package com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters.Fragments.Sections;
import com.virtualrobe.virtualrobe.virtualrobe_app.TypefaceSpan;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;


public class MallSections extends StatelessSection {
    private ArrayList<String> images,category;
    private Context context;
    private String title;

    private SectionListener listener;
    public interface SectionListener{
        void onSectionselected(String category);
    }

    public MallSections(String Title, ArrayList<String> images, ArrayList<String> category, Context context,SectionListener listener) {
        super(new SectionParameters.Builder(R.layout.wardrobe_list)
                .headerResourceId(R.layout.header)
                .build());

        this.images = images;
        this.category = category;
        this.context = context;
        this.title = Title;
        this.listener = listener;
    }

    @Override
    public int getContentItemsTotal() {
        return images.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;
        Glide.with(itemHolder.background_image.getContext())
                .load(images.get(position))
                .placeholder(R.drawable.grey)
                .error(R.drawable.error_placeholder)
                .into(itemHolder.background_image);

        itemHolder.category_name.setText(category.get(position));

        itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSectionselected(category.get(position));
            }
        });


    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
        SpannableString fashion = new SpannableString(title);
        fashion.setSpan(new TypefaceSpan(context, "LobsterTwo-Bold.ttf"), 0, fashion.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        headerHolder.title.setText(fashion);
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title)
        TextView title;

        HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.background_image)
        ImageView background_image;

        @BindView(R.id.category_name)
        TextView category_name;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }
}
