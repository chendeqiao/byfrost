package com.intelligence.browser.markLock;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.browser.markLock.base.LockBaseActivity;
import com.intelligence.browser.markLock.base.LockFromType;
import com.intelligence.browser.markLock.lock.LockPatternView;
import com.intelligence.browser.markLock.lock.NumberKeyboard;
import com.intelligence.browser.markLock.lock.NumberPasswordProcessor;
import com.intelligence.browser.markLock.lock.OnNumberChangedListener;
import com.intelligence.browser.markLock.util.LockPasswordManager;
import com.intelligence.browser.markLock.util.LockSetListener;
import com.intelligence.browser.markLock.util.MarkLockJumper;

import java.util.List;

public class MarkLockPassActivity extends LockBaseActivity {

    public static void launch(Context context, int from){
        Intent intent = new Intent(context, MarkLockPassActivity.class);
        intent.putExtra("from", from);
        try{
            context.startActivity(intent);
        }catch (Exception e){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }



    /* ===================================================================================== */




    private LockPatternView lockPatternView;
    private NumberKeyboard numberKeyboard;
    private NumberPasswordProcessor numberPasswordProcessor;
    private View numberLayout;

    private TextView topTV;
    private int from;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        from = getIntent().getIntExtra("from", LockFromType.FROM_BOOK_MARK);
        Boolean typeNumber = LockPasswordManager.getInstance().isTypeNumber();
        if(!LockPasswordManager.getInstance().isLockEnable() || typeNumber==null){
            finish();
            return ;
        }
        setContentView(R.layout.browser_applock_mark_lock_pass);
        lockPatternView = (LockPatternView) findViewById(R.id.lock_pattern);
        numberKeyboard = (NumberKeyboard) findViewById(R.id.number_keyboard);
        numberPasswordProcessor = (NumberPasswordProcessor) findViewById(R.id.number_processor);
        numberLayout = findViewById(R.id.layout_number_keyboard);
        topTV = (TextView) findViewById(R.id.tv_top2);

        if(typeNumber){
            lockPatternView.setVisibility(View.GONE);
            numberLayout.setVisibility(View.VISIBLE);
        }else{
            lockPatternView.setVisibility(View.VISIBLE);
            numberLayout.setVisibility(View.GONE);
        }

        lockPatternView.setOnPatternListener(new LockPatternView.OnPatternListener() {
            public void onPatternStart() {
            }
            public void onPatternCleared() {
            }
            public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
            }
            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                String pwd = LockPatternView.patternToString(pattern);
                boolean check = LockPasswordManager.getInstance().check(pwd);
                if(check){
                    lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
                    lockPatternView.postDelayed(new Runnable() {
                        public void run() {
                            onPwdPassed();
                        }
                    }, LockPatternView.SUCCESS_RESET_TIME);
                }else{
                    lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                    topTV.setText(R.string.lock_pwd_error);
                    lockPatternView.postDelayed(new Runnable() {
                        public void run() {
                            lockPatternView.clearPattern();
                            topTV.setText(R.string.lock_enter_password);
                        }
                    }, LockPatternView.ERROR_RESET_TIME);
                }
            }
        });

        numberKeyboard.setPasswordProcessor(numberPasswordProcessor);
        numberKeyboard.setOnNumberChangedListener(new OnNumberChangedListener() {
            public void onNumberChanged(String numbers, boolean fullType) {
                if(!fullType)
                    return ;
                boolean check = LockPasswordManager.getInstance().check(numbers);
                if(check){
                    onPwdPassed();
                }else{
                    numberKeyboard.clearPassword();
                    numberPasswordProcessor.clearPassword();
                    numberPasswordProcessor.startFailedAnimation();
                    topTV.setText(R.string.lock_pwd_error);
                    numberPasswordProcessor.postDelayed(new Runnable() {
                        public void run() {
                            topTV.setText(R.string.lock_enter_password);
                        }
                    }, LockPatternView.ERROR_RESET_TIME);
                }
            }
        });

        //忘记密码点击事件
        findViewById(R.id.tv_bottom).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mkj==null){
                    mkj = new MarkLockJumper(MarkLockPassActivity.this, new LockSetListener() {
                        public void onSuccess(boolean reset) {
                            passed = true;
                            finish();
                        }
                    });
                }
                mkj.jumpToResetPWD(from);
            }});

    }

    private MarkLockJumper mkj;



    private boolean passed = false;

    private void onPwdPassed(){
        String action = from == LockFromType.FROM_BOOK_MARK ? "101" : "102";

        passed = true;
        finish();
    }


    @Override
    public void finish() {
        super.finish();
        LockSetListener.callAndClear(new LockSetListener.IteratorCallback<LockSetListener>() {
            public void call(LockSetListener lockSetListener) {
                lockSetListener.onCheck(passed);
            }
        }, LockSetListener.TYPE_CHECK);
    }


    @Override
    protected boolean onBackClick() {
        return super.onBackClick();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    private long pageStartTime;

    @Override
    protected void onStart() {
        super.onStart();
        pageStartTime = System.currentTimeMillis();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }




    /* ==================================================================================== */




    private String getPageEventContent1(){
        return from==LockFromType.FROM_BOOK_MARK ? "0" : "1";
    }
    public static final String PAGE_ID = "e016";




}
