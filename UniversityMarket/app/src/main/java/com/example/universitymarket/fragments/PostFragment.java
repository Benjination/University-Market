package com.example.universitymarket.fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.universitymarket.R;
import com.example.universitymarket.adapters.CarouselAdapter;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.viewmodels.myPostsProfileViewModel;
import com.example.universitymarket.viewmodels.myPostsViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class PostFragment extends Fragment implements View.OnClickListener {

    private View root;
    private myPostsViewModel mypostsViewModel;
    private myPostsProfileViewModel mypostsProfileViewModel;
    private Button submit, addmore;
    private FloatingActionButton removeImage;
    private ImageButton imageupload;
    private EditText title, price, description;
    private RadioGroup genres;
    private TextView genrelabel, imagelabel;
    private TextInputLayout titleLayout, priceLayout, descriptionLayout;
    private TaskCompletionSource<String> load;
    private Thread uploadImages;
    private FragmentManager fm;
    private ViewPager2 imagepager;
    private LinearLayout indicatorContainer, carousel;
    private int numIndicators = 0, position = 0;
    private final Pattern priceFormat = Pattern.compile("^\\d*(\\.\\d\\d)?$");
    private final HashMap<Integer, Spanned> requiredText = new HashMap<>();
    private ArrayList<String> imageURLsToBeUploaded = new ArrayList<>();
    private ArrayList<String> imageURLs = new ArrayList<>();
    private final Post post = new Post();
    private final Bundle dashMessage = new Bundle();

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
        root = inflater.inflate(R.layout.fragment_post, container, false);
        mypostsViewModel = new ViewModelProvider(requireActivity()).get(myPostsViewModel.class);
        mypostsProfileViewModel = new ViewModelProvider(requireActivity()).get(myPostsProfileViewModel.class);
        configure(root);
        return root;
    }

    private void configure(View v) {
        Data.clearImageCache(requireActivity());
        submit = v.findViewById(R.id.post_submit_button);
        addmore = v.findViewById(R.id.post_image_addmore_button);
        removeImage = v.findViewById(R.id.post_imageremove_button);
        imageupload = v.findViewById(R.id.post_imageupload_button);
        imagepager = v.findViewById(R.id.post_image_pager);
        indicatorContainer = v.findViewById(R.id.post_indicator_container);
        carousel = v.findViewById(R.id.post_image_carousel);
        title = v.findViewById(R.id.post_title_field);
        price = v.findViewById(R.id.post_price_field);
        description = v.findViewById(R.id.post_description_field);
        genres = v.findViewById(R.id.post_genre_group);
        genrelabel = v.findViewById(R.id.post_genre_label);
        imagelabel = v.findViewById(R.id.post_image_label);
        titleLayout = v.findViewById(R.id.post_title_layout);
        priceLayout = v.findViewById(R.id.post_price_layout);
        descriptionLayout = v.findViewById(R.id.post_description_layout);

        requiredFields(titleLayout, priceLayout, descriptionLayout, genrelabel, imagelabel);
        imagepager.setAdapter(new CarouselAdapter());
        imagepager.setUserInputEnabled(true);

        for(String genre : Policy.genres) {
            RadioButton newGenre = new RadioButton(requireContext());
            newGenre.setText(genre);
            genres.addView(newGenre);
        }

        submit.setOnClickListener(this);
        imageupload.setOnClickListener(this);
        addmore.setOnClickListener(this);
        removeImage.setOnClickListener(this);

        fm
                .setFragmentResultListener(
                        "retrieveImages",
                        this,
                        (requestKey, result) -> {
                            String error = result.getString("error");
                            if(error != null) {
                                Toast.makeText(
                                        getContext(),
                                        error,
                                        Toast.LENGTH_SHORT
                                ).show();
                            } else {
                                if(imageupload.getVisibility() == View.VISIBLE) {
                                    imageupload.setVisibility(View.INVISIBLE);
                                    imagelabel.setVisibility(View.INVISIBLE);
                                    addmore.setVisibility(View.VISIBLE);
                                    removeImage.setVisibility(View.VISIBLE);
                                }
                                for (String uri : result.getStringArrayList("uris")) {
                                    numIndicators += 1;
                                    ImageView dot = new ImageView(v.getContext());
                                    dot.setImageResource(R.drawable.dot_icon);
                                    dot.setScaleX(-Data.convertComplexToPixel(requireActivity(), 3));
                                    dot.setScaleY(-Data.convertComplexToPixel(requireActivity(), 3));
                                    dot.setPadding(12,0,12,0);
                                    indicatorContainer.addView(dot);

                                    imageURLsToBeUploaded.add(uri);
                                    addToCarousel(Uri.parse(uri));
                                }
                            }
                            if(Policy.max_images_per_post == imageURLsToBeUploaded.size()) {
                                addmore.setVisibility(View.INVISIBLE);
                            }
                            if (!load.getTask().isComplete())
                                load.setResult("image");
                        }
                );
        fm
                .setFragmentResultListener(
                        "retrieveImage",
                        this,
                        (requestKey, result) -> {
                            String error = result.getString("error");
                            if(error != null) {
                                Toast.makeText(
                                        getContext(),
                                        error,
                                        Toast.LENGTH_SHORT
                                ).show();
                            } else {
                                numIndicators += 1;
                                String uri = result.getString("uri");
                                imageURLsToBeUploaded.add(uri);
                                addToCarousel(Uri.parse(uri));
                                imagelabel.setVisibility(View.INVISIBLE);
                                imageupload.setVisibility(View.INVISIBLE);
                                removeImage.setVisibility(View.VISIBLE);
                            }
                            if(Policy.max_images_per_post == imageURLsToBeUploaded.size()) {
                                addmore.setVisibility(View.INVISIBLE);
                            }
                            if (!load.getTask().isComplete())
                                load.setResult("image");
                        }
                );
    }

    private void requiredFields(View... views) {
        for(View v : views) {
            String base;
            if(v.getClass() == TextInputLayout.class) {
                base = ((TextInputLayout) v).getHint() != null ? ((TextInputLayout) v).getHint().toString() : Objects.requireNonNull(((TextInputLayout) v).getEditText()).getText().toString();
            } else {
                base = ((TextView) v).getHint() != null ? ((TextView) v).getHint().toString() : ((TextView) v).getText().toString();
            }

            Spanned hint = Html.fromHtml(
                    "<string style=\"color:grey;\">" + base + " <span style=\"color:red;\">*</span></string>",
                    Html.FROM_HTML_MODE_LEGACY
            );
            requiredText.put(v.getId(), hint);
        }
        setRequiredText(views);
    }

    private void setRequiredText(View... views) {
        imagelabel.setVisibility(View.VISIBLE);
        for(View v : views) {
            if(v.getClass() == TextInputLayout.class) {
                if (((TextInputLayout) v).getHint() != null)
                    ((TextInputLayout) v).setHint(requiredText.get(v.getId()));
                else
                    Objects.requireNonNull(((TextInputLayout) v).getEditText()).setText(requiredText.get(v.getId()));
            } else {
                if (((TextView) v).getHint() != null)
                    ((TextView) v).setHint(requiredText.get(v.getId()));
                else
                    ((TextView) v).setText(requiredText.get(v.getId()));
            }
        }
    }

    private void loadPage(Task<String> task) {
        dashMessage.putBoolean("isLoading", true);
        fm.setFragmentResult("setLoading", dashMessage);

        task
                .addOnSuccessListener(res -> {
                    if (res.equals("post"))
                        resetPage();
                    dashMessage.putBoolean("isLoading", false);
                    fm.setFragmentResult("setLoading", dashMessage);
                })
                .addOnFailureListener(error -> {
                    Log.e("loadPage", error.getMessage());
                    dashMessage.putBoolean("isLoading", false);
                    fm.setFragmentResult("setLoading", dashMessage);
                });
    }

    private void resetPage() {
        //is called after image-less post object was successfully
        //created and every image was able to be uploaded
        post.setImageUrls(imageURLs);
        ActiveUser.post_ids.add(post.getId());
        Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
            @Override
            public void onSuccess(User ignored) {
                //setActiveUser caches ActiveUser
                Data.setActiveUser(requireActivity(), Data.activeUserToPOJO());
                Network.setPost(post, false, new Callback<Post>() {
                    @Override
                    public void onSuccess(Post result) {
                        Toast.makeText(
                                getContext(),
                                "Posted to marketplace",
                                Toast.LENGTH_LONG
                        ).show();

                        //update LiveData for myPosts
                        mypostsViewModel.addMyPost();
                        mypostsProfileViewModel.addUserPost(ActiveUser.email);

                        title.getText().clear();
                        price.getText().clear();
                        description.getText().clear();
                        title.clearFocus();
                        price.clearFocus();
                        description.clearFocus();
                        imageURLsToBeUploaded.clear();
                        imageURLs.clear();
                        ((RadioButton) root.findViewById(genres.getCheckedRadioButtonId())).setChecked(false);
                        carousel.removeAllViews();
                        indicatorContainer.removeAllViews();
                        addmore.setVisibility(View.INVISIBLE);
                        removeImage.setVisibility(View.INVISIBLE);
                        imageupload.setVisibility(View.VISIBLE);
                        imagelabel.setVisibility(View.VISIBLE);

                        setRequiredText(title, price, description);
                        Data.clearImageCache(requireActivity());
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
                Network.setPost(post, true, null);
                Toast.makeText(
                        getContext(),
                        "Could not finish uploading: " + error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void retrievePhoto() {
        dashMessage.putInt("numPictures", imageURLsToBeUploaded.size());
        fm.setFragmentResult("requestGallery", dashMessage);
        load = new TaskCompletionSource<>();
        loadPage(load.getTask());
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
        if(ID == removeImage.getId()) {
            carousel.removeAllViews();
            imageURLsToBeUploaded.clear();
            position = 0;
            numIndicators = 0;
            indicatorContainer.removeAllViews();
            addmore.setVisibility(View.INVISIBLE);
            removeImage.setVisibility(View.INVISIBLE);
            imageupload.setVisibility(View.VISIBLE);
            imagelabel.setVisibility(View.VISIBLE);
        }
        if(ID == addmore.getId()) {
            if(imageURLsToBeUploaded.size() < Policy.max_images_per_post)
                retrievePhoto();
        }
        if(ID == submit.getId()) {
            String priceText = price.getText().toString();
            if(!priceFormat.matcher(priceText).matches()) {
                Toast.makeText(
                        getContext(),
                        "Please specify the price as an integer or decimal to the hundredth's place",
                        Toast.LENGTH_LONG
                ).show();
                return;
            } else if(!priceText.contains(".")){
                priceText = priceText + ".00";
            }

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

            post.setId(Data.generateID("pst"));
            post.setAbout(
                    1,
                    new Date().toString(),
                    new ArrayList<>(),
                    selected.getText().toString(),
                    ActiveUser.email,
                    imageURLs,
                    Float.parseFloat(priceText),
                    new ArrayList<>(),
                    title.getText().toString(),
                    description.getText().toString()
            );

            Network.setPost(post, false, new Callback<Post>() {
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