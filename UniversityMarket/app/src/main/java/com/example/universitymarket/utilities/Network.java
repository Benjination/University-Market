package com.example.universitymarket.utilities;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.ActiveUser;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.microsoft.identity.client.AcquireTokenSilentParameters;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SignInParameters;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Network {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public boolean signIn(Activity cur_act){
        ISingleAccountPublicClientApplication msalApp = getMSALObj(cur_act);
        final IAccount[] account = new IAccount[1];
        final String[] access_token = new String[1];

        //check for token
        try {
            final boolean[] needs_resync = new boolean[1];
            msalApp.acquireTokenSilent(
                    new AcquireTokenSilentParameters.Builder()
                            .withScopes(Arrays.asList(Policy.scopes))
                            .withCallback(new SilentAuthenticationCallback() {
                                @Override public void onSuccess(IAuthenticationResult res) {
                                    access_token[0] = res.getAccessToken();
                                    account[0] = res.getAccount();
                                }
                                @Override public void onError(MsalException exception) {
                                    //user is logged out
                                    needs_resync[0] = true;
                                }
                            })
                            .build()
            );

            msalApp.signIn(
                    SignInParameters.builder()
                            .withActivity(cur_act)
                            .withScopes(Arrays.asList(Policy.scopes))
                            .withCallback(new AuthenticationCallback() {
                                @Override public void onCancel() { Log.d("Login", "User cancelled"); }
                                @Override public void onSuccess(IAuthenticationResult res) {
                                    Log.d("Login", "Signed on");

                                    boolean dne = false; //check with firebase against email
                                    if(!dne) {
                                        if (needs_resync[0]) {
                                            clearCache(cur_act);
                                            syncCache(cur_act, "download");
                                            access_token[0] = res.getAccessToken();
                                            account[0] = res.getAccount();
                                        }
                                        //updateCache(cur_act, "access_token", access_token[0]);
                                        syncCache(cur_act, "upload");
                                    } else {
                                        /*TODO:  String[][] data = createAccount(); //graph API
                                        for (String[] dat : data) {
                                            updateCache(cur_act, dat[0], dat[1]);
                                        }*/
                                        syncCache(cur_act, "upload");
                                    }
                                }
                                @Override public void onError(MsalException e) {
                                    Log.e(e.getErrorCode(), e.getMessage());
                                }
                            })
                            .build()
            );
        } catch (Exception e) {
            Log.e("Azure connection error", e.getMessage());
        }
//add return false for bad case
        return true;
    }

    private ISingleAccountPublicClientApplication getMSALObj(Activity cur_act) {
        final ISingleAccountPublicClientApplication[] msalApp = new ISingleAccountPublicClientApplication[1];
        try {
            PublicClientApplication.createSingleAccountPublicClientApplication(
                    cur_act,
                    R.raw.auth_config,
                    new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                        @Override public void onError(MsalException e) { Log.e(e.getExceptionName(), e.getMessage()); }
                        @Override public void onCreated(ISingleAccountPublicClientApplication app) {
                            msalApp[0] = app;
                        }
                    }
            );
        } catch (Exception e) {
            Log.e(e.toString(), e.getMessage());
            return null;
        }
        return msalApp[0];
    }

    public void uploadCache(Activity cur_act) {
        db.collection("users").document(ActiveUser.email).set(Data.mapCache(cur_act))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("uploadCache", "Data uploaded successfully");
                    } else {
                        Log.e("uploadCache", "Error sending data: ", task.getException());
                    }
                });
    }

    public void downloadCache(Activity cur_act) {

    }



    private void syncCache(Activity cur_act, String direction) {
        if(direction.equals("upload")){

        } else if (direction.equals("download")) {

        }
    }

    private void clearCache(Activity cur_act) {

    }

    public static @Nullable Post getPost(String postID) {
        final Map<String, Object>[] rawdata = (Map<String, Object>[]) new HashMap<?,?>[1];

        List<String> genres = new ArrayList<>();
        List<HashMap<String, Object>> descriptors = new ArrayList<>();

        db.collection("posts").document(postID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        rawdata[0] = (Map<String, Object>) task.getResult().getData();
                    } else {
                        Log.e("getPost", "Error getting data: ", task.getException());
                    }
                })
        ;
        timeOut(rawdata);
        if(rawdata[0].isEmpty())
            return null;

        List<?> indefinite_genre = (ArrayList<?>) Arrays.asList(((HashMap<String, ?>) rawdata[0].get("genres")).keySet().toArray());
        List<?> indefinite_desc = (ArrayList<?>) Arrays.asList(((HashMap<String, ?>) rawdata[0].get("descriptors")).keySet().toArray());
        for(int i=0; i<Policy.max_genres_per_item && i<indefinite_genre.size() && i<indefinite_desc.size(); i++) {
            genres.add((String) indefinite_genre.get(i));
            descriptors.add((HashMap<String, Object>) indefinite_desc.get(i));
        }

        Post data = new Post();
        data.put("id", (String) rawdata[0].get("item_id"));
        data.put("about", (HashMap<String, Object>) rawdata[0].get("about"));
        data.put("genres", genres);
        data.put("descriptors", descriptors);
        data.lateConstructor();

        return data;
    }

    public static @Nullable User getUser(String email) {
        final Map<String, Object>[] rawdata = (Map<String, Object>[]) new HashMap<?,?>[1];

        List<String> post_ids = new ArrayList<>();
        List<String> shopping_cart = new ArrayList<>();

        db.collection("users").document(email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        rawdata[0] = (Map<String, Object>) task.getResult().getData();
                    } else {
                        Log.e("getUser", "Error getting data: ", task.getException());
                    }
                })
        ;
        timeOut(rawdata);
        if(rawdata[0].isEmpty())
            return null;

        List<?> indefinite_post = (ArrayList<?>) Arrays.asList(((HashMap<String, ?>) rawdata[0].get("post_ids")).keySet().toArray());
        for(int i=0; i<Policy.max_posts_per_user && i<indefinite_post.size(); i++) {
            post_ids.add((String) indefinite_post.get(i));
        }

        List<?> indefinite_cart = (ArrayList<?>) Arrays.asList(((HashMap<String, ?>) rawdata[0].get("shopping_cart")).keySet().toArray());
        for(Object o : indefinite_cart) {
            shopping_cart.add((String) o);
        }

        User data = new User();
        data.put("email", (String) rawdata[0].get("email"));
        data.put("info", (HashMap<String, Object>) rawdata[0].get("info"));
        data.put("post_ids", post_ids);
        data.put("shopping_cart", shopping_cart);
        data.lateConstructor();

        return data;
    }

    public static void timeOut(@Nullable Object[] mutex) {
        long startTime = System.currentTimeMillis();
        while(mutex[0] == null && startTime <= startTime + 1000 * Policy.max_seconds_before_timeout);
        if(mutex[0] == null) {
            Log.e("Network", "Connection timeout");
        }
    }
}
