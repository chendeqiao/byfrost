package com.intelligence.commonlib.swiperecyclerview;

public interface Openable {
    boolean isMenuOpen();

    boolean isLeftMenuOpen();

    boolean isLeftMenuOpenNotEqual();

    boolean isMenuOpenNotEqual();

    boolean isRightMenuOpen();

    boolean isRightMenuOpenNotEqual();

    void smoothOpenMenu();

    void smoothOpenLeftMenu();

    void smoothOpenLeftMenu(int var1);

    void smoothOpenRightMenu();

    void smoothOpenRightMenu(int var1);
}
