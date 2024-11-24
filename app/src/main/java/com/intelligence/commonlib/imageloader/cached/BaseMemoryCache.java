/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
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
import android.util.ArrayMap;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public abstract class BaseMemoryCache implements MemoryCache {

    /** Stores not strong references to objects */
    private final Map<String, Reference<Bitmap>> mSoftMap = Collections.synchronizedMap(new ArrayMap<String, Reference<Bitmap>>());

    @Override
    public Bitmap get(String key) {
        Bitmap result = null;
        Reference<Bitmap> reference = mSoftMap.get(key);
        if (reference != null) {
            result = reference.get();
        }
        return result;
    }

    @Override
    public boolean put(String key, Bitmap value) {
        mSoftMap.put(key, createReference(value));
        return true;
    }

    @Override
    public Bitmap remove(String key) {
        Reference<Bitmap> bmpRef = mSoftMap.remove(key);
        return bmpRef == null ? null : bmpRef.get();
    }

    @Override
    public Collection<String> keys() {
        synchronized (mSoftMap) {
            return new HashSet<String>(mSoftMap.keySet());
        }
    }

    @Override
    public void clear() {
        mSoftMap.clear();
    }

    /** Creates {@linkplain Reference not strong} reference of value */
    protected abstract Reference<Bitmap> createReference(Bitmap value);
}
