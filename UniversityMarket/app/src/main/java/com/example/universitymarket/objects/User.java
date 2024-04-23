package com.example.universitymarket.objects;

import androidx.annotation.Discouraged;
import java.util.HashMap;
import java.util.ArrayList;
import com.example.universitymarket.utilities.Data;

/**
 * <b>
 * ANY MODIFICATIONS WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * File based on 'user_skeleton_cached.json' located at <a href="file:///Users/johnnyboi/University-Market/UniversityMarket/app/src/main/res/raw/">main/res/raw/</a>
*/
public class User extends HashMap<String, Object> {
    private HashMap<String, Object> about = new HashMap<>();
    private String id = null;
    private HashMap<String, ArrayList> interactions = new HashMap<>();
    private String date_created = null;
    private ArrayList<HashMap> ratings = new ArrayList<>();
    private String last_name = null;
    private String description = null;
    private String first_name = null;
    private String email = null;
    private ArrayList<String> chat_ids = new ArrayList<>();
    private ArrayList<String> watch_ids = new ArrayList<>();
    private ArrayList<String> transact_ids = new ArrayList<>();
    private ArrayList<String> post_ids = new ArrayList<>();
    private HashMap<String, Object> ratingsMap = new HashMap<>();

    @Discouraged(message = "Unless you are initializing from another skeleton POJO, do not use this constructor")
    public User(HashMap<String, Object> rawdata) {
        ArrayList<String> rawKeys = new ArrayList<>(rawdata != null ? rawdata.keySet() : new ArrayList<>());
        super.put("about", rawKeys.contains("about") ? rawdata.get("about") : null);
        super.put("id", rawKeys.contains("id") ? rawdata.get("id") : null);
        super.put("interactions", rawKeys.contains("interactions") ? rawdata.get("interactions") : null);
        ArrayList<String> superKeys = new ArrayList<>(super.keySet());
        about = superKeys.contains("about") ? (HashMap<String, Object>) super.get("about") : new HashMap<>();
        id = superKeys.contains("id") ? (String) super.get("id") : null;
        interactions = superKeys.contains("interactions") ? (HashMap<String, ArrayList>) super.get("interactions") : new HashMap<>();
        ArrayList<String> aboutKeys = new ArrayList<>(about != null ? about.keySet() : new ArrayList<>());
        date_created = aboutKeys.contains("date_created") ? (String) about.get("date_created") : null;
        ratings = aboutKeys.contains("ratings") ? (ArrayList<HashMap>) about.get("ratings") : new ArrayList<>();
        last_name = aboutKeys.contains("last_name") ? (String) about.get("last_name") : null;
        description = aboutKeys.contains("description") ? (String) about.get("description") : null;
        first_name = aboutKeys.contains("first_name") ? (String) about.get("first_name") : null;
        email = aboutKeys.contains("email") ? (String) about.get("email") : null;
        ArrayList<String> interactionsKeys = new ArrayList<>(interactions != null ? interactions.keySet() : new ArrayList<>());
        chat_ids = interactionsKeys.contains("chat_ids") ? (ArrayList<String>) interactions.get("chat_ids") : new ArrayList<>();
        watch_ids = interactionsKeys.contains("watch_ids") ? (ArrayList<String>) interactions.get("watch_ids") : new ArrayList<>();
        transact_ids = interactionsKeys.contains("transact_ids") ? (ArrayList<String>) interactions.get("transact_ids") : new ArrayList<>();
        post_ids = interactionsKeys.contains("post_ids") ? (ArrayList<String>) interactions.get("post_ids") : new ArrayList<>();

        for(int i = 0; ratings != null && i < ratings.size(); i++) {
            Data.mergeHash(ratingsMap, (HashMap<String, Object>) ratings.get(i));
        }
        formatSuper();
    }

    public User(String date_created, ArrayList<HashMap> ratings, String last_name, String description, String first_name, String email, String id, ArrayList<String> chat_ids, ArrayList<String> watch_ids, ArrayList<String> transact_ids, ArrayList<String> post_ids) {
        setAbout(date_created, ratings, last_name, description, first_name, email);
        setInteractions(chat_ids, watch_ids, transact_ids, post_ids);
        this.id = id;
        formatSuper();
    }

    public User(String id) {
        this.id = id;
        formatSuper();
    }

    public HashMap<String, Object> getAbout() { return about; }

    public String getId() { return id; }

    public HashMap<String, ArrayList> getInteractions() { return interactions; }

    public String getDateCreated() { return date_created; }

    public ArrayList<HashMap> getRatings() { return ratings; }

    public String getLastName() { return last_name; }

    public String getDescription() { return description; }

    public String getFirstName() { return first_name; }

    public String getEmail() { return email; }

    public ArrayList<String> getChatIds() { return chat_ids; }

    public ArrayList<String> getWatchIds() { return watch_ids; }

    public ArrayList<String> getTransactIds() { return transact_ids; }

    public ArrayList<String> getPostIds() { return post_ids; }

    public HashMap<String, Object> getRatingsMap() { return ratingsMap; }

    public void setAbout(String date_created, ArrayList<HashMap> ratings, String last_name, String description, String first_name, String email) {
        this.date_created = date_created;
        this.ratings = ratings;
        this.last_name = last_name;
        this.description = description;
        this.first_name = first_name;
        this.email = email;
        formatSuper();
    }

    public void setId(String id) {
        this.id = id;
        formatSuper();
    }

    public void setInteractions(ArrayList<String> chat_ids, ArrayList<String> watch_ids, ArrayList<String> transact_ids, ArrayList<String> post_ids) {
        this.chat_ids = chat_ids;
        this.watch_ids = watch_ids;
        this.transact_ids = transact_ids;
        this.post_ids = post_ids;
        formatSuper();
    }

    public void setDateCreated(String date_created) {
        this.date_created = date_created;
        formatSuper();
    }

    public void setRatings(ArrayList<HashMap> ratings) {
        this.ratings = ratings;
        formatSuper();
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
        formatSuper();
    }

    public void setDescription(String description) {
        this.description = description;
        formatSuper();
    }

    public void setFirstName(String first_name) {
        this.first_name = first_name;
        formatSuper();
    }

    public void setEmail(String email) {
        this.email = email;
        formatSuper();
    }

    public void setChatIds(ArrayList<String> chat_ids) {
        this.chat_ids = chat_ids;
        formatSuper();
    }

    public void setWatchIds(ArrayList<String> watch_ids) {
        this.watch_ids = watch_ids;
        formatSuper();
    }

    public void setTransactIds(ArrayList<String> transact_ids) {
        this.transact_ids = transact_ids;
        formatSuper();
    }

    public void setPostIds(ArrayList<String> post_ids) {
        this.post_ids = post_ids;
        formatSuper();
    }

    public HashMap<String, Object> getSuper() { return this; }

    private void formatSuper() {
        interactions = new HashMap<>();
        interactions.put("chat_ids", chat_ids);
        interactions.put("watch_ids", watch_ids);
        interactions.put("transact_ids", transact_ids);
        interactions.put("post_ids", post_ids);
        about = new HashMap<>();
        about.put("date_created", date_created);
        about.put("ratings", ratings);
        about.put("last_name", last_name);
        about.put("description", description);
        about.put("first_name", first_name);
        about.put("email", email);
        super.clear();
        super.put("about", about);
        super.put("id", id);
        super.put("interactions", interactions);
    }
}