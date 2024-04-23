package com.example.universitymarket.objects;

import androidx.annotation.Discouraged;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * <b>
 * ANY MODIFICATIONS WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * File based on 'chat_skeleton.json' located at <a href="file:///Users/johnnyboi/University-Market/UniversityMarket/app/src/main/res/raw/">main/res/raw/</a>
*/
public class Chat extends HashMap<String, Object> {
    private HashMap<String, Object> about = new HashMap<>();
    private String id = null;
    private String date_created = null;
    private ArrayList<String> participant_emails = new ArrayList<>();
    private ArrayList<String> message_ids = new ArrayList<>();

    @Discouraged(message = "Unless you are initializing from another skeleton POJO, do not use this constructor")
    public Chat(HashMap<String, Object> rawdata) {
        ArrayList<String> rawKeys = new ArrayList<>(rawdata != null ? rawdata.keySet() : new ArrayList<>());
        super.put("about", rawKeys.contains("about") ? rawdata.get("about") : null);
        super.put("id", rawKeys.contains("id") ? rawdata.get("id") : null);
        ArrayList<String> superKeys = new ArrayList<>(super.keySet());
        about = superKeys.contains("about") ? (HashMap<String, Object>) super.get("about") : new HashMap<>();
        id = superKeys.contains("id") ? (String) super.get("id") : null;
        ArrayList<String> aboutKeys = new ArrayList<>(about != null ? about.keySet() : new ArrayList<>());
        date_created = aboutKeys.contains("date_created") ? (String) about.get("date_created") : null;
        participant_emails = aboutKeys.contains("participant_emails") ? (ArrayList<String>) about.get("participant_emails") : new ArrayList<>();
        message_ids = aboutKeys.contains("message_ids") ? (ArrayList<String>) about.get("message_ids") : new ArrayList<>();
        formatSuper();
    }

    public Chat(String date_created, ArrayList<String> participant_emails, ArrayList<String> message_ids, String id) {
        setAbout(date_created, participant_emails, message_ids);
        this.id = id;
        formatSuper();
    }

    public Chat() {
        formatSuper();
    }

    public HashMap<String, Object> getAbout() { return about; }

    public String getId() { return id; }

    public String getDateCreated() { return date_created; }

    public ArrayList<String> getParticipantEmails() { return participant_emails; }

    public ArrayList<String> getMessageIds() { return message_ids; }

    public void setAbout(String date_created, ArrayList<String> participant_emails, ArrayList<String> message_ids) {
        this.date_created = date_created;
        this.participant_emails = participant_emails;
        this.message_ids = message_ids;
        formatSuper();
    }

    public void setId(String id) {
        this.id = id;
        formatSuper();
    }

    public void setDateCreated(String date_created) {
        this.date_created = date_created;
        formatSuper();
    }

    public void setParticipantEmails(ArrayList<String> participant_emails) {
        this.participant_emails = participant_emails;
        formatSuper();
    }

    public void setMessageIds(ArrayList<String> message_ids) {
        this.message_ids = message_ids;
        formatSuper();
    }

    public HashMap<String, Object> getSuper() { return this; }

    private void formatSuper() {
        about = new HashMap<>();
        about.put("date_created", date_created);
        about.put("participant_emails", participant_emails);
        about.put("message_ids", message_ids);
        super.clear();
        super.put("about", about);
        super.put("id", id);
    }
}