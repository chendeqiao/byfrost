package com.intelligence.browser.markLock.lock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.intelligence.browser.R;


public class BGLinearLayout extends LinearLayout{


    public BGLinearLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        setBackgroundResource(R.drawable.browser_bg_main_gradient);
    }

    public BGLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        setBackgroundResource(R.drawable.browser_bg_main_gradient);
    }

    public BGLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        setBackgroundResource(R.drawable.browser_bg_main_gradient);
    }


    private Bitmap bottomBmp;


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(getWidth()==0 || getHeight()==0)
            return ;

        if(bottomBmp==null) {
            bottomBmp = BGBmp.getCommonBGBmp();
            if(bottomBmp==null){
//                Bitmap originBmp = ((BitmapDrawable) getResources().getDrawable(R.drawable.bg_tree_bottom)).getBitmap();
//                bottomBmp = Bitmap.createScaledBitmap(originBmp, getWidth(),  (int) ((float) getWidth() / 72f * 35f), false);
//                BGBmp.setCommonBGBmp(bottomBmp);
            }
        }

        if(bottomBmp != null)
            canvas.drawBitmap(bottomBmp, 0, getHeight()-bottomBmp.getHeight(), null);
    }

}
