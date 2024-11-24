/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.intelligence.browser.webview;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.ui.BrowserPhoneUi;
import com.intelligence.browser.R;
import com.intelligence.browser.base.UI;
import com.intelligence.browser.base.UiController;
import com.intelligence.browser.ui.home.ImmersiveController;
import com.intelligence.browser.view.InputWordDeleteView;
import com.intelligence.browser.view.carousellayoutmanager.CarouselLayoutManager;
import com.intelligence.browser.view.carousellayoutmanager.CarouselRecyclerView;
import com.intelligence.browser.view.carousellayoutmanager.CarouselZoomPostLayoutListener;
import com.intelligence.browser.view.carousellayoutmanager.CenterScrollListener;

public class TabNavScreen extends RelativeLayout
        implements OnClickListener, OnMenuItemClickListener {

    UiController mUiController;
    BrowserPhoneUi mUi;
    Activity mActivity;
    ImageView mNewTab;
    ImageView mBack;
    InputWordDeleteView mTabModeSwith;
    CarouselRecyclerView mTabsRecyclerView;
    RelativeLayout mTabBar;
    private CarouselTabAdapter mTabAdapter;
    private CarouselLayoutManager mTabLayoutManager;
    private ItemTouchHelper mItemTouchHelper;
    int mOrientation;
    boolean mNeedsMenu;
    private boolean mBlockEvents = false;
    boolean mShowNavAnimating = false;
    FrameLayout mTabsContent;
    private View mRootView;

    public TabNavScreen(Activity activity, UiController ctl, BrowserPhoneUi ui) {
        super(activity);
        mActivity = activity;
        mUiController = ctl;
        mUi = ui;
        mOrientation = activity.getResources().getConfiguration().orientation;
        init();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mUiController.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mBlockEvents || super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        return mBlockEvents || super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mBlockEvents || super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        return mBlockEvents || super.dispatchTrackballEvent(ev);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        return mBlockEvents || super.dispatchGenericMotionEvent(ev);
    }

    public void setBlockEvents(boolean block) {
        mBlockEvents = block;
    }

    @Override
    protected void onConfigurationChanged(Configuration newconfig) {
    }

    public void refreshAdapter() {
        int position = mUiController.getTabControl().getCurrentPosition();
        mTabsRecyclerView.scrollToPosition(position + 1);
        mTabAdapter.notifyDataSetChanged();
    }

    public void setShowNavScreenAnimating(boolean showNavScreenAnimating) {
        mShowNavAnimating = showNavScreenAnimating;
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.browser_nav_screen_view, this);
        setContentDescription(getContext().getResources().getString(
                R.string.accessibility_transition_navscreen));
        mNewTab = findViewById(R.id.newtab);
        mRootView = findViewById(R.id.nav_screen);
        mRootView.setOnClickListener(this);
        mNewTab.setOnClickListener(this);
        mBack = findViewById(R.id.back_ui);
        mBack.setOnClickListener(this);
        mTabsContent = findViewById(R.id.tabs_content);
        mTabsContent.setOnClickListener(this);
        mTabModeSwith = findViewById(R.id.tab_mode_switch);
        mTabModeSwith.setStyle(InputWordDeleteView.STYLE_TABS_PAGE);
        mTabModeSwith.setTipsContent(getContext().getString(R.string.browser_tab_close_all));
        mTabModeSwith.setOnclickListener(new InputWordDeleteView.onDeleteWordClick() {
            @Override
            public void DeleteAllData() {
                if(mTabAdapter != null){
                    mTabAdapter.closeAllTabView();
                }
            }

            @Override
            public void clickIcon() {

            }
        });
        mTabBar = findViewById(R.id.tabbar);
        mTabsRecyclerView = findViewById(R.id.tabs_recyclerview);
        mTabAdapter = new CarouselTabAdapter(getContext(), mUiController, mUi, this, mTabsRecyclerView);
        initRecyclerView(mTabsRecyclerView, new CarouselLayoutManager(mOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ? CarouselLayoutManager
                .VERTICAL : CarouselLayoutManager.HORIZONTAL, false), mTabAdapter);
        mNeedsMenu = !ViewConfiguration.get(getContext()).hasPermanentMenuKey();
//        if (mUiController.getTabControl().isIncognitoShowing()) {
//            showIncognitoTabMode();
//        } else {
            showNormalTabMode();
//        }
    }

    private void initRecyclerView(final RecyclerView recyclerView, final CarouselLayoutManager layoutManager, final
    CarouselTabAdapter adapter) {
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());

        recyclerView.setLayoutManager(layoutManager);
        mTabLayoutManager = layoutManager;
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        int position = mUiController.getTabControl().getCurrentPosition();
        recyclerView.scrollToPosition(position + 1);
        recyclerView.addOnScrollListener(new CenterScrollListener());
        int swipeDirection  = layoutManager.getOrientation() == CarouselLayoutManager.VERTICAL ? ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT : ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, swipeDirection) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView
                    .ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == 0 || position == adapter.getItemCount() - 1) {
                    return;
                }
                mTabAdapter.onCloseTab(mUiController.getTabControl().getTab(viewHolder.getAdapterPosition() - 1));
                mTabAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                if (!mUiController.getTabControl()
                        .hasAnyOpenNormalTabs()) {
                    mUi.createNewTabWithNavScreen(false);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float
                    dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (layoutManager.getOrientation() == CarouselLayoutManager.VERTICAL) {
                    viewHolder.itemView.setAlpha(1 - Math.abs(dX) / viewHolder.itemView.getWidth() * 0.3f);
                } else {
                    viewHolder.itemView.setAlpha(1 - Math.abs(dY) / viewHolder.itemView.getHeight());
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                viewHolder.itemView.setAlpha(1);
                float originalElevation;
                originalElevation = ViewCompat.getElevation(viewHolder.itemView);
                super.clearView(recyclerView, viewHolder);
                ViewCompat.setElevation(viewHolder.itemView, originalElevation);
                viewHolder.itemView.setTag(androidx.recyclerview.R.id.item_touch_helper_previous_elevation,
                        originalElevation);
            }
        };
        mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onClick(View v) {
        if(mTabModeSwith != null){
            mTabModeSwith.startAnim(false);
        }
        if (mNewTab == v) {
            if (!mUiController.getTabControl().canCreateNewTab()) {
                mUi.showMaxTabsWarning();
                return;
            }
            mUi.createNewTabWithNavScreen(mUiController.getTabControl().isIncognitoShowing());
        } else if (mTabModeSwith == v) {
            deleteAllTabs();
        } else if (mBack == v) {
            Tab currTab = mUiController.getTabControl().getCurrentTab();
            if (currTab != null && currTab.isNativePage()) {
                mUi.panelSwitchHome(mUiController.getTabControl().getTabPosition(currTab), true);
            } else if (currTab != null) {
                mUi.panelSwitch(UI.ComboHomeViews.VIEW_WEBVIEW, mUiController.getTabControl().getTabPosition(currTab)
                        , true);
            }
        }
    }

    public View getTabBar() {
        return mTabBar;
    }

    public void showIncognitoTabMode() {
//        ImmersiveController.getInstance().changeStatus();
//        mTabModeSwith.setImageResource(R.drawable.ic_browser_label_switch);
    }

    public void showNormalTabMode() {
        ImmersiveController.getInstance().changeStatus();
//        mTabModeSwith.setImageResource(R.drawable.ic_browser_input_search_delete);
        if(mTabModeSwith != null){
            mTabModeSwith.restoreState();
        }
        mUi.adHintVisible(false,"");
    }

    public  void close(int position) {
        close(position, true);
    }

    public  void close(int position, boolean animate) {
        mUi.hideNavScreen(position, animate);
    }

    public void onTabCountUpdate(int tabCount) {
    }

    public View getTabView(Tab tab) {
        View v = null;
        if (mTabLayoutManager != null) {
            v = mTabLayoutManager.findViewByPosition(mUiController.getTabControl().getTabPosition(tab) + 1);
        }
        return v;
    }

    private void deleteAllTabs(){

    }
}
