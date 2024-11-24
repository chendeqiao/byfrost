package com.intelligence.news.news.mode;

import com.intelligence.commonlib.data.IEntity;
import com.intelligence.news.NetConfig;

import org.json.JSONObject;

public class HotWordData implements IEntity {
    public static final int LABLE_TYPE_NEW = 1;
    public static final int LABLE_TYPE_RECOMEND = 5;
    public static final int LABLE_TYPE_HOT = 3;
    public static final int TYPE_AD = 201;
    public String hotword;
    public int index;
    public boolean trend;
    public int labletype;
    public int dataType;
    public String id;
    public String hotwordnum;
    public int pageType;
    public int position;

    @Override
    public void parseJSON(JSONObject jsonObject) throws Exception {
        if (jsonObject == null) {
            return;
        }
        if (jsonObject.has("keyword")) {
            hotword = jsonObject.getString("keyword");
        }
        if (jsonObject.has("word")) {
            hotword = jsonObject.getString("word");
        }
        if (jsonObject.has("hotword")) {
            hotword = jsonObject.getString("hotword");
        }
        if (jsonObject.has("label")) {
            labletype = jsonObject.optInt("label");
        }
        if (labletype == 0 && pageType != NetConfig.PAGE_DOUYIN) {
            int type = getRandomBoolean();
            if (type == 2) {
                labletype = LABLE_TYPE_NEW;
            } else if (type == 5) {
                labletype = LABLE_TYPE_RECOMEND;
            } else if (type == 7) {
                labletype = LABLE_TYPE_HOT;
            }
        }
        if (jsonObject.has("trend")) {
            trend = !jsonObject.getString("trend").equals("fall");
        }
        if (jsonObject.has("index")) {
            index = jsonObject.getInt("index");
        }
        if (jsonObject.has("hotwordnum")) {
            hotwordnum = jsonObject.getString("hotwordnum");
        }
        if (jsonObject.has("hotindex")) {
            hotwordnum = jsonObject.getString("hotindex");
        }
    }

    protected int getRandomBoolean() {
        return (int) (1 + Math.random() * (10 - 1 + 1));
    }
}
