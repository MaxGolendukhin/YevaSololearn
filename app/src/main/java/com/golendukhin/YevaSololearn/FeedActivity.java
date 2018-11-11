package com.golendukhin.YevaSololearn;


import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FeedActivity extends AppCompatActivity {
    //@BindView(R.id.toolbar_like_button) Button toolbarLikeButton;
    //@BindView(R.id.toolbar_back_button) Button toolbarBackButton;
    //@BindView(R.id.toolbar_category_text_view) TextView toolbarTitleTextView;

    private static final int NUM_COLUMNS = 3;
    private static final int INTERVAL = 30000;

    private ArrayList<Feed> feedItems = new ArrayList<>();
    private StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter;
    private ProgressBar progressBar;

    private static final String GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search?q&api-key=test&show-fields=thumbnail&from-date=2018-01-01&orderBy=newest&page-size=50";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_layout);
        //ButterKnife.bind(this);
        invalidateMenu();
        initStaggeredRecyclerVIewAdapter();
    }

    private void initStaggeredRecyclerVIewAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        runTicker();
        staggeredRecyclerViewAdapter = new StaggeredRecyclerViewAdapter(feedItems, this);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
        staggeredRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void runTicker() {
        final Handler handler = new Handler();
        Timer timer = new Timer(false);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        jsonRequest();
                    }
                });

            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, INTERVAL);
    }

    private void jsonRequest() {
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(GUARDIAN_REQUEST_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    JSONObject root;
                    try {
                        root = response.getJSONObject("response");
                        JSONArray result = root.getJSONArray("results");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject item = result.getJSONObject(i);
                            String title = item.getString("webTitle");
                            String category = item.getString("sectionName");

                            JSONObject fields = item.getJSONObject("fields");
                            String imageUrl = fields.getString("thumbnail");

                            String id = item.getString("id");

                            Feed newFeed = new Feed(title, category, imageUrl, id);
                            if (!isInFeedList(newFeed)) {
                                feedItems.add(newFeed);
                            }
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                //Log.e("!!!!!!!!!!!!!!!!!!!!!", String.valueOf(feedItems.size()));
                //freaky bug if adapter is set, but data is still not fetched, need to update adapter
                if (staggeredRecyclerViewAdapter != null)
                    staggeredRecyclerViewAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private boolean isInFeedList(Feed newFeed) {
        for (Feed feed : feedItems) {
            if (feed.getId().equals(newFeed.getId())) return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable("feedItems", feedItems);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        feedItems = (ArrayList<Feed>) savedInstanceState.getSerializable("feedItems");
        initStaggeredRecyclerVIewAdapter();
    }

    private void invalidateMenu() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub

//        menu.add("menu1");
//        menu.add("menu2");
//        menu.add("menu3");
//        menu.add("menu4");

        return super.onCreateOptionsMenu(menu);
    }
}