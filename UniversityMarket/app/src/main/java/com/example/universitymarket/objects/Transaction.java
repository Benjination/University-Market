package com.example.universitymarket.objects;

import androidx.annotation.Discouraged;
import java.util.HashMap;
import java.util.ArrayList;
import com.example.universitymarket.utilities.Data;

/**
 * <b>
 * ANY MODIFICATIONS WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * File based on 'transaction_skeleton.json' located at <a href="file:///Users/johnnyboi/University-Market/UniversityMarket/app/src/main/res/raw/">main/res/raw/</a>
*/
public class Transaction extends HashMap<String, Object> {
    private HashMap<String, Object> record = new HashMap<>();
    private ArrayList<HashMap> payments = new ArrayList<>();
    private HashMap<String, Object> about = new HashMap<>();
    private String id = null;
    private ArrayList<HashMap> descriptors = new ArrayList<>();
    private String post_id = null;
    private String genre = null;
    private Boolean dead_post = null;
    private String description = null;
    private String serial_number = null;
    private ArrayList<String> image_contexts = new ArrayList<>();
    private String title = null;
    private String chat_id = null;
    private String buyer_email = null;
    private String closing_date = null;
    private String seller_email = null;
    private Float final_amount = null;
    private String status = null;
    private HashMap<String, Object> paymentsMap = new HashMap<>();
    private HashMap<String, Object> descriptorsMap = new HashMap<>();

    @Discouraged(message = "Unless you are initializing from another skeleton POJO, do not use this constructor")
    public Transaction(HashMap<String, Object> rawdata) {
        ArrayList<String> rawKeys = new ArrayList<>(rawdata != null ? rawdata.keySet() : new ArrayList<>());
        super.put("record", rawKeys.contains("record") ? rawdata.get("record") : null);
        super.put("payments", rawKeys.contains("payments") ? rawdata.get("payments") : null);
        super.put("about", rawKeys.contains("about") ? rawdata.get("about") : null);
        super.put("id", rawKeys.contains("id") ? rawdata.get("id") : null);
        ArrayList<String> superKeys = new ArrayList<>(super.keySet());
        record = superKeys.contains("record") ? (HashMap<String, Object>) super.get("record") : new HashMap<>();
        payments = superKeys.contains("payments") ? (ArrayList<HashMap>) super.get("payments") : new ArrayList<>();
        about = superKeys.contains("about") ? (HashMap<String, Object>) super.get("about") : new HashMap<>();
        id = superKeys.contains("id") ? (String) super.get("id") : null;
        ArrayList<String> recordKeys = new ArrayList<>(record != null ? record.keySet() : new ArrayList<>());
        descriptors = recordKeys.contains("descriptors") ? (ArrayList<HashMap>) record.get("descriptors") : new ArrayList<>();
        post_id = recordKeys.contains("post_id") ? (String) record.get("post_id") : null;
        genre = recordKeys.contains("genre") ? (String) record.get("genre") : null;
        dead_post = recordKeys.contains("dead_post") ? (Boolean) record.get("dead_post") : null;
        description = recordKeys.contains("description") ? (String) record.get("description") : null;
        serial_number = recordKeys.contains("serial_number") ? (String) record.get("serial_number") : null;
        image_contexts = recordKeys.contains("image_contexts") ? (ArrayList<String>) record.get("image_contexts") : new ArrayList<>();
        title = recordKeys.contains("title") ? (String) record.get("title") : null;
        chat_id = recordKeys.contains("chat_id") ? (String) record.get("chat_id") : null;
        ArrayList<String> aboutKeys = new ArrayList<>(about != null ? about.keySet() : new ArrayList<>());
        buyer_email = aboutKeys.contains("buyer_email") ? (String) about.get("buyer_email") : null;
        closing_date = aboutKeys.contains("closing_date") ? (String) about.get("closing_date") : null;
        seller_email = aboutKeys.contains("seller_email") ? (String) about.get("seller_email") : null;
        final_amount = aboutKeys.contains("final_amount") ? ((Number) about.get("final_amount")).floatValue() : null;
        status = aboutKeys.contains("status") ? (String) about.get("status") : null;

        for(int i = 0; payments != null && i < payments.size(); i++) {
            Data.mergeHash(paymentsMap, (HashMap<String, Object>) payments.get(i));
        }

        for(int i = 0; descriptors != null && i < descriptors.size(); i++) {
            Data.mergeHash(descriptorsMap, (HashMap<String, Object>) descriptors.get(i));
        }
        formatSuper();
    }

    public Transaction(ArrayList<HashMap> descriptors, String post_id, String genre, Boolean dead_post, String description, String serial_number, ArrayList<String> image_contexts, String title, String chat_id, ArrayList<HashMap> payments, String buyer_email, String closing_date, String seller_email, Float final_amount, String status, String id) {
        setRecord(descriptors, post_id, genre, dead_post, description, serial_number, image_contexts, title, chat_id);
        setAbout(buyer_email, closing_date, seller_email, final_amount, status);
        this.payments = payments;
        this.id = id;
        formatSuper();
    }

    public Transaction() {
        formatSuper();
    }

    public HashMap<String, Object> getRecord() { return record; }

    public ArrayList<HashMap> getPayments() { return payments; }

    public HashMap<String, Object> getAbout() { return about; }

    public String getId() { return id; }

    public ArrayList<HashMap> getDescriptors() { return descriptors; }

    public String getPostId() { return post_id; }

    public String getGenre() { return genre; }

    public Boolean getDeadPost() { return dead_post; }

    public String getDescription() { return description; }

    public String getSerialNumber() { return serial_number; }

    public ArrayList<String> getImageContexts() { return image_contexts; }

    public String getTitle() { return title; }

    public String getChatId() { return chat_id; }

    public String getBuyerEmail() { return buyer_email; }

    public String getClosingDate() { return closing_date; }

    public String getSellerEmail() { return seller_email; }

    public Float getFinalAmount() { return final_amount; }

    public String getStatus() { return status; }

    public HashMap<String, Object> getPaymentsMap() { return paymentsMap; }

    public HashMap<String, Object> getDescriptorsMap() { return descriptorsMap; }

    public void setRecord(ArrayList<HashMap> descriptors, String post_id, String genre, Boolean dead_post, String description, String serial_number, ArrayList<String> image_contexts, String title, String chat_id) {
        this.descriptors = descriptors;
        this.post_id = post_id;
        this.genre = genre;
        this.dead_post = dead_post;
        this.description = description;
        this.serial_number = serial_number;
        this.image_contexts = image_contexts;
        this.title = title;
        this.chat_id = chat_id;
        formatSuper();
    }

    public void setPayments(ArrayList<HashMap> payments) {
        this.payments = payments;
        formatSuper();
    }

    public void setAbout(String buyer_email, String closing_date, String seller_email, Float final_amount, String status) {
        this.buyer_email = buyer_email;
        this.closing_date = closing_date;
        this.seller_email = seller_email;
        this.final_amount = final_amount;
        this.status = status;
        formatSuper();
    }

    public void setId(String id) {
        this.id = id;
        formatSuper();
    }

    public void setDescriptors(ArrayList<HashMap> descriptors) {
        this.descriptors = descriptors;
        formatSuper();
    }

    public void setPostId(String post_id) {
        this.post_id = post_id;
        formatSuper();
    }

    public void setGenre(String genre) {
        this.genre = genre;
        formatSuper();
    }

    public void setDeadPost(Boolean dead_post) {
        this.dead_post = dead_post;
        formatSuper();
    }

    public void setDescription(String description) {
        this.description = description;
        formatSuper();
    }

    public void setSerialNumber(String serial_number) {
        this.serial_number = serial_number;
        formatSuper();
    }

    public void setImageContexts(ArrayList<String> image_contexts) {
        this.image_contexts = image_contexts;
        formatSuper();
    }

    public void setTitle(String title) {
        this.title = title;
        formatSuper();
    }

    public void setChatId(String chat_id) {
        this.chat_id = chat_id;
        formatSuper();
    }

    public void setBuyerEmail(String buyer_email) {
        this.buyer_email = buyer_email;
        formatSuper();
    }

    public void setClosingDate(String closing_date) {
        this.closing_date = closing_date;
        formatSuper();
    }

    public void setSellerEmail(String seller_email) {
        this.seller_email = seller_email;
        formatSuper();
    }

    public void setFinalAmount(Float final_amount) {
        this.final_amount = final_amount;
        formatSuper();
    }

    public void setStatus(String status) {
        this.status = status;
        formatSuper();
    }

    public HashMap<String, Object> getSuper() { return this; }

    private void formatSuper() {
        about = new HashMap<>();
        about.put("buyer_email", buyer_email);
        about.put("closing_date", closing_date);
        about.put("seller_email", seller_email);
        about.put("final_amount", final_amount);
        about.put("status", status);
        record = new HashMap<>();
        record.put("descriptors", descriptors);
        record.put("post_id", post_id);
        record.put("genre", genre);
        record.put("dead_post", dead_post);
        record.put("description", description);
        record.put("serial_number", serial_number);
        record.put("image_contexts", image_contexts);
        record.put("title", title);
        record.put("chat_id", chat_id);
        super.clear();
        super.put("record", record);
        super.put("payments", payments);
        super.put("about", about);
        super.put("id", id);
    }
}