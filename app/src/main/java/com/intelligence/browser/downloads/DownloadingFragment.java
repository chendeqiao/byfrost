package com.intelligence.browser.downloads;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.intelligence.browser.R;
import com.intelligence.browser.downloads.support.WinkManageEvent;
import com.intelligence.browser.downloads.ui.DownloadingAdapter;
import com.intelligence.commonlib.notification.NotificationCenter;
import com.intelligence.commonlib.notification.Subscriber;
import com.intelligence.commonlib.swiperecyclerview.Closeable;
import com.intelligence.commonlib.swiperecyclerview.OnSwipeMenuItemClickListener;
import com.intelligence.commonlib.swiperecyclerview.SwipeMenuRecyclerView;
import com.intelligence.commonlib.download.Wink;
import com.intelligence.commonlib.download.request.DownloadInfo;
import com.intelligence.commonlib.download.util.Comparators;
import com.intelligence.commonlib.download.util.Objects;
import com.intelligence.commonlib.download.util.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DownloadingFragment extends Fragment implements View.OnClickListener, DownloadingAdapter
        .OnDownloadChangeListener {
    SwipeMenuRecyclerView mRecyclerView;
    DownloadingAdapter mDownloadingAdapter;
    private View mViews;
    //    private RelativeLayout mNoDownloads;
    private View mBrowserDownloadingLayout;
    private List<DownloadInfo> mDownloading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        mViews = inflater.inflate(R.layout.browser_fragment_downloading, container, false);
        mRecyclerView = mViews.findViewById(R.id.swipe_recyclerview);
        mBrowserDownloadingLayout = mViews.findViewById(R.id.browser_downloading_layout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false; // 禁用垂直滚动
            }

            @Override
            public boolean canScrollHorizontally() {
                return false; // 禁用水平滚动
            }
        };
        mRecyclerView.setLayoutManager(layoutManager);
//
//        mRecyclerView.setItemAnimator(new NoAlphaItemAnimator());
//        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.setSwipeMenuCreator(swipeMenuCreator);
//        mRecyclerView.setSwipeMenuItemClickListener(menuItemClickListener);
//        mRecyclerView.setLongPressDragEnabled(true);
        mDownloadingAdapter = new DownloadingAdapter(getActivity(), this);
        mDownloadingAdapter.setSwipeMenuItemClickListener(menuItemClickListener);
        mRecyclerView.setAdapter(mDownloadingAdapter);

        NotificationCenter.defaultCenter().subscriber(DownloadingFragment.class, eventSubscriber);

        return mViews;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData(null);
    }

    private void refreshData(WinkManageEvent event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Wink wink = Wink.get();
                    mDownloading = wink.getDownloadingResources();
                    if (!Utils.isEmpty(mDownloading)) {
                        Collections.sort(mDownloading, downloadingComp);
                    }
                    mDownloadingAdapter.setDownloadedTasks(mDownloading, DownloadingAdapter.ITEM_VIEW_DOWNLOADING_ITEM);
                    mDownloadingAdapter.notifyDataSetChangedMainLooper();
                    showNoDownload();
                    if (getParentFragment() instanceof DownloadedFragment.OnDataChangeListener) {
                        ((DownloadedFragment.OnDataChangeListener) getParentFragment()).onDataChangeListener();
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    private void showNoDownload() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(mBrowserDownloadingLayout != null) {
                    mBrowserDownloadingLayout.setVisibility(mDownloading != null && mDownloading.size() > 0 ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    public boolean isEmpty() {
        return mDownloading == null || mDownloading.size() <= 0;
    }

    Comparator<DownloadInfo> downloadingComp = new Comparator<DownloadInfo>() {
        @Override
        public int compare(DownloadInfo lhs, DownloadInfo rhs) {
            int v = Comparators.compare(rhs.getTracer().startTime, lhs.getTracer().startTime);
            if (v == 0) {
                v = Comparators.compare(rhs.getId(), lhs.getId());
                if (v == 0)
                    v = Objects.compare(lhs.getTitle(), rhs.getTitle(),
                            String.CASE_INSENSITIVE_ORDER);
            }
            return v;
        }
    };

    private Subscriber<WinkManageEvent> eventSubscriber = new Subscriber<WinkManageEvent>() {
        @Override
        public void onEvent(WinkManageEvent event) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    refreshData(event);
                }
            });
        }
    };


    @Override
    public void onClick(View view) {

    }


    /**
     * 菜单点击监听。
     */
    private OnSwipeMenuItemClickListener menuItemClickListener = new OnSwipeMenuItemClickListener() {
        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
//            closeable.smoothCloseMenu();// 关闭被点击的菜单。
            try {
                mDownloadingAdapter.deleteItem(adapterPosition);
                mDownloading.remove(adapterPosition);
                showNoDownload();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onDownloaded() {
        refreshData(null);
    }

    @Override
    public void itemRename(long id) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationCenter.defaultCenter().unsubscribe(DownloadingFragment.class, eventSubscriber);
        if (mDownloadingAdapter != null && mRecyclerView != null) {
            mDownloadingAdapter.onDetachedFromRecyclerView(mRecyclerView);
        }
    }

    @Override
    public void deleteItem(int postion) {

    }
}
