package com.intelligence.browser.view.carousellayoutmanager;

import androidx.recyclerview.widget.RecyclerView;

public class CenterScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof CarouselLayoutManager)) {
            return;
        }

        final CarouselLayoutManager lm = (CarouselLayoutManager) layoutManager;
        lm.setScrollState(newState);
        if (RecyclerView.SCROLL_STATE_IDLE == newState) {
            if (lm.getItemCount() > 2 && (lm.getCenterItemPosition() == 0
                    || (lm.getOffsetForCurrentView(lm.findViewByPosition(lm.getCenterItemPosition())) < 0 && lm.getCenterItemPosition() == 1))) {
                recyclerView.smoothScrollToPosition(1);
            } else if (lm.getItemCount() > 2 && (lm.getCenterItemPosition() == lm.getItemCount() - 1
                    || (lm.getOffsetForCurrentView(lm.findViewByPosition(lm.getCenterItemPosition())) > 0 && lm.getCenterItemPosition() == lm.getItemCount() - 2))) {
                recyclerView.smoothScrollToPosition(lm.getItemCount() - 2);
            }
        }
    }
}
