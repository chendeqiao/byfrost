package com.intelligence.browser.ui.home.mostvisited;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.browser.historybookmark.BrowserHistoryPage;
import com.intelligence.browser.database.provider.BrowserContract;
import com.intelligence.browser.ui.activity.BrowserSearchActivity;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

public class MostVisitedView extends Fragment implements LoaderManager.LoaderCallbacks, View.OnClickListener {
    private static final String SEARCH_MOSTVISITED_SHOW = "search_mostvisited_show";

    private RecyclerView mMostVisitedList;
    private ImageView mMostVisitedSwitch;
    private View mTitle;
    private String mMostVisitsLimit;
    private MostVisitedAdapter mAdapter;
    private TextView mMostVisitedTitle;

    private View mRootView;

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        int mvlimit = getResources().getInteger(R.integer.most_visits_search_limit);
        mMostVisitsLimit = Integer.toString(mvlimit);
        Uri.Builder combinedBuilder = BrowserContract.History.CONTENT_URI.buildUpon();
                Uri uri = combinedBuilder
                        .appendQueryParameter(BrowserContract.PARAM_LIMIT, mMostVisitsLimit)
                        .build();
                String where = BrowserContract.Combined.VISITS + " > 0";
                CursorLoader loader = new CursorLoader(getContext(), uri,
                        BrowserHistoryPage.HistoryQuery.PROJECTION, where, null, BrowserContract.Combined.VISITS + " DESC" + "," + BrowserContract.Combined
                        .DATE_LAST_VISITED + " DESC");
                return loader;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        mAdapter.changeMostVisitedCursor((Cursor) data);
        checkIfEmpty((Cursor) data);
    }

    void checkIfEmpty(Cursor mMostVisited) {
        if (mMostVisited == null || mMostVisited.getCount() == 0) {
            // Both cursors have loaded - check to see if we have data
            mRootView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView =  inflater.inflate(R.layout.browser_most_visited_view, container, false);
        initView(mRootView);
        return mRootView;
    }

    private MostVisitedAdapter.onClickMostVisitedUrl mOnClickListener;

    public void setOnclickListener(MostVisitedAdapter.onClickMostVisitedUrl onclickListener){
        mOnClickListener = onclickListener;
    }

    private void initView(View view) {
        mMostVisitedList = view.findViewById(R.id.search_most_visited_list);
        mTitle = view.findViewById(R.id.search_most_visited_title);
        mMostVisitedSwitch= view.findViewById(R.id.search_most_visited_switch);
        mMostVisitedTitle= view.findViewById(R.id.browser_most_visited);
        mMostVisitedTitle.setText((getResources().getString(R.string.most_visited)));
        mMostVisitedTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SchemeUtil.openHistoryPage(getActivity(),2);
            }
        });
        mMostVisitedSwitch.setOnClickListener(this);
        mAdapter = new MostVisitedAdapter(getContext());
        mAdapter.setOnclickListener(new MostVisitedAdapter.onClickMostVisitedUrl() {
            @Override
            public void openMostVisitedUrl(String url) {
                if(mOnClickListener != null){
                    mOnClickListener.openMostVisitedUrl(url);
                }
            }
        });
        mMostVisitedList.setLayoutManager(new GridLayoutManager(getContext(), 2) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mMostVisitedList.setAdapter(mAdapter);
        setVisibleHintMositedVisited((boolean) SharedPreferencesUtils.get(SEARCH_MOSTVISITED_SHOW,true));
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_most_visited_switch:
                if (mMostVisitedList != null && mMostVisitedList.getVisibility() == View.VISIBLE) {
                    setVisibleHintMositedVisited(false);
                } else {
                    setVisibleHintMositedVisited(true);
                }
                break;
        }
    }

    private void setVisibleHintMositedVisited(boolean isShow) {
        SharedPreferencesUtils.put(SEARCH_MOSTVISITED_SHOW, isShow);
        mMostVisitedList.setVisibility(isShow ? View.VISIBLE : View.GONE);
        mMostVisitedSwitch.setImageDrawable(getContext().getResources().getDrawable(isShow ? R.drawable.browser_search_most_visite_switch_open : R.drawable.browser_search_most_visite_switch_close));
    }

}

