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
import com.intelligence.browser.markLock.lock.BirthdayPicker;
import com.intelligence.browser.markLock.util.LockPasswordManager;
import com.intelligence.browser.utils.ToastUtil;

public class CheckSecurityBirthActivity extends LockBaseActivity {


    public static void launch(Context context, int from){
        Intent intent = new Intent(context, CheckSecurityBirthActivity.class);
        intent.putExtra("from", from);
        try{
            context.startActivity(intent);
        }catch (Exception e){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }



    /* =========================================================================================== */



    private BirthdayPicker birthdayPicker;
    private int from;


    private boolean isResetSecurity(){
        return from == LockFromType.FROM_BOOK_MARK_CHANGE;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        from = getIntent().getIntExtra("from", LockFromType.FROM_BOOK_MARK);
        setContentView(R.layout.browser_applock_security_birth);

        birthdayPicker = (BirthdayPicker) findViewById(R.id.birthdayPicker);

        if(isResetSecurity()){
            ((TextView)findViewById(R.id.tv_top2)).setText(R.string.lock_set_protected_question_desc);
        }

        findViewById(R.id.btn_bottom).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isResetSecurity()){
                    doReSetBirthSecurity();
                }else{
                    doReSetPWD();
                }
            }
        });
    }


    private void doReSetPWD(){
        String b = birthdayPicker.getSelectedString();
        boolean ok = LockPasswordManager.getInstance().checkBirth(b);
        if(ok){
            MarkLockSetActivity.launch(birthdayPicker.getContext(), true, from);
            finish();
        }else{
            ToastUtil.show(R.string.lock_answer_error);
        }
    }


    private void doReSetBirthSecurity(){
        String pwd = birthdayPicker.getSelectedString();
        LockPasswordManager.getInstance().setSecurityBirth(pwd);
        ToastUtil.showLongToast(CheckSecurityBirthActivity.this,R.string.lock_change_security_birth_success);
        if(isResetSecurity())
        finish();
    }


    @Override
    protected boolean onBackClick() {
        return super.onBackClick();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
