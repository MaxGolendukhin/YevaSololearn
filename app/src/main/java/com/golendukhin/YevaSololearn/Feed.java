package com.golendukhin.YevaSololearn;

import java.io.Serializable;

public class Feed implements Serializable {
    private String title, category, imageUrl, feedId, webUrl;
    private boolean isPinnned;

    public Feed(String feedId, String title, String category, String imageUrl,  String webUrl, boolean isPinnned){
        this.feedId = feedId;
        this.title = title;
        this.category = category;
        this.imageUrl = imageUrl;
        this.webUrl = webUrl;
        this.isPinnned = isPinnned;
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