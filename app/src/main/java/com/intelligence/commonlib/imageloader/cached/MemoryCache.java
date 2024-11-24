/*******************************************************************************
 * Copyright 2014 Sergey Tarasevich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.intelligence.commonlib.imageloader.cached;

import android.graphics.Bitmap;

import java.util.Collection;

public interface MemoryCache {
    /**
     * Puts value into cache by key
     *
     * @return <b>true</b> - if value was put into cache successfully, <b>false</b> - if value was <b>not</b> put into
     * cache
     */
    boolean put(String key, Bitmap value);

    /** Returns value by key. If there is no value for key then null will be returned. */
    Bitmap get(String key);

    /** Removes item by key */
    Bitmap remove(String key);

    /** Returns all keys of cache */
    Collection<String> keys();

    /** Remove all items from cache */
    void clear();
}
