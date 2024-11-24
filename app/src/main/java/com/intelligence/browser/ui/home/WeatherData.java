package com.intelligence.browser.ui.home;

import com.intelligence.commonlib.data.IEntity;

import org.json.JSONObject;

public class WeatherData implements IEntity {
    public String area;
    public String date;
    public String week;
    public String weather;
    public String weatherimg;
    public String real;
    public String lowest;
    public String highest;
    public String wind;
    public String winddeg;
    public String windspeed;
    public String windsc;
    public String sunrise;
    public String sunset;
    public String moonrise;
    public String pcpn;
    public String moondown;
    public String tips;
    public String humidity;
    public String vis;

    @Override
    public void parseJSON(JSONObject jsonObject) throws Exception {
        if (jsonObject == null) {
            return;
        }
        if (jsonObject.has("tips")) {
            tips = jsonObject.getString("tips");
        }
        if (jsonObject.has("area")) {
            area = jsonObject.getString("area");
        }
        if (jsonObject.has("real")) {
            real = jsonObject.getString("real");
        }
        if (jsonObject.has("weatherimg")) {
            weatherimg = jsonObject.optString("weatherimg");
        }
        if (jsonObject.has("weather")) {
            weather = jsonObject.optString("weather");
        }
    }
}
