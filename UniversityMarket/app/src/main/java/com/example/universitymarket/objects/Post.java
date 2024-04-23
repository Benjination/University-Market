package com.example.universitymarket.objects;

import androidx.annotation.Discouraged;
import java.util.HashMap;
import java.util.ArrayList;
import com.example.universitymarket.utilities.Data;

/**
 * <b>
 * ANY MODIFICATIONS WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * File based on 'post_skeleton.json' located at <a href="file:///Users/johnnyboi/University-Market/UniversityMarket/app/src/main/res/raw/">main/res/raw/</a>
*/
public class Post extends HashMap<String, Object> {
    private ArrayList<HashMap> descriptors = new ArrayList<>();
    private HashMap<String, Object> about = new HashMap<>();
    private String id = null;
    private Integer quantity = null;
    private String date_created = null;
    private ArrayList<String> transact_ids = new ArrayList<>();
    private String genre = null;
    private String author_email = null;
    private ArrayList<String> image_urls = new ArrayList<>();
    private Float list_price = null;
    private ArrayList<String> image_contexts = new ArrayList<>();
    private String item_title = null;
    private String item_description = null;
    private HashMap<String, Object> descriptorsMap = new HashMap<>();

    @Discouraged(message = "Unless you are initializing from another skeleton POJO, do not use this constructor")
    public Post(HashMap<String, Object> rawdata) {
        ArrayList<String> rawKeys = new ArrayList<>(rawdata != null ? rawdata.keySet() : new ArrayList<>());
        super.put("descriptors", rawKeys.contains("descriptors") ? rawdata.get("descriptors") : null);
        super.put("about", rawKeys.contains("about") ? rawdata.get("about") : null);
        super.put("id", rawKeys.contains("id") ? rawdata.get("id") : null);
        ArrayList<String> superKeys = new ArrayList<>(super.keySet());
        descriptors = superKeys.contains("descriptors") ? (ArrayList<HashMap>) super.get("descriptors") : new ArrayList<>();
        about = superKeys.contains("about") ? (HashMap<String, Object>) super.get("about") : new HashMap<>();
        id = superKeys.contains("id") ? (String) super.get("id") : null;
        ArrayList<String> aboutKeys = new ArrayList<>(about != null ? about.keySet() : new ArrayList<>());
        quantity = aboutKeys.contains("quantity") ? ((Number) about.get("quantity")).intValue() : null;
        date_created = aboutKeys.contains("date_created") ? (String) about.get("date_created") : null;
        transact_ids = aboutKeys.contains("transact_ids") ? (ArrayList<String>) about.get("transact_ids") : new ArrayList<>();
        genre = aboutKeys.contains("genre") ? (String) about.get("genre") : null;
        author_email = aboutKeys.contains("author_email") ? (String) about.get("author_email") : null;
        image_urls = aboutKeys.contains("image_urls") ? (ArrayList<String>) about.get("image_urls") : new ArrayList<>();
        list_price = aboutKeys.contains("list_price") ? ((Number) about.get("list_price")).floatValue() : null;
        image_contexts = aboutKeys.contains("image_contexts") ? (ArrayList<String>) about.get("image_contexts") : new ArrayList<>();
        item_title = aboutKeys.contains("item_title") ? (String) about.get("item_title") : null;
        item_description = aboutKeys.contains("item_description") ? (String) about.get("item_description") : null;

        for(int i = 0; descriptors != null && i < descriptors.size(); i++) {
            Data.mergeHash(descriptorsMap, (HashMap<String, Object>) descriptors.get(i));
        }
        formatSuper();
    }

    public Post(ArrayList<HashMap> descriptors, Integer quantity, String date_created, ArrayList<String> transact_ids, String genre, String author_email, ArrayList<String> image_urls, Float list_price, ArrayList<String> image_contexts, String item_title, String item_description, String id) {
        setAbout(quantity, date_created, transact_ids, genre, author_email, image_urls, list_price, image_contexts, item_title, item_description);
        this.descriptors = descriptors;
        this.id = id;
        formatSuper();
    }

    public Post() {
        formatSuper();
    }

    public ArrayList<HashMap> getDescriptors() { return descriptors; }

    public HashMap<String, Object> getAbout() { return about; }

    public String getId() { return id; }

    public Integer getQuantity() { return quantity; }

    public String getDateCreated() { return date_created; }

    public ArrayList<String> getTransactIds() { return transact_ids; }

    public String getGenre() { return genre; }

    public String getAuthorEmail() { return author_email; }

    public ArrayList<String> getImageUrls() { return image_urls; }

    public Float getListPrice() { return list_price; }

    public ArrayList<String> getImageContexts() { return image_contexts; }

    public String getItemTitle() { return item_title; }

    public String getItemDescription() { return item_description; }

    public HashMap<String, Object> getDescriptorsMap() { return descriptorsMap; }

    public void setDescriptors(ArrayList<HashMap> descriptors) {
        this.descriptors = descriptors;
        formatSuper();
    }

    public void setAbout(Integer quantity, String date_created, ArrayList<String> transact_ids, String genre, String author_email, ArrayList<String> image_urls, Float list_price, ArrayList<String> image_contexts, String item_title, String item_description) {
        this.quantity = quantity;
        this.date_created = date_created;
        this.transact_ids = transact_ids;
        this.genre = genre;
        this.author_email = author_email;
        this.image_urls = image_urls;
        this.list_price = list_price;
        this.image_contexts = image_contexts;
        this.item_title = item_title;
        this.item_description = item_description;
        formatSuper();
    }

    public void setId(String id) {
        this.id = id;
        formatSuper();
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        formatSuper();
    }

    public void setDateCreated(String date_created) {
        this.date_created = date_created;
        formatSuper();
    }

    public void setTransactIds(ArrayList<String> transact_ids) {
        this.transact_ids = transact_ids;
        formatSuper();
    }

    public void setGenre(String genre) {
        this.genre = genre;
        formatSuper();
    }

    public void setAuthorEmail(String author_email) {
        this.author_email = author_email;
        formatSuper();
    }

    public void setImageUrls(ArrayList<String> image_urls) {
        this.image_urls = image_urls;
        formatSuper();
    }

    public void setListPrice(Float list_price) {
        this.list_price = list_price;
        formatSuper();
    }

    public void setImageContexts(ArrayList<String> image_contexts) {
        this.image_contexts = image_contexts;
        formatSuper();
    }

    public void setItemTitle(String item_title) {
        this.item_title = item_title;
        formatSuper();
    }

    public void setItemDescription(String item_description) {
        this.item_description = item_description;
        formatSuper();
    }

    public HashMap<String, Object> getSuper() { return this; }

    private void formatSuper() {
        about = new HashMap<>();
        about.put("quantity", quantity);
        about.put("date_created", date_created);
        about.put("transact_ids", transact_ids);
        about.put("genre", genre);
        about.put("author_email", author_email);
        about.put("image_urls", image_urls);
        about.put("list_price", list_price);
        about.put("image_contexts", image_contexts);
        about.put("item_title", item_title);
        about.put("item_description", item_description);
        super.clear();
        super.put("descriptors", descriptors);
        super.put("about", about);
        super.put("id", id);
    }
}