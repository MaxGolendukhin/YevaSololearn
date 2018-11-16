package com.golendukhin.YevaSololearn.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.golendukhin.YevaSololearn.DetailsActivity;
import com.golendukhin.YevaSololearn.Feed;
import com.golendukhin.YevaSololearn.R;

import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_CATEGORY;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_FEED_ID;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_IMAGE_URL;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_TITLE;
import static com.golendukhin.YevaSololearn.dataBase.DataBaseContract.FeedEntry.COLUMN_WEB_URL;

public class PinnedItemsCursorAdapter extends CursorRecyclerViewAdapter<PinnedItemsCursorAdapter.ViewHolder> {
    Context context;

    public PinnedItemsCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.context = context;
    }

    @Override
    public PinnedItemsCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.staggered_item_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
        String category = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));
        String imageURL = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URL));
        String feedId = cursor.getString(cursor.getColumnIndex(COLUMN_FEED_ID));
        String webUrl = cursor.getString(cursor.getColumnIndex(COLUMN_WEB_URL));
        final Feed feed = new Feed(title, category, imageURL, feedId, webUrl);

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

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView, categoryTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.feed_image_view);
            this.titleTextView = itemView.findViewById(R.id.title_text_view);
            this.categoryTextView = itemView.findViewById(R.id.category_text_view);
        }
    }
}