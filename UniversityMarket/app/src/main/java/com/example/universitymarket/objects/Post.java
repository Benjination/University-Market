package com.example.universitymarket.objects;

import com.example.universitymarket.utilities.Data;
import java.util.HashMap;
import java.util.List;

public class Post extends HashMap<String, Object> {
    private String id;
    private HashMap<String, Object> about;
    private List<Object> descriptors;
    private List<String> image_urls;
    private List<String> transact_ids;
    private HashMap<String, Object> descriptorMap = new HashMap<>();

    public Post(HashMap<String, Object> rawdata) {
        super.put("id", rawdata.get("id"));
        super.put("about", rawdata.get("about"));
        super.put("descriptors", rawdata.get("descriptors"));
        id = (String) super.get("id");
        about = (HashMap<String, Object>) super.get("about");
        descriptors = (List<Object>) super.get("descriptors");
        image_urls = (List<String>) about.get("image_urls");
        transact_ids = (List<String>) about.get("transact_ids");

        for(Object o : descriptors) {
            Data.mergeHash(descriptorMap, (HashMap<String, Object>) descriptors);
        }
    }

    public String getId() { return id; }

    public HashMap<String, Object> getAbout() { return about; }

    public List<Object> getDescriptors() { return descriptors; }

    public List<String> getImageUrls() { return image_urls; }

    public List<String> getTransactIds() { return transact_ids; }

    public HashMap<String, Object> getDescriptorMap() { return descriptorMap; }

    public String getTitle() { return (String) about.get("item_title"); }

    public String getDescription() { return (String) about.get("item_description"); }

    public String getAuthorEmail() { return (String) about.get("author_email"); }

    public String getPrice() { return (String) about.get("list_price"); }

    public String getDateCreated() { return (String) about.get("date_created"); }

    public String getGenre() { return (String) about.get("genre"); }

    public HashMap<String, Object> getSuper() {
        HashMap<String, Object> parent = this;
        return parent;
    }
}
