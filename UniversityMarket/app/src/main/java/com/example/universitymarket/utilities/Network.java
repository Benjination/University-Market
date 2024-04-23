package com.example.universitymarket.utilities;

import android.widget.Toast;
import com.example.universitymarket.models.User;
import com.example.universitymarket.models.Chat;
import com.example.universitymarket.models.Message;
import com.example.universitymarket.models.Transaction;
import com.example.universitymarket.models.Post;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Pair;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public abstract class Network {

    @NonNull
    private static Task<HashMap<String, Object>> setDoc(@NonNull String collID, @NonNull String docID, boolean clear, @NonNull HashMap<String, Object> obj) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final TaskCompletionSource<HashMap<String, Object>> source = new TaskCompletionSource<>();
        final HashMap<String, Object> pojo;
        String illReqObj = "Cached data for '" + docID + "' not found";
        String illFormat = "'" + docID + "' from '" + collID + "' is not in skeleton format!";
        String illColl = collID + " is not among the existing collections + ( ";
        String illTime = "Connection timeout";
        for(String s : Policy.collection_names)
            illColl = illColl.concat(s + " ");
        illColl = illColl.concat(")");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!source.getTask().isComplete())
                source.setException(new TimeoutException(illTime));
        }, Policy.max_seconds_before_timeout * 1000);

        if(Policy.collection_names.contains(collID)) {
            final DocumentReference reference = db.collection(collID).document(docID);
            if(!clear) {
                pojo = obj;
                reference.set(pojo)
                        .addOnSuccessListener(task -> {
                           // if(!source.getTask().isComplete() && Data.isAnyObjectNull(pojo.values().toArray())) {
                             //   source.setException(new JSONException(illFormat));
                               // return;
                           // }
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
    private static Task<HashMap<String, Object>> getDoc(@NonNull String collID, @NonNull String docID) {
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
        String illTime = "Connection timeout";
        for(String s : collNames)
            illColl = illColl.concat(s + " ");
        illColl = illColl.concat(")");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!source.getTask().isComplete())
                source.setException(new TimeoutException(illTime));
        }, Policy.max_seconds_before_timeout * 1000);

        if(Policy.collection_names.contains(collID)) {
            Task<DocumentSnapshot> reference = db.collection(collID).document(docID).get();

            reference
                    .addOnSuccessListener(task -> {
                        if(!source.getTask().isComplete() && !reference.getResult().exists()) {
                            source.setException(new NullPointerException(illNullData));
                            return;
                        }

                        Map<String, Object> rawdata = task.getData();
                        if(!source.getTask().isComplete()) {
                            source.setResult((HashMap<String, Object>) rawdata);
                        }
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

    private static void getCollRecursive(@NonNull String collID, @NonNull TaskCompletionSource<List<HashMap<String, Object>>> source, @NonNull List<HashMap<String, Object>> list, @NonNull List<DocumentSnapshot> docs, int i) {
        DocumentSnapshot thisDoc = docs.get(i);
        Task<HashMap<String, Object>> echo = getDoc(collID, thisDoc.getId());
        echo.addOnFailureListener(err ->
                Log.e("getCollRecursive", thisDoc.getId() + " doc is invalid: " + err)
        );
        if (i == docs.size() - 1) {
            echo.addOnSuccessListener(task -> {
                list.add(task);
                if (!source.getTask().isComplete())
                    source.setResult(list);
            });
        } else {
            echo.addOnSuccessListener(task -> {
                list.add(task);
                getCollRecursive(collID, source, list, docs, i + 1);
            });
        }
    }

    @NonNull
    private static Task<List<HashMap<String, Object>>> getColl(@NonNull String collID, @Nullable Filter filter, @Nullable SortByField sortByField, int pageNo) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final TaskCompletionSource<List<HashMap<String, Object>>> source = new TaskCompletionSource<>();
        String illNullData = "Collection '" + collID + "' does not exist";
        String illColl = collID + " is not among the existing collections + ( ";
        String illTime = "Connection timeout";
        for(String s : Policy.collection_names)
            illColl = illColl.concat(s + " ");
        illColl = illColl.concat(")");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!source.getTask().isComplete())
                source.setException(new TimeoutException(illTime));
        }, Policy.max_seconds_before_timeout * 1000);

        Log.i("sortByField", sortByField == null ? "null" : sortByField.direction.toString());

        if(Policy.collection_names.contains(collID)) {
            Query query = db.collection(collID);
            query = filter != null ? query.where(filter) : query;
            if(sortByField != null) {
                sortByField.direction = sortByField.direction != null ? sortByField.direction : Query.Direction.ASCENDING;
                Log.d("sortByField: ", sortByField.direction.toString());
                query = query.orderBy(sortByField.fieldName, sortByField.direction);
            }
            else{
                Log.d("sortByField ", "NULL");

                query = query.orderBy("id");
            }
            if(pageNo >= 0) {
                query.startAt(Policy.max_docs_loaded * pageNo - 1);
                query.limit(Policy.max_docs_loaded);
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

                        getCollRecursive(collID, source, list, docs, 0);
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

    @NonNull
    private static Pair<DocumentReference, Task<DocumentSnapshot>> listenToDoc(@NonNull String collID, @NonNull String docID) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference ref = db.collection(collID).document(docID);

        Task<DocumentSnapshot> snapshot = ref.get();
        return new Pair<>(ref, snapshot);
    }

    @NonNull
    private static Pair<CollectionReference, Task<QuerySnapshot>> listenToColl(@NonNull String collID) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference ref = db.collection(collID);

        Task<QuerySnapshot> snapshot = ref.get();
        return new Pair<>(ref, snapshot);
    }

    public static void uploadImage(@NonNull Uri uri, @NonNull String directory, @NonNull String filename, @Nullable Callback<Uri> response) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference reference = storage.getReference(directory + "/" + Data.generateID(filename) + ".png");
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

    public static void removeImages(@NonNull List<String> imageUrls, @Nullable Callback<Boolean> response) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();

        for(String url : imageUrls) {
            StorageReference reference = storage.getReferenceFromUrl(url);
            Task<Void> deletion = reference.delete();

            if(response != null) {
                deletion.addOnSuccessListener(task -> response.onSuccess(true));
                deletion.addOnFailureListener(response::onFailure);
            }
        }
    }

/**
 * <b>
 * ANY MODIFICATIONS BELOW WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * DO NOT REMOVE THIS BLOCK 
*/

    public static void setUser(@NonNull User userOBJ, boolean clear, @Nullable Callback<User> response) {
        if(response == null && userOBJ.getId() == null)
            return;
        else if(response != null && userOBJ.getId() == null) {
            response.onFailure(new NullPointerException("User ID does not exist in user object " + userOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc("users", userOBJ.getId(), clear, userOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                User result = new User(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setUsers(@NonNull User[] userOBJ, boolean clear, @Nullable Callback<List<User>> response) {
        List<User> responses = new ArrayList<>();
        for(int i = 0; i < userOBJ.length; i++) {
            if(response == null && userOBJ[i].getId() == null)
                continue;
            else if(response != null && userOBJ[i].getId() == null) {
                response.onFailure(new NullPointerException("User ID does not exist in user object " + userOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc("users", userOBJ[i].getId(), clear, userOBJ[i]);
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

    public static void getUser(@NonNull String docID, @NonNull Callback<User> response) {
        getDoc("users", docID)
                .addOnSuccessListener(task -> {
                    User result = new User(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getUsers(@NonNull List<String> docID, @Nullable SortByField sortByField, @NonNull Callback<List<User>> response) {
        List<User> list = new ArrayList<>();
        if(docID.isEmpty()) {
            response.onFailure(new Exception("A non-empty docID list is required"));
            return;
        }
        getColl("users", Filter.inArray("id", docID), sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        User result = new User(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getUsers(@Nullable Filter filter, @Nullable SortByField sortByField, @Nullable Integer pageNo, @NonNull Callback<List<User>> response) {
        List<User> list = new ArrayList<>();
        getColl("users", filter, sortByField, pageNo == null ? -1 : Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        User result = new User(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getAllUsers(@Nullable SortByField sortByField, @NonNull Callback<List<User>> response) {
        List<User> list = new ArrayList<>();
        getColl("users", null, sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        User result = new User(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToUser(@NonNull String docID, @NonNull Listener<User> response) {
        Pair<DocumentReference, Task<DocumentSnapshot>> pair = listenToDoc("users", docID);
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getData() == null || snapshot.getData() == null || task.getData().equals(snapshot.getData())) {
                        return;
                    }

                    int before = task.getData().entrySet().size(), after = snapshot.getData().entrySet().size();
                    if(before < after) {
                        response.onAdded(new User((HashMap<String, Object>) snapshot.getData()));
                    } else if(before > after) {
                        response.onRemoved(new User((HashMap<String, Object>) snapshot.getData()));
                    } else {
                        response.onModified(new User((HashMap<String, Object>) snapshot.getData()));
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToUsers(@NonNull Listener<User> response) {
        Pair<CollectionReference, Task<QuerySnapshot>> pair = listenToColl("users");
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getDocumentChanges().equals(snapshot.getDocumentChanges())) {
                        return;
                    }

                    for(DocumentChange dc : snapshot.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                response.onAdded(new User((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case MODIFIED:
                                response.onModified(new User((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case REMOVED:
                                response.onRemoved(new User((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                        }
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

    public static void setChat(@NonNull Chat chatOBJ, boolean clear, @Nullable Callback<Chat> response) {
        if(response == null && chatOBJ.getId() == null)
            return;
        else if(response != null && chatOBJ.getId() == null) {
            response.onFailure(new NullPointerException("Chat ID does not exist in chat object " + chatOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc("chats", chatOBJ.getId(), clear, chatOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Chat result = new Chat(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setChats(@NonNull Chat[] chatOBJ, boolean clear, @Nullable Callback<List<Chat>> response) {
        List<Chat> responses = new ArrayList<>();
        for(int i = 0; i < chatOBJ.length; i++) {
            if(response == null && chatOBJ[i].getId() == null)
                continue;
            else if(response != null && chatOBJ[i].getId() == null) {
                response.onFailure(new NullPointerException("Chat ID does not exist in chat object " + chatOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc("chats", chatOBJ[i].getId(), clear, chatOBJ[i]);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == chatOBJ.length - 1) {
                    echo.addOnSuccessListener(task -> {
                        Chat result = new Chat(task);
                        responses.add(result);
                        response.onSuccess(responses);
                    });
                } else {
                    echo.addOnSuccessListener(task -> {
                        Chat result = new Chat(task);
                        responses.add(result);
                    });
                }
            }
        }
    }

    public static void getChat(@NonNull String docID, @NonNull Callback<Chat> response) {
        getDoc("chats", docID)
                .addOnSuccessListener(task -> {
                    Chat result = new Chat(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getChats(@NonNull List<String> docID, @Nullable SortByField sortByField, @NonNull Callback<List<Chat>> response) {
        List<Chat> list = new ArrayList<>();
        if(docID.isEmpty()) {
            response.onFailure(new Exception("A non-empty docID list is required"));
            return;
        }
        getColl("chats", Filter.inArray("id", docID), sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Chat result = new Chat(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getChats(@Nullable Filter filter, @Nullable SortByField sortByField, @Nullable Integer pageNo, @NonNull Callback<List<Chat>> response) {
        List<Chat> list = new ArrayList<>();
        getColl("chats", filter, sortByField, pageNo == null ? -1 : Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Chat result = new Chat(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getAllChats(@Nullable SortByField sortByField, @NonNull Callback<List<Chat>> response) {
        List<Chat> list = new ArrayList<>();
        getColl("chats", null, sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Chat result = new Chat(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToChat(@NonNull String docID, @NonNull Listener<Chat> response) {
        Pair<DocumentReference, Task<DocumentSnapshot>> pair = listenToDoc("chats", docID);
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getData() == null || snapshot.getData() == null || task.getData().equals(snapshot.getData())) {
                        return;
                    }

                    int before = task.getData().entrySet().size(), after = snapshot.getData().entrySet().size();
                    if(before < after) {
                        response.onAdded(new Chat((HashMap<String, Object>) snapshot.getData()));
                    } else if(before > after) {
                        response.onRemoved(new Chat((HashMap<String, Object>) snapshot.getData()));
                    } else {
                        response.onModified(new Chat((HashMap<String, Object>) snapshot.getData()));
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToChats(@NonNull Listener<Chat> response) {
        Pair<CollectionReference, Task<QuerySnapshot>> pair = listenToColl("chats");
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getDocumentChanges().equals(snapshot.getDocumentChanges())) {
                        return;
                    }

                    for(DocumentChange dc : snapshot.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                response.onAdded(new Chat((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case MODIFIED:
                                response.onModified(new Chat((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case REMOVED:
                                response.onRemoved(new Chat((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                        }
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

    public static void setMessage(@NonNull Message messageOBJ, boolean clear, @Nullable Callback<Message> response) {
        if(response == null && messageOBJ.getId() == null)
            return;
        else if(response != null && messageOBJ.getId() == null) {
            response.onFailure(new NullPointerException("Message ID does not exist in message object " + messageOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc("messages", messageOBJ.getId(), clear, messageOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Message result = new Message(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setMessages(@NonNull Message[] messageOBJ, boolean clear, @Nullable Callback<List<Message>> response) {
        List<Message> responses = new ArrayList<>();
        for(int i = 0; i < messageOBJ.length; i++) {
            if(response == null && messageOBJ[i].getId() == null)
                continue;
            else if(response != null && messageOBJ[i].getId() == null) {
                response.onFailure(new NullPointerException("Message ID does not exist in message object " + messageOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc("messages", messageOBJ[i].getId(), clear, messageOBJ[i]);
            if(response != null) {
                echo.addOnFailureListener(response::onFailure);
                if(i == messageOBJ.length - 1) {
                    echo.addOnSuccessListener(task -> {
                        Message result = new Message(task);
                        responses.add(result);
                        response.onSuccess(responses);
                    });
                } else {
                    echo.addOnSuccessListener(task -> {
                        Message result = new Message(task);
                        responses.add(result);
                    });
                }
            }
        }
    }

    public static void getMessage(@NonNull String docID, @NonNull Callback<Message> response) {
        getDoc("messages", docID)
                .addOnSuccessListener(task -> {
                    Message result = new Message(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getMessages(@NonNull List<String> docID, @Nullable SortByField sortByField, @NonNull Callback<List<Message>> response) {
        List<Message> list = new ArrayList<>();
        if(docID.isEmpty()) {
            response.onFailure(new Exception("A non-empty docID list is required"));
            return;
        }
        getColl("messages", Filter.inArray("id", docID), sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Message result = new Message(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getMessages(@Nullable Filter filter, @Nullable SortByField sortByField, @Nullable Integer pageNo, @NonNull Callback<List<Message>> response) {
        List<Message> list = new ArrayList<>();
        getColl("messages", filter, sortByField, pageNo == null ? -1 : Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Message result = new Message(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getAllMessages(@Nullable SortByField sortByField, @NonNull Callback<List<Message>> response) {
        List<Message> list = new ArrayList<>();
        getColl("messages", null, sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Message result = new Message(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToMessage(@NonNull String docID, @NonNull Listener<Message> response) {
        Pair<DocumentReference, Task<DocumentSnapshot>> pair = listenToDoc("messages", docID);
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getData() == null || snapshot.getData() == null || task.getData().equals(snapshot.getData())) {
                        return;
                    }

                    int before = task.getData().entrySet().size(), after = snapshot.getData().entrySet().size();
                    if(before < after) {
                        response.onAdded(new Message((HashMap<String, Object>) snapshot.getData()));
                    } else if(before > after) {
                        response.onRemoved(new Message((HashMap<String, Object>) snapshot.getData()));
                    } else {
                        response.onModified(new Message((HashMap<String, Object>) snapshot.getData()));
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToMessages(@NonNull Listener<Message> response) {
        Pair<CollectionReference, Task<QuerySnapshot>> pair = listenToColl("messages");
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getDocumentChanges().equals(snapshot.getDocumentChanges())) {
                        return;
                    }

                    for(DocumentChange dc : snapshot.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                response.onAdded(new Message((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case MODIFIED:
                                response.onModified(new Message((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case REMOVED:
                                response.onRemoved(new Message((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                        }
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

    public static void setTransaction(@NonNull Transaction transactionOBJ, boolean clear, @Nullable Callback<Transaction> response) {
        if(response == null && transactionOBJ.getId() == null)
            return;
        else if(response != null && transactionOBJ.getId() == null) {
            response.onFailure(new NullPointerException("Transaction ID does not exist in transaction object " + transactionOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc("transactions", transactionOBJ.getId(), clear, transactionOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Transaction result = new Transaction(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setTransactions(@NonNull Transaction[] transactionOBJ, boolean clear, @Nullable Callback<List<Transaction>> response) {
        List<Transaction> responses = new ArrayList<>();
        for(int i = 0; i < transactionOBJ.length; i++) {
            if(response == null && transactionOBJ[i].getId() == null)
                continue;
            else if(response != null && transactionOBJ[i].getId() == null) {
                response.onFailure(new NullPointerException("Transaction ID does not exist in transaction object " + transactionOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc("transactions", transactionOBJ[i].getId(), clear, transactionOBJ[i]);
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

    public static void getTransaction(@NonNull String docID, @NonNull Callback<Transaction> response) {
        getDoc("transactions", docID)
                .addOnSuccessListener(task -> {
                    Transaction result = new Transaction(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTransactions(@NonNull List<String> docID, @Nullable SortByField sortByField, @NonNull Callback<List<Transaction>> response) {
        List<Transaction> list = new ArrayList<>();
        if(docID.isEmpty()) {
            response.onFailure(new Exception("A non-empty docID list is required"));
            return;
        }
        getColl("transactions", Filter.inArray("id", docID), sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Transaction result = new Transaction(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getTransactions(@Nullable Filter filter, @Nullable SortByField sortByField, @Nullable Integer pageNo, @NonNull Callback<List<Transaction>> response) {
        List<Transaction> list = new ArrayList<>();
        getColl("transactions", filter, sortByField, pageNo == null ? -1 : Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Transaction result = new Transaction(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getAllTransactions(@Nullable SortByField sortByField, @NonNull Callback<List<Transaction>> response) {
        List<Transaction> list = new ArrayList<>();
        getColl("transactions", null, sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Transaction result = new Transaction(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToTransaction(@NonNull String docID, @NonNull Listener<Transaction> response) {
        Pair<DocumentReference, Task<DocumentSnapshot>> pair = listenToDoc("transactions", docID);
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getData() == null || snapshot.getData() == null || task.getData().equals(snapshot.getData())) {
                        return;
                    }

                    int before = task.getData().entrySet().size(), after = snapshot.getData().entrySet().size();
                    if(before < after) {
                        response.onAdded(new Transaction((HashMap<String, Object>) snapshot.getData()));
                    } else if(before > after) {
                        response.onRemoved(new Transaction((HashMap<String, Object>) snapshot.getData()));
                    } else {
                        response.onModified(new Transaction((HashMap<String, Object>) snapshot.getData()));
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToTransactions(@NonNull Listener<Transaction> response) {
        Pair<CollectionReference, Task<QuerySnapshot>> pair = listenToColl("transactions");
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getDocumentChanges().equals(snapshot.getDocumentChanges())) {
                        return;
                    }

                    for(DocumentChange dc : snapshot.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                response.onAdded(new Transaction((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case MODIFIED:
                                response.onModified(new Transaction((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case REMOVED:
                                response.onRemoved(new Transaction((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                        }
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

    public static void setPost(@NonNull Post postOBJ, boolean clear, @Nullable Callback<Post> response) {
        if(response == null && postOBJ.getId() == null)
            return;
        else if(response != null && postOBJ.getId() == null) {
            response.onFailure(new NullPointerException("Post ID does not exist in post object " + postOBJ));
            return;
        }
        Task<HashMap<String, Object>> echo = setDoc("posts", postOBJ.getId(), clear, postOBJ);
        if(response != null) {
            echo.addOnSuccessListener(task -> {
                Post result = new Post(task);
                response.onSuccess(result);
            });
            echo.addOnFailureListener(response::onFailure);
        }
    }

    public static void setPosts(@NonNull Post[] postOBJ, boolean clear, @Nullable Callback<List<Post>> response) {
        List<Post> responses = new ArrayList<>();
        for(int i = 0; i < postOBJ.length; i++) {
            if(response == null && postOBJ[i].getId() == null)
                continue;
            else if(response != null && postOBJ[i].getId() == null) {
                response.onFailure(new NullPointerException("Post ID does not exist in post object " + postOBJ[i]));
                continue;
            }
            Task<HashMap<String, Object>> echo = setDoc("posts", postOBJ[i].getId(), clear, postOBJ[i]);
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

    public static void getPost(@NonNull String docID, @NonNull Callback<Post> response) {
        getDoc("posts", docID)
                .addOnSuccessListener(task -> {
                    Post result = new Post(task);
                    response.onSuccess(result);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getPosts(@NonNull List<String> docID, @Nullable SortByField sortByField, @NonNull Callback<List<Post>> response) {
        List<Post> list = new ArrayList<>();
        if(docID.isEmpty()) {
            response.onFailure(new Exception("A non-empty docID list is required"));
            return;
        }
        getColl("posts", Filter.inArray("id", docID), sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Post result = new Post(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getPosts(@Nullable Filter filter, @Nullable SortByField sortByField, @Nullable Integer pageNo, @NonNull Callback<List<Post>> response) {
        List<Post> list = new ArrayList<>();
        getColl("posts", filter, sortByField, pageNo == null ? -1 : Math.max(pageNo, 0))
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Post result = new Post(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void getAllPosts(@Nullable SortByField sortByField, @NonNull Callback<List<Post>> response) {
        List<Post> list = new ArrayList<>();
        getColl("posts", null, sortByField, -1)
                .addOnSuccessListener(task -> {
                    for (HashMap<String, Object> hash : task) {
                        Post result = new Post(hash);
                        list.add(result);
                    }
                    response.onSuccess(list);
                })
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToPost(@NonNull String docID, @NonNull Listener<Post> response) {
        Pair<DocumentReference, Task<DocumentSnapshot>> pair = listenToDoc("posts", docID);
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getData() == null || snapshot.getData() == null || task.getData().equals(snapshot.getData())) {
                        return;
                    }

                    int before = task.getData().entrySet().size(), after = snapshot.getData().entrySet().size();
                    if(before < after) {
                        response.onAdded(new Post((HashMap<String, Object>) snapshot.getData()));
                    } else if(before > after) {
                        response.onRemoved(new Post((HashMap<String, Object>) snapshot.getData()));
                    } else {
                        response.onModified(new Post((HashMap<String, Object>) snapshot.getData()));
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

    public static void listenToPosts(@NonNull Listener<Post> response) {
        Pair<CollectionReference, Task<QuerySnapshot>> pair = listenToColl("posts");
        pair.second
                .addOnSuccessListener(task -> pair.first.addSnapshotListener((snapshot, error) -> {
                    if(snapshot == null || task.getDocumentChanges().equals(snapshot.getDocumentChanges())) {
                        return;
                    }

                    for(DocumentChange dc : snapshot.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                response.onAdded(new Post((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case MODIFIED:
                                response.onModified(new Post((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                            case REMOVED:
                                response.onRemoved(new Post((HashMap<String, Object>) dc.getDocument().getData()));
                                break;
                        }
                    }
                }))
                .addOnFailureListener(response::onFailure);
    }

}