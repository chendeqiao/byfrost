package com.intelligence.browser.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.webview.Tab;
import com.intelligence.browser.ui.home.ToolBar;
import com.intelligence.browser.base.UiController;
import com.intelligence.commonlib.tools.ScreenUtils;

public class ToolbarCenterView extends RelativeLayout {
    private RelativeLayout mWebTitleParent;
    private OnClickListener mClickListener;

    protected TextView mUrlInput;
    private View mIncognitoIcon;
    private boolean mIncognito;

    public ToolbarCenterView(Context context) {
        super(context);
    }

    public ToolbarCenterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mWebTitleParent = findViewById(R.id.title_loading_view);
        mUrlInput = findViewById(R.id.web_view_title_view);
        mIncognitoIcon =  findViewById(R.id.webview_incognito_icon);
        setBackground(getResources().getDrawable(R.drawable.browser_webview_search_url_shape));
    }

    public void setOnItemClickListener(OnClickListener listener) {
        if (listener == null) {
            return;
        }
        mClickListener = listener;
        mWebTitleParent.setOnClickListener(mClickListener);
        mUrlInput.setOnClickListener(mClickListener);
    }

    public void updateStyle(boolean incognito) {
        mIncognito = incognito;
        mIncognitoIcon.setVisibility(mIncognito && mDirection == ToolBar.DIRECTION.DOWN ? VISIBLE : GONE);
    }

    public void setUiController(UiController controller) {
    }

    private ToolBar.DIRECTION mDirection;
    public void startScrollAnimation(ToolBar.DIRECTION direction, float fraction) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)getLayoutParams();
        mDirection = direction;
        switch (direction) {
            case DOWN:
                layoutParams.bottomMargin = ScreenUtils.dpToPxInt(getContext(),(12.0f * (1-fraction)));
                setTextSize(fraction);
                mIncognitoIcon.setVisibility(mIncognito?VISIBLE:GONE);
                break;
            case UP:
                layoutParams.bottomMargin = ScreenUtils.dpToPxInt(getContext(),(12.0f * fraction));
                setTextSize(1.0f - fraction);
                mIncognitoIcon.setVisibility(GONE);
                break;
        }

        setLayoutParams(layoutParams);
    }

    private void setTextSize(float fraction) {
        if (mUrlInput != null) {
            mUrlInput.setScaleX(0.9f + (0.1f*fraction));
            mUrlInput.setScaleY(0.9f + (0.1f*fraction));
//            mUrlInput.setTextSize(2 * fraction + 10);
//            mUrlInput.getLayoutParams().width = ScreenUtils.dpToPxInt(getContext(), 68) + ScreenUtils.dpToPxInt(getContext(),(int) (11 * fraction));
        }
        if (getBackground() != null) {
            getBackground().setAlpha((int) (255 * fraction));
        }
    }

   public void setUrlTitle(String title) {
       if (title == null) {
           return;
       }
       if (mUrlInput != null && !title.equals(mUrlInput.getText().toString())) {
           mUrlInput.setText(title);
       }
   }

    public void setIsSearchResultPage(boolean isSearchResultPage) {
    }

    public void onPageLoadFinished(boolean isSearchResultPage, Tab tab) {
    }

    public String getTitle() {
        return mUrlInput.getText().toString();
    }

    public void onConfigurationChanged(Configuration config) {
    }
}
