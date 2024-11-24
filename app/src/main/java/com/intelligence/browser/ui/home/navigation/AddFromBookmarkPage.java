package com.intelligence.browser.ui.home.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import androidx.loader.content.Loader;

import com.intelligence.browser.R;
import com.intelligence.browser.historybookmark.BookmarksLoader;
import com.intelligence.browser.historybookmark.BrowserBookmarksItem;
import com.intelligence.browser.database.provider.BrowserContract;
import com.intelligence.browser.utils.ColorUtils;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.ThreadedCursorAdapter;
import com.intelligence.browser.view.RoundImageView;
import com.intelligence.commonlib.tools.UrlUtils;

@SuppressLint("ValidFragment")
public class AddFromBookmarkPage extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public AddFromBookmarkPage(){
        super();
    }
    private View mRoot;
    private ListView mListView;
    private View mEmptyView;
    private BookmarksAdapter mCursorAdapter;
    private WebNavigationEditable mEditable;

    @SuppressLint("ValidFragment")
    public AddFromBookmarkPage(WebNavigationEditable editable) {
        this.mEditable = editable;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRoot != null) {
            return mRoot;
        }
        mRoot = inflater.inflate(R.layout.browser_add_from_bookmark_page, container, false);
        mEmptyView = mRoot.findViewById(android.R.id.empty);
        mListView = mRoot.findViewById(R.id.bookmark_list);
        mCursorAdapter = new BookmarksAdapter(getActivity());
        mListView.setAdapter(mCursorAdapter);
        // Start the loaders
        LoaderManager lm = getLoaderManager();
        lm.restartLoader(0, null, this);
        return mRoot;
    }

    public int getCount() {
        if (mCursorAdapter == null) return 0;
        if (mCursorAdapter.getCount() != 0) {
            mListView.setVisibility(View.VISIBLE);
        }
        return mCursorAdapter.getCount();
    }

    @Override
    public void onLoaderReset(Loader loader) {
        if (mCursorAdapter != null) {
            mCursorAdapter.changeCursor(null);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String accountType = "";
        String accountName = "";
        return new BookmarksLoader(getActivity(),
                accountType, accountName,null,null);
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

    class BookmarksAdapter extends ThreadedCursorAdapter<BrowserBookmarksItem> {

        private LayoutInflater mInflater;
        private Context mContext;

        public BookmarksAdapter(Context context) {
            super(context, null);
            mInflater = LayoutInflater.from(context);
            mContext = context;
        }

        @Override
        public View newView(Context context, ViewGroup parent) {
            return mInflater.inflate(R.layout.browser_add_from_bookmark_item, parent, false);
        }

        private ViewHolder createViewHolder(View itemView) {
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.mItemView = itemView;
            viewHolder.mIconView = itemView.findViewById(R.id.bookmark_item_icon);
            viewHolder.mContent = itemView.findViewById(R.id.content);
            viewHolder.mTitleView = itemView.findViewById(R.id.bookmark_item_title);
            viewHolder.mSelectedView = itemView.findViewById(R.id.bookmark_item_complete);
            viewHolder.mPlaceView = itemView.findViewById(R.id.place_view);
            itemView.setTag(viewHolder);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mEditable.isEdit()) return;
                    BrowserBookmarksItem data = viewHolder.mData;
                    if (data != null) {
                        int position = mEditable.doUrlContained(data.url);
                        if (position == -1) {
                            boolean isAddSuccess = mEditable.addNewNavigation(data.title, data.url,null, false);
                            if (isAddSuccess) {
                                viewHolder.onSelect(true);
                            }
                        } else {
                            boolean isDeleteSuccess = mEditable.deleteNavigation(position);
                            if (isDeleteSuccess) {
                                viewHolder.onSelect(false);
                            }
                        }
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void bindView(View view, int position, BrowserBookmarksItem item) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (viewHolder == null) {
                viewHolder = createViewHolder(view);
            }
            viewHolder.mData = item;
            if (item.title != null) {
                viewHolder.mTitleView.setText(item.title);
            }
            viewHolder.mIconView.setBackgroundBg(ColorUtils.getColor(R.color.snap_item_backgroud));
            viewHolder.mIconView.setImageBitmap(null);
            if (item.thumbnail == null) {
                if (item.favicon == null) {
                    viewHolder.mIconView.setDefaultIconByUrl(item.url);
                } else {
                    viewHolder.mIconView.setImageBitmap(item.favicon);
                }
            } else {
                viewHolder.mIconView.setImageBitmap(item.thumbnail);
            }
            if (mEditable != null && mEditable.doUrlContained(item.url) != -1) {
                viewHolder.mSelectedView.setSelected(true);
                viewHolder.mContent.setBackgroundColor(ColorUtils.getColor(R.color.navigation_on_select_bg));
            } else {
                viewHolder.mSelectedView.setSelected(false);
                viewHolder.mContent.setBackgroundColor(ColorUtils.getColor(R.color.white));
            }
            if (position == getCount() - 1) {
                viewHolder.mPlaceView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mPlaceView.setVisibility(View.GONE);
            }
        }

        @Override
        public BrowserBookmarksItem getRowObject(Cursor c, BrowserBookmarksItem item) {
            if (item == null) {
                item = new BrowserBookmarksItem();
            }
            item.thumbnail = null;
            item.thumbnail = getBitmap(c,
                    BookmarksLoader.COLUMN_INDEX_TOUCH_ICON, null);
            item.hasThumbnail = item.thumbnail != null;
            item.favicon = getBitmap(c,
                    BookmarksLoader.COLUMN_INDEX_FAVICON, null);
            item.isFolder = c.getInt(BookmarksLoader.COLUMN_INDEX_IS_FOLDER) != 0;
            item.url = c.getString(BookmarksLoader.COLUMN_INDEX_URL);
            item.title = getTitle(c);
            if (TextUtils.isEmpty(item.title)) {
                item.title = UrlUtils.getHost(item.url);
            }
            item.id = c.getString(BookmarksLoader.COLUMN_INDEX_ID);
            item.time = c.getLong(BookmarksLoader.COLUMN_INDEX_CREATE_TIME);
            return item;
        }

        String getTitle(Cursor cursor) {
            int type = cursor.getInt(BookmarksLoader.COLUMN_INDEX_TYPE);
            switch (type) {
                case BrowserContract.Bookmarks.BOOKMARK_TYPE_OTHER_FOLDER:
                    return mContext.getString(R.string.other_bookmarks);
            }
            return cursor.getString(BookmarksLoader.COLUMN_INDEX_TITLE);
        }

        @Override
        public BrowserBookmarksItem getLoadingObject() {
            return new BrowserBookmarksItem();
        }

        @Override
        protected long getItemId(Cursor c) {
            return c.getLong(BookmarksLoader.COLUMN_INDEX_ID);
        }
    }

    static Bitmap getBitmap(Cursor cursor, int columnIndex, Bitmap bitmap) {
        if (cursor == null) {
            return null;
        }
        byte[] data = cursor.getBlob(columnIndex);
        return ImageUtils.getBitmap(data, bitmap);
    }

    static class ViewHolder {
        BrowserBookmarksItem mData;
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

}
