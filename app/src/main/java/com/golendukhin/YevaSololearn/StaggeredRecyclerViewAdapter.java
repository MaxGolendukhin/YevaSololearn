package com.golendukhin.YevaSololearn;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class StaggeredRecyclerViewAdapter extends RecyclerView.Adapter<StaggeredRecyclerViewAdapter.ViewHolder>{
    private ArrayList<Feed> feedItems;
    private Context context;

    public StaggeredRecyclerViewAdapter(ArrayList<Feed> feedItems, Context context) {
        this.feedItems = feedItems;
        this.context = context;
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView, categoryTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.feed_image_view);
            this.titleTextView = itemView.findViewById(R.id.title_text_view);
            this.categoryTextView = itemView.findViewById(R.id.category_text_view);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.feed_item_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.titleTextView.setText(feedItems.get(i).getTitle());
        viewHolder.categoryTextView.setText(feedItems.get(i).getCategory());

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background);
        Glide.with(context)
                .load(feedItems.get(i).getImageUrl())
                .apply(requestOptions)
                .into(viewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }
}