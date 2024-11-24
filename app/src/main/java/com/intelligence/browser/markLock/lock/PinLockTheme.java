package com.intelligence.browser.markLock.lock;

import java.io.Serializable;

public class PinLockTheme implements Serializable {

    protected int indicatorSDCardPath;

    protected String[] numberBgSDCardPaths;
    protected int[]    numberBgDrawableIds;
    protected int[]    selectedNumberBgDrawableIds;
    protected String backBgSDCardPath;
    protected int      backBgDrawableId;


    public int getIndicatorSDCardPath() {
        return indicatorSDCardPath;
    }

    public void setIndicatorSDCardPath(int indicatorSDCardPath) {
        this.indicatorSDCardPath = indicatorSDCardPath;
    }

    public String[] getNumberBgSDCardPaths() {
        return numberBgSDCardPaths;
    }

    public void setNumberBgSDCardPaths(String[] numberBgSDCardPaths) {
        this.numberBgSDCardPaths = numberBgSDCardPaths;
    }

    public int[] getNumberBgDrawableIds() {
        return numberBgDrawableIds;
    }

    public void setNumberBgDrawableIds(int[] numberBgDrawableIds) {
        this.numberBgDrawableIds = numberBgDrawableIds;
    }

    public String getBackBgSDCardPath() {
        return backBgSDCardPath;
    }

    public void setBackBgSDCardPath(String backBgSDCardPath) {
        this.backBgSDCardPath = backBgSDCardPath;
    }

    public int getBackBgDrawableId() {
        return backBgDrawableId;
    }

    public void setBackBgDrawableId(int backBgDrawableId) {
        this.backBgDrawableId = backBgDrawableId;
    }

    public int[] getSelectedNumberBgDrawableIds() {
        return selectedNumberBgDrawableIds;
    }

    public void setSelectedNumberBgDrawableIds(int[] selectedNumberBgDrawableIds) {
        this.selectedNumberBgDrawableIds = selectedNumberBgDrawableIds;
    }
}
