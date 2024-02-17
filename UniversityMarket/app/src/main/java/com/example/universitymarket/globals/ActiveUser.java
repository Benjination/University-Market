package com.example.universitymarket.globals;

import android.app.Application;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.utilities.Network;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveUser extends Application {
    public static String email = "unknown";
    public static String access_token = "unknown";
    public static Map<String, Object> info = new HashMap<>();
    public static List<String> post_ids = new ArrayList<>();
    public static List<String> shopping_cart = new ArrayList<>();
    public static String username = "unknown";
    public static String domain = "unknown";

    @Override public void onCreate() {
        super.onCreate();
        initInfo();
    }

    private void initInfo(){
        info.put("username","unknown");
        info.put("first_name", "unknown");
        info.put("middle_name", "unknown");
        info.put("last_name", "unknown");
        info.put("domain", "unknown");
        info.put("tenant_id", "unknown");
        info.put("date_registered", "unknown");
    }

    public static List<Post> getPosts(){
        List<Post> buffer = new ArrayList<>();
        for(String id : post_ids)
            buffer.add(Network.getPost(id));
        return buffer;
    }

    public static List<Post> getCart() {
        List<Post> buffer = new ArrayList<>();
        for(String id : shopping_cart)
            buffer.add(Network.getPost(id));
        return buffer;
    }
}
