package com.golendukhin.YevaSololearn;

import android.app.usage.StorageStatsManager;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;

public class FeedActivity extends AppCompatActivity {

    private static final int NUM_COLUMNS = 3;

    private ArrayList<Feed> feedItems = new ArrayList<>();
    private ReferenceQueue referenceQueue;
    private JsonArrayRequest jsonArrayRequest;
    private StaggeredRecyclerViewAdapter staggeredRecyclerViewAdapter;
    private ProgressBar progressBar;


    private static final String GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search?q=Armenia&api-key=test&show-fields=thumbnail&from-date=2018-01-01&page-size=100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        initStaggeredRecyclerVIewAdapter();
    }


    private void initStaggeredRecyclerVIewAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        jsonRequest();
        staggeredRecyclerViewAdapter = new StaggeredRecyclerViewAdapter(feedItems, this);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
        staggeredRecyclerViewAdapter.notifyDataSetChanged();
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

                            feedItems.add(new Feed(title, category, imageUrl));
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
}