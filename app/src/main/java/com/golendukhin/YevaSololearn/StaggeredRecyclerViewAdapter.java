package com.golendukhin.YevaSololearn;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import butterknife.BindView;

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
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String title = feedItems.get(i).getTitle();
        String category = feedItems.get(i).getCategory();
        String imageURL = feedItems.get(i).getImageUrl();
        String id = feedItems.get(i).getId();
        final Feed feed = new Feed(title, category, imageURL, id);

        final TextView titleTextView = viewHolder.titleTextView;
        final TextView categoryTextView = viewHolder.categoryTextView;
        final ImageView imageView = viewHolder.imageView;

        titleTextView.setText(title);
        categoryTextView.setText(category);

        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.ic_launcher_background);
        Glide.with(context)
                .load(imageURL)
                .apply(requestOptions)
                .into(imageView);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("feed", feed);

                Pair[] pairs = new Pair[3];
                pairs[0] = new Pair<View, String>(titleTextView, "title_transition");
                pairs[1] = new Pair<View, String>(categoryTextView, "category_transition");
                pairs[2] = new Pair<View, String>(imageView, "image_transition");

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity)context, pairs);
                context.startActivity(intent, activityOptions.toBundle());
            }
        });

    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }
}