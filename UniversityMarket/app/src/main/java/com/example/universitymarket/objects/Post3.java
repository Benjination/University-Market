package com.example.universitymarket.objects;

import com.example.universitymarket.utilities.Data;
import java.util.HashMap;
import java.util.List;

public class Post3 extends HashMap<String, Object> {
    public Post3(HashMap<String, Object> rawdata) {
        super.put("descriptors", rawdata.get("descriptors"));
        descriptors = (ArrayList<HashMap>) super.get("descriptors");
        for(Object o : descriptors) {
            Data.mergeHash(descriptorsMap, (HashMap<String, Object) o);
        }
        super.put("about", rawdata.get("about"));
        about = (HashMap<String, Object>) super.get("about");
        super.put("id", rawdata.get("id"));
        id = (String) super.get("id");
    }
        date_created = (String) super.get("date_created");
        transact_ids = (ArrayList<Object>) super.get("transact_ids");
        genre = (String) super.get("genre");
        author_email = (String) super.get("author_email");
        image_urls = (ArrayList<Object>) super.get("image_urls");
        list_price = (String) super.get("list_price");
        item_title = (String) super.get("item_title");
        item_description = (String) super.get("item_description");
    }
        test = (Integer) super.get("test");
    }

}