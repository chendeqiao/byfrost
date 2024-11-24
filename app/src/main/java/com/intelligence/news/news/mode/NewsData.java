package com.intelligence.news.news.mode;

import android.text.TextUtils;

import com.intelligence.commonlib.data.IEntity;
import com.intelligence.news.NetConfig;
import com.intelligence.news.news.channel.CardFactory;

import org.json.JSONObject;

public class NewsData implements IEntity {
    public String id;
    public String time;
    public String title;
    public String description;
    public String source;
    public String picUrl;
    public String url;
    public int cardType = 0;
    public int mediaType = NetConfig.PAGE_BAIDU;
    public int position;

    @Override
    public void parseJSON(JSONObject jsonObject) throws Exception {
        if (jsonObject == null) {
            return;
        }
        if (jsonObject.has("id")) {
            id = jsonObject.getString("id");
        }
        if (jsonObject.has("ctime")) {
            time = jsonObject.getString("ctime");
        }
        if (jsonObject.has("mtime")) {
            time = jsonObject.getString("mtime");
        }

        if (jsonObject.has("title")) {
            title = jsonObject.getString("title");
        }
        if (jsonObject.has("description")) {
            description = jsonObject.getString("description");
        }

        if (jsonObject.has("digest")) {
            description = jsonObject.getString("digest");
        }
        if (jsonObject.has("source")) {
            source = jsonObject.getString("source");
        }
        if (jsonObject.has("picUrl")) {
            picUrl = jsonObject.getString("picUrl");
        }

        if (jsonObject.has("imgsrc")) {
            picUrl = jsonObject.getString("imgsrc");
        }
        if (jsonObject.has("url")) {
            url = jsonObject.getString("url");
        }
        if (TextUtils.isEmpty(picUrl)) {
            cardType = CardFactory.CARD_TEXT;
        } else {
            if (getRandomBoolean()>7) {
                cardType = CardFactory.CARD_IMAGE;
            } else {
                cardType = CardFactory.CARD_TEXT_IMAGE;
            }
        }
    }

    protected int getRandomBoolean() {
        return (int) (1 + Math.random() * (10 - 1 + 1));
    }
}
