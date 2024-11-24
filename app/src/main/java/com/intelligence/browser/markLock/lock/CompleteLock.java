package com.intelligence.browser.markLock.lock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.ScreenUtils;


public class CompleteLock extends View{


    public CompleteLock(Context context) {
        super(context);
    }

    public CompleteLock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CompleteLock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    private Bitmap lock;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#22000000"));
        canvas.translate(getWidth()/2, getHeight()/2);
        canvas.drawCircle(0, 0, getWidth()/2, paint);
        canvas.restore();

        canvas.save();
        canvas.translate(getWidth()/2, getHeight()/2);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#55ffffff"));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(ScreenUtils.dpToPx(getContext(),4));
        final int lineLen = (int) ScreenUtils.dpToPx(getContext(),8);
        final int gap = (int) ScreenUtils.dpToPx(getContext(),8);
        for (int i = 0; i < 12; i++) {
            canvas.drawLine(0, getHeight()/4 - lineLen+gap, 0, getHeight()/4+gap, paint);
            canvas.rotate(360/12);
        }
        canvas.restore();

        int dp2 = (int) ScreenUtils.dpToPx(getContext(),2);
        if(lock == null){
            Bitmap originLock = BitmapFactory.decodeResource(getResources(), R.drawable.browser_chain_lock);
            lock = Bitmap.createScaledBitmap(originLock, getWidth()/4-dp2, getHeight()/4-dp2, false);
        }
        canvas.drawBitmap(lock, getWidth()/8*3, getHeight()/8*3, null);
    }

}
