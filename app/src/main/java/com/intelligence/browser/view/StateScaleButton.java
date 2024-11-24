package com.intelligence.browser.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.intelligence.browser.R;


public class StateScaleButton extends Button implements View.OnTouchListener{

    private float mScaleFactor = 0.96f;
    public StateScaleButton(Context context) {
        super(context);
        init();
    }

    public StateScaleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        parseAttrs(context,attrs);
    }

    public StateScaleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        parseAttrs(context,attrs);
    }

    private void init() {
        setOnTouchListener(this);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        //Load from custom attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Base_State_Scale_Button);
        if (a == null) return;
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.Base_State_Scale_Button_scalefactor) {
                mScaleFactor = a.getFloat(attr, mScaleFactor);
            }
        }
        a.recycle();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.setScaleX(mScaleFactor);
                this.setScaleY(mScaleFactor);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                this.setScaleX(1);
                this.setScaleY(1);
                break;
        }
        return false;
    }
}
