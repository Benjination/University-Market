package com.example.universitymarket.utilities;

import com.example.universitymarket.objects.User;
import com.example.universitymarket.globals.actives.ActiveUser;
/**
 * <b>
 * ANY MODIFICATIONS ABOVE WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * DO NOT REMOVE THIS BLOCK 
*/
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import org.checkerframework.checker.initialization.qual.Initialized;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Data {
    public static void mergeHash(Map<String, Object> from, Map<String, Object> to) {
        from.forEach((key, value) -> to.merge(key, value, (oldValue, newValue) ->
                !oldValue.equals(newValue) ? oldValue : newValue));
    }

    public static Task<Uri> refineImage(@NonNull Activity cur_act, @NonNull Uri uri) {
        TaskCompletionSource<Uri> result = new TaskCompletionSource<>();
        int side = convertDpToPixel(cur_act, 250);
        ImageView image = new ImageView(cur_act);
        Picasso
                .get()
                .load(uri)
                .resize(side, side)
                .centerCrop()
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bmp = Bitmap.createBitmap(((BitmapDrawable)image.getDrawable()).getBitmap());
                        File path = new File(cachePath(cur_act) + "/images");
                        path.mkdir();
                        File dir = new File(path, generateID("pic") + ".png");
                        try {
                            FileOutputStream out = new FileOutputStream(dir);
                            bmp.compress(Bitmap.CompressFormat.PNG, 50, out);
                            out.flush();
                            out.close();
                            result.setResult(Uri.fromFile(new File(dir.getAbsolutePath())));
                        } catch(IOException e) {
                            result.setException(e);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        result.setException(e);
                    }
                });

        return result.getTask();
    }

    public static boolean clearImageCache(@NonNull Activity cur_act) {
        boolean result = true;
        File[] images = new File(cachePath(cur_act) + "/images").listFiles();
        try {
            for(File f : Objects.requireNonNull(images)) {
                if(!f.delete())
                    result = false;
            }
        } catch(Exception ignored) {
            result = false;
        }

        return result;
    }

    public static int convertDpToPixel(@NonNull Activity cur_act, int dp){
        return dp * (int) cur_act.getResources().getDisplayMetrics().density;
    }
    public static int convertPixelsToDp(@NonNull Activity cur_act, int px){
        return px / (int) cur_act.getResources().getDisplayMetrics().density;
    }

    public static boolean isAnyObjectNull(Object... objects) {
        for(Object o : objects) {
            if(o == null)
                return true;
        }
        return false;
    }

    public static boolean isAnyStringEmpty(String... strings) {
        for(String s : strings) {
            if(s.length() == 0)
                return true;
        }
        return false;
    }

    private static boolean setCache(@NonNull Activity cur_act, String name, HashMap<String, Object> pojo, boolean clear) {
        return setCache(cur_act, name, pojoToJSON(pojo), clear);
    }

    private static boolean setCache(@NonNull Activity cur_act, String name, String json, boolean clear) {
        String path = cachePath(cur_act);

        if(name == null)
            return false;
        try {
            if(clear) {
                return getCachedFile(cur_act, name).delete();
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

    public static File getCachedFile(@NonNull Activity cur_act, @NonNull String name) {
        return new File(cachePath(cur_act) + "/" + name + ".json");
    }

    public static File[] getCachedFiles(@NonNull Activity cur_act) {
        return new File(cachePath(cur_act)).listFiles();
    }

    public static String getCachedToJSON(@NonNull Activity cur_act, @NonNull String name) {
        return fileToJSON(getCachedFile(cur_act, name));
    }

    public static HashMap<String, Object> getCachedToPOJO(@NonNull Activity cur_act, @NonNull String name) {
        return fileToPOJO(getCachedFile(cur_act, name));
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
        HashMap<String, Object> result = new HashMap<>();
        try {
            result = (HashMap<String, Object>) new ObjectMapper().readValue(json.toString(), HashMap.class);
        } catch (Exception e) {
            Log.e("jsonToPOJO", e.getMessage());
        }
        return result;
    }

    @NonNull
    public static String pojoToJSON(@NonNull HashMap<String, Object> pojo) {
        String result = "";
        try {
            result = new ObjectMapper().writeValueAsString(pojo);
        } catch(JsonProcessingException e) {
            Log.e("pojoToJSON", e.getMessage());
        }
        return result;
    }

    @NonNull
    public static String getDate() {
        return DateTimeFormatter.ofPattern("ddMMMyyyy").format(LocalDateTime.now());
    }

    @NonNull
    public static String generateID(@Nullable String prefix) {
        DateTimeFormatter idFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        String refine = "";
        if(prefix != null)
            refine = prefix.replaceAll("[^a-zA-Z]", "").toLowerCase();
        return refine + getDate() + "_" + idFormat.format(LocalDateTime.now());
    }

/**
 * <b>
 * ANY MODIFICATIONS BELOW WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * DO NOT REMOVE THIS BLOCK 
*/


    public static void setActiveUser(@NonNull Activity cur_act, User userOBJ) {
        ActiveUser.about = userOBJ.getAbout();
        ActiveUser.id = userOBJ.getId();
        ActiveUser.interactions = userOBJ.getInteractions();
        ActiveUser.date_created = userOBJ.getDateCreated();
        ActiveUser.last_name = userOBJ.getLastName();
        ActiveUser.first_name = userOBJ.getFirstName();
        ActiveUser.email = userOBJ.getEmail();
        ActiveUser.watch_ids = userOBJ.getWatchIds();
        ActiveUser.transact_ids = userOBJ.getTransactIds();
        ActiveUser.post_ids = userOBJ.getPostIds();

        Log.d("setActiveUser", "Success = " + setCache(cur_act, "ActiveUser", userOBJ, false));
    }

    public static User activeUserToPOJO() {
        return new User(ActiveUser.date_created, ActiveUser.last_name, ActiveUser.first_name, ActiveUser.email, ActiveUser.id, ActiveUser.watch_ids, ActiveUser.transact_ids, ActiveUser.post_ids );
    }
}