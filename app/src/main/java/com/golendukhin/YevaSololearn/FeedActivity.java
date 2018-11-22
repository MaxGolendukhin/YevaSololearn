package com.golendukhin.YevaSololearn;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
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

public class FeedActivity extends AppCompatActivity {
    private static final int NUM_COLUMNS = 3;
    private static final int INTERVAL = 10000;

    private ArrayList<Feed> feedItems = new ArrayList<>();
    private StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter;
    private PinnedItemsCursorAdapter pinnedItemsCursorAdapter;
    private ProgressBar progressBar;

    private final int pageSize = 50;
    private int page = 1;

    private static final String GUARDIAN_REQUEST_API_KEY = "test";
    private static final String GUARDIAN_REQUEST_URL =
            "http://content.guardianapis.com/search?show-fields=thumbnail&orderBy=newest&order-date=last-modified&format=json&api-key=".concat(GUARDIAN_REQUEST_API_KEY);

    private boolean isPinterestStyle = true;

    RecyclerView recyclerView;

    private Menu menu;

    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private LinearLayoutManager listItemsLayoutManager;
    private LinearLayoutManager pinnedItemsLinearLayoutManager;

    DataBaseHelper dataBaseHelper;
    Cursor cursor;

    private RecyclerView pinnedRecyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!recyclerView.canScrollVertically(1)) {
                jsonRequest();
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivity();
    }

    private void initActivity() {
        setContentView(R.layout.activity_feed_layout);
        jsonRequest();
        //runTicker();

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
        listItemsLayoutManager = new LinearLayoutManager(this);

        if(isPinterestStyle) {
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
            recyclerView.setAdapter(staggeredRecyclerViewAdapter);
        } else {
            recyclerView.setLayoutManager(listItemsLayoutManager);
            recyclerView.setAdapter(recyclerViewAdapter );
        }

        recyclerView.addOnScrollListener(onScrollListener);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
    }

    private void initPinnedItemsRecyclerView() {
        cursor = dataBaseHelper.fetchAllData();
        pinnedRecyclerView = findViewById(R.id.pinned_items_recycler_view);
        pinnedItemsLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        pinnedRecyclerView.setLayoutManager(pinnedItemsLinearLayoutManager);
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
                handler.post( new Runnable() {
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
        String request = GUARDIAN_REQUEST_URL.concat("&page-size=").concat(String.valueOf(pageSize)).concat("&page=").concat(String.valueOf(page));
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(request, null, new Response.Listener<JSONObject>() {
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
                //weird peculiarity if adapter is set, but data is still not fetched, need to update adapter
                if (staggeredRecyclerViewAdapter != null)
                    staggeredRecyclerViewAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Problems with your internet connection", Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
        page++;
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
        int firstVisiblePosition = getFirstVisiblePosition();

        switch (item.getItemId()) {
            case R.id.pinterest_style_menu:
                recyclerView.setLayoutManager(staggeredGridLayoutManager);
                recyclerView.setAdapter(staggeredRecyclerViewAdapter);

                isPinterestStyle = !isPinterestStyle;
                updateOptionsMenu();
                staggeredGridLayoutManager.scrollToPosition(firstVisiblePosition);

                return true;

            case R.id.list_style_menu:
                recyclerView.setLayoutManager(listItemsLayoutManager);
                recyclerView.setAdapter(recyclerViewAdapter);

                isPinterestStyle = !isPinterestStyle;
                updateOptionsMenu();
                listItemsLayoutManager.scrollToPosition(firstVisiblePosition);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method with most complicated logic
     * First need to obtain feed item and boolean variable if article pinned was switched or not
     * If state was not switched, there is nothing to do anymore here, quit
     * If it was, then need to obtain first visible position in pinned articles horizontal view
     * Then need to update this view in any cases with swapping the cursor with new one
     * If state was switched within staggered adapter then need to find this item in array list that populate it
     * and remove this item, notifying adapter(might be of two types) data has changed
     * In this case pinned items view is scrolled to the end
     * If state was switched from pinned items adapter, then pinned recycler view was shortened minus one position
     * Need just scroll to prevously defined first visible position
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Feed feed = (Feed)data.getSerializableExtra("feed");
        boolean isSwitched = data.getBooleanExtra("isSwitched", false);
        if (!isSwitched) {
            return;
        }

        int pinnedItemsFirstVisiblePosition = pinnedItemsLinearLayoutManager.findFirstVisibleItemPosition();
        updatePinnedItemsCursorAdapter();

        if (requestCode == STAGGERED_RECYCLE_VIEW_ADAPTER) {
            String feedId = feed.getFeedId();
            int index = 0;
            for (int i = 0; i < feedItems.size(); i++) {
                if (feedItems.get(i).getFeedId().equals(feedId)) {
                    index = i;
                    break;
                }
            }
            feedItems.remove(index);

            if (isPinterestStyle) {
                staggeredRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                recyclerViewAdapter.notifyDataSetChanged();
            }
            pinnedRecyclerView.scrollToPosition(cursor.getCount() - 1);
        } else if (requestCode == PINNED_ITEMS_CURSOR_ADAPTER) {
            pinnedRecyclerView.scrollToPosition(pinnedItemsFirstVisiblePosition);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("isPinterestStyle", isPinterestStyle);
        savedInstanceState.putInt("firstVisiblePosition", getFirstVisiblePosition());
        savedInstanceState.putSerializable("feedItems", feedItems);
        //todo another list with raw data
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        initActivity();
        int firstVisiblePosition = savedInstanceState.getInt("firstVisiblePosition");
        isPinterestStyle = savedInstanceState.getBoolean("firstVisiblePosition");
        feedItems = (ArrayList<Feed>)savedInstanceState.getSerializable("feedItems");

        if (isPinterestStyle) {
            staggeredRecyclerViewAdapter.notifyDataSetChanged();
        } else {
            recyclerViewAdapter.notifyDataSetChanged();
        }
        pinnedRecyclerView.scrollToPosition(firstVisiblePosition);
    }

    private int getFirstVisiblePosition() {
        int [] firstVisiblePositions = null;
        if (isPinterestStyle) {
            firstVisiblePositions = staggeredGridLayoutManager.findFirstVisibleItemPositions(firstVisiblePositions);
            return firstVisiblePositions[0];
        } else {
            return listItemsLayoutManager.findFirstVisibleItemPosition();
        }
    }

    @Override
    protected void onStop() {
        //dataBaseHelper.close();
        //cursor.close();
        super.onStop();
    }
}