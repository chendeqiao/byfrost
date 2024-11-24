package com.intelligence.commonlib.swiperecyclerview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class SwipeMenuRecyclerView extends RecyclerView {
    public static final int LEFT_DIRECTION = 1;
    public static final int RIGHT_DIRECTION = -1;
    public static final int MENU_OPEN = 1;
    public static final int MENU_CLOSE = 0;
    private static final int INVALID_POSITION = -1;
    protected ViewConfiguration mViewConfig;
    protected SwipeMenuLayout mOldSwipedLayout;
    protected int mOldTouchedPosition;
    private int mDownX;
    private int mDownY;
    private boolean isInterceptTouchEvent;
    private SwipeMenuCreator mSwipeMenuCreator;
    private OnSwipeMenuItemClickListener mSwipeMenuItemClickListener;
//    private DefaultItemTouchHelper mDefaultItemTouchHelper;
    private SwipeMenuCreator mDefaultMenuCreator;
    private OnSwipeMenuItemClickListener mDefaultMenuItemClickListener;

    public SwipeMenuRecyclerView(Context context) {
        this(context, null);
    }

    public SwipeMenuRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mOldTouchedPosition = -1;
        this.isInterceptTouchEvent = true;
        this.mDefaultMenuCreator = new SwipeMenuCreator() {
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
                if (SwipeMenuRecyclerView.this.mSwipeMenuCreator != null) {
                    SwipeMenuRecyclerView.this.mSwipeMenuCreator.onCreateMenu(swipeLeftMenu, swipeRightMenu, viewType);
                }

            }
        };
        this.mDefaultMenuItemClickListener = new OnSwipeMenuItemClickListener() {
            public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
                if (SwipeMenuRecyclerView.this.mSwipeMenuItemClickListener != null) {
                    SwipeMenuRecyclerView.this.mSwipeMenuItemClickListener.onItemClick(closeable, adapterPosition, menuPosition, direction);
                }

            }
        };
        this.mViewConfig = ViewConfiguration.get(this.getContext());
    }

    private void initializeItemTouchHelper() {

    }

    public void setLongPressDragEnabled(boolean canDrag) {
        this.initializeItemTouchHelper();
//        this.mDefaultItemTouchHelper.setLongPressDragEnabled(canDrag);
    }

    public void setSwipeMenuCreator(SwipeMenuCreator swipeMenuCreator) {
        this.mSwipeMenuCreator = swipeMenuCreator;
    }

    public void setSwipeMenuItemClickListener(OnSwipeMenuItemClickListener swipeMenuItemClickListener) {
        this.mSwipeMenuItemClickListener = swipeMenuItemClickListener;
    }

    public void setAdapter(Adapter adapter) {
        if (adapter instanceof SwipeMenuAdapter) {
            SwipeMenuAdapter menuAdapter = (SwipeMenuAdapter)adapter;
            menuAdapter.setSwipeMenuCreator(this.mDefaultMenuCreator);
            menuAdapter.setSwipeMenuItemClickListener(this.mDefaultMenuItemClickListener);
        }

        super.setAdapter(adapter);
    }

    public void smoothOpenMenu(int position, int direction, int duration) {
        if (this.mOldSwipedLayout != null && this.mOldSwipedLayout.isMenuOpen()) {
            this.mOldSwipedLayout.smoothCloseMenu();
        }

        ViewHolder vh = this.findViewHolderForAdapterPosition(position);
        if (vh != null) {
            View itemView = this.getSwipeMenuView(vh.itemView);
            if (itemView != null && itemView instanceof SwipeMenuLayout) {
                this.mOldSwipedLayout = (SwipeMenuLayout)itemView;
                if (direction == -1) {
                    this.mOldTouchedPosition = position;
                    this.mOldSwipedLayout.smoothOpenRightMenu(duration);
                } else if (direction == 1) {
                    this.mOldTouchedPosition = position;
                    this.mOldSwipedLayout.smoothOpenLeftMenu(duration);
                }
            }
        }

    }

    public void smoothCloseMenu() {
        if (this.mOldSwipedLayout != null && this.mOldSwipedLayout.isMenuOpen()) {
            this.mOldSwipedLayout.smoothCloseMenu();
        }

    }

    private View getSwipeMenuView(View itemView) {
        if (itemView instanceof SwipeMenuLayout) {
            return itemView;
        } else {
            List<View> unvisited = new ArrayList();
            unvisited.add(itemView);

            while(true) {
                View child;
                do {
                    if (unvisited.isEmpty()) {
                        return itemView;
                    }

                    child = unvisited.remove(0);
                } while(!(child instanceof ViewGroup));

                if (child instanceof SwipeMenuLayout) {
                    return child;
                }

                ViewGroup group = (ViewGroup)child;
                int childCount = group.getChildCount();

                for(int i = 0; i < childCount; ++i) {
                    unvisited.add(group.getChildAt(i));
                }
            }
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent e) {
        boolean isIntercepted = super.onInterceptTouchEvent(e);
        if (!this.isInterceptTouchEvent) {
            return isIntercepted;
        } else if (e.getPointerCount() > 1) {
            return true;
        } else {
            int action = e.getAction();
            int x = (int)e.getX();
            int y = (int)e.getY();
            switch(action) {
                case 0:
                    this.mDownX = x;
                    this.mDownY = y;
                    isIntercepted = false;
                    int touchingPosition = this.getChildAdapterPosition(this.findChildViewUnder((float)x, (float)y));
                    if (touchingPosition != this.mOldTouchedPosition && this.mOldSwipedLayout != null && this.mOldSwipedLayout.isMenuOpen()) {
                        this.mOldSwipedLayout.smoothCloseMenu();
                        isIntercepted = true;
                    }

                    if (isIntercepted) {
                        this.mOldSwipedLayout = null;
                        this.mOldTouchedPosition = -1;
                    } else {
                        ViewHolder vh = this.findViewHolderForAdapterPosition(touchingPosition);
                        if (vh != null) {
                            View itemView = this.getSwipeMenuView(vh.itemView);
                            if (itemView != null && itemView instanceof SwipeMenuLayout) {
                                this.mOldSwipedLayout = (SwipeMenuLayout)itemView;
                                this.mOldTouchedPosition = touchingPosition;
                            }
                        }
                    }
                    break;
                case 1:
                    isIntercepted = this.handleUnDown(x, y, isIntercepted);
                    break;
                case 2:
                    isIntercepted = this.handleUnDown(x, y, isIntercepted);
                    ViewParent viewParent = this.getParent();
                    if (viewParent != null) {
                        viewParent.requestDisallowInterceptTouchEvent(!isIntercepted);
                    }
                    break;
                case 3:
                    isIntercepted = this.handleUnDown(x, y, isIntercepted);
            }

            return isIntercepted;
        }
    }

    private boolean handleUnDown(int x, int y, boolean defaultValue) {
        int disX = this.mDownX - x;
        int disY = this.mDownY - y;
        if (Math.abs(disX) > this.mViewConfig.getScaledTouchSlop()) {
            defaultValue = false;
        }

        if (Math.abs(disY) < this.mViewConfig.getScaledTouchSlop() && Math.abs(disX) < this.mViewConfig.getScaledTouchSlop()) {
            defaultValue = false;
        }

        return defaultValue;
    }

    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch(action) {
            case 2:
                if (this.mOldSwipedLayout != null && this.mOldSwipedLayout.isMenuOpen()) {
                    this.mOldSwipedLayout.smoothCloseMenu();
                }
            case 0:
            case 1:
            case 3:
            default:
                return super.onTouchEvent(e);
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface DirectionMode {
    }
}
