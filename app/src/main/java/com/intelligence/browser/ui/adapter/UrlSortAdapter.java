package com.intelligence.browser.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.browser.database.DatabaseManager;
import com.intelligence.browser.R;
import com.intelligence.browser.data.UrlInfo;
import com.intelligence.browser.database.BrowserSQLiteHelper;
import com.intelligence.browser.utils.SqliteEscape;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class UrlSortAdapter extends BaseAdapter {
    private static final int HEADER = 0;
    private static final int BODY = 1;
    private static final int RECOMEND_URL_TAG = 1;
    private static final int HISTORY_URL_TAG = 2;
    private static final int INPU_TURL_TAG = 3;
    private Context mContext;
    private LayoutInflater mInflater;
    private List<UrlInfo> mList;
    private List<UrlInfo> mSearchList;
    private EditText mEditView;
    private String mCurrSearchString;
    private Handler mainH = new Handler();
    private View.OnClickListener mListener;
    // 只在每次刷新之前预编译一次
    private Pattern pattern;

    public UrlSortAdapter(Context mContext, List<UrlInfo> list, View.OnClickListener listener) {
        this.mContext = mContext;
        if (list == null) {
            this.mList = new ArrayList<UrlInfo>();
        } else {
            this.mList = list;
        }
        this.mListener = listener;
    }

    /**
     * 当ListView数据发生变化时,调用此方法来更新ListView
     */
    private void updateListView() {

        if (mSearchList == null) {
            return;
        }
        Collections.reverse(this.mList = mSearchList);
        notifyDataSetChanged();

    }

    public int getCount() {
        return this.mList.size();
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        UrlInfo info = mList.get(position);
        if (info.isHeader()) {
            return HEADER;
        } else {
            return BODY;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public View getView(final int position, View view, ViewGroup arg2) {
        ViewHolder viewHolder = null;
        UrlInfo mContent = mList.get(position);
        if (this.getItemViewType(position) == HEADER) {
            if (view == null) {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(mContext).inflate(R.layout.browser_item_url_header, null);
                viewHolder.tvUrl = view.findViewById(R.id.url_item);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.tvUrl.setText(mCurrSearchString);
        } else {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.browser_item_url, null);
                viewHolder = new ViewHolder();
                viewHolder.tvTitle = view.findViewById(R.id.url_title);
                viewHolder.tvUrl = view.findViewById(R.id.url_item);
                viewHolder.selectUrl = view.findViewById(R.id.select_url);
                viewHolder.urlIcon = view.findViewById(R.id.icon_url);
                viewHolder.selectUrl.setOnClickListener(mListener);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.selectUrl.setTag(mContent);
            viewHolder.tvTitle.setText(mCurrSearchString == null ? mContent.getDisplayname() : StringUtil.spanWrap(mContext, mCurrSearchString, mContent.getDisplayname()));
            viewHolder.tvUrl.setText(SchemeUtil.hideLocalUrl(mCurrSearchString == null ? mContent.getUrl() : StringUtil.spanWrap(mContext, mCurrSearchString, mContent.getUrl())+""));
            if (mContent.getDatatype() == HISTORY_URL_TAG) {
                viewHolder.urlIcon.setImageResource(R.drawable.browser_search_url_icon);
            } else {
                viewHolder.urlIcon.setImageResource(R.drawable.browser_search_input_recommend);
            }
        }

        viewHolder.data = mContent;
        return view;

    }

    public static class ViewHolder {
        public TextView tvTitle;
        public TextView tvUrl;
        public UrlInfo data;
        public ImageView selectUrl;
        public ImageView urlIcon;
    }

    /**
     * 刷新数据
     *
     * @param keyword
     */
    public int refrush(String keyword) {
        /*
        1.  检索浏览历史数据
		2.  检索hot url列表数据
		 */
        try {
            mCurrSearchString = keyword;
            mSearchList = new ArrayList<UrlInfo>();
            if (keyword != null && !"".equals(keyword)) {
                mSearchList.add(new UrlInfo(keyword, true));
            }
            int count = findDataFromHistory(keyword, mSearchList);
            int update = findDataFromHotUrl(keyword, mSearchList);
//		Collections.sort(urls,new SortCompare());

            mainH.post(searchRun);

            return update + count;
        } catch (Exception e) {
            return 0;
        }
    }

    private Runnable searchRun = new Runnable() {
        @Override
        public void run() {
            updateListView();
        }
    };

    /**
     * 检索历史浏览记录
     *
     * @param keyword
     */
    @SuppressLint("Range")
    private int findDataFromHistory(String keyword, List<UrlInfo> baseUrl) {
        String s = SqliteEscape.encodeSql(keyword);
        String sql = String.format("select * from " + BrowserSQLiteHelper.TALBE_HISTORY_URL + " where (url LIKE '%%%s%%' OR title LIKE '%%%s%%') order by date desc limit 4", s, s);
        Cursor cursor = DatabaseManager.getInstance().findBySql(sql, null);
        int size = 0;
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }

            do {
                UrlInfo info = new UrlInfo();
                info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                info.setDisplayname(cursor.getString(cursor.getColumnIndex("title")));
                info.setDatatype(HISTORY_URL_TAG);
                baseUrl.add(info);
                size++;
            } while (cursor.moveToNext());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }

    /**
     * 检索热门的url (区分国家)
     *
     * @param keyword
     */
    @SuppressLint("Range")
    private int findDataFromHotUrl(String keyword, List<UrlInfo> baseUrl) {
        String s = SqliteEscape.encodeSql(keyword);
        String sql = String.format("select * from " + BrowserSQLiteHelper.TABLE_RECOMMEND_WEB_URL + " where  (url LIKE '%%%s%%' OR title LIKE '%%%s%%') limit 4", s, s);

        Cursor cursor = DatabaseManager.getInstance().findBySql(sql, null);
        int size = 0;
        try {
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }
            do {
                UrlInfo info = new UrlInfo();
                info.setUrl(cursor.getString(cursor.getColumnIndex("url")));
                info.setDisplayname(cursor.getString(cursor.getColumnIndex("displayname")));
                info.setDatatype(RECOMEND_URL_TAG);
                baseUrl.add(info);
                size++;
            } while (cursor.moveToNext());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return size;
    }


}
