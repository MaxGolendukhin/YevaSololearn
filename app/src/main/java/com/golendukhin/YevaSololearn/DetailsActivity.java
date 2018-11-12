package com.golendukhin.YevaSololearn;

import android.os.Bundle;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {
    private Feed feed;
    @BindView(R.id.category_details_text_view) TextView categoryTextView;
    @BindView(R.id.title_details_text_view) TextView titleTextVIew;
    @BindView(R.id.details_view_image_view) ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_layout);
        ButterKnife.bind(this);
        feed = (Feed)getIntent().getSerializableExtra("feed");
        invalidateMenu();

        categoryTextView.setText(feed.getCategory());
        titleTextVIew.setText(feed.getTitle());

        Glide.with(this)
                .load(feed.getImageUrl())
                .into(imageView);
    }

    private void invalidateMenu() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_activity_menu, menu);
        MenuItem pinnedMenu = menu.findItem(R.id.pinned_menu);
        MenuItem unpinnedMenu = menu.findItem(R.id.unpinned_menu);
        pinnedMenu.setVisible(feed.isPinnned());
        unpinnedMenu.setVisible(!feed.isPinnned());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.pinned_menu:
                feed.setPinnned(false);
                supportInvalidateOptionsMenu();
                return true;

            case R.id.unpinned_menu:
                feed.setPinnned(true);
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}