package com.intelligence.browser.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.intelligence.browser.R;
import com.intelligence.browser.ui.adapter.ImageAdapter;
import com.intelligence.browser.utils.ActivityUtils;
import com.intelligence.browser.utils.Constants;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.utils.FormatTools;
import com.intelligence.browser.utils.InputMethodUtils;
import com.intelligence.browser.utils.ToastUtil;
import com.intelligence.browser.view.LoadingDialog;
import com.intelligence.browser.view.StateScaleButton;
import com.intelligence.commonlib.tools.NetworkUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.news.NetConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BrowserFeedbackFragment extends BasePreferenceFragment implements View.OnClickListener, ImageAdapter
        .OnImageClickListener, View.OnTouchListener, BrowserSettingActivity.OnRightIconClickListener {

    private static final int UPDATE_SUCCESS = 0;
    private static final int UPDATE_FAIL = 1;

    private static final int PICK_FROM_FILE = 1;
    private static final int IMAGE_MAX_NUM = 3;
    private static final int TEXT_MAX_NUM = 1000;
    private static final int TEXT_NUM_WHEN_TOAST = 800;

    private View mViews;
    private EditText mFeedbackContent, mEmail;
    private LinearLayout mChoose;
    private LoadingDialog mLoading;
    private BrowserSettingActivity mPreferencesPage;
    private RelativeLayout mContactUsOnFacebook;
    private TextView mTextCountLimitToast;
    private TextView mCountImageText;

    private Uri mSelectImageUri;
    private List<ImageAdapter.ImageItem> mImageList;
    private ImageAdapter mAdapter;
    private StateScaleButton mSubmitFeedBack;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        mViews = inflater.inflate(R.layout.browser_setting_feedback, container, false);
        initView();
        return mViews;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_FILE && data != null) {
            mSelectImageUri = data.getData();
            Bitmap bitmap = FormatTools.getBitmap(mPreferencesPage, mSelectImageUri);
            if (bitmap != null) {
                int imageCount = mImageList.size();
                mImageList.add(mImageList.size() - 1, new ImageAdapter.ImageItem(ImageAdapter.IMAGE_NORMAL, bitmap));
                mAdapter.setList(mImageList);
                mCountImageText.setVisibility(View.VISIBLE);
                mCountImageText.setText(getString(R.string.image_count, imageCount, IMAGE_MAX_NUM));
            } else {
                ToastUtil.showLongToast(mPreferencesPage, R.string.get_photo_failure);
            }

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String content = (String) SharedPreferencesUtils.get(Constants.FEEDBACK_CONTENT, "");
        if (!TextUtils.isEmpty(content)) {
            mFeedbackContent.setText(content);
        }
        mAdapter = new ImageAdapter(mChoose, this);
        mImageList = new ArrayList<>();
        mImageList.add(new ImageAdapter.ImageItem(ImageAdapter.IMAGE_ADD, null));
        mAdapter.setList(mImageList);
    }

    @Override
    public void onResume() {
        super.onResume();
        setBrowserActionBarTitle(getText(R.string.pref_feedback).toString());
    }


    private void initView() {
        mFeedbackContent = mViews.findViewById(R.id.feedback_content);
        mChoose = mViews.findViewById(R.id.choose_pic);
        mEmail = mViews.findViewById(R.id.email);
        mContactUsOnFacebook = mViews.findViewById(R.id.contact_us_on_facebook);
        mTextCountLimitToast = mViews.findViewById(R.id.text_count_can_input);
        mCountImageText = mViews.findViewById(R.id.image_count);

        mSubmitFeedBack = mViews.findViewById(R.id.feedback_submit_bt);
        mSubmitFeedBack.setOnClickListener(this);

        mSubmitFeedBack.setBackgroundResource(R.drawable.browser_main_statescalebtn_default_disable_shape);
        mSubmitFeedBack.setTextColor(Color.parseColor("#AEB3C4"));

        mPreferencesPage = (BrowserSettingActivity) getActivity();
//        mPreferencesPage.getRightActionbarIcon().setVisibility(View.GONE);
//        mPreferencesPage.disableRightIcons();
//        mPreferencesPage.setRightIcon(R.drawable.ic_browser_complete);
//        mPreferencesPage.setOnRightIconClickListener(this);
        mFeedbackContent.setOnClickListener(this);
        mFeedbackContent.setOnTouchListener(this);
        mEmail.setOnClickListener(this);
        mContactUsOnFacebook.setOnClickListener(this);
        mFeedbackContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int textLengthInput = s.length();
                if (textLengthInput < 6) {
                    mSubmitFeedBack.setBackgroundResource(R.drawable.browser_main_statescalebtn_default_disable_shape);
                    mSubmitFeedBack.setTextColor(Color.parseColor("#AEB3C4"));
                } else {
                    mFeedbackContent.setBackgroundResource(R.drawable.browser_feedback_edit_text_bg);
                    mSubmitFeedBack.setBackgroundResource(R.drawable.browser_main_statescalebtn_default_press_shape);
                    mSubmitFeedBack.setTextColor(Color.parseColor("#FFFFFF"));
                }
                if (textLengthInput < TEXT_NUM_WHEN_TOAST) {
                    mTextCountLimitToast.setVisibility(View.GONE);
                } else if (textLengthInput >= TEXT_NUM_WHEN_TOAST && textLengthInput < TEXT_MAX_NUM) {
                    mTextCountLimitToast.setVisibility(View.VISIBLE);
                    mTextCountLimitToast.setText(getString(R.string.text_count_can_input, TEXT_MAX_NUM - textLengthInput));
                    mTextCountLimitToast.setBackgroundColor(FormatTools.getColor(R.color.count_limit_toast_color_1));
                } else {
                    mTextCountLimitToast.setVisibility(View.VISIBLE);
                    mTextCountLimitToast.setText(getString(R.string.text_count_can_input, 0));
                    mTextCountLimitToast.setBackgroundColor(FormatTools.getColor(R.color.count_limit_toast_color_2));
                }
            }
        });
        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int textLengthInput = s.length();
                if (textLengthInput > 0) {
                    mEmail.setBackgroundResource(R.drawable.browser_shape_feedback_input_bg);
                }
            }
        });
        mTextCountLimitToast.setText(getString(R.string.text_count_can_input, TEXT_MAX_NUM));
        mLoading = new LoadingDialog(getActivity());
        mLoading.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mPreferencesPage.setRightIconEnable(true);
            }
        });
    }

    public void sendFeedBack(View item) {
        long time = (long) SharedPreferencesUtils.get(SharedPreferencesUtils.BROWSER_FEEDBACK_TIME, 0l);
        if (System.currentTimeMillis() - time < 60 * 1000) {
            ToastUtil.showCenterofScreen(item.getContext().getResources().getString(R.string.byfrost_suggestion_time));
            return;
        }
        String feedback = mFeedbackContent.getText().toString().trim();

        if (feedback.length() < 6) {
            mFeedbackContent.setBackgroundResource(R.drawable.browser_shape_feedback_input_error_bg);
            ToastUtil.showLongToast(getActivity(), R.string.please_input_over_six);
            return;
        }

        String mEmailContent = mEmail.getText().toString().trim();

        if (TextUtils.isEmpty(mEmailContent)) {
            mEmail.setBackgroundResource(R.drawable.browser_shape_feedback_input_error_bg);
            ToastUtil.showLongToast(getActivity(), R.string.please_leave_your_email);
            return;
        }

        mFeedbackContent.setBackgroundResource(R.drawable.browser_shape_feedback_input_bg);

        String email = mEmail.getText().toString();
//        if (!TextUtils.isEmpty(email) && !StringUtil.isPhoneNumber(email)) {
//            mEmail.setBackgroundResource(R.drawable.shape_feedback_input_error_bg);
//            ToastUtil.showLongToast(getActivity(), R.string.email_is_error);
//            return;
//        }
        mLoading.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                item.setEnabled(true);
            }
        });
        mLoading.show();
        item.setEnabled(false);
        InputMethodUtils.hideKeyboard(getActivity());
        try {
//            if(BrowserApplication.getInstance().isChina()) {
//                postFeedBack(email, feedback);
//            }else {
            sendFeedbackViaGmail(mEmail.getContext(), email,feedback);
//            }
        } catch (Exception e) {
            //to do nothing.
            ToastUtil.showCenterofScreen(mFeedbackContent.getContext().getResources().getString(R.string.byfrost_suggestion));
        }
    }

    public void sendFeedbackViaGmail(Context context,String email, String body) {
        Bundle bundle = new Bundle();
        bundle.putString("user_email", email);

        int maxLength = 70;  // 每个参数的最大长度
        int totalLength = body.length();
        int paramCount = 15;
        for (int i = 0; i < paramCount; i++) {
            int start = i * maxLength;
            int end = Math.min(start + maxLength, totalLength);  // 确保不超过总长度
            if (start < totalLength) {  // 仅在字符串长度未超过的情况下添加参数
                String part = body.substring(start, end);
                bundle.putString("user_feedback" + (i + 1), part);
            }
        }
        FirebaseAnalytics.getInstance(getActivity()).logEvent("user_feedback_event", bundle);
        mSubmitFeedBack.setEnabled(true);
        mEmail.setText("");
        mFeedbackContent.setText("");
            if(mFeedbackContent != null) {
                mFeedbackContent.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferencesUtils.put(SharedPreferencesUtils.BROWSER_FEEDBACK_TIME, System.currentTimeMillis());
                        mLoading.hide();
                        ToastUtil.showCenterofScreen(mFeedbackContent.getContext().getResources().getString(R.string.byfrost_suggestion));
                    }
                },1000);
          }
    }

    public static boolean startThirdPartyActivity(Context context, Intent intent) {
        boolean result = false;
        if (intent == null) {
            return result;
        }
        if (intent.resolveActivity(context.getPackageManager()) != null) {//存第三方Activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            result = true;
        }
        return result;
    }

    private void postFeedBack(String numble, String feedbackContent) throws Exception {
        JSONObject jsonObjectTemp = new JSONObject();
        jsonObjectTemp.put("feedbackcontent", feedbackContent);
        jsonObjectTemp.put("userphone", numble);
        jsonObjectTemp.put("userinfo", DeviceInfoUtils.getUserInfo(getActivity()));
        Log.e("fdafafda", "josn:" + jsonObjectTemp.toString());
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObjectTemp.toString());
        OkHttpClient mOkHttpClient = new OkHttpClient();
        String url = "https://api2.bmob.cn/1/classes/feedback";
        Request.Builder requestBuilder = new Request.Builder().url(url)
                .addHeader("X-Bmob-Application-Id", NetConfig.BMOB_APPLICATION_ID)
                .addHeader("X-Bmob-REST-API-Key", NetConfig.BMOB_REST_KEY)
                .addHeader("Content-Type", "application/json");
        requestBuilder.method("POST", body);
        Request request = requestBuilder.build();
        Call mcall = mOkHttpClient.newCall(request);
        mcall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mSubmitFeedBack.setEnabled(true);
//                         ToastUtil.showCenterofScreen("反馈失败，请检查网络！");
                        if (mFeedbackContent != null) {
                            ToastUtil.showCenterofScreen(mFeedbackContent.getContext().getResources().getString(R.string.byfrost_suggestion));
                        }
                        mLoading.hide();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mSubmitFeedBack.setEnabled(true);
                        if (mFeedbackContent != null) {
                            ToastUtil.showCenterofScreen(mFeedbackContent.getContext().getResources().getString(R.string.byfrost_suggestion));
                        }
                        SharedPreferencesUtils.put(SharedPreferencesUtils.BROWSER_FEEDBACK_TIME, System.currentTimeMillis());
                        mLoading.hide();
                    }
                });

            }
        });
    }


    @Override
    public void onRightIconClick(View v) {
        sendFeedBack(v);
    }

    @Override
    public void onSecondRightIconClick(View v) {
        //do nothing
    }


    private static class InnerHandler extends Handler {

        private WeakReference<BrowserFeedbackFragment> mFeedBackFragmentHolder;

        public InnerHandler(BrowserFeedbackFragment browserFeedbackFragment) {
            this.mFeedBackFragmentHolder = new WeakReference<>(browserFeedbackFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            BrowserFeedbackFragment browserFeedbackFragment = mFeedBackFragmentHolder.get();
            if (null == browserFeedbackFragment.getActivity() || browserFeedbackFragment.getActivity().isFinishing()) {
                return;
            }

            switch (msg.what) {
                case UPDATE_SUCCESS: {
                    browserFeedbackFragment.mLoading.dismiss();
                    BrowserSettingActivity activity = (BrowserSettingActivity) browserFeedbackFragment.getActivity();
                    activity.setRightIconEnable(true);
                    browserFeedbackFragment.mFeedbackContent.setText(null);
                    SharedPreferencesUtils.put(Constants.FEEDBACK_CONTENT, "");
                    ToastUtil.showShortToast(browserFeedbackFragment.getActivity(), R.string.feed_back_sucess);
                    activity.back();
                }
                break;
                case UPDATE_FAIL: {
                    browserFeedbackFragment.mLoading.dismiss();
                    String feedback = browserFeedbackFragment.mFeedbackContent.getText().toString();
                    SharedPreferencesUtils.put(Constants.FEEDBACK_CONTENT, feedback);
                    if (!NetworkUtils.isNetworkAvailable()) {
                        ToastUtil.showShortToast(browserFeedbackFragment.getActivity(), R.string.check_network);
                    } else {
                        ToastUtil.showShortToast(browserFeedbackFragment.getActivity(), R.string.commit_fail);
                    }
                }
                break;
            }
        }
    }

    private Handler mHandler = new InnerHandler(this);


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.feedback_content:
                mFeedbackContent.setBackgroundResource(R.drawable.browser_shape_feedback_input_bg);
                break;
            case R.id.email:
                mEmail.setBackgroundResource(R.drawable.browser_shape_feedback_input_bg);
                break;
            case R.id.contact_us_on_facebook:
//                ActivityUtils.openUrl(getActivity(), getString(R.string.contact_facebook));
                break;
            case R.id.feedback_submit_bt:
                sendFeedBack(v);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLoading != null) {
            mLoading.hide();
        }
    }

    @Override
    public void onDestroy() {
        mHandler.removeMessages(UPDATE_SUCCESS);
        mHandler.removeMessages(UPDATE_FAIL);
        InputMethodUtils.hideKeyboard(getActivity());
        super.onDestroy();
    }

    @Override
    public void onChooseClick() {
        if (mImageList != null && mImageList.size() <= IMAGE_MAX_NUM) {
            selectPicFromFile();
        }
    }

    @Override
    public void onCloseClick(ImageAdapter.ImageItem v) {
        if (mImageList != null && mImageList.size() <= IMAGE_MAX_NUM + 1) {
            mImageList.remove(v);
            mAdapter.setList(mImageList);
            if (mImageList.size() == 1) {
                mCountImageText.setVisibility(View.GONE);
            } else {
                mCountImageText.setText(getString(R.string.image_count, mImageList.size() - 1, IMAGE_MAX_NUM));
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.getParent().requestDisallowInterceptTouchEvent(true);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                v.getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return false;
    }

    private void selectPicFromFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.upload_image)),
                PICK_FROM_FILE);
    }

}
