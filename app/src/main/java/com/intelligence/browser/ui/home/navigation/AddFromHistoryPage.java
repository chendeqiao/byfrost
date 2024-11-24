package com.intelligence.browser.ui.home.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.intelligence.browser.R;
import com.intelligence.browser.historybookmark.BrowserHistoryPage;
import com.intelligence.browser.database.provider.BrowserContract;
import com.intelligence.browser.utils.ColorUtils;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.ThreadedCursorAdapter;
import com.intelligence.browser.view.RoundImageView;
import com.intelligence.commonlib.tools.UrlUtils;

@SuppressLint("ValidFragment")
public class AddFromHistoryPage extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOGTAG = AddFromHistoryPage.class.getSimpleName();
    private View mRoot;
    private ListView mListView;
    private View mEmptyView;
    private BookmarksAdapter mCursorAdapter;
    private WebNavigationEditable mEditable;

    @SuppressLint("ValidFragment")
    public AddFromHistoryPage(WebNavigationEditable editable) {
        this.mEditable = editable;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRoot != null) {
            return mRoot;
        }
        mRoot = inflater.inflate(R.layout.browser_add_from_history_page, container, false);
        mEmptyView = mRoot.findViewById(android.R.id.empty);
        mListView = mRoot.findViewById(R.id.history_list);
        mCursorAdapter = new BookmarksAdapter(getActivity());
        mListView.setAdapter(mCursorAdapter);
        // Start the loaders
        LoaderManager lm = getLoaderManager();
        lm.restartLoader(0, null, this);
        return mRoot;
    }

    @Override
    public void onLoaderReset(Loader loader) {
        if (mCursorAdapter != null) {
            mCursorAdapter.changeCursor(null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri.Builder combinedBuilder = BrowserContract.History.CONTENT_URI.buildUpon();
        String sort = BrowserContract.Combined.DATE_LAST_VISITED + " DESC";
        String where = BrowserContract.Combined.DATE_LAST_VISITED + " > 0";
        return new CursorLoader(getActivity(), combinedBuilder.build(),
                BrowserHistoryPage.HistoryQuery.PROJECTION, where, null, sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mListView.setVisibility(View.VISIBLE);
        mCursorAdapter.changeCursor(cursor);
        boolean empty = mCursorAdapter.isEmpty();
        mEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);

    }

    public void onDataSetChange() {
        if (mCursorAdapter != null) {
            mCursorAdapter.notifyDataSetChanged();
        }
    }

    class BookmarksAdapter extends ThreadedCursorAdapter<HistoryBean> {

        private LayoutInflater mInflater;

        BookmarksAdapter(Context context) {
            super(context, null);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, ViewGroup parent) {
            return mInflater.inflate(R.layout.browser_add_from_bookmark_item, parent, false);
        }

        private ViewHolder createViewHolder(View itemView) {
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.mItemView = itemView;
            viewHolder.mContent = itemView.findViewById(R.id.content);
            viewHolder.mIconView = itemView.findViewById(R.id.bookmark_item_icon);
            viewHolder.mTitleView = itemView.findViewById(R.id.bookmark_item_title);
            viewHolder.mSelectedView = itemView.findViewById(R.id.bookmark_item_complete);
            viewHolder.mPlaceView = itemView.findViewById(R.id.place_view);
            itemView.setTag(viewHolder);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mEditable.isEdit()) return;
                    HistoryBean data = viewHolder.mData;
                    if (data != null) {
                        int position = mEditable.doUrlContained(data.mUrl);
                        if (position == -1) {
                            boolean isAddSuccess = mEditable.addNewNavigation(data.mTitle, data.mUrl, null,false);
                            if(isAddSuccess) {
                                viewHolder.onSelect(true);
                            }
                        } else {
                            boolean isDeleteSuccess = mEditable.deleteNavigation(position);
                            if(isDeleteSuccess) {
                                viewHolder.onSelect(false);
                            }
                        }
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void bindView(View view, int position, HistoryBean item) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (viewHolder == null) {
                viewHolder = createViewHolder(view);
            }
            viewHolder.mData = item;
            if (item.mTitle != null) {
                viewHolder.mTitleView.setText(item.mTitle);
            }
            viewHolder.mIconView.setImageBitmap(null);
            if (item.mIcon == null) {
                viewHolder.mIconView.setDefaultIconByUrl(item.mUrl);
            } else {
                viewHolder.mIconView.setImageBitmap(item.mIcon);
            }
            viewHolder.onSelect(mEditable.doUrlContained(item.mUrl) != -1);
            if (position == getCount() - 1) {
                viewHolder.mPlaceView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mPlaceView.setVisibility(View.GONE);
            }
        }

        @Override
        public HistoryBean getRowObject(Cursor cursor, HistoryBean item) {
            if (item == null) {
                item = new HistoryBean();
            }
            item.mUrl = cursor.getString(BrowserHistoryPage.HistoryQuery.INDEX_URL);
            item.mTitle = cursor.getString(BrowserHistoryPage.HistoryQuery.INDEX_TITE);
            if(TextUtils.isEmpty(item.mTitle)) {
                item.mTitle = UrlUtils.getHost(item.mUrl);
            }
            byte[] data = cursor.getBlob(BrowserHistoryPage.HistoryQuery.TOUCH_ICON);
//            byte[] favicon = cursor.getBlob(BrowserHistoryPage.HistoryQuery.FAVICON);
            if (data != null) {
                item.mIcon = ImageUtils.decodeByteToBitmap(data);
            }
            return item;
        }


        @Override
        public HistoryBean getLoadingObject() {
            return new HistoryBean();
        }

        @Override
        protected long getItemId(Cursor c) {
            return c.getInt(BrowserHistoryPage.HistoryQuery.INDEX_ID);
        }
    }

    static class ViewHolder {
        HistoryBean mData;
        View mItemView;
        View mContent;
        RoundImageView mIconView;
        TextView mTitleView;
        ImageView mSelectedView;
        View mPlaceView;

        void onSelect(boolean select) {
            mSelectedView.setSelected(select);
            if (select) {
                mContent.setBackgroundColor(ColorUtils.getColor(R.color.settings_list_divider));
            } else {
                mContent.setBackgroundColor(ColorUtils.getColor(R.color.white));
            }
        }
    }

    static class HistoryBean {
        public String mTitle;
        public Bitmap mIcon;
        public String mUrl;
    }

}
