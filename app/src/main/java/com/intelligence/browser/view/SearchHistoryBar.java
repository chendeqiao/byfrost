package com.intelligence.browser.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;

public class SearchHistoryBar extends FrameLayout implements TextWatcher {
    private EditText mEditText;
    public static final String KEY_ARGS = "searchword";

    public SearchHistoryBar(@NonNull Context context) {
        this(context, null, 0);
    }

    public SearchHistoryBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchHistoryBar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(getContext(), R.layout.browser_search_history_bar, this);
        initView();
    }

    private View mClear;
    private View mCardLine;
    private void initView() {
        mEditText = findViewById(R.id.search_text);
        mClear = findViewById(R.id.iv_view_url_txt_clear);
        mCardLine = findViewById(R.id.search_card_line);
        mClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText != null) {
                    mEditText.setText("");
                }
            }
        });
        mEditText.addTextChangedListener(this);

        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                        String content = mEditText.getText().toString().trim();
                        if (content != null && content.length() != 0) {
                            fireSearch(content);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void fireSearch(CharSequence content) {
       if(mSearchwordChangeLisenter != null){
           mSearchwordChangeLisenter.onChange(content);
       }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(s)) {
            fireSearch("");
            mClear.setVisibility(GONE);
        } else {
            fireSearch(s+"");
            mClear.setVisibility(VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public void setSearchwordChangeLisenter(SearchwordChangeLisenter searchwordChangeLisenter){
        mSearchwordChangeLisenter = searchwordChangeLisenter;
    }

    public void setShowDividerLine(boolean isShow) {
        if(mCardLine != null){
            mCardLine.setVisibility(isShow?VISIBLE:GONE);
        }
    }
    public void setListView(ListView listView) {
        if (listView != null) {
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        try {
                            View firstVisibleView = listView.getChildAt(0);
                            if (view.getAdapter() != null && view.getAdapter().getCount() == 0) {
                                setShowDividerLine(false);
                                return;
                            }
                            if (firstVisibleView != null) {
                                // 获取第一个可见项的顶部偏移
                                int top = firstVisibleView.getTop();
                                // 计算滚动距离
                                int scrollY = -top + firstVisibleItem * firstVisibleView.getHeight();
                                if (scrollY > 20) {
                                    setShowDividerLine(true);
                                } else {
                                    setShowDividerLine(false);
                                }
                            } else {
                                setShowDividerLine(false);
                            }
                        } catch (Exception e) {
                        }
                    }
                });
        }
//            mCardLine.setVisibility(isShow ? VISIBLE : GONE);
    }

    private SearchwordChangeLisenter mSearchwordChangeLisenter;
    public interface SearchwordChangeLisenter{
        void onChange(CharSequence s);
    }
}