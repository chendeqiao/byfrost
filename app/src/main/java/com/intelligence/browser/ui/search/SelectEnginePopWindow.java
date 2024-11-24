package com.intelligence.browser.ui.search;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.intelligence.browser.ui.BaseUi;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.ui.home.BrowserMainPageController;
import com.intelligence.browser.settings.PreferenceKeys;
import com.intelligence.browser.R;
import com.intelligence.browser.database.provider.BrowserContract;
import com.intelligence.commonlib.tools.ScreenUtils;

import java.util.List;


public class SelectEnginePopWindow implements AdapterView.OnItemClickListener {
    private static final int DELAY_TIME = 1000;
    private static final int POP_WINDOW_HEIGHT = 168;
    private static final int HEIGHT_OFFSET = 9;
    private static final int WIDTH_OFFSET = 6;
    private static final int PADDING_SEARCH_CARD_VIEW = 16;
    private View mView;
    private PopupWindow mPopWindow;
    private ListView mList;
    private Context mContext;
    private EngineAdapter mAdapter;
    private List<SearchEngineInfo> list;
    private BrowserSettings mBrowserSettings;
    private BaseUi.SelectSearchEngineListener mSelectSearchEngineListener;


    private static final String[] PROJECTION = new String[]{
            BrowserContract.Bookmarks.TITLE,
            BrowserContract.Bookmarks.URL,
            BrowserContract.Bookmarks.FAVICON};

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mAdapter.setSelectEngine(i);
    }

    public SelectEnginePopWindow(Context context, BaseUi.SelectSearchEngineListener selectSearchEngineListener) {
        mContext = context;
        mSelectSearchEngineListener = selectSearchEngineListener;
        initWindow();
    }

    public void initWindow() {
        this.mView = View.inflate(mContext, R.layout.browser_select_engine_popwindow, null);
        list = SearchEngines.getSearchEngineInfos(mContext);
        int height = list.size() * mContext.getResources().getDimensionPixelOffset(R.dimen.browser_selete_engine_item) + 6 * mContext.getResources().getDimensionPixelOffset(R.dimen.browser_selete_engine_magin_top);
        mPopWindow = new PopupWindow(mView, mContext.getResources().getDimensionPixelSize(R.dimen.browser_select_engine_width), height, true);
        mPopWindow.setFocusable(false);
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mBrowserSettings = BrowserSettings.getInstance();

        mAdapter = new EngineAdapter(mContext, list);
        mList = mView.findViewById(R.id.lv_select_engine);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
    }

    public void show(View parentView, int animStype) {
        int[] location = new int[2];
        parentView.getLocationOnScreen(location);
        mPopWindow.setAnimationStyle(animStype == 0 ? R.style.select_engine_popwindow_arab : R.style.select_engine_popwindow_down);
        mPopWindow.showAsDropDown(parentView, ScreenUtils.dpToPxInt(mContext, animStype == 0 ? 2 : 0), -mContext.getResources().getDimensionPixelOffset(animStype == 0 ? R.dimen.preference_screen_side_margin : R.dimen.preference_screen_side_margin_down), Gravity.NO_GRAVITY);
    }

    public void dismiss() {
        mSelectSearchEngineListener.updateSearchEngine();
        mPopWindow.dismiss();
    }

    private class EngineAdapter extends BaseAdapter {
        private List<SearchEngineInfo> mList;
        private String[] mSeachEngineName;
        private Context mContext;

        public EngineAdapter(Context context, List<SearchEngineInfo> list) {
            mContext = context;
            mList = list;
            mSeachEngineName = context.getResources().getStringArray(R.array.custom_search_engine);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            view = View.inflate(mContext, R.layout.browser_item_select_search_engine, null);
            ((TextView) view.findViewById(R.id.select_search_engine_name)).setText(SearchEnginePreference
                    .getSearchValue(mContext, mList.get(i).getName()));
            for (int num = 0; num < mSeachEngineName.length; num++) {
                if (mList.get(i).getName().toLowerCase().contains(mSeachEngineName[num])) {
                    ((ImageView) view.findViewById(R.id.icon_search_engine)).setImageDrawable(mContext.getResources()
                            .getDrawable(BrowserMainPageController.mIconSearch[num]));
                }
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSelectEngine(i);
                    dismiss();
                }
            });
            return view;
        }

        public void setSelectEngine(int position) {
            mBrowserSettings.getPreferences().edit().putString(PreferenceKeys.PREF_SEARCH_ENGINE, mList.get(position)
                    .getName()).apply();
            mAdapter.notifyDataSetChanged();
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(DELAY_TIME);
                        dismiss();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
    }
}
