package com.example.universitymarket.utilities;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.NonNull;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.ActiveUser;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.Test;
import com.example.universitymarket.objects.Transaction;
import com.example.universitymarket.objects.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import com.microsoft.identity.common.java.exception.ArgumentException;
import org.json.JSONException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public abstract class Network extends AsyncTask {
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
                                            //clearCache(cur_act);
                                            //syncCache(cur_act, "download");
                                            access_token[0] = res.getAccessToken();
                                            account[0] = res.getAccount();
                                        }
                                        //updateCache(cur_act, "access_token", access_token[0]);
                                        //syncCache(cur_act, "upload");
                                    } else {
                                        /*TODO:  String[][] data = createAccount(); //graph API
                                        for (String[] dat : data) {
                                            updateCache(cur_act, dat[0], dat[1]);
                                        }*/
                                        //syncCache(cur_act, "upload");
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

    @NonNull
    @SafeVarargs
    private static Task<HashMap<String, Object>> setDoc(@NonNull Activity cur_act, @NonNull String collID, @NonNull String docID, boolean clear, HashMap<String, Object>... obj) {
        final TaskCompletionSource<HashMap<String, Object>> source = new TaskCompletionSource<>();
        HashMap<String, Object> pojoObj = new HashMap<>();
        HashMap<String, Object> pojo;
        String illCollID = "Must be 'users', 'posts', 'transactions', or 'test'";
        String illNumObj = "Must contain either zero or one argument";
        String illReqObj = "Must contain an argument for 'posts' or 'transactions'";
        String illNullData = "POJO data could not be found";

        if(obj.length > 1) {
            if(!source.getTask().isComplete())
                source.setException(new ArgumentException("setDoc", "obj", illNumObj));
            return source.getTask();
        } else if(obj.length == 1) {
            HashMap<String, Object> buffer = obj[0];
            Data.mergeHash(pojoObj, buffer);
        }

        String filename = "test_cache.json";
        DocumentReference reference = db.collection(collID).document(docID);

        switch(collID) {
            case "users":
                filename = "user_cache.json";
            case "test":
                try {
                    InputStream inp = new FileInputStream(
                            cur_act.getCacheDir().getAbsolutePath() + "/um_cache/" + filename
                    );
                    pojo = Data.jsonToPOJO(inp);
                } catch(FileNotFoundException e) {
                    source.setException(e);
                    return source.getTask();
                }
                break;
            case "transactions":
            case "posts":
                if(obj.length == 0 && !clear) {
                    if (!source.getTask().isComplete())
                        source.setException(new ArgumentException("setDoc", "obj", illReqObj));
                    return source.getTask();
                } else {
                    pojo = pojoObj;
                }
                break;
            default:
                if(!source.getTask().isComplete())
                    source.setException(new ArgumentException("setDoc", "collID", illCollID));
                return source.getTask();
        }

        if(clear || pojo == null) {
            if (!source.getTask().isComplete()) {
                if (clear) {
                    reference.delete();
                    source.setResult(new HashMap<>());
                } else {
                    source.setException(new NullPointerException(illNullData));
                }
            }
            return source.getTask();
        }

        reference.set(pojo)
                .addOnSuccessListener(task -> {
                    if(!source.getTask().isComplete())
                        source.setResult(pojo);
                })
                .addOnFailureListener(e -> {
                    if(!source.getTask().isComplete())
                        source.setException(e);
                });
        return source.getTask();
    }

    @NonNull
    private static Task<HashMap<String, Object>> getDoc(@NonNull Activity cur_act, @NonNull String collID, @NonNull String docID) {
        final TaskCompletionSource<HashMap<String, Object>> source = new TaskCompletionSource<>();
        String illCollID = "Must be 'users', 'posts', 'transactions', or 'test'";
        String illNullData = "Collection '" + collID + "' or document '" + docID + "' does not exist";
        String illFormat = "'" + docID + "' from '" + collID + "' is not in skeleton format!";
        new Handler().postDelayed(() -> {
            if (!source.getTask().isComplete())
                source.setException(new TimeoutException());
        }, Policy.max_seconds_before_timeout * 1000);

        int resID;
        HashMap<String, Object> pojo;
        Task<DocumentSnapshot> reference = db.collection(collID).document(docID).get();

        switch(collID) {
            case "users":
                resID = R.raw.user_skeleton;
                break;
            case "posts":
                resID = R.raw.post_skeleton;
                break;
            case "transactions":
                resID = R.raw.transaction_skeleton;
                break;
            case "test":
                resID = R.raw.test_skeleton;
                break;
            default:
                if(!source.getTask().isComplete())
                    source.setException(new ArgumentException("getDoc", "collID", illCollID));
                return source.getTask();
        }

        InputStream inp = cur_act.getResources().openRawResource(resID);
        pojo = Data.jsonToPOJO(inp);

        reference
                .addOnSuccessListener(task -> {
                    if(!source.getTask().isComplete() && !reference.getResult().exists()) {
                        source.setException(new NullPointerException(illNullData));
                        return;
                    }

                    Map<String, Object> rawdata = task.getData();
                    List<Object> members = new ArrayList<>();
                    for(String s : pojo.keySet()) {
                        if(s.equals("access_token"))
                            continue;
                        members.add(rawdata.get(s));
                    }

                    if(!source.getTask().isComplete() && isAnyObjectNull(members)) {
                        source.setException(new JSONException(illFormat));
                        return;
                    }

                    Data.mergeHash(pojo, rawdata);
                    source.setResult((HashMap<String, Object>) rawdata);
                })
                .addOnFailureListener(e -> {
                    if(!source.getTask().isComplete())
                        source.setException(e);
                });

        return source.getTask();
    }

    @NonNull
    public static Task<HashMap<String, Object>> setTest(@NonNull Activity cur_act, @NonNull String docID, boolean clear) {
        return setDoc(cur_act, "test", docID, clear);
    }

    @NonNull
    public static Task<Test> getTest(@NonNull Activity cur_act, @NonNull String docID) {
        TaskCompletionSource<Test> source = new TaskCompletionSource<>();
        getDoc(cur_act, "test", docID)
                .addOnSuccessListener(task -> {
                    Test result = new Test(task);
                    source.setResult(result);
                })
                .addOnFailureListener(source::setException);
        return source.getTask();
    }

    @NonNull
    public static Task<HashMap<String, Object>> uploadActiveUser(@NonNull Activity cur_act, boolean clear) {
        if(!ActiveUser.email.equals("unknown"))
            return setDoc(cur_act, "users", ActiveUser.email, clear);
        return setDoc(cur_act, "unknown", "", false);
    }

    @NonNull
    public static Task<User> downloadActiveUser(@NonNull Activity cur_act) {
        TaskCompletionSource<User> source = new TaskCompletionSource<>();
        getDoc(cur_act, "users", ActiveUser.email)
                .addOnSuccessListener(task -> {
                    User result = new User(task);
                    source.setResult(result);

                    Pair<String, HashMap<String, Object>> pair = new Pair<>(
                            "user_cache.json",
                            result.getSuper()
                    );
                    List<Pair<String, HashMap<String, Object>>> list = new ArrayList<>();
                    list.add(pair);
                    Data.setCache(cur_act, list);
                })
                .addOnFailureListener(source::setException);
        return source.getTask();
    }

    @NonNull
    public static Task<User> getOtherUser(@NonNull Activity cur_act, @NonNull String docID) {
        TaskCompletionSource<User> source = new TaskCompletionSource<>();
        getDoc(cur_act, "users", docID)
                .addOnSuccessListener(task -> {
                    User result = new User(task);
                    source.setResult(result);
                })
                .addOnFailureListener(source::setException);
        return source.getTask();
    }

    @NonNull
    public static List<Task<User>> getOtherUsers(@NonNull Activity cur_act, @NonNull String[] docID) {
        List<Task<User>> responses = new ArrayList<>();
        for(String s : docID) {
            TaskCompletionSource<User> source = new TaskCompletionSource<>();
            getDoc(cur_act, "users", s)
                    .addOnSuccessListener(task -> {
                        User result = new User(task);
                        source.setResult(result);
                    })
                    .addOnFailureListener(source::setException);
            responses.add(source.getTask());
        }
        return responses;
    }

    @NonNull
    public static Task<HashMap<String, Object>> setPost(@NonNull Activity cur_act, boolean clear, @NonNull Post postOBJ) {
        if(postOBJ.getId() != null)
            return setDoc(cur_act, "posts", postOBJ.getId(), clear, postOBJ);
        return setDoc(cur_act, "unknown", "", false);
    }

    @NonNull
    public static List<Task<HashMap<String, Object>>> setPosts(@NonNull Activity cur_act, boolean clear, @NonNull Post[] postOBJ) {
        List<Task<HashMap<String, Object>>> responses = new ArrayList<>();
        for(Post post : postOBJ) {
            if(post.getId() != null)
                responses.add(setDoc(cur_act, "posts", post.getId(), clear, post));
        }
        return responses;
    }

    @NonNull
    public static Task<Post> getPost(@NonNull Activity cur_act, @NonNull String docID) {
        TaskCompletionSource<Post> source = new TaskCompletionSource<>();
        getDoc(cur_act, "posts", docID)
                .addOnSuccessListener(task -> {
                    Post result = new Post(task);
                    source.setResult(result);
                })
                .addOnFailureListener(source::setException);
        return source.getTask();
    }

    @NonNull
    public static List<Task<Post>> getPosts(@NonNull Activity cur_act, @NonNull String[] docID) {
        List<Task<Post>> responses = new ArrayList<>();
        for(String s : docID) {
            TaskCompletionSource<Post> source = new TaskCompletionSource<>();
            getDoc(cur_act, "posts", s)
                    .addOnSuccessListener(task -> {
                        Post result = new Post(task);
                        source.setResult(result);
                    })
                    .addOnFailureListener(source::setException);
            responses.add(source.getTask());
        }
        return responses;
    }

    @NonNull
    public static Task<HashMap<String, Object>> setTransaction(@NonNull Activity cur_act, boolean clear, @NonNull Transaction tsctOBJ) {
        if(tsctOBJ.getId() != null)
            return setDoc(cur_act, "transactions", tsctOBJ.getId(), clear, tsctOBJ);
        return setDoc(cur_act, "unknown", "", false);
    }

    @NonNull
    public static List<Task<HashMap<String, Object>>> setTransactions(@NonNull Activity cur_act, boolean clear, @NonNull Transaction[] tsctOBJ) {
        List<Task<HashMap<String, Object>>> responses = new ArrayList<>();
        for(Transaction tsct : tsctOBJ) {
            if(tsct.getId() != null)
                responses.add(setDoc(cur_act, "transactions", tsct.getId(), clear, tsct));
        }
        return responses;
    }

    @NonNull
    public static Task<Transaction> getTransaction(@NonNull Activity cur_act, @NonNull String docID) {
        TaskCompletionSource<Transaction> source = new TaskCompletionSource<>();
        getDoc(cur_act, "transactions", docID)
                .addOnSuccessListener(task -> {
                    Transaction result = new Transaction(task);
                    source.setResult(result);
                })
                .addOnFailureListener(source::setException);
        return source.getTask();
    }

    @NonNull
    public static List<Task<Transaction>> getTransactions(@NonNull Activity cur_act, @NonNull String[] docID) {
        List<Task<Transaction>> responses = new ArrayList<>();
        for(String s : docID) {
            TaskCompletionSource<Transaction> source = new TaskCompletionSource<>();
            getDoc(cur_act, "transactions", s)
                    .addOnSuccessListener(task -> {
                        Transaction result = new Transaction(task);
                        source.setResult(result);
                    })
                    .addOnFailureListener(source::setException);
            responses.add(source.getTask());
        }
        return responses;
    }

    public static boolean isAnyObjectNull(Object... objects) {
        for (Object o : objects) { return o == null; }
        return false;
    }
}
