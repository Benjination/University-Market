package com.example.universitymarket.objects;

import com.example.universitymarket.utilities.Data;
import java.util.HashMap;
import java.util.List;

public class Transaction extends HashMap<String, Object> {
    private String id;
    private HashMap<String, Object> about;
    private List<Object> payments;
    private HashMap<String, Object> paymentsMap = new HashMap<>();

    public Transaction(HashMap<String, Object> rawdata) {
        super.put("id", rawdata.get("id"));
        super.put("about", rawdata.get("about"));
        super.put("payments", rawdata.get("payments"));
        id = (String) super.get("id");
        about = (HashMap<String, Object>) super.get("about");
        payments = (List<Object>) super.get("payments");

        for(Object o : payments) {
            Data.mergeHash(paymentsMap, (HashMap<String, Object>) o);
        }
    }

    public String getId() { return id; }

    public HashMap<String, Object> getAbout() { return about; }

    public List<Object> getPayments() { return payments; }

    public HashMap<String, Object> getPaymentsMap() { return paymentsMap; }

    public String getItemId() { return (String) about.get("item_id"); }

    public String getTotalAmount() { return (String) about.get("total_amount"); }

    public String getBuyerEmail() { return (String) about.get("buyer_email"); }

    public String getSellerEmail() { return (String) about.get("seller_email"); }

    public String getClosingDate() { return (String) about.get("closing_date"); }

    public String getStatus() { return (String) about.get("status"); }

    public HashMap<String, Object> getSuper() {
        HashMap<String, Object> parent = this;
        return parent;
    }
}