package com.example.universitymarket.globals;

public interface Policy {
    int max_posts_per_user = 20;
    int max_post_lifetime_days = 30;
    int max_docs_loaded = 30;
    int max_seconds_before_timeout = 5;
    int max_images_per_post = 5;
    int max_descriptors_per_genre = 10;
    int max_chars_per_description = 300;
    int max_stars = 5;

    String[] genres = {
            "Textbooks",
            "Technology",
            "Supplies",
            "Miscellaneous"
    };

    String[] prices = {
            "Free",
            "< $5.00",
            "< $10.00",
            "< $20.00",
            "< $50.00",
            "> $50.00"
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

}