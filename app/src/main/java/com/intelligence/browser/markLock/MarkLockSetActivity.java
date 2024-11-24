package com.intelligence.browser.markLock;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.intelligence.browser.R;
import com.intelligence.browser.markLock.base.LockBaseActivity;
import com.intelligence.browser.markLock.base.LockFromType;
import com.intelligence.browser.markLock.lock.BirthdayPicker;
import com.intelligence.browser.markLock.lock.LockPatternView;
import com.intelligence.browser.markLock.lock.NumberKeyboard;
import com.intelligence.browser.markLock.lock.NumberPasswordProcessor;
import com.intelligence.browser.markLock.lock.OnNumberChangedListener;
import com.intelligence.browser.markLock.util.LockPasswordManager;
import com.intelligence.browser.markLock.util.LockSetListener;
import com.intelligence.browser.markLock.util.ViewUtil;

import java.util.ArrayList;
import java.util.List;

public class MarkLockSetActivity extends LockBaseActivity {

    public static void launch(Context context, boolean reset, int from){
        Intent intent = new Intent(context, MarkLockSetActivity.class);
        intent.putExtra("reset", reset);
        intent.putExtra("from", from);
        try{
            context.startActivity(intent);
        }catch (Exception e){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }



    /* ============================================================= */



    private ImageView topLeftIV;
    private TextView topLeftTV;

    private TextView topTV1;
    private TextView topTV2;

    private LockPatternView lockPatternView;
    private View numberKeyboardLayout;
    private NumberPasswordProcessor numberPasswordProcessor;
    private NumberKeyboard numberKeyboardView;
    private BirthdayPicker birthdayPicker;

    private TextView bottomTV;
    private View bottomButton;

    //记录第一次用户输入的密码
    private String firstPassword;
    //当前的密码类型
    private int nowPwdType;
    //记录是否是重新设置密码的流程
    private boolean reset;
    // 记录是否修改成功
    private boolean success = false;
    //记录来源
    private int from;



    private boolean isProcessingOnCreate = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        isProcessingOnCreate = true;
        super.onCreate(savedInstanceState);
        reset = getIntent().getBooleanExtra("reset", false);
        from = getIntent().getIntExtra("from", LockFromType.FROM_BOOK_MARK);
        setContentView(R.layout.browser_applock_mark_set);
        findView();
        initViewDataAndEvent();
        //如果是用户重置密码, 并且原来的密码类型是数字, 那么进来的时候首先显示数字密码
        Boolean originTypeIsNumber = LockPasswordManager.getInstance().isTypeNumber();
        if(reset && originTypeIsNumber!=null && originTypeIsNumber )
            bottomTV.performClick();

        reportPV();
        //这句要放到最底下
        isProcessingOnCreate = false;
    }

    private void findView(){
        topLeftIV = (ImageView) findViewById(R.id.iv_left_up);
        topLeftTV = (TextView) findViewById(R.id.tv_left_up);
        topTV1 = (TextView) findViewById(R.id.tv_top1);
        topTV2 = (TextView) findViewById(R.id.tv_top2);
        lockPatternView = (LockPatternView) findViewById(R.id.lock_pattern);
        numberKeyboardLayout = findViewById(R.id.layout_number_keyboard);
        numberPasswordProcessor = (NumberPasswordProcessor) findViewById(R.id.number_processor);
        numberKeyboardView = (NumberKeyboard) findViewById(R.id.number_keyboard);
        birthdayPicker = (BirthdayPicker) findViewById(R.id.birthdayPicker);
        bottomTV = (TextView) findViewById(R.id.tv_bottom);
        bottomButton = findViewById(R.id.btn_bottom);
        //----------------
        numberKeyboardLayout.setVisibility(View.GONE);
        birthdayPicker.setVisibility(View.GONE);
        bottomButton.setVisibility(View.GONE);
    }

    private void initViewDataAndEvent(){
        nowPwdType = LockPasswordManager.TYPE_LINE;
        int res_xx = R.drawable.browser_lock_not_now_xx;
        int res_back = R.drawable.browser_setting_back_white;
        if(reset){
            topLeftIV.setImageResource(res_back);
            topLeftTV.setText("");
        }else{
            topLeftIV.setImageResource(res_xx);
            topLeftTV.setText(R.string.lock_not_now);
        }
        View.OnClickListener listener = new View.OnClickListener(){
            public void onClick(View v) {
                reportLeftUp();
                finish();
            }};
        topLeftIV.setOnClickListener(listener);
        topLeftTV.setOnClickListener(listener);
        //----------------
        setTopTVText();
        //----------------
        setLockPatternViewEvent();
        //----------------
        setNumberLockEvent();
        //----------------
        setBottomTvText();
        bottomTV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ViewUtil.toggleVisibleAndGone(lockPatternView, numberKeyboardLayout);
                nowPwdType=(nowPwdType==LockPasswordManager.TYPE_LINE) ? LockPasswordManager.TYPE_NUMBER : LockPasswordManager.TYPE_LINE;
                setBottomTvText();
                firstPassword = null;
                setTopTVText();
                if(!isProcessingOnCreate)
                    reportPWDTypeChange();
            }});
        bottomButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(firstPassword==null)
                    return ;
                reportPage("7");
                complete();
            }});
    }
    private void setTopTVText(){
        boolean lineShow = lockPatternView.getVisibility()==View.VISIBLE;
        boolean birthShow = birthdayPicker.getVisibility()==View.VISIBLE;
        if(firstPassword==null){
            topTV1.setText(R.string.lock_lock_for_to_safe);
            topTV2.setText(lineShow?R.string.lock_set_new_lock_line:R.string.lock_set_new_lock_number);
        }else{
            if(birthShow){
                topTV1.setText(R.string.lock_set_protected_question_desc);
                topTV2.setText(R.string.lock_your_birth);
            }else{
                topTV1.setText(R.string.lock_lock_for_to_safe);
                topTV2.setText(lineShow ? R.string.lock_confirm_lock_line : R.string.lock_confirm_lock_number);
            }
        }
        if(reset) topTV1.setText(R.string.lock_reset_pwd);
    }
    private void setLockPatternViewEvent(){
        lockPatternView.setOnPatternListener(new LockPatternView.OnPatternListener() {
            public void onPatternStart() {}
            public void onPatternCleared() {}
            public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {}
            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                final String pwd = LockPatternView.patternToString(pattern);
                if(pwd.length()<4){
                    lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                    lockPatternView.postDelayed(new Runnable() {public void run() {
                        lockPatternView.clearPattern();
                        if(firstPassword != null){
                            firstPassword=null;
                            setTopTVText();
                        }
                    }}, LockPatternView.ERROR_RESET_TIME);
                }else{
                    if(firstPassword==null){
                        lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
                        lockPatternView.postDelayed(new Runnable() {public void run() {
                            lockPatternView.clearPattern();
                            firstPassword = pwd;
                            setTopTVText();
                            reportPV();
                        }}, LockPatternView.SUCCESS_RESET_TIME);
                    }else{
                        if(firstPassword.equals(pwd)){
                            lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
                            lockPatternView.postDelayed(new Runnable() {public void run() {
                                if(reset){
                                    complete();
                                    reportPV();
                                    return ;
                                }
                                ViewUtil.toggleVisibleAndGone(lockPatternView, birthdayPicker, bottomButton, bottomTV);
                                setTopTVText();
                                reportPV();
                            }}, LockPatternView.SUCCESS_RESET_TIME);
                        }else{
                            lockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                            lockPatternView.postDelayed(new Runnable() {public void run() {
                                lockPatternView.clearPattern();
                                if(firstPassword != null){
                                    firstPassword=null;
                                    setTopTVText();
                                }
                            }}, LockPatternView.ERROR_RESET_TIME);
                        }
                    }
                }
            }
        });
    }
    private void setNumberLockEvent(){
        numberKeyboardView.setPasswordProcessor(numberPasswordProcessor);
        numberKeyboardView.setOnNumberChangedListener(new OnNumberChangedListener() {
            public void onNumberChanged(String pwd, boolean fullType) {
                if(!fullType)
                    return ;
                if(firstPassword==null){
                    firstPassword = pwd;
                    numberPasswordProcessor.clearPassword();
                    numberKeyboardView.clearPassword();
                    setTopTVText();
                    reportPV();
                }else{
                    if(firstPassword.equals(pwd)){
                        if(reset){
                            complete();
                            reportPV();
                            return ;
                        }
                        ViewUtil.toggleVisibleAndGone(numberKeyboardLayout, birthdayPicker, bottomButton, bottomTV);
                        setTopTVText();
                        reportPV();
                    }else{
                        firstPassword = null;
                        setTopTVText();
                        numberPasswordProcessor.startFailedAnimation();
                        numberKeyboardView.clearPassword();
                    }
                }
            }});
    }
    private void setBottomTvText(){
        boolean lineShow = lockPatternView.getVisibility()==View.VISIBLE;
        bottomTV.setText(lineShow ? R.string.lock_turn_to_type_number : R.string.lock_turn_to_type_line);
    }
    private void complete(){
        isCompleteShowing = true;
        String pwd = firstPassword;
        String birth = reset ? null : birthdayPicker.getSelectedString();
        int type = nowPwdType;
        LockPasswordManager.getInstance().savePwd(pwd, birth, type);
        success = true;
        setContentView(R.layout.browser_applock_mark_set_complete);
        ((TextView)findViewById(R.id.tv)).setText(reset?R.string.lock_pwd_has_reset:R.string.lock_lock_success_common);
        getInActivityLifeHandler().postDelayed(new Runnable() {
            public void run() {
                if(!reset);
                finish();
            }}, 1500);
    }


    private boolean isCompleteShowing = false;
    @Override
    public void onBackPressed() {
        if(isCompleteShowing)
            return ;
        super.onBackPressed();
        reportPage("5");
    }


    @Override
    public void finish() {
        super.finish();
        LockSetListener.callAndClear(new LockSetListener.IteratorCallback<LockSetListener>() {
            public void call(LockSetListener lockSetListener) {
                if(success)
                    lockSetListener.onSuccess(reset);
                else
                    lockSetListener.onInterrupt(reset);
            }
        }, LockSetListener.TYPE_SET_RESET);
    }

    @Override
    protected boolean onBackClick() {
        reportLeftUp();
        return super.onBackClick();
    }




    /* ================================================================== */




    private void reportPWDTypeChange(){
        String nowTypeContent;
        if(nowPwdType==LockPasswordManager.TYPE_LINE) nowTypeContent = "3"; else nowTypeContent = "2";
        reportPage(nowTypeContent);
    }
    private void reportLeftUp(){
        if(!reset) reportPage("1");
        else reportPage("4");
    }
    private void reportPage(String content){
    }
    private String getPageID(){
        if(reset){
            if(firstPassword==null)
                return "e022";
            else
                return "e023";
        }else{
            if(firstPassword==null)
                return "e017";
            if(birthdayPicker.getVisibility()==View.VISIBLE)
                return "e019";
            return "e018";
        }
    }

    private ArrayList<String> pvReportedList = new ArrayList<>(3);
    public void reportPV(){
        String pid = getPageID();
        if(pvReportedList.contains(pid))
            return ;
        pvReportedList.add(pid);
    }

}
