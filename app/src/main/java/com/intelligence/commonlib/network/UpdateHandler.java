package com.intelligence.commonlib.network;

import android.content.Context;
import android.text.TextUtils;

import com.intelligence.commonlib.tools.NetworkUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class UpdateHandler {
    private static final String LOGTAG = UpdateHandler.class.getSimpleName();

    protected Context mContext = null;
    private List<NameValuePair> mParams = null;
    private long mDelay = 0L; // Milliseconds.

    public UpdateHandler(Context context) {
        mContext = context;
        mParams = new ArrayList<NameValuePair>();

        // Init something for feature.
        init();
    }

    protected long delay() {
        return mDelay;
    }

    protected void setParams(String name, String value) {
        if (mParams == null) {
            mParams = new ArrayList<NameValuePair>();
        }
        if (TextUtils.isEmpty(name)) return;
        mParams.add(new NameValuePair(name, value));
    }

    protected void setParams(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            setParams(entry.getKey(), entry.getValue());
        }
    }

    protected void removeParams(String name) {
        for (NameValuePair item : mParams) {
            String key = item.getName();
            if (key.equals(name)) {
                mParams.remove(item);
                break;
            }
        }
    }

    protected List<NameValuePair> getParams() {
        return mParams;
    }

    protected Object getParam(String name) {
        if (mParams == null) return null;
        for (NameValuePair item : mParams) {
            String key = item.getName();
            if (key.equals(name)) {
                return item.getValue();
            }
        }
        return null;
    }

    public void init() {
        // Init for feature.
    }

    // Check update for feature.
    public boolean checkUpdate() {
        String token = (String) SharedPreferencesUtils.get(SharedPreferencesUtils.ACCESS_TOKEN, "");
        if (!TextUtils.isEmpty(token)) {
            removeParams("tk");
            setParams("tk", token);
        }

        return NetworkUtils.isNetworkAvailable();
    }

    public void initForUpdate() {
        // Init something for update.
    }

    public void doUpdateBefore() {
        // Do something before update.
    }

    public boolean doUpdateNow() {
        // Do update now.

        return true;  // default
    }

    public void doUpdateSuccess() {
        // Success update.
    }

    public void doUpdateFail() {
        // Fail update.
    }

    public void checkUpdateFail() {
        // Check update fail.
    }

}
