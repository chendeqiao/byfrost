package com.intelligence.browser.markLock.util;


import android.content.Context;
import android.content.SharedPreferences;

public class LockPasswordManager {

    //密码类型: 数字密码
    public static final int TYPE_NUMBER = 1;
    //密码类型: 九宫格划线密码
    public static final int TYPE_LINE = 2;


    public static final LockPasswordManager instance = new LockPasswordManager();
    public static final Context context = BrowserApplication.getInstance();
    public static LockPasswordManager getInstance() {
        return instance;
    }



    private final String KEY_FILE_NAME="mark_lock_config";
    private final String KEY_VALUE="value1";
    private final String GAP="______";


    /* 加密一下密码 */
    private String encryptPwd(String pwd){
        if(pwd==null || pwd.length()<1)
            return "";
        String md5 = MD5Util.getStringMD5(pwd);
        return md5==null ? "" : md5;
    }



    /**
     * 保存密码
     * @param pwd null或空字符表示清空 并且参数type无效
     * @param birth 密保生日
     * @param type 类型 {@link LockPasswordManager#TYPE_LINE} {@link LockPasswordManager#TYPE_NUMBER}
     */
    public void savePwd(String pwd, String birth, int type){
        if(type != TYPE_NUMBER && type != TYPE_LINE)
            throw new IllegalStateException("param type must be one of TYPE_NUMBER or TYPE_LINE");

        SharedPreferences sp = context.getSharedPreferences(KEY_FILE_NAME, Context.MODE_PRIVATE);
        if(pwd==null || pwd.isEmpty()){
            sp.edit().putString(KEY_VALUE, "").apply();
        }else
            sp.edit().putString(KEY_VALUE, encryptPwd(pwd)+GAP+type).apply();

        if(birth != null && birth.trim().length()>0)
            setSecurityBirth(birth);
    }


    /**
     * 验证密码是否正确
     */
    public boolean check(String pwd){
        if(pwd==null||pwd.isEmpty())
            return false;
        SharedPreferences sp = context.getSharedPreferences(KEY_FILE_NAME, Context.MODE_PRIVATE);
        String pwdInfo = sp.getString(KEY_VALUE, null);
        if(pwdInfo == null)
            return false;
        try{
            return pwdInfo.split(GAP)[0].equals(encryptPwd(pwd));
        }catch (Exception e){
            return false;
        }
    }


    /**
     * 获取当前密码类型
     * true: 是数字锁
     * false: 是九宫格锁
     * null: 没有设置密码锁
     */
    public Boolean isTypeNumber(){
        SharedPreferences sp = context.getSharedPreferences(KEY_FILE_NAME, Context.MODE_PRIVATE);
        try{
            String pwdInfo = sp.getString(KEY_VALUE, null);
            if(pwdInfo==null)
                return null;
            return pwdInfo.split(GAP)[1].equals(TYPE_NUMBER+"");
        }catch (Exception e){
            return null;
        }
    }


    /**
     * 书签锁是否启用
     */
    public boolean isLockEnable(){
        if(!isSecurityBirthEnable())
            return false;
        SharedPreferences sp = context.getSharedPreferences(KEY_FILE_NAME, Context.MODE_PRIVATE);
        String pwdInfo = sp.getString(KEY_VALUE, null);
        return pwdInfo != null && pwdInfo.contains(GAP) && pwdInfo.split(GAP).length==2;
    }




    /* ================================================================================================ */




    private final String KEY_VALUE_BIRTH="value_birth";

    private String encryptBirth(String birth){
        if(birth==null || birth.trim().length()<1)
            return "";
        String md5 = MD5Util.getStringMD5(birth);
        return md5==null ? "" : md5;
    }


    public boolean isSecurityBirthEnable(){
        String securityBirthCipher = getSecurityBirthCipher();
        return securityBirthCipher != null && securityBirthCipher.trim().length()>0;
    }
    public boolean checkBirth(String birth){
        return encryptBirth(birth).equals(getSecurityBirthCipher());
    }
    public String getSecurityBirthCipher(){
        SharedPreferences sp = context.getSharedPreferences(KEY_FILE_NAME, Context.MODE_PRIVATE);
        return sp.getString(KEY_VALUE_BIRTH, null);
    }
    public void setSecurityBirth(String birth){
        SharedPreferences sp = context.getSharedPreferences(KEY_FILE_NAME, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_VALUE_BIRTH, encryptBirth(birth)).apply();
    }


}
