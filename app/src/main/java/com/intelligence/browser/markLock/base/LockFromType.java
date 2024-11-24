package com.intelligence.browser.markLock.base;


/**
 * 用来区分锁界面的操作是从哪个流程发起的
 */
public class LockFromType {

    /** 书签流程 */
    public static final int FROM_BOOK_MARK=0;

    /** 下载流程 */
    public static final int FROM_DOWNLOAD=1;

    /** 书签已经解锁进入后的操作流程 */
    public static final int FROM_BOOK_MARK_CHANGE=2;


}
