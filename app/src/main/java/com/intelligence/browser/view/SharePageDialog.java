package com.intelligence.browser.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.data.ShareAppInfoBean;
import com.intelligence.browser.utils.Constants;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

public class SharePageDialog {
    private PopupWindow mSharePopWindow;
    private View mContentView;
    private Context mContext;
    private List<ShareAppInfoBean> mShareAppList;
    private GridView mGirdView;
    private ShareAppAdapter mAdapter;
    String[] mPkgNames;
    String[] mAppNames;
    String[] mLocalShareNames;
    int[] mLocalShareDrawables;
    private String mShareLinkTitle;
    private String mShareLinkUrl;
    private String mShareAppUrl;
    private Bitmap mScreenShot;

    public SharePageDialog(Context context) {
        mContext = context;
        mPkgNames = mContext.getResources().getStringArray(R.array.share_default_pkgname);
        mAppNames = mContext.getResources().getStringArray(R.array.share_default_appname);
        mLocalShareNames = mContext.getResources().getStringArray(R.array.share_local_default_appname);
        mLocalShareDrawables = new int[]{
                R.drawable.browser_share,
                R.drawable.browser_share_website,
        };
    }

    public void showShareDialog(View anchor, String title, String url, Bitmap screenShort) {
        mShareLinkTitle = title;
        mShareLinkUrl = url;
        mScreenShot= screenShort;
        initShareAppList();

        mContentView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.browser_share_page_layout, null);
        if (mSharePopWindow == null) {
            mSharePopWindow = new PopupWindow(mContentView, WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT, true);
        }
        mSharePopWindow.setOutsideTouchable(true);
        mSharePopWindow.setBackgroundDrawable(new BitmapDrawable());
        mSharePopWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mGirdView = mContentView.findViewById(R.id.share_app_grid);
        mAdapter = new ShareAppAdapter();
        mGirdView.setAdapter(mAdapter);
        mSharePopWindow.showAtLocation(anchor, Gravity.BOTTOM, 0, 0);
        View bg = mContentView.findViewById(R.id.popwindow_bg);
        bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharePopWindow.dismiss();
            }
        });
    }


    private List<ResolveInfo> getShareAppsList() {
        List<ResolveInfo> resolveList = new ArrayList<ResolveInfo>();
        if (mShareAppList != null && mShareAppList.size() > 0) {
            return null;
        }
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("text/plain");
        PackageManager pkgManager = mContext.getPackageManager();
        resolveList = pkgManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return resolveList;
    }

    private void initShareAppList() {
        if (mShareAppList != null && mShareAppList.size() > 0) {
            return;
        }
        if (mShareAppList == null) {
            mShareAppList = new ArrayList<>();
        }

        List<ResolveInfo> resolveInfos = getShareAppsList();
        if (resolveInfos == null || resolveInfos.size() <= 0) {
            return;
        }
        List<String> defaultAppList = new ArrayList<>();
        PackageManager packageManager = mContext.getPackageManager();
        for (ResolveInfo resolveInfo : resolveInfos) {
            ShareAppInfoBean appInfo = new ShareAppInfoBean();
            appInfo.mPkgName = resolveInfo.activityInfo.packageName;
            appInfo.mAppName = resolveInfo.loadLabel(packageManager).toString();
            appInfo.mAppIcon = resolveInfo.loadIcon(packageManager);
            if (appInfo.mAppName != null && appInfo.mAppIcon != null) {
                for (int i = 0; i < mPkgNames.length; i++) {
                    if (mPkgNames[i].equalsIgnoreCase(appInfo.mPkgName) && !defaultAppList.contains(mAppNames[i])) {
                        appInfo.mAppName = mAppNames[i];
                        defaultAppList.add(mAppNames[i]); //twitter重复两个图标现象
                        mShareAppList.add(appInfo);
                        break;
                    }
                }
            }
        }
        addDefaultItem();
    }

    private void addDefaultItem() {
        for (int i = 0; i < mLocalShareNames.length; i ++) {
            ShareAppInfoBean appInfo = new ShareAppInfoBean();
            appInfo.mAppName = mLocalShareNames[i];
            appInfo.mAppIcon = mContext.getResources().getDrawable(mLocalShareDrawables[i]);
            mShareAppList.add(appInfo);
        }
    }

    private class ShareAppAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mShareAppList == null) {
                return 0;
            } else {
                return mShareAppList.size();
            }
        }

        @Override
        public Object getItem(int position) {
            if (mShareAppList == null || mShareAppList.size() < position) {
                return null;
            }
            return mShareAppList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.browser_share_page_item, null);
                viewHolder.mAppIcon = convertView.findViewById(R.id.share_app_icon_view);
                viewHolder.mAppTitle = convertView.findViewById(R.id.share_app_title_view);
                viewHolder.mItemParent = convertView.findViewById(R.id.share_item_parent);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.mAppIcon.setBackgroundDrawable(mShareAppList.get(position).mAppIcon);
            viewHolder.mAppTitle.setText(mShareAppList.get(position).mAppName);

            viewHolder.mItemParent.setOnClickListener(new ItemClickListener(position));

            return convertView;
        }

        private class ViewHolder {
            private ImageView mAppIcon;
            private TextView mAppTitle;
            private RelativeLayout mItemParent;
        }
    }

    public class ItemClickListener implements View.OnClickListener {
        int mPosition;
        public ItemClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            ShareAppInfoBean bean = mShareAppList.get(mPosition);
            Intent send = new Intent(Intent.ACTION_SEND);
            if (!TextUtils.isEmpty(bean.mPkgName)) {
                //处理app
                send.setPackage(bean.mPkgName);
                send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT, mShareLinkTitle + "\n" + mShareLinkUrl);
                try {
                    mContext.startActivity(send);
                } catch (android.content.ActivityNotFoundException ex) {
                    // if no app handles it, do nothing
                }
            } else if (bean.mAppName.equalsIgnoreCase(mLocalShareNames[0])) {
                shareApp(v.getContext());
            } else if (bean.mAppName.equalsIgnoreCase(mLocalShareNames[1])) {
                if (!TextUtils.isEmpty(mShareLinkUrl) && mShareLinkUrl.endsWith(Constants.NATIVE_PAGE_URL)) {
                    shareApp(v.getContext());
                } else {
                    shareUrl(DeviceInfoUtils.getAppName(mContext)+"-"+mContext.getResources().getString(R.string.contextmenu_sharelink)+":", mShareLinkUrl);
                }
            }
            mSharePopWindow.dismiss();
        }
    }

    private void shareApp(Context context){
        String appDownLoadUrl = DeviceInfoUtils.getAppDownUrl(mContext);
        if (!TextUtils.isEmpty(SharedPreferencesUtils.getAppDownUrl())) {
            appDownLoadUrl = SharedPreferencesUtils.getAppDownUrl();
        }
        shareUrl(context.getResources().getString(R.string.byfrost_slogan),appDownLoadUrl);
    }

    private void shareUrl(String title, String shareLinkUrl){
        //更多
        Intent systemSend = new Intent(Intent.ACTION_SEND);
        systemSend.setType("text/plain");
        if (TextUtils.isEmpty(title)) {
            systemSend.putExtra(Intent.EXTRA_TEXT, shareLinkUrl);
        } else {
            systemSend.putExtra(Intent.EXTRA_TEXT, title + "\n" + shareLinkUrl);
            systemSend.putExtra(Intent.EXTRA_SUBJECT, title);
        }
        try {
            mContext.startActivity(Intent.createChooser(systemSend, mContext.getString(
                    R.string.choosertitle_sharevia)));
        } catch (android.content.ActivityNotFoundException ex) {
            // if no app handles it, do nothing
        }
    }
}
