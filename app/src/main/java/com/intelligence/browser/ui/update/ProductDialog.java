package com.intelligence.browser.ui.update;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.intelligence.browser.ui.MainActivity;
import com.intelligence.browser.ui.activity.BrowserProductActivity;
import com.intelligence.browser.R;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.ui.widget.BrowserDialog;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

public class ProductDialog extends BrowserDialog {
    private Button mPositiveBotton;
    private Button mNegativeeBotton;
    private TextView mProductContent;

    public ProductDialog(Context context) {
        super(context, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private boolean isSecound = false;

    private Activity mActivity;
    public void setmNegativeeBotton(Activity activity){
        mActivity = activity;
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.browser_product_dialog, null);
        setBrowserContentView(view);

        mPositiveBotton = view.findViewById(R.id.star_positive);
        mProductContent = view.findViewById(R.id.product_content);
        mProductContent.setMovementMethod(LinkMovementMethod.getInstance());
        mNegativeeBotton = view.findViewById(R.id.star_negative);
        mNegativeeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSecound) {
                    if(mActivity != null && mActivity instanceof MainActivity) {
                        ((MainActivity)mActivity).exitApp();
                    }
//                    dismiss();
                } else {
                    isSecound = true;
                    mNegativeeBotton.setText("仍不同意");
                    ToastUtil.show("不同意相关协议将无法使用该APP.");
                }
            }
        });
        mPositiveBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                SharedPreferencesUtils.setPrivateProduct(true);
            }
        });

        UnderlineSpan underlineSpan = new UnderlineSpan();

        BackgroundColorSpan colorSpan = new BackgroundColorSpan(getContext().getResources().getColor(R.color.browser_title_blue));
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD_ITALIC);
//        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(BrowserConfig.BROWSER_PRODUCT_TIPS);
//
//        String tips = BrowserConfig.BROWSER_PRODUCT_TIPS;
//        int start = tips.indexOf("《");
//        int end = tips.indexOf("》");
//        SpannableString spanableInfo = new SpannableString(
//                BrowserConfig.BROWSER_PRODUCT_TIPS);
//        spanableInfo.setSpan(new ClickableSpan() {
//                                 @Override
//                                 public void onClick(@NonNull View view) {
//                                     openProductActivity(0);
//                                 }
//                             }, start, end+1,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spanableInfo.setSpan(new ClickableSpan() {
//                                 @Override
//                                 public void onClick(@NonNull View view) {
//                                     openProductActivity(1);
//                                 }
//                             }, start + 7, end + 8,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        mProductContent.setText(spanableInfo);
    }

    private void openProductActivity(int type){
        Intent intent = new Intent(getContext(), BrowserProductActivity.class);
        intent.putExtra("producttype",type);
        getContext().startActivity(intent);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismiss();
    }

    @Override
    public void show() {
        setAttributes();
        super.show();
    }

    public void onConfigurationChanged() {
        setAttributes();
    }

    private void setAttributes() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER);
    }
}
