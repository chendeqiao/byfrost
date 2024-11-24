package com.intelligence.browser.ui.home.search;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.UrlUtils;

public class InputWordView extends FrameLayout implements View.OnClickListener {

    private ImageView mInputWordDeleteIcon;
    private TextView mInputWordContent;
    private HotWordModeListener mInputWordModeListener;
    private int mIndex;
    private RelativeLayout mLayout;
    private ImageView mInputMark;

    public InputWordView(Context context) {
        super(context);
        initView();
    }

    public interface HotWordModeListener {
        void openUrl(String url);

        void deleteWord(int id);

        void ChangeEdit();
    }

    private void initView() {
        View view = View.inflate(getContext(), R.layout.browser_input_url_view, this);
        mInputWordDeleteIcon = view.findViewById(R.id.hot_word_delete_icon);
        mInputWordContent = view.findViewById(R.id.hot_word_content);
        mLayout = view.findViewById(R.id.hot_word_layout);
        mInputMark = view.findViewById(R.id.url_search_input_icon);

//        mLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                InputMethodUtils.hideKeyboard((Activity) getContext());
//                return false;
//            }
//        });
        mLayout.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(mInputWordModeListener != null) {
                    mInputWordModeListener.ChangeEdit();
                }
                return true;
            }
        });
        mLayout.setOnClickListener(this);
        mInputWordDeleteIcon.setOnClickListener(this);
    }

    public void resetState(boolean isreset) {
        mInputWordDeleteIcon.setVisibility(isreset ? GONE : VISIBLE);
        mLayout.setBackground(isreset?getResources().getDrawable(R.drawable.browser_search_word_bg):getResources().getDrawable(R.drawable.browser_input_word_delete_bg));
    }

    public void setData(int index, String content) {
        mIndex = index;
        mInputWordContent.setText(content);
        mInputMark.setVisibility(UrlUtils.isSearch(content) ? GONE : VISIBLE);
    }

    public int getIndex(){
        return mIndex;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.hot_word_delete_icon:
                deleteWord();
                break;
            case R.id.hot_word_layout:
                openWord();
                break;
        }
    }

    private void deleteWord() {
        mInputWordModeListener.deleteWord(mIndex);
    }

    private void openWord() {
        mInputWordModeListener.openUrl(mInputWordContent.getText().toString().trim());
    }

    public void setModeListener(HotWordModeListener hotWordModeListener) {
        this.mInputWordModeListener = hotWordModeListener;
    }
}

