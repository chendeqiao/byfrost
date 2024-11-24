/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intelligence.browser.utils;

import android.content.Context;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.ui.search.SearchEngine;
import com.intelligence.browser.ui.search.SearchEngineInfo;
import com.intelligence.browser.ui.search.SearchEngines;

public class UrlUtils {

    private UrlUtils() { /* cannot be instantiated */ }

    public static String filterBySearchEngine(Context context, String url) {
        SearchEngine searchEngine = BrowserSettings.getInstance()
                .getSearchEngine();
        if (searchEngine == null) return null;
        SearchEngineInfo engineInfo = SearchEngines
                .getInstance(context).getSearchEngineInfo(context, searchEngine.getName());
        if (engineInfo == null) return null;
        return engineInfo.getSearchUriForQuery(url);
    }
}
