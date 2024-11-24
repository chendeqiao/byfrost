package com.intelligence.browser.markLock.lock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;

public class BirthNumberPicker extends NumberPicker{


    public BirthNumberPicker(Context context) {
        super(context);
        disableDivider();
    }
    public BirthNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        disableDivider();
    }
    public BirthNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        disableDivider();
    }

    private void disableDivider(){
        try{
            Field f = getClass().getSuperclass().getDeclaredField("mSelectionDivider");
            f.setAccessible(true);
            Drawable drawable = (Drawable) f.get(this);
            drawable.setAlpha(0);
            drawable.setBounds(0,0,0,0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    @Override
    public void addView(View child) {
        super.addView(child);
        editView(child);
    }
    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        editView(child);
    }
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        editView(child);
    }
    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        editView(child);
    }
    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        editView(child);
    }






    private synchronized void editView(View view){
        if(view instanceof EditText){
            ((EditText)view).setTextColor(Color.WHITE);
            ((EditText)view).setTextSize(16);
            ((EditText)view).setAlpha(0.9f);
            ((EditText)view).setEnabled(false);
        }
    }





    //用来绘制那两条横线
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    {
        paint.setColor(Color.WHITE);
        paint.setAlpha(160);
        paint.setStrokeWidth(dp2px(2));
    }

    private int dp2px(int dp){
        return (int) (getResources().getDisplayMetrics().density * dp +0.5f);
    }


    private final int centerColor = Color.parseColor("#062B3C");
    private Rect rect = new Rect();
    private Paint centerBigLinePaint = new Paint();
    {
        centerBigLinePaint.setColor(centerColor);
        centerBigLinePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        int h = getHeight() / 3;
//        rect.set(0, h, getWidth(),  h * 2);
//        canvas.drawRect(rect, centerBigLinePaint);

        super.onDraw(canvas);

        //去掉这两个大横线

//        float y1 = getHeight()/3+dp2px(2)+paint.getStrokeWidth();
//        canvas.drawLine(0, y1, getWidth(), y1, paint);
//
//        float y2 = getHeight()/3*2 - dp2px(2) - paint.getStrokeWidth();
//        canvas.drawLine(0, y2, getWidth(), y2, paint);

    }


}
