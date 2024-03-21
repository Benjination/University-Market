package com.example.universitymarket.fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

public class PostFragment extends Fragment implements View.OnClickListener {

    private View root;
    private LayoutInflater inflater;
    private Button submit;
    private ImageButton imageupload;
    private EditText title, price, description;
    private RadioGroup genres;
    private LinearLayout carousel;
    private ProgressBar loadbar;
    private View loadscreen;
    private TextView genrelabel, imagelabel;
    private TaskCompletionSource<String> load;
    private Thread uploadImages;
    private final FragmentManager fm;
    private final HashMap<Integer, Spanned> requiredText = new HashMap<>();

    private ArrayList<String> imageURLsToBeUploaded = new ArrayList<>();
    private ArrayList<String> imageURLs = new ArrayList<>();
    private final Post post = new Post();

    public PostFragment(FragmentManager fm) {
        this.fm = fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        root = inflater.inflate(R.layout.fragment_post, container, false);
        configure(root);
        return root;
    }

    private void configure(View v) {
        Data.clearImageCache(requireActivity());
        submit = v.findViewById(R.id.post_submit_button);
        imageupload = v.findViewById(R.id.post_imageupload_button);
        title = v.findViewById(R.id.post_title_field);
        price = v.findViewById(R.id.post_price_field);
        description = v.findViewById(R.id.post_description_field);
        genres = v.findViewById(R.id.post_genre_group);
        carousel = v.findViewById(R.id.post_images);
        loadbar = v.findViewById(R.id.post_load_animation);
        loadscreen = v.findViewById(R.id.post_load_screen);
        genrelabel = v.findViewById(R.id.post_genre_label);
        imagelabel = v.findViewById(R.id.post_image_label);

        requiredFields(title, price, description, genrelabel, imagelabel);

        for(String genre : Policy.genres) {
            RadioButton newGenre = new RadioButton(requireContext());
            newGenre.setText(genre);
            genres.addView(newGenre);
        }

        submit.setOnClickListener(this);
        imageupload.setOnClickListener(this);
    }

    private void requiredFields(TextView... views) {
        for(TextView v : views) {
            String base = v.getHint() != null ? v.getHint().toString() : v.getText().toString();
            Spanned hint = Html.fromHtml(
                   "<string style=\"color:grey;\">" + base + " <span style=\"color:red;\">*</span></string>",
                    Html.FROM_HTML_MODE_LEGACY
            );
            requiredText.put(v.getId(), hint);
        }
        setRequiredText(views);
    }

    private void setRequiredText(TextView... views) {
        imagelabel.setVisibility(View.VISIBLE);
        for(TextView v : views) {
            if(v.getHint() != null)
                v.setHint(requiredText.get(v.getId()));
            else
                v.setText(requiredText.get(v.getId()));
        }
    }

    private void loadPage(Task<String> task) {
        submit.setEnabled(false);
        loadscreen.setEnabled(true);
        loadscreen.setVisibility(View.VISIBLE);
        loadbar.setVisibility(View.VISIBLE);

        task.addOnCompleteListener(res -> {
            String val = res.getResult();
            if(val.equals("post")) {
                resetPage();
            }
            loadscreen.setVisibility(View.INVISIBLE);
            loadbar.setVisibility(View.INVISIBLE);
            loadscreen.setEnabled(false);
            submit.setEnabled(true);
        });
    }

    private void resetPage() {
        //is called after image-less post object was successfully
        //created and every image was able to be uploaded
        ActiveUser.post_ids.add(post.getId());
        Network.setUser(requireActivity(), Data.activeUserToPOJO(), false, new Callback<User>() {
            @Override
            public void onSuccess(User ignored) {
                //setActiveUser caches ActiveUser
                Data.setActiveUser(requireActivity(), Data.activeUserToPOJO());
                Network.setPost(requireActivity(), post, false, new Callback<Post>() {
                    @Override
                    public void onSuccess(Post result) {
                        Toast.makeText(
                                getContext(),
                                "Posted to marketplace",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Toast.makeText(
                                getContext(),
                                "Could not finish uploading: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception error) {
                Network.setPost(requireActivity(), post, true, null);
                Toast.makeText(
                        getContext(),
                        "Could not finish uploading: " + error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        title.getText().clear();
        price.getText().clear();
        description.getText().clear();
        title.clearFocus();
        price.clearFocus();
        description.clearFocus();
        imageURLsToBeUploaded = new ArrayList<>();
        imageURLs = new ArrayList<>();
        ((RadioButton) root.findViewById(genres.getCheckedRadioButtonId())).setChecked(false);
        ArrayList<View> remove = new ArrayList<>();
        for(int i = 0; i < carousel.getChildCount(); i++) {
            View v = carousel.getChildAt(i);
            if(!(v instanceof ImageButton))
                remove.add(v);
        }
        for(View v : remove)
            carousel.removeView(v);

        setRequiredText(title, price, description);
        Data.clearImageCache(requireActivity());
    }

    private void retrievePhoto() {
        fm.setFragmentResult("requestGallery", new Bundle());
        load = new TaskCompletionSource<>();
        loadPage(load.getTask());
        fm
                .setFragmentResultListener(
                        "retrieveImage",
                        this,
                        (requestKey, result) -> {
                            String buffer = result.getString(null);

                            if(buffer.contains("uriRetreival")) {
                                Toast.makeText(
                                        getContext(),
                                        buffer.split("~")[1],
                                        Toast.LENGTH_SHORT
                                ).show();
                            } else {
                                imageURLsToBeUploaded.add(buffer);
                                addToCarousel(Uri.parse(buffer));
                                imagelabel.setVisibility(View.INVISIBLE);
                                fm.clearFragmentResultListener("retrieveImage");
                            }
                            load.setResult("image");
                        }
                );
    }

    private void addToCarousel(Uri uri) {
        ImageView newImage = new ImageView(requireContext());
        Picasso.get().load(uri).into(newImage, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                carousel.addView(newImage);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(
                        getContext(),
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void uploadPhoto(Uri uri) {
        Network.uploadImage(uri, "PostImages", "pic", new Callback<Uri>() {
                    @Override
                    public void onSuccess(Uri result) {
                        imageURLs.add(result.toString());
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Toast.makeText(
                                getContext(),
                                error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        if(ID == imageupload.getId()) {
            if(imageURLsToBeUploaded.size() < Policy.max_images_per_post)
                retrievePhoto();
        }
        if(ID == submit.getId()) {
            RadioButton selected = genres.findViewById(genres.getCheckedRadioButtonId());
            if(Data.isAnyObjectNull(selected, price, title, description) ||
                    Data.isAnyStringEmpty(
                            price.getText().toString(),
                            title.getText().toString(),
                            description.getText().toString()
                    ) ||
                    imageURLsToBeUploaded.size() == 0
            ) {
                Toast.makeText(
                        getContext(),
                        "Please fill out the required fields",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Log.e("price", price.getText().toString().length() + "");
            Log.e("title", title.getText().toString().length() + "");
            Log.e("desc", description.getText().toString().length() + "");
            post.setId(Data.generateID("pst"));
            post.setAbout(
                    Data.getDate(),
                    new ArrayList<>(),
                    selected.getText().toString(),
                    ActiveUser.email,
                    imageURLs,
                    price.getText().toString(),
                    title.getText().toString(),
                    description.getText().toString()
            );

            Network.setPost(requireActivity(), post, false, new Callback<Post>() {
                @Override
                public void onSuccess(Post result) {
                    for(String s : imageURLsToBeUploaded) {
                        uploadPhoto(Uri.parse(s));
                    }

                    load = new TaskCompletionSource<>();
                    loadPage(load.getTask());
                    uploadImages = new Thread(() -> {
                        while(imageURLs.size() != imageURLsToBeUploaded.size());
                        load.setResult("post");
                    });
                    uploadImages.start();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!load.getTask().isComplete()) {
                            load.setException(new TimeoutException());
                            uploadImages.interrupt();
                        }
                    }, Policy.max_seconds_before_timeout * 1000);
                }

                @Override
                public void onFailure(Exception error) {
                    Toast.makeText(
                            getContext(),
                            error.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }
}