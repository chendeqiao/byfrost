package com.intelligence.browser.downloads.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.download.Resource;
import com.intelligence.commonlib.download.request.DownloadInfo;
import com.intelligence.commonlib.download.request.ResourceStatus;
import com.intelligence.commonlib.download.request.SimpleURLResource;
import com.intelligence.commonlib.download.util.Utils;

import java.util.Locale;

public class DownloadedItemHolder extends DownloadedAdapter.ViewHolder {

    public final static int SECONDS_PER_HOUR = 3600;
    public final static int SECONDS_PER_MINUTE = 60;

    public DownloadedItemHolder(Context context, View itemView) {
        super(context, itemView);
    }

    public static String formatTimeInSecs(int secs) {
        int hour = secs / SECONDS_PER_HOUR;
        secs = secs % SECONDS_PER_HOUR;
        int minute = secs / SECONDS_PER_MINUTE;
        secs = secs % SECONDS_PER_MINUTE;

        if (hour > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hour, minute, secs);
        } else {
            return String.format(Locale.US, "%02d:%02d", minute, secs);
        }
    }

    @Override
    public void bindView(DownloadInfo data, boolean isEditMode) {
        setItemText(R.id.item_title, data.getTitle());

        if(TextUtils.isEmpty(data.sizeText)) {
            String sizeText = Utils.convertFileSize(data.getTotalSizeInBytes());
            if (data.getDownloadState() == ResourceStatus.DELETED) {
                sizeText = getContext().getResources().getString(R.string.file_not_exist);
            }
            data.sizeText = sizeText;
        }
        setItemText(R.id.item_size, data.sizeText);
        setViewTag(R.id.item_action, data);
        setViewTag(R.id.item_container, data);
        Resource res = data.getResource();
        if (res instanceof SimpleURLResource) {
            String mime = ((SimpleURLResource) res).getMimeType();
            if ("application/vnd.android.package-archive".equalsIgnoreCase(mime) && data.getApkIcon() != null) {
                loadThumbnail(data);
                setItemText(R.id.item_title, data.getApkName());
            } else {
                loadThumbnail(data);
            }
        }
        ImageView editIcon = itemView.findViewById(R.id.download_item_edit_icon);
        if(isEditMode){
            if (data.isSelect) {
                editIcon.setImageResource(R.drawable.browser_item_selete);
            } else {
                editIcon.setImageResource(R.drawable.browser_item_unselete);
            }
        }else {
            editIcon.setImageResource(R.drawable.browser_home_other_more_gray);
        }
    }

    @Override
    protected void attach(DownloadedAdapter adapter) {
        super.attach(adapter);
        setViewOnClickListener(R.id.item_action, adapter);
        setViewOnClickListener(R.id.item_container, adapter);
        setViewOnLongClickListener(R.id.item_container,adapter);
    }
}