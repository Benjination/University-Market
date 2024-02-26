package com.example.universitymarket.objects;

import java.util.HashMap;
import java.util.List;

public class User extends HashMap<String, Object> {
    private String id;
    private HashMap<String, Object> about;
    private HashMap<String, Object> interactions;
    private List<String> post_ids;
    private List<String> watch_ids;
    private List<String> transact_ids;

    public User(HashMap<String, Object> rawdata) {
        super.put("id", rawdata.get("id"));
        super.put("about", rawdata.get("about"));
        super.put("interactions", rawdata.get("interactions"));
        id = (String) super.get("id");
        about = (HashMap<String, Object>) super.get("about");
        interactions = (HashMap<String, Object>) super.get("interactions");
        post_ids = (List<String>) interactions.get("post_ids");
        watch_ids = (List<String>) interactions.get("watch_ids");
        transact_ids = (List<String>) interactions.get("transact_ids");
    }

    public String getId() { return id; }

    public String getEmail() { return id; }

    public HashMap<String, Object> getAbout() { return about; }

    public HashMap<String, Object> getInteractions() { return interactions; }

    public List<String> getPostIds() { return post_ids; }

    public List<String> getWatchIds() { return watch_ids; }

    public List<String> getTransactIds() { return transact_ids; }

    public HashMap<String, Object> getSuper() {
        HashMap<String, Object> parent = this;
        return parent;
    }
}
