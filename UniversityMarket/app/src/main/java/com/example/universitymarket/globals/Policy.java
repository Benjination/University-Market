package com.example.universitymarket.globals;

public interface Policy {
    //microsoft graph delegated functionality
    String[] scopes = {
            "https://graph.microsoft.com/User.Read",
            "https://graph.microsoft.com/profile",
            "https://graph.microsoft.com/openid",
            "https://graph.microsoft.com/offline_access",
            "https://graph.microsoft.com/email",
            "https://graph.microsoft.com/Domain.Read.All",
            "https://graph.microsoft.com/CustomTags.ReadWrite.All",
            "https://graph.microsoft.com/AppRoleAssignment.ReadWrite.All",
            "https://graph.microsoft.com/Application.ReadWrite.All"
    };

    int max_posts_per_user = 20;
    int max_post_lifetime_days = 30;
    int max_docs_loaded = 30;
    int max_genres_per_item = 3;
    int max_seconds_before_timeout = 5;
    int max_images_per_post = 5;
    int max_descriptors_per_genre = 10;
    int max_chars_per_description = 300;
}
