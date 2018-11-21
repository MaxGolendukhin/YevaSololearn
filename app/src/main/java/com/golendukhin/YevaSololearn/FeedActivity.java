package com.golendukhin.YevaSololearn;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.golendukhin.YevaSololearn.adapters.PinnedItemsCursorAdapter;
import com.golendukhin.YevaSololearn.adapters.RecyclerViewAdapter;
import com.golendukhin.YevaSololearn.adapters.StaggeredRecyclerViewAdapter;
import com.golendukhin.YevaSololearn.dataBase.DataBaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


import static com.golendukhin.YevaSololearn.adapters.PinnedItemsCursorAdapter.PINNED_ITEMS_CURSOR_ADAPTER;
import static com.golendukhin.YevaSololearn.adapters.StaggeredRecyclerViewAdapter.STAGGERED_RECYCLE_VIEW_ADAPTER;

public class FeedActivity extends AppCompatActivity /*implements LoaderManager.LoaderCallbacks<Cursor>*/ {
    private static final int NUM_COLUMNS = 3;
    private static final int INTERVAL = 10000;

    private ArrayList<Feed> feedItems = new ArrayList<>();
    private StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter;
    private PinnedItemsCursorAdapter pinnedItemsCursorAdapter;
    private ProgressBar progressBar;

    private static final String GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search?q&api-key=test&show-fields=thumbnail&from-date=2018-01-01&orderBy=newest&page-size=50";

    private boolean isPinterestStyle = true;

    RecyclerView recyclerView;

    private Menu menu;

    StaggeredGridLayoutManager staggeredGridLayoutManager;

    DataBaseHelper dataBaseHelper;
    Cursor cursor;

    private RecyclerView pinnedRecyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_layout);
        runTicker();

        dataBaseHelper = new DataBaseHelper(this);
        invalidateMenu();
        initRecyclerView();
        initPinnedItemsRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);

        staggeredRecyclerViewAdapter = new StaggeredRecyclerViewAdapter(feedItems, this);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);

        recyclerViewAdapter = new RecyclerViewAdapter(feedItems, this);
        linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
    }

    private void initPinnedItemsRecyclerView() {
        cursor = dataBaseHelper.fetchAllData();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        pinnedRecyclerView.setLayoutManager(linearLayoutManager);
        pinnedItemsCursorAdapter = new PinnedItemsCursorAdapter(this, cursor);
        pinnedRecyclerView.setAdapter(pinnedItemsCursorAdapter);

        if (cursor.getCount() > 0) {
            pinnedRecyclerView.setVisibility(View.VISIBLE);
        } else {
            pinnedRecyclerView.setVisibility(View.GONE);
        }
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
                            try {
                                JSONObject item = result.getJSONObject(i);
                                String title = item.getString("webTitle");
                                String category = item.getString("sectionName");

                                JSONObject fields = item.getJSONObject("fields");
                                String imageUrl = fields.getString("thumbnail");
                                String feedId = item.getString("id");
                                String webUrl = item.getString("webUrl");

                                Feed newFeed = new Feed(feedId, title, category, imageUrl, webUrl, false);
                                if (!isInFeedList(newFeed)) {
                                    feedItems.add(newFeed);
                                }
                            } catch (JSONException e){
                                e.printStackTrace();
                                continue; //if some of fields are absent not to stop parsing request
                            }
                        }
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                //Log.e("!!!!!!!!!!!!!!!!!!!!!", String.valueOf(feedItems.size()));
                //weird bug if adapter is set, but data is still not fetched, need to update adapter
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
            if (feed.getFeedId().equals(newFeed.getFeedId())) return true;
        }
        return false;
    }

    private void invalidateMenu() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feed_activity_menu, menu);
        updateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
    }

    private void updateOptionsMenu() {
        MenuItem pinterestStyleMenu = menu.findItem(R.id.pinterest_style_menu);
        MenuItem listStyleMenu = menu.findItem(R.id.list_style_menu);
        pinterestStyleMenu.setVisible(!isPinterestStyle);
        listStyleMenu.setVisible(isPinterestStyle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int [] firstVisiblePositions = null;
        int firstVisiblePosition;
        if (isPinterestStyle) {
            firstVisiblePositions = staggeredGridLayoutManager.findFirstVisibleItemPositions(firstVisiblePositions);
            firstVisiblePosition = firstVisiblePositions[0];
        } else {
            firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
        }

        switch (item.getItemId()) {
            case R.id.pinterest_style_menu:
                recyclerView.setLayoutManager(staggeredGridLayoutManager);
                recyclerView.setAdapter(staggeredRecyclerViewAdapter);

                isPinterestStyle = !isPinterestStyle;
                updateOptionsMenu();
                staggeredGridLayoutManager.scrollToPosition(firstVisiblePosition);

                return true;

            case R.id.list_style_menu:
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(recyclerViewAdapter);

                isPinterestStyle = !isPinterestStyle;
                updateOptionsMenu();
                linearLayoutManager.scrollToPosition(firstVisiblePosition);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Feed feed = (Feed)data.getSerializableExtra("feed");
        boolean isPinned = feed.isPinnned();

        if (requestCode == PINNED_ITEMS_CURSOR_ADAPTER && !isPinned) {
            updatePinnedItemsCursorAdapter();
        } else if (requestCode == STAGGERED_RECYCLE_VIEW_ADAPTER && isPinned) {
            String feedId = feed.getFeedId();
            int index = 0;
            for (int i = 0; i < feedItems.size(); i++) {
                if (feedItems.get(i).getFeedId().equals(feedId)) {
                    index = i;
                    break;
                }
            }
            feedItems.remove(index);

            if (isPinterestStyle)
                staggeredRecyclerViewAdapter.notifyDataSetChanged();
            else
                recyclerViewAdapter.notifyDataSetChanged();

            updatePinnedItemsCursorAdapter();
            pinnedRecyclerView.scrollToPosition(cursor.getCount() - 1);
        }
    }

    private void updatePinnedItemsCursorAdapter() {
        cursor = dataBaseHelper.fetchAllData();
        pinnedItemsCursorAdapter.swapCursor(cursor);
        if (cursor.getCount() > 0) {
            pinnedRecyclerView.setVisibility(View.VISIBLE);
        } else {
            pinnedRecyclerView.setVisibility(View.GONE);
        }
    }
}