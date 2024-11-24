package com.intelligence.commonlib.data;

import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import org.json.JSONObject;

public class MainifestData implements IEntity {
    private int versioncode;
    private String browser_navigation;
    private String ad_top_forbid;
    private String ad_bottom_forbid;
    private String appdownurl;
    private String hotwordUrlGroups;
    private AdSwitchData adSwitchData;
    private int isRecommendCheck;

    public int getIsRecommendCheck() {
        return isRecommendCheck;
    }

    public void setIsRecommendCheck(int isRecommendCheck) {
        this.isRecommendCheck = isRecommendCheck;
    }

    public int getVersioncode() {
        return versioncode;
    }

    public void setVersioncode(int versioncode) {
        this.versioncode = versioncode;
    }

    public String getBrowser_navigation() {
        return browser_navigation;
    }

    public void setBrowser_navigation(String browser_navigation) {
        this.browser_navigation = browser_navigation;
    }

    public String getAd_top_forbid() {
        return ad_top_forbid;
    }

    public void setAd_top_forbid(String ad_top_forbid) {
        this.ad_top_forbid = ad_top_forbid;
    }

    public String getAd_bottom_forbid() {
        return ad_bottom_forbid;
    }

    public void setAd_bottom_forbid(String ad_bottom_forbid) {
        this.ad_bottom_forbid = ad_bottom_forbid;
    }

    public AdSwitchData getAdSwitchData() {
        return adSwitchData;
    }

    public void setAdSwitchData(AdSwitchData adSwitchData) {
        this.adSwitchData = adSwitchData;
    }

    public String getAppdownurl() {
        return appdownurl;
    }

    public void setAppdownurl(String appdownurl) {
        this.appdownurl = appdownurl;
    }

    public String getHotwordUrlGroups() {
        return hotwordUrlGroups;
    }

    public void setHotwordUrlGroups(String hotwordUrlGroups) {
        this.hotwordUrlGroups = hotwordUrlGroups;
    }

    @Override
    public void parseJSON(JSONObject jsonObject) throws Exception {
        if(jsonObject == null){
            return;
        }
        this.versioncode = jsonObject.optInt("versioncode");
        this.isRecommendCheck = jsonObject.optInt("isRecommendCheck");
        this.browser_navigation = jsonObject.optString("browser_navigation");
        this.ad_top_forbid = jsonObject.optString("ad_top_forbid");
        this.ad_bottom_forbid = jsonObject.optString("ad_bottom_forbid");
        this.appdownurl = jsonObject.optString("appdownurl");
        this.hotwordUrlGroups = jsonObject.optString("hotwordUrlGroups");
        if(jsonObject.has("adSwitch")){
            SharedPreferencesUtils.setAdConfig(jsonObject.optString("adSwitch"));
        }
    }
}
