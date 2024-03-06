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
 * File based on 'test_skeleton.json' located at <a href="file:///Users/prestonscott/Desktop/OOSE/University-Market/UniversityMarket/app/src/main/res/raw/">main/res/raw/</a>
*/
public class Test extends HashMap<String, Object> {
    private HashMap<String, Object> coll_lvl1 = null;
    private ArrayList<HashMap> list_lvl1 = null;
    private String field_lvl1 = null;
    private ArrayList<Object> list_lvl2 = null;
    private String field_lvl2 = null;
    private HashMap<String, Object> list_lvl1Map = new HashMap<>();

    public Test(HashMap<String, Object> rawdata) {
        super.put("coll_lvl1", rawdata.get("coll_lvl1"));
        super.put("list_lvl1", rawdata.get("list_lvl1"));
        super.put("field_lvl1", rawdata.get("field_lvl1"));
        coll_lvl1 = (HashMap<String, Object>) super.get("coll_lvl1");
        list_lvl1 = (ArrayList<HashMap>) super.get("list_lvl1");
        field_lvl1 = (String) super.get("field_lvl1");
        list_lvl2 = (ArrayList<Object>) coll_lvl1.get("list_lvl2");
        field_lvl2 = (String) coll_lvl1.get("field_lvl2");

        for(Object o : list_lvl1) {
            Data.mergeHash(list_lvl1Map, (HashMap<String, Object>) o);
        }
    }

    public HashMap<String, Object> getCollLvl1() { return coll_lvl1; }

    public ArrayList<HashMap> getListLvl1() { return list_lvl1; }

    public String getFieldLvl1() { return field_lvl1; }

    public ArrayList<Object> getListLvl2() { return list_lvl2; }

    public String getFieldLvl2() { return field_lvl2; }

    public HashMap<String, Object> getListLvl1Map() { return list_lvl1Map; }

    public void setCollLvl1(HashMap<String, Object> coll_lvl1) { super.put("coll_lvl1", coll_lvl1); }

    public void setListLvl1(ArrayList<HashMap> list_lvl1) { super.put("list_lvl1", list_lvl1); }

    public void setFieldLvl1(String field_lvl1) { super.put("field_lvl1", field_lvl1); }

    public HashMap<String, Object> getSuper() { return this; }
}