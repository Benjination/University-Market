package com.example.universitymarket.utilities;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.universitymarket.globals.ActiveUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class Data {
    public static void mergeHash(Map<String, Object> from, Map<String, Object> to) {
        from.forEach((key, value) -> to.merge(key, value, (oldValue, newValue) ->
                !oldValue.equals(newValue) ? oldValue : newValue));
    }

    public static void setActiveUser(Map<String, Object> pojo, String json) {
        if(json != null) {
            pojo = jsonToPOJO(json);
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

    private static String getID(@NonNull Object identifier) {
        if(identifier instanceof String && ((String) identifier).contains(":\"")) {
            try {
                String[] split = ((String) identifier).split("(,\")[^\"]*(\":\")");
                return alphaNumerify(split[split.length - 1].split("\"")[0]);
            } catch(ArrayIndexOutOfBoundsException e) {
                Log.e("getID", e.getMessage());
            }
        } else if(identifier instanceof String) {
            return (String) identifier;
        } else if(identifier instanceof HashMap) {
            try {
                return (String) getID(pojoToJSON((HashMap<String, Object>) identifier));
            } catch(NullPointerException e) {
                Log.e("getID", e.getMessage());
            }
        }
        return null;
    }

    public static String alphaNumerify(@Initialized String input) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < input.length(); i++) {
            if(!(input.charAt(i) + "").matches("[a-zA-Z0-9]")) {
                result.append("_");
            } else {
                result.append(input.charAt(i));
            }
        }
        return result.toString();
    }

    public static boolean setCache(@NonNull Activity cur_act, HashMap<String, Object> pojo, boolean clear) {
        return setCache(cur_act, pojoToJSON(pojo), clear);
    }

    public static boolean setCache(@NonNull Activity cur_act, String json, boolean clear) {
        String path = cachePath(cur_act), name = getID(json);

        if(name == null)
            return false;
        try {
            if(clear) {
                File f = getCachedFile(cur_act, json);
                if(f != null) {
                    return f.delete();
                }
            } else {
                FileWriter out = new FileWriter(path + "/" + name + ".json");
                out.write(json);
                out.close();
            }
        } catch(IOException e) {
            Log.e("setCache", e.getMessage());
            return false;
        }
        return true;
    }

    public static boolean clearAllCache(@NonNull Activity cur_act) {
        boolean result = true;
        File[] files = getCachedFiles(cur_act);
        if(files == null)
            return false;

        for(File file : files) {
            result = file.delete();
        }
        return result;
    }

    public static boolean clearSubCache(@NonNull Activity cur_act, String prefix) {
        boolean result = true;
        File[] files = (File[]) Stream.of(new File(cachePath(cur_act)).listFiles()).filter(e -> e.getName().startsWith(prefix)).toArray();
        for(File file : files) {
            result = file.delete();
        }
        return result;
    }

    public static File getCachedFile(@NonNull Activity cur_act, @NonNull Object identifier) {
        String ID = getID(identifier);
        if(ID == null)
            return null;
        return new File(cachePath(cur_act) + "/" + ID + ".json");
    }

    public static File[] getCachedFiles(@NonNull Activity cur_act) {
        return new File(cachePath(cur_act)).listFiles();
    }

    @NonNull
    public static File[] getCachedFiles(@NonNull Activity cur_act, String prefix) {
        return (File[]) Stream.of(new File(cachePath(cur_act)).listFiles()).filter(e -> e.getName().startsWith(prefix)).toArray();
    }

    public static String getCachedToJSON(@NonNull Activity cur_act, @NonNull Object identifier) {
        File file = getCachedFile(cur_act, identifier);
        if(file == null)
            return null;
        return fileToJSON(file);
    }

    public static HashMap<String, Object> getCachedToPOJO(@NonNull Activity cur_act, @NonNull Object identifier) {
        File file = getCachedFile(cur_act, identifier);
        if(file == null)
            return null;
        return fileToPOJO(file);
    }

    @NonNull
    public static String cachePath(Activity cur_act) {
        File cache_dir = new File(cur_act.getCacheDir().getAbsolutePath() + "/um_cache");
        if(cache_dir.mkdir())
            Log.d("cachePath", "um_cache directory created");
        return cache_dir.getAbsolutePath();
    }

    @NonNull
    public static String readData(@Initialized InputStream inp) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inp, StandardCharsets.UTF_8));
        StringBuilder data = new StringBuilder();

        String line = reader.readLine();

        while(line != null) {
            data.append(line).append("\n");
            line = reader.readLine();
        }

        inp.close();
        return data.toString();
    }

    @NonNull
    public static String resToJSON(@NonNull Activity cur_act, @NonNull int resourceID) {
        String result = "";
        try {
            InputStream inp = cur_act.getResources().openRawResource(resourceID);
            result = readData(inp);
        } catch (IOException e) {
            Log.e("resToJSON", e.getMessage());
        }
        return result;
    }

    @NonNull
    public static String fileToJSON(@NonNull File file) {
        String result = "";
        try {
            InputStream inp = new FileInputStream(file);
            result =  readData(inp);
        } catch (IOException e) {
            Log.e("fileToJSON", e.getMessage());
        }
        return result;
    }

    @NonNull
    public static HashMap<String, Object> resToPOJO(@NonNull Activity cur_act, int resourceID) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            InputStream inp = cur_act.getResources().openRawResource(resourceID);
            result = jsonToPOJO(readData(inp));
        } catch (IOException e) {
            Log.e("resToPOJO", e.getMessage());
        }
        return result;
    }

    @NonNull
    public static HashMap<String, Object> fileToPOJO(@NonNull File file) {
        HashMap<String, Object> result = new HashMap<>();
        try {
            InputStream inp = new FileInputStream(file);
            result = jsonToPOJO(readData(inp));
        } catch (IOException e) {
            Log.e("fileToPOJO", e.getMessage());
        }
        return result;
    }

    @NonNull
    public static HashMap<String, Object> jsonToPOJO(@NonNull String json) {
        return (HashMap<String, Object>) new GsonBuilder().create().fromJson(json, HashMap.class);
    }

    @NonNull
    public static String pojoToJSON(@NonNull HashMap<String, Object> pojo) {
        return new Gson().toJson(pojo);
    }
}
