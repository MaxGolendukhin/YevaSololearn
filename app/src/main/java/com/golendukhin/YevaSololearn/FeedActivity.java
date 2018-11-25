package com.golendukhin.YevaSololearn;

import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
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

import static com.golendukhin.YevaSololearn.adapters.PinnedItemsCursorAdapter.PINNED_ITEMS_CURSOR_ADAPTER;
import static com.golendukhin.YevaSololearn.adapters.StaggeredRecyclerViewAdapter.STAGGERED_RECYCLE_VIEW_ADAPTER;

public class FeedActivity extends AppCompatActivity {
    public static final String GUARDIAN_REQUEST_API_KEY = "test";
    public static final String GUARDIAN_REQUEST_URL =
            "http://content.guardianapis.com/search?show-fields=thumbnail&orderBy=newest&order-date=last-modified&format=json&api-key=".concat(GUARDIAN_REQUEST_API_KEY);
    private static final int NUM_COLUMNS = 3;
    private static final int PAGE_SIZE = 50;
    private ArrayList<Feed> feedItems = new ArrayList<>();
    private int page = 1;
    private boolean isPinterestStyle = true;

    private ProgressBar progressBar;
    private Menu menu;

    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private LinearLayoutManager listItemsLayoutManager;
    private LinearLayoutManager pinnedItemsLinearLayoutManager;
    private RecyclerView pinnedRecyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView recyclerView;
    private StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter;
    private PinnedItemsCursorAdapter pinnedItemsCursorAdapter;

    private DataBaseHelper dataBaseHelper;
    private Cursor cursor;

    private ItemsCheckService itemsCheckService;

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!recyclerView.canScrollVertically(1)) {
                jsonRequest();
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ItemsCheckService.LocalBinder binder = (ItemsCheckService.LocalBinder) service;
            itemsCheckService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_layout);
        dataBaseHelper = new DataBaseHelper(this);

        int firstVisiblePosition = 0;
        int pinnedItemsFirstVisiblePosition = 0;
        if (savedInstanceState != null) {
            firstVisiblePosition = savedInstanceState.getInt("firstVisiblePosition");
            pinnedItemsFirstVisiblePosition = savedInstanceState.getInt("pinnedItemsFirstVisiblePosition");
            isPinterestStyle = savedInstanceState.getBoolean("isPinterestStyle");
            feedItems = (ArrayList<Feed>) savedInstanceState.getSerializable("feedItems");
        } else { //normal mode
            jsonRequest();
        }

        invalidateMenu();
        initRecyclerView();
        initPinnedItemsRecyclerView();

        if (savedInstanceState != null) {
            if (isPinterestStyle) {
                staggeredRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                recyclerViewAdapter.notifyDataSetChanged();
            }
            recyclerView.scrollToPosition(firstVisiblePosition);
            pinnedRecyclerView.scrollToPosition(pinnedItemsFirstVisiblePosition);
        }
    }

    /**
     * If service, that checks for new items is still running need to stop it
     */
    @Override
    public void onStart() {
        super.onStart();
        itemsCheckService = new ItemsCheckService();
        bindToService();
        itemsCheckService.onDestroy();
    }

    /**
     * Menu is created dynamically, depend on which view state(usual or staggered) is chosen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feed_activity_menu, menu);
        updateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
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
     * If screen is rotated need to keep first visible positions of both recycler views,
     * is it a staggered view or not and list of items to populate recycler view
     * and not to fetch data again
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("isPinterestStyle", isPinterestStyle);
        savedInstanceState.putInt("firstVisiblePosition", getFirstVisiblePosition());
        savedInstanceState.putInt("pinnedItemsFirstVisiblePosition", pinnedItemsLinearLayoutManager.findFirstVisibleItemPosition());
        savedInstanceState.putSerializable("feedItems", feedItems);
    }

    /**
     * Method is triggered after returning from details activity
     * Method handles if item was pinned or unpinned and add or remove from vies
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Feed feed = (Feed) data.getSerializableExtra("feed");
        boolean isSwitched = data.getBooleanExtra("isSwitched", false);
        int cursorPosition = data.getIntExtra("cursorPosition", 0);
        dataBaseHelper = new DataBaseHelper(this);
        cursor = dataBaseHelper.fetchAllFeedItems();
        updatePinnedItemsLayoutVisibility();

        if (cursor.getCount() > 0) {
            pinnedItemsCursorAdapter.swapCursor(cursor);
            pinnedItemsLinearLayoutManager.scrollToPosition(cursorPosition);
        }

        if (isSwitched) {
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
                pinnedRecyclerView.scrollToPosition(cursorPosition);
            }
        } else {
            pinnedRecyclerView.scrollToPosition(cursorPosition);
        }
    }

    /**
     * If app is stopped need to start service that checks for new items
     * Also closes cursor and dataBaseHelper to avoid memory leaks
     */
    @Override
    protected void onStop() {
        super.onStop();
        bindToService();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        itemsCheckService.startCheck(this, notificationManager);
        dataBaseHelper.close();
        cursor.close();
    }

    /**
     * Items are fed from Guardian API and updates feedItems with items, that are not already fetched
     * and that sre not in cursor.
     * Also ids of all items put into the database to check via service for new ones
     * Adapter is notified.
     * If internet connection is bad user is notified via toast message
     */
    private void jsonRequest() {
        String request = GUARDIAN_REQUEST_URL.concat("&page-size=").concat(String.valueOf(PAGE_SIZE)).concat("&page=").concat(String.valueOf(page));
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
                                if (!isInFeedList(newFeed) && !isInPinnedItems(newFeed)) {
                                    feedItems.add(newFeed);
                                }

                                dataBaseHelper.addWatchedItem(feedId);
                            } catch (JSONException e) {
                                e.printStackTrace();
//                                continue; //if some of fields are absent not to stop parsing request
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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

    /**
     * Auxiliary method to check if item already in feedItems
     *
     * @param newFeed to check
     * @return true if in list, false otherwise
     */
    private boolean isInFeedList(Feed newFeed) {
        for (Feed feed : feedItems) {
            if (feed.getFeedId().equals(newFeed.getFeedId())) return true;
        }
        return false;
    }

    /**
     * Auxiliary method to check if item already in cursor
     *
     * @param newFeed to check
     * @return true if in cursor, false otherwise
     */
    private boolean isInPinnedItems(Feed newFeed) {
        return dataBaseHelper.inPinnedItems(newFeed.getFeedId());
    }

    /**
     * Invalidates menu icons visibility for this activity
     */
    private void invalidateMenu() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }

    /**
     * Initiates recycler view depending on if isPinterestStyle or not
     */
    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);

        staggeredRecyclerViewAdapter = new StaggeredRecyclerViewAdapter(feedItems, this);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);

        recyclerViewAdapter = new RecyclerViewAdapter(feedItems, this);
        listItemsLayoutManager = new LinearLayoutManager(this);

        if (isPinterestStyle) {
            recyclerView.setLayoutManager(staggeredGridLayoutManager);
            recyclerView.setAdapter(staggeredRecyclerViewAdapter);
        } else {
            recyclerView.setLayoutManager(listItemsLayoutManager);
            recyclerView.setAdapter(recyclerViewAdapter);
        }

        recyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     * Initiates pinned items recycler view depending on if pinned items exists or not
     */
    private void initPinnedItemsRecyclerView() {
        cursor = dataBaseHelper.fetchAllFeedItems();

        pinnedRecyclerView = findViewById(R.id.pinned_items_recycler_view);
        pinnedItemsLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        pinnedRecyclerView.setLayoutManager(pinnedItemsLinearLayoutManager);
        pinnedItemsCursorAdapter = new PinnedItemsCursorAdapter(this, cursor);
        pinnedRecyclerView.setAdapter(pinnedItemsCursorAdapter);
        updatePinnedItemsLayoutVisibility();
    }

    /**
     * Auxiliary to hide or show pinned recycler view depending on if pinned items exists or not
     */
    private void updatePinnedItemsLayoutVisibility() {
        if (cursor.getCount() > 0) {
            pinnedRecyclerView.setVisibility(View.VISIBLE);
        } else {
            pinnedRecyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Binds to service to check for new items
     */
    void bindToService() {
        Intent intent = new Intent(FeedActivity.this, ItemsCheckService.class);
        bindService(intent, serviceConnection, 0);
    }

    /**
     * Invalidates visibility of icons depending what state of view app is in (staggered or simple list)
     */
    private void updateOptionsMenu() {
        MenuItem pinterestStyleMenu = menu.findItem(R.id.pinterest_style_menu);
        MenuItem listStyleMenu = menu.findItem(R.id.list_style_menu);
        pinterestStyleMenu.setVisible(!isPinterestStyle);
        listStyleMenu.setVisible(isPinterestStyle);
    }

    /**
     * @return first visible position depending what state of view app is in (staggered or simple list)
     */
    private int getFirstVisiblePosition() {
        int[] firstVisiblePositions;
        if (isPinterestStyle) {
            firstVisiblePositions = staggeredGridLayoutManager.findFirstVisibleItemPositions(null);
            return firstVisiblePositions[0];
        } else {
            return listItemsLayoutManager.findFirstVisibleItemPosition();
        }
    }
}