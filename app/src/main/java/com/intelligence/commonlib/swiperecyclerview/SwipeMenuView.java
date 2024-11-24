package com.intelligence.commonlib.swiperecyclerview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Iterator;
import java.util.List;

public class SwipeMenuView extends LinearLayout {
    private SwipeSwitch mSwipeSwitch;
    private RecyclerView.ViewHolder mAdapterVIewHolder;
    private int mDirection;
    private OnSwipeMenuItemClickListener mItemClickListener;
    private OnClickListener mMenuClickListener;

    public SwipeMenuView(Context context) {
        this(context, null);
    }

    public SwipeMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mMenuClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (SwipeMenuView.this.mItemClickListener != null && SwipeMenuView.this.mSwipeSwitch != null && SwipeMenuView.this.mSwipeSwitch.isMenuOpen()) {
                    SwipeMenuView.this.mItemClickListener.onItemClick(SwipeMenuView.this.mSwipeSwitch, SwipeMenuView.this.mAdapterVIewHolder.getAdapterPosition(), v.getId(), SwipeMenuView.this.mDirection);
                }

            }
        };
    }

    public void bindMenu(SwipeMenu swipeMenu, int direction) {
        this.removeAllViews();
        this.mDirection = direction;
        List<SwipeMenuItem> items = swipeMenu.getMenuItems();
        int index = 0;
        Iterator var5 = items.iterator();

        while(var5.hasNext()) {
            SwipeMenuItem item = (SwipeMenuItem)var5.next();
            this.addItem(item, index++);
        }

    }

    public void bindMenuItemClickListener(OnSwipeMenuItemClickListener swipeMenuItemClickListener, SwipeSwitch swipeSwitch) {
        this.mItemClickListener = swipeMenuItemClickListener;
        this.mSwipeSwitch = swipeSwitch;
    }

    public void bindAdapterViewHolder(RecyclerView.ViewHolder adapterVIewHolder) {
        this.mAdapterVIewHolder = adapterVIewHolder;
    }

    private void addItem(SwipeMenuItem item, int index) {
        LayoutParams params = new LayoutParams(item.getWidth(), item.getHeight());
        params.weight = (float)item.getWeight();
        LinearLayout parent = new LinearLayout(this.getContext());
        parent.setId(index);
        parent.setGravity(17);
        parent.setOrientation(1);
        parent.setLayoutParams(params);
        ResCompat.setBackground(parent, item.getBackground());
        parent.setOnClickListener(this.mMenuClickListener);
        this.addView(parent);
        if (item.getImage() != null) {
            parent.addView(this.createIcon(item));
        }

        if (!TextUtils.isEmpty(item.getText())) {
            parent.addView(this.createTitle(item));
        }

    }

    private ImageView createIcon(SwipeMenuItem item) {
        ImageView imageView = new ImageView(this.getContext());
        imageView.setImageDrawable(item.getImage());
        return imageView;
    }

    private TextView createTitle(SwipeMenuItem item) {
        TextView textView = new TextView(this.getContext());
        textView.setText(item.getText());
        textView.setGravity(17);
        int textSize = item.getTextSize();
        if (textSize > 0) {
            textView.setTextSize((float)textSize);
        }

        ColorStateList textColor = item.getTitleColor();
        if (textColor != null) {
            textView.setTextColor(textColor);
        }

        int textAppearance = item.getTextAppearance();
        if (textAppearance != 0) {
            ResCompat.setTextAppearance(textView, textAppearance);
        }

        Typeface typeface = item.getTextTypeface();
        if (typeface != null) {
            textView.setTypeface(typeface);
        }

        return textView;
    }
}
