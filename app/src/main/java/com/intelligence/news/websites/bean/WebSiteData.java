package com.intelligence.news.websites.bean;

import com.intelligence.commonlib.data.IEntity;

import org.json.JSONObject;

public class WebSiteData implements IEntity {
    public String title;
    public String logo;
    public String scheme;
    public String shorttitle;
    public boolean isEditAdd;

    public void setEditAdd(boolean isEditAdd){
        this.isEditAdd = isEditAdd;
    }

    @Override
    public void parseJSON(JSONObject object) throws Exception {
        if (object == null) {
            return;
        }
        if (object.has("logo")) {
            logo = object.optString("logo");
        }
        if (object.has("title")) {
            title = object.optString("title");
        }
        if (object.has("scheme")) {
            scheme = object.optString("scheme");
        }
        if (object.has("shorttitle")) {
            shorttitle = object.optString("shorttitle");
        }
    }
}
