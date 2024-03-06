package com.example.universitymarket.objects;

import com.example.universitymarket.utilities.Data;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * <b>
 * ANY MODIFICATIONS WILL BE OVERWRITTEN ON BUILD 
 * </b><p>
 * Autogenerated on: Mar 5, 2024, 7:52:29 PM
 * <div>
 * File based on 'transaction_skeleton.json' located at <a href="file:///Users/prestonscott/Desktop/OOSE/University-Market/UniversityMarket/app/src/main/res/raw/">main/res/raw/</a>
*/
public class Transaction extends HashMap<String, Object> {
    private ArrayList<Object> payments = null;
    private HashMap<String, String> about = null;
    private String id = null;
    private String buyer_email = null;
    private String closing_date = null;
    private String seller_email = null;
    private String item_id = null;
    private String total_amount = null;
    private String status = null;

    public Transaction(HashMap<String, Object> rawdata) {
        super.put("payments", rawdata.get("payments"));
        super.put("about", rawdata.get("about"));
        super.put("id", rawdata.get("id"));
        payments = (ArrayList<Object>) super.get("payments");
        about = (HashMap<String, String>) super.get("about");
        id = (String) super.get("id");
        buyer_email = (String) about.get("buyer_email");
        closing_date = (String) about.get("closing_date");
        seller_email = (String) about.get("seller_email");
        item_id = (String) about.get("item_id");
        total_amount = (String) about.get("total_amount");
        status = (String) about.get("status");
    }

    public ArrayList<Object> getPayments() { return payments; }

    public HashMap<String, String> getAbout() { return about; }

    public String getId() { return id; }

    public String getBuyerEmail() { return buyer_email; }

    public String getClosingDate() { return closing_date; }

    public String getSellerEmail() { return seller_email; }

    public String getItemId() { return item_id; }

    public String getTotalAmount() { return total_amount; }

    public String getStatus() { return status; }

    public void setPayments(ArrayList<Object> payments) { super.put("payments", payments); }

    public void setAbout(HashMap<String, String> about) { super.put("about", about); }

    public void setId(String id) { super.put("id", id); }

    public HashMap<String, Object> getSuper() { return this; }
}