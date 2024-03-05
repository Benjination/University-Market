package com.example.universitymarket.globals;

import android.app.Application;
import android.content.Context;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public class ActiveUser extends Application {
    public static String email = "unknown";
    public static String id = "unknown";
    public static String access_token = "unknown";
    public static Map<String, Object> about = new HashMap<>();
    public static Map<String, Object> interactions = new HashMap<>();
    public static List<String> post_ids = new ArrayList<>();
    public static List<String> watch_ids = new ArrayList<>();
    public static List<String> transact_ids = new ArrayList<>();
    public static String username = "unknown";
    public static String first_name = "unknown";
    public static String middle_name = "unknown";
    public static String last_name = "unknown";
    public static String domain = "unknown";
    public static String tenant_id = "unknown";
    public static String date_created = "unknown";

    @Override
    public void onCreate() {
        super.onCreate();
    }


    /*
    public static List<Task<Post>> getPosts(Activity cur_act){
        return Network.getPosts(cur_act, (String[]) post_ids.toArray());
    }

    public static List<Task<Post>> getWatches(Activity cur_act) {
        return Network.getPosts(cur_act, (String[]) watch_ids.toArray());
    }*/


}
