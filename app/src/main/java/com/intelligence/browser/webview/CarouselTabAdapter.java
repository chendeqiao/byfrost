package com.intelligence.browser.webview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.browser.base.UI;
import com.intelligence.browser.base.UiController;
import com.intelligence.browser.ui.BrowserPhoneUi;
import com.intelligence.browser.utils.ThreadUtils;
import com.intelligence.browser.view.carousellayoutmanager.CarouselLayoutManager;
import com.intelligence.browser.view.carousellayoutmanager.CarouselRecyclerView;

public class CarouselTabAdapter extends RecyclerView.Adapter<CarouselTabAdapter.TabViewHolder> {
    private Context mContext;
    private UiController mUiController;
    private BrowserPhoneUi mUi;
    private final View mFooterView;
    private final View mHeaderView;
    private static final int GAP_SIZE = 1000;
    private static final int VIEW_TYPE_HEADER = 1000;
    private static final int VIEW_TYPE_FOOTER = 2000;
    private CarouselRecyclerView mCarouselRecyclerView;
    private TabNavScreen mTabNavScreen;

    public CarouselTabAdapter(Context context, UiController uiController, BrowserPhoneUi browserPhoneUi, TabNavScreen tabNavScreen, CarouselRecyclerView carouselRecyclerView) {
        mContext = context;
        mUiController = uiController;
        mUi = browserPhoneUi;
        mTabNavScreen = tabNavScreen;
        mCarouselRecyclerView = carouselRecyclerView;
        mFooterView = createGapView();
        mHeaderView = createGapView();
    }

    private double dpToPx(double dp) {
        Resources resources = mContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return  dp * ((double) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private View createGapView() {
        final View view = new View(mContext);
        final int width = mContext.getResources().getDimensionPixelSize(R.dimen.nav_tab_width);
        final int height = ViewGroup.LayoutParams.MATCH_PARENT;

        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(width, height);

        params.height = 1;

        view.setLayoutParams(params);

        return view;
    }


    private class FooterHolder extends TabViewHolder {
        public FooterHolder(View v) {
            super(v);
        }
    }

    private class HeaderHolder extends TabViewHolder {
        public HeaderHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_HEADER;

        if (position == getItemCount() - 1)
            return VIEW_TYPE_FOOTER;

        return super.getItemViewType(position);
    }

    @Override
    public TabViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER)
            return new HeaderHolder(mHeaderView);

        if (viewType == VIEW_TYPE_FOOTER)
            return new FooterHolder(mFooterView);
        return new TabViewHolder(LayoutInflater.from(mContext).inflate(R.layout.browser_tab_capture_view, parent, false));
    }

    @Override
    public void onBindViewHolder(final TabViewHolder holder, final int position) {
        if (position != 0 && position != getItemCount() - 1) {
            int fixedPostion = position - 1;
            final Tab tab = mUiController.getTabControl().getTab(fixedPostion);
            if (tab != null) {
                holder.mTitle.setText(tab.getTitle());
                holder.mTabIcon.setImageBitmap(tab.getFavicon());
                holder.mCloseView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mTabNavScreen.setBlockEvents(true);
                        mUiController.setBlockEvents(true);
                        setCloseTabAnimation(holder, tab);
                    }
                });
                Bitmap capture = tab.getScreenshot();
                if (capture == null && !tab.getCaptureSuccess()) {
                    capture = mUiController.getTabControl().getHomeCapture();
                }
                holder.mTabView.setImageBitmap(capture);
                holder.mTabView.setScaleType(ImageView.ScaleType.MATRIX);
                if (capture != null) {
                    float sfx = mContext.getResources().getDimensionPixelSize(R.dimen.tab_thumbnail_width) / (float) (capture.getWidth());
                    Matrix m = new Matrix();
                    m.setScale(sfx, sfx);
                    holder.mTabView.setImageMatrix(m);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (tryOpenHomeTab(tab)) return;
                        closeTabView(tab);
                    }
                });
            }
        }
    }

    private void setCloseTabAnimation(final TabViewHolder holder, final Tab tab)
    {
        holder.itemView.clearAnimation();
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.browser_slide_out_right);
        if (mCarouselRecyclerView.getLayoutManager() instanceof CarouselLayoutManager) {
            if (((CarouselLayoutManager) mCarouselRecyclerView.getLayoutManager()).getOrientation() == CarouselLayoutManager.HORIZONTAL) {
                animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.browser_slide_out_top);
            }
        }
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ThreadUtils.runOnUiThreadBlocking(new Runnable() {
                    @Override
                    public void run() {
                        onCloseTab(tab);
                        holder.itemView.setAlpha(0);
                        notifyItemRemoved(holder.getAdapterPosition());
                        mTabNavScreen.setBlockEvents(false);
                        mUiController.setBlockEvents(false);
                        if (!mUiController.getTabControl().isIncognitoShowing() && !mUiController.getTabControl().hasAnyOpenNormalTabs()) {
                            mUi.createNewTabWithNavScreen(false);
                        }
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return mUiController.getTabControl().getTabCount() + 2;
    }

    private boolean tryOpenHomeTab(Tab tab) {
        if (tab != null) {
            if (tab.isNativePage()) {
                mUiController.getTabControl().setIncognitoShowing(tab.isPrivateBrowsingEnabled());
                mUiController.loadNativePage(tab);
                return true;
            } else {
                tab.setNativePage(false);
            }
        }
        return false;
    }

    protected void closeTabView(Tab tab) {
        closeTabView(tab, true);
    }

    protected void closeTabView(Tab tab, boolean animate) {
        mUi.panelSwitch(UI.ComboHomeViews.VIEW_WEBVIEW, mUiController.getTabControl().getTabPosition(tab), animate);
    }

    public void closeAllTabView() {
        mUiController.closeAllTabs(false);
    }

    public void onCloseTab(Tab tab) {
        if (tab != null) {
            mUiController.closeTab(tab);
        }
    }

    class TabViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;
        public ImageView mCloseView;
        public ImageView mTabView;
        public ImageView mTabIcon;
        public View mItemView;

        public TabViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mTitle = itemView.findViewById(R.id.title);
            mCloseView = itemView.findViewById(R.id.close);
            mTabView = itemView.findViewById(R.id.tab_view);
            mTabIcon = itemView.findViewById(R.id.tab_icon);
        }
    }
}
