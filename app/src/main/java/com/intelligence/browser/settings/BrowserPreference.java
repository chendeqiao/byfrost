package com.intelligence.browser.settings;

import android.content.Context;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.ui.search.SearchEnginePreference;

public class BrowserPreference extends Preference {

    private int mVisibility;
    private String mSelectValue;
    private ImageView mSelectIcon;


    public BrowserPreference(Context context) {
        super(context);
    }

    public BrowserPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrowserPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public String getSelectValue() {
        return mSelectValue;
    }

    public void setSelectValue(String selectValue) {
        if (!TextUtils.isEmpty(selectValue)) {
            mSelectValue = selectValue;
            try {
                if (mSelectIcon != null) {
                    SearchEnginePreference.setSearchEngineIcon(getContext(), mSelectIcon, mSelectValue);
                }
            }catch (Exception e){

            }
        }
    }

    public void updateSelectValue(String selectValue) {
        mSelectValue = selectValue;
        if (mSelectTextView != null && !TextUtils.isEmpty(mSelectValue)) {
            mSelectTextView.setText(mSelectValue);
        }
    }

    public void setDeviderVisibility(int visibility) {
        this.mVisibility = visibility;
    }

    private TextView mSelectTextView;
    protected void onBindView(View view) {
        super.onBindView(view);

        TextView select = view.findViewById(R.id.select_value);
        if (select != null && !TextUtils.isEmpty(mSelectValue)) {
            select.setText(mSelectValue);
        }
        mSelectTextView = select;

        mSelectIcon = view.findViewById(R.id.select_icon);
        if (mSelectIcon != null) {
            try {
                SearchEnginePreference.setSearchEngineIcon(getContext(), mSelectIcon, mSelectValue);
            } catch (Exception e) {
            }
        }

        ImageView imageView = view.findViewById(R.id.divider);

        if (imageView != null) {
            imageView.setVisibility(mVisibility);
        }

//        BadgeView redPoint = view.findViewById(R.id.setting_item_badgeview);
//        redPoint.setVisibility(View.VISIBLE);
//        redPoint.showCircleDotBadge();
    }

}
