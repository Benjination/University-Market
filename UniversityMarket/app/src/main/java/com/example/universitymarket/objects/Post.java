package com.example.universitymarket.objects;

import androidx.annotation.Nullable;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.utilities.Network;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Post extends HashMap<String, Object> {
    @Nullable private String id = null;
    @Nullable private HashMap<String, Object> about = null;
    @Nullable private List<String> genres = null;
    @Nullable private List<HashMap<String, Object>> descriptors = null;
    private List<String> image_urls = new ArrayList<>();
    private HashMap<String, Object> descriptor_map = new HashMap<>();

    public void lateConstructor() {
        if(id == null) {
            id = (String) super.get("id");
            about = (HashMap<String, Object>) super.get("info");
            genres = (List<String>) super.get("genres");
            descriptors = (List<HashMap<String, Object>>) super.get("descriptors");
            initImageUrls();
            initDescriptors();
        }
    }

    private void initImageUrls() {
        List<?> indefinite = (ArrayList<?>) Arrays.asList(((HashMap<String, ?>) about.get("image_urls")).keySet().toArray());
        for(int i=0; i<Policy.max_images_per_post && i<indefinite.size(); i++) {
            image_urls.add((String) indefinite.get(i));
        }
    }

    private void initDescriptors() {
        for(int i=0; i<Policy.max_genres_per_item * Policy.max_descriptors_per_genre && i<descriptors.size(); i++) {
            descriptors.get(i).forEach((key, value) -> descriptor_map.merge(key, value, (oldValue, newValue) -> {
                if (oldValue.toString().equals(newValue)) { return oldValue; }
                else { return newValue; }
            }));
        }
    }

    public String getId() { return id; }

    public String getTitle() { return (String) about.get("item_title"); }

    public String getDescription() { return (String) about.get("item_description"); }

    public String getAuthorEmail() { return (String) about.get("author_email"); }

    public List<String> getAuthorName() {
        User author = Network.getUser(getAuthorEmail());
        assert author != null;
        List<String> names = new ArrayList<>(author.getName());
        return names;
    }

    public float getPrice() { return Float.parseFloat((String) about.get("list_price")); }

    public List<String> getImages() { return image_urls; }

    public String getDateCreated() { return (String) about.get("date_created"); }

    public List<String> getGenres() { return genres; }

    public HashMap<String, Object> getDescriptors() { return descriptor_map; }

    public List<HashMap<String, Object>> getSeparatedDescriptors() { return descriptors; }
}
