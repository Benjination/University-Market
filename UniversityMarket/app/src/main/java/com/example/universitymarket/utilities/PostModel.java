package com.example.universitymarket.utilities;

public class PostModel {
    private String post_name;
    private int imgid;

    public PostModel(String post_name, int imgid) {
        this.post_name = post_name;
        this.imgid = imgid;
    }

    public String getPost_name() {
        return post_name;
    }

    public void setPost_name(String post_name) {
        this.post_name = post_name;
    }

    public int getImgid() {
        return imgid;
    }

    public void setImgid(int imgid) {
        this.imgid = imgid;
    }
}
