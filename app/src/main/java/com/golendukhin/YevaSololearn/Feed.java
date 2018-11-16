package com.golendukhin.YevaSololearn;

import java.io.Serializable;

public class Feed implements Serializable {
    private String title, category, imageUrl, feedId, webUrl;
    private boolean isPinnned;

    public Feed(String feedId, String title, String category, String imageUrl,  String webUrl){
        this.feedId = feedId;
        this.title = title;
        this.category = category;
        this.imageUrl = imageUrl;
        this.webUrl = webUrl;
        this.isPinnned = false;
    }

    public void setPinnned(boolean pinnned) { isPinnned = pinnned; }

    public String getFeedId() { return feedId; }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public boolean isPinnned() { return isPinnned; }
}