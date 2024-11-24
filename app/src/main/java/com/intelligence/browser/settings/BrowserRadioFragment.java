package com.intelligence.browser.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.utils.BrowserHandler;
import com.intelligence.browser.ui.search.SearchEngineInfo;
import com.intelligence.browser.ui.search.SearchEnginePreference;
import com.intelligence.browser.ui.search.SearchEngines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BrowserRadioFragment extends BasePreferenceFragment implements AdapterView.OnItemClickListener {

    private BaseAdapter mAdapter;
    private String mSelect;
    private onFragmentCallBack mCallBack;
    private String mKey;
    private BrowserSettings mBrowserSettings;
    private ArrayList<String> mListValue;
    private boolean isAnimaRuning = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mAdapter instanceof SeachEngineRadioAdapter) {
                mSelect = mBrowserSettings.getSearchEngineName();
                List<SearchEngineInfo> searchEngineList = SearchEngines.getInstance(getActivity()).getSearchEngineInfos();
                ((SeachEngineRadioAdapter) mAdapter).changeList(searchEngineList);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = super.onCreateView(inflater, container, bundle);
        initData();
        return view;
    }

    public void setTitle(String title) {
        setBrowserActionBarTitle(title);
    }

    private void initData() {
        Bundle arguments = getArguments();
        mKey = arguments.getString(KEY);
        mBrowserSettings = BrowserSettings.getInstance();
        String[] array;
        switch (mKey) {
            case PreferenceKeys.PREF_SEARCH_ENGINE:
                setTitle(getString(R.string.pref_search_engine_dialog_title));
                mSelect = mBrowserSettings.getSearchEngineName();
                List<SearchEngineInfo> searchEngineList = SearchEngines.getInstance(getActivity()).getSearchEngineInfos();
                if (TextUtils.isEmpty(mSelect) || searchEngineList == null || searchEngineList.size() == 0) {
                    finish();
                }
                mAdapter = new SeachEngineRadioAdapter(getActivity(), searchEngineList);
                registSearchEngineUpgradeListener();
                break;
            case PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING:
                setTitle(getString(R.string.pref_default_text_encoding));
                mSelect = mBrowserSettings.getDefaultTextEncoding();
                array = getResources().getStringArray(R.array.pref_default_text_encoding_choices);
                mListValue = getListData(getActivity().getResources().getStringArray(R.array.pref_default_text_encoding_values));
                if (TextUtils.isEmpty(mSelect) || array == null || array.length == 0) {
                    finish();
                }
                mAdapter = new RadioAdapter(getActivity(), getListData(array));
                break;
            case PreferenceKeys.PREF_DEFAULT_AD_TIME:
                setTitle(getString(R.string.mark_image_ad));
                mSelect = mBrowserSettings.getDefaultAdTimeLite();
                array = getResources().getStringArray(R.array.pref_default_ad_time_limite);
                mListValue = getListData(getActivity().getResources().getStringArray(R.array.pref_default_ad_time_limite));
                if (TextUtils.isEmpty(mSelect) || array == null || array.length == 0) {
                    finish();
                }
                mAdapter = new RadioAdapter(getActivity(), getListData(array));
                break;
            case PreferenceKeys.PREF_USER_AGENT:
                setTitle(getString(R.string.accessibility_button_uaswitch));
                array = getResources().getStringArray(R.array.pref_user_agent_choices);
                mSelect = mBrowserSettings.getUserAgent() + "";
                mListValue = getListData(getActivity().getResources().getStringArray(R.array.pref_default_text_encoding_values));
                if (TextUtils.isEmpty(mSelect) || array == null || array.length == 0) {
                    finish();
                }
                mAdapter = new RadioAdapter(getActivity(), getListData(array));
                break;
            case PreferenceKeys.PREF_USER_VIDEO:
                setTitle(getString(R.string.browser_setting_video_title));
                array = getResources().getStringArray(R.array.pref_user_video_choices);
                mSelect = mBrowserSettings.getUserVideo() + "";
                mListValue = getListData(getActivity().getResources().getStringArray(R.array.pref_default_text_encoding_values));
                if (TextUtils.isEmpty(mSelect) || array == null || array.length == 0) {
                    finish();
                }
                mAdapter = new RadioAdapter(getActivity(), getListData(array));
                break;
        }

        mList.setOnItemClickListener(this);
        mList.setAdapter(mAdapter);
    }

    public void back() {
        BrowserHandler.getInstance().handlerPostDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCallBack != null) {
                    mCallBack.onFragmentCallBack(mKey, mSelect);
                }
                finish();
            }
        }, 200);
    }

    public void setOnFragmentCallBack(onFragmentCallBack callBack) {
        mCallBack = callBack;
    }

    private ArrayList<String> getListData(String[] array) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, array);
        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isAnimaRuning) {
            return;
        }
        isAnimaRuning = true;
        String value = (String) view.getTag(R.id.item_tag);
        if (value != null && !mSelect.equals(value)) {
            mSelect = value;
            BrowserHandler.getInstance().handlerPostDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                    back();
                }
            }, 200);
        } else {
            BrowserHandler.getInstance().handlerPostDelayed(new Runnable() {
                @Override
                public void run() {
                    ((BrowserSettingActivity) getActivity()).back();
                }
            }, 200);
        }
    }

    public void registSearchEngineUpgradeListener() {

    }


    @Override
    public void finish() {
        super.finish();
        registSearchEngineUpgradeListener();
    }

    private class SeachEngineRadioAdapter extends BaseAdapter {
        private ArrayList<SearchEngineInfo> mArrayList = new ArrayList<>();
        private Context mContext;

        public SeachEngineRadioAdapter(Context context, List<SearchEngineInfo> arrayList) {
            changeList(arrayList);
            this.mContext = context;
        }

        public void changeList(List<SearchEngineInfo> arrayList) {
            if (arrayList != null && arrayList.size() > 0) {
                mArrayList.clear();
                mArrayList.addAll(arrayList);
            }
        }

        @Override
        public int getCount() {
            return mArrayList == null ? 0 : mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.browser_radio_fragment_item, parent, false);
                holder = new ViewHolder();
                holder.mIcon = convertView.findViewById(R.id.icon);
                holder.mContent = convertView.findViewById(R.id.content_item);
                holder.mSelect = convertView.findViewById(R.id.select);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            SearchEngineInfo info = mArrayList.get(position);
            if (info == null) {
                return null;
            }
            holder.mIcon.setVisibility(View.VISIBLE);
            String name = info.getName();
            SearchEnginePreference.setSearchEngineIcon(mContext, holder.mIcon, name);
            holder.mContent.setText(info.getLabel());

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(mSelect) && name.equals(mSelect)) {
                holder.mSelect.setVisibility(View.VISIBLE);
            } else {
                holder.mSelect.setVisibility(View.INVISIBLE);
            }

            convertView.setTag(R.id.item_tag, name);
            return convertView;
        }
    }

    private class RadioAdapter extends BaseAdapter {
        private ArrayList<String> mArrayList = new ArrayList<>();
        private Context mContext;

        public RadioAdapter(Context context, ArrayList<String> arrayList) {
            if (arrayList != null && arrayList.size() > 0) {
                mArrayList.addAll(arrayList);
            }
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mArrayList == null ? 0 : mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.browser_radio_fragment_item, parent, false);
                holder = new ViewHolder();
                holder.mIcon = convertView.findViewById(R.id.icon);
                holder.mContent = convertView.findViewById(R.id.content_item);
                holder.mSelect = convertView.findViewById(R.id.select);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String content = mArrayList.get(position);
            String value = null;
            switch (mKey) {
                case PreferenceKeys.PREF_DEFAULT_TEXT_ENCODING:
                    holder.mContent.setText(content);
                    if (mListValue != null && mArrayList.size() > position) {
                        value = mListValue.get(position);
                    }
                    break;
                case PreferenceKeys.PREF_USER_AGENT:
                    holder.mContent.setText(content);
                    if (mListValue != null && mArrayList.size() > position) {
                        value = position + "";
                    }
                case PreferenceKeys.PREF_USER_VIDEO:
                    holder.mContent.setText(content);
                    if (mListValue != null && mArrayList.size() > position) {
                        value = position + "";
                    }
                case PreferenceKeys.PREF_DEFAULT_AD_TIME:
                    holder.mContent.setText(content);
                    if (mListValue != null && mArrayList.size() > position) {
                        value = position + "";
                    }
                    break;
            }

            if (!TextUtils.isEmpty(value) && !TextUtils.isEmpty(mSelect) && value.equals(mSelect)) {
                holder.mSelect.setVisibility(View.VISIBLE);
            } else {
                holder.mSelect.setVisibility(View.INVISIBLE);
            }

            convertView.setTag(R.id.item_tag, value);
            return convertView;
        }

    }

    public class ViewHolder {
        private ImageView mIcon;
        private TextView mContent;
        private ImageView mSelect;
    }
}
