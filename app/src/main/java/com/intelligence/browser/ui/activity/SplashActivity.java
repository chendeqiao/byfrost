package com.intelligence.browser.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelligence.browser.controller.BackgroundHandler;
import com.intelligence.browser.ui.MainActivity;
import com.intelligence.browser.R;
import com.intelligence.browser.markLock.util.MarkLockJumper;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.download.Wink;
import com.intelligence.browser.ui.BaseActivity;

public class SplashActivity extends BaseActivity implements Runnable {

    private static final int LOADING_TIME_FAST = 1200;
    private MarkLockJumper markLock;
    private View mPolicyLayout;
    private Button mStartBotton;
    private TextView mContent1;
    private TextView mContent2;
    private ImageView mCheckBox1;
    private View mLogoLayout;

    private LinearLayout mBrowserSlogo;
    private ImageView mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            BrowserApplication.getInstance().setIsChina(BrowserApplication.isSystemLanguageChinese(this));
//            overridePendingTransition(R.anim.start_page_in, R.anim.start_page_out);
            setContentView(R.layout.browser_start_page);
            mLogoLayout = findViewById(R.id.title_layout);
            mName = findViewById(R.id.name);
            mBrowserSlogo = findViewById(R.id.browser_slogo);
            mBrowserSlogo.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        int width = mBrowserSlogo.getWidth();  // 获取 LinearLayout 的宽度
                        ViewGroup.LayoutParams params = mName.getLayoutParams();
                        params.width = width;  // 设置图片宽度与 LinearLayout 的宽度一致
                        mName.setLayoutParams(params);
                    }catch (Exception e){
                        startApp();
                    }
                }
            });
            mPolicyLayout = findViewById(R.id.policy_layout);
            mStartBotton = findViewById(R.id.start_botton);

            mCheckBox1 = findViewById(R.id.checkoutbox1);
            mCheckBox1.setSelected(true);
            mCheckBox1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCheckBox1.setSelected(!mCheckBox1.isSelected());
                }
            });

            mContent1 = findViewById(R.id.content_item1);

            handleTextPolicacy(getResources().getString(R.string.browser_product_tips), mContent1);

            mStartBotton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCheckBox1.isSelected()) {
                        SharedPreferencesUtils.put(SharedPreferencesUtils.FIVE_STAR_LAST_TIME,System.currentTimeMillis());
                        startApp();
                    } else {
                        ToastUtil.showLongToast(SplashActivity.this, R.string.browser_product_agree_tips);
                    }
                }
            });
            outIntent = isOutSiteOpenApp();
            if (SharedPreferencesUtils.getPrivateProduct()) {
                BackgroundHandler.getMainHandler().postDelayed(this, LOADING_TIME_FAST);
                mLogoLayout.setPadding(0, 0, 0, ScreenUtils.dpToPxInt(SplashActivity.this, 60));
            } else {
                mLogoLayout.setPadding(0, 0, 0, ScreenUtils.dpToPxInt(SplashActivity.this, 120));
                mPolicyLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            startApp();
        }
    }

    private Intent outIntent;

    private Intent isOutSiteOpenApp() {
        try {
            Intent intent = getIntent();
            String action = intent.getAction();
            Uri data = intent.getData();
            String type = intent.getType();

            if (Intent.ACTION_VIEW.equals(action) && data != null && type != null && (type.startsWith("video/") || type.startsWith("audio/") || type.startsWith("image/"))) {
                // 如果是外部打开的视频文件，跳转到 VideoActivity
                Intent videoIntent = new Intent();
                videoIntent.setDataAndType(data, type);
                videoIntent.setAction(action);
                return videoIntent;
            }
        }catch (Exception e){
        }
        return null;
    }

    private void startApp(){
        BrowserApplication.getInstance().setIsFirstStart(false);
        SharedPreferencesUtils.setPrivateProduct(true);
        BackgroundHandler.getMainHandler().postDelayed(SplashActivity.this, 0);
    }

    private void handleTextPolicacy(String fullText,TextView textView){
        SpannableString spannableString = new SpannableString(fullText);

        ClickableSpan privacyPolicyClick = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openProductActivity(0);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(SplashActivity.this.getResources().getColor(R.color.ahlib_common_tabbar_color_normal_new)); // 设置文本颜色
                ds.setUnderlineText(true); // 是否显示下划线
            }
        };

        ClickableSpan userAgreementClick = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openProductActivity(1);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(SplashActivity.this.getResources().getColor(R.color.ahlib_common_tabbar_color_normal_new)); // 设置文本颜色
                ds.setUnderlineText(true); // 是否显示下划线
            }
        };

        int privacyStart = fullText.indexOf("Privacy Policy");
        int privacyEnd = privacyStart + "Privacy Policy".length();
        int agreementStart = fullText.indexOf("User Agreement");
        int agreementEnd = agreementStart + "User Agreement".length();
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        spannableString.setSpan(privacyPolicyClick, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(userAgreementClick, agreementStart, agreementEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 设置 TextView 显示 SpannableString
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance()); // 使点击生效
        textView.setHighlightColor(Color.TRANSPARENT);
    }

    private void openProductActivity(int type) {
        Intent intent = new Intent(SplashActivity.this, BrowserProductActivity.class);
        intent.putExtra(BrowserProductActivity.PRODUCT_TYPE, type);
        SplashActivity.this.startActivity(intent);
    }

    @Override
    public void run() {
        if (outIntent == null) {
            outIntent = getIntent();
        }
        outIntent.setClass(this, MainActivity.class);
        startActivity(outIntent);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.browser_start_page_in, R.anim.browser_start_page_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BackgroundHandler.getMainHandler().removeCallbacks(this);
    }
}
