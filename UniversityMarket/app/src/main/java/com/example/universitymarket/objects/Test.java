package com.example.universitymarket.objects;

import com.example.universitymarket.utilities.Data;
import java.util.HashMap;
import java.util.List;

public class Test extends HashMap<String, Object> {
    private String field_lvl1;
    private HashMap<String, Object> coll_lvl1;
    private List<Object> list_lvl1;
    private List<String> list_lvl2;
    private HashMap<String, Object> coll_lvl1Map = new HashMap<>();

    public Test(HashMap<String, Object> rawdata) {
        super.put("field_lvl1", rawdata.get("field_lvl1"));
        super.put("coll_lvl1", rawdata.get("coll_lvl1"));
        super.put("list_lvl1", rawdata.get("list_lvl1"));
        field_lvl1 = (String) super.get("field_lvl1");
        coll_lvl1 = (HashMap<String, Object>) super.get("coll_lvl1");
        list_lvl1 = (List<Object>) super.get("list_lvl1");
        list_lvl2 = (List<String>) coll_lvl1.get("list_lvl2");

        for(Object o : list_lvl1) {
            Data.mergeHash(coll_lvl1Map, (HashMap<String, Object>) o);
        }
    }

    public String getFieldLvl1() {
        return field_lvl1;
    }

    public HashMap<String, Object> getCollLvl1() {
        return coll_lvl1;
    }

    public List<Object> getListLvl1() { return list_lvl1; }

    public List<String> getListLvl2() { return list_lvl2; }

    public HashMap<String, Object> getCollLvl1Map() { return coll_lvl1Map; }

    public String getFieldLvl2() { return (String) coll_lvl1.get("field_lvl2"); }

    public String getFieldLvl3() { return (String) coll_lvl1Map.get("field_lvl3"); }

    public HashMap<String, Object> getSuper() {
        HashMap<String, Object> parent = this;
        return parent;
    }
}
