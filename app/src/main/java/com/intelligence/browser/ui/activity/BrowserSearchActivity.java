package com.intelligence.browser.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.intelligence.browser.R;
import com.intelligence.browser.database.DatabaseManager;
import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.ui.BaseUi;
import com.intelligence.browser.ui.adapter.UrlSortAdapter;
import com.intelligence.browser.data.InputUrlEntity;
import com.intelligence.browser.data.UrlInfo;
import com.intelligence.browser.database.SqlBuild;
import com.intelligence.browser.ui.home.mostvisited.MostVisitedAdapter;
import com.intelligence.browser.ui.home.mostvisited.MostVisitedView;
import com.intelligence.browser.ui.home.search.FlowLayout;
import com.intelligence.browser.ui.home.search.InputWord;
import com.intelligence.browser.ui.home.search.InputWordView;
import com.intelligence.browser.ui.search.SearchEnginePreference;
import com.intelligence.browser.ui.search.SelectEnginePopWindow;
import com.intelligence.browser.utils.InputMethodUtils;
import com.intelligence.browser.utils.RecommendUrlUtil;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.view.InputWordDeleteView;
import com.intelligence.browser.view.SearchInputHintView;
import com.intelligence.commonlib.Global;
import com.intelligence.commonlib.tools.SchemeUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.tools.UrlUtils;
import com.intelligence.news.news.header.BrowserHisotryView;
import com.intelligence.news.websites.bean.WebSiteData;
import com.intelligence.browser.ui.BaseActivity;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BrowserSearchActivity extends BaseActivity implements View.OnClickListener, TextWatcher, InputWordView
        .HotWordModeListener, SearchInputHintView.IHintClickListener {
    private static final int WAIT_TIME = 10;
    private boolean mIncognito = false;

    private Handler mSearchHandler;
    private EditText mSearchView;

    private View mSearchContainer;
    private ListView mUrlList;
    private ImageView mClearText;
    private FlowLayout mFlowLayout;
    private UrlSortAdapter mAdapter;
    private ImageView mPerformUrl;
    private FrameLayout mReminderLayout;
    private String mUrl;
    private View mRootView;
    private int mTouchSlop;

    private int mCurrHeight = -1;
    private SearchInputHintView mInputHintView;
    private InputWordDeleteView mDeleteAll;
    private boolean isFirstEnter = false;
    private ImageView mSearchHotWord;
    private RelativeLayout mContainer;
    private ImageView mSearchEngineIcon;
    private RelativeLayout mDeleteAllLayout;
    private View mInputWordLayout;
    private MostVisitedView mMostVisitedView;
    private View mIncognitoView;
    private View mSearchEmptyScrollView;
    private TextView mSearchCopyUrl;
    private BrowserHisotryView mBrowserHistory;
    private View mBrowserHistoryLine;

    private View mSearchCopyPaste;
    private View mCopyArraw;
    private View mMostVisited;
    private View mSearchPasteIcon;
    private View mSearchCopyIcon;

    private ViewTreeObserver.OnGlobalLayoutListener mGlobalListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mRootView == null) return;
            Rect r = new Rect();
            mRootView.getWindowVisibleDisplayFrame(r);
            int visitHeight = mRootView.getBottom() - r.bottom;
            if (mCurrHeight == visitHeight) {
                return;
            }
            mCurrHeight = visitHeight;
            if (visitHeight == 0 && mInputHintView != null) {
                mInputHintView.setVisibility(View.GONE);
            } else {
                mInputHintView.setVisibility(View.VISIBLE);
            }
            mSearchContainer.scrollTo(0, (visitHeight));

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_url_search_view);
        mSearchContainer = findViewById(R.id.search_root);
        mCopyArraw = findViewById(R.id.search_copy_arraw);
        mSearchCopyUrl = findViewById(R.id.search_copy_url);
        mCopyArraw.setOnClickListener(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMostVisitedView = (MostVisitedView) fragmentManager.findFragmentById(R.id.search_most_visited_layout);
        mMostVisitedView.setOnclickListener(new MostVisitedAdapter.onClickMostVisitedUrl() {
            @Override
            public void openMostVisitedUrl(String url) {
                openUrl(url);
            }
        });

        mSearchEngineIcon = findViewById(R.id.select_search_engine_icon);
        SearchEnginePreference.getDefaultSearchIcon(this, mSearchEngineIcon);
        mSearchEngineIcon.setOnClickListener(this);
        mRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalListener);
        initActionBar();
        init();
        Global.addActivity(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void init() {
        mTouchSlop = ViewConfiguration.get(BrowserSearchActivity.this).getScaledTouchSlop();
        mUrl = getIntent().getStringExtra(BaseUi.INPUT_SEARCH_URL);
        mSearchContainer.setOnClickListener(this);
        mSearchView = findViewById(R.id.et_view_url_search);
        mSearchCopyPaste =  findViewById(R.id.search_copy_paste);
        mSearchPasteIcon = findViewById(R.id.search_paster_icon);
        mSearchCopyIcon = findViewById(R.id.search_copy_icon);
        mSearchPasteIcon.setOnClickListener(this);
        mSearchCopyIcon.setOnClickListener(this);
        mIncognitoView =  findViewById(R.id.incognito_icon);
        mIncognito = BrowserSettings.getInstance().noFooterBrowser();
        mIncognitoView.setVisibility(mIncognito?View.VISIBLE:View.GONE);
        mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_PHONETIC);
        if (TextUtils.getLayoutDirectionFromLocale(this.getResources().getConfiguration().locale) == View.LAYOUT_DIRECTION_RTL) {
            mSearchView.setTextDirection(View.TEXT_DIRECTION_LTR);
            mSearchView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        }
        mUrlList = findViewById(R.id.view_url_list);
        mClearText = findViewById(R.id.iv_view_url_txt_clear);
        mInputHintView = findViewById(R.id.search_hint_view);
        mFlowLayout = findViewById(R.id.input_url_flow);
        mReminderLayout = findViewById(R.id.url_search_candidate);
        mContainer = findViewById(R.id.view_url_search_container);
        mContainer.setOnClickListener(this);
        mPerformUrl = findViewById(R.id.perform_search_url);
        mSearchHotWord = findViewById(R.id.search_hot_word);
//        if (SharedPreferencesUtils.getRecommonWebsites()) {
//            mSearchHotWord.setImageDrawable(BrowserSearchActivity.this.getResources().getDrawable(R.drawable.hot_word_icon));
//        }else {
            mSearchHotWord.setImageDrawable(BrowserSearchActivity.this.getResources().getDrawable(R.drawable.browser_search_go_back_icon));
//        }
        mBrowserHistory =  findViewById(R.id.browser_history_view);
        mBrowserHistoryLine =  findViewById(R.id.browser_history_view_line);
        mSearchHotWord.setOnClickListener(this);
        mPerformUrl.setVisibility(View.GONE);
        mPerformUrl.setOnClickListener(this);
        mAdapter = new UrlSortAdapter(this, null, this);
        mUrlList.setAdapter(mAdapter);
        initListener();
        touchListener(mUrlList);
        mSearchHandler = new Handler();
        mSearchView.requestFocus();
        if (TextUtils.isEmpty(mUrl) || (mUrl.contains("file://") && mUrl.contains("files/"))) {
            mUrl = "";
        }
        try {
            if (mUrl != null) {
                isFirstEnter = true;
//            mSearchView.setText(mUrl);
//            mSearchView.setSelection(0, mUrl.length());
                if (!TextUtils.isEmpty(mUrl.toString().trim())) {
                    mSearchCopyUrl.setText(mUrl);
                    mSearchCopyPaste.setVisibility(View.VISIBLE);
                    isShowWebUrl = true;
                } else {
                    mSearchCopyPaste.setVisibility(View.GONE);
                }
            }
        }catch (Exception e){
        }
        mInputHintView.registerHintListener(this);
        if(SharedPreferencesUtils.getRecommonWebsites()) {
            requestData();
        }else {
            mBrowserHistory.setVisibility(View.GONE);
            mBrowserHistoryLine.setVisibility(View.GONE);
        }
    }

    private boolean isShowWebUrl;

    private void requestData() {
//        if (BrowserApplication.getInstance().isChina()) {
//            WebsitesHttpRequest websitesHttpRequest = new WebsitesHttpRequest();
//            DataResult dataResult = websitesHttpRequest.parseData(SharedPreferencesUtils.getNavigationCache(BrowserInitDataContants.BROWSER_NEW_HEADER), true);
//            if (dataResult.websites != null) {
//                mBrowserHistory.setHeightMargin(ScreenUtils.dpToPxInt(this, 2), ScreenUtils.dpToPxInt(this, 2));
//                mBrowserHistory.setData(dataResult.browserhistory);
//            }
//        } else {
            ArrayList<WebSiteData> webSiteData = RecommendUrlUtil.getWebsites(BrowserSearchActivity.this, R.array.search_lables);
            if (webSiteData != null) {
                mBrowserHistory.setHeightMargin(ScreenUtils.dpToPxInt(this, 2), ScreenUtils.dpToPxInt(this, 2));
                mBrowserHistory.setData(webSiteData);
            }
//        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus && !isShowWebUrl) {
            initCopyPaste();
        }
    }

    private void initCopyPaste() {
        String pasteContent = getClipBoardContent();
        String lastPaste = (String) SharedPreferencesUtils.get(SharedPreferencesUtils.SEARCH_WORD_LAST_PASTE, "");
        if (!TextUtils.isEmpty(pasteContent) && !pasteContent.equals(lastPaste)) {
            mSearchCopyUrl.setText(pasteContent);
            mSearchCopyPaste.setVisibility(View.VISIBLE);
            SharedPreferencesUtils.put(SharedPreferencesUtils.SEARCH_WORD_LAST_PASTE, pasteContent);
        }else {
            mSearchCopyPaste.setVisibility(View.GONE);
        }
    }

    private void copyUrl(String url){
        try {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newUri(getContentResolver(), "uri", Uri.parse(url)));
            ToastUtil.showLongToast(this, R.string.copylink_success);
        }catch (Exception e){
        }
    }

    private String getClipBoardContent() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            CharSequence text = clipData.getItemAt(0).getText();
            if(text != null) {
                String pasteContent = text.toString();
                return pasteContent;
            }
        }
        return "";
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.browser_url_search_input_enter, R.anim.browser_url_search_input_exit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InputMethodUtils.hideKeyboard(this);
    }

    @Override
    public void onBackPressed() {
        if (mDeleteAll != null && mDeleteAll.mIsEdit()) {
            setInputSearchState(true);
            mDeleteAll.startAnim(false);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initHotWord();
        initFlowLayout();
    }

    private void initListener() {
        mUrlList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
                UrlSortAdapter.ViewHolder viewHolder = (UrlSortAdapter.ViewHolder) view.getTag();
                fireSearch(viewHolder.tvUrl.getText().toString());
            }
        });
        mClearText.setOnClickListener(this);
        mSearchView.addTextChangedListener(this);

        mSearchView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_ENTER:
                        String content = mSearchView.getText().toString().trim();
                        if (content != null && content.length() != 0) {
                            fireSearch(content);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void addInputWordView(List<InputWord> inputWordList) {
        if (inputWordList == null) {
            return;
        }
        Collections.reverse(inputWordList);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < inputWordList.size(); i++) {
            final InputWordView textView = new InputWordView(this);
            textView.setData(inputWordList.get(i).getmIndex(), inputWordList.get(i).getmInputWord());
            textView.setModeListener(this);
            mFlowLayout.addView(textView, i, lp);
        }
        if (TextUtils.isEmpty(mSearchView.getText().toString().trim())) {
            mInputWordLayout.setVisibility(isHasChildView() ? View.VISIBLE : View.GONE);
//            mFlowLayout.setVisibility(isHasChildView() ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 默认是进入８个默认输入网址的
     *
     * @param content
     */
    private void fireSearch(String content) {
        boolean isInputUrl = true;
        if(content.contains(SchemeUtil.BROWSER_SCHEME_PATH)){
           jumpNativePage(content);
           return;
        }
        if (UrlUtils.isSearch(content)) {
            String filterUrl = com.intelligence.browser.utils.UrlUtils.filterBySearchEngine(this, content);
            if (filterUrl != null) {
                content = filterUrl;
                isInputUrl = true;
            }
        }
        /**
         * TODO URL的过滤处理
         * 自定义的一些scheme , 如 ： abc://
         */
        finishActivity(content, isInputUrl);
    }

    private void jumpNativePage(String url){
        SchemeUtil.jumpToScheme(this,url);
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        mReminderLayout.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(mSearchView.getText().toString().trim())) {
            mUrlList.setVisibility(View.GONE);
            mFlowLayout.setVisibility(View.VISIBLE);
            mClearText.setVisibility(View.GONE);
            mSearchHotWord.setVisibility(View.VISIBLE);
            mPerformUrl.setVisibility(View.GONE);
            mInputWordLayout.setVisibility(isHasChildView() ? View.VISIBLE : View.GONE);
            mSearchEmptyScrollView.setVisibility(View.VISIBLE);
            mIncognitoView.setVisibility(mIncognito?View.VISIBLE:View.GONE);
        } else {
            mSearchEmptyScrollView.setVisibility(View.GONE);
            mUrlList.setVisibility(View.VISIBLE);
            mFlowLayout.setVisibility(View.GONE);
            mClearText.setVisibility(View.VISIBLE);
            mPerformUrl.setVisibility(View.VISIBLE);
            mSearchHotWord.setVisibility(View.GONE);
            mIncognitoView.setVisibility(View.GONE);
        }
        arterTextChangeData(arg0);
    }


    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        //
    }

    @Override
    public void afterTextChanged(Editable e) {

    }

    private void arterTextChangeData(CharSequence e){
        try {
//        if (!isFirstEnter) {
            mSearchHandler.removeCallbacks(runnable);
            mSearchHandler.postDelayed(runnable, WAIT_TIME);
            String content = mSearchView.getText().toString();
//        } else {
//            isFirstEnter = false;
//        }
            if (mInputHintView != null && e.toString().length() == 0) {
                mInputHintView.setDataBefore();
            } else if (mInputHintView != null && e.toString().length() > 0) {
                mInputHintView.setDataAfter();
            }
        }catch (Exception exception){

        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            String content = mSearchView.getText().toString();
            mAdapter.refrush(content);
        }
    };

    private float mDownTouchX;
    private float mDownTouchY;
    boolean isTouchHistory;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                isTouchHistory = false;
                mDownTouchX = ev.getRawX();
                mDownTouchY = ev.getRawY();
                int[] location = new int[2];
                mBrowserHistory.getLocationOnScreen(location);
                int y = location[1];
                isTouchHistory = mDownTouchY >= y;
                break;
            case MotionEvent.ACTION_MOVE:
                if(!isTouchHistory && (Math.abs(ev.getRawX() - mDownTouchX) > mTouchSlop || Math.abs(ev.getRawY() - mDownTouchY) > mTouchSlop)) {
                    InputMethodUtils.hideKeyboard(BrowserSearchActivity.this);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.perform_search_url:
                String content = mSearchView.getText().toString().trim();
                if (!TextUtils.isEmpty(content))
                    fireSearch(content);
                break;
            case R.id.iv_view_url_txt_clear:
                mSearchView.setText("");
                break;
            case R.id.actionbar_right_icon:
                deleteAllInputWordData();
                break;
            case R.id.select_url:
                UrlInfo mContent = (UrlInfo) v.getTag();
                mSearchView.setText(mContent.getUrl());
                mSearchView.setSelection(mContent.getUrl().length());
                break;
            case R.id.search_hot_word:
//                if (SharedPreferencesUtils.getRecommonWebsites()) {
//                    SchemeUtil.jumpHotWorsPage(BrowserSearchActivity.this, 2);
//                    Global.clearActivity();
//                } else {
                    finish();
//                }
                break;
            case R.id.actionbar_left_icon:
                finish();
                break;
            case R.id.search_root:
                setInputSearchState(true);
                if (mDeleteAll != null) {
                    mDeleteAll.startAnim(false);
                }
                break;
            case R.id.search_delete_all:
                deleteAllInputWordData();
                break;
            case R.id.input_word_tips_layout:
                setInputSearchState(true);
                mDeleteAll.startAnim(false);
                break;
            case R.id.select_search_engine_icon:
                SelectEnginePopWindow searchEngineWindow = new SelectEnginePopWindow(BrowserSearchActivity.this, new BaseUi.SelectSearchEngineListener() {
                    @Override
                    public void updateSearchEngine() {
                        SearchEnginePreference.getDefaultSearchIcon(BrowserSearchActivity.this, mSearchEngineIcon);
                    }
                });
                searchEngineWindow.show(mSearchEngineIcon,0);
                break;
            case R.id.search_copy_arraw:
                try {
                    mSearchCopyPaste.setVisibility(View.GONE);
                    mSearchView.setText(mSearchCopyUrl.getText());
                    mSearchView.setSelection(mSearchView.getText().toString().trim().length());
                }catch (Exception e){

                }
                break;
            case R.id.search_paster_icon:
                openUrl(mSearchCopyUrl.getText()+"");
                break;
            case R.id.search_copy_icon:
                copyUrl(mSearchCopyUrl.getText()+"");
                break;

        }
    }

    private void setInputSearchState(boolean isreset) {
        if (mFlowLayout == null) {
            return;
        }
        int count = mFlowLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mFlowLayout.getChildAt(i);
            if (view instanceof InputWordView) {
                ((InputWordView) view).resetState(isreset);
            }
        }
    }

    @Override
    protected void onPause() {
//        InputMethodUtils.hideKeyboard(this);
        super.onPause();
    }

    public void setIncognito(boolean incognito) {
        setSearchViewStyle(incognito);
    }

    private void setSearchViewStyle(boolean incognito) {
    }

    private void touchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE:
//                        InputMethodUtils.hideKeyboard(BrowserSearchActivity.this);
                        break;
                }
                return false;
            }
        });
    }

    private void finishActivity(String url, boolean isInputUrl) {
        SchemeUtil.jumpToScheme(BrowserSearchActivity.this,SchemeUtil.BROWSER_SCHEME_PATH_HOME+"&url="+URLEncoder.encode(url)+"&isInputUrl="+isInputUrl+"&inputword="+URLEncoder.encode(mSearchView.getText().toString())+"&fromsource="+SchemeUtil.BROWSER_SOURCE_KEY_SEARCH);
//        SchemeUtil.jumpToScheme(BrowserSearchActivity.this,SchemeUtil.BROWSER_SCHEME_PATH_HOME+"&url="+URLEncoder.encode(url));
        Global.clearActivity();
        finish();
    }

    @Override
    public void onHintClick(String text) {
        int index = mSearchView.getText().toString().length();
        mSearchView.setSelection(index, index);
        Editable editable = mSearchView.getText();
        editable.insert(index, text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_combined, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar() {
        mDeleteAll = findViewById(R.id.search_delete_all);
        mDeleteAllLayout = findViewById(R.id.search_delete_all_layout);
        mInputWordLayout =  findViewById(R.id.input_word_tips_layout);
        mInputWordLayout.setOnClickListener(this);
        mSearchEmptyScrollView =  findViewById(R.id.search_input_empty_scroll);
        mDeleteAll.setOnClickListener(this);
        mDeleteAll.setOnclickListener(new InputWordDeleteView.onDeleteWordClick(){
            @Override
            public void DeleteAllData() {
                deleteAllInputWordData();
            }

            @Override
            public void clickIcon() {
                setInputSearchState(false);
            }
        });
        touchListener(mInputWordLayout);
    }

    private List getInputWordData() {
        List<InputWord> list = new ArrayList<>();
        try {
            Cursor cursor = DatabaseManager.getInstance().findBySql(SqlBuild.INPUT_word_SQL, null);
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        InputWord iw = new InputWord();
                        iw.setmIndex(cursor.getInt(0));
                        iw.setmContent(cursor.getString(1));
                        iw.setmInputWord(cursor.getString(2));
                        if (!TextUtils.isEmpty(iw.getmInputWord())) {
                            list.add(iw);
                        }
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        } catch (Exception e) {
            //this is not need handle
        }
        return list;
    }

    private void initHotWord(){

    }

    private void initFlowLayout() {
        if (mFlowLayout != null) {
            mFlowLayout.removeAllViews();
        }
        addInputWordView(getInputWordData());
    }

    private void deleteInputWordData(int id) {
        DatabaseManager.getInstance().deleteById(InputUrlEntity.class, id);
    }

    private void deleteAllInputWordData() {
        DatabaseManager.getInstance().deleteAllData(InputUrlEntity.class);
        mFlowLayout.removeAllViews();
        mInputWordLayout.setVisibility(View.GONE);
    }

    @Override
    public void openUrl(String url) {
        fireSearch(url);
    }

    @Override
    public void deleteWord(int id) {
        deleteInputWordData(id);
        deleteFlowLayout(id);
        mInputWordLayout.setVisibility(isHasChildView() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void ChangeEdit() {
        setInputSearchState(false);
        mDeleteAll.startAnim(true);
    }

    private void deleteFlowLayout(int id) {
        if (mFlowLayout == null) {
            return;
        }
        int count = mFlowLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mFlowLayout.getChildAt(i);
            if (view instanceof InputWordView && ((InputWordView) view).getIndex() == id) {
                mFlowLayout.removeView(view);
            }
        }
    }
    private boolean isHasChildView() {
        return mFlowLayout.getChildCount() > 0;
    }
}
