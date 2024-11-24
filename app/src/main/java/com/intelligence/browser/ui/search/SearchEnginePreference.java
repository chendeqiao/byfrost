package com.intelligence.browser.ui.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.ui.home.BrowserMainPageController;
import com.intelligence.browser.R;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;

import java.util.Arrays;
import java.util.List;

public class SearchEnginePreference {

    public static SearchEngineInfo getdefaultSelectEngine(Context context) {
        String name = BrowserSettings.getInstance().getSearchEngineName();
        if (SearchEngines.getInstance(context).getSearchEngineMap() != null
                && SearchEngines.getInstance(context).getSearchEngineMap().containsKey(name)) {
            return new SearchEngineInfo(SearchEngines.getInstance(context).getSearchEngineMap().get(name));
        } else {
            String[] searchEngines = context.getResources().getStringArray(R.array.search_engines);
            List<String> tempList = Arrays.asList(searchEngines);
            if (!tempList.contains(name)) {
                //防止切换语言时导致引擎出错
                name = SharedPreferencesUtils.getDefaultSearchEngine(context);
                BrowserSettings.getInstance().setSearchEngineName(name);
            }
            return getSearchEngine(context, name);
        }

    }

    /**
     * get Search Engine
     *
     * @param context
     * @return
     */
    public static SearchEngineInfo getSearchEngine(Context context, String name) {
        if (SearchEngines.getInstance(context).getSearchEngineMap() != null
                && SearchEngines.getInstance(context).getSearchEngineMap().containsKey(name)) {
            return new SearchEngineInfo(SearchEngines.getInstance(context).getSearchEngineMap().get(name));
        } else {
            return new SearchEngineInfo(context, name);
        }
    }

    /**
     * Use url search
     *
     * @param context
     * @param key
     * @return
     */
    public static String getSearchUrl(Context context, String key) {
        if (SearchEngines.getInstance(context).getSearchEngineMap() != null
                && SearchEngines.getInstance(context).getSearchEngineMap().containsKey(key)) {
            return SearchEngines.getInstance(context).getSearchEngineMap().get(key).getUrl();
        } else {
            return getSearchEngine(context, key).getSearchUriForQuery(key);
        }
    }

    /**
     * get label from name
     *
     * @param context
     * @param name
     * @return
     */
    public static String getSearchValue(Context context, String name) {
        if (SearchEngines.getInstance(context).getSearchEngineMap() != null
                && SearchEngines.getInstance(context).getSearchEngineMap().containsKey(name)) {
            return SearchEngines.getInstance(context).getSearchEngineMap().get(name).getTitle();
        } else {
            return getSearchEngine(context, name).getLabel();
        }
    }

    /**
     * get Key from name
     *
     * @param context
     * @param name
     * @return
     */
    public static String getSearchKey(Context context, String name) {
        if (SearchEngines.getInstance(context).getSearchEngineMap() != null
                && SearchEngines.getInstance(context).getSearchEngineMap().containsKey(name)) {
            return SearchEngines.getInstance(context).getSearchEngineMap().get(name).getTitle();
        } else {
            return getSearchEngine(context, name).getName();
        }
    }

    public static String getSearchIconUrl(Context context, String name) {
        if (SearchEngines.getInstance(context).getSearchEngineMap() != null
                && SearchEngines.getInstance(context).getSearchEngineMap().containsKey(name)) {
            return SearchEngines.getInstance(context).getSearchEngineMap().get(name).getImageUrl();
        } else {
            return null;
        }
    }

    public static Bitmap getSearchIconBitmap(Context context, String name) {
        if (SearchEngines.getInstance(context).getSearchEngineMap() != null &&
                SearchEngines.getInstance(context).getSearchEngineMap().containsKey(name)) {
            return ImageUtils.getBitmap(SearchEngines.getInstance(context).getSearchEngineMap().get(name).getImageIcon(), null);
        } else {
            return null;
        }
    }

    public static void setSearchEngineIcon(Context context, ImageView imageView, String name) {
        if (name == null) {
            setSearchEngineIconLocal(context, imageView, name);
        }
        Bitmap bitmap = getSearchIconBitmap(context, name);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            setSearchEngineIconLocal(context, imageView, name);
        }
    }

    public static boolean isHaveSearchEngineLogo(Context context, String name) {
        try {
            String[] mSeachEngineName = context.getResources().getStringArray(R.array.custom_search_engine);
            for (int num = 0; num < mSeachEngineName.length; num++) {
                String listName = mSeachEngineName[num];
                if (name.toLowerCase().contains(listName)) {
                    return true;
                }
            }
        } catch (Exception e) {

        }
        return false;
    }

    private static void setSearchEngineIconLocal(Context context, ImageView imageView, String name) {
        try {
            String[] mSeachEngineName = context.getResources().getStringArray(R.array.custom_search_engine);
            for (int num = 0; num < mSeachEngineName.length; num++) {
                String listName = mSeachEngineName[num];
                if (name.toLowerCase().contains(listName)) {
                    imageView.setImageDrawable(context.getResources().getDrawable(BrowserMainPageController.mIconSearch[num]));
                    return;
                }
            }
        } catch (Exception e) {

        }
    }


    public static void getDefaultSearchIcon(Context context, ImageView imageView) {
        String name = BrowserSettings.getInstance().getSearchEngineName();
        if (SearchEngines.getInstance(context).getSearchEngineMap() != null
                && SearchEngines.getInstance(context).getSearchEngineMap().containsKey(name)) {
            if (SearchEngines.getInstance(context).getSearchEngineMap().get(name).getImageIcon() != null) {
                imageView.setImageBitmap(ImageUtils.getBitmap(SearchEngines.getInstance(context).getSearchEngineMap().get(name).getImageIcon(), null));
            } else if (SearchEngines.getInstance(context).getSearchEngineMap().get(name).is_default == 1) {
                imageView.setImageResource(R.drawable.browser_recommend_blank);
            }
        } else {
            String[] searchEngineName = context.getResources().getStringArray(R.array.search_engines);
            boolean contains = false;
            for (int num = 0; num < searchEngineName.length; num++) {
                //切换语言的情况
                if (name.toLowerCase().equals(searchEngineName[num].toLowerCase())) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                name = SharedPreferencesUtils.getDefaultSearchEngine(context);;
                BrowserSettings.getInstance().setSearchEngineName(name);
            }
            setSearchEngineIconLocal(context, imageView, name);
        }
    }
}
