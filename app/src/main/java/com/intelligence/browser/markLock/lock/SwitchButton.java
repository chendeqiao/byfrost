package com.intelligence.browser.markLock.lock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;


public class SwitchButton extends View{


    public SwitchButton(Context context) {
        super(context);
    }
    public SwitchButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public SwitchButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private boolean isOpen = false;

    public boolean isOpen() {
        return isOpen;
    }

    public void setState(boolean open){
        isOpen = open;
        invalidate();
    }







    private int openBGColor = Color.parseColor("#009688");
    private int openCircleColor = Color.parseColor("#009688");

    private int closeBGColor = Color.parseColor("#221F1F");
    private int closeCircleColor = Color.parseColor("#F1F1F1");


    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF rect = new RectF();



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int gap = dp2px(getContext(), 2);
        int dp1 = dp2px(getContext(), 1);
        rect.set(gap, getHeight()/6 + dp1, getWidth()-gap, (getHeight()/3*2-dp1)+getHeight()/6);

        bgPaint.setAlpha(255);
        circlePaint.setAlpha(255);

        int dpCorner = getHeight();

        if(isOpen){
            bgPaint.setColor(openBGColor);
            bgPaint.setAlpha(255/2);
            circlePaint.setColor(openCircleColor);

            canvas.drawRoundRect(rect, dpCorner, dpCorner, bgPaint);

            int radius = (getHeight() - gap * 2) / 2;
            canvas.drawCircle(getWidth()-gap-radius, getHeight()/2, radius, circlePaint);
        }else{
            bgPaint.setColor(closeBGColor);
            bgPaint.setAlpha((int) (255*0.3f));
            circlePaint.setColor(closeCircleColor);

            canvas.drawRoundRect(rect, dpCorner, dpCorner, bgPaint);
            int radius = (getHeight() - gap * 2) / 2;
            canvas.drawCircle(gap+radius, getHeight()/2, radius, circlePaint);
        }

    }

    private int dp2px(Context context, int dp){
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5f);
    }

}
