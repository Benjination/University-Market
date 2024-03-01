package com.example.universitymarket.utilities;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.NonNull;
import com.example.universitymarket.globals.ActiveUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Data {
    public static void mergeHash(Map<String, Object> from, Map<String, Object> to) {
        from.forEach((key, value) -> to.merge(key, value, (oldValue, newValue) ->
                !oldValue.equals(newValue) ? oldValue : newValue));
    }

    public static void setActiveUser(Map<String, Object> pojo, String json) {
        if(json != null) {
            pojo = jsonToPOJO(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        }
        ActiveUser.email = (String) pojo.get("email");
        ActiveUser.id = (String) pojo.get("email");
        ActiveUser.about = (HashMap<String, Object>) pojo.get("about");
        ActiveUser.interactions = (HashMap<String, Object>) pojo.get("interactions");
        ActiveUser.post_ids = (List<String>) ActiveUser.interactions.get("post_ids");
        ActiveUser.watch_ids = (List<String>) ActiveUser.interactions.get("watch_ids");
        ActiveUser.transact_ids = (List<String>) ActiveUser.interactions.get("transact_ids");
        ActiveUser.username = (String) ActiveUser.about.get("username");
        ActiveUser.first_name = (String) ActiveUser.about.get("first_name");
        ActiveUser.middle_name = (String) ActiveUser.about.get("middle_name");
        ActiveUser.last_name = (String) ActiveUser.about.get("last_name");
        ActiveUser.domain = (String) ActiveUser.about.get("domain");
        ActiveUser.tenant_id = (String) ActiveUser.about.get("tenant_id");
        ActiveUser.date_created = (String) ActiveUser.about.get("date_created");
    }

    public static void setCache(@NonNull Activity cur_act, List<Pair<String, HashMap<String, Object>>> pojoPAIR) {
        File cd = new File(cur_act.getCacheDir().getAbsolutePath() + "/um_cache");
        String path = cd.getAbsolutePath();
        if(!cd.mkdir()) {
            Log.d("updateCache", "um_cache directory already exists");
        }

        try {
            for (Pair<String, HashMap<String, Object>> p : pojoPAIR) {
                String json = pojoToJSON(p.second);
                FileWriter out = new FileWriter(path + "/" + p.first + ".json");
                out.write(json);
                out.close();
            }
        } catch(IOException e) {
            Log.e("updateCache", e.getMessage());
        }
    }

    @NonNull
    public static HashMap<String, Object> getCache(@NonNull Activity cur_act, @NonNull String item_name) {
        File file = new File(cur_act.getCacheDir().getAbsolutePath() + "/um_cache/" + item_name + ".json");
        if(!file.exists()) {
            return new HashMap<>();
        }

        try {
            InputStream inp = new FileInputStream(file);
            return jsonToPOJO(inp);
        } catch(FileNotFoundException e) {
            Log.e("getCache", e.getMessage());
            return new HashMap<>();
        }
    }

    @NonNull
    public static HashMap<String, Object> jsonToPOJO(@NonNull InputStream inp) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inp, StandardCharsets.UTF_8));
            StringBuilder json = new StringBuilder();
            String line = reader.readLine();

            while(line != null && !line.contains("access_token")) {
                json.append(line);
                line = reader.readLine();
            }

            inp.close();
            HashMap<String, Object> result = (HashMap<String, Object>) new ObjectMapper().readValue(json.toString(), HashMap.class);
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            Log.e("JSON read error", e.getMessage());
        }
        return new HashMap<>();
    }

    @NonNull
    public static String pojoToJSON(@NonNull HashMap<String, Object> pojo) {
        try {
            return new ObjectMapper().writeValueAsString(pojo);
        } catch(JsonProcessingException e) {
            Log.e("POJO parsing error", e.getMessage());
        }
        return "";
    }
}
