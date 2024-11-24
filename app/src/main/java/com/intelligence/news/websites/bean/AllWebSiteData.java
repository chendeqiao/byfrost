package com.intelligence.news.websites.bean;

import com.intelligence.commonlib.data.IEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class AllWebSiteData implements IEntity {
    public String title;
    public ArrayList<WebSiteData> webSites = new ArrayList<>();
    public ArrayList<WebSiteData> lables = new ArrayList<>();

    @Override
    public void parseJSON(JSONObject object) throws Exception {
        if (object == null) {
            return;
        }
        if (object.has("title")) {
            title = object.optString("title");
        }

        if (object.has("tools")) {
            JSONArray function = object.getJSONArray("tools");
            webSites = new ArrayList<>();
            int functionLength = function != null ? function.length() : 0;
            for (int i = 0; i < functionLength; i++) {
                WebSiteData rcmscenes = new WebSiteData();
                rcmscenes.parseJSON(function.getJSONObject(i));
                webSites.add(rcmscenes);
            }
        }
        if (object.has("lables")) {
            JSONArray function = object.getJSONArray("lables");
            lables = new ArrayList<>();
            int functionLength = function != null ? function.length() : 0;
            for (int i = 0; i < functionLength; i++) {
                WebSiteData rcmscenes = new WebSiteData();
                rcmscenes.parseJSON(function.getJSONObject(i));
                lables.add(rcmscenes);
            }
        }
    }
}
