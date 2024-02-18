package com.example.universitymarket.objects;

import android.util.Log;
import androidx.annotation.Nullable;
import com.example.universitymarket.utilities.Network;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class User extends HashMap<String, Object> {
    @Nullable private String email = null;
    @Nullable private HashMap<String, Object> info = null;
    @Nullable private List<String> post_ids = null;
    @Nullable private List<String> shopping_cart = null;

    public void lateConstructor() {
        if(email == null) {
            email = (String) super.get("email");
            info = (HashMap<String, Object>) super.get("info");
            post_ids = (List<String>) super.get("post_ids");
            shopping_cart = (List<String>) super.get("shopping_cart");
        }
    }

    public List<String> getName() {
        List<String> name = new ArrayList<>();
        name.add((String) info.get("first_name"));
        name.add((String) info.get("middle_name"));
        name.add((String) info.get("last_name"));
        return name;
    }

    public long getEpochRegistered() {
        String raw = (String) info.get("date_registered");
        Date date;
        try {
            date = DateFormat.getDateInstance().parse(raw);
            date.getTime();
        } catch(Exception e) {
            Log.e("Date parsing error", e.getMessage());
            return -1;
        }
        return date.getTime();
    }

    public List<Post> getPosts() {
        List<Post> buffer = new ArrayList<>();
        for(String id : post_ids)
            buffer.add(Network.getPost(id));
        return buffer;
    }

    public String getEmail() { return email; }

    public String getUsername() {
        return (String) info.get("username");
    }

    public String getDomain() { return (String) info.get("domain"); }

    public int getTenantID() {
        return Integer.parseInt((String) info.get("tenant_id"));
    }

    public String getDateRegistered() { return (String) info.get("date_registered"); }

    public HashMap<String, Object> getInfo() { return info; }

    public List<String> getPostIds() { return post_ids; }

    public List<String> getShoppingCart() { return shopping_cart; }
}
