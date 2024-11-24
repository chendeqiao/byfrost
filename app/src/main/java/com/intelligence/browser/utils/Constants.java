package com.intelligence.browser.utils;

import com.intelligence.browser.database.provider.BrowserContract;

public class Constants {

    public static final String NATIVE_PAGE_URL = "x-native://homepage";

    public static final String SCREEN_BRIGHTNESS = "screen_brightness";

    public static final String FEEDBACK_CONTENT = "feedback_content";

    public static final String JAVASCRIPT_THEME_COLOR = "(function () {\n" +
            "   \"use strict\";\n" +
            "    var metas, i, tag;\n" +
            "    metas = document.getElementsByTagName('meta');\n" +
            "    if (metas !== null) {\n" +
            "        for (i = 0; i < metas.length; i++) {\n" +
            "            tag = metas[i].getAttribute('name');\n" +
            "            if (tag !== null && tag.toLowerCase() === 'theme-color') {\n" +
            "                return metas[i].getAttribute('content');\n" +
            "            }\n" +
            "            if (tag !== null && tag.toLowerCase() === 'msapplication-navbutton-color') {\n" +
            "                return metas[i].getAttribute('content');\n" +
            "            }\n" +
            "            if (tag !== null && tag.toLowerCase() === 'apple-mobile-web-app-status-bar-style') {\n" +
            "                return metas[i].getAttribute('content');\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            '\n' +
            "    var none = 'rgba(0, 0, 0, 0)';\n" +
            "    function rgb2hex(rgb){ \n" +
            "        rgb = rgb.match(/^rgba?[\\s+]?\\([\\s+]?(\\d+)[\\s+]?,[\\s+]?(\\d+)[\\s+]?,[\\s+]?(\\d+)[\\s+]?/i);\n" +
            "        if (rgb && rgb.length === 4) {\n" +
            "              if (parseInt(rgb[1],10) >= 200 && parseInt(rgb[2],10) >= 200 && parseInt(rgb[3],10) >= 200) {\n" +
            "                  return none\n" +
            "              }\n" +
            "              if (parseInt(rgb[1],10) === 0 && parseInt(rgb[2],10) === 0 && parseInt(rgb[3],10) === 0) {\n" +
            "                  return none\n" +
            "              }\n" +
            "        }\n" +
            "        return (rgb && rgb.length === 4) ? '#' + \n" +
            "        ('0' + parseInt(rgb[1],10).toString(16)).slice(-2) +\n" +
            "        ('0' + parseInt(rgb[2],10).toString(16)).slice(-2) +\n" +
            "        ('0' + parseInt(rgb[3],10).toString(16)).slice(-2) : '';\n" +
            "    }\n" +
            "\n" +
            "    var elems = document.body.querySelectorAll('div');\n" +
            "    if (elems !== null) {\n" +
            "        for (i = 0; i < elems.length && i < 50; i++) {\n" +
            "            if (window.getComputedStyle(elems[i]).getPropertyValue('background-color') !== none) {\n" +
            "                var strColor = rgb2hex(window.getComputedStyle(elems[i]).getPropertyValue('background-color'))\n" +
            "                if (strColor !== none) {\n" +
            "                    return strColor\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    if (window.getComputedStyle(document.body).getPropertyValue('background-color') !== none) {\n" +
            "         var strColor = rgb2hex(window.getComputedStyle(document.body).getPropertyValue('background-color'))\n" +
            "         if (strColor !== none) {\n" +
            "             return strColor\n" +
            "         }\n" +
            "    }\n" +
            "\n" +
            "    return '';\n" +
            "}());";

    public static final String MENU_ID = "menu_id";
    public static final String PAGE_INDEX = "page_index";

    public static final String DOWNLOAD_REFERANCE = "download_referance";
    public static final String DOWNLOAD_PATH = "download_path";
    public static final String DOWNLOAD_KEY = "download_key";
    public static final String DOWNLOAD_STATE = "download_state";

    public static final String DOWNLOAD_VIEW_ACTION = BrowserContract.AUTHORITY + ".action.download.view";
    public static final String DOWNLOAD_ITEM_ACTION = BrowserContract.AUTHORITY + ".action.download";
}

