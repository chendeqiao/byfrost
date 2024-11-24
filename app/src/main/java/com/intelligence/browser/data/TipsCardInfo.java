package com.intelligence.browser.data;

public class TipsCardInfo {
    public static final int  CARD_TYPE_AD = 101;
    public static final int  CARD_TYPE_NOTIFY = 1;
    public static final int  CARD_TYPE_DEFAULT_BROWSER = 2;
    public static final int  CARD_TYPE_SETTING = 4;
    public static final int  CARD_TYPE_LANGUAGE = 5;
    public static final int  CARD_TYPE_APPLOCK = 6;
    private int typeId;
    private String tips;
    private String title;

    public String getBotton() {
        return botton;
    }

    public void setBotton(String botton) {
        this.botton = botton;
    }

    private String botton;
    private boolean isAd;

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAd() {
        return isAd;
    }

    public void setAd(boolean ad) {
        isAd = ad;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    private int resId;

    public TipsCardInfo() {
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
}
