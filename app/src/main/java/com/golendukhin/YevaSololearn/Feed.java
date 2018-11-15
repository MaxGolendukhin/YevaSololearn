package com.golendukhin.YevaSololearn;

import java.io.Serializable;

class Feed implements Serializable {
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

    void setPinnned(boolean pinnned) { isPinnned = pinnned; }

    String getFeedId() { return feedId; }

    String getTitle() {
        return title;
    }

    String getCategory() {
        return category;
    }

    String getImageUrl() {
        return imageUrl;
    }

    String getWebUrl() {
        return webUrl;
    }

    boolean isPinnned() { return isPinnned; }
}