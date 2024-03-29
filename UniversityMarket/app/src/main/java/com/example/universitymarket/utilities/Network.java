package com.example.universitymarket.utilities;

import android.widget.Toast;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.objects.Transaction;
import com.example.universitymarket.objects.Test;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.globals.actives.ActiveUser;
/**
 * <b>
 * ANY MODIFICATIONS ABOVE WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * DO NOT REMOVE THIS BLOCK 
*/
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.universitymarket.globals.Policy;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public abstract class Network {

    @NonNull
    private static Task<HashMap<String, Object>> setDoc(@NonNull Activity cur_act, @NonNull String collID, @NonNull String docID, boolean clear, @Nullable HashMap<String, Object> obj) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final TaskCompletionSource<HashMap<String, Object>> source = new TaskCompletionSource<>();
        final HashMap<String, Object> pojo;
        List<String> collNames = new ArrayList<>();
        for(String s : Policy.json_filenames)
            collNames.add(s.split("_")[0] + "s");
        String illReqObj = "Cached data for '" + docID + "' not found";
        String illFormat = "'" + docID + "' from '" + collID + "' is not in skeleton format!";
        String illColl = collID + " is not among the existing collections + ( ";
        for(String s : collNames)
            illColl = illColl.concat(s + " ");
        illColl = illColl.concat(")");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!source.getTask().isComplete())
                source.setException(new TimeoutException());
        }, Policy.max_seconds_before_timeout * 1000);

        if(collNames.contains(collID)) {
            final DocumentReference reference = db.collection(collID).document(docID);
            if(!clear) {
                if(obj != null) {
                    pojo = obj;
                } else {
                    HashMap<String, Object> buffer = Data.getCachedToPOJO(cur_act, docID);
                    if(buffer != null) {
                        pojo = buffer;
                    } else {
                        if(!source.getTask().isComplete())
                            source.setException(new IllegalArgumentException(illReqObj));
                        return source.getTask();
                    }
                }

                reference.set(pojo)
                        .addOnSuccessListener(task -> {
                            if(!source.getTask().isComplete() && Data.isAnyObjectNull(pojo.values().toArray())) {
                                source.setException(new JSONException(illFormat));
                                return;
                            }
                            if(!source.getTask().isComplete())
                                source.setResult(pojo);
                        })
                        .addOnFailureListener(e -> {
                            if(!source.getTask().isComplete())
                                source.setException(e);
                        });
            } else {
                reference.delete();
                source.setResult(new HashMap<>());
            }
        } else {
            if(!source.getTask().isComplete())
                source.setException(new IllegalArgumentException(illColl));
        }

        return source.getTask();
    }

    @NonNull
    private static Task<HashMap<String, Object>> getDoc(@NonNull Activity cur_act, @NonNull String collID, @NonNull String docID) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final TaskCompletionSource<HashMap<String, Object>> source = new TaskCompletionSource<>();
        HashMap<String, String> collMap = new HashMap<>();
        List<String> collNames = new ArrayList<>();
        for(String s : Policy.json_filenames) {
            String buffer = s.split("_")[0] + "s";
            collNames.add(buffer);
            collMap.put(buffer, s.split("\\.")[0]);
        }
        String illNullData = "Collection '" + collID + "' or document '" + docID + "' does not exist";
        String illColl = collID + " is not among the existing collections + ( ";
        for(String s : collNames)
            illColl = illColl.concat(s + " ");
        illColl = illColl.concat(")");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!source.getTask().isComplete())
                source.setException(new TimeoutException());
        }, Policy.max_seconds_before_timeout * 1000);

        if(collNames.contains(collID)) {
            Task<DocumentSnapshot> reference = db.collection(collID).document(docID).get();
            int resID = cur_act.getResources().getIdentifier(collMap.get(collID),"raw", cur_act.getPackageName());
            HashMap<String, Object> pojo = Data.resToPOJO(cur_act, resID);

            reference
                    .addOnSuccessListener(task -> {
                        if(!source.getTask().isComplete() && !reference.getResult().exists()) {
                            source.setException(new NullPointerException(illNullData));
                            return;
                        }

                        Map<String, Object> rawdata = task.getData();
                        Data.mergeHash(pojo, rawdata);
                        if(!source.getTask().isComplete())
                        source.setResult((HashMap<String, Object>) rawdata);
                    })
                    .addOnFailureListener(e -> {
                        if(!source.getTask().isComplete())
                            source.setException(e);
                    });
        } else {
            if(!source.getTask().isComplete())
                source.setException(new IllegalArgumentException(illColl));
        }

        return source.getTask();
    }

    @NonNull
    private static Task<List<HashMap<String, Object>>> getColl(@NonNull Activity cur_act, @NonNull String collID, @Nullable Filter filter, int pageNo) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final TaskCompletionSource<List<HashMap<String, Object>>> source = new TaskCompletionSource<>();
        List<String> collNames = new ArrayList<>();
        for(String s : Policy.json_filenames)
            collNames.add(s.split("_")[0] + "s");
        String illNullData = "Collection '" + collID + "' does not exist";
        String illColl = collID + " is not among the existing collections + ( ";
        String illPageNo = "Page number must be 1 or greater";
        for(String s : collNames)
            illColl = illColl.concat(s + " ");
        illColl = illColl.concat(")");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!source.getTask().isComplete())
                source.setException(new TimeoutException());
        }, Policy.max_seconds_before_timeout * 1000);

        if(collNames.contains(collID)) {
            Query query = db.collection(collID);
            query = filter != null ? query.where(filter) : query;
            if(pageNo >= 0) {
                query.orderBy("id").startAt(Policy.max_docs_loaded * pageNo - 1);
                query.limit(Policy.max_docs_loaded);
            } else {
                if(!source.getTask().isComplete()) {
                    source.setException(new IllegalArgumentException(illPageNo));
                    return source.getTask();
                }
            }
            Task<QuerySnapshot> reference = query.get();

            reference
                    .addOnSuccessListener(coll -> {
                        if (!source.getTask().isComplete() && reference.getResult().isEmpty()) {
                            source.setException(new NullPointerException(illNullData));
                            return;
                        }
                        List<HashMap<String, Object>> list = new ArrayList<>();
                        List<DocumentSnapshot> docs = coll.getDocuments();

                        for (int i = 0; i < docs.size(); i++) {
                            DocumentSnapshot thisDoc = docs.get(i);
                            Task<HashMap<String, Object>> echo = getDoc(cur_act, collID, thisDoc.getId());
                            echo.addOnFailureListener(err ->
                                    Log.e("getColl", thisDoc.getId() + " doc is invalid: " + err)
                            );
                            if (i == docs.size() - 1) {
                                echo.addOnSuccessListener(task -> {
                                    list.add(task);
                                    if (!source.getTask().isComplete())
                                        source.setResult(list);
                                });
                            } else {
                                echo.addOnSuccessListener(list::add);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!source.getTask().isComplete())
                            source.setException(e);
                    });
        } else {
            if(!source.getTask().isComplete())
                source.setException(new IllegalArgumentException(illColl));
        }

        return source.getTask();
    }

    public static void uploadImage(@NonNull Uri uri, @NonNull String directory, @NonNull String filename, @Nullable Callback<Uri> response) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference reference = storage.getReference(directory + "/" + Data.generateID(filename));
        UploadTask upload = reference.putFile(uri);

        if(response != null) {
            upload
                    .addOnSuccessListener(task ->
                            reference.getDownloadUrl()
                                .addOnSuccessListener(response::onSuccess)
                                .addOnFailureListener(response::onFailure)
                    )
                    .addOnFailureListener(response::onFailure);
        }
    }



/**
 * <b>
 * ANY MODIFICATIONS BELOW WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * DO NOT REMOVE THIS BLOCK 
*/

    public static void setUserFromCache(@NonNull Activity cur_act, @NonNull String docID, boolean clear, @Nullable Callback<User> response) {
        Task<HashMap<String, Object>> echo = setDoc(cur_act, "users", docID, clear, null);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                User result = new User(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setUserFromCache(@NonNull Activity cur_act, boolean clear, @Nullable Callback<User> response) {
        Task<HashMap<String, Object>> echo = setDoc(cur_act, "users", ActiveUser.id, clear, null);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                User result = new User(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setUser(@NonNull Activity cur_act, @NonNull User userOBJ, boolean clear, @Nullable Callback<User> response) {
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

    public static void setUsers(@NonNull Activity cur_act, @NonNull User[] userOBJ, boolean clear, @Nullable Callback<List<User>> response) {
        List<User> responses = new ArrayList<>();
        for(int i = 0; i < userOBJ.length; i++) {
            if(response == null && userOBJ[i].getId() == null)
                continue;
            else if(response != null && userOBJ[i].getId() == null) {
                response.onFailure(new NullPointerException("User ID does not exist in user object " + userOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc(cur_act,"users", userOBJ[i].getId(), clear, userOBJ[i]);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == userOBJ.length - 1) {
                    echo.addOnSuccessListener(task -> {
                        User result = new User(task);
                        responses.add(result);
                        response.onSuccess(responses);
                    });
                } else {
                    echo.addOnSuccessListener(task -> {
                        User result = new User(task);
                        responses.add(result);
                    });
                }
            }
        }
    }

    public static void syncUserCache(@NonNull Activity cur_act) {
        getDoc(cur_act, "users", ActiveUser.id)
                .addOnSuccessListener(task -> {
                    User result = new User(task);
                    Data.setActiveUser(cur_act, result);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        cur_act,
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }

    public static void getUser(@NonNull Activity cur_act, @NonNull String docID, @NonNull Callback<User> response) {
        getDoc(cur_act,"users", docID)
                .addOnSuccessListener(task -> {
                    User result = new User(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getUsers(@NonNull Activity cur_act, @NonNull List<String> docID, @NonNull Callback<List<User>> response) {
        List<User> list = new ArrayList<>();
        if(docID.size() == 0)
            response.onFailure(new NullPointerException("No documents are available"));
        for(int i = 0; i < docID.size(); i++) {
            Task<HashMap<String, Object>> echo = getDoc(cur_act,"users", docID.get(i));
            echo.addOnFailureListener(response::onFailure);
            if(i == docID.size() - 1) {
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

    public static void getUsers(@NonNull Activity cur_act, @NonNull Filter filter, int pageNo, @NonNull Callback<List<User>> response) {
        List<User> list = new ArrayList<>();
        getColl(cur_act,"users", filter, Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        User result = new User(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getUsers(@NonNull Activity cur_act, int pageNo, @NonNull Callback<List<User>> response) {
        List<User> list = new ArrayList<>();
        getColl(cur_act,"users", null, (char) (Math.max(pageNo, 0)))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        User result = new User(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void setTransaction(@NonNull Activity cur_act, @NonNull Transaction transactionOBJ, boolean clear, @Nullable Callback<Transaction> response) {
        if(response == null && transactionOBJ.getId() == null)
            return;
        else if(response != null && transactionOBJ.getId() == null) {
            response.onFailure(new NullPointerException("Transaction ID does not exist in transaction object " + transactionOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc(cur_act,"transactions", transactionOBJ.getId(), clear, transactionOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Transaction result = new Transaction(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setTransactions(@NonNull Activity cur_act, @NonNull Transaction[] transactionOBJ, boolean clear, @Nullable Callback<List<Transaction>> response) {
        List<Transaction> responses = new ArrayList<>();
        for(int i = 0; i < transactionOBJ.length; i++) {
            if(response == null && transactionOBJ[i].getId() == null)
                continue;
            else if(response != null && transactionOBJ[i].getId() == null) {
                response.onFailure(new NullPointerException("Transaction ID does not exist in transaction object " + transactionOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc(cur_act,"transactions", transactionOBJ[i].getId(), clear, transactionOBJ[i]);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == transactionOBJ.length - 1) {
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

    public static void getTransaction(@NonNull Activity cur_act, @NonNull String docID, @NonNull Callback<Transaction> response) {
        getDoc(cur_act,"transactions", docID)
                .addOnSuccessListener(task -> {
                    Transaction result = new Transaction(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTransactions(@NonNull Activity cur_act, @NonNull List<String> docID, @NonNull Callback<List<Transaction>> response) {
        List<Transaction> list = new ArrayList<>();
        if(docID.size() == 0)
            response.onFailure(new NullPointerException("No documents are available"));
        for(int i = 0; i < docID.size(); i++) {
            Task<HashMap<String, Object>> echo = getDoc(cur_act,"transactions", docID.get(i));
            echo.addOnFailureListener(response::onFailure);
            if(i == docID.size() - 1) {
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

    public static void getTransactions(@NonNull Activity cur_act, @NonNull Filter filter, int pageNo, @NonNull Callback<List<Transaction>> response) {
        List<Transaction> list = new ArrayList<>();
        getColl(cur_act,"transactions", filter, Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Transaction result = new Transaction(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTransactions(@NonNull Activity cur_act, int pageNo, @NonNull Callback<List<Transaction>> response) {
        List<Transaction> list = new ArrayList<>();
        getColl(cur_act,"transactions", null, (char) (Math.max(pageNo, 0)))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Transaction result = new Transaction(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void setTest(@NonNull Activity cur_act, @NonNull Test testOBJ, boolean clear, @Nullable Callback<Test> response) {
        if(response == null && testOBJ.getFieldLvl1() == null)
            return;
        else if(response != null && testOBJ.getFieldLvl1() == null) {
            response.onFailure(new NullPointerException("Test ID does not exist in test object " + testOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc(cur_act,"tests", testOBJ.getFieldLvl1(), clear, testOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Test result = new Test(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setTests(@NonNull Activity cur_act, @NonNull Test[] testOBJ, boolean clear, @Nullable Callback<List<Test>> response) {
        List<Test> responses = new ArrayList<>();
        for(int i = 0; i < testOBJ.length; i++) {
            if(response == null && testOBJ[i].getFieldLvl1() == null)
                continue;
            else if(response != null && testOBJ[i].getFieldLvl1() == null) {
                response.onFailure(new NullPointerException("Test ID does not exist in test object " + testOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc(cur_act,"tests", testOBJ[i].getFieldLvl1(), clear, testOBJ[i]);
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

    public static void getTest(@NonNull Activity cur_act, @NonNull String docID, @NonNull Callback<Test> response) {
        getDoc(cur_act,"tests", docID)
                .addOnSuccessListener(task -> {
                    Test result = new Test(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTests(@NonNull Activity cur_act, @NonNull List<String> docID, @NonNull Callback<List<Test>> response) {
        List<Test> list = new ArrayList<>();
        if(docID.size() == 0)
            response.onFailure(new NullPointerException("No documents are available"));
        for(int i = 0; i < docID.size(); i++) {
            Task<HashMap<String, Object>> echo = getDoc(cur_act,"tests", docID.get(i));
            echo.addOnFailureListener(response::onFailure);
            if(i == docID.size() - 1) {
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

    public static void getTests(@NonNull Activity cur_act, @NonNull Filter filter, int pageNo, @NonNull Callback<List<Test>> response) {
        List<Test> list = new ArrayList<>();
        getColl(cur_act,"tests", filter, Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Test result = new Test(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTests(@NonNull Activity cur_act, int pageNo, @NonNull Callback<List<Test>> response) {
        List<Test> list = new ArrayList<>();
        getColl(cur_act,"tests", null, (char) (Math.max(pageNo, 0)))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Test result = new Test(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void setPost(@NonNull Activity cur_act, @NonNull Post postOBJ, boolean clear, @Nullable Callback<Post> response) {
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

    public static void setPosts(@NonNull Activity cur_act, @NonNull Post[] postOBJ, boolean clear, @Nullable Callback<List<Post>> response) {
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

    public static void getPost(@NonNull Activity cur_act, @NonNull String docID, @NonNull Callback<Post> response) {
        getDoc(cur_act,"posts", docID)
                .addOnSuccessListener(task -> {
                    Post result = new Post(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getPosts(@NonNull Activity cur_act, @NonNull List<String> docID, @NonNull Callback<List<Post>> response) {
        List<Post> list = new ArrayList<>();
        if(docID.size() == 0)
            response.onFailure(new NullPointerException("No documents are available"));
        for(int i = 0; i < docID.size(); i++) {
            Task<HashMap<String, Object>> echo = getDoc(cur_act,"posts", docID.get(i));
            echo.addOnFailureListener(response::onFailure);
            if(i == docID.size() - 1) {
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

    public static void getPosts(@NonNull Activity cur_act, @NonNull Filter filter, int pageNo, @NonNull Callback<List<Post>> response) {
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

    public static void getPosts(@NonNull Activity cur_act, int pageNo, @NonNull Callback<List<Post>> response) {
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

}