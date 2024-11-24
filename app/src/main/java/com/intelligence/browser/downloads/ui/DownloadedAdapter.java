package com.intelligence.browser.downloads.ui;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.drm.DrmManagerClient;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;
import com.intelligence.browser.R;
import com.intelligence.browser.downloads.support.WinkEvent;
import com.intelligence.browser.ui.media.PhotoViewPagerActivity;
import com.intelligence.browser.ui.media.VideoActivity;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.view.DownloadsPopWindow;
import com.intelligence.commonlib.notification.NotificationCenter;
import com.intelligence.commonlib.notification.Subscriber;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.tools.StringUtil;
import com.intelligence.commonlib.download.Resource;
import com.intelligence.commonlib.download.SpeedWatcher;
import com.intelligence.commonlib.download.Wink;
import com.intelligence.commonlib.download.request.DownloadInfo;
import com.intelligence.commonlib.download.request.ResourceStatus;
import com.intelligence.commonlib.download.request.SimpleURLResource;
import com.intelligence.commonlib.download.util.FileUtils;
import com.intelligence.commonlib.download.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DownloadedAdapter extends RecyclerView.Adapter<DownloadedAdapter.ViewHolder> implements View
        .OnClickListener,View.OnLongClickListener {
    private static final String TAG = "DownloadingAdapter";

    public final static int ITEM_VIEW_DOWNLOADED_ITEM = 2;

    private final Context mContext;
    public List<DownloadInfo> mDownloadTasks = new ArrayList<>();
    private final LayoutInflater mLayoutInflater;
    private DownloadsPopWindow mPopWindow;
    private OnDownloadChangeListener mOnDownloadChangeListener;

    public DownloadedAdapter(Context context, OnDownloadChangeListener listener) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mOnDownloadChangeListener = listener;
    }

    public void setDownloadedTasks(Collection<DownloadInfo> tasks) {
        mDownloadTasks.clear();
        if (!Utils.isEmpty(tasks)) {
            mDownloadTasks.addAll(tasks);
        }
    }

    private void refreshProgress(DownloadInfo data) {
        int index = mDownloadTasks.indexOf(data);
        if (index == -1) {
            return;
        }
    }

    private void onStatusChanged(DownloadInfo data) {
        int index = mDownloadTasks.indexOf(data);
        if (data.getDownloadState() == ResourceStatus.DOWNLOADED) {
            mDownloadTasks.remove(data);
            notifyDataSetChanged();
            if (mOnDownloadChangeListener != null) {
                mOnDownloadChangeListener.onDownloaded();
            }
        } else if (index != -1) {
            notifyItemChanged(index, data);
        }
    }

    private RecyclerView mAttachedRecyclerView;
    private SpeedWatcher speedWatcher = new SpeedWatcher() {
        @Override
        public void onSpeedChanged(Collection<DownloadInfo> tasks) {
            if (mAttachedRecyclerView != null && !mAttachedRecyclerView.isComputingLayout()) {
                notifyDataSetChanged();
            }
        }
    };

    private Subscriber<WinkEvent> eventSubscriber = new Subscriber<WinkEvent>() {
        @Override
        public void onEvent(WinkEvent event) {
            if (event.event == WinkEvent.EVENT_PROGRESS) {
                refreshProgress(event.entity);
            } else if (event.event == WinkEvent.EVENT_STATUS_CHANGE) {
                onStatusChanged(event.entity);
            }
        }
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.browser_item_downloaded, parent, false);
        return new DownloadedItemHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DownloadInfo data = getItemData(position);
        holder.bindView(data,mIsEditMode);
        holder.attach(this);
    }

    public DownloadInfo getItemData(int position) {
        if (mDownloadTasks != null) {
            return mDownloadTasks.get(position);
        }
        return null;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        NotificationCenter.defaultCenter().subscriber(DownloadedAdapter.class, eventSubscriber);
        if (Wink.get() != null) {
            Wink.get().setSpeedWatcher(speedWatcher);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        NotificationCenter.defaultCenter().unsubscribe(DownloadedAdapter.class, eventSubscriber);
    }

    @Override
    public int getItemCount() {
        return mDownloadTasks.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_VIEW_DOWNLOADED_ITEM;
    }

    private void handle(View v, DownloadInfo data) {
        if (mPopWindow == null) {
            mPopWindow = new DownloadsPopWindow(v.getContext());
        }
        Boolean downloading = true;
        if (data.getDownloadState() == ResourceStatus.DOWNLOADED) {
            downloading = false;
        }
        mPopWindow.setDownloadInfo(data);
        mPopWindow.setDownloading(downloading);
        mPopWindow.setClickListener(this);
        mPopWindow.show(v);

    }

    private void action(View view, DownloadInfo data) {
        int state = data.getDownloadState();
        switch (state) {
            case ResourceStatus.WAIT:
            case ResourceStatus.DOWNLOADING:
                ((ImageView) view).setImageResource(R.drawable.browser_download_play);
                Wink.get().stop(data.getKey());
                break;
            case ResourceStatus.DOWNLOAD_FAILED:
                ((ImageView) view).setImageResource(R.drawable.browser_downloading_refresh_icon);
                Wink.get().wink(data.getResource());
                break;
            case ResourceStatus.INIT:
            case ResourceStatus.PAUSE:
                ((ImageView) view).setImageResource(R.drawable.browser_download_pause);
                Wink.get().wink(data.getResource());
                break;
            case ResourceStatus.DELETED:
                ToastUtil.showLongToast(view.getContext(), R.string.file_not_exist);
                break;
            case ResourceStatus.DOWNLOADED:
                handle(view, data);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(v.getId() == R.id.item_container){
            if(v.getTag() instanceof DownloadInfo) {
                entryEditMode((DownloadInfo) (v.getTag()));
            }
        }
        return false;
    }

    public void entryEditMode(DownloadInfo info){
        restoreLastData();
        mIsEditMode = true;
        info.isSelect = true;
        if(mOnDownloadChangeListener != null) {
            mOnDownloadChangeListener.editMode(true);
        }
        notifyDataSetChanged();
    }

    private void restoreLastData() {
        if (!CollectionUtils.isEmpty(mDownloadTasks)) {
            for (DownloadInfo downloadInfo : mDownloadTasks) {
                downloadInfo.isSelect = false;
            }
        }
    }

    public boolean exitEditMode() {
        if (!mIsEditMode) {
            return false;
        }
        selectAll(false);
        if (mOnDownloadChangeListener != null) {
            mOnDownloadChangeListener.editMode(false);
        }
        notifyDataSetChanged();
        mIsEditMode = false;
        return true;
    }

    public boolean isSelectAll() {
        return isSelectAll;
    }

    public boolean isSelectAll;

    public void selectAll(boolean isSelect) {
        isSelectAll = isSelect;
        if (!CollectionUtils.isEmpty(mDownloadTasks)) {
            for (DownloadInfo info : mDownloadTasks) {
                info.isSelect = isSelect;
            }
        }
        notifyDataSetChanged();
    }

    private boolean mIsEditMode;
    @Override
    public void onClick(View v) {
        DownloadInfo data = (DownloadInfo) v.getTag();
        switch (v.getId()) {
            case R.id.item_action:
                if(mIsEditMode){
                    data.isSelect = !data.isSelect;
                    notifyDataSetChanged();
                }else {
                    action(v, data);
                }
                break;
            case R.id.item_container:
                try {
                    if(mIsEditMode){
                        data.isSelect = !data.isSelect;
                        notifyDataSetChanged();
                    }else {
                        openFile(mContext, data);
                    }
                } catch (Exception e) {
                }
                break;
            case R.id.popwindow_copylink:
                if (mPopWindow != null) {
                    mPopWindow.dismiss();
                }
                if (mPopWindow.getDownloadInfo() != null) {
                    copy(v.getContext(), mPopWindow.getDownloadInfo().getUrl());
                    ToastUtil.showShortToast(v.getContext(), R.string.copylink_success);
                }
                break;
            case R.id.popwindow_rename:
                if (mPopWindow != null) {
                    mPopWindow.dismiss();
                }
                if (mOnDownloadChangeListener != null && mPopWindow.getDownloadInfo() != null) {
                    mOnDownloadChangeListener.itemRename(mPopWindow.getDownloadInfo().getId());
                }
                break;
            case R.id.popwindow_share:
                if (mPopWindow != null) {
                    mPopWindow.dismiss();
                }
                if (mPopWindow.getDownloadInfo() != null) {
                    sharePage(mContext, mPopWindow.getDownloadInfo().getTitle(), mPopWindow.getDownloadInfo().getUrl());
                }
                break;
            case R.id.popwindow_delete:
                if (mPopWindow != null) {
                    mPopWindow.dismiss();
                }
                deleteItem(mPopWindow.getDownloadInfo());
                break;
        }
    }

    private String[] getAllPicutre() {
        ArrayList<String> list = new ArrayList();
        if (CollectionUtils.isEmpty(mDownloadTasks)) {
            return null;
        }
        for (int i = 0; i < mDownloadTasks.size(); i++) {
            if (FileUtils.isPicture(mDownloadTasks.get(i).getLocalFilePath())) {
                list.add(mDownloadTasks.get(i).getLocalFilePath() + "");
            }
        }
        return StringUtil.getStringForArray(list);
    }

    private int getCurrentPosition(String filePath, String[] allFile) {
        if (allFile.length == 1) {
            return 0;
        }
        for (int i = 0; i < allFile.length; i++) {
            if (filePath.equals(allFile[i])) {
                return i;
            }
        }
        return 0;
    }

    private void openFile(Context context, DownloadInfo data) {
        if (!FileUtils.fileIsExists(data.getLocalFilePath())) {
            ToastUtil.showLongToast(context, R.string.file_not_exist);
            return;
        }

        File file = new File(data.getLocalFilePath());
        if (FileUtils.isPicture(data.getLocalFilePath())) {
            String[] allPictures = getAllPicutre();
            if (allPictures.length == 0) {
                allPictures[0] = data.getLocalFilePath();
                Intent intent = new Intent(context, PhotoViewPagerActivity.class);
                intent.putExtra(PhotoViewPagerActivity.IMAGE_URL_PRAMAS, allPictures);
                intent.putExtra(PhotoViewPagerActivity.IMAGE_FROM_NET, "0");
                intent.putExtra(PhotoViewPagerActivity.IMAGE_POSITION, 0);
                ((Activity) context).startActivityForResult(intent, PhotoViewPagerActivity.FINISH_CODE);
            } else {
                int position = getCurrentPosition(data.getLocalFilePath(), allPictures);
                Intent intent = new Intent(context, PhotoViewPagerActivity.class);
                intent.putExtra(PhotoViewPagerActivity.IMAGE_URL_PRAMAS, allPictures);
                intent.putExtra(PhotoViewPagerActivity.IMAGE_FROM_NET, "0");
                intent.putExtra(PhotoViewPagerActivity.IMAGE_POSITION, position);
                ((Activity) context).startActivityForResult(intent, PhotoViewPagerActivity.FINISH_CODE);
            }
            return;
        } else if (FileUtils.isVideoFile(data.getLocalFilePath())) {
            Intent intent = new Intent(context, VideoActivity.class);
            intent.putExtra(VideoActivity.VIDEO_URL_PRAMAS, data.getLocalFilePath());
            intent.putExtra(VideoActivity.VIDEO_TITLE_PRAMAS, data.getTitle());
            intent.putExtra(VideoActivity.VIDEO_FROM_NET, "0");
            context.startActivity(intent);
            return;
        }


        Intent intent = new Intent();

        String type = null;
        Resource res = data.getResource();
        if (res instanceof SimpleURLResource) {
            type = ((SimpleURLResource) res).getMimeType();
        }

        String fileExtension = FileUtils.getFileExtensionFromUrl(file.getPath()).toLowerCase();
        if (TextUtils.isEmpty(type) && !TextUtils.isEmpty(fileExtension)) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        }

        if (!TextUtils.isEmpty(type) && type.startsWith("application/vnd.oma.drm")) {
            DrmManagerClient client = new DrmManagerClient(context);
            type = client.getOriginalMimeType(file.getAbsolutePath());
        }

        if (TextUtils.isEmpty(type)) {
            ToastUtil.showShortToast(context, R.string.unknown_file);
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", new File(data.getLocalFilePath()));
                intent.setDataAndType(contentUri, type);
            } else {
//                intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(file), type);
            }


            try {
                if (null != intent.resolveActivity(context.getPackageManager())) {
                    context.startActivity(intent);
                } else {
                    ToastUtil.showShortToast(context, R.string.cannot_open_the_file);
                }
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    static abstract class ViewHolder extends RecyclerView.ViewHolder {

        private final Context context;

        public ViewHolder(Context context, View itemView) {
            super(itemView);
            this.context = context;
        }

        public Context getContext() {
            return context;
        }

        public void setItemText(int itemId, CharSequence text) {
            TextView view = itemView.findViewById(itemId);
            if (view != null)
                view.setText(text);
        }

        public void setProgress(int progress) {
            ProgressBar bar = itemView.findViewById(R.id.progress);
            if (bar != null) {
                bar.setProgress(progress);
            }
        }

        public void loadThumbnail(DownloadInfo downloadInfo) {
            String url = downloadInfo.getTitle();
            String mime = ((SimpleURLResource) downloadInfo.getResource()).getMimeType();
            if(downloadInfo.fileMidiaType == -1) {
                if ("image/jpeg".equals(mime)) {
                    downloadInfo.fileMidiaType = FileUtils.MEDIA_TYPE_PICTURE;
                } else if ("video/mp4".equals(mime)) {
                    downloadInfo.fileMidiaType = FileUtils.MEDIA_TYPE_VIDEO;
                } else  {
                    downloadInfo.fileMidiaType = FileUtils.getMediaType(url);
                }
                if (downloadInfo.fileMidiaType == FileUtils.MEDIA_TYPE_DEFAULT) {
                    if (FileUtils.isPicture(downloadInfo.getLocalFilePath())) {
                        downloadInfo.fileMidiaType = FileUtils.MEDIA_TYPE_PICTURE;
                    } else if (FileUtils.isVideoFile(downloadInfo.getLocalFilePath())) {
                        downloadInfo.fileMidiaType = FileUtils.MEDIA_TYPE_VIDEO;
                    }
                }
            }

            ImageView view = itemView.findViewById(R.id.item_icon);
            ImageView videoIcon = itemView.findViewById(R.id.download_video_play_icon);
            if(videoIcon != null){
                videoIcon.setVisibility(View.GONE);
            }
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.setBackground(null);
            if (downloadInfo.fileMidiaType == FileUtils.MEDIA_TYPE_PICTURE) {
                Bitmap bitmap = null;
                if(downloadInfo.bitmap != null && !downloadInfo.bitmap.isRecycled()){
                    bitmap = downloadInfo.bitmap;
                    if (bitmap != null) {
                        view.setImageBitmap(bitmap);
//                        view.setBackground(view.getContext().getResources().getDrawable(R.drawable.browser_download_pic_bg));
                        view.setScaleType(ImageView.ScaleType.FIT_XY);
                    } else {
                        view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_picture));
                    }
                } else {
                    ImageUtils.loadLocalImage(downloadInfo.getLocalFilePath(), ScreenUtils.dpToPxInt(view.getContext(), 32), ScreenUtils.dpToPxInt(view.getContext(), 32), new ImageUtils.Callback() {
                        @Override
                        public void onResponse(Bitmap bp) {
                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    downloadInfo.bitmap = bp;
                                    if (bp != null) {
                                        view.setImageBitmap(bp);
//                                        view.setBackground(view.getContext().getResources().getDrawable(R.drawable.browser_download_pic_bg));
                                        view.setScaleType(ImageView.ScaleType.FIT_XY);
                                    } else {
                                        view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_picture));
                                    }
                                }
                            });
                        }
                    });
                }
            } else if (downloadInfo.fileMidiaType == FileUtils.MEDIA_TYPE_VIDEO) {
                Bitmap bitmap = null;
                if (downloadInfo.bitmap != null && !downloadInfo.bitmap.isRecycled()) {
                    bitmap = downloadInfo.bitmap;
                    if (bitmap != null) {
                        if(videoIcon != null){
                            videoIcon.setVisibility(View.VISIBLE);
                        }
                        view.setImageBitmap(bitmap);
                        view.setBackground(view.getContext().getResources().getDrawable(R.drawable.browser_download_video_bg));
                    } else {
                        view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_mp4));
                    }
                } else {
                    ImageUtils.loadLocalImage(downloadInfo, new ImageUtils.Callback() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (bitmap != null) {
                                        downloadInfo.bitmap = bitmap;
                                        if (videoIcon != null) {
                                            videoIcon.setVisibility(View.VISIBLE);
                                        }
                                        view.setImageBitmap(bitmap);
                                        view.setBackground(view.getContext().getResources().getDrawable(R.drawable.browser_download_video_bg));
                                    } else {
                                        view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_mp4));
                                    }
                                }
                            });
                        }
                    });
                }
            } else if (downloadInfo.fileMidiaType == FileUtils.MEDIA_TYPE_AUDIO) {
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_audio));
            } else if (downloadInfo.fileMidiaType == FileUtils.MEDIA_TYPE_FILE) {
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_pdf));
            } else if (downloadInfo.fileMidiaType== FileUtils.MEDIA_TYPE_APK) {
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_apk));
            } else if (downloadInfo.fileMidiaType == FileUtils.MEDIA_TYPE_ZIP) {
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_zip));
            } else if (downloadInfo.fileMidiaType == FileUtils.MEDIA_TYPE_DEFAULT) {
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_file));
            } else {
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.browser_download_file));
            }
//            ImageLoader.getInstance().loadIcon(context, url, R.drawable.ic_video_default, view);
        }

        public void setViewOnClickListener(int viewId, View.OnClickListener listener) {
            View v = itemView.findViewById(viewId);
            if (v != null) {
                v.setOnClickListener(listener);
            }
        }

        public void setViewOnLongClickListener(int viewId, View.OnLongClickListener listener){
            View v = itemView.findViewById(viewId);
            if (v != null) {
                v.setOnLongClickListener(listener);
            }
        }

        public void setViewTag(int viewId, Object tag) {
            View v = itemView.findViewById(viewId);
            if (v != null) {
                v.setTag(tag);
            }
        }

        public abstract void bindView(DownloadInfo data,boolean isEditMode);

        protected void attach(DownloadedAdapter adapter) {

        }
    }

    public void deleteFile(String[] urls) {
        if (urls != null && urls.length > 0) {
            for (String url : urls) {
                try {
                    deleteItem(getItemDownloadInfo(url));
                }catch (Exception e){
                }
            }
        }
    }

    private DownloadInfo getItemDownloadInfo(String s) {
        if (CollectionUtils.isEmpty(mDownloadTasks) || TextUtils.isEmpty(s)) {
            return null;
        }
        for (DownloadInfo downloadInfo : mDownloadTasks) {
            if(s.equals(downloadInfo.getLocalFilePath())){
                return downloadInfo;
            }
        }
        return null;
    }

    public void deleteSelectItem() {
        if (CollectionUtils.isEmpty(mDownloadTasks)) {
            return;
        }
        Iterator<DownloadInfo> iterator = mDownloadTasks.iterator();
        while (iterator.hasNext()) {
            DownloadInfo downloadInfo = iterator.next();
            if (downloadInfo.isSelect) {
                iterator.remove();
                Wink.get().delete(downloadInfo.getKey(), true);
                if (mOnDownloadChangeListener != null) {
                    mOnDownloadChangeListener.deleteItem(downloadInfo);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void deleteItem(DownloadInfo data) {
        if (data == null) return;
        mDownloadTasks.remove(data);
        notifyDataSetChanged();
        Wink.get().delete(data.getKey(), true);
        if (mOnDownloadChangeListener != null) {
            mOnDownloadChangeListener.deleteItem(data);
        }
    }

    private void copy(Context context, CharSequence text) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(
                Context.CLIPBOARD_SERVICE);
        cm.setText(text);
    }

    public interface OnDownloadChangeListener {
        void onDownloaded();

        void itemRename(long id);

        void deleteItem(DownloadInfo info);

        void deleteSelectItem();

        void deleteAllItem();

        void editMode(boolean isEdit);
    }

    private void sharePage(Context c, String title, String url) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, title + "\n" + url);
        try {
            c.startActivity(Intent.createChooser(send, c.getString(
                    R.string.choosertitle_sharevia)));
        } catch (ActivityNotFoundException ex) {
            // if no app handles it, do nothing
        }
    }
}