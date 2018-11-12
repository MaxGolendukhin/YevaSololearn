package com.golendukhin.YevaSololearn;

import java.io.Serializable;

class Feed implements Serializable {
    private String title, category, imageUrl, id;
    private boolean isPinnned;

    Feed(String title, String category, String imageUrl, String id){
        this.title = title;
        this.category = category;
        this.imageUrl = imageUrl;
        this.id = id;
        this.isPinnned = false;
    }

    public void setPinnned(boolean pinnned) { isPinnned = pinnned; }

    String getTitle() {
        return title;
    }

    String getCategory() {
        return category;
    }

    String getImageUrl() {
        return imageUrl;
    }

    String getId() { return id; }

    public boolean isPinnned() { return isPinnned;
    }
}