package com.intelligence.news.news.cards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.news.NetConfig;
import com.intelligence.news.hotword.BrowserInitDataContants;
import com.intelligence.news.hotword.HotWordHttpRequest;
import com.intelligence.news.news.mode.DataResult;
import com.intelligence.news.news.mode.HotWordData;

import java.util.ArrayList;

public class HotwordCardItemView extends FrameLayout {
    private HotWordAdapter mhotWordAdapter;
    private RecyclerView mRecycleView;

    public HotwordCardItemView(@NonNull Context context) {
        this(context,null);
    }

    public HotwordCardItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HotwordCardItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.browser_card_hotword_item, this);
        initView(view);
    }

    private void initView(View view){
        mRecycleView = view.findViewById(R.id.card_hotword_recyclerview);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));

        mhotWordAdapter = new HotWordAdapter(getContext());
        mhotWordAdapter.setIsShowDivider(false);
        mhotWordAdapter.setClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == null || view.getTag() == null || view.getTag() instanceof HotWordData){
                    HotWordData hotWordData = (HotWordData) view.getTag();
                    if (hotWordData.pageType == NetConfig.PAGE_WEIBO) {
                        SchemeUtil.openNewWebView(getContext(), BrowserInitDataContants.WEIBO_SEARCH_URL + hotWordData.hotword);
                    } else if (hotWordData.pageType == NetConfig.PAGE_BAIDU) {
                        SchemeUtil.openNewWebView(getContext(), "https://m.baidu.com/s?wd="+ hotWordData.hotword+"");
                    } else {
                        SchemeUtil.openNewWebView(getContext(), hotWordData.hotword);
                    }
                    Global.clearActivity();
                }
            }
        });

        mRecycleView.setAdapter(mhotWordAdapter);
    }

    public void setType(int type){
        DataResult dataResult = HotWordHttpRequest.getData(type);
        ArrayList list = new ArrayList();
        if(dataResult!= null && dataResult.newsList.size()>2) {
            list.add(dataResult.newsList.get(0));
            list.add(dataResult.newsList.get(1));
            list.add(dataResult.newsList.get(2));
            mhotWordAdapter.setData(list);
        }
    }
}
