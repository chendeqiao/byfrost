package com.intelligence.browser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView {

    public MarqueeTextView(Context context) {
        super(context);
//        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        init();
    }

    @Override

    public boolean isFocused() {
        return true;
    }
}
