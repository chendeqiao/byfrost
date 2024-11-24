package com.intelligence.commonlib.tools;

import android.content.Context;
import android.graphics.Typeface;

public class FontsUtil {
    private static final String FONT_CONDENSED_BOLD = "fonts/DIN Condensed.ttf";
    private static final String FONT_ALTERNATE_BOLD = "fonts/DIN Alternate.ttf";

    public FontsUtil() {
    }

    public static Typeface getCondensedBoldFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/DIN Condensed.ttf");
    }

    public static Typeface getAlternateBoldFont(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/DIN Alternate.ttf");
    }
}
