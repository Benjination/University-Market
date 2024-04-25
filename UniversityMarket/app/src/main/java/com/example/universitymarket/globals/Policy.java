package com.example.universitymarket.globals;

import android.widget.RadioButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface Policy {
    int max_posts_per_user = 20;
    int max_docs_loaded = 30;
    int max_seconds_before_timeout = 5;
    int max_images_per_post = 5;
    int max_descriptors_per_genre = 10;
    int max_chars_per_description = 300;
    int max_stars = 5;
    int post_lifetime = 30;


    List<String> invalid_image = Collections.singletonList("https://firebasestorage.googleapis.com/v0/b/university-market-e4aa7.appspot.com/o/invalid.png?alt=media&token=4034f579-5c6f-4ac9-a38b-29e3a2b005bb");

    String[] genres = {
            "Textbooks",
            "Technology",
            "Supplies",
            "Miscellaneous"
    };

    String[] genre_filters = {
            "Textbooks",
            "Technology",
            "Supplies",
            "Miscellaneous"
    };

    String[] price_filters = {
            "Low to High",
            "High to Low"
    };

    String[] upload_date_filters = {
            "Newest to Oldest",
            "Oldest to Newest"
    };

    String[] prices = {
            "  $0.00",
            "< $10.00",
            "< $20.00",
            "> $20.00"
    };

    String[] expiring = {
            "Today",
            "Soonest First",
            "Latest First"
    };

/**
 * <b>
 * ANY MODIFICATIONS BELOW WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * DO NOT REMOVE THIS BLOCK 
*/
    String[] json_filenames = { 
            "transaction_skeleton.json",
            "user_skeleton_cached.json",
            "chat_skeleton.json",
            "post_skeleton.json",
            "message_skeleton.json"
    };

    List<String> collection_names = Arrays.stream(json_filenames).map(string -> string.split("_")[0] + "s").collect(Collectors.toList());
}