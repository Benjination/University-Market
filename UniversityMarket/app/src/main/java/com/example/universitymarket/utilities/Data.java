package com.example.universitymarket.utilities;

import android.app.Activity;
import android.util.Log;
import com.example.universitymarket.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public abstract class Data {


    private void updateCache(Activity cur_act, String tag, String value) {
        try {
            FileDescriptor fd = cur_act.getAssets().openNonAssetFd(String.valueOf(R.raw.user_cache)).getFileDescriptor();
            InputStream cache = new FileInputStream(fd);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(cache);


        } catch (IOException | ParserConfigurationException | SAXException e) {
            Log.e(e.toString(), e.getMessage());
        }
    }

    public static Map<String, Object> mapCache(Activity cur_act) {
        try {
            InputStream is = cur_act.getResources().openRawResource(R.raw.user_cache);
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];

            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
            is.close();
            String json = writer.toString();
            Map<String,Object> mapping = new ObjectMapper().readValue(json, HashMap.class);
            return mapping;
        } catch (Exception e) {
            Log.e("JSON read error", e.getMessage());
        }
        return null;
    }
}
