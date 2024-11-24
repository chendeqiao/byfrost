package com.intelligence.browser.ui.home.navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.intelligence.browser.controller.BackgroundHandler;
import com.intelligence.browser.settings.PreferenceKeys;
import com.intelligence.browser.R;
import com.intelligence.browser.utils.ImageUtils;
import com.intelligence.browser.utils.RecommendUrlUtil;
import com.intelligence.commonlib.network.Network;
import com.intelligence.commonlib.tools.SharedPreferencesUtils;
import com.intelligence.commonlib.tools.UrlUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavigationService {

    private static final int WEB_ICON_STANDARD_SIZE = 32;

    private static final String HOT_WEB_ICONS_DIR_NAME = "hot_web_icons";

    /**
     * 根据Url获取
     *
     * @param context  Context
     * @param callback 网络获取图片回调,获取失败不回调
     * @return 从本地读取网址icon, 如果为热门网站或者无缓存则返回空
     */
    static byte[] loadWebsiteIconAndSave(final Context context, final String webUrl, final OnAsyncGetWebIconCallback callback) {
        if(TextUtils.isEmpty(webUrl)){
            return null;
        }
        //1.如果为热门推荐网站,则本地获取
        final WebIconLoader webIconLoader = new WebIconLoader(context);
        final String host = UrlUtils.getHost(webUrl);
        Bitmap iconBmp = webIconLoader.readCache(host);
        final byte[] iconBytes = ImageUtils.bitmapToBytes(iconBmp);
        //本地读取成功则不需要网络获取
        if (checkWebIconSize(iconBmp) && iconBytes != null && iconBytes.length > 0) {
            return iconBytes;
        }
        if (callback != null)
            BackgroundHandler.execute(new Runnable() {
                @Override
                public void run() {
                    //2.通过爬虫抓取网站icon
                    String iconUrl = null;
                    try {
                        iconUrl = getIconUrlString(context, webUrl);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
//                    if (TextUtils.isEmpty(iconUrl)) return;
                    webIconLoader.syncLoadBitmap(iconUrl, webUrl, new FaviconCallback() {
                        @Override
                        public void onFaviconReceived(Bitmap favicon) {
                            if (favicon == null) {
                                callback.onGetWebIconFromNetwork(null);
                                return;
                            }
                            final byte[] iconBytes = ImageUtils.bitmapToBytes(favicon);
                            if (iconBytes == null || iconBytes.length == 0) return;
                            webIconLoader.saveCache(host, favicon);
                            BackgroundHandler.getMainHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onGetWebIconFromNetwork(iconBytes);
                                }
                            });
                        }
                    });
                }
            });
        return null;
    }

    public static boolean checkWebIconSize(Bitmap webIconBmp) {
        return webIconBmp != null && webIconBmp.getWidth() >= WEB_ICON_STANDARD_SIZE
                && webIconBmp.getHeight() >= WEB_ICON_STANDARD_SIZE;
    }

    private static final Pattern[] ICON_PATTERNS = new Pattern[]{
            Pattern.compile("rel=[\"']shortcut icon[\"'][^\r\n>]+?((?<=href=[\"']).+?(?=[\"']))"),
            Pattern.compile("((?<=href=[\"']).+?(?=[\"']))[^\r\n<]+?rel=[\"']shortcut icon[\"']")};
    private static final Pattern HEAD_END_PATTERN = Pattern.compile("</head>");

    // 获取稳定url
    private static String getFinalUrl(Context context, String urlString) {
        HttpURLConnection connection = null;
        try {
            connection = Network.getProxiableURLConnection(context, new URL(urlString));
            connection.connect();
            // 是否跳转，若跳转则跟踪到跳转页面
            if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM
                    || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = connection.getHeaderField("Location");
                if (!location.contains("http")) {
                    location = urlString + "/" + location;
                }
                return location;
            }
        } catch (Exception e) {
            //do nothing
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return urlString;
    }

    // 获取Icon地址
    private static String getIconUrlString(Context context, String urlString) throws MalformedURLException {
        urlString = getFinalUrl(context, urlString);
        URL url = new URL(urlString);
        String iconUrl = url.getProtocol() + "://" + url.getHost() + "/favicon.ico";// 保证从域名根路径搜索
        if (hasRootIcon(context,iconUrl)) {
            return iconUrl;
        }
        return getIconUrlByRegex(context, urlString);
    }

    // 判断在根目录下是否有Icon
    private static boolean hasRootIcon(Context context, String urlString) {
        HttpURLConnection connection = null;
        try {
            connection = Network.getProxiableURLConnection(context, new URL(urlString));
            connection.connect();
            int responseCode = connection.getResponseCode();
            String contentType = connection.getContentType();
            return responseCode == HttpURLConnection.HTTP_OK &&
                    !TextUtils.isEmpty(contentType) && contentType.contains("image");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    // 从html中获取Icon地址
    private static String getIconUrlByRegex(Context context, String urlString) {

        try {
            String headString = getHead(context, urlString);
            if (TextUtils.isEmpty(headString)) return null;
            for (Pattern iconPattern : ICON_PATTERNS) {
                Matcher matcher = iconPattern.matcher(headString);
                if (matcher.find()) {
                    String iconUrl = matcher.group(1);
                    if (iconUrl.contains("http"))
                        return iconUrl;
                    if (iconUrl.charAt(0) == '/') {//判断是否为相对路径或根路径
                        URL url = new URL(urlString);
                        iconUrl = url.getProtocol() + "://" + url.getHost() + iconUrl;
                    } else {
                        iconUrl = urlString + "/" + iconUrl;
                    }
                    return iconUrl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取截止到head尾标签的文本
    private static String getHead(Context context, String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            connection = Network.getProxiableURLConnection(context,new URL(urlString));
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            StringBuilder headBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                Matcher matcher = HEAD_END_PATTERN.matcher(line);
                if (matcher.find())
                    break;
                headBuilder.append(line);
            }

            return headBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (connection != null)
                    connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void downloadAndUnzipWebIcons(final Context context) {
        BackgroundHandler.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    File dataPath = new File(context.getCacheDir() + "/" + HOT_WEB_ICONS_DIR_NAME);
                    boolean needCopy = (boolean) SharedPreferencesUtils.get(PreferenceKeys.DEFAULT_CACHE_WEB_ICONS, true);
                    if (!needCopy) {
                        if (dataPath.exists()) {
                            if (!dataPath.isDirectory()) {
                                dataPath.delete();
                                needCopy = true;
                            }
                            if (dataPath.list() == null || dataPath.list().length == 0) {
                                needCopy = true;
                            }
                        } else {
                            needCopy = true;
                        }
                    }
                    if (needCopy) {
                        File cacheDir = context.getCacheDir();
                        OutputStream os = null;
                        File tmpZipFile = new File(cacheDir, UUID.randomUUID().toString() + ".zip");
                        try {
                            os = new FileOutputStream(tmpZipFile);
                            Network.downloadFile(context, context.getString(R.string.download_web_icons_url), os);
                            os.flush();
                            // TODO: 2024/10/12 下载文件
//                        FileUtils.unZipFile(tmpZipFile.getPath(), dataPath.getParent());
                            SharedPreferencesUtils.put(PreferenceKeys.DEFAULT_CACHE_WEB_ICONS, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (os != null) os.close();
                            } catch (Exception e) {
                            }
                            tmpZipFile.delete();
                        }
                    }
                }catch (Exception e){

                }
            }
        });
    }

    static Bitmap getHotWebIconByUrl(Context context, String url) {
        return getHotWebIcon(context, RecommendUrlUtil.getWebNameByUrl(url));
    }

    static Bitmap getHotWebIcon(Context context, String webName) {
        File imgFile = new File(context.getCacheDir() + "/" + HOT_WEB_ICONS_DIR_NAME + "/" + webName + ".png");
        return ImageUtils.readImgFile(imgFile);
    }

    public interface FaviconCallback {
        void onFaviconReceived(Bitmap favicon);
    }

    interface OnAsyncGetWebIconCallback {
        /*网络获取,获取失败不回调*/
        void onGetWebIconFromNetwork(@NonNull byte[] webIcon);
    }

}
