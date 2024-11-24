package com.intelligence.browser.ui.home.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.browser.data.TipsCardInfo;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.settings.SettingsPreferenecesFragment;
import com.intelligence.browser.utils.DefaultBrowserSetUtils;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.commonlib.tools.CollectionUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import java.util.ArrayList;

public class CardTipsView extends FrameLayout {
    private RecyclerView mCardTipsView;
    private ArrayList<TipsCardInfo> mTipsCards;
    private BrowserCardTipsAdapter mBrowserCardTipsAdapter;

    public CardTipsView(FragmentActivity activity) {
        this(activity,null);
    }

    public CardTipsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardTipsView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context activity) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.browser_card_tips_layout, this);
        mCardTipsView = findViewById(R.id.browser_card_tips_view);
    }

    public void notifyTipsCardData(){
        if(CollectionUtils.isEmpty(mTipsCards)){
            setVisibility(GONE);
        }
        if(mCardTipsNotifyState != null){
            mCardTipsNotifyState.cardTipsNotifyState();
        }
        if(mBrowserCardTipsAdapter != null){
            mBrowserCardTipsAdapter.notifyDataSetChanged();
        }
    }

    public boolean isShowCardTips(){
        return getVisibility() == VISIBLE;
    }

    public boolean notifyTipsCardState() {
        mTipsCards = getTipsCardData();
        if (CollectionUtils.isEmpty(mTipsCards)) {
            setVisibility(GONE);
            return false;
        }
        mCardTipsView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        mBrowserCardTipsAdapter = new BrowserCardTipsAdapter(getContext());
        mBrowserCardTipsAdapter.setOnCardTipsClickListener(new BrowserCardTipsAdapter.OnCardTipsClickListener() {
            @Override
            public void onCloseCard(TipsCardInfo cardInfo) {
                try {
                    if (cardInfo != null) {
                        int position = mTipsCards.indexOf(cardInfo);
                        mTipsCards.remove(position);
                        mBrowserCardTipsAdapter.notifyItemRemoved(position);
                        if (cardInfo.getTypeId() == TipsCardInfo.CARD_TYPE_NOTIFY) {
                            SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_NOTIFY_BAR_SHOW, true);
                        } else if (cardInfo.getTypeId() == TipsCardInfo.CARD_TYPE_AD) {
                            SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_AD_HOME_PAGE_TIME, System.currentTimeMillis());
                        } else if (cardInfo.getTypeId() == TipsCardInfo.CARD_TYPE_DEFAULT_BROWSER) {
                            SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_DEFAULT_HOME_PAGE_TIME, true);
                        } else if (cardInfo.getTypeId() == TipsCardInfo.CARD_TYPE_LANGUAGE) {
                            SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_SELECT_LANGUAGE_SHOW, true);
                        }
                        notifyTipsCardData();
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onOpenCard(TipsCardInfo cardInfo) {
                if (cardInfo != null) {
                    try {
                        int position = mTipsCards.indexOf(cardInfo);
                        mTipsCards.remove(position);
                        mBrowserCardTipsAdapter.notifyItemRemoved(position);
                        if (cardInfo.getTypeId() == TipsCardInfo.CARD_TYPE_NOTIFY && !requestNotifyPermission()) {
                            BrowserSettings.getInstance().showNotification();
                            BrowserSettings.getInstance().setNotificationToolShow();
                            SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_NOTIFY_BAR_SHOW, true);
                        } else if (cardInfo.getTypeId() == TipsCardInfo.CARD_TYPE_AD) {
                            SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_AD_HOME_PAGE_TIME, System.currentTimeMillis());
                        } else if (cardInfo.getTypeId() == TipsCardInfo.CARD_TYPE_DEFAULT_BROWSER) {
                            DefaultBrowserSetUtils.setDefaultBrowser((Activity) getContext());
                            SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_DEFAULT_HOME_PAGE_TIME, true);
                        } else if (cardInfo.getTypeId() == TipsCardInfo.CARD_TYPE_LANGUAGE) {
                            SharedPreferencesUtils.put(SharedPreferencesUtils.SETTING_SELECT_LANGUAGE_SHOW, true);
                        }
                        notifyTipsCardData();
                    }catch (Exception e){
                    }
                }
            }
        });
        mCardTipsView.setAdapter(mBrowserCardTipsAdapter);
        mBrowserCardTipsAdapter.setData(mTipsCards);
        setVisibility(VISIBLE);
        return true;
    }


    private ArrayList getTipsCardData() {
        ArrayList arrayList = new ArrayList();
        boolean defaultBrowserShow = (boolean)SharedPreferencesUtils.get(SharedPreferencesUtils.SETTING_DEFAULT_HOME_PAGE_TIME, false);
        String defaultBrowser = DefaultBrowserSetUtils.getDefaultBrowserName(getContext());
        int webviewOpenCount = SharedPreferencesUtils.getWebviewOpenCount();
        if (!defaultBrowserShow && !DeviceInfoUtils.getAppName(getContext()).equals(defaultBrowser) && webviewOpenCount > 5) {
            TipsCardInfo tipsCardInfo = new TipsCardInfo();
            tipsCardInfo.setTypeId(TipsCardInfo.CARD_TYPE_DEFAULT_BROWSER);
            tipsCardInfo.setResId(R.drawable.bg_notify_search_home_card);
            tipsCardInfo.setTips(getContext().getResources().getString(R.string.open));
            tipsCardInfo.setTitle(getContext().getResources().getString(R.string.set_default_browser));
            arrayList.add(tipsCardInfo);
        }

        boolean isShowNotifyBar = (boolean) SharedPreferencesUtils.get(SharedPreferencesUtils.SETTING_NOTIFY_BAR_SHOW, false);

        if (!BrowserSettings.getInstance().getNotificationToolShow() && !isShowNotifyBar && webviewOpenCount > 10) {
            TipsCardInfo tipsCardInfo = new TipsCardInfo();
            tipsCardInfo.setTypeId(TipsCardInfo.CARD_TYPE_NOTIFY);
            tipsCardInfo.setResId(R.drawable.bg_notify_search_home_card);
            tipsCardInfo.setBotton(getContext().getResources().getString(R.string.browser_setting_notify_button));
            tipsCardInfo.setTitle(getContext().getResources().getString(R.string.browser_setting_notify_title));
            arrayList.add(tipsCardInfo);
        }

        boolean isSelectLanguage = (boolean) SharedPreferencesUtils.get(SharedPreferencesUtils.SETTING_SELECT_LANGUAGE_SHOW, false);
        if (!isSelectLanguage) {
            TipsCardInfo tipsCardInfo = new TipsCardInfo();
            tipsCardInfo.setTypeId(TipsCardInfo.CARD_TYPE_LANGUAGE);
            tipsCardInfo.setResId(R.drawable.bg_notify_search_home_card);
            tipsCardInfo.setBotton(getContext().getResources().getString(R.string.browser_setting_notify_button));
            tipsCardInfo.setTitle(getContext().getResources().getString(R.string.browser_setting_notify_title));
            arrayList.add(tipsCardInfo);
        }
//        long adShowTime = (long) SharedPreferencesUtils.get(SharedPreferencesUtils.SETTING_AD_HOME_PAGE_TIME, 0l);
//        boolean hasHomePageAdData = true;
//        if (!DateUtils.isToday(adShowTime) && hasHomePageAdData) {
//            TipsCardInfo tipsCardInfo = new TipsCardInfo();
//            tipsCardInfo.setTypeId(TipsCardInfo.CARD_TYPE_AD);
//            tipsCardInfo.setAd(true);
//            arrayList.add(tipsCardInfo);
//        }
        return arrayList;
    }

    private boolean requestNotifyPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission((Activity) getContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ((Activity) getContext()).requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, SettingsPreferenecesFragment.NOTIFY_PERMISSION_HOME_REQUEST_CODE);
                return true;
            }
        }
        return false;
    }


    private CardTipsNotifyState mCardTipsNotifyState;
    public void setCardTipsNotifyState(CardTipsNotifyState cardTipsNotifyState){
        mCardTipsNotifyState = cardTipsNotifyState;
    }

    public interface CardTipsNotifyState{
        void cardTipsNotifyState();
    }

}
