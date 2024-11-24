package com.intelligence.commonlib.data;

import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import org.json.JSONObject;

public class AdSwitchData implements IEntity {
    private int totalAdSwitch;
    private int webviewTopAdSwitch;
    private int webviewBottomAdSwitch;
    private int hotwordAdSwitch;
    private int navigationAdSwitch;
    private int adInterval;
    private String json;

    public int getAdInterval() {
        return adInterval;
    }

    public void setAdInterval(int adInterval) {
        this.adInterval = adInterval;
    }

    public int getTotalAdSwith() {
        return totalAdSwitch;
    }

    public void setTotalAdSwith(int totalAdSwith) {
        this.totalAdSwitch = totalAdSwith;
    }

    public int getWebviewTopAdSwith() {
        return webviewTopAdSwitch;
    }

    public void setWebviewTopAdSwith(int webviewTopAdSwith) {
        this.webviewTopAdSwitch = webviewTopAdSwith;
    }

    public int getWebviewBottomAdSwith() {
        return webviewBottomAdSwitch;
    }

    public void setWebviewBottomAdSwith(int webviewBottomAdSwith) {
        this.webviewBottomAdSwitch = webviewBottomAdSwith;
    }

    public int getHotwordAdSwith() {
        return hotwordAdSwitch;
    }

    public void setHotwordAdSwith(int hotwordAdSwith) {
        this.hotwordAdSwitch = hotwordAdSwith;
    }

    public int getNavigationAdSwith() {
        return navigationAdSwitch;
    }

    public void setNavigationAdSwith(int navigationAdSwith) {
        this.navigationAdSwitch = navigationAdSwith;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public void parseJSON(JSONObject jsonObject) throws Exception {
        if(jsonObject == null){
            return;
        }
        this.totalAdSwitch = jsonObject.optInt("totalAdSwitch");
        this.webviewTopAdSwitch = jsonObject.optInt("webviewTopAdSwitch");
        this.webviewBottomAdSwitch = jsonObject.optInt("webviewBottomAdSwitch");
        this.hotwordAdSwitch = jsonObject.optInt("hotwordAdSwitch");
        this.navigationAdSwitch = jsonObject.optInt("navigationAdSwitch");
        this.adInterval = jsonObject.optInt("adInterval",5);
        SharedPreferencesUtils.setAdInterval(jsonObject.optInt("adInterval",5));
        json = jsonObject.toString();
    }
}
