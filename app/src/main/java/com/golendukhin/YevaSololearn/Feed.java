package com.golendukhin.YevaSololearn;

class Feed {
    private String title, category;
    private String imageUrl;

    Feed(String title, String category, String imageUrl){
        this.title = title;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    String getTitle() {
        return title;
    }

    String getCategory() {
        return category;
    }

    String getImageUrl() {
        return imageUrl;
    }
}