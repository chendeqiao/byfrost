package com.intelligence.browser.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelligence.browser.ui.activity.BrowserProductActivity;
import com.intelligence.browser.R;
import com.intelligence.browser.downloads.BrowserDownloadManager;
import com.intelligence.browser.ui.update.UpdateDialog;
import com.intelligence.browser.utils.ActivityUtils;
import com.intelligence.browser.utils.ChannelUtil;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

public class BrowserAboutFragment extends BasePreferenceFragment implements View.OnClickListener {
    private static final int CLICK_COUNT = 4;
    private static final long CLICK_INTERVAL_MAX_TIME = 3000;

    private int mChannelCount = 0;
    private long mPreTime = 0;
    private LinearLayout mView;
    private onFragmentCallBack mCallBack;
    private String mKey = "";
    private View mFiveStar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        mView = new LinearLayout(getActivity());
        mView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        initView(mView);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        setBrowserActionBarTitle(getText(R.string.pref_about).toString());
    }

    private View mContactUs;
    private View mUpdateTips;
    private void initView(LinearLayout view) {
        Bundle arguments = getArguments();
        if(arguments != null) {
            mKey = arguments.getString(KEY);
        }

        int about_layout = R.layout.browser_about_fragment;
        View v = LayoutInflater.from(getActivity()).inflate(about_layout, null);
        ((TextView) v.findViewById(R.id.version)).setText(getString(R.string.pref_version_summary, DeviceInfoUtils
                .getAppVersionName(getActivity())));
        ((TextView) v.findViewById(R.id.app_name)).setText(getResources().getString(R.string.application_name));
        mContactUs = v.findViewById(R.id.contact_type);
        mUpdateTips = v.findViewById(R.id.update_new_version_tips);
        mFiveStar = v.findViewById(R.id.fivestart_type);
        if (DeviceInfoUtils.getAppVersionCode(getActivity()) < SharedPreferencesUtils.getVersionCode()) {
            mUpdateTips.setVisibility(View.VISIBLE);
        }else {
            mUpdateTips.setVisibility(View.GONE);
        }

        mContactUs.setOnClickListener(this);
        mFiveStar.setOnClickListener(this);
        v.findViewById(R.id.icon).setOnClickListener(this);
        v.findViewById(R.id.update).setOnClickListener(this);
        v.findViewById(R.id.contact_google).setOnClickListener(this);
        v.findViewById(R.id.contact_twitter).setOnClickListener(this);
        v.findViewById(R.id.contact_facebook).setOnClickListener(this);
        v.findViewById(R.id.contact_instagram).setOnClickListener(this);
        v.findViewById(R.id.agreement).setOnClickListener(this);
        v.findViewById(R.id.privacy_policy).setOnClickListener(this);
        view.addView(v);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        if (newConfig.orientation != mOrientation) {
//            mView.removeAllViews();
//            mOrientation = newConfig.orientation;
//            initView(mView);
//        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.icon:
                if (mPreTime == 0) {
                    mPreTime = System.currentTimeMillis();
                } else if (System.currentTimeMillis() - mPreTime > CLICK_INTERVAL_MAX_TIME) {
                    mChannelCount = 0;
                    mPreTime = System.currentTimeMillis();
                }
                mChannelCount++;
                if (mChannelCount > CLICK_COUNT) {
                    boolean isOpen = (boolean) SharedPreferencesUtils.get(SharedPreferencesUtils.IS_OPEN_DEVERLO_PEROPTIONS, false);
                    if (!isOpen && !ChannelUtil.isOpenPlatform()) {
                        ToastUtil.showLongToastByString(getActivity(), getResources().getString(R.string.show_dev_on));
                        if (mCallBack != null) {
                            mCallBack.onFragmentCallBack(mKey, true);
                        }
                        SharedPreferencesUtils.put(SharedPreferencesUtils.IS_OPEN_DEVERLO_PEROPTIONS, true);
                    }
                    mChannelCount = 0;
                }
                break;
            case R.id.update:
                checkUpdate(v.getContext());
                break;
            case R.id.fivestart_type:
                DeviceInfoUtils.openAppMark(getActivity());
                break;
            case R.id.contact_type:
                openEmail();
                break;
//            case R.id.contact_google:
//                openUrl(getString(R.string.contact_google));
//                break;
//            case R.id.contact_twitter:
//                openUrl(getString(R.string.contact_twitter));
//                break;
//            case R.id.contact_facebook:
//                openUrl(getString(R.string.contact_facebook));
//                break;
//            case R.id.contact_instagram:
//                openUrl(getString(R.string.contact_instagram));
//                break;
            case R.id.agreement:
                openProductActivity(1);
                break;
            case R.id.privacy_policy:
                openProductActivity(0);
//                openUrl(getString(R.string.pref_privacy_policy_url));
                break;
        }
    }

    private void openProductActivity(int type){
        Intent intent = new Intent( getActivity(), BrowserProductActivity.class);
        intent.putExtra("producttype",type);
        getActivity().startActivity(intent);
    }

    /**
     * 打开邮箱发送邮件
     */
    private void openEmail() {
        //GoogleAnalyticsUtil.send(GoogleAnalyticsUtil.buttonEven("Email_About"));
        //CleanApplication.getInstance().getDefaultTracker().send(GoogleAnalyticsUtil.buttonEven("主页面的我分页面-关于-联系我们"));
        // DataReport.getInstance().send(new ButtonEvent("7,4,5"));
//        Intent intent = new Intent(Intent.ACTION_SENDTO);
//        intent.setData(Uri.parse("mailto:" + "634560649@qq.com"));//email 暂时写在了这里
//        startThirdPartyActivity(getActivity(), intent);
        ((BrowserSettingActivity)getActivity()).startFeedback();
    }

    public static boolean startThirdPartyActivity(Context context, Intent intent) {
        boolean result = false;
        if (intent == null) {
            return result;
        }
        if (intent.resolveActivity(context.getPackageManager()) != null) {//存第三方Activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            result = true;
        }
        return result;
    }


    public void setOnFragmentCallBack(onFragmentCallBack callBack) {
        mCallBack = callBack;
    }

    private void openUrl(String url) {
        ActivityUtils.openUrl(getActivity(), url);
    }

    /**
     * 检查更新
     */
//    @TargetApi(Build.VERSION_CODES.M)
    private void checkUpdate(Context context) {
        if (DeviceInfoUtils.getAppVersionCode(getActivity()) < SharedPreferencesUtils.getVersionCode()) {
            showUpdateDialog();
//        DeviceInfoUtils.openAppMark(context);
        } else {
           ToastUtil.show(context.getResources().getString(R.string.is_latest));
        }
        if(mUpdateTips != null) {
            mUpdateTips.setVisibility(View.GONE);
        }
        SharedPreferencesUtils.put(UPDATE_PAGE_CLICK, DeviceInfoUtils.getAppVersionCode(getActivity()));
    }

    public static final String UPDATE_PAGE_CLICK = "update_page_click";

    private void showUpdateDialog() {
        UpdateDialog mUpdateDialog = new UpdateDialog(getActivity());
        mUpdateDialog.setCancelable(false);
        mUpdateDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BrowserDownloadManager.getInstance().onDestroy();
    }
}
