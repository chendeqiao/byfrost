/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.intelligence.browser.settings;

public interface PreferenceKeys {

    String PREF_AUTOFILL_ACTIVE_PROFILE_ID = "autofill_active_profile_id";
    String PREF_DEBUG_MENU = "debug_menu";

    // ----------------------
    // Keys for accessibility_preferences.xml
    // ----------------------
    String PREF_MIN_FONT_SIZE = "min_font_size";
    String PREF_TEXT_SIZE = "text_size";
    String PREF_TEXT_ZOOM = "text_zoom";
    String PREF_DOUBLE_TAP_ZOOM = "double_tap_zoom";
    String PREF_FORCE_USERSCALABLE = "force_userscalable";

    // ----------------------
    // Keys for settings_preferences.xml
    // ----------------------
    String PREF_WEB_BROWSING = "web_browsing";
    String PREF_PRIVACY_SECURITY = "privacy_security";
    String PREF_ADVANCED = "advanced";
    String PREF_APP_NAME = "app_name";
    String PREF_CUSTOM_DOWNLOAD_PATH = "custom_download_path";
    String PREF_NIGHT_MODE = "night_mode";
    String PREF_FULLSCREEN = "fullscreen";
    String PREF_TEMP_FULLSCREEN = "temp_fullscreen";
    String PREF_DEFAULT_BROWSER = "default_browser";
    String PREF_APP_SCORE = "app_score";
    String PREF_SHOW_STATUS_BAR = "show_status_bar";
    String PREF_DOWNLOAD = "download";

    String PREF_AUTOFIT_PAGES = "autofit_pages";
    String PREF_BLOCK_POPUP_WINDOWS = "block_popup_windows";
    String PREF_DEFAULT_TEXT_ENCODING = "default_text_encoding";
    String PREF_DEFAULT_AD_TIME = "default_ad_time";
    String PREF_ENABLE_JAVASCRIPT = "enable_javascript";
    String PREF_ENABLE_COOKIES_INCOGNITO = "enable_cookies_incognito";
    String PREF_RESET_DEFAULT_PREFERENCES = "reset_default_preferences";
    String PREF_SEARCH_ENGINE = "search_engine";
    String PREF_WEBSITE_SETTINGS = "website_settings";
    String PREF_SEARCH_SUGGESTIONS = "search_suggestions";
    String PREF_RESTORE_TABS_ON_STARTUP = "restore_tabs_on_startup";
    String PREF_HOMEPAGE_PICKER = "homepage_picker";
    String PREF_NOTIFICATION_TOOL_SHOW = "notification_tool_show";
    String PREF_SWIPE_WEBVIEW = "swipe_webview";

    String SEARCH_ENGINE_LOAD_TIME = "search_engine_load_time";

    // ----------------------
    // Keys for debug_preferences.xml
    // ----------------------
    String PREF_ENABLE_HARDWARE_ACCEL = "enable_hardware_accel";
    String PREF_ENABLE_HARDWARE_ACCEL_SKIA = "enable_hardware_accel_skia";
    String PREF_USER_AGENT = "user_agent";
    String PREF_USER_VIDEO = "user_video";
    String PREF_USER_SWIPE = "user_swipe";
    String PREF_DISABLE_PERF = "disable_perf";

    // ----------------------
    // Keys for general_preferences.xml
    // ----------------------
    String PREF_AUTOFILL_ENABLED = "autofill_enabled";
    String PREF_AUTOFILL_PROFILE = "autofill_profile";
    String PREF_HOMEPAGE = "homepage";
    String PREF_HOMEPAGE_CHANGED = "homepage_changed";
    String PREF_SYNC_WITH_CHROME = "sync_with_chrome";

    // ----------------------
    // Keys for hidden_debug_preferences.xml
    // ----------------------
    String PREF_ENABLE_LIGHT_TOUCH = "enable_light_touch";
    String PREF_ENABLE_NAV_DUMP = "enable_nav_dump";
    String PREF_ENABLE_TRACING = "enable_tracing";
    String PREF_ENABLE_VISUAL_INDICATOR = "enable_visual_indicator";
    String PREF_ENABLE_CPU_UPLOAD_PATH = "enable_cpu_upload_path";
    String PREF_JAVASCRIPT_CONSOLE = "javascript_console";
    String PREF_JS_ENGINE_FLAGS = "js_engine_flags";
    String PREF_NORMAL_LAYOUT = "normal_layout";
    String PREF_WIDE_VIEWPORT = "wide_viewport";
    String PREF_DEVERLOPER_OPTIONS = "deverloper_options";
    String PREF_OPEN_DEBUG = "open_debug";
    String PREF_CHANNEL = "channel";
    String PREF_NEW_FEATURES = "new_features";

    // ----------------------
    // Keys for lab_preferences.xml
    // ----------------------
    String PREF_ENABLE_QUICK_CONTROLS = "enable_quick_controls";

    // ----------------------
    // Keys for privacy_security_preferences.xml
    // ----------------------
    String PREF_CLEAR_DATA = "privacy_clear_data";
    String PREF_MULTI_LANGUAGE = "multi_language";
    String PREF_CLEAR_SELECTED_DATA = "privacy_clear_selected";
    String PREF_ACCEPT_COOKIES = "accept_cookies";
    String PREF_ENABLE_GEOLOCATION = "enable_geolocation";
    String PREF_PRIVACY_CLEAR_CACHE = "privacy_clear_cache";
    String PREF_PRIVACY_CLEAR_COOKIES = "privacy_clear_cookies";
    String PREF_PRIVACY_CLEAR_FORM_DATA = "privacy_clear_form_data";
    String PREF_PRIVACY_CLEAR_GEOLOCATION_ACCESS = "privacy_clear_geolocation_access";
    String PREF_PRIVACY_CLEAR_HISTORY = "privacy_clear_history";
    String PREF_PRIVACY_CLEAR_PASSWORDS = "privacy_clear_passwords";
    String PREF_REMEMBER_PASSWORDS = "remember_passwords";
    String PREF_SAVE_FORMDATA = "save_formdata";
    String PREF_SHOW_SECURITY_WARNINGS = "show_security_warnings";
    String PREF_CLEAR_HISTORY_CACHE_EXITING = "clear_history_cache_exiting";
    String PREF_AD_BLOCK = "ad_block";
    String PREF_IMG_AD_BLOCK_COUNT = "img_ad_block_count";
    String PREF_JS_AD_BLOCK_COUNT = "js_ad_block_count";
    String PREF_POPUP_AD_BLOCK_COUNT = "popup_ad_block_count";
    String PREF_CONFIRM_ON_EXIT = "confirm_on_exit";
    String PREF_RED_BADGE_NOTIFY = "show_red_badge_notify";
    String PREF_HEADER_NOTIFY = "show_header_notify";

    // ----------------------
    // Keys for About_preferences.xml
    // ----
    String PREF_CONTACT_US = "contact_us";
    String PREF_FEEDBACK = "feedback";
    String PREF_VERSION = "version";
    String PREF_CHECK_UPDATE = "check_update";
    String PREF_TERMS_PRIVACY = "terms_privacy";

    String PREF_DOWNLOAD_DIALOG_SHOW = "download_dialog_show";
    String PREF_DOWNLOAD_ADM = "download_adm";

    /**
     * 是否请求过引擎
     */
    String IS_FIRST_CREATE_ENGINES = "is_first_create_engines";

    // ----------------------
    // Keys for bandwidth_preferences.xml
    // ----------------------
    String PREF_LOAD_IMAGES = "load_images";

    String PREF_LOAD_NO_FOOTER = "pref_load_no_footer";

    // ----------------------
    // Keys for browser recovery
    // ----------------------
    /**
     * The last time recovery was started as System.currentTimeMillis.
     * 0 if not set.
     */
    String KEY_LAST_RECOVERED = "last_recovered";

    /**
     * Key for whether or not the last run was paused.
     */
    String KEY_LAST_RUN_PAUSED = "last_paused";

    String DEFAULT_CACHE_DATA_COPIED = "pref_default_cache_data_copied";

    String DEFAULT_CACHE_WEB_ICONS = "pref_default_cache_web_icons";

    String RESTRICTIONS = "App Restrictions";

    String LAST_READ_ALLOW_GEOLOCATION_ORIGINS = "last_read_allow_geolocation_origins";

    String BRIGHTNESS = "brightness";
}
