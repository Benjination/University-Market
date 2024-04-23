package com.example.universitymarket.objects;

import androidx.annotation.Discouraged;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * <b>
 * ANY MODIFICATIONS WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * File based on 'message_skeleton.json' located at <a href="file:///Users/johnnyboi/University-Market/UniversityMarket/app/src/main/res/raw/">main/res/raw/</a>
*/
public class Message extends HashMap<String, Object> {
    private HashMap<String, Object> offer = new HashMap<>();
    private HashMap<String, Object> about = new HashMap<>();
    private String id = null;
    private Boolean offer_taken = null;
    private String offer_post_id = null;
    private String contents = null;
    private ArrayList<String> read_emails = new ArrayList<>();
    private String sender_email = null;
    private String timestamp = null;

    @Discouraged(message = "Unless you are initializing from another skeleton POJO, do not use this constructor")
    public Message(HashMap<String, Object> rawdata) {
        ArrayList<String> rawKeys = new ArrayList<>(rawdata != null ? rawdata.keySet() : new ArrayList<>());
        super.put("offer", rawKeys.contains("offer") ? rawdata.get("offer") : null);
        super.put("about", rawKeys.contains("about") ? rawdata.get("about") : null);
        super.put("id", rawKeys.contains("id") ? rawdata.get("id") : null);
        ArrayList<String> superKeys = new ArrayList<>(super.keySet());
        offer = superKeys.contains("offer") ? (HashMap<String, Object>) super.get("offer") : new HashMap<>();
        about = superKeys.contains("about") ? (HashMap<String, Object>) super.get("about") : new HashMap<>();
        id = superKeys.contains("id") ? (String) super.get("id") : null;
        ArrayList<String> offerKeys = new ArrayList<>(offer != null ? offer.keySet() : new ArrayList<>());
        offer_taken = offerKeys.contains("offer_taken") ? (Boolean) offer.get("offer_taken") : null;
        offer_post_id = offerKeys.contains("offer_post_id") ? (String) offer.get("offer_post_id") : null;
        ArrayList<String> aboutKeys = new ArrayList<>(about != null ? about.keySet() : new ArrayList<>());
        contents = aboutKeys.contains("contents") ? (String) about.get("contents") : null;
        read_emails = aboutKeys.contains("read_emails") ? (ArrayList<String>) about.get("read_emails") : new ArrayList<>();
        sender_email = aboutKeys.contains("sender_email") ? (String) about.get("sender_email") : null;
        timestamp = aboutKeys.contains("timestamp") ? (String) about.get("timestamp") : null;
        formatSuper();
    }

    public Message(Boolean offer_taken, String offer_post_id, String contents, ArrayList<String> read_emails, String sender_email, String timestamp, String id) {
        setOffer(offer_taken, offer_post_id);
        setAbout(contents, read_emails, sender_email, timestamp);
        this.id = id;
        formatSuper();
    }

    public Message() {
        formatSuper();
    }

    public HashMap<String, Object> getOffer() { return offer; }

    public HashMap<String, Object> getAbout() { return about; }

    public String getId() { return id; }

    public Boolean getOfferTaken() { return offer_taken; }

    public String getOfferPostId() { return offer_post_id; }

    public String getContents() { return contents; }

    public ArrayList<String> getReadEmails() { return read_emails; }

    public String getSenderEmail() { return sender_email; }

    public String getTimestamp() { return timestamp; }

    public void setOffer(Boolean offer_taken, String offer_post_id) {
        this.offer_taken = offer_taken;
        this.offer_post_id = offer_post_id;
        formatSuper();
    }

    public void setAbout(String contents, ArrayList<String> read_emails, String sender_email, String timestamp) {
        this.contents = contents;
        this.read_emails = read_emails;
        this.sender_email = sender_email;
        this.timestamp = timestamp;
        formatSuper();
    }

    public void setId(String id) {
        this.id = id;
        formatSuper();
    }

    public void setOfferTaken(Boolean offer_taken) {
        this.offer_taken = offer_taken;
        formatSuper();
    }

    public void setOfferPostId(String offer_post_id) {
        this.offer_post_id = offer_post_id;
        formatSuper();
    }

    public void setContents(String contents) {
        this.contents = contents;
        formatSuper();
    }

    public void setReadEmails(ArrayList<String> read_emails) {
        this.read_emails = read_emails;
        formatSuper();
    }

    public void setSenderEmail(String sender_email) {
        this.sender_email = sender_email;
        formatSuper();
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        formatSuper();
    }

    public HashMap<String, Object> getSuper() { return this; }

    private void formatSuper() {
        about = new HashMap<>();
        about.put("contents", contents);
        about.put("read_emails", read_emails);
        about.put("sender_email", sender_email);
        about.put("timestamp", timestamp);
        offer = new HashMap<>();
        offer.put("offer_taken", offer_taken);
        offer.put("offer_post_id", offer_post_id);
        super.clear();
        super.put("offer", offer);
        super.put("about", about);
        super.put("id", id);
    }
}