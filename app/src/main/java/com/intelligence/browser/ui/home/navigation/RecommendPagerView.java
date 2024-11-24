package com.intelligence.browser.ui.home.navigation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.database.DatabaseManager;
import com.intelligence.browser.R;
import com.intelligence.browser.ui.adapter.RecommendAdapter;
import com.intelligence.browser.data.RecommendUrlEntity;
import com.intelligence.commonlib.tools.DisplayUtil;

import java.util.List;

public class RecommendPagerView extends RelativeLayout implements DatabaseManager.DataChangeListener<RecommendUrlEntity> {
    private static final int RECOMMEND_COLUMN_PORTRAIT = 5;
    private static final int NAVIGATION_MARGIN_TOP_ON_OFFSET = DisplayUtil.dip2px(BrowserApplication.getInstance(), 8f);
    private static final int INDEX_OF_INPUT_PAGE = 2;

    private Activity mActivity;
    private LayoutInflater mInflater;
    private RecyclerView mNavRecycler;
    private RecommendAdapter mRecommendAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private Scroller mScroller;
    private int currWindowBottom = 0;
    private int mNavColumn;
    private View mRootLayout;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (!DisplayUtil.isScreenPortrait(getContext())) {
                return;
            }
            if (!mRecommendAdapter.isEdit()) return;
            Rect r = new Rect();
            getWindowVisibleDisplayFrame(r);
            int visitHeight = getBottom() - r.bottom;
            if (currWindowBottom == visitHeight) return;
            currWindowBottom = visitHeight;
        }
    };


    public void exitEditState(){
        if(mRecommendAdapter != null && mRecommendAdapter.isDeleteMode()){
            mRecommendAdapter.setDeleteMode(false);
        }
    }

    public RecommendPagerView(Activity activity, RecommendAdapter.AdapterItemListener listener) {
        super(activity);
        this.mActivity = activity;
        mScroller = new Scroller(activity);
        mInflater = LayoutInflater.from(activity);
        createTouchHelper();
        createNavAdapter(activity,listener);
        init(activity);

        getViewTreeObserver().addOnGlobalLayoutListener(mGlobalListener);
    }

    private void createTouchHelper() {
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    final int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                } else {
                    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return mRecommendAdapter.getItemType(viewHolder.getAdapterPosition()) == mRecommendAdapter.getItemType(target.getAdapterPosition()) &&
                        mRecommendAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (viewHolder == null) {
                    mRecommendAdapter.fireSortChangeIfNeed();
                }
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

        });
    }

    public void updateRecommendData(List<RecommendUrlEntity> list){
        if(mRecommendAdapter != null){
            mRecommendAdapter.replaceData(list);
        }
    }

    private void createNavAdapter(final Context context, RecommendAdapter.AdapterItemListener listener) {
        mRecommendAdapter = new RecommendAdapter(context, mItemTouchHelper,false);
        mRecommendAdapter.registerListener(listener);
    }

    private void init(Activity activity) {
        removeAllViews();
        mNavRecycler =  (RecyclerView) mInflater.inflate(R.layout.browser_recommend_pager, this, false);
        addView(mNavRecycler);
        mNavColumn = RECOMMEND_COLUMN_PORTRAIT;
        mNavRecycler.setLayoutManager(new GridLayoutManager(getContext(), RECOMMEND_COLUMN_PORTRAIT) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mNavRecycler.setAdapter(mRecommendAdapter);
        mItemTouchHelper.attachToRecyclerView(mNavRecycler);
    }

    private boolean canNavScroll() {
        int visibleCount = mRecommendAdapter.getVisibleCount();
        int rows = visibleCount / mNavColumn;
        if (visibleCount % mNavColumn != 0) {
            rows++;
        }
        int itemSize = DisplayUtil.getDimenPxValue(mActivity, R.dimen.nav_item_size);
        return itemSize * rows > mNavRecycler.getHeight();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
    }

    public void onResume() {
        if (mRecommendAdapter.isDeleteMode()) {
            mRecommendAdapter.setDeleteMode(false);
        }else {
            mRecommendAdapter.notifyDataSetChanged();
        }
    }

    public void setDeleteMode(boolean isDelete){
        if(mRecommendAdapter != null){
            mRecommendAdapter.setDeleteMode(isDelete);
        }
    }

    public boolean isDeleteMode() {
        if (mRecommendAdapter != null) {
            return mRecommendAdapter.isDeleteMode();
        }
        return false;
    }

    @Override
    public void onInsertToDB(RecommendUrlEntity entity) {
        if(mRecommendAdapter != null){
            mRecommendAdapter.onInsertToDB(entity);
        }
    }

    @Override
    public void onDeleteToDB(RecommendUrlEntity entity) {
        if(mRecommendAdapter != null){
            mRecommendAdapter.onDeleteToDB(entity);
        }
    }

    @Override
    public void onUpdateToDB(RecommendUrlEntity entity) {
        if(mRecommendAdapter != null){
            mRecommendAdapter.onUpdateToDB(entity);
        }
    }

    public void notifyDataSetChanged(){
        if(mRecommendAdapter != null){
            mRecommendAdapter.notifyDataSetChanged();
        }
    }
}