package com.intelligence.news.news.cards;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class NoNullSpaceTextView extends TextView {
    public NoNullSpaceTextView(Context context) {
        super(context);
    }

    public NoNullSpaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoNullSpaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setText(CharSequence text, BufferType type) {
        if (text != null && !TextUtils.isEmpty(text.toString().trim())) {
            this.setVisibility(VISIBLE);
        } else {
            this.setVisibility(GONE);
        }

        super.setText(text, type);
    }
}
