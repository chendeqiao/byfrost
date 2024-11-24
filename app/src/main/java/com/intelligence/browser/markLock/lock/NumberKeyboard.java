package com.intelligence.browser.markLock.lock;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.intelligence.browser.R;

import java.util.Arrays;
import java.util.Random;

public class NumberKeyboard extends LinearLayout implements View.OnClickListener, OnPswHandledListener {

    private StringBuilder mCurrentString;
    private IPasswordProcessor mPasswordProcessor;
    private OnNumberChangedListener mNumberChangedListener;
    private int mPasswordLength;
    private int[] mNumberIds = {R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9};
    private int[] defaultThemeDrawables = {R.drawable.browser_code_0, R.drawable.browser_code_1, R.drawable.browser_code_2, R.drawable.browser_code_3, R.drawable.browser_code_4,
            R.drawable.browser_code_5, R.drawable.browser_code_6, R.drawable.browser_code_7, R.drawable.browser_code_8, R.drawable.browser_code_9, R.drawable.browser_backspace_white_36dp};
    private final Integer[] mDefaultNumberSequence = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
    private Integer[] mNumberSequence = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
    private boolean mSoundEffectsEnabled;
    private PinLockTheme mTheme;
    private ImageView[] mBtns;
    private boolean mDisorganizeMode = false;

    public NumberKeyboard(Context context) {
        super(context);
        init(context);
    }

    public NumberKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NumberKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        initView(context);
        mCurrentString = new StringBuilder();
        mPasswordLength = 4;
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.browser_digit_layout, this);
        ImageView mBtn1 = (ImageView) findViewById(R.id.button1);
        ImageView mBtn2 = (ImageView) findViewById(R.id.button2);
        ImageView mBtn3 = (ImageView) findViewById(R.id.button3);
        ImageView mBtn4 = (ImageView) findViewById(R.id.button4);
        ImageView mBtn5 = (ImageView) findViewById(R.id.button5);
        ImageView mBtn6 = (ImageView) findViewById(R.id.button6);
        ImageView mBtn7 = (ImageView) findViewById(R.id.button7);
        ImageView mBtn8 = (ImageView) findViewById(R.id.button8);
        ImageView mBtn9 = (ImageView) findViewById(R.id.button9);
        ImageView mBtn0 = (ImageView) findViewById(R.id.button0);
        ImageView mBtnErase = (ImageView) findViewById(R.id.button_erase);

        mBtn0.setOnClickListener(this);
        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        mBtn3.setOnClickListener(this);
        mBtn4.setOnClickListener(this);
        mBtn5.setOnClickListener(this);
        mBtn6.setOnClickListener(this);
        mBtn7.setOnClickListener(this);
        mBtn8.setOnClickListener(this);
        mBtn9.setOnClickListener(this);
        mBtnErase.setOnClickListener(this);

        mBtns = new ImageView[]{mBtn1, mBtn2, mBtn3, mBtn4, mBtn5, mBtn6, mBtn7, mBtn8, mBtn9, mBtn0, mBtnErase};
        setSoundEffectsEnabled(mSoundEffectsEnabled);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_erase) {
            deleteLastNumber();
        } else if (isNumberBtnId(v.getId())) {
            appendNumber(v.getContentDescription().toString());
        }


        //if (ConfigSetting.getIns(getContext()).isVibrateEnable()) {
        if (true) {
            performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                            | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        }

    }

    private boolean isNumberBtnId(int id) {//test
        for (int index = 0; index < mNumberIds.length; index++) {
            if (id == mNumberIds[index]) {
                return true;
            }
        }
        return false;
    }

    private void appendNumber(String number) {
        if (mCurrentString.length() < mPasswordLength) {
            mCurrentString.append(number);
            notifyNumberChanged();
        }
    }

    private void deleteLastNumber() {
        if (!TextUtils.isEmpty(mCurrentString)) {
            mCurrentString = mCurrentString.deleteCharAt(mCurrentString.length() - 1);
            notifyNumberChanged();
        }
    }

    private void notifyNumberChanged() {
        if (mNumberChangedListener != null) {
            mNumberChangedListener.onNumberChanged(mCurrentString.toString(), mCurrentString.length() == mPasswordLength);
        }
        if (mPasswordProcessor != null) {
            mPasswordProcessor.handlePassword(mCurrentString.toString(), mPasswordLength, this);
        }
    }


    public IPasswordProcessor getPasswordProcessor() {
        return mPasswordProcessor;
    }


    public void setPasswordProcessor(IPasswordProcessor passwordProcessor) {
        this.mPasswordProcessor = passwordProcessor;
    }

    public OnNumberChangedListener getOnNumberChangedListener() {
        return mNumberChangedListener;
    }

    public void setOnNumberChangedListener(OnNumberChangedListener onNumberChangedListener) {
        this.mNumberChangedListener = onNumberChangedListener;
    }

    @Override
    public void checked(boolean match) {
        clearPassword();
    }


    public boolean isSoundEffectsEnabled() {
        return mSoundEffectsEnabled;
    }

    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        this.mSoundEffectsEnabled = soundEffectsEnabled;

        for (ImageView view : mBtns) {
            view.setSoundEffectsEnabled(soundEffectsEnabled);
        }
    }



    public void disorganizeNumber(boolean disorganizeMode) {
        if (disorganizeMode ^ mDisorganizeMode) {
            mDisorganizeMode = disorganizeMode;
            if (disorganizeMode) {
                mNumberSequence = randomSort(mNumberSequence);
            } else {
                mNumberSequence = Arrays.copyOf(mDefaultNumberSequence, mDefaultNumberSequence.length);
            }
            initBtnSrc();
        }
    }


    public boolean getDisorganizeMode() {
        return mDisorganizeMode;
    }

    public void clearPassword() {
        mCurrentString = new StringBuilder();
    }


    public void setTheme(PinLockTheme theme) {
        this.mTheme = theme;
        initBtnSrc();
    }

    public PinLockTheme getTheme() {
        return mTheme;
    }

    private void initBtnSrc() {
        int[] numberBgDrawableIds;
        int[] selectedDrawableIds;

        if (mTheme != null) {
            numberBgDrawableIds = mTheme.getNumberBgDrawableIds();
            selectedDrawableIds = mTheme.getSelectedNumberBgDrawableIds();
        } else {
            selectedDrawableIds = numberBgDrawableIds = defaultThemeDrawables;
        }

        initNumberSrc(mNumberSequence, numberBgDrawableIds, selectedDrawableIds);
        // 删除按钮
        ImageView eraseView = mBtns[mBtns.length - 1];
        eraseView.setImageDrawable(createSelector(getResources().getDrawable(numberBgDrawableIds[numberBgDrawableIds.length - 1])
                , getResources().getDrawable(selectedDrawableIds[selectedDrawableIds.length - 1])));
        changeViewStyle(eraseView, mTheme);
    }

    private void initNumberSrc(Integer[] sequence, int[] numberBgDrawableIds, int[] selectedDrawableIds) {
        int length = Math.min(Math.min(numberBgDrawableIds.length, selectedDrawableIds.length), Math.min(sequence.length, mBtns.length));//防止角标越界
        for (int i = 0; i < length; i++) {
            int number = sequence[i];
            mBtns[i].setImageDrawable(createSelector(getResources().getDrawable(numberBgDrawableIds[number]), getResources().getDrawable(selectedDrawableIds[number])));
            mBtns[i].setContentDescription(String.valueOf(number));
            changeViewStyle(mBtns[i], mTheme);
        }
    }

    private void changeViewStyle(ImageView view, PinLockTheme theme) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.height = layoutParams.width = theme == null ? getResources().getDimensionPixelOffset(R.dimen.digit_number_layout_width) : FrameLayout.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }

    private StateListDrawable createSelector(Drawable idNormal, Drawable idPressed) {
        StateListDrawable drawable = new StateListDrawable();
        Drawable normal = idNormal;
        Drawable pressed = idPressed;
        drawable.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled}, normal);
        drawable.addState(new int[]{}, normal);
        return drawable;
    }


    public static <T> T[] randomSort(T[] t) {
        T[] result = Arrays.copyOf(t, t.length);
        Random random = new Random();
        for (int i = 0; i < t.length; i++) {
            int r = random.nextInt(t.length - i);
            result[i] = t[r];
            t[r] = t[t.length - 1 - i];
        }
        return result;
    }

}
