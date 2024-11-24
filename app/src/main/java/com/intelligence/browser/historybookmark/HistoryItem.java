/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intelligence.browser.historybookmark;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.utils.ImageBackgroundGenerator;
import com.intelligence.browser.view.RoundImageView;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.StringUtil;
import com.intelligence.commonlib.tools.UrlUtils;

public class HistoryItem extends HorizontalScrollView {
    final static int MAX_TEXTVIEW_LEN = 80;

    protected TextView mTextView;
    protected TextView mUrlText;
    protected RoundImageView mImageView;
    protected String mUrl;
    protected String mTitle;
    protected boolean mEnableScrolling = false;
    protected View mLineTop;
    protected ImageView mCloseTv;

    /**
     * Instantiate a bookmark item, including a default favicon.
     *
     * @param context The application context for the item.
     */
    HistoryItem(Context context) {
        super(context);

        setClickable(false);
        setEnableScrolling(false);
        LayoutInflater factory = LayoutInflater.from(context);
        factory.inflate(R.layout.browser_history_item, this);
        mTextView = findViewById(R.id.title_history_item);
        mUrlText = findViewById(R.id.url_history_item);
        mImageView = findViewById(R.id.logo_history_item);
        mCloseTv = findViewById(R.id.close_history_item);
    }

    /**
     * Copy this BookmarkItem to item.
     *
     * @param item BookmarkItem to receive the info from this BookmarkItem.
     */
    /* package */ void copyTo(BookmarkItem item) {
        item.mTextView.setText(mTextView.getText());
        item.mUrlText.setText(mUrlText.getText());
        item.mImageView.setImageDrawable(mImageView.getDrawable());
    }

    /**
     * Return the name assigned to this bookmark item.
     */
    /* package */ String getName() {
        return mTitle;
    }

    /* package */ String getUrl() {
        return mUrl;
    }

    /**
     * Set the favicon for this item.
     *
     * @param b The new bitmap for this item.
     *          If it is null, will use the default.
     */
    /* package */ void setFavicon(Bitmap b) {
        if (b != null) {
            mImageView.setRoundBg(ImageBackgroundGenerator.getBackgroundColor(b));
            mImageView.setImageBitmap(b);
        } else {
            mImageView.setImageResource(R.drawable.browser_search_url_icon);
        }
    }

    /**
     * Set the default icon for this item.
     */
    /* package */ void setFavicon(String url) {
            mImageView.setDefaultIconByUrl(url);
    }

    void setFaviconBackground(Drawable d) {
        mImageView.setBackgroundDrawable(d);
    }

    /**
     * Set the new name for the bookmark item.
     *
     * @param name The new name for the bookmark item.
     */
    /* package */ void setName(String name,String keyword) {
        if (name == null) {
            return;
        }

        mTitle = name;

        if (name.length() > MAX_TEXTVIEW_LEN) {
            name = name.substring(0, MAX_TEXTVIEW_LEN);
        }
        mTextView.setText(TextUtils.isEmpty(keyword) ? name : StringUtil.spanWrap(mTextView.getContext(), keyword, name));
    }

    /**
     * Set the new url for the bookmark item.
     *
     * @param url The new url for the bookmark item.
     */
    /* package */ void setUrl(String url,String keyword) {
        if (url == null) {
            return;
        }

        mUrl = url;

        url = UrlUtils.stripUrl(url);
        if (url.length() > MAX_TEXTVIEW_LEN) {
            url = url.substring(0, MAX_TEXTVIEW_LEN);
        }
        mUrlText.setText(TextUtils.isEmpty(keyword) ? SchemeUtil.hideLocalUrl(url) : SchemeUtil.hideLocalUrl(StringUtil.spanWrap(mTextView.getContext(), keyword, url)+""));
    }

    void setEnableScrolling(boolean enable) {
        mEnableScrolling = enable;
        setFocusable(mEnableScrolling);
        setFocusableInTouchMode(mEnableScrolling);
        requestDisallowInterceptTouchEvent(!mEnableScrolling);
        requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mEnableScrolling) {
            return super.onTouchEvent(ev);
        }
        return false;
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec,
                                int parentHeightMeasureSpec) {
        if (mEnableScrolling) {
            super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
            return;
        }

        final ViewGroup.LayoutParams lp = child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight(), lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom(), lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child,
                                           int parentWidthMeasureSpec, int widthUsed,
                                           int parentHeightMeasureSpec, int heightUsed) {
        if (mEnableScrolling) {
            super.measureChildWithMargins(child, parentWidthMeasureSpec,
                    widthUsed, parentHeightMeasureSpec, heightUsed);
            return;
        }

        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin
                        + heightUsed, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }
}
