package com.example.universitymarket.utilities;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
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

        String cache_name = "test_cache";
        DocumentReference reference = db.collection(collID).document(docID);

        switch(collID) {
            case "users":
                cache_name = "user_cache";
            case "test":
                pojo = Data.getCache(cur_act, cache_name);
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

        if(clear || pojo.size() == 0) {
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
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
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
                        members.add(rawdata != null ? rawdata.get(s) : null);
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
    private static Task<List<HashMap<String, Object>>> getColl(@NonNull Activity cur_act, @NonNull String collID, @Nullable Filter filter, int pageNo) {
        final TaskCompletionSource<List<HashMap<String, Object>>> source = new TaskCompletionSource<>();
        String illCollID = "Must be 'users', 'posts', 'transactions', or 'test'";
        String illNullData = "Collection '" + collID + "' does not exist";
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!source.getTask().isComplete())
                source.setException(new TimeoutException());
        }, Policy.max_seconds_before_timeout * 1000);

        Query query = db.collection(collID);
        query = filter != null ? query.where(filter) : query;
        if(pageNo >= 0) {
            query.startAt(Policy.max_docs_loaded * pageNo);
            query.limit(Policy.max_docs_loaded);
        }
        Task<QuerySnapshot> reference = query.get();

        switch(collID) {
            case "users":
            case "posts":
            case "transactions":
            case "test":
                break;
            default:
                if(!source.getTask().isComplete())
                    source.setException(new ArgumentException("getColl", "collID", illCollID));
                return source.getTask();
        }

        reference
                .addOnSuccessListener(coll -> {
                    if(!source.getTask().isComplete() && reference.getResult().isEmpty()) {
                        source.setException(new NullPointerException(illNullData));
                        return;
                    }
                    List<HashMap<String, Object>> list = new ArrayList<>();
                    List<DocumentSnapshot> docs = coll.getDocuments();

                    for(int i = 0; i < docs.size(); i++) {
                        DocumentSnapshot thisDoc = docs.get(i);
                        Task<HashMap<String, Object>> echo = getDoc(cur_act, collID, thisDoc.getId());
                        echo.addOnFailureListener(err ->
                                Log.e("getColl", thisDoc.getId() + " doc is invalid: " + err)
                        );
                        if (i == docs.size() - 1) {
                            echo.addOnSuccessListener(task -> {
                                list.add(task);
                                if(!source.getTask().isComplete())
                                    source.setResult(list);
                            });
                        } else {
                            echo.addOnSuccessListener(list::add);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if(!source.getTask().isComplete())
                        source.setException(e);
                });

        return source.getTask();
    }

    public static void setTest(@NonNull Activity cur_act, @NonNull String docID, boolean clear, @Nullable NetListener<Test> response) {
        Task<HashMap<String, Object>> echo = setDoc(cur_act, "test", docID, clear);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Test result = new Test(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void getTest(@NonNull Activity cur_act, @NonNull String docID, @NonNull NetListener<Test> response) {
        getDoc(cur_act, "test", docID)
                .addOnSuccessListener(task -> {
                    Test result = new Test(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTests(@NonNull Activity cur_act, @NonNull String[] docID, @NonNull NetListener<List<Test>> response) {
        List<Test> list = new ArrayList<>();
        for(int i = 0; i < docID.length; i++) {
            Task<HashMap<String, Object>> echo = getDoc(cur_act,"test", docID[i]);
            echo.addOnFailureListener(response::onFailure);
            if(i == docID.length - 1) {
                echo.addOnSuccessListener(task -> {
                    Test result = new Test(task);
                    list.add(result);
                    response.onSuccess(list);
                });
            } else {
                echo.addOnSuccessListener(task -> {
                    Test result = new Test(task);
                    list.add(result);
                });
            }
        }
    }

    public static void getTests(@NonNull Activity cur_act, @NonNull Filter filter, int pageNo, @NonNull NetListener<List<Test>> response) {
        List<Test> list = new ArrayList<>();
        getColl(cur_act,"test", filter, Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Test result = new Test(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTests(@NonNull Activity cur_act, int pageNo, @NonNull NetListener<List<Test>> response) {
        List<Test> list = new ArrayList<>();
        getColl(cur_act,"test", null, Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Test result = new Test(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTests(@NonNull Activity cur_act, @NonNull Filter filter, @NonNull NetListener<List<Test>> response) {
        List<Test> list = new ArrayList<>();
        getColl(cur_act,"test", filter, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Test result = new Test(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTests(@NonNull Activity cur_act, @NonNull NetListener<List<Test>> response) {
        List<Test> list = new ArrayList<>();
        getColl(cur_act,"test", null, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Test result = new Test(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void uploadActiveUser(@NonNull Activity cur_act, boolean clear, @Nullable NetListener<User> response) {
        Task<HashMap<String, Object>> echo = setDoc(cur_act,"users", ActiveUser.email, clear);
        if(response == null && ActiveUser.email.equals("unknown"))
            return;
        if(response != null) {
            if(ActiveUser.email.equals("unknown")) {
                response.onFailure(new InstantiationException("ActiveUser has never been initialized"));
                return;
            }
            echo.addOnSuccessListener(task -> {
                User result = new User(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void downloadActiveUser(@NonNull Activity cur_act) {
        getDoc(cur_act, "users", ActiveUser.email)
                .addOnSuccessListener(task -> {
                    User result = new User(task);
                    Data.setActiveUser(result, null);
                    Pair<String, HashMap<String, Object>> pair = new Pair<>(
                            "user_cache.json",
                            result.getSuper()
                    );
                    List<Pair<String, HashMap<String, Object>>> list = new ArrayList<>();
                    list.add(pair);
                    Data.setCache(cur_act, list);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        cur_act,
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }

    public static void getOtherUser(@NonNull Activity cur_act, @NonNull String docID, @NonNull NetListener<User> response) {
        getDoc(cur_act, "users", docID)
                .addOnSuccessListener(task -> {
                    User result = new User(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getOtherUsers(@NonNull Activity cur_act, @NonNull String[] docID, @NonNull NetListener<List<User>> response) {
        List<User> list = new ArrayList<>();
        for(int i = 0; i < docID.length; i++) {
            Task<HashMap<String, Object>> echo = getDoc(cur_act,"users", docID[i]);
            echo.addOnFailureListener(response::onFailure);
            if(i == docID.length - 1) {
                echo.addOnSuccessListener(task -> {
                    User result = new User(task);
                    list.add(result);
                    response.onSuccess(list);
                });
            } else {
                echo.addOnSuccessListener(task -> {
                    User result = new User(task);
                    list.add(result);
                });
            }
        }
    }

    public static void getOtherUsers(@NonNull Activity cur_act, @NonNull Filter filter, @NonNull NetListener<List<User>> response) {
        List<User> list = new ArrayList<>();
        getColl(cur_act,"users", filter, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        User result = new User(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getOtherUsers(@NonNull Activity cur_act, @NonNull NetListener<List<User>> response) {
        List<User> list = new ArrayList<>();
        getColl(cur_act,"users", null, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        User result = new User(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void setPost(@NonNull Activity cur_act, @NonNull Post postOBJ, boolean clear, @Nullable NetListener<Post> response) {
        if(response == null && postOBJ.getId() == null)
            return;
        else if(response != null && postOBJ.getId() == null) {
            response.onFailure(new NullPointerException("Post ID does not exist in post object " + postOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc(cur_act,"posts", postOBJ.getId(), clear, postOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Post result = new Post(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setPosts(@NonNull Activity cur_act, @NonNull Post[] postOBJ, boolean clear, @Nullable NetListener<List<Post>> response) {
        List<Post> responses = new ArrayList<>();
        for(int i = 0; i < postOBJ.length; i++) {
            if(response == null && postOBJ[i].getId() == null)
                continue;
            else if(response != null && postOBJ[i].getId() == null) {
                response.onFailure(new NullPointerException("Post ID does not exist in post object " + postOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc(cur_act,"posts", postOBJ[i].getId(), clear, postOBJ[i]);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == postOBJ.length - 1) {
                    echo.addOnSuccessListener(task -> {
                        Post result = new Post(task);
                        responses.add(result);
                        response.onSuccess(responses);
                    });
                } else {
                    echo.addOnSuccessListener(task -> {
                        Post result = new Post(task);
                        responses.add(result);
                    });
                }
            }
        }
    }

    public static void getPost(@NonNull Activity cur_act, @NonNull String docID, @NonNull NetListener<Post> response) {
        getDoc(cur_act,"posts", docID)
                .addOnSuccessListener(task -> {
                    Post result = new Post(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getPosts(@NonNull Activity cur_act, @NonNull String[] docID, @NonNull NetListener<List<Post>> response) {
        List<Post> list = new ArrayList<>();
        for(int i = 0; i < docID.length; i++) {
            Task<HashMap<String, Object>> echo = getDoc(cur_act,"posts", docID[i]);
            echo.addOnFailureListener(response::onFailure);
            if(i == docID.length - 1) {
                echo.addOnSuccessListener(task -> {
                    Post result = new Post(task);
                    list.add(result);
                    response.onSuccess(list);
                });
            } else {
                echo.addOnSuccessListener(task -> {
                    Post result = new Post(task);
                    list.add(result);
                });
            }
        }
    }

    public static void getPosts(@NonNull Activity cur_act, @NonNull Filter filter, int pageNo, @NonNull NetListener<List<Post>> response) {
        List<Post> list = new ArrayList<>();
        getColl(cur_act,"posts", filter, Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Post result = new Post(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getPosts(@NonNull Activity cur_act, int pageNo, @NonNull NetListener<List<Post>> response) {
        List<Post> list = new ArrayList<>();
        getColl(cur_act,"posts", null, (char) (Math.max(pageNo, 0)))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Post result = new Post(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void setTransaction(@NonNull Activity cur_act, @NonNull Transaction tsctOBJ, boolean clear, @Nullable NetListener<Transaction> response) {
        if(response == null && tsctOBJ.getId() == null)
            return;
        else if(response != null && tsctOBJ.getId() == null) {
            response.onFailure(new NullPointerException("Transaction ID does not exist in transaction object " + tsctOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc(cur_act,"transactions", tsctOBJ.getId(), clear, tsctOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Transaction result = new Transaction(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setTransactions(@NonNull Activity cur_act, @NonNull Transaction[] tsctOBJ, boolean clear, @Nullable NetListener<List<Transaction>> response) {
        List<Transaction> responses = new ArrayList<>();
        for(int i = 0; i < tsctOBJ.length; i++) {
            if(response == null && tsctOBJ[i].getId() == null)
                continue;
            else if(response != null && tsctOBJ[i].getId() == null) {
                response.onFailure(new NullPointerException("Transaction ID does not exist in transaction object " + tsctOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc(cur_act,"transactions", tsctOBJ[i].getId(), clear, tsctOBJ[i]);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == tsctOBJ.length - 1) {
                    echo.addOnSuccessListener(task -> {
                        Transaction result = new Transaction(task);
                        responses.add(result);
                        response.onSuccess(responses);
                    });
                } else {
                    echo.addOnSuccessListener(task -> {
                        Transaction result = new Transaction(task);
                        responses.add(result);
                    });
                }
            }
        }
    }

    public static void getTransaction(@NonNull Activity cur_act, @NonNull String docID, @NonNull NetListener<Transaction> response) {
        getDoc(cur_act,"transactions", docID)
                .addOnSuccessListener(task -> {
                    Transaction result = new Transaction(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTransactions(@NonNull Activity cur_act, @NonNull String[] docID, @NonNull NetListener<List<Transaction>> response) {
        List<Transaction> list = new ArrayList<>();
        for(int i = 0; i < docID.length; i++) {
            Task<HashMap<String, Object>> echo = getDoc(cur_act,"transactions", docID[i]);
            echo.addOnFailureListener(response::onFailure);
            if(i == docID.length - 1) {
                echo.addOnSuccessListener(task -> {
                    Transaction result = new Transaction(task);
                    list.add(result);
                    response.onSuccess(list);
                });
            } else {
                echo.addOnSuccessListener(task -> {
                    Transaction result = new Transaction(task);
                    list.add(result);
                });
            }
        }
    }

    public static void getTransactions(@NonNull Activity cur_act, @NonNull Filter filter, @NonNull NetListener<List<Transaction>> response) {
        List<Transaction> list = new ArrayList<>();
        getColl(cur_act,"transactions", filter, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Transaction result = new Transaction(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static boolean isAnyObjectNull(Object... objects) {
        for (Object o : objects) { return o == null; }
        return false;
    }
}

