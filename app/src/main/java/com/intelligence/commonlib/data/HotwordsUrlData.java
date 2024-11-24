package com.intelligence.commonlib.data;

import org.json.JSONObject;

public class HotwordsUrlData implements IEntity {
    private String hotwordmaintop;
    private String hotwordmainbottom;
    private String hotwordsearchpage;
    private String hotwordNotification;

    public String getHotwordmaintop() {
        return hotwordmaintop;
    }

    public void setHotwordmaintop(String hotwordmaintop) {
        this.hotwordmaintop = hotwordmaintop;
    }

    public String getHotwordmainbottom() {
        return hotwordmainbottom;
    }

    public void setHotwordmainbottom(String hotwordmainbottom) {
        this.hotwordmainbottom = hotwordmainbottom;
    }

    public String getHotwordsearchpage() {
        return hotwordsearchpage;
    }

    public void setHotwordsearchpage(String hotwordsearchpage) {
        this.hotwordsearchpage = hotwordsearchpage;
    }

    public String getHotwordNotification() {
        return hotwordNotification;
    }

    public void setHotwordNotification(String hotwordNotification) {
        this.hotwordNotification = hotwordNotification;
    }

    @Override
    public void parseJSON(JSONObject jsonObject) throws Exception {
        if(jsonObject == null){
            return;
        }
        this.hotwordmaintop = jsonObject.optString("hotwordmaintop");
        this.hotwordmainbottom = jsonObject.optString("hotwordmainbottom");
        this.hotwordsearchpage = jsonObject.optString("hotwordsearchpage");
        this.hotwordNotification = jsonObject.optString("hotwordNotification");
    }
}
