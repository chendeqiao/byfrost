package com.intelligence.browser.ui.home.mostvisited;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.intelligence.browser.R;
import com.intelligence.browser.historybookmark.BrowserHistoryPage;
import com.intelligence.browser.utils.ImageBackgroundGenerator;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.view.RoundImageView;
import com.intelligence.commonlib.tools.SchemeUtil;

public class MostVisitedAdapter extends RecyclerView.Adapter<MostVisitedAdapter.ViewHolder> implements View
        .OnClickListener {
    private final LayoutInflater mLayoutInflater;
    private onClickMostVisitedUrl mOnClickListener;
    private Context mContext;
    private Cursor mMostVisited;

    public MostVisitedAdapter(Context context) {
        mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    public void setOnclickListener(onClickMostVisitedUrl onclickListener){
        mOnClickListener = onclickListener;
    }

    public interface onClickMostVisitedUrl {
        void openMostVisitedUrl(String url);
    }
    @Override
    public MostVisitedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.browser_item_most_visited, parent, false);
        return new MostVisitedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MostVisitedAdapter.ViewHolder holder, int position) {
        final Cursor cursor = mMostVisited;
        cursor.moveToPosition(position);
        holder.mTextView.setText(cursor.getString(BrowserHistoryPage.HistoryQuery.INDEX_TITE));
        final String url = cursor.getString(BrowserHistoryPage.HistoryQuery.INDEX_URL);
        holder.mUrlText.setText(SchemeUtil.hideLocalUrl(url));
        byte[] data = cursor.getBlob(BrowserHistoryPage.HistoryQuery.TOUCH_ICON);
//        byte[] favicon = cursor.getBlob(BrowserHistoryPage.HistoryQuery.FAVICON);

        holder.mImageView.setBackgroundBg(Color.WHITE);
        holder.mImageView.setImageDrawable(null);
        if (data != null) {
            holder.setFavicon(ImageUtils.decodeByteToBitmap(data));
        } else {
            String urltemp = cursor.getString(BrowserHistoryPage.HistoryQuery.INDEX_URL);
            holder.setFavicon(urltemp);
        }
        holder.mItem.setTag(url);
        holder.mItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnClickListener != null){
                    mOnClickListener.openMostVisitedUrl(view.getTag()+"");
                }
            }
        });
        holder.setShowDivider(position%2 == 0);
        //this contentValues is saved hitory record‘s temp data
//        ContentValues valuesTemp;
//        if (item.mCloseTv.getTag() == null) {
//            valuesTemp = new ContentValues();
//        } else {
//            valuesTemp = (ContentValues) item.mCloseTv.getTag();
//        }
//        valuesTemp.put(Combined._ID, cursor.getInt(HistoryQuery.INDEX_ID));

//        item.mCloseTv.setTag(valuesTemp);
//        final String deleteUrl = item.getUrl();
//        item.mCloseTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ContentValues values = (ContentValues) v.getTag();
//                clearData(0, (Integer) values.get(BrowserContract.History._ID));
//            }
//        });
//
    }

    @Override
    public int getItemCount() {
        if (mMostVisited == null) {
            return 0;
        }
        return mMostVisited.getCount();
    }

    @Override
    public void onClick(View view) {
    }


    void changeMostVisitedCursor(Cursor cursor) {
        if (mMostVisited == cursor) {
            return;
        }
        mMostVisited = cursor;
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return isMostVisitedEmpty();
    }

    private boolean isMostVisitedEmpty() {
        return mMostVisited == null
                || mMostVisited.isClosed()
                || mMostVisited.getCount() == 0;
    }
//    public void setData(ArrayList hotwordlist) {
//        if (CollectionUtils.isEmpty(hotwordlist)) {
//            return;
//        }
//        notifyDataSetChanged();
//    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;
        private TextView mUrlText;
        private RoundImageView mImageView;
        private View mItem;
        private View mDivider;

        void setFavicon(String url) {
            mImageView.setDefaultIconByUrl(url);
        }

        void setFavicon(Bitmap b){
            if (b != null) {
                mImageView.setRoundBg(ImageBackgroundGenerator.getBackgroundColor(b));
                mImageView.setImageBitmap(b);
            } else {
                mImageView.setImageResource(R.drawable.browser_search_url_icon);
            }
        }

        void setShowDivider(boolean isshow){
            mDivider.setVisibility(isshow?View.VISIBLE:View.GONE);
        }

        public ViewHolder(View itemView) {
            super(itemView);
            mItem = itemView.findViewById(R.id.layout_history_item);
            mTextView = itemView.findViewById(R.id.title_history_item);
            mUrlText = itemView.findViewById(R.id.url_history_item);
            mImageView = itemView.findViewById(R.id.logo_history_item);
            mDivider = itemView.findViewById(R.id.search_most_visited_divider);
        }
    }
}
