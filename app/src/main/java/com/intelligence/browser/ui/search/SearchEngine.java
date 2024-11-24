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
package com.intelligence.browser.ui.search;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

public interface SearchEngine {

    // Used if the search engine is Google
    String GOOGLE = "google";

    /**
     * Gets the unique name of this search engine.
     */
    String getName();

    /**
     * Gets the human-readable name of this search engine.
     */
    CharSequence getLabel();

    /**
     * Starts a search.
     */
    Intent startSearch(Context context, String query, Bundle appData, String extraData);

    /**
     * Gets search suggestions.
     */
    Cursor getSuggestions(Context context, String query);

    /**
     * Checks whether this search engine supports search suggestions.
     */
    boolean supportsSuggestions();

    /**
     * Closes this search engine.
     */
    void close();

    /**
     * Checks whether this search engine should be sent zero char query.
     */
    boolean wantsEmptyQuery();
}
