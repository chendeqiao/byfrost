package com.intelligence.browser.markLock.util;


import java.util.WeakHashMap;

public abstract class LockSetListener {

    /**
     * 成功的修改了密码
     * @param reset 是否是重新修改
     */
    public void onSuccess(boolean reset){};
    /**
     * 中断了设置
     * @param reset 是否是重新修改
     */
    public void onInterrupt(boolean reset){};
    /**
     * 通过了验证
     * @param pass 是否通过了, 如果是false,表示没通过就退出了
     */
    public void onCheck(boolean pass){}



    public static final String TYPE_SET_RESET = "1";
    public static final String TYPE_CHECK = "2";



    private static final WeakHashMap<LockSetListener, LockSetListener> setListeners = new WeakHashMap<>(3);
    private static final WeakHashMap<LockSetListener, LockSetListener> checkListeners = new WeakHashMap<>(3);




    static void registerOnce(LockSetListener listener, String type){
        if(TYPE_SET_RESET.equals(type)){
            if(!setListeners.containsKey(listener))
                setListeners.put(listener, listener);
        }else if(TYPE_CHECK.equals(type)){
            if(!checkListeners.containsKey(listener))
                checkListeners.put(listener, listener);
        }
    }



    public interface IteratorCallback<T>{
        void call(T t);
    }


    public static void callAndClear(IteratorCallback<LockSetListener> callback, String type){
        if(TYPE_SET_RESET.equals(type)){

                            if(setListeners.isEmpty())
                                return ;
                            for (LockSetListener item : setListeners.keySet()) {
                                if(item != null)
                                    callback.call(item);
                            }
                            setListeners.clear();

        }else if(TYPE_CHECK.equals(type)){

                            if(checkListeners.isEmpty())
                                return ;
                            for (LockSetListener item : checkListeners.keySet()) {
                                if(item != null)
                                    callback.call(item);
                            }
                            checkListeners.clear();

        }

    }



}
