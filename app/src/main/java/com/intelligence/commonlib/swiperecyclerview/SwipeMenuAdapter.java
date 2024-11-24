package com.intelligence.commonlib.swiperecyclerview;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;

import java.util.List;

public abstract class SwipeMenuAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private SwipeMenuCreator mSwipeMenuCreator;
    private OnSwipeMenuItemClickListener mSwipeMenuItemClickListener;
    private SwipeMenuLayout.OnSwipeStateListener mOnSwipeStateListener;

    public SwipeMenuAdapter() {
    }

    public void setOnSwipeStateListener(SwipeMenuLayout.OnSwipeStateListener mOnSwipeStateListener) {
        this.mOnSwipeStateListener = mOnSwipeStateListener;
    }

    void setSwipeMenuCreator(SwipeMenuCreator swipeMenuCreator) {
        this.mSwipeMenuCreator = swipeMenuCreator;
    }

    void setSwipeMenuItemClickListener(OnSwipeMenuItemClickListener swipeMenuItemClickListener) {
        this.mSwipeMenuItemClickListener = swipeMenuItemClickListener;
    }

    @SuppressLint("WrongConstant")
    public final VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = this.onCreateContentView(parent, viewType);
        if (this.mSwipeMenuCreator != null) {
            SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout)LayoutInflater.from(parent.getContext()).inflate(R.layout.browser_yanzhenjie_item_default, parent, false);
            SwipeMenu swipeLeftMenu = new SwipeMenu(swipeMenuLayout, viewType);
            SwipeMenu swipeRightMenu = new SwipeMenu(swipeMenuLayout, viewType);
            this.mSwipeMenuCreator.onCreateMenu(swipeLeftMenu, swipeRightMenu, viewType);
            swipeMenuLayout.setOnSwipeStateListener(this.mOnSwipeStateListener);
            int leftMenuCount = swipeLeftMenu.getMenuItems().size();
            if (leftMenuCount > 0) {
                SwipeMenuView swipeLeftMenuView = swipeMenuLayout.findViewById(com.intelligence.browser.R.id.swipe_left);
                swipeLeftMenuView.setOrientation(swipeLeftMenu.getOrientation());
                swipeLeftMenuView.bindMenu(swipeLeftMenu, 1);
                swipeLeftMenuView.bindMenuItemClickListener(this.mSwipeMenuItemClickListener, swipeMenuLayout);
            }

            int rightMenuCount = swipeRightMenu.getMenuItems().size();
            if (rightMenuCount > 0) {
                SwipeMenuView swipeRightMenuView = swipeMenuLayout.findViewById(R.id.swipe_right);
                swipeRightMenuView.setOrientation(swipeRightMenu.getOrientation());
                swipeRightMenuView.bindMenu(swipeRightMenu, -1);
                swipeRightMenuView.bindMenuItemClickListener(this.mSwipeMenuItemClickListener, swipeMenuLayout);
            }

            if (leftMenuCount > 0 || rightMenuCount > 0) {
                ViewGroup viewGroup = swipeMenuLayout.findViewById(R.id.swipe_content);
                viewGroup.addView(contentView);
                contentView = swipeMenuLayout;
            }
        }

        return this.onCompatCreateViewHolder(contentView, viewType);
    }

    public abstract View onCreateContentView(ViewGroup var1, int var2);

    public abstract VH onCompatCreateViewHolder(View var1, int var2);

    public final void onBindViewHolder(VH holder, int position, List<Object> payloads) {
        View itemView = holder.itemView;
        if (itemView instanceof SwipeMenuLayout) {
            SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout)itemView;
            swipeMenuLayout.bindViewHolder(holder);
            int childCount = swipeMenuLayout.getChildCount();

            for(int i = 0; i < childCount; ++i) {
                View childView = swipeMenuLayout.getChildAt(i);
                if (childView instanceof SwipeMenuView) {
                    ((SwipeMenuView)childView).bindAdapterViewHolder(holder);
                }
            }
        }

        this.onCompatBindViewHolder(holder, position, payloads);
    }

    public void onCompatBindViewHolder(VH holder, int position, List<Object> payloads) {
        this.onBindViewHolder(holder, position);
    }
}
