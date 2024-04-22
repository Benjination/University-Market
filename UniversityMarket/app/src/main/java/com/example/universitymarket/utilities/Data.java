package com.example.universitymarket.utilities;

import com.example.universitymarket.models.User;
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
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

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
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Data {
    public static void mergeHash(Map<String, Object> from, Map<String, Object> to) {
        from.forEach((key, value) -> to.merge(key, value, (oldValue, newValue) ->
                !oldValue.equals(newValue) ? oldValue : newValue));
    }

    public static void updateAdapter(List<? extends HashMap<String, Object>> oldModelList, List<? extends HashMap<String, Object>> newModelList, RecyclerView.Adapter<? extends RecyclerView.ViewHolder> adapter) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldModelList.size();
            }
            @Override
            public int getNewListSize() {
                return newModelList.size();
            }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                String idOld = oldModelList.get(oldItemPosition).containsKey("id") ? String.valueOf(oldModelList.get(oldItemPosition).get("id")) : "null";
                String idNew = newModelList.get(newItemPosition).containsKey("id") ? String.valueOf(newModelList.get(newItemPosition).get("id")) : "null";

                return !(idOld.equals("null") || idNew.equals("null")) && idOld.equals(idNew);
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldModelList.get(oldItemPosition).hashCode() == newModelList.get(newItemPosition).hashCode();
            }
        });

        result.dispatchUpdatesTo(adapter);
    }

    public static List<Map.Entry<String, Object>> differingValuePairs(HashMap<String, Object> firstPOJO, HashMap<String, Object> secondPOJO) {
        Stream<Map.Entry<String, Object>> differingStream = Stream.concat(
                firstPOJO.entrySet().stream().filter(entry -> !Objects.equals(entry.getValue(), secondPOJO.get(entry.getKey())))
                        .flatMap(entry -> {
                            String key = entry.getKey();
                            Object firstVal = entry.getValue(), secondVal = secondPOJO.get(key);
                            if(firstVal instanceof Map && secondVal instanceof Map) {
                                return differingValuePairs((HashMap<String, Object>) firstVal, (HashMap<String, Object>) secondVal).stream();
                            } else {
                                return Stream.of(new AbstractMap.SimpleEntry<>(key, firstVal));
                            }
                        }),
                secondPOJO.entrySet().stream().filter(entry -> !firstPOJO.containsKey(entry.getKey()))
                        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()))
        );

        return differingStream.collect(Collectors.toList());
    }

    public static void callPicasso(@NonNull Activity cur_act, @NonNull List<Uri> uris, @NonNull List<Uri> processed, @NonNull List<Exception> exceptions, int depth, @NonNull TaskCompletionSource<List<Uri>> task) {
        int side = convertDpToPixel(cur_act, 250);
        ImageView image = new ImageView(cur_act);

        Picasso
                .get()
                .load(uris.get(depth - 1))
                .resize(side, side)
                .centerCrop()
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bmp = Bitmap.createBitmap(((BitmapDrawable) image.getDrawable()).getBitmap());
                        File path = new File(cachePath(cur_act) + "/images");
                        path.mkdir();
                        File dir = new File(path, generateID("pic") + ".png");
                        try {
                            FileOutputStream out = new FileOutputStream(dir);
                            bmp.compress(Bitmap.CompressFormat.PNG, 50, out);
                            out.flush();
                            out.close();
                            processed.add(Uri.fromFile(new File(dir.getAbsolutePath())));
                        } catch (IOException e) {
                            exceptions.add(e);
                        }

                        if(depth > 1)
                            callPicasso(cur_act, uris, processed, exceptions, depth - 1, task);
                        else {
                            if(exceptions.size() > 0) {
                                String combined = exceptions.stream().map(Throwable::toString).collect(Collectors.joining(","));
                                task.setException(new Exception(combined));
                            } else {
                                task.setResult(processed);
                            }
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        exceptions.add(e);
                        if(depth > 1)
                            callPicasso(cur_act, uris, processed, exceptions, depth - 1, task);
                        else {
                            String combined = exceptions.stream().map(Throwable::toString).collect(Collectors.joining(","));
                            task.setException(new Exception(combined));
                        }
                    }
                });
    }

    public static Task<List<Uri>> refineImages(@NonNull Activity cur_act, @NonNull List<Uri> uris) {
        TaskCompletionSource<List<Uri>> task = new TaskCompletionSource<>();
        callPicasso(cur_act, uris, new ArrayList<>(), new ArrayList<>(), uris.size(), task);
        return task.getTask();
    }

    public static Task<Uri> refineImage(@NonNull Activity cur_act, @NonNull Uri uri) {
        TaskCompletionSource<Uri> task = new TaskCompletionSource<>();
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
                        Bitmap bmp = Bitmap.createBitmap(((BitmapDrawable) image.getDrawable()).getBitmap());
                        File path = new File(cachePath(cur_act) + "/images");
                        path.mkdir();
                        File dir = new File(path, generateID("pic") + ".png");
                        try {
                            FileOutputStream out = new FileOutputStream(dir);
                            bmp.compress(Bitmap.CompressFormat.PNG, 50, out);
                            out.flush();
                            out.close();
                            task.setResult(Uri.fromFile(new File(dir.getAbsolutePath())));
                        } catch (IOException e) {
                            task.setException(e);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        task.setException(e);
                    }
                });

        return task.getTask();
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

    public static int convertDpToPixel(@NonNull Activity cur_act, int dp) {
        return dp * (int) cur_act.getResources().getDisplayMetrics().density;
    }

    public static int convertPixelsToDp(@NonNull Activity cur_act, int px) {
        return px / (int) cur_act.getResources().getDisplayMetrics().density;
    }

    public static int convertComplexToPixel(@NonNull Activity cur_act, int complex) {
        return TypedValue.complexToDimensionPixelSize(complex, cur_act.getResources().getDisplayMetrics());
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
        Log.e("checking", "cache");
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
            result = (HashMap<String, Object>) new ObjectMapper().readValue(json, HashMap.class);
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

    public static TemporalAccessor parseDate(String date) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss zzz yyyy");

        if(dtf != null) {
            try {
                return dtf.parse(date);
            } catch(Exception e) {
                Log.e("parseDate", e.getMessage());
            }
        }
        return null;
    }

    public static String formatDate(@NonNull TemporalAccessor parsed, @NonNull String pattern) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);

        if(dtf != null) {
            return dtf.format(parsed);
        }
        return null;
    }

    @NonNull
    public static String getDate() {
        return DateTimeFormatter.ofPattern("ddMMMyyyy").format(LocalDateTime.now());
    }

    @NonNull
    public static String generateID(@Nullable String prefix) {
        DateTimeFormatter idFormat = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
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


    public static void setActiveUser(  Activity cur_act, User userOBJ) {
        ActiveUser.about = userOBJ.getAbout();
        ActiveUser.id = userOBJ.getId();
        ActiveUser.interactions = userOBJ.getInteractions();
        ActiveUser.date_created = userOBJ.getDateCreated();
        ActiveUser.ratings = userOBJ.getRatings();
        ActiveUser.last_name = userOBJ.getLastName();
        ActiveUser.description = userOBJ.getDescription();
        ActiveUser.first_name = userOBJ.getFirstName();
        ActiveUser.email = userOBJ.getEmail();
        ActiveUser.chat_ids = userOBJ.getChatIds();
        ActiveUser.watch_ids = userOBJ.getWatchIds();
        ActiveUser.transact_ids = userOBJ.getTransactIds();
        ActiveUser.post_ids = userOBJ.getPostIds();

        Log.d("setActiveUser", "Success = " + setCache(cur_act, "ActiveUser", userOBJ, false));
    }

    public static User activeUserToPOJO() {
        return new User(ActiveUser.date_created, ActiveUser.ratings, ActiveUser.last_name, ActiveUser.description, ActiveUser.first_name, ActiveUser.email, ActiveUser.id, ActiveUser.chat_ids, ActiveUser.watch_ids, ActiveUser.transact_ids, ActiveUser.post_ids );
    }
}