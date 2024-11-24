package com.intelligence.browser.markLock.lock;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class BirthdayCenterLineLinearLayout extends FrameLayout{


    public BirthdayCenterLineLinearLayout(Context context) {
        super(context);setWillNotDraw(false);
    }
    public BirthdayCenterLineLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);setWillNotDraw(false);
    }
    public BirthdayCenterLineLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);setWillNotDraw(false);
    }



    private final int centerColor = Color.parseColor("#11AAFF");
    private Rect rect = new Rect();
    private Paint centerBigLinePaint = new Paint();
    {
        centerBigLinePaint.setColor(centerColor);
        centerBigLinePaint.setStyle(Paint.Style.FILL);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int h = getHeight() / 3;
        rect.set(0, h, getWidth(),  h * 2);
        canvas.drawRect(rect, centerBigLinePaint);

    }


}
