package com.intelligence.browser.settings;

import static com.intelligence.browser.settings.PreferenceKeys.PREF_DOWNLOAD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.intelligence.browser.R;
import com.intelligence.browser.settings.multilanguage.LanguageSettingActivity;
import com.intelligence.browser.utils.DefaultBrowserSetUtils;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.settings.multilanguage.LanguageUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

public class SettingsPreferenecesFragment extends BasePreferenceFragment implements Preference
        .OnPreferenceClickListener, Preference.OnPreferenceChangeListener,
        onFragmentCallBack {

    private static final int NO_BROWSER_SET = 0;
    private static final int THIS_BROWSER_SET_AS_DEFAULT = 1;
    private static final int OTHER_BROWSER_SET_AS_DEFAULT = 2;

    private BrowserPreference mEnginePreference;
    private BrowserPreference mDefaultBrwoserPreference;
    private BrowserPreference mMultiPreference;
    private Preference mClearData;
    //    private BrowserDialogPreference mResetDefault;
    private BrowserSettings mSettings;
    private int mDefaultBrowserSetStatus = NO_BROWSER_SET;
    private String[] userAgent;
    private String[] userVideo;
//    private Preference mDeverloperOptionsPref;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mEnginePreference.setSelectValue(mSettings.getSearchEngineName());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the XML preferences file
        addPreferencesFromResource(R.xml.browser_settings_preferences);
        init();
        registerSearchEngineUpgradeListener();
        updateDefaultBrowserSettingStatus();
        SharedPreferencesUtils.put(SharedPreferencesUtils.IS_OPEN_SETTING, DeviceInfoUtils.getAppVersionCode(getActivity()));
    }

    private void init() {
        mSettings = BrowserSettings.getInstance();

        mEnginePreference = (BrowserPreference) findPreference(PreferenceKeys.PREF_SEARCH_ENGINE);
        mEnginePreference.setSelectValue(mSettings.getSearchEngineName());
        mEnginePreference.setOnPreferenceClickListener(this);

        mDefaultBrwoserPreference = (BrowserPreference) findPreference(PreferenceKeys.PREF_DEFAULT_BROWSER);
        String defaultBrowser = DefaultBrowserSetUtils.getDefaultBrowserName(getActivity());
        mDefaultBrwoserPreference.setSelectValue(defaultBrowser);
        mDefaultBrwoserPreference.setOnPreferenceClickListener(this);

        mMultiPreference = (BrowserPreference) findPreference(PreferenceKeys.PREF_MULTI_LANGUAGE);
        String defaultLanguage = LanguageUtil.getLanguageName(getActivity());
        mMultiPreference.setSelectValue(defaultLanguage);
        mMultiPreference.setOnPreferenceClickListener(this);

        mClearData = findPreference(PreferenceKeys.PREF_CLEAR_DATA);
        mClearData.setOnPreferenceClickListener(this);

        Preference preference = findPreference(PreferenceKeys.PREF_AD_BLOCK);
        preference.setOnPreferenceClickListener(this);

        preference = findPreference(PreferenceKeys.PREF_CONFIRM_ON_EXIT);
        preference.setOnPreferenceChangeListener(this);

//        preference = findPreference(PreferenceKeys.PREF_RED_BADGE_NOTIFY);
//        preference.setOnPreferenceChangeListener(this);

//        preference = findPreference(PreferenceKeys.PREF_HEADER_NOTIFY);
//        preference.setOnPreferenceChangeListener(this);

        preference = findPreference(PreferenceKeys.PREF_TEXT_SIZE);
        preference.setOnPreferenceClickListener(this);

        preference = findPreference(PREF_DOWNLOAD);
        preference.setOnPreferenceClickListener(this);

        preference = findPreference(PreferenceKeys.PREF_USER_AGENT);
        preference.setPersistent(false);
        userAgent = getResources().getStringArray(R.array.pref_user_agent_choices);
        String value = userAgent[mSettings.getUserAgent()];
        ((BrowserPreference) preference).setSelectValue(value);
        preference.setOnPreferenceClickListener(this);

        preference = findPreference(PreferenceKeys.PREF_USER_VIDEO);
        preference.setPersistent(false);
        userVideo = getResources().getStringArray(R.array.pref_user_video_choices);
        String videostate = userVideo[mSettings.getUserVideo()];
        ((BrowserPreference) preference).setSelectValue(videostate);
        preference.setOnPreferenceClickListener(this);

//        preference = findPreference(PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING);
//        ((BrowserPreference) preference).setDeviderVisibility(View.GONE);
//        value = mSettings.getDefaultTextEncoding();
//        ((BrowserPreference) preference).setSelectValue(value);
//        preference.setOnPreferenceClickListener(this);

//        preference = findPreference(PreferenceKeys.PREF_SHOW_STATUS_BAR);
//        preference.setOnPreferenceChangeListener(this);

        preference = findPreference(PreferenceKeys.PREF_NOTIFICATION_TOOL_SHOW);
        ((BrowserSwitchPreference) preference).setChecked(mSettings.getNotificationToolShow());
        preference.setOnPreferenceChangeListener(this);

        preference = findPreference(PreferenceKeys.PREF_SWIPE_WEBVIEW);
        ((BrowserSwitchPreference) preference).setChecked(mSettings.getSwipeState());
        preference.setOnPreferenceChangeListener(this);

        preference = findPreference(PreferenceKeys.PREF_PRIVACY_SECURITY);
        preference.setOnPreferenceClickListener(this);

//        preference = findPreference(PreferenceKeys.PREF_ADVANCED);
//        ((BrowserPreference) preference).setDeviderVisibility(View.GONE);
//        preference.setOnPreferenceClickListener(this);


//        preference = findPreference(PreferenceKeys.PREF_DEFAULT_BROWSER);
//        if (!DefaultBrowserSetUtils.canSetDefaultBrowser(getActivity())) {
//            getPreferenceScreen().removePreference(preference);
//        } else {
//            preference.setOnPreferenceChangeListener(this);
//        }

        preference = findPreference(PreferenceKeys.PREF_FORCE_USERSCALABLE);
        ((BrowserSwitchPreference) preference).setDeviderVisibility(View.GONE);

        preference = findPreference(PreferenceKeys.PREF_APP_NAME);
        ((BrowserPreference) preference).setDeviderVisibility(View.GONE);
        preference.setOnPreferenceClickListener(this);

        preference = findPreference(PreferenceKeys.PREF_FEEDBACK);
        preference.setOnPreferenceClickListener(this);

//        mDeverloperOptionsPref = findPreference(PreferenceKeys.PREF_DEVERLOPER_OPTIONS);
//        boolean isOpen = (boolean) SharedPreferencesUtils.get(SharedPreferencesUtils.IS_OPEN_DEVERLO_PEROPTIONS, false);
//        if (!isOpen && !ChannelUtil.isOpenPlatform()) {
//            getPreferenceScreen().removePreference(mDeverloperOptionsPref);
//        }
//        mDeverloperOptionsPref.setOnPreferenceClickListener(this);

//        preference = findPreference(PreferenceKeys.PREF_APP_SCORE);
//        if (ChannelUtil.isGoogleChannel()) {
//            preference.setOnPreferenceClickListener(this);
//        } else {
//            getPreferenceScreen().removePreference(preference);
//        }

//        mResetDefault = (BrowserDialogPreference) findPreference(
//                PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES);
//        mResetDefault.setOnPreferenceChangeListener(this);
//        mResetDefault.setOnPreferenceClickListener(this);

    }

    private void updateDefaultBrowserSettingStatus() {
        Activity activity = getActivity();
//
        String defaultBrowser = DefaultBrowserSetUtils.getDefaultBrowserName(getActivity());
        ((BrowserPreference) findPreference(PreferenceKeys
                .PREF_DEFAULT_BROWSER)).updateSelectValue(defaultBrowser);
    }

    @Override
    public void onPause() {
//        mResetDefault.onPause();
        super.onPause();
    }


    @Override
    public void onResume() {
        super.onResume();
        setBrowserActionBarTitle(getActivity().getTitle().toString());
        updateDefaultBrowserSettingStatus();
        updateMultiLanguage();
    }

    private void updateMultiLanguage(){
        String languageName = LanguageUtil.getLanguageName(getActivity());
        ((BrowserPreference) findPreference(PreferenceKeys
                .PREF_MULTI_LANGUAGE)).updateSelectValue(languageName);
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        Fragment newFragment;
        Bundle bundle;
        switch (preference.getKey()) {
            case PreferenceKeys.PREF_SEARCH_ENGINE:
                BrowserRadioFragment browserRadioFragment = new BrowserRadioFragment();
                bundle = new Bundle();
                bundle.putCharSequence(BrowserRadioFragment.KEY, PreferenceKeys.PREF_SEARCH_ENGINE);
                browserRadioFragment.setArguments(bundle);
                browserRadioFragment.setOnFragmentCallBack(new onFragmentCallBack() {
                    @Override
                    public void onFragmentCallBack(String key, Object newValue) {
                        mEnginePreference.setSelectValue((String) newValue);
                        BrowserSettings.getInstance().setSearchEngineName((String) newValue);
                    }
                });
                startFragment(browserRadioFragment);
                break;
            case PreferenceKeys.PREF_DEFAULT_BROWSER:
//                setDefaultBrowser();
                DefaultBrowserSetUtils.setDefaultBrowser(getActivity());
                break;
//            case PreferenceKeys.PREF_APP_SCORE:
//                //todo 评价我们
//                break;
            case PreferenceKeys.PREF_TEXT_SIZE:
                newFragment = new BrowserFontSizeFragment();
                startFragment(newFragment);
                break;
            case PreferenceKeys.PREF_DOWNLOAD:
                BrowserDownloadSettingFragment browserDownloadSettingFragment = new BrowserDownloadSettingFragment();
                startFragment(browserDownloadSettingFragment);
                break;
            case PreferenceKeys.PREF_USER_AGENT:
                BrowserRadioFragment browserRadioFragment1 = new BrowserRadioFragment();
                bundle = new Bundle();
                bundle.putCharSequence(BrowserRadioFragment.KEY, preference.getKey());
                browserRadioFragment1.setArguments(bundle);
                browserRadioFragment1.setOnFragmentCallBack(this);
                startFragment(browserRadioFragment1);
                break;
            case PreferenceKeys.PREF_USER_VIDEO:
                BrowserRadioFragment browserVideoRadioFragment = new BrowserRadioFragment();
                bundle = new Bundle();
                bundle.putCharSequence(BrowserRadioFragment.KEY, preference.getKey());
                browserVideoRadioFragment.setArguments(bundle);
                browserVideoRadioFragment.setOnFragmentCallBack(this);
                startFragment(browserVideoRadioFragment);
                break;
            case PreferenceKeys.PREF_PRIVACY_SECURITY:
                newFragment = new PrivacySecurityPreferencesFragment();
                startFragment(newFragment);
                break;
//            case PreferenceKeys.PREF_ADVANCED:
//                BrowserAnalytics.trackEvent(BrowserAnalytics.Event.SETTING_EVENT, AnalyticsSettings
//                        .ID_ADVANCED);
//                newFragment = new AdvancedPreferencesFragment();
//                startFragment(newFragment);
//                break;
//            case PreferenceKeys.PREF_DEVERLOPER_OPTIONS:
//                newFragment = new DebugFragment();
//                startFragment(newFragment);
//                break;
            case PreferenceKeys.PREF_APP_NAME:
                BrowserAboutFragment browserAboutFragment = new BrowserAboutFragment();
                bundle = new Bundle();
                bundle.putCharSequence(BrowserAboutFragment.KEY, preference.getKey());
                browserAboutFragment.setArguments(bundle);
                browserAboutFragment.setOnFragmentCallBack(this);
                startFragment(browserAboutFragment);

                break;
            case PreferenceKeys.PREF_FEEDBACK:
                newFragment = new BrowserFeedbackFragment();
                startFragment(newFragment);
                break;
            case PreferenceKeys.PREF_CLEAR_DATA:
                newFragment = new BrowserClearDataPreferencesFragment();
                startFragment(newFragment);
                break;
            case PreferenceKeys.PREF_MULTI_LANGUAGE:
                LanguageSettingActivity.launch(getActivity());
                break;
//            case PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES:
//                break;
            case PreferenceKeys.PREF_AD_BLOCK:
                newFragment = new BrowserAdBlockSettingsFragment();
                startFragment(newFragment);
                break;
        }

        return false;
    }

    /**
     * on click setDefaultBrowser option
     */
    private void setDefaultBrowser() {
        Activity activity = getActivity();
        switch (mDefaultBrowserSetStatus) {
            case NO_BROWSER_SET:
//                 TODO: 2019/8/19 设置默认浏览器
//                break;
            case THIS_BROWSER_SET_AS_DEFAULT:
            case OTHER_BROWSER_SET_AS_DEFAULT:
                ResolveInfo defaultBrowser = DefaultBrowserSetUtils.findDefaultBrowser(activity);
                startFragment(BrowserClearDefaultBrowserFragment.create(defaultBrowser));
                break;
            default:
                //do nothing
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            /*case PreferenceKeys.PREF_AD_BLOCK:
                BrowserAnalytics.trackEvent(BrowserAnalytics.Event.SETTING_EVENT, AnalyticsSettings
                        .ID_ADBLOCK);

                BrowserAnalytics.trackEvent(BrowserAnalytics.Event.ADBLOCK_EVENTS, (boolean) newValue ?
                        AnalyticsSettings.ID_ON : AnalyticsSettings.ID_OFF);
                return true;*/
            case PreferenceKeys.PREF_RESET_DEFAULT_PREFERENCES:
//                Intent intent = new Intent();
//                intent.putExtra(Intent.EXTRA_TEXT, preference.getKey());
//                getActivity().setResult(Activity.RESULT_OK, intent);
//                getActivity().finish();
                break;
//            case PreferenceKeys.PREF_DEFAULT_BROWSER:
//                setDefaultBrowser();
//                break;
//            case PreferenceKeys.PREF_SHOW_STATUS_BAR:
//                if (!(boolean) newValue) {
//                    SharedPreferencesUtils.put(SharedPreferencesUtils.IS_SHOW_STATUS_BAR,false);
//                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
//                            .LayoutParams.FLAG_FULLSCREEN);
//                } else {
//                    SharedPreferencesUtils.put(SharedPreferencesUtils.IS_SHOW_STATUS_BAR,true);
//                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                }
//                return true;
            case PreferenceKeys.PREF_NOTIFICATION_TOOL_SHOW:
                // 权限已经授予
                try {
                    if ((boolean) newValue) {
                        if (requestNotifyPermission()) {
                            ((BrowserSwitchPreference) preference).setChecked(false);
                            break;
                        }
                        mSettings.showNotification();
                    } else {
                        mSettings.hideNotification();
                    }
                    SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_NOTIFY_BAR_SHOW,true);
                } catch (Exception e) {

                }
                break;
            case PreferenceKeys.PREF_SWIPE_WEBVIEW:
                // 权限已经授予
                try {
                    if ((boolean) newValue) {
                        ((BrowserSwitchPreference) preference).setChecked(false);
                        break;
                    }
                } catch (Exception e) {
                }
                break;
            case PreferenceKeys.PREF_CONFIRM_ON_EXIT:
                return true;
//            case PreferenceKeys.PREF_RED_BADGE_NOTIFY:
//                return true;
//            case PreferenceKeys.PREF_HEADER_NOTIFY:
//                if ((boolean) newValue) {
//                    SharedPreferencesUtils.put(SharedPreferencesUtils.IS_SHOW_HEADER_HOTWORD,true);
//                } else {
//                    SharedPreferencesUtils.put(SharedPreferencesUtils.IS_SHOW_HEADER_HOTWORD,false);
//                }
//                return true;
        }
        return true;
    }

    public void openNotifyBar() {
        if (mSettings != null) {
            mSettings.showNotification();
        }
    }

    public static final int NOTIFY_PERMISSION_REQUEST_CODE = 1012;
    public static final int NOTIFY_PERMISSION_HOME_REQUEST_CODE = 1016;

    public void setDownloadPath(Preference preference, String downloadPath) {
        preference.setSummary(downloadPath);
    }

    public void registerSearchEngineUpgradeListener() {
    }

    @Override
    public void finish() {
        super.finish();
        registerSearchEngineUpgradeListener();
    }

    @Override
    public void onFragmentCallBack(String key, Object object) {
        String value;
        switch (key) {
            case PreferenceKeys.PREF_CUSTOM_DOWNLOAD_PATH:
                value = (String) object;
                if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(key)) {
                    setDownloadPath(findPreference(key), value);
                }
                break;
            case PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING:
                value = (String) object;
                if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(key)) {
                    ((BrowserPreference) findPreference(key)).setSelectValue(value);
                    BrowserSettings.getInstance().setDefaultTextEncoding(value);
                }
                break;
            case PreferenceKeys.PREF_USER_AGENT:
                value = (String) object;
                if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(key)) {
                    ((BrowserPreference) findPreference(key)).setSelectValue(userAgent[Integer.parseInt(value)]);
                    mSettings.setUserAgent(value);
                }
                break;
            case PreferenceKeys.PREF_USER_VIDEO:
                value = (String) object;
                if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(key)) {
                    ((BrowserPreference) findPreference(key)).setSelectValue(userVideo[Integer.parseInt(value)]);
                    mSettings.setUserVideo(value);
                }
                break;
//            case PreferenceKeys.PREF_APP_NAME:
//                boolean isFlog = (boolean) object;
//                if (isFlog && mDeverloperOptionsPref != null) {
//                    getPreferenceScreen().addPreference(mDeverloperOptionsPref);
//                }
//                break;
        }
    }

    private boolean requestNotifyPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFY_PERMISSION_REQUEST_CODE);
                return true;
            }
        }
        return false;
    }

    private void showPermissionDeniedDialog(Activity mActivity, String message) {
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.POST_NOTIFICATIONS)) {
                            requestNotifyPermission();
                            return;
                        }
                        // 跳转到应用程序的权限设置页面
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                        intent.setData(uri);
                        mActivity.startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFY_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openNotifyBar();
                BrowserSwitchPreference preference = (BrowserSwitchPreference) findPreference(PreferenceKeys
                        .PREF_NOTIFICATION_TOOL_SHOW);
                ((BrowserSwitchPreference) preference).setChecked(true);
            } else {
                BrowserSwitchPreference preference = (BrowserSwitchPreference) findPreference(PreferenceKeys
                        .PREF_NOTIFICATION_TOOL_SHOW);
                ((BrowserSwitchPreference) preference).setChecked(false);
                showPermissionDeniedDialog(getActivity(), getActivity().getResources().getString(R.string.browser_permission_notification));
            }
        }
    }
}
