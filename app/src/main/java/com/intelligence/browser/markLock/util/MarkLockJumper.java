package com.intelligence.browser.markLock.util;


import android.content.Context;

import com.intelligence.browser.markLock.CheckSecurityBirthActivity;
import com.intelligence.browser.markLock.MarkLockPassActivity;
import com.intelligence.browser.markLock.MarkLockSetActivity;
import com.intelligence.browser.markLock.base.LockFromType;

public class MarkLockJumper {

    private final LockSetListener listener;
    private final Context context;



    public MarkLockJumper(Context context, final LockSetListener l){
        this.context = context;
        listener = l;
    }



    /** 如果已经设置了密码, 跳转到密码验证页面
           如果未设置密码, 跳转到新设置密码页面 */
    public void jumpToSetOrPass(int from){
        if(LockPasswordManager.getInstance().isLockEnable()){
            MarkLockPassActivity.launch(context, from);
            LockSetListener.registerOnce(listener, LockSetListener.TYPE_CHECK);
        }else{
            MarkLockSetActivity.launch(context, false, from);
            LockSetListener.registerOnce(listener, LockSetListener.TYPE_SET_RESET);
        }
    }



    /** 先跳转到密保验证页面, 通过后跳转到重置密码页面  */
    public void jumpToResetPWD(int from){
        CheckSecurityBirthActivity.launch(context, from);
        LockSetListener.registerOnce(listener, LockSetListener.TYPE_SET_RESET);
    }


    /** 直接跳转到修改密码界面, 不通过密保验证 */
    public void jumpToResetPWDWithSecurity(){
        MarkLockSetActivity.launch(context, true, LockFromType.FROM_BOOK_MARK_CHANGE);
        LockSetListener.registerOnce(listener, LockSetListener.TYPE_SET_RESET);
    }

}
