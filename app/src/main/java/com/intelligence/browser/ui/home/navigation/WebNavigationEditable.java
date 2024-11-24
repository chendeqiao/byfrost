package com.intelligence.browser.ui.home.navigation;

import com.intelligence.browser.data.RecommendUrlEntity;

public interface WebNavigationEditable {

    boolean addNewNavigation(String title, String url, String logo, boolean needCheck);

    void onFinishAddNewNavigation();

    boolean modifyNavigation(int position, RecommendUrlEntity entity, String newTitle, String newUrl);

    boolean deleteNavigation(int position);

    int doUrlContained(String url);

    boolean isEdit();

}
