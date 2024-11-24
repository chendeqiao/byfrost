package com.intelligence.browser.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.ui.widget.BrowserSwitchButton;

import java.util.ArrayList;
import java.util.Collections;

public class BrowserAdBlockSettingsFragment extends BasePreferenceFragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, onFragmentCallBack {

    private BrowserSettings mSettings;

    private BrowserSwitchButton mSwitchButton;
    private TextView mTvImgAdBlockCount;
    private TextView mTvJsAdBlockCount;
    private TextView mTvPopupAdBlockCount;
    private TextView mClear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.browser_fragment_adblock_settings, container, false);
        setBrowserActionBarTitle(getString(R.string.pref_ad_block));
        mSettings = BrowserSettings.getInstance();
        init(view);
        return view;
    }

    private TextView mLimiteTime;
    private String getTimeLite(String position){
        ArrayList<String> array = getListData(getResources().getStringArray(R.array.pref_default_ad_time_limite));
        return array.get(Integer.parseInt(position));
    }

    private ArrayList<String> getListData(String[] array) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, array);
        return list;
    }

    private void init(View rootView) {
        mSwitchButton = rootView.findViewById(R.id.switch_button);
        mTvImgAdBlockCount = rootView.findViewById(R.id.pic_ad_block_count_text);
        mTvJsAdBlockCount = rootView.findViewById(R.id.js_ad_block_count_text);
        mTvPopupAdBlockCount = rootView.findViewById(R.id.popup_ad_block_count_text);
        mLimiteTime = rootView.findViewById(R.id.ad_limite_time_text);
        mLimiteTime.setText(getTimeLite(BrowserSettings.getInstance().getDefaultAdTimeLite()));
        mClear = rootView.findViewById(R.id.clear);

        mSwitchButton.setFocusable(false);
        mSwitchButton.setChecked(mSettings.getAdBlockEnabled());
        mSwitchButton.setOnCheckedChangeListener(this);

        refreshAdBlockCount();

        mClear.setOnClickListener(this);
        rootView.findViewById(R.id.edit_rules_layout).setOnClickListener(this);
        rootView.findViewById(R.id.set_js_disable_layout).setOnClickListener(this);

    }


    private void refreshAdBlockCount() {
        mTvImgAdBlockCount.setText(getString(R.string.ad_block_num, mSettings.getImgAdBlockCount()));
        mTvJsAdBlockCount.setText(getString(R.string.ad_block_num, mSettings.getJsAdBlockCount()));
        mTvPopupAdBlockCount.setText(getString(R.string.ad_block_num, mSettings.getPopupAdBlockCount()));
        mClear.setEnabled(mSettings.getAdBlockCount() > 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                mSettings.clearAdBlockCount();
                refreshAdBlockCount();
                break;
            case R.id.edit_rules_layout:
                BrowserEditAdBlockRuleFragment browserEditAdBlockRuleFragment = new BrowserEditAdBlockRuleFragment();
                startFragment(browserEditAdBlockRuleFragment);
                break;
            case R.id.set_js_disable_layout:
//                EditJsDisableHostsFragment editJsDisableHostsFragment = new EditJsDisableHostsFragment();
//                startFragment(editJsDisableHostsFragment);
                BrowserRadioFragment browserRadioFragment1 = new BrowserRadioFragment();
                Bundle bundle;
                bundle = new Bundle();
                bundle.putCharSequence(BrowserRadioFragment.KEY, PreferenceKeys.PREF_DEFAULT_AD_TIME);
                browserRadioFragment1.setArguments(bundle);
                browserRadioFragment1.setOnFragmentCallBack(this);
                startFragment(browserRadioFragment1);
                break;
            default:
                //do nothing
        }
    }

    @Override
    public void onFragmentCallBack(String key, Object object) {
        if (PreferenceKeys.PREF_DEFAULT_AD_TIME.equals(key)) {
            mLimiteTime.setText(getTimeLite((String) object));
            BrowserSettings.getInstance().setDefaultAdTimeLite((String) object);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mSettings.setAdBlockEnabled(isChecked);
    }
}
