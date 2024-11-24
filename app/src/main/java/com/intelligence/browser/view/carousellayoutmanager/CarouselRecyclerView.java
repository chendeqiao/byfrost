package com.intelligence.browser.view.carousellayoutmanager;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CarouselRecyclerView extends RecyclerView {
    private int FLING_SCALE_DOWN_DISTANCE = 10000;

    public CarouselRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        return super.fling(velocityX, velocityY);
    }

}
