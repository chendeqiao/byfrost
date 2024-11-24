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

package com.intelligence.browser.downloads;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.android.gms.common.util.CollectionUtils;
import com.intelligence.browser.R;
import com.intelligence.browser.historybookmark.BookmarkItem;
import com.intelligence.browser.base.CombinedBookmarksCallbacks;
import com.intelligence.browser.ui.activity.BrowserDownloadActivity;
import com.intelligence.browser.utils.BrowserHandler;
import com.intelligence.browser.database.provider.SnapshotProvider.Snapshots;
import com.intelligence.browser.utils.ImageBackgroundGenerator;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.view.RoundImageView;
import com.intelligence.browser.view.SearchHistoryBar;
import com.intelligence.commonlib.tools.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class BrowserSnapshotPage extends Fragment implements
        LoaderManager.LoaderCallbacks {

    public static final String EXTRA_ANIMATE_ID = "animate_id";
    private static final int LOADER_SNAPSHOTS = 1;
    private static final String[] PROJECTION = new String[]{
            Snapshots._ID,
            Snapshots.TITLE,
            Snapshots.VIEWSTATE_SIZE,
            Snapshots.THUMBNAIL,
            //regard this filed to Touch icon
            Snapshots.FAVICON,
            Snapshots.URL,
            Snapshots.DATE_CREATED,
            Snapshots.VIEWSTATE_PATH,
    };
    private static final int SNAPSHOT_ID = 0;
    private static final int SNAPSHOT_TITLE = 1;
    private static final int SNAPSHOT_VIEWSTATE_SIZE = 2;
    private static final int SNAPSHOT_THUMBNAIL = 3;
    private static final int SNAPSHOT_TOUCHICON = 4;
    private static final int SNAPSHOT_URL = 5;
    private static final int SNAPSHOT_DATE_CREATED = 6;
    private static final int SNAPSHOT_VIEWSTATE_PATH = 7;

    ListView mList;
    View mEmpty;
    SnapshotAdapter mAdapter;
    CombinedBookmarksCallbacks mCallback;
    long mAnimateId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallback = (CombinedBookmarksCallbacks) getActivity();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mAnimateId = bundle.getLong(EXTRA_ANIMATE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browser_snapshots_page, container, false);
        mEmpty = view.findViewById(android.R.id.empty);
        mList = view.findViewById(R.id.snapshot_listview);
        SearchHistoryBar mSearchHistoryBar = view.findViewById(R.id.snapshots_search_bar);
        mSearchHistoryBar.setSearchwordChangeLisenter(new SearchHistoryBar.SearchwordChangeLisenter() {
            @Override
            public void onChange(CharSequence s) {
                Bundle bundle = new Bundle();
                bundle.putCharSequence(SearchHistoryBar.KEY_ARGS,s);
                getLoaderManager().restartLoader(LOADER_SNAPSHOTS, bundle, BrowserSnapshotPage.this);
            }
        });
        mSearchHistoryBar.setListView(mList);
        setupGrid(inflater);
        getLoaderManager().initLoader(LOADER_SNAPSHOTS, null, this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().destroyLoader(LOADER_SNAPSHOTS);
        if (mAdapter != null) {
            mAdapter.changeCursor(null);
            mAdapter = null;
        }
    }

    void setupGrid(LayoutInflater inflater) {
        View item = inflater.inflate(R.layout.browser_snapshot_item, mList, false);
    }

    @Override
    public void onLoadFinished(androidx.loader.content.Loader loader, Object data) {
        if (loader.getId() == LOADER_SNAPSHOTS) {
            if (mAdapter == null) {
                mAdapter = new SnapshotAdapter(getActivity(), (Cursor) data,mSearchWord);
                mList.setAdapter(mAdapter);
            } else {
                mAdapter.changeCursor((Cursor) data,mSearchWord);
            }

            boolean empty = mAdapter.isEmpty();
            mList.setVisibility(empty ? View.GONE : View.VISIBLE);
            mEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            ((BrowserDownloadActivity)getActivity()).setDeleteIconVisible(!empty);
        }
    }

    @Override
    public void onLoaderReset(androidx.loader.content.Loader loader) {

    }

    private String mSearchWord;
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_SNAPSHOTS) {
            CharSequence keyword;
            String key = "";
            mSearchWord = "";
            if(args != null) {
                keyword = args.getCharSequence(SearchHistoryBar.KEY_ARGS);
                if (!TextUtils.isEmpty(keyword)) {
                    key = "%" + keyword + "%";
                    mSearchWord = keyword + "";
                }
            }
            if (TextUtils.isEmpty(key)) {
                return new CursorLoader(getActivity(),
                        Snapshots.CONTENT_URI, PROJECTION,
                        null, null, Snapshots.DATE_CREATED + " DESC");
            } else {
                return new CursorLoader(getActivity(),
                        Snapshots.CONTENT_URI, PROJECTION,
                        "(url LIKE ? OR title LIKE ?)", new String[]{key, key}, Snapshots.DATE_CREATED + " DESC");
            }
        }
        return null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.browser_snapshots_context, menu);
        // Create the header, re-use BookmarkItem (has the layout we want)
        BookmarkItem header = new BookmarkItem(getActivity());
        header.setEnableScrolling(true);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        populateBookmarkItem(mAdapter.getItem(info.position), header);
        menu.setHeaderView(header);
    }

    private void populateBookmarkItem(Cursor cursor, BookmarkItem item) {
        item.setName(cursor.getString(SNAPSHOT_TITLE));
        item.setUrl(cursor.getString(SNAPSHOT_URL));
        item.setFavicon(getBitmap(cursor, SNAPSHOT_TOUCHICON));
    }

    static Bitmap getBitmap(Cursor cursor, int columnIndex) {
        byte[] data = cursor.getBlob(columnIndex);
        if (data == null) {
            return null;
        }
        return ImageUtils.decodeByteToBitmap(data);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_context_menu_id) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            deleteSnapshot(info.id);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    void deleteSnapshot(long id) {
        final Uri uri = ContentUris.withAppendedId(Snapshots.CONTENT_URI, id);
        final ContentResolver cr = getActivity().getContentResolver();
        new Thread() {
            @Override
            public void run() {
                cr.delete(uri, null, null);
            }
        }.start();
    }

    public void removeAllSelected() {
        if (mAdapter != null && !CollectionUtils.isEmpty(mAdapter.mSnapShotList)) {
            for (String s : mAdapter.mSnapShotList) {
                deleteSnapshot(Long.parseLong(s));
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public boolean isEmpty() {
        return mAdapter == null || mAdapter.getCount() == 0;
    }

    private class SnapshotAdapter extends ResourceCursorAdapter {
        private List<String> mSnapShotList = new ArrayList<>();
        private String mKeyWord;
        public SnapshotAdapter(Context context, Cursor c,String keyWord) {
            super(context, R.layout.browser_snapshot_item, c, 0);
            mKeyWord = keyWord;
        }

        public void changeCursor(Cursor cursor,String keyword) {
            super.changeCursor(cursor);
            mKeyWord = keyword;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            int position = cursor.getPosition();
            long id = cursor.getLong(SNAPSHOT_ID);
            if (mSnapShotList != null && !mSnapShotList.contains(String.valueOf(id))) {
                mSnapShotList.add(id + "");
            }
            RoundImageView thumbnail = view.findViewById(R.id.snap_item_thumb);
            thumbnail.setRoundBg(Color.WHITE);
            thumbnail.setImageDrawable(null);

            RelativeLayout parentView = view.findViewById(R.id.snap_item_parent);
            TextView title = view.findViewById(R.id.snap_item_title);

            String titleName = TextUtils.isEmpty(cursor.getString(SNAPSHOT_TITLE)) ? getActivity().getResources()
                    .getString(R.string.snap_shot_no_title) : cursor.getString(SNAPSHOT_TITLE);
            title.setText(TextUtils.isEmpty(mSearchWord) ? titleName : StringUtil.spanWrap(title.getContext(), mSearchWord, titleName));

            ImageView more = view.findViewById(R.id.snapshot_more);
            more.setImageResource(R.drawable.browser_home_close);
            parentView.setOnClickListener(new SnapshotItemClick(id, cursor, position));
            more.setOnClickListener(new SnapshotItemClick(id, cursor, position));
            parentView.setOnLongClickListener(new SnapshotItemLongClickListener(id, cursor, position));

            byte[] thumbBlob = cursor.getBlob(SNAPSHOT_TOUCHICON);
            byte[] thumb = cursor.getBlob(SNAPSHOT_THUMBNAIL);
            thumbnail.setImageDrawable(null);

            if (thumbBlob != null) {
                Bitmap thumbBitmap = ImageUtils.decodeByteToBitmap(thumbBlob);
                if (thumbBitmap != null) {
                    thumbnail.setRoundBg(ImageBackgroundGenerator.getBackgroundColor(thumbBitmap));
                    thumbnail.setImageBitmap(thumbBitmap);
                } else {
                    thumbnail.setDefaultIconByUrl(cursor.getString(SNAPSHOT_URL));
                }
            } else {
                if(thumb !=  null){
                    Bitmap thumbBitmap = ImageUtils.decodeByteToBitmap(thumb);
                    if (thumbBitmap != null) {
                        thumbnail.setRoundBg(ImageBackgroundGenerator.getBackgroundColor(thumbBitmap));
                        thumbnail.setImageBitmap(thumbBitmap);
                    } else {
                        thumbnail.setDefaultIconByUrl(cursor.getString(SNAPSHOT_URL));
                    }
                }
                thumbnail.setDefaultIconByUrl(cursor.getString(SNAPSHOT_URL));
            }
        }

        @Override
        public Cursor getItem(int position) {
            return (Cursor) super.getItem(position);
        }

        private class SnapshotItemClick implements View.OnClickListener {
            long mId;
            Cursor mCursor;
            int mPosition;

            public SnapshotItemClick(long id, Cursor cursor, int pos) {
                mId = id;
                mCursor = cursor;
                mPosition = pos;
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.snap_item_parent:
                        BrowserHandler.getInstance().handlerPostDelayed(new Runnable() {
                            @Override
                            public void run() {
                                    mCallback.openSnapshot(mId);
                            }
                        }, 200);
                        break;
                    case R.id.snapshot_more:
                        deleteSnapshot(mId);
                        break;
                    default:
                        break;
                }
            }
        }

        private class SnapshotItemLongClickListener implements View.OnLongClickListener {
            long mId;
            Cursor mCursor;

            public SnapshotItemLongClickListener(long id, Cursor cursor, int pos) {
                mId = id;
                mCursor = cursor;
            }

            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        }
    }
}
