package com.intelligence.browser.markLock.lock;

import java.io.Serializable;

public class PatternLockTheme implements Serializable {

    protected int dotDrawableID;
    protected int dotActivatedDrawableID;
    protected int dotActivated2DrawableID;
    protected int dotErrorDrawableId;

    protected String dotSDCardPath;
    protected String dotActivatedSDCardPath;
    protected String dotActivated2SDCardPath;
    protected String dotErrorSDCardPath;

    protected int lineColor;
    protected int lineWidthDp;


    public int getDotDrawableID() {
        return dotDrawableID;
    }

    public void setDotDrawableID(int dotDrawableID) {
        this.dotDrawableID = dotDrawableID;
    }

    public int getDotActivatedDrawableID() {
        return dotActivatedDrawableID;
    }

    public void setDotActivatedDrawableID(int dotActivatedDrawableID) {
        this.dotActivatedDrawableID = dotActivatedDrawableID;
    }

    public int getDotActivated2DrawableID() {
        return dotActivated2DrawableID;
    }

    public void setDotActivated2DrawableID(int dotActivated2DrawableID) {
        this.dotActivated2DrawableID = dotActivated2DrawableID;
    }

    public int getDotErrorDrawableId() {
        return dotErrorDrawableId;
    }

    public void setDotErrorDrawableId(int dotErrorDrawableId) {
        this.dotErrorDrawableId = dotErrorDrawableId;
    }

    public String getDotSDCardPath() {
        return dotSDCardPath;
    }

    public void setDotSDCardPath(String dotSDCardPath) {
        this.dotSDCardPath = dotSDCardPath;
    }

    public String getDotActivatedSDCardPath() {
        return dotActivatedSDCardPath;
    }

    public void setDotActivatedSDCardPath(String dotActivatedSDCardPath) {
        this.dotActivatedSDCardPath = dotActivatedSDCardPath;
    }

    public String getDotActivated2SDCardPath() {
        return dotActivated2SDCardPath;
    }

    public void setDotActivated2SDCardPath(String dotActivated2SDCardPath) {
        this.dotActivated2SDCardPath = dotActivated2SDCardPath;
    }

    public String getDotErrorSDCardPath() {
        return dotErrorSDCardPath;
    }

    public void setDotErrorSDCardPath(String dotErrorSDCardPath) {
        this.dotErrorSDCardPath = dotErrorSDCardPath;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public int getLineWidthDp() {
        return lineWidthDp;
    }

    public void setLineWidthDp(int lineWidthDp) {
        this.lineWidthDp = lineWidthDp;
    }
}
