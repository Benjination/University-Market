package com.example.universitymarket.globals;

public interface Policy {
    int max_posts_per_user = 20;
    int max_post_lifetime_days = 30;
    int max_docs_loaded = 30;
    int max_genres_per_item = 3;
    int max_seconds_before_timeout = 5;
    int max_images_per_post = 5;
    int max_descriptors_per_genre = 10;
    int max_chars_per_description = 300;

    String[] genres = {
            "Textbooks",
            "Technology",
            "Supplies",
            "Miscellaneous"
    };

/**
 * <b>
 * ANY MODIFICATIONS BELOW WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * DO NOT REMOVE THIS BLOCK 
*/
    String[] json_filenames = { 
            "test_skeleton.json",
            "transaction_skeleton.json",
            "user_skeleton_cached.json",
            "post_skeleton.json"
    };

}