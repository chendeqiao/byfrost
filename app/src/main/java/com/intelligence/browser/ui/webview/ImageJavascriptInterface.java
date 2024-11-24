package com.intelligence.browser.ui.webview;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.webview.Controller;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.tools.StringUtil;
import com.intelligence.commonlib.download.util.StringUtils;

import org.json.JSONArray;

import java.util.ArrayList;

public class ImageJavascriptInterface {
    private Context mContext;
    private String[] imageUrls;
    private Controller mController;

    public ImageJavascriptInterface(Controller controller) {
        mController = controller;
    }

    @JavascriptInterface
    public void receiveImagesArray(String array) {
        try {
            JSONArray jsonArray = new JSONArray(array);
            String[] stringArray = new String[]{};
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < jsonArray.length(); i++) {
                String url = jsonArray.getString(i);
                if (!TextUtils.isEmpty(url) && !url.endsWith("svg") && mController.isSameMediaType(url)) {
                    arrayList.add(jsonArray.getString(i));
                }
            }
            if (arrayList.contains(mController.mImageUrl)) {
                mController.openPhotoView((String[]) arrayList.toArray(stringArray));
            } else {
                mController.openPhotoView();
            }
        } catch (Exception e) {
            if (mController != null) {
                mController.openPhotoView();
            }
        }
    }

    @JavascriptInterface
    public void onBase64ImageReceived(String base64Data) {
        if(base64Data.startsWith("blob:")){
            return;
        }
        if(mController.getActivity() != null) {
            String localPath = StringUtils.saveBase64Image(mController.getActivity(), base64Data);
            mController.openPhotoView(new String[]{localPath});
        }
    }

    public static ArrayList<String> mAdLists = new ArrayList<>();
    private static boolean mIsInitAd;
    public static void initAdLists(){
        if(mIsInitAd){
            return;
        }
        mIsInitAd = true;
        mAdLists = SharedPreferencesUtils.getArrayListFromPreferences(SharedPreferencesUtils.AD_CLOSE_TIME_KEY);
    }

    @JavascriptInterface
    public void closeAdSuccuess(String data) {
        try {
            if (!TextUtils.isEmpty(data) && !mAdLists.contains(data)) {
                mAdLists.add(data);
                syncAdData();
            }
        } catch (Exception e) {
        }
    }

    public static void syncAdData(){
        if(!CollectionUtils.isEmpty(mAdLists)) {
            BrowserSettings.getInstance().setImgAdBlockCount(mAdLists.size()+BrowserSettings.getInstance().getImgAdBlockCount());
            SharedPreferencesUtils.saveArrayListToPreferences(SharedPreferencesUtils.AD_CLOSE_TIME_KEY, StringUtil.getLastTwoHundred(mAdLists,200));
            SharedPreferencesUtils.put(SharedPreferencesUtils.AD_CLOSE_TIME_CURRENT_KEY,System.currentTimeMillis());
        }
    }
}
