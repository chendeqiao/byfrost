package com.intelligence.commonlib.imageloader.utils;

import com.intelligence.commonlib.imageloader.impl.ImageSize;

public class MemoryCacheUtils {
    private static final String URI_AND_SIZE_SEPARATOR = "_";
    private static final String WIDTH_AND_HEIGHT_SEPARATOR = "x";

    private MemoryCacheUtils() {
    }

    /**
     * Generates key for memory cache for incoming image (URI + size).<br />
     * Pattern for cache key - <b>[imageUri]_[width]x[height]</b>.
     */
    public static String generateKey(String imageUri, ImageSize targetSize) {
        if (targetSize == null) return imageUri;
        return new StringBuilder(imageUri).append(URI_AND_SIZE_SEPARATOR).append(targetSize.getWidth()).append(WIDTH_AND_HEIGHT_SEPARATOR).append(targetSize.getHeight()).toString();
    }
}
