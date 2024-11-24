package com.intelligence.browser.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intelligence.browser.ui.BaseUi;
import com.intelligence.browser.ui.home.BrowserMainPageController;
import com.intelligence.browser.R;
import com.intelligence.browser.ui.search.SearchEnginePreference;
import com.intelligence.browser.ui.search.SelectEnginePopWindow;
import com.intelligence.browser.utils.ClickUtil;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.InputMethodUtils;

public class SearchCardView extends RelativeLayout implements View.OnClickListener {
    private ImageView mVoiceIcon;
    private ImageView mScanIcon;
    private TextView mText;
    private ImageView mIncognitoIcon;
    private ImageView mSearchEngine;
    private BrowserMainPageController mBrowserMainPageController;
    private boolean mIsClick = true;
    private View mSearchCardView;
    private View mSearchEngineLayout;

    public SearchCardView(Context context, BrowserMainPageController browserMainPageController) {
        super(context);
        mBrowserMainPageController = browserMainPageController;
        initView();
    }

    public void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.browser_search_card_view, this, true);
        setBackgroundResource(R.drawable.browser_search_card_shape);
        mVoiceIcon = findViewById(R.id.voice_icon);
        mScanIcon = findViewById(R.id.searchbar_qr);
        mIncognitoIcon = findViewById(R.id.incognito_icon);
        mText = findViewById(R.id.search_text);
        mSearchCardView = findViewById(R.id.search_card_view);
        mSearchEngine = findViewById(R.id.select_search_engine_icon);
        mSearchEngineLayout = findViewById(R.id.select_search_engine_layout);
        SearchEnginePreference.getDefaultSearchIcon(getContext(), mSearchEngine);
        mSearchEngine.setOnClickListener(this);
        mSearchEngineLayout.setOnClickListener(this);
        mText.setOnClickListener(this);
        mVoiceIcon.setOnClickListener(this);
        mScanIcon.setOnClickListener(this);
        mSearchCardView.setOnClickListener(this);
        setOnClickListener(this);
        setSearchEngineIcon();
        ImageUtils.tinyIconColor(mScanIcon);
        ImageUtils.tinyIconColor(mVoiceIcon);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.voice_icon:
                mBrowserMainPageController.startVoiceRecognizer();
                break;
            case R.id.searchbar_qr:
                mBrowserMainPageController.startScan();
                break;
            case R.id.search_card_view:
            case R.id.search_text:
                if (!ClickUtil.clickShort(view.getId())) {
                    InputMethodUtils.showKeyboard((Activity) getContext());
                    mBrowserMainPageController.setSearchViewMoveStyle();
                }
                break;
            case R.id.select_search_engine_layout:
            case R.id.select_search_engine_icon:
                SelectEnginePopWindow searchEngineWindow = new SelectEnginePopWindow(getContext(), new BaseUi.SelectSearchEngineListener() {
                    @Override
                    public void updateSearchEngine() {
                        SearchEnginePreference.getDefaultSearchIcon(getContext(), mSearchEngine);
                    }
                });
                searchEngineWindow.show(mSearchEngine,1);
                break;
        }
    }

    public void setState(float state) {
//        setClickSearchEngine(state == 1);
    }

    public void setSearchEngineIcon() {
        SearchEnginePreference.getDefaultSearchIcon(getContext(), mSearchEngine);
    }

    public void onResume() {
        setSearchEngineIcon();
    }

    public void setIsCanClick(boolean isCanClick) {
        mIsClick = isCanClick;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !mIsClick;
    }

    public void onIncognito(boolean incognito) {
        mIncognitoIcon.setVisibility(incognito ? VISIBLE : GONE);
    }
}
