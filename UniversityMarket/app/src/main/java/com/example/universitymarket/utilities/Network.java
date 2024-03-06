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
import org.json.JSONException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public abstract class Network {

    @NonNull
    @SafeVarargs
    private static Task<HashMap<String, Object>> setDoc(@NonNull Activity cur_act, @NonNull String collID, @NonNull String docID, boolean clear, HashMap<String, Object>... obj) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final TaskCompletionSource<HashMap<String, Object>> source = new TaskCompletionSource<>();
        HashMap<String, Object> pojoObj = new HashMap<>();
        HashMap<String, Object> pojo;
        String illCollID = "Must be 'users', 'posts', 'transactions', or 'test'";
        String illNumObj = "Must contain either zero or one argument";
        String illReqObj = "Cached data for '" + docID + "' not found";
        String illNullData = "POJO data could not be found";

        if(obj.length > 1) {
            if(!source.getTask().isComplete())
                source.setException(new IllegalArgumentException(illNumObj));
            return source.getTask();
        } else if(obj.length == 1) {
            HashMap<String, Object> buffer = obj[0];
            Data.mergeHash(pojoObj, buffer);
        }

        DocumentReference reference = db.collection(collID).document(docID);

        switch(collID) {
            case "users":
            case "test":
            case "transactions":
            case "posts":
                if(obj.length == 0 && !clear) {
                    HashMap<String, Object> buffer = Data.getCachedToPOJO(cur_act, docID);
                    if(buffer != null) {
                        pojo = buffer;
                    } else {
                        if (!source.getTask().isComplete())
                            source.setException(new IllegalArgumentException(illReqObj));
                        return source.getTask();
                    }
                } else {
                    pojo = pojoObj;
                }
                break;
            default:
                if(!source.getTask().isComplete())
                    source.setException(new IllegalArgumentException(illCollID));
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
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                    source.setException(new IllegalArgumentException(illCollID));
                return source.getTask();
        }

        pojo = Data.resToPOJO(cur_act, resID);

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
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                    source.setException(new IllegalArgumentException(illCollID));
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

    public static void setTestFromCache(@NonNull Activity cur_act, @NonNull String docID, boolean clear, @Nullable NetListener<Test> response) {
        Task<HashMap<String, Object>> echo = setDoc(cur_act, "test", docID, clear);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Test result = new Test(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setTestsFromCache(@NonNull Activity cur_act, boolean clear, @Nullable NetListener<List<Test>> response) {
        File[] files = Data.getCachedFiles(cur_act, "test");
        List<Test> responses = new ArrayList<>();
        for(int i = 0; i < files.length; i++) {
            Task<HashMap<String, Object>> echo = setDoc(cur_act,"test", files[i].getName().split("(.json)")[0], clear);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == files.length - 1) {
                    echo.addOnSuccessListener(task -> {
                        Test result = new Test(task);
                        responses.add(result);
                        response.onSuccess(responses);
                    });
                } else {
                    echo.addOnSuccessListener(task -> {
                        Test result = new Test(task);
                        responses.add(result);
                    });
                }
            }
        }
    }

    public static void setTest(@NonNull Activity cur_act, @NonNull Test testOBJ, boolean clear, @Nullable NetListener<Test> response) {
        if(response == null && testOBJ.getFieldLvl1() == null)
            return;
        else if(response != null && testOBJ.getFieldLvl1() == null) {
            response.onFailure(new NullPointerException("Test ID does not exist in test object " + testOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc(cur_act,"test", testOBJ.getFieldLvl1(), clear, testOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Test result = new Test(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setTests(@NonNull Activity cur_act, @NonNull Test[] testOBJ, boolean clear, @Nullable NetListener<List<Test>> response) {
        List<Test> responses = new ArrayList<>();
        for(int i = 0; i < testOBJ.length; i++) {
            if(response == null && testOBJ[i].getFieldLvl1() == null)
                continue;
            else if(response != null && testOBJ[i].getFieldLvl1() == null) {
                response.onFailure(new NullPointerException("Test ID does not exist in test object " + testOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc(cur_act,"test", testOBJ[i].getFieldLvl1(), clear, testOBJ[i]);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == testOBJ.length - 1) {
                    echo.addOnSuccessListener(task -> {
                        Test result = new Test(task);
                        responses.add(result);
                        response.onSuccess(responses);
                    });
                } else {
                    echo.addOnSuccessListener(task -> {
                        Test result = new Test(task);
                        responses.add(result);
                    });
                }
            }
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

    public static void setUserFromCache(@NonNull Activity cur_act, boolean clear, @Nullable NetListener<User> response) {
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

    public static void getUser(@NonNull Activity cur_act) {
        getDoc(cur_act, "users", ActiveUser.email)
                .addOnSuccessListener(task -> {
                    User result = new User(task);
                    Data.setActiveUser(result, null);
                    Pair<String, HashMap<String, Object>> pair = new Pair<>(
                            "user_cache.json",
                            result.getSuper()
                    );
                    Data.setCache(cur_act, task, false);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        cur_act,
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }

    public static void setOtherUser(@NonNull Activity cur_act, @NonNull User userOBJ, boolean clear, @Nullable NetListener<User> response) {
        if(response == null && userOBJ.getId() == null)
            return;
        else if(response != null && userOBJ.getId() == null) {
            response.onFailure(new NullPointerException("User ID does not exist in user object " + userOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc(cur_act,"users", userOBJ.getId(), clear, userOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                User result = new User(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
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


    public static void setPostFromCache(@NonNull Activity cur_act, @NonNull String docID, boolean clear, @Nullable NetListener<Post> response) {
        Task<HashMap<String, Object>> echo = setDoc(cur_act, "posts", docID, clear);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Post result = new Post(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setPostsFromCache(@NonNull Activity cur_act, boolean clear, @Nullable NetListener<List<Post>> response) {
        File[] files = Data.getCachedFiles(cur_act, "post");
        List<Post> responses = new ArrayList<>();
        for(int i = 0; i < files.length; i++) {
            Task<HashMap<String, Object>> echo = setDoc(cur_act,"posts", files[i].getName().split("(.json)")[0], clear);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == files.length - 1) {
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

    public static void setTransactionFromCache(@NonNull Activity cur_act, @NonNull String docID, boolean clear, @Nullable NetListener<Transaction> response) {
        Task<HashMap<String, Object>> echo = setDoc(cur_act, "transactions", docID, clear);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Transaction result = new Transaction(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setTransactionsFromCache(@NonNull Activity cur_act, boolean clear, @Nullable NetListener<List<Transaction>> response) {
        File[] files = Data.getCachedFiles(cur_act, "tsct");
        List<Transaction> responses = new ArrayList<>();
        for(int i = 0; i < files.length; i++) {
            Task<HashMap<String, Object>> echo = setDoc(cur_act,"transactions", files[i].getName().split("(.json)")[0], clear);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == files.length - 1) {
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

