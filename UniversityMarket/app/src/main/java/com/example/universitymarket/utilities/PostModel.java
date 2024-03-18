package com.example.universitymarket.utilities;

public class PostModel {
    private String post_name;
    private String imageURL;

    public PostModel(String post_name, String imageURL) {
        this.post_name = post_name;
        this.imageURL = imageURL;
    }

    public String getPost_name() {
        return post_name;
    }

    public void setPost_name(String post_name) {
        this.post_name = post_name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImgid(String imageURL) {
        this.imageURL = imageURL;
    }
}
