package com.golendukhin.YevaSololearn;

import android.app.usage.StorageStatsManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

public class FeedActivity extends AppCompatActivity {

    private static final int NUM_COLUMNS = 2;
    private ArrayList<Feed> feedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        initFakeFeedsList();
        initStaggeredRecyclerVIewAdapter();
    }

    private void initFakeFeedsList() {
        feedItems.add(new Feed("awesome title", "awesome category", getResources().getIdentifier("a" , "drawable", getPackageName())));
        feedItems.add(new Feed("awesome title", "awesome category", getResources().getIdentifier("b" , "drawable", getPackageName())));
        feedItems.add(new Feed("awesome title", "awesome category", getResources().getIdentifier("c" , "drawable", getPackageName())));
        feedItems.add(new Feed("awesome title", "awesome category", getResources().getIdentifier("d" , "drawable", getPackageName())));
        feedItems.add(new Feed("awesome title", "awesome category", getResources().getIdentifier("e" , "drawable", getPackageName())));
    }

    private void initStaggeredRecyclerVIewAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter = new StaggeredRecyclerViewAdapter(feedItems, this);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
    }
}