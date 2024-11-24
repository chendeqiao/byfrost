package com.intelligence.commonlib.baseUi;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;

public class MultiTextScrollTextView extends TextSwitcher implements ViewSwitcher.ViewFactory {

    private static final int FLAG_START_AUTO_SCROLL = 0;
    private static final int FLAG_STOP_AUTO_SCROLL = 1;

    private float mTextSize = 12;
    private int mPadding = 5;
    private int textColor = Color.BLACK;

    public void setText(float textSize, int padding, int textColor) {
        mTextSize = textSize;
        mPadding = padding;
        this.textColor = textColor;
    }

    private OnItemClickListener itemClickListener;
    private Context mContext;
    private int currentId = -1;
    private ArrayList<String> textList;
    private Handler handler;

    public MultiTextScrollTextView(Context context) {
        this(context, null);
        mContext = context;
    }

    public MultiTextScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        textList = new ArrayList<String>();
    }

    public void setAnimTime(long animDuration) {
        setFactory(this);
        Animation in = new TranslateAnimation(0, 0, animDuration, 0);
        in.setDuration(animDuration);
        in.setInterpolator(new AccelerateInterpolator());
        Animation out = new TranslateAnimation(0, 0, 0, -animDuration);
        out.setDuration(animDuration);
        out.setInterpolator(new AccelerateInterpolator());
        setInAnimation(in);
        setOutAnimation(out);
    }

    public void setTextStillTime(final long time) {
        if (handler == null) {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case FLAG_START_AUTO_SCROLL:
                            if (textList.size() > 0) {
                                currentId++;
                                setText(textList.get(currentId % textList.size()));
                            }
                            handler.sendEmptyMessageDelayed(FLAG_START_AUTO_SCROLL, time);
                            break;
                        case FLAG_STOP_AUTO_SCROLL:
                            handler.removeMessages(FLAG_START_AUTO_SCROLL);
                            break;
                    }
                }
            };
        } else {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public void setTextList(ArrayList<String> titles) {
        textList.clear();
        textList.addAll(titles);
        currentId = -1;
    }

    public void startAutoScroll() {
        handler.sendEmptyMessage(FLAG_START_AUTO_SCROLL);
    }

    public void stopAutoScroll() {
        handler.sendEmptyMessage(FLAG_STOP_AUTO_SCROLL);
    }

    @Override
    public View makeView() {
        TextView t = new TextView(mContext);
        t.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        t.setMaxLines(1);
        t.setPadding(mPadding, mPadding, mPadding, mPadding);
        t.setTextColor(textColor);
        t.setAlpha(0.6f);
        t.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
        t.setTextSize(mTextSize);

//        t.setClickable(true);
//        t.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (itemClickListener != null && textList.size() > 0 && currentId != -1) {
//                    itemClickListener.onItemClick(currentId % textList.size());
//                }
//            }
//        });
        return t;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}