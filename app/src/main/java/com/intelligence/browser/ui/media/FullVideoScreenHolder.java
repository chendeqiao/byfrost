package com.intelligence.browser.ui.media;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.browser.ui.webview.WebviewVideoPlayerLayer;

public class FullVideoScreenHolder extends FrameLayout implements WebviewVideoPlayerLayer.Listener {
    private boolean mLockerState = false;

    public FullVideoScreenHolder(Context context, @Nullable AttributeSet attrs) {
        super(context,attrs);
        setBackgroundColor(context.getResources().getColor(R.color.transparent));
    }

    public FullVideoScreenHolder(Context ctx) {
        super(ctx);
        setBackgroundColor(ctx.getResources().getColor(R.color.transparent));
    }

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (mLockerState && (event.getKeyCode() == KeyEvent.KEYCODE_BACK
//                || event.getKeyCode() == KeyEvent.KEYCODE_MENU)) {
//            return true;
//        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setLockerState(boolean lockerstate) {
        mLockerState = lockerstate;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // dispatch to the last child first, then one by one to the other child
        if (this.getChildCount() != 0) {
            getChildAt(this.getChildCount() - 1).dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}
