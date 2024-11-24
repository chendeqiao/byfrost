package com.intelligence.browser.downloads;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.browser.downloads.support.WinkManageEvent;
import com.intelligence.browser.downloads.ui.DownloadedAdapter;
import com.intelligence.browser.utils.Constants;
import com.intelligence.commonlib.notification.NotificationCenter;
import com.intelligence.commonlib.notification.Subscriber;
import com.intelligence.commonlib.download.Resource;
import com.intelligence.commonlib.download.Wink;
import com.intelligence.commonlib.download.request.DownloadInfo;
import com.intelligence.commonlib.download.request.ResourceStatus;
import com.intelligence.commonlib.download.request.SimpleURLResource;
import com.intelligence.commonlib.download.util.Comparators;
import com.intelligence.commonlib.download.util.FileUtils;
import com.intelligence.commonlib.download.util.Objects;
import com.intelligence.commonlib.download.util.Utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DownloadedFragment extends Fragment implements DownloadedAdapter
        .OnDownloadChangeListener {
    RecyclerView mRecyclerView;
    DownloadedAdapter mDownloadAdapter;
    private View mViews;
    private List<DownloadInfo> mDownloaded;
    private View mBrowserDownloadedLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        mViews = inflater.inflate(R.layout.browser_fragment_downloaded, container, false);
        mRecyclerView = mViews.findViewById(R.id.swipe_recyclerview);
        mBrowserDownloadedLayout = mViews.findViewById(R.id.browser_downloaded_layout);

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

        mDownloadAdapter = new DownloadedAdapter(getActivity(), this);
        mRecyclerView.setAdapter(mDownloadAdapter);

        NotificationCenter.defaultCenter().subscriber(DownloadedFragment.class, eventSubscriber);
        return mViews;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    private void refreshData() {
        Wink wink = Wink.get();
        mDownloaded = wink.getDownloadedResources();

        if (mDownloaded == null) {
            showNoDownload();
            return;
        }

        if (!Utils.isEmpty(mDownloaded)) {
            Collections.sort(mDownloaded, downloadedComp);
        }

        for (DownloadInfo data : mDownloaded) {
            if (!FileUtils.fileIsExists(data.getLocalFilePath())) {
                data.setDownloadState(ResourceStatus.DELETED);
            }

            Resource res = data.getResource();
            if (res instanceof SimpleURLResource) {
                String mime = ((SimpleURLResource) res).getMimeType();
                if ("application/vnd.android.package-archive".equalsIgnoreCase(mime) && data.getApkIcon() == null) {
                    BrowserDownloadManager.getInstance().apkInfo(data.getLocalFilePath(), getActivity(), data);
                    Wink.get().updateDownloadInfo(data);
                }
            }
        }
        if (mDownloadAdapter != null) {
            mDownloadAdapter.setDownloadedTasks(mDownloaded);
            mDownloadAdapter.notifyDataSetChanged();
        }
        showNoDownload();
    }

    private OnDataChangeListener mOnDataChangeListener;
    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){
        mOnDataChangeListener = onDataChangeListener;
    }

    private void showNoDownload() {
        mBrowserDownloadedLayout.setVisibility(mDownloaded != null && mDownloaded.size() > 0 ? View.VISIBLE : View.GONE);
        if(mOnDataChangeListener != null){
            mOnDataChangeListener.onDataChangeListener();
        }
    }

    public boolean isEmpty(){
       return mDownloaded == null || mDownloaded.size() <= 0;
    }

    Comparator<DownloadInfo> downloadedComp = new Comparator<DownloadInfo>() {
        @Override
        public int compare(DownloadInfo lhs, DownloadInfo rhs) {
            int v = Comparators.compare(rhs.getTracer().endTime, lhs.getTracer().endTime);
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
                    refreshData();
                }
            });
        }
    };

    @Override
    public void onDownloaded() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationCenter.defaultCenter().unsubscribe(DownloadedFragment.class, eventSubscriber);
        if (mDownloadAdapter != null && mRecyclerView != null) {
            mDownloadAdapter.onDetachedFromRecyclerView(mRecyclerView);
        }
    }

    @Override
    public void itemRename(long id) {
        Intent intent = new Intent(getActivity(), BrowserDownloadRenamePage.class);
        intent.putExtra(Constants.DOWNLOAD_REFERANCE, id);
        getActivity().startActivityForResult(intent, DOWNLOAD_RENAME);
    }

        public static final int DOWNLOAD_RENAME = 1;

    @Override
    public void deleteSelectItem() {
        try {
            if (mDownloadAdapter != null) {
                mDownloadAdapter.deleteSelectItem();
            }
        } catch (Exception e) {
        }
        exitEditMode();
        showNoDownload();
    }

    @Override
    public void deleteAllItem() {
        mDownloaded.clear();
    }

    @Override
    public void deleteItem(DownloadInfo info) {
        try {
            mDownloaded.remove(info);
        } catch (Exception e) {

        }
        showNoDownload();
    }

    @Override
    public void editMode(boolean isEdit) {
        if(mOnDataChangeListener != null){
            mOnDataChangeListener.onEditMode(isEdit);
        }
    }

    public void refresh() {
        refreshData();
    }

    public boolean isDownloaded(String key) {
        Wink wink = Wink.get();
        mDownloaded = wink.getDownloadedResources();

        if (mDownloaded == null) return false;

        for (DownloadInfo data : mDownloaded) {
            if (key.equals(data.getKey())) {
                return true;
            }
        }
        return false;
    }

    public void deleteFile(String[] url) {
        if (mDownloadAdapter != null) {
            mDownloadAdapter.deleteFile(url);
        }
    }

    public  static interface OnDataChangeListener{
        void onDataChangeListener();

        void onEditMode(boolean isEdit);
    }

    public void selectAll(){
        if(mDownloadAdapter != null){
            mDownloadAdapter.selectAll(!mDownloadAdapter.isSelectAll());
        }
    }

    public boolean exitEditMode(){
        if(mDownloadAdapter != null){
          return mDownloadAdapter.exitEditMode();
        }
        return false;
    }
}
