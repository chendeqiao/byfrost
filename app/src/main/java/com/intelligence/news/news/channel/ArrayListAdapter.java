package com.intelligence.news.news.channel;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public abstract class ArrayListAdapter<T> extends BaseAdapter {
    public List<T> mList = new ArrayList();
    public Context mContext;
    public ListView mListView;
    protected String mKey;

    public ArrayListAdapter(Context context) {
        this.mContext = context;
    }

    public int getCount() {
        return this.mList != null ? this.mList.size() : 0;
    }

    public Object getItem(int position) {
        return this.mList == null ? null : this.mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public abstract View getView(int var1, View var2, ViewGroup var3);

    public void setList(List<T> list) {
        this.mList = list;
        this.notifyDataSetChanged();
    }

    public void insertDataAtPosition(int position, T t) {
        if (t != null) {
            if (position >= this.mList.size()) {
                this.mList.add(t);
            } else {
                this.mList.add(position, t);
            }

            this.notifyDataSetChanged();
        }
    }

    public void initData(List<T> mLists) {
        if (null != mLists && mLists.size() != 0) {
            this.mList.clear();
            this.mList.addAll(mLists);
            this.notifyDataSetChanged();
        }
    }

    public void loadMoreData(List<T> mLists) {
        if (null != mLists && mLists.size() != 0) {
            this.mList.addAll(mLists);
            this.notifyDataSetChanged();
        }
    }

    public List<T> getList() {
        return this.mList;
    }

    public void setList(T[] list) {
        ArrayList<T> arrayList = new ArrayList(list.length);
        T[] var3 = list;
        int var4 = list.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            T t = var3[var5];
            arrayList.add(t);
        }

        this.setList(arrayList);
    }

    public ListView getListView() {
        return this.mListView;
    }

    public void setListView(ListView listView) {
        this.mListView = listView;
    }
}