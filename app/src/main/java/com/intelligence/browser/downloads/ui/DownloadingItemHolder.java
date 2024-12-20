package com.intelligence.browser.downloads.ui;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.download.request.DownloadInfo;
import com.intelligence.commonlib.download.request.ResourceStatus;
import com.intelligence.commonlib.download.util.Utils;

public class DownloadingItemHolder extends DownloadingAdapter.ViewHolder {

    public DownloadingItemHolder(Context context, View itemView) {
        super(context, itemView);
    }

    private void updateActionState(DownloadInfo data) {
        int state = data.getDownloadState();
        loadThumbnail(data.getTitle());
        ImageView view = itemView.findViewById(R.id.item_action);
        view.setTag(data);
        switch (state) {
            case ResourceStatus.WAIT:
            case ResourceStatus.DOWNLOADING:
                view.setImageResource(R.drawable.browser_download_pause);
                break;
            case ResourceStatus.DOWNLOAD_FAILED:
                view.setImageResource(R.drawable.browser_downloading_refresh_icon);
                setItemText(R.id.item_speed, view.getResources().getText(R.string.download_failed));
                break;
            case ResourceStatus.PAUSE:
            case ResourceStatus.INIT:
                view.setImageResource(R.drawable.browser_download_play);
                setItemText(R.id.item_speed, view.getResources().getText(R.string.download_pause));
                break;
        }
    }

    @Override
    public void bindView(DownloadInfo data,int pos) {
        setItemText(R.id.item_title, data.getTitle());
        setItemTag(R.id.downloading_item_delete, pos+"");

        String speedText = Utils.speedOf(data.getTracer().avgSpeed);
        setItemText(R.id.item_speed, speedText);

        String dsizeText = Utils.convertFileSize(data.getDownloadedSizeInBytes());
        String tsizeText = Utils.convertFileSize(data.getTotalSizeInBytes());

        String s = getContext().getResources().getString(R.string.download_size_text_format, dsizeText, tsizeText);

        if (data.getTotalSizeInBytes() <= 0 && data.getDownloadedSizeInBytes() > 0) {
            s = dsizeText;
        }

        setItemText(R.id.item_percent, s);
        setProgress(data.getDownloadProgress());

        updateActionState(data);
    }

    @Override
    protected void attach(DownloadingAdapter adapter) {
        super.attach(adapter);
        setViewOnClickListener(R.id.item_action, adapter);
        setViewOnClickListener(R.id.downloading_item_delete, adapter);
    }
}
