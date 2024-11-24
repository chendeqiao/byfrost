package com.intelligence.browser.markLock.lock;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.browser.markLock.ApplicationUtils;
import com.intelligence.commonlib.tools.DisplayUtil;

public class DarkBGView extends View{


    public DarkBGView(Context context) {
        super(context);
        if(ApplicationUtils.isIndividualPrivacyApp(context))
            setBackgroundColor(Color.WHITE);
        else
            setBackgroundResource(R.drawable.browser_bg_main_gradient);
    }
    public DarkBGView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if(ApplicationUtils.isIndividualPrivacyApp(context))
            setBackgroundColor(Color.WHITE);
        else
            setBackgroundResource(R.drawable.browser_bg_main_gradient);
    }
    public DarkBGView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(ApplicationUtils.isIndividualPrivacyApp(context))
            setBackgroundColor(Color.WHITE);
        else
            setBackgroundResource(R.drawable.browser_bg_main_gradient);
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                DisplayUtil.getScreenHeight(getContext())-DisplayUtil.getStatusBarHeight(getContext())
                , MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }




    private Bitmap bottomBmp;


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(ApplicationUtils.isIndividualPrivacyApp(getContext()))
            return ;


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
