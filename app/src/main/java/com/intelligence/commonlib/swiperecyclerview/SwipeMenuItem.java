package com.intelligence.commonlib.swiperecyclerview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class SwipeMenuItem {
    private Context mContext;
    private Drawable background;
    private Drawable icon;
    private String title;
    private ColorStateList titleColor;
    private int titleSize;
    private Typeface textTypeface;
    private int textAppearance;
    private int width = -2;
    private int height = -2;
    private int weight = 0;

    public SwipeMenuItem(Context context) {
        this.mContext = context;
    }

    public SwipeMenuItem setBackgroundDrawable(Drawable background) {
        this.background = background;
        return this;
    }

    public SwipeMenuItem setBackgroundDrawable(int resId) {
        this.background = ResCompat.getDrawable(this.mContext, resId);
        return this;
    }

    public SwipeMenuItem setBackgroundColor(int color) {
        this.background = new ColorDrawable(color);
        return this;
    }

    public Drawable getBackground() {
        return this.background;
    }

    public SwipeMenuItem setText(String title) {
        this.title = title;
        return this;
    }

    public SwipeMenuItem setImage(Drawable icon) {
        this.icon = icon;
        return this;
    }

    public SwipeMenuItem setImage(int resId) {
        return this.setImage(ResCompat.getDrawable(this.mContext, resId));
    }

    public Drawable getImage() {
        return this.icon;
    }

    public SwipeMenuItem setText(int resId) {
        this.setText(this.mContext.getString(resId));
        return this;
    }

    public SwipeMenuItem setTextColor(int titleColor) {
        this.titleColor = ColorStateList.valueOf(titleColor);
        return this;
    }

    public ColorStateList getTitleColor() {
        return this.titleColor;
    }

    public SwipeMenuItem setTextSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public int getTextSize() {
        return this.titleSize;
    }

    public String getText() {
        return this.title;
    }

    public SwipeMenuItem setTextAppearance(int textAppearance) {
        this.textAppearance = textAppearance;
        return this;
    }

    public int getTextAppearance() {
        return this.textAppearance;
    }

    public SwipeMenuItem setTextTypeface(Typeface textTypeface) {
        this.textTypeface = textTypeface;
        return this;
    }

    public Typeface getTextTypeface() {
        return this.textTypeface;
    }

    public int getWidth() {
        return this.width;
    }

    public SwipeMenuItem setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return this.height;
    }

    public SwipeMenuItem setHeight(int height) {
        this.height = height;
        return this;
    }

    public SwipeMenuItem setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public int getWeight() {
        return this.weight;
    }
}
