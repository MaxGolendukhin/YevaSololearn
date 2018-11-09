package com.golendukhin.YevaSololearn;

import java.net.URL;

public class Feed {
    private String title, category;
    //private URL imageURL;
    //private String imageUrl;
    int image;

    public Feed(String title, String category, int image){
        this.title = title;
        this.category = category;
        //this.imageURL = QueryUtils.createUrl(sImageURL);
        this.image = image;


    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    //public URL getImageURL() {
        //return imageURL;
    //}

    public int getImageUrl() {
        return image;
    }
}