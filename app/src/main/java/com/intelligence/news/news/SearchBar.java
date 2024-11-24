package com.intelligence.news.news;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.SchemeUtil;

public class SearchBar extends FrameLayout {
    public SearchBar(@NonNull Context context) {
        this(context, null, 0);
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(getContext(), R.layout.browser_search_bar, this);
        initView();
    }

    private void initView() {
        findViewById(R.id.search_bar_camera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getContext() instanceof Activity) {
//                    SchemeUtil.jumpToScheme(getContext(), SchemeUtil.BROWSER_SCHEME_PATH_QRCODE);
//                    ((Activity) getContext()).finish();
                }
            }
        });
    }
}