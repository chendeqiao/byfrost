package com.intelligence.browser.markLock.lock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.intelligence.browser.R;
import com.intelligence.browser.markLock.util.LockPasswordManager;

public class NumberPasswordProcessor extends FrameLayout implements IPasswordProcessor {


    private OnPswHandledListener mPswHandledListener;

    public NumberPasswordProcessor(Context context) {
        super(context);
        initView(context);
    }

    public NumberPasswordProcessor(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NumberPasswordProcessor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    private void initView(Context context) {
        View.inflate(context, R.layout.browser_number_password_processor_layout, this);
        activateChildren(0);
    }

    private void activateChildren(int activeCount) {
        LinearLayout circleGroup = (LinearLayout) getChildAt(0);
        int childCount = circleGroup.getChildCount();
        activeCount = activeCount <= childCount ? activeCount : childCount;
        for (int count = 0; count < activeCount; count++) {
            circleGroup.getChildAt(count).setEnabled(true);
        }
        for (int count = activeCount; count < childCount; count++) {
            circleGroup.getChildAt(count).setEnabled(false);
        }
    }

    @Override
    public void handlePassword(@NonNull String pwd, int pwdLength, OnPswHandledListener listener) {
        activateChildren(pwd.length());
//        if (pwd.length() == pwdLength) {
//            checkPassword(pwd, listener);
//        }
    }

    private void checkPassword(String password, final OnPswHandledListener listener) {
        boolean pass = LockPasswordManager.getInstance().check(password);
        if (!pass) {
            startFailedAnimation(new onFailedAnimationEndListener() {
                public void onAnimationEnd(Animation animation) {
                    if (listener != null) {
                        listener.checked(true);
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.checked(true);
            }
        }
        if (mPswHandledListener != null) {
            mPswHandledListener.checked(pass);
        }
    }

    public OnPswHandledListener getOnPswHandledListener() {
        return mPswHandledListener;
    }

    public void setOnPswHandledListener(OnPswHandledListener onPswHandledListener) {
        this.mPswHandledListener = onPswHandledListener;
    }

    public void clearPassword() {
        activateChildren(0);
    }

    public void startFailedAnimation() {
        startFailedAnimation(null);
    }

    private void startFailedAnimation(final onFailedAnimationEndListener listener) {
        Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.browser_shake);
        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                clearPassword();
                if (listener != null) {
                    listener.onAnimationEnd(animation);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        startAnimation(shake);
    }

    interface onFailedAnimationEndListener {
        void onAnimationEnd(Animation animation);
    }
}
