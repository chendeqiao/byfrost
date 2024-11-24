package com.intelligence.news.news.header;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.news.websites.bean.WebSiteData;

public class AppSitesItemView extends FrameLayout {
    private View mRootLayout;
    private ImageView mAppSiteIcon;
    private TextView mAppSiteTitle;
    private int mWidth;

    public AppSitesItemView(@NonNull Context context) {
        this(context, null, 0);
    }

    public AppSitesItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppSitesItemView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = View.inflate(getContext(), R.layout.browser_function_entry_item_new_view, this);
        initView(view);
    }

    private void initView(View view) {
        mRootLayout = view.findViewById(R.id.function_layout_first_card);
        mAppSiteIcon = view.findViewById(R.id.function_layout_first_img);
        mAppSiteTitle = view.findViewById(R.id.function_layout_secound_title);
    }

    public void setLayout(int width, int height) {
        mWidth = width;
        mRootLayout.getLayoutParams().width = width;
        mRootLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getContext() instanceof Activity){
                    Object object = view.getTag();
                    SchemeUtil.openNewWebView(getContext(),((WebSiteData)object).scheme);
                    Global.clearActivity();
                }
            }
        });
    }

    public void setData(WebSiteData rcmscenes, int position) {
        if (rcmscenes == null) {
            return;
        }
        int resID = mAppSiteIcon.getContext().getResources().getIdentifier(rcmscenes.logo,
                "drawable", mAppSiteIcon.getContext().getPackageName());
        mAppSiteIcon.setImageResource(resID);
//                Glide.with(Global.getInstance()).load("https://upload-images.jianshu.io/upload_images/13855150-534a45dfca05b0b4.gif?imageMogr2/auto-orient/strip|imageView2/2/w/391").asBitmap()
//                .format(DecodeFormat.PREFER_ARGB_8888)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.color.image_place_holder)
//                .error(R.drawable.ic_load_fail)
//                .into(mAppSiteIcon);
        mAppSiteTitle.setText(rcmscenes.title);
        setVisibility(VISIBLE);
        mRootLayout.setTag(rcmscenes);
    }
}