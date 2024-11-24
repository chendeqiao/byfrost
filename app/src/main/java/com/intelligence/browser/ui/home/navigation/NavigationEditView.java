package com.intelligence.browser.ui.home.navigation;

import static com.intelligence.browser.data.RecommendUrlEntity.WEIGHT_BOOKMARK_WEBSITE;
import static com.intelligence.browser.data.RecommendUrlEntity.WEIGHT_HOT_WEBSITE;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.intelligence.browser.controller.BackgroundHandler;
import com.intelligence.browser.database.DatabaseManager;
import com.intelligence.browser.R;
import com.intelligence.browser.ui.adapter.RecommendAdapter;
import com.intelligence.browser.data.RecommendUrlEntity;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.InputMethodUtils;
import com.intelligence.browser.utils.RecommendUrlUtil;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.utils.WebIconUtils;
import com.intelligence.browser.ui.widget.AnimationListener;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.componentlib.viewpagetabbar.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.List;

public class NavigationEditView extends RelativeLayout implements WebNavigationEditable, ViewPager.OnPageChangeListener,View.OnFocusChangeListener, DatabaseManager.DataChangeListener<RecommendUrlEntity> {

    private static final int RECOMMEND_COLUMN_PORTRAIT = 5;
    private static final int NAVIGATION_SCROLL_DISTANCE = DisplayUtil.dip2px(BrowserApplication.getInstance(), 260f);
    private static int NAVIGATION_MARGIN_TOP_ON_EDIT = DisplayUtil.dip2px(BrowserApplication.getInstance(), 0f);
    private static final long ICON_TRANSLATE_TOTAL_TIME = 240L;
    private final int[] iconTranslateDelayTimes = {48, 24, 0, 24, 48, 96, 84, 72, 84, 96, 126, 126, 126, 126, 126};
    private static final int KEY_BOARD_SCROLL_TIME = 500;
    private static final int INDEX_OF_INPUT_PAGE = 2;
    private static final int TRANSFER_ON_EDIT_DELAY = 68;
    private static final int NAV_TRANSFER_OFF_EDIT_DELAY = 60;
    private static final int NAV_TRANSFER_OFF_EDIT_TIME = 250;
    private static final int NAV_SHOW_DISMISS_TIME = 300;
    private static final int VIEW_PAGER_UP_SCROLL_TIME = 300;
    private static final int VIEW_PAGER_UP_SCROLL_DELAY_PORTRAIT = 420;
    private static final int VIEW_PAGER_UP_SCROLL_DELAY_LANDSCAPE = 480;
    private static final int VIEW_PAGER_DOWN_SCROLL_TIME = 250;
    private static final int KEYBOARD_NOTIFY_DELAY = 100;

    private Context mActivity;
    private LayoutInflater mInflater;
    private WebNavigationListener mWebNavigationListener;
    private RelativeLayout mRootView;
    private RecyclerView mNavRecycler;
    private int mNavColumnOnLandscapeEdit;
    private RecommendAdapter mRecommendAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private FrameLayout mViewPagerLayout;
    private LinearLayout mViewPagerContainer;
    private ViewPager mViewPager;
    private EditNavigationView mEditNavigationView;
    private Scroller mScroller;
    private ArrayList<Fragment> mPageList = new ArrayList<>();
    private AddFromBookmarkPage mAddFromBookmarkPage;
    private AddFromHistoryPage mAddFromHistoryPage;
    private AddNewNavigationPage mAddNewNavigationPage;
    private DatabaseManager mDbManager = DatabaseManager.getInstance();
    private AddNewNavigationPage.OnFocusChangeListener mOnFocusChangeListener;

    private int currWindowBottom = 0;
    private int mNavColumn;

    private EditText mTitle;
    private EditText mAddress;
    private View mLine1, mLine2;
    private TextView mOk, mCancel;

    private boolean mIsNavListFull;

//    private WebNavigationEditable mNavigationEditable;

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
            if (visitHeight <= 0) {
                onKeyBoardHint();
            } else {
                onKeyBoardShow();
            }
        }
    };

    public boolean mIsEdit;

    public boolean isAutoEdit(){
        return mIsEdit;
    }

    public void showEditCardView(boolean isShowEdit) {
        try {
            mIsEdit = isShowEdit;
            mEditCardLayout.setVisibility(isShowEdit ? VISIBLE : GONE);
            if (isShowEdit) {
                mTitle.post(new Runnable() {
                    @Override
                    public void run() {
                        mTitle.requestFocus();
                        InputMethodUtils.showKeyboardForView(mTitle);
                    }
                });
            } else {
                InputMethodUtils.hideKeyboard(mTitle);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.edit_title:
                mLine1.setBackgroundResource(hasFocus ? R.color.add_book_mark_save : R.color.add_book_mark_line);
                break;
            case R.id.edit_address:
                mLine2.setBackgroundResource(hasFocus ? R.color.add_book_mark_save : R.color.add_book_mark_line);
                String url = mAddress.getText().toString();
                if (hasFocus) {
                    if (TextUtils.isEmpty(url)) {
                        String hintText = "http://";
                        mAddress.setText(hintText);
                        mAddress.setSelection(hintText.length());
                    }
                } else {
                    if (!TextUtils.isEmpty(url) && "http://".equals(url)) {
                        mAddress.setText("");
                    }
                }
                break;
        }
        onFocusChange();
    }

    private boolean checkEditTextNull() {
        return mTitle == null || mAddress == null;
    }

    public void finishEdit() {
        if (checkEditTextNull()) return;
        mTitle.setText("");
        mAddress.setText("http://");
    }

    public void exitEditState(){
        if(mRecommendAdapter != null && mRecommendAdapter.isDeleteMode()){
            mRecommendAdapter.setDeleteMode(false);
        }
    }

    public NavigationEditView(@NonNull Context context) {
        this(context, null, 0);
    }

    public NavigationEditView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigationEditView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView(Context activity) {
        this.mActivity = activity;
        NAVIGATION_MARGIN_TOP_ON_EDIT = (int) activity.getResources().getDimension(R.dimen.navigation_margin_top);
        mScroller = new Scroller(activity);
        mInflater = LayoutInflater.from(activity);
        createTouchHelper();
        createNavAdapter(activity);
        createPages();
        init(activity);
        createViewPager(activity);
        addViewPagerToLayout();
        getViewTreeObserver().addOnGlobalLayoutListener(mGlobalListener);
        refreshInputUrlAndRecommend();
        RecommendUrlUtil.addContentObserver(this);
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
                return !isEdit();
            }

        });
    }

    private void createNavAdapter(final Context context) {
        mRecommendAdapter = new RecommendAdapter(context, mItemTouchHelper,true);
        mRecommendAdapter.setIsEdit(true);
        mRecommendAdapter.registerListener(new RecommendAdapter.AdapterItemListener() {

            @Override
            public void openUrl(RecommendAdapter.CommonUrlItemViewHolder holder) {
                String url = holder.data.getUrl();
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                SchemeUtil.openWebView(getContext(), url);
//                mWebNavigationListener.openUrl(url);
            }

            @Override
            public void editNavigation(RecommendAdapter.CommonUrlItemViewHolder viewHolder) {
                if (viewHolder != null && "yunxin://browser/news?goods=1".equals(viewHolder.data.getUrl())) {
                    return;
                }
                mEditNavigationView.setVisibility(VISIBLE);
                mEditNavigationView.initNavigationItem(viewHolder);
                setViewPagerVisible(false);
                refreshBottomButton();
                showEditCardView(false);
            }

            @Override
            public void onClickAdd(RecommendAdapter.CommonUrlItemViewHolder viewHolder) {
                transferOnEditStatus();
            }

            @Override
            public void onDataSetChange() {
                mAddFromBookmarkPage.onDataSetChange();
                mAddFromHistoryPage.onDataSetChange();
                mIsNavListFull = mRecommendAdapter.isFull();
                mAddNewNavigationPage.updateData(mRecommendAdapter.getData());
                mNavRecycler.setLayoutManager(new GridLayoutManager(context, mNavColumn) {
                    @Override
                    public boolean canScrollVertically() {
                        return !DisplayUtil.isScreenPortrait(context) && canNavScroll();
                    }
                });
            }

            @Override
            public void onDeleteMode() {
                refreshBottomButton();
            }

            @Override
            public void offDeleteMode() {
                refreshBottomButton();
            }

        });
    }


    public void refreshInputUrlAndRecommend() {
        new AsyncTask<Void, Void, Void>() {
            List<RecommendUrlEntity> recommendUrls = null;
            //
            @Override
            protected Void doInBackground(Void... vs) {
                recommendUrls = RecommendUrlUtil.getLocalRecommendInfos();
                return null;
            }
            //
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                replaceData(recommendUrls);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private void replaceData(List<RecommendUrlEntity> list) {
        if (list == null) {
            return;
        }
        mRecommendAdapter.replaceData(list);
        mAddNewNavigationPage.updateData(list);
//        mIndicator.setVisibility(list.size() > RECMMEND_ITEM_COUNT - 1 ? VISIBLE : GONE);
//        if (mPagerAdapter == null) {
//            mPagerAdapter = new RecommendPagerAdapter(mActivity, handleListCount(list), this);
//            mViewPager.setAdapter(mPagerAdapter);
//        } else {
//            mPagerAdapter.updateData(handleListCount(list));
//        }
    }


    private void createPages() {
        mPageList.clear();
        mAddFromBookmarkPage = new AddFromBookmarkPage(this);
        mAddFromHistoryPage = new AddFromHistoryPage(this);
        mAddNewNavigationPage = new AddNewNavigationPage(this,mRecommendAdapter.getData());
//        mAddNewNavigationPage.setWebNavigationEditable(this);
        mPageList.add(mAddNewNavigationPage);
        mPageList.add(mAddFromBookmarkPage);
        mPageList.add(mAddFromHistoryPage);
    }

    public void onFocusChange() {
        BackgroundHandler.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mGlobalListener.onGlobalLayout();
            }
        }, KEYBOARD_NOTIFY_DELAY);
    }

    private void init(Context activity) {
        if (mViewPagerLayout != null) {
            mViewPagerLayout.removeView(mViewPagerContainer);
        }
        removeAllViews();
        boolean isScreenPortrait = DisplayUtil.isScreenPortrait(activity);
        initRootView(isScreenPortrait);
        initRecommendView(activity, isScreenPortrait);
        initEditNavigationView();
    }

    private void initRootView(boolean isScreenPortrait) {
        mRootView = (RelativeLayout) mInflater.inflate(R.layout.browser_edit_navigation, this, false);
        addView(mRootView);
    }

    private void initRecommendView(final Context context, boolean isScreenPortrait) {
        boolean isEdit = isEdit();
        mNavRecycler = findViewById(R.id.recommend_url_recycler);
        mNavColumn = RECOMMEND_COLUMN_PORTRAIT;
        mNavRecycler.setLayoutManager(new GridLayoutManager(context, RECOMMEND_COLUMN_PORTRAIT) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        layoutNavigationPortrait(isEdit);
        mNavRecycler.setAdapter(mRecommendAdapter);
        mItemTouchHelper.attachToRecyclerView(mNavRecycler);
//        mRecommendAdapter.refreshInputUrlAndRecommend();
    }

    private void moveNavRecyclerIfNeed(boolean edit) {
        if (DisplayUtil.isScreenPortrait(mActivity)) {
            throw new IllegalStateException("cannot call this method when screen portrait");
        }
        if (edit) {
            putNavToContainerOnEdit();
        } else {
            putNavToContainerOffEdit();
        }
        mNavRecycler.setLayoutManager(new GridLayoutManager(mActivity, mNavColumn) {
            @Override
            public boolean canScrollVertically() {
                return !DisplayUtil.isScreenPortrait(mActivity) && canNavScroll();
            }
        });
    }

    private void putNavToContainerOffEdit() {
    }

    private void putNavToContainerOnEdit() {
    }

    //计算横屏时编辑模式下一行放几个icon
    private int calcNavColumnOnLandscapeEdit(Context context) {
        int screenHeight = DisplayUtil.getScreenWidth(context);
        int viewPagerWidth = DisplayUtil.getDimenPxValue(context, R.dimen.nav_view_pager_landscape_width);
        int navMargin = DisplayUtil.getDimenPxValue(context, R.dimen.nav_margin_left_on_edit);
        int freeSpace = screenHeight - navMargin * 2 - viewPagerWidth;
        int itemSize = DisplayUtil.getDimenPxValue(context, R.dimen.nav_item_size);
        mNavColumnOnLandscapeEdit = freeSpace / itemSize;
        if (mNavColumnOnLandscapeEdit < 1) {
            mNavColumnOnLandscapeEdit = 1;//列数不可小于1
        }
        return mNavColumnOnLandscapeEdit;
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

    private View mRootLayout;
    private void createViewPager(Context activity) {
        mRootLayout = findViewById(R.id.root_layout);
        mRootLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                exitEditState();
            }
        });
        mViewPagerLayout = findViewById(R.id.view_pager_layout);
        mViewPagerContainer = (LinearLayout) mInflater.inflate(R.layout.browser_main_nav_edit_view_pager, mViewPagerLayout, false);
        mViewPager = mViewPagerContainer.findViewById(R.id.viewpager_combo);
        BasePagerAdapter adapter = new BasePagerAdapter(((FragmentActivity)activity).getSupportFragmentManager(), mPageList,mActivity);
        mViewPager.setAdapter(adapter);
        PagerSlidingTabStrip mIndicator = mViewPagerContainer.findViewById(R.id.tab_page_indicator_combo);
        mIndicator.setViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(this);

    }

    private void getWebIcon(Bitmap iconBmp, String url, NavigationService.OnAsyncGetWebIconCallback callback){
        if (!NavigationService.checkWebIconSize(iconBmp)) {
            return;
        }
        final byte[] iconBytes = ImageUtils.bitmapToBytes(iconBmp);
        if (iconBytes == null || iconBytes.length == 0)
            return;
        BackgroundHandler.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                callback.onGetWebIconFromNetwork(iconBytes);
            }
        });
    }


    private void addViewPagerToLayout() {
        if (mViewPagerContainer.getParent() != null) {
            throw new IllegalStateException("this method must called once after init root view");
        }
        mViewPagerLayout = findViewById(R.id.view_pager_layout);
        mViewPagerLayout.addView(mViewPagerContainer);
        setViewPagerVisible(isEdit());
    }

    private View mEditCardLayout;;

    public boolean saveEdit() {
        InputMethodUtils.hideKeyboard((Activity) mActivity);
        String title = mTitle.getText().toString();
        String url = mAddress.getText().toString();
        if (addNewNavigation(title, url, null,true)) {
            finishEdit();
            return true;
        }
        return false;
    }

    private View mEditCardView;
    private void initEditNavigationView() {
        mEditNavigationView = findViewById(R.id.edit_navigation);

        mEditCardView = findViewById(R.id.edit_navigation_botton_layout);
        mEditCardLayout = findViewById(R.id.edit_card_layout);
        mEditCardView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mEditCardLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditCardView(false);
            }
        });
        mTitle = findViewById(R.id.edit_title);
        mAddress = findViewById(R.id.edit_address);
        mTitle.setOnFocusChangeListener(this);
        mAddress.setOnFocusChangeListener(this);
        mLine1 = findViewById(R.id.add_link_line1);
        mLine2 = findViewById(R.id.add_link_line2);
        mOk = findViewById(R.id.edit_ok);
        mCancel = findViewById(R.id.edit_cancel);

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onFinishAddNewNavigation();
                finishEdit();
            }
        });
        mOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsNavListFull) {
                    ToastUtil.showShortToast(getContext(), R.string.add_up_to_15);
                    return;
                }
                if (saveEdit()) {
                    onFinishAddNewNavigation();
                }
            }
        });

        mEditNavigationView.setOnNavigationEditListener(new EditNavigationView.OnNavigationEditListener() {
            @Override
            public void onEditCompleted(RecommendAdapter.CommonUrlItemViewHolder targetItemViewHolder, String title, String url) {
                if (modifyNavigation(targetItemViewHolder.position, targetItemViewHolder.data, title, url)) {
                    mEditNavigationView.setVisibility(GONE);
                    setViewPagerVisible(true);
                    onKeyBoardHint();
                    refreshBottomButton();
                }
            }

            @Override
            public void onDelete(RecommendAdapter.CommonUrlItemViewHolder targetItemViewHolder, String title, String url) {
                int position = doUrlContained(url);
                if(position<0){
                    return;
                }
                try {
                    deleteNavigation(position);
                    mEditNavigationView.setVisibility(GONE);
                    setViewPagerVisible(true);
                    onKeyBoardHint();
                    refreshBottomButton();
                }catch (Exception e){

                }
            }

            @Override
            public void onCancel() {
                mEditNavigationView.setVisibility(GONE);
                setViewPagerVisible(true);
                onKeyBoardHint();
                refreshBottomButton();
            }
        });
    }

    private void refreshBottomButton() {
    }

    private void setViewPagerVisible(boolean show) {
            mViewPagerLayout.setVisibility(VISIBLE);

    }

    private int distanceScrollWhenKeyBoardShow() {
        return mViewPagerLayout.getTop() - DisplayUtil.dip2px(getContext(), 10);
    }

    private void onKeyBoardShow() {
        if (mEditNavigationView.getVisibility() == VISIBLE) {
            mEditNavigationView.onKeyBoardShow();
        } else {
            mRootView.scrollTo(0, distanceScrollWhenKeyBoardShow());
        }
        mAddNewNavigationPage.onKeyBoardShow();
    }

    private void onKeyBoardHint() {
        mAddNewNavigationPage.onKeyBoardHint();
        if (mEditNavigationView.getVisibility() == VISIBLE) {
            mEditNavigationView.onKeyBoardHint();
        }
        if (DisplayUtil.isScreenPortrait(getContext())) {
            mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), KEY_BOARD_SCROLL_TIME);
        }
    }

    private void transferOnEditStatus() {
        if (mWebNavigationListener != null) {
            mWebNavigationListener.onTransferOnEditStatus();
        }
        if(mAddNewNavigationPage != null){
            mAddNewNavigationPage.updateGroupSitesData(this,mRecommendAdapter.getData());
        }
        mRecommendAdapter.setIsEdit(true);
        refreshBottomButton();
            BackgroundHandler.getMainHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    navIconsScrollUpPortrait();
                }
            }, TRANSFER_ON_EDIT_DELAY);
        viewPagerScrollUp(true);
    }

    private void navIconsScrollUpPortrait() {
        int itemCount = mRecommendAdapter.getItemCount();
        final ArrayList<View> targetViews = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            RecommendAdapter.CommonUrlItemViewHolder holder = mRecommendAdapter.getViewHolderByPosition(i);
            if (holder == null) {
                break;
            }
            TranslateAnimation animation = startOneNavIconScrollAnim(holder.mItemView, iconTranslateDelayTimes[i]);
            targetViews.add(holder.mItemView);
            if (i == itemCount - 1) {
                animation.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        for (View targetView : targetViews) {
                            targetView.clearAnimation();
                        }
                        layoutNavigationPortrait(true);
                    }
                });
            }
        }
    }

    public TranslateAnimation startOneNavIconScrollAnim(final View itemView, long delayTime) {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -NAVIGATION_SCROLL_DISTANCE);
        animation.setDuration(ICON_TRANSLATE_TOTAL_TIME);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setFillAfter(true);
        animation.setStartOffset(delayTime);
        itemView.startAnimation(animation);
        return animation;
    }

    private void viewPagerScrollUp(boolean isPortrait) {
        Animation viewPagerAnim = AnimationUtils.loadAnimation(getContext(), R.anim.browser_nav_view_pager_show);
        viewPagerAnim.setDuration(VIEW_PAGER_UP_SCROLL_TIME);
        if (isPortrait) {
            viewPagerAnim.setStartOffset(VIEW_PAGER_UP_SCROLL_DELAY_PORTRAIT);
        } else {
            viewPagerAnim.setStartOffset(VIEW_PAGER_UP_SCROLL_DELAY_LANDSCAPE);
        }
        viewPagerAnim.setInterpolator(new OvershootInterpolator(1.2f));
        viewPagerAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setViewPagerVisible(true);
            }
        });
        mViewPagerLayout.startAnimation(viewPagerAnim);
    }


    private void transferOffEditStatus() {
        //viewPager 下落
        viewPagerScrollDown();
//        mAddNewNavigationPage.finishEdit();
        if (DisplayUtil.isScreenPortrait(getContext())) {
            mRecommendAdapter.setIsEdit(false);
            navIconsScrollDownPortrait();
        } else {
            navIconsScrollDownLandscape();
        }
        //图标下落完成同时 logo 搜索框渐显并下落
        mWebNavigationListener.onTransferOffEditStatus();
    }

    private void navIconsScrollDownPortrait() {
        TranslateAnimation navIconsAnim = new TranslateAnimation(0f, 0f, 0f, NAVIGATION_SCROLL_DISTANCE);
        navIconsAnim.setDuration(NAV_TRANSFER_OFF_EDIT_TIME);
        navIconsAnim.setStartOffset(NAV_TRANSFER_OFF_EDIT_DELAY);
        navIconsAnim.setInterpolator(new AccelerateInterpolator());
        navIconsAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(final Animation animation) {
                mNavRecycler.clearAnimation();
                layoutNavigationPortrait(false);
                refreshBottomButton();
            }
        });
        mNavRecycler.startAnimation(navIconsAnim);
    }

    private void navIconsScrollDownLandscape() {
        AlphaAnimation dismissAnim = new AlphaAnimation(1f, 0f);
        dismissAnim.setDuration(NAV_SHOW_DISMISS_TIME);
        final AlphaAnimation showAnim = new AlphaAnimation(0f, 1f);
        showAnim.setDuration(NAV_SHOW_DISMISS_TIME);
        dismissAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mRecommendAdapter.setIsEdit(false);
                refreshBottomButton();
                moveNavRecyclerIfNeed(false);
            }
        });
    }

    private void viewPagerScrollDown() {
        Animation viewPagerAnim = AnimationUtils.loadAnimation(getContext(), R.anim.browser_nav_view_pager_dismiss);
        viewPagerAnim.setDuration(VIEW_PAGER_DOWN_SCROLL_TIME);
        viewPagerAnim.setInterpolator(new AccelerateInterpolator());
        viewPagerAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setViewPagerVisible(false);
                mViewPager.setCurrentItem(0, false);
            }
        });
        mViewPagerLayout.startAnimation(viewPagerAnim);
    }

    private void layoutNavigationPortrait(boolean isEdit) {
        MarginLayoutParams layoutParams = (MarginLayoutParams) mNavRecycler.getLayoutParams();
        mNavRecycler.setLayoutParams(layoutParams);
        mNavRecycler.requestLayout();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int currY = mScroller.getCurrY();
            scroll(currY);
            postInvalidate();
        }
        super.computeScroll();
    }

    private void scroll(int currY) {
        mRootView.scrollTo(0, currY);
    }

    public void onResume() {
        if (mRecommendAdapter.isDeleteMode()) {
            mRecommendAdapter.setDeleteMode(false);
        }else {
            mRecommendAdapter.notifyDataSetChanged();
        }
    }

    public boolean isWebEditShowing() {
        return isEdit();
    }

    public boolean onBackKey() {
        if (mEditNavigationView != null && mEditNavigationView.getVisibility() == VISIBLE) {
            mEditNavigationView.setVisibility(GONE);
            setViewPagerVisible(true);
            refreshBottomButton();
            return true;
        }
//        if (mRecommendAdapter.isDeleteMode()) {
//            mRecommendAdapter.setDeleteMode(false);
//            refreshBottomButton();
//            return true;
//        }
//        if (mRecommendAdapter.isEdit()) {
//            transferOffEditStatus();
//            return true;
//        }
//        refreshBottomButton();
        return false;
    }

    @Override
    public int doUrlContained(String url) {
        if (url == null) return -1;
        List<RecommendUrlEntity> data = mRecommendAdapter.getData();
        for (int i = 0; i < data.size(); i++) {
            RecommendUrlEntity entity = data.get(i);
            if (TextUtils.equals(entity.getUrl(), url)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isEdit() {
        return mRecommendAdapter != null && mRecommendAdapter.isEdit();
    }

    private boolean checkNavigationInput(String title, String url) {
        if (TextUtils.isEmpty(title)) {
            ToastUtil.showShortToast(mActivity, R.string.right_recommend_add_link_not_title);
            return true;
        }

        if (TextUtils.isEmpty(url)) {
            ToastUtil.showShortToast(mActivity, R.string.right_recommend_add_link_not_url);
            return true;
        }
        return false;
    }

    @Override
    public boolean addNewNavigation(String title, String url, String logo, boolean needCheck) {
        try {
            if (needCheck && checkNavigationInput(title, url)) return false;
            if (mRecommendAdapter.isFull()) {
                ToastUtil.showShortToast(getContext(), R.string.add_up_to_15);
                return false;
            }
            final RecommendUrlEntity info = new RecommendUrlEntity();
            info.setDisplayName(title);
            info.setUrl(url);
            setlogoByte(info, logo);
            List<RecommendUrlEntity> list = mDbManager.findByArgs(RecommendUrlEntity.class,
                    RecommendUrlEntity.Column.URL + " = ? AND " +
                            RecommendUrlEntity.Column.DISPLAY_NAME + " = ? ", new String[]{url, title});
            if (list == null || list.size() == 0) {
                if (TextUtils.isEmpty(logo)) {
                    setNavIcon(info, url);
                }
                DatabaseManager.getInstance().insert(info);
                return true;
            } else {
                ToastUtil.showShortToast(mActivity, R.string.right_recommend_add_link_repeat);
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    private void setlogoByte(final RecommendUrlEntity info,String logo){
        if(TextUtils.isEmpty(logo)){
            return;
        }
        info.setWeight(WEIGHT_HOT_WEBSITE);

        int resID = getContext().getResources().getIdentifier(logo,
                "drawable", getContext().getPackageName());
        Drawable drawable = getContext().getResources().getDrawable(resID);
        info.setImageIcon(ImageUtils.bitmapToBytes(drawableToBitmap(drawable)));
    }

    public  Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap((int) getContext().getResources().getDimension(R.dimen.nav_icon_size),
                (int) getContext().getResources().getDimension(R.dimen.nav_icon_size), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void setNavIcon(final RecommendUrlEntity info, String url) {
        Bitmap hotWebIconByUrl = NavigationService.getHotWebIconByUrl(mActivity, url);
        if (hotWebIconByUrl != null) {
            info.setWeight(WEIGHT_HOT_WEBSITE);
            info.setImageIcon(ImageUtils.bitmapToBytes(hotWebIconByUrl));
        } else {
            Bitmap iconBmp = WebIconUtils.getWebIconFromLocalDb(mActivity, url);
            byte[] webIconLocal = ImageUtils.bitmapToBytes(iconBmp);
            if (webIconLocal != null && webIconLocal.length != 0) {
                info.setWeight(WEIGHT_BOOKMARK_WEBSITE);
            } else {
                webIconLocal = NavigationService.loadWebsiteIconAndSave(mActivity, url, new NavigationService.OnAsyncGetWebIconCallback() {
                    @Override
                    public void onGetWebIconFromNetwork(@NonNull byte[] webIcon) {
                        if(webIcon == null){
                        }else {
                            //网络获取后更新数据并通知UI更新
                            info.setImageIcon(webIcon);
                            DatabaseManager.getInstance().updateBy(info);
                        }
                    }
                });
            }
            info.setImageIcon(webIconLocal);
        }
    }

    @Override
    public void onFinishAddNewNavigation() {
        showEditCardView(false);
        mViewPager.setCurrentItem(0, false);
    }

    @Override
    public boolean modifyNavigation(int position, RecommendUrlEntity entity, String newTitle, String newUrl) {
        if (checkNavigationInput(newTitle, newUrl)) return false;
        entity.setDisplayName(newTitle);
        entity.setUrl(newUrl);
        setNavIcon(entity, newUrl);
        return mDbManager.updateBy(entity) >= 1;
    }

    @Override
    public boolean deleteNavigation(int position) {
        RecommendUrlEntity entity = mRecommendAdapter.getData(position);
        if(entity != null) {
            return mDbManager.deleteById(RecommendUrlEntity.class, entity.getId()) >= 1;
        }else {
            return false;
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position != INDEX_OF_INPUT_PAGE) {
            InputMethodUtils.hideKeyboard((Activity) mActivity);
        }
        showEditCardView(false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public interface WebNavigationListener {

        void openUrl(String url);

        void onTransferOnEditStatus();

        void onTransferOffEditStatus();

    }

    static class BasePagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> mPageList;
        private ArrayList<CharSequence> mPageTitle = new ArrayList<>();

        public BasePagerAdapter(FragmentManager fm, ArrayList<Fragment> pageList,Context context) {
            super(fm);
            mPageList = pageList;
            mPageTitle.add(context.getResources().getString(R.string.browser_navigation));
            mPageTitle.add(context.getResources().getString(R.string.menu_browser_bookmarks));
            mPageTitle.add(context.getResources().getString(R.string.menu_browser_history));
        }

        @Override
        public Fragment getItem(int position) {
            return mPageList.get(position);
        }

        @Override
        public int getCount() {
            return mPageList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPageTitle.get(position);
        }
    }

    @Override
    public void onInsertToDB(RecommendUrlEntity entity) {
        if(mRecommendAdapter != null){
            mRecommendAdapter.onInsertToDB(entity);
        }
    }

    @Override
    public void onUpdateToDB(RecommendUrlEntity entity) {
        if(mRecommendAdapter != null){
            mRecommendAdapter.onUpdateToDB(entity);
        }
    }

    @Override
    public void onDeleteToDB(RecommendUrlEntity entity) {
        if(mRecommendAdapter != null){
            mRecommendAdapter.onDeleteToDB(entity);
        }
    }
}
