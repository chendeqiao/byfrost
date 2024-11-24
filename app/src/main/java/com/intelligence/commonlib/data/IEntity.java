package com.intelligence.commonlib.data;

import org.json.JSONObject;

public interface IEntity {
    void parseJSON(JSONObject jsonObject) throws Exception;
}
