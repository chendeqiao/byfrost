package com.intelligence.commonlib.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.webkit.WebView;

import androidx.annotation.StringRes;

import com.intelligence.browser.R;
import com.intelligence.commonlib.Global;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nixionglin on 2019/8/28.
 * 字符串工具类
 */

public class StringUtil {

    /**
     * 判断字符是否是汉字
     *
     * @param c 字符
     * @return 是否是汉字
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);

        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }


    /**
     * @param str
     * @return
     */
    public static String recoder(String str) {
        try {
            return StandardCharsets.ISO_8859_1.newEncoder().canEncode(str) ? new String(str.getBytes(StandardCharsets.ISO_8859_1), "GB2312") : str;
        } catch (UnsupportedEncodingException e) {
            //Method is documented to just ignore invalid support encoding
            //recoder will be unchanged
            return str;
        }
    }

    public static String cutString(String string, int endIndes) {
        if (endIndes < string.length()) {
            string = string.substring(0, endIndes) + "...";
        }
        return string;
    }

    public static boolean isPhoneNumber(String phoneNo) {
        if (TextUtils.isEmpty(phoneNo)) {
            return false;
        }
        if (phoneNo.length() == 11) {
            for (int i = 0; i < 11; i++) {
                if (!PhoneNumberUtils.isISODigit(phoneNo.charAt(i))) {
                    return false;
                }
            }
            Pattern p = Pattern.compile("^((13[^4,\\D])" + "|(134[^9,\\D])" +
                    "|(14[5,7])" +
                    "|(15[^4,\\D])" +
                    "|(17[3,6-8])" +
                    "|(18[0-9]))\\d{8}$");
            Matcher m = p.matcher(phoneNo);
            return m.matches();
        }
        return false;
    }

    /**
     * 判断邮箱是否合法
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        // Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p = Pattern
                .compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");// 复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static String getString(@StringRes int strId, Object... objects) {
        return Global.getInstance().getString(strId, objects);
    }

    public static boolean checkAsciiText(String text) {
        if (TextUtils.isEmpty(text)) return false;
        final char ASCII = 127;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > ASCII) {
                return false;
            }
        }
        return true;
    }

    public static int getInt(String str, int defaultValue) {
        if (TextUtils.isEmpty(str)) {
            return defaultValue;
        } else {
            int result = defaultValue;
            try {
                result = Integer.parseInt(str);
            } catch (Exception var4) {
                var4.printStackTrace();
            }
            return result;
        }
    }

    public static ArrayList getArrayForStrings(String[] ImageUrls) {
        ArrayList arrayList = new ArrayList();
        try {
            for (String s : ImageUrls) {
                if (!TextUtils.isEmpty(s)) {
                    arrayList.add(s);
                }
            }
        } catch (Exception e) {
        }
        return arrayList;
    }

    public static String[] getStringForArray(ArrayList<String> list) {
        if (CollectionUtils.isEmpty(list)) {
            return new String[]{};
        }
        String[] allPicture = new String[list.size()];
        try {
            for (int i = 0; i < list.size(); i++) {
                allPicture[i] = list.get(i);
            }
        } catch (Exception e) {

        }
        return allPicture;
    }

    public static int getPosition(String[] images, String imageUrl) {
        if (images == null || images.length == 0) {
            return 0;
        }
        for (int i = 0; i < images.length; i++) {
            if (images[i].equals(imageUrl)) {
                return i;
            }
        }
        return 0;
    }

    public static void injectJavascript(Activity activity, WebView webview, String url) {
        try {
            // inject javascript
            String js = "";
            InputStream is = activity.getResources().getAssets().open(url);
            if (is != null) {
                InputStreamReader reader = new InputStreamReader(is);
                BufferedReader bufreader = new BufferedReader(reader);
                String line;
                while ((line = bufreader.readLine()) != null) {
                    js += line;
                }

                bufreader.close();
                reader.close();
                is.close();
            }
            if (webview != null) {
                webview.loadUrl("javascript:" + js);
            }
        } catch (Exception e) {

        }
    }


    public static String join(@androidx.annotation.NonNull CharSequence delimiter, @androidx.annotation.NonNull Object[] tokens) {
        final int length = tokens.length;
        if (length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("'" + tokens[0] + "'");
        for (int i = 1; i < length; i++) {
            sb.append(delimiter);
            sb.append("'" + tokens[i] + "'");
        }
        return "[" + sb.toString() + "]";
    }

    public static ArrayList<String> getLastTwoHundred(ArrayList<String> list,int numble) {
        // 获取列表的大小
        int size = list.size();

        // 如果列表少于 200 条，直接返回整个列表
        if (size <= numble) {
            return new ArrayList<>(list);
        }

        // 否则返回最后 200 条数据
        return new ArrayList<>(list.subList(size - numble, size));
    }

    public static SpannableString spanWrap(Context context,String keyword, String str) {
        try {
            if (str == null || str.isEmpty()) {
                return new SpannableString("");
            }
            SpannableString s = new SpannableString(str);
//		Matcher m = pattern.matcher(s);
            int index = str.indexOf(keyword);
            if (index >= 0) {
                s.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.five_star_btn_color_high)), index, index + keyword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                s.setSpan(new StyleSpan(Typeface.BOLD), index, index + keyword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return s;
        }catch (Exception e){
            return new SpannableString(str);
        }
    }
}
