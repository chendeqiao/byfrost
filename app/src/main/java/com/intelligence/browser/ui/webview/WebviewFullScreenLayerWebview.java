package com.intelligence.browser.ui.webview;

import static com.intelligence.browser.ui.webview.WebviewFullScreenLayerWebview.TOUCH_OPERATION_TYPE.BRIGHTNESS;
import static com.intelligence.browser.ui.webview.WebviewFullScreenLayerWebview.TOUCH_OPERATION_TYPE.FAST_FORWARD;
import static com.intelligence.browser.ui.webview.WebviewFullScreenLayerWebview.TOUCH_OPERATION_TYPE.NONE;
import static com.intelligence.browser.ui.webview.WebviewFullScreenLayerWebview.TOUCH_OPERATION_TYPE.REWIND;
import static com.intelligence.browser.ui.webview.WebviewFullScreenLayerWebview.TOUCH_OPERATION_TYPE.VOLUME;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intelligence.browser.ui.BaseUi;
import com.intelligence.browser.R;
import com.intelligence.browser.downloads.BrowserNetworkStateNotifier;
import com.intelligence.browser.utils.DeviceControlUtils;
import com.intelligence.browser.view.BatteryView;
import com.intelligence.browser.view.GestureView;
import com.intelligence.browser.view.OnCustomGestureListener;
import com.intelligence.browser.ui.widget.BrowserDialog;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.NetworkUtils;
import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.download.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class WebviewFullScreenLayerWebview implements
        WebviewVideoPlayerLayer,
        OnCustomGestureListener
        , WebviewVideoPlayerLayer.MediaInfoListener
        , BrowserNetworkStateNotifier.NetworkStateChangedListener {

    // static variable
    private static final int DELAY_TIME = 2000;
    private static final int TIME_PROMPT = 0;
    private static final int ALPHA_TRANSPARENT = 0;
    private static final int PROGRESS_ZERO = 0;
    private static final int PROGRESS_STEP = 10;
    private static final int PROGRESS_MIN = 2; // This is for the min brightness
    private static final int PROGRESS_MAX = 100;
    private static final float BRIGHTNESS_MAX = 1.0f;
    private static final int TIME_SCALE = 60;
    private static final int HOUR_SECOND_SCALE = 3600;
    private static final int DECIMAL = 10;
    private static final int ZERO = 0;
    private static final int TITLE_BAR_ALPHA = 138;  // Gui design, 54% alpha
    private static final int UPDATE_PLAY_BUTTON = 0;
    private static final int UPDATE_TOTAL_DURATION = 1;
    private static final int UPDATE_CURRENT_DURATION = 2;
    private static final int INIT_MEDIA_CONTROLS = 3;


    enum TOUCH_OPERATION_TYPE {
        NONE,
        VOLUME,
        BRIGHTNESS,
        FAST_FORWARD,
        REWIND,
    }

    private GestureView mToolLayer; // The layout over the fullscreen webview
    private RelativeLayout mLockLayout;
    private ImageButton mLockButton;
//    private ImageButton mPlayButton;

    // volume short cut controller
    private ImageView mVolumeImage;
    private ImageView mBrightnessImage;
    private ProgressBar mProgressBar;
    private LinearLayout mQuickController;

    private ImageView mFastImage;
    private ImageView mRewindImage;
    private TextView mSeekTime;
    private LinearLayout mPlayController;

    // volume bar, a idiotic design
//    private SeekBar mVolumeBar;

    //TitleBar
    private RelativeLayout mTitleBar;
    private ImageView mBack;
    private ImageView mShare;
    private TextView  mTitle;
    private TextView  mSysTime;
    private BatteryView mBattery;
    private RelativeLayout mBatteryLayout;
//    private WifiView mWifi;
//    private RelativeLayout mWifiLayout;
//    private MobileSignalView mSignalView;
//    private RelativeLayout mSignalLayout;
//    private RelativeLayout mMediaControls;
//    private ImageView mMediaControlPlayBtn;
//    private TextView mMediaControlCurrentDuration;
//    private SeekBar mMediaControlProgressBar;
//    private TextView mMediaControlTotalDuration;
//    private ImageView mMediaControlVolumeBtn;


//    private GestureDetector mGestureDetector;
    private Activity mContext;
    private BaseUi mBaseUi;

    // Device control
    private AudioManager mAudioManager;
    private TelephonyManager mTelephonyManager;
    PhoneStateListener mPhoneListener;

    private WebviewVideoPlayerLayer.Listener mListener;

    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mLockState = false;
    private boolean mThreadState = false;
    private boolean mScrollState = false;
    private boolean mCanControlVideo = false;
    private TOUCH_OPERATION_TYPE mTouchType = NONE;
    private int mMaxVolume;
    private int mVolumeProgress;
    private int mBrightnessProgress;
    private int mDuration;
    private int mCurrentPlayTime;
    private int mSeconds;
    private int mSignalPercent = 50;
    private float mSavedBrightness;
    private boolean mIsPaused = false;
    private int mMediaControlProgress;

    private Runnable mThread;
    private Runnable mHideVolumeThread;
    private Handler mHandler;

    private boolean mInMultiWindow;

    private ImageView mChangeScreenView;
    private ImageView mDownloadVideo;
    private ImageView mAddBookmarks;
    private ViewGroup mBrowserFunction;

    private String mUserAgent;
    private VideoInfo mVideoInfo;

    public WebviewFullScreenLayerWebview(BaseUi ui) {
        mBaseUi = ui;
        mContext = mBaseUi.getActivity();
        mTopHeight = (int)ScreenUtils.dpToPx(mContext, 60);
        initDeviceControl(mContext);
        initOverlayer(mContext);
        try {
            injectJavascript();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initEventHandle(mContext);
        removeWebMediaControls();
    }

    private void initEventHandle(Context context) {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_PLAY_BUTTON:
//                        if ((boolean) msg.obj) {
//                            mMediaControlPlayBtn.setImageResource(R.drawable.ic_browser_video_pause);
//                        } else {
//                            mMediaControlPlayBtn.setImageResource(R.drawable.ic_browser_video_play);
//                        }
                        break;
                    case UPDATE_TOTAL_DURATION:
//                        mMediaControlTotalDuration.setText((String) msg.obj);
                        break;
                    case UPDATE_CURRENT_DURATION:
//                        mMediaControlCurrentDuration.setText((String) msg.obj);
//                        if (mDuration > 0) {
//                            mMediaControlProgressBar.setProgress(100 * mCurrentPlayTime / mDuration);
//                        }

                        break;
                    case INIT_MEDIA_CONTROLS:
                        break;
                    default:
                        break;
                }
            }
        };
        mThread = new TimerThread();
//        mGestureDetector = new GestureDetector(context, new FullScreenGestureListener(this));
        startThread(DELAY_TIME);
    }

    private void initDeviceControl(Activity context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//                String signalInfo = signalStrength.toString();
//                String[] parts = signalInfo.split(" ");
//                // lte dbm
//                int ltedbm = (!TextUtils.isEmpty(parts[9]) && TextUtils.isDigitsOnly(parts[9])) ? Integer.parseInt(parts[9]) : -113;
//                // 2g/3g dbm
//                int asu = signalStrength.getGsmSignalStrength();
//                int dbm = -113 + 2 * asu;
//                switch (mTelephonyManager.getNetworkType()) {
//                    case TelephonyManager.NETWORK_TYPE_LTE:
//                        if (ltedbm >= -44) {
//                            mSignalPercent = 100;
//                        } else if (ltedbm >= -98) {
//                            mSignalPercent = 75;
//                        } else if (ltedbm >= -108) {
//                            mSignalPercent = 50;
//                        } else if (ltedbm >= -128) {
//                            mSignalPercent = 25;
//                        } else {
//                            mSignalPercent = 0;
//                        }
//                        break;
//                    case TelephonyManager.NETWORK_TYPE_HSDPA:
//                    case TelephonyManager.NETWORK_TYPE_HSPA:
//                    case TelephonyManager.NETWORK_TYPE_HSUPA:
//                    case TelephonyManager.NETWORK_TYPE_UMTS:
//                        if (dbm > -75) {
//                            mSignalPercent = 100;
//                        } else if (dbm > -85) {
//                            mSignalPercent = 75;
//                        } else if (dbm > -95) {
//                            mSignalPercent = 50;
//                        } else if (dbm > -100) {
//                            mSignalPercent = 25;
//                        } else {
//                            mSignalPercent = 0;
//                        }
//                        break;
//                    default:
//                        if (asu < 0 || asu >= 99) {
//                            mSignalPercent = 0;
//                        } else if (asu >= 16) {
//                            mSignalPercent = 100;
//                        } else if (asu >= 8) {
//                            mSignalPercent = 75;
//                        } else if (asu >= 4) {
//                            mSignalPercent = 50;
//                        } else {
//                            mSignalPercent = 25;
//                        }
//                        break;
//                }

                super.onSignalStrengthsChanged(signalStrength);
            }
        };
        mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        mSavedBrightness = DisplayUtil.getScreenBrightness(mContext);
        mMaxVolume = DeviceControlUtils.getMaxVolume(mAudioManager);
    }

    private void injectJavascript() throws IOException {
        // inject javascript
        String js = "";
        InputStream is = mContext.getResources().getAssets().open("video_player.js");
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

        WebView webview = mBaseUi.getBrowserWebView();
        mUserAgent = webview.getSettings().getUserAgentString();
        if (webview != null) {
            webview.loadUrl("javascript:" + js);
        }
    }

    private void initOverlayer(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.browser_fullscreen_overlayer, null);

        // Get all views
        mToolLayer = view.findViewById(R.id.video_overlay);
        mToolLayer.setSimpleOnGestureListener(this);

        mTitleBar = mToolLayer.findViewById(R.id.video_title_bar);
        mTitleBar.setBackgroundColor(context.getResources().getColor(R.color.video_title_bar_background));
        mTitleBar.getBackground().setAlpha(TITLE_BAR_ALPHA);
        mBack = mToolLayer.findViewById(R.id.video_back);
        mBack.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (mBack.getVisibility() == View.VISIBLE) {
                    WebView webview = mBaseUi.getBrowserWebView();
                    if (webview != null) {
                        webview.loadUrl("javascript:__byfrost_browser__.exitFullScreen()");
                    }
                }
            }

        });

        mTitle = mToolLayer.findViewById(R.id.video_title);
        mBrowserFunction = mToolLayer.findViewById(R.id.browser_full_function);
        mChangeScreenView = mToolLayer.findViewById(R.id.full_change_screen);
        mDownloadVideo = mToolLayer.findViewById(R.id.download_video);
        mDownloadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mVideoInfo == null || TextUtils.isEmpty(mVideoInfo.mVideoUrl)){
                    return;
                }
                if (mBaseUi != null && mBaseUi.getController() != null) {
                    mBaseUi.getController().onDownloadStart(mVideoInfo.mVideoUrl, mVideoInfo.mUserAgent, mVideoInfo.mMimeType, mVideoInfo.mContentLength);
                }
            }
        });
        mChangeScreenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBaseUi != null) {
                    mBaseUi.changeScreenSize();
                }
                onSingleTapUp(null);
            }
        });
        mAddBookmarks = mToolLayer.findViewById(R.id.full_add_bookmarks);
        if (mBaseUi != null) {
            mAddBookmarks.setImageResource(mBaseUi.canAddBookmark() ? R.drawable.browser_full_added_bookmark : R.drawable.browser_full_add_bookmark);
        }
        mAddBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBaseUi != null) {
                    boolean isAdd = mBaseUi.setBookMarksStyle();
                    mAddBookmarks.setImageResource(isAdd ? R.drawable.browser_full_added_bookmark : R.drawable.browser_full_add_bookmark);
                    onSingleTapUp(null);
                }
            }
        });
        mTitle.setText(getVideoTitle());
        mShare = mToolLayer.findViewById(R.id.video_share);
        mShare.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (mShare.getVisibility() == View.VISIBLE) {
                    mBaseUi.shareCurrentPage();
                }
            }
        });
        mBatteryLayout = mToolLayer.findViewById(R.id.video_battery_layout);
        mBattery = mToolLayer.findViewById(R.id.video_battery);
        mBattery.setLevelHeight(DeviceControlUtils.getBatteryVoltagePercent(mContext));
//        mWifiLayout = mToolLayer.findViewById(R.id.wifi_layout);
//        mWifi = mToolLayer.findViewById(R.id.video_wifi);
//        mSignalLayout = mToolLayer.findViewById(R.id.signal_layout);
//        mSignalView = mToolLayer.findViewById(R.id.video_mobile_signal);
        mSysTime = mToolLayer.findViewById(R.id.video_sys_time);
        mSysTime.setText(getTextSystime());

        mLockLayout = mToolLayer.findViewById(R.id.video_lock_layout);
        mLockButton = mToolLayer.findViewById(R.id.video_controller_lock);
        mLockButton.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (mLockLayout.getVisibility() == View.VISIBLE) {
                    if (mLockState) {
                        mLockButton.setImageResource(R.drawable.browser_video_unlocked);
                        setLockState(false);
                    } else {
                        mLockButton.setImageResource(R.drawable.browser_video_locked);
                        setLockState(true);
                    }
                    displayControl(true);
                    startThread(DELAY_TIME);
                }
            }
        });
//
//        mPlayButton = mToolLayer.findViewById(R.id.video_play_button);
//        mPlayButton.setOnClickListener(new NoDoubleClickListener() {
//            @Override
//            public void onNoDoubleClick(View view) {
//                if (mIsPaused) {
//                    playVideo();
//                    mPlayButton.setVisibility(View.GONE);
//                } else {
//                    pauseVideo();
//                    mPlayButton.setVisibility(View.VISIBLE);
//                }
//            }
//        });

//        mVolumeBar = mToolLayer.findViewById(R.id.volume_bar);
//        mVolumeBar.setProgress(getCurrentVolumeProgress());
//        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                DeviceControlUtils.setPlayerVolume(mAudioManager, i * mMaxVolume / PROGRESS_MAX);
//                if (i == ZERO) {
//                    mMediaControlVolumeBtn.setImageResource(R.drawable.ic_browser_video_voice_close);
//                } else {
//                    mMediaControlVolumeBtn.setImageResource(R.drawable.ic_browser_video_voice);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                // keep show
//                stopThread();
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                startThread(TIME_PROMPT);
//            }
//        });

        mQuickController = mToolLayer.findViewById(R.id.video_quick_controller);
        mVolumeImage = mToolLayer.findViewById(R.id.video_volume);
        mBrightnessImage = mToolLayer.findViewById(R.id.video_brightness);
        mProgressBar = mToolLayer.findViewById(R.id.common_progressbar);
        mProgressBar.setMax(PROGRESS_MAX);

        mPlayController = mToolLayer.findViewById(R.id.video_play_controller);
        mFastImage = mToolLayer.findViewById(R.id.video_fast_forward);
        mRewindImage = mToolLayer.findViewById(R.id.video_rewind);
        mSeekTime = mToolLayer.findViewById(R.id.seek_time);

//        mMediaControls = mToolLayer.findViewById(R.id.media_controls);
//        mMediaControlPlayBtn = mMediaControls.findViewById(R.id.media_controls_play_state);

//        mMediaControlPlayBtn.setOnClickListener(new NoDoubleClickListener() {
//            @Override
//            public void onNoDoubleClick(View view) {
//                if (mIsPaused) {
//                    playVideo();
//                    mPlayButton.setVisibility(View.GONE);
//                } else {
//                    pauseVideo();
//                    mPlayButton.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//        mMediaControlCurrentDuration = mMediaControls.findViewById(R.id.media_controls_current_duration);
//        mMediaControlProgressBar = mMediaControls.findViewById(R.id.media_controls_seek_bar);
//        mMediaControlProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                mMediaControlProgress = i;
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                stopThread();
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                startThread(DELAY_TIME);
//                mMediaControlProgressBar.setProgress(mMediaControlProgress);
//                int seconds = mMediaControlProgress * mDuration / 100;
//                mMediaControlCurrentDuration.setText(convertToText(seconds));
//                seekToTime(seconds);
//
//            }
//        });

//        mMediaControlTotalDuration = mMediaControls.findViewById(R.id.media_controls_total_duration);
//        mMediaControlVolumeBtn = mMediaControls.findViewById(R.id.media_controls_volume);
//        mMediaControlVolumeBtn.setOnClickListener(new NoDoubleClickListener() {
//            @Override
//            public void onNoDoubleClick(View view) {
//                if (mVolumeBar.getVisibility() == View.VISIBLE) {
//                    mVolumeBar.setVisibility(View.GONE);
//                } else {
//                    mVolumeBar.setVisibility(View.VISIBLE);
//                }
//
//            }
//        });

//        displaySignalStrength();
        setAlpha(ALPHA_TRANSPARENT);
    }

//    @Override
    public boolean onTouch(MotionEvent event) {
//        // not response this region
//        if (mScreenWidth == 0) {
            mScreenWidth = DisplayUtil.getScreenWidth(mContext);
//        }
//        if (mScreenHeight == 0) {
            mScreenHeight = DisplayUtil.getScreenHeight(mContext);
//        }
//
        if (!validTouchRegion(event)) {
            return true;
        }

//        if (expandSeekBarTouch(event)) {
//            return false;
//        }
        return false;
    }

    boolean isForbidGesture = false;
    int mTopHeight;
    @Override
    public boolean onDown(MotionEvent e) {
        mVolumeProgress = getCurrentVolumeProgress();
        mBrightnessProgress = getCurrentBrightnessProgress();
        if(e.getY()<mTopHeight) {
            isForbidGesture = true;
        }
        return false;
    }

    @Override
    public void onUp(MotionEvent event) {
        isForbidGesture = false;
        hideProgressBar();
        if (mScrollState
                && (mTouchType == TOUCH_OPERATION_TYPE.FAST_FORWARD
                || mTouchType == TOUCH_OPERATION_TYPE.REWIND) && mSeconds != 0) {
            seekPlayTime(mSeconds);
        }
        mScrollState = false;
        mSeconds = 0;
    }

    @Override
    public void onMove(MotionEvent event) {

    }

    @Override
    public void onDoubleTap(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        return super.onScroll(e1, e2, distanceX, distanceY);
//        if (mLockState) {
//            return false;
//        }
//        stopThread();
//        displayControl(false);
        if(!isForbidGesture) {
            activateProgressControl(e1, e2, getOperationOfTouch(e1, e2));
        }
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
//        return super.onSingleTapUp(e);
//        getMediaDuration();
//        if (mLockLayout.getVisibility() == View.VISIBLE
//                || mTitleBar.getVisibility() == View.VISIBLE) {
//            startThread(TIME_PROMPT);
//        } else {
            displayControl(true);
            startThread(DELAY_TIME);
//        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
//        super.onLongPress(e);
    }

    class TimerThread implements Runnable {
        @Override
        public void run() {
//            if (mLockLayout.getVisibility() == View.VISIBLE
//                    || mTitleBar.getVisibility() == View.VISIBLE) {
                displayControl(false);
//            } else {
//                displayControl(true);
//            }
            mThreadState = false;
        }
    }

    @Override
    public boolean dispatchKey(int code, KeyEvent event) {
        int alpha = 0;
        switch (code) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                alpha = 1;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (alpha == 0) {
                    alpha = -1;
                }
                mVolumeProgress = getCurrentVolumeProgress();
                handleVolume(alpha * PROGRESS_STEP);

                // Hide volume and it's value bar.
                if (mHideVolumeThread == null) {
                    mHideVolumeThread = new Runnable() {
                        @Override
                        public void run() {
                            mQuickController.setVisibility(View.GONE);
                        }
                    };
                } else if (mHandler != null) {
                    mHandler.removeCallbacks(mHideVolumeThread);
                    mHandler.postDelayed(mHideVolumeThread, 1000); // Delay hide volume by 1000ms.
                }
                return true;
            default:
                break;
        }
        return false;
    }

    // Implement NetworkStateChangedListener
    @Override
    public void onNetworkStateChanged() {
        switch (NetworkUtils.getNetworkType()) {
            case ConnectivityManager.TYPE_MOBILE:
//                handleNetworkChange();
                break;
            default:
                break;
        }
    }

    private void startThread(int time) {
        stopThread();
        mHandler.postDelayed(mThread, time);
        mThreadState = true;
    }

    private void stopThread() {
        if (mThreadState) {
            mHandler.removeCallbacks(mThread);
            mThreadState = false;
        }
    }

    private void setAlpha(int alpha) {
        mToolLayer.getBackground().setAlpha(alpha);
        mLockButton.getBackground().setAlpha(alpha);
    }

    private void hideProgressBar() {
        mQuickController.setVisibility(View.GONE);
        mPlayController.setVisibility(View.GONE);
        mBrightnessImage.setVisibility(View.GONE);
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
    private synchronized void activateProgressControl(MotionEvent e1, MotionEvent e2, TOUCH_OPERATION_TYPE type) {
        int progress;
        switch (type) {
            case VOLUME:
                // change volume
                handleVolume((int)((e1.getY() - e2.getY()) / PROGRESS_STEP));
                break;
            case BRIGHTNESS:
                // change brightness
//                try {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (Settings.System.canWrite(mContext)) {
//                            handleBrightness((int) ((e1.getY() - e2.getY()) / PROGRESS_STEP));
//                        } else {
//                            // 动态请求WRITE_SETTINGS权限
//                            if (!Settings.System.canWrite(mContext)) {
//                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
//                                mContext.startActivity(intent);
//                            }
//                        }
//                    } else {
//                        handleBrightness((int) ((e1.getY() - e2.getY()) / PROGRESS_STEP));
//                    }
//                }catch (Exception e){
//
//                }
                handleBrightness((int) ((e1.getY() - e2.getY()) / PROGRESS_STEP));
                break;
            case FAST_FORWARD:
                mPlayController.setVisibility(mHasDuration?View.VISIBLE:View.GONE);
                mFastImage.setVisibility(View.VISIBLE);
                mRewindImage.setVisibility(View.GONE);
                mSeconds = (int) ((e2.getX() - e1.getX()) / PROGRESS_STEP);
                getCurrentPlayTime();
//                mSeekTime.setText(convertToText(mCurrentPlayTime + mSeconds) + "/" + convertToText(mDuration));
                mSeekTime.setText(convertToText(mCurrentPlayTime + mSeconds));
                break;
            case REWIND:
                mPlayController.setVisibility(mHasDuration?View.VISIBLE:View.GONE);
                mRewindImage.setVisibility(View.VISIBLE);
                mFastImage.setVisibility(View.GONE);
                mSeconds = (int) ((e2.getX() - e1.getX()) / PROGRESS_STEP);
                getCurrentPlayTime();
//                mSeekTime.setText(convertToText(mCurrentPlayTime + mSeconds) + "/" + convertToText(mDuration));
                mSeekTime.setText(convertToText(mCurrentPlayTime + mSeconds));
                break;
            case NONE:
                break;
        }
    }

    private int getCurrentVolumeProgress() {
        int volume = DeviceControlUtils.getCurrentVolume(mAudioManager);
        return volume * PROGRESS_MAX / mMaxVolume;
    }

    private void handleVolume(int progress) {
        mQuickController.setVisibility(View.VISIBLE);
        mBrightnessImage.setVisibility(View.GONE);

        progress += mVolumeProgress;
        if (progress > PROGRESS_MAX) {
            progress = PROGRESS_MAX;
        } else if (progress <= PROGRESS_ZERO) {
            progress = PROGRESS_ZERO;
            mVolumeImage.setImageResource(R.drawable.browser_video_voice_close);
        }
        if (progress > PROGRESS_ZERO) {
            mVolumeImage.setImageResource(R.drawable.browser_video_voice);
        }
        mProgressBar.setProgress(progress);
        mVolumeImage.setVisibility(View.VISIBLE);
        int maxVolume = DeviceControlUtils.getMaxVolume(mAudioManager);
        DeviceControlUtils.setPlayerVolume(mAudioManager, progress * maxVolume / PROGRESS_MAX);
    }

    private void restoreBrightness(){
        try {
            mBrightnessProgress = getCurrentBrightnessProgress();
            mProgressBar.setProgress(mBrightnessProgress);
            sBrightnessProgress = mBrightnessProgress * BRIGHTNESS_MAX / (float) PROGRESS_MAX;
            DeviceControlUtils.setBrightness(mContext, sBrightnessProgress);
        } catch (Exception e) {

        }
    }

    private void handleBrightness(int progress) {
        mQuickController.setVisibility(View.VISIBLE);
        mVolumeImage.setVisibility(View.GONE);

        progress += mBrightnessProgress;
        if (progress > PROGRESS_MAX) {
            progress = PROGRESS_MAX;
        } else if (progress < PROGRESS_MIN) {
            progress = PROGRESS_MIN;
        }
        mProgressBar.setProgress(progress);
        mBrightnessImage.setVisibility(View.VISIBLE);
        sBrightnessProgress = progress * BRIGHTNESS_MAX / (float) PROGRESS_MAX;
        DeviceControlUtils.setBrightness(mContext, sBrightnessProgress);
    }

    private static float sBrightnessProgress = -1;
    private static boolean sIsFirstFullScreen = true;
    private int getCurrentBrightnessProgress() {
        float brightness;
        if (sBrightnessProgress >= 0) {
            brightness = sBrightnessProgress;
        } else {
            brightness = DeviceControlUtils.getCurrentBrightness(mContext);
        }
        return (int) (brightness * PROGRESS_MAX / BRIGHTNESS_MAX);
    }

    private void invokeJsMethod(String js) {
        WebView webview = mBaseUi.getBrowserWebView();
        if (webview != null) {
            webview.loadUrl(js);
        }
    }

    // Invoke the javascript function
    private void registerListener() {
        invokeJsMethod("javascript:__byfrost_browser__.registerListener()");
    }

    private void unregisterListener() {
        invokeJsMethod("javascript:__byfrost_browser__.unregisterListener()");
    }

    private void seekPlayTime(int seconds) {
        invokeJsMethod("javascript:__byfrost_browser__.seek(" + seconds + ")");
    }

    private void seekToTime(int seconds) {
        invokeJsMethod("javascript:__byfrost_browser__.seekTo(" + seconds + ")");
    }

    private void getCurrentPlayTime() {
        invokeJsMethod("javascript:__byfrost_browser__.getCurrentPlayTime()");
    }

    private void getMediaDuration() {
        invokeJsMethod("javascript:__byfrost_browser__.getMediaDuration()");
    }

    private void checkVideoObject() {
        invokeJsMethod("javascript:__byfrost_browser__.retrieveVideo()");
    }

    private void getVideoUrl() {
        invokeJsMethod("javascript:__byfrost_browser__.getVideoUrl()");
    }

    private void pauseVideo() {
        invokeJsMethod("javascript:__byfrost_browser__.pause()");
    }

    private void playVideo() {
        invokeJsMethod("javascript:__byfrost_browser__.play()");
    }

    private void isPaused() {
        invokeJsMethod("javascript:__byfrost_browser__.isPaused()");
    }

    // hide the web media controls
    private void removeWebMediaControls() {
        invokeJsMethod("javascript:__byfrost_media_hack__.replaceVideoShadowStyle()");
    }

    // recover the web media controls
    private void resetWebMediaControls() {
        invokeJsMethod("javascript:__byfrost_media_hack__.resetVideoShadowStyle()");
    }
    // Invoke the javascript function

    // WebviewVideoPlayerLayer.MediaInfoListener
    boolean mHasDuration;
    @Override
    public void getMediaDuration(String duration) {
        if(StringUtils.isEmpty(duration)){
            mHasDuration = false;
            return;
        }
        mHasDuration = true;
        mDuration = (int) Float.parseFloat(duration);
        sendMessage(UPDATE_TOTAL_DURATION, convertToText(mDuration));
    }

    @Override
    public void getVideoErrorInfo(String info) {
    }

    private boolean mCanDownloadVideo = false;

    @Override
    public void getVideoUrl(String url,String src) {
        try {
            if(mCanDownloadVideo){
                return;
            }
            if(TextUtils.isEmpty(src) && TextUtils.isEmpty(url)){
                return;
            }
            if(url.startsWith("blob:")){
                // TODO: 2024/10/21 解析blob数据
                return;
            }
            handleVideoInfo(src);
            if (mVideoInfo == null) {
                handleVideoInfo(url);
            }

            if (mVideoInfo == null) {
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadVideo.setVisibility(View.VISIBLE);
                    mCanDownloadVideo = true;
                }
            });
        } catch (Exception e) {
        }
    }

    private void handleVideoInfo(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        VideoInfo videoInfo = new VideoInfo();
        try {
            URL videourl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) videourl.openConnection();
            urlConnection.setRequestMethod("HEAD");
            urlConnection.setRequestProperty("User-Agent", mUserAgent);
            urlConnection.connect();
            videoInfo.mMimeType = urlConnection.getContentType();
            if (TextUtils.isEmpty(videoInfo.mMimeType) || !videoInfo.mMimeType.startsWith("video/")) {
                return;
            }
            videoInfo.mContentLength = urlConnection.getContentLength();
            videoInfo.mUserAgent = mUserAgent;
            videoInfo.mVideoUrl = url;
            mVideoInfo = videoInfo;
        } catch (Exception e) {

        }
    }

    @Override
    public void getCurrentPlayTime(String time) {
        mCurrentPlayTime = (int) Float.parseFloat(time);
        sendMessage(UPDATE_CURRENT_DURATION, convertToText(mCurrentPlayTime));
    }

    @Override
    public void canControlVideoPlay(boolean success) {
        mCanControlVideo = success;
    }

    @Override
    public void isPaused(boolean isPaused) {
        mIsPaused = isPaused;
        sendMessage(UPDATE_PLAY_BUTTON, !isPaused);
    }

    @Override
    public void initMediaControlsProgressBar() {
        sendMessage(INIT_MEDIA_CONTROLS, null);
    }

    @Override
    public void played() {
        sendMessage(UPDATE_PLAY_BUTTON, true);
        initVideoInfo();

//        mPlayButton.setVisibility(View.GONE);
    }

    private void initVideoInfo(){
        mVideoInfo = null;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    getVideoUrl();
                }catch (Exception e){
                }
            }
        });
    }

    @Override
    public void paused() {
        // TODO: 2024/10/14 广告
        sendMessage(UPDATE_PLAY_BUTTON, false);
//        mPlayButton.setVisibility(View.VISIBLE);
    }
    // WebviewVideoPlayerLayer.MediaInfoListener

    private String convertToText(int sec) {
        if (sec < ZERO) {
            sec = ZERO;
        } else if (sec > mDuration) {
            sec = mDuration;
        }

        String time = "";
        int hours = sec / HOUR_SECOND_SCALE;
        if (hours < DECIMAL) {
            time = "0";
        }
        time = time + hours + ":";

        int minutes = (sec % HOUR_SECOND_SCALE) / TIME_SCALE;
        if (minutes < DECIMAL) {
            time = time + "0";
        }

        time = time + minutes + ":";
        int second = sec % TIME_SCALE;
        if (second < DECIMAL) {
            time = time + "0";
        }

        return (time + second);
    }

    public void setListener (WebviewVideoPlayerLayer.Listener listener) {
        mListener = listener;
    }

    private void setLockState (boolean state) {
        mLockState = state;

        if (mListener != null) {
            mListener.setLockerState(state);
        }
    }

    // This is for android 4.4, the video surface view may cover this layer default
    private void keepOnTop(View view) {
        if ((view instanceof ViewGroup)) {
            int num = ((ViewGroup) view).getChildCount();
            int i = 0;
            while (i < num) {
                View child = ((ViewGroup) view).getChildAt(i);
                if ((child instanceof SurfaceView)) {
                    ((SurfaceView) child).setZOrderOnTop(false);
                } else if ((child instanceof ViewGroup)) {
                    keepOnTop(child);
                }
                i++;
            }
        } else if ((view instanceof SurfaceView)) {
            ((SurfaceView) view).setZOrderOnTop(false);
        }
    }

    // Return a appropriate operation type according to MotionEvent
    private TOUCH_OPERATION_TYPE getOperationOfTouch(MotionEvent e1, MotionEvent e2) {
        if ( e1 == null || e2 == null) {
            return NONE;
        }

        float x1 = e1.getX();
        float x2 = e2.getX();
        float y1 = e1.getY();
        float y2 = e2.getY();

        if (mScrollState && (mTouchType == TOUCH_OPERATION_TYPE.VOLUME
                || mTouchType == TOUCH_OPERATION_TYPE.BRIGHTNESS)) {
            return mTouchType;
        }

        float degreeTan = Math.abs(y2 - y1) / Math.abs(x2 - x1);
        // if the angle bigger than 30 degree, we consider the the user want to change volume or brightness
        if (!mScrollState && degreeTan >= 0.58f) {
            // approximate more than 45 degree, at this time we think the operation is valid on the y axis
            mScrollState = true;
            if (x1 >= (mScreenWidth / 2)) {
                mTouchType = VOLUME;
                return mTouchType;
            }
            mTouchType = BRIGHTNESS;
            return mTouchType;
        } else {
            mScrollState = true;
            if (x2 > x1) {
                mTouchType = FAST_FORWARD;
                return FAST_FORWARD;
            }
            mTouchType = REWIND;
            return REWIND;
        }
    }

    private String getVideoTitle() {
        WebView webview = mBaseUi.getBrowserWebView();
        if (webview != null) {
            return webview.getTitle();
        }
        return "";
    }

    private String getTextSystime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        String time = "";
        if (hour < DECIMAL) {
            time = "0";
        }
        time += hour + ":";
        if (minute < DECIMAL) {
            time += "0";
        }
        time += String.valueOf(minute);
        return time;
    }

    private void displayControl(boolean display) {
        if (display) {
            mLockButton.setEnabled(true);
            mLockLayout.setVisibility(View.VISIBLE);
//            lightenNavigationBar(true);
            if (mLockState) {
                setButtonEnable(false);
                mTitleBar.setVisibility(View.GONE);
                mBrowserFunction.setVisibility(View.GONE);
//                mMediaControls.setVisibility(View.GONE);
//                mVolumeBar.setVisibility(View.GONE);
            } else {
                setButtonEnable(true);
                mTitle.setText(getVideoTitle());
                mSysTime.setText(getTextSystime());
                mBattery.setLevelHeight(DeviceControlUtils.getBatteryVoltagePercent(mContext));
//                displaySignalStrength();
                mTitleBar.setVisibility(View.VISIBLE);
                mBrowserFunction.setVisibility(View.VISIBLE);
//                mMediaControls.setVisibility(View.VISIBLE);
                updateMediaControls();
//                mVolumeBar.setProgress(getCurrentVolumeProgress());
//                mVolumeBar.setVisibility(View.VISIBLE);
            }
        } else {
            setButtonEnable(false);
            mLockButton.setEnabled(false);
            mLockLayout.setVisibility(View.GONE);
            mTitleBar.setVisibility(View.GONE);
            mBrowserFunction.setVisibility(View.GONE);
//            mMediaControls.setVisibility(View.GONE);
//            mVolumeBar.setVisibility(View.GONE);
//            lightenNavigationBar(false);
        }
    }

//    private void displaySignalStrength() {
//        if (mInMultiWindow) {
//            return;
//        }
//        switch (NetworkUtils.getNetworkType()) {
//            case ConnectivityManager.TYPE_WIFI:
//            case ConnectivityManager.TYPE_WIMAX:
////                mWifiLayout.setVisibility(View.VISIBLE);
////                mWifi.setValue(NetworkUtils.getWifiStrengthPercent());
////                mSignalLayout.setVisibility(View.GONE);
//                break;
//            case ConnectivityManager.TYPE_MOBILE:
//            case ConnectivityManager.TYPE_MOBILE_DUN:
////                mSignalLayout.setVisibility(View.VISIBLE);
////                mSignalView.setValue(mSignalPercent);
////                mWifiLayout.setVisibility(View.GONE);
//                break;
//            default:
////                mWifi.setValue(ZERO);
////                mWifiLayout.setVisibility(View.VISIBLE);
////                mSignalLayout.setVisibility(View.GONE);
//                break;
//        }
//    }

//    public void lightenNavigationBar(boolean lighten) {
//        if (mContext == null) {
//            return;
//        }
//        int systemUi = mContext.getWindow().getDecorView().getSystemUiVisibility();
//        if (lighten) {
//            mContext.getWindow().getDecorView().setSystemUiVisibility(systemUi & ~View.SYSTEM_UI_FLAG_LOW_PROFILE);
//        } else {
//            mContext.getWindow().getDecorView().setSystemUiVisibility(systemUi | View.SYSTEM_UI_FLAG_LOW_PROFILE);
//        }
//    }

    // WebviewVideoPlayerLayer
    @Override
    public void setPreView(View view) {
        keepOnTop(view);
    }

    @Override
    public View getLayer() {
        return mToolLayer;
    }

    @Override
    public void beginFullScreen() {
        if(!sIsFirstFullScreen) {
//            restoreBrightness();
//            mBrightnessProgress = getCurrentBrightnessProgress();
        }
        sIsFirstFullScreen = false;
        checkVideoObject();
        initVideoInfo();
        registerListener();
        isPaused();
        updateMediaControls();
    }

    @Override
    public void endFullScreen() {
        if ((mTelephonyManager != null) && (mPhoneListener != null)) {
            mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
        }

        if (mContext != null) {
            DisplayUtil.setScreenBrightness(mContext, mSavedBrightness);
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

//        if (mGestureDetector != null) {
//            mGestureDetector = null;
//        }

        pauseVideo();
        resetWebMediaControls();
        unregisterListener();
    }

    @Override
    public void multiWindow() {
        if (isMultiWindow(mContext)) {
            // only show title and share button in the title bar
//            mSignalLayout.setVisibility(View.GONE);
//            mWifiLayout.setVisibility(View.GONE);
            mBatteryLayout.setVisibility(View.GONE);
            mSysTime.setVisibility(View.GONE);
            mTitleBar.setVerticalGravity(Gravity.BOTTOM);
            DisplayMetrics metric = DisplayUtil.getDisplayMetrics(mContext);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mShare.getLayoutParams();
            lp.setMargins(lp.leftMargin, lp.topMargin, (int) (12 * metric.density), lp.bottomMargin);
            mShare.setLayoutParams(lp);
        }
    }

    protected boolean isMultiWindow(Context context) {
       // This is a approximate method to judge if it is in multiple window state
        // when the version of build tools is less than 24
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        float height = display.heightPixels;
        float width = (float) (1.2 * display.widthPixels);
        mInMultiWindow = height < width;
        return mInMultiWindow;
    }

    protected void updateMediaControls() {
//        if (getCurrentVolumeProgress() == 0) {
//            mMediaControlVolumeBtn.setImageResource(R.drawable.ic_browser_video_voice_close);
//        } else {
//            mMediaControlVolumeBtn.setImageResource(R.drawable.ic_browser_video_voice);
//        }
        getMediaDuration();
        getCurrentPlayTime();
//        mMediaControlCurrentDuration.setText(convertToText(mCurrentPlayTime));
//        mMediaControlTotalDuration.setText(convertToText(mDuration));
//        if (mDuration != 0) {
//            mMediaControlProgressBar.setProgress(100 * mCurrentPlayTime / mDuration);
//        }
    }

    private void sendMessage(int what, Object data) {
        Message msg = Message.obtain(mHandler, what, data);
        msg.sendToTarget();
    }

    //judge the touch region valid or not
    private boolean validTouchRegion(MotionEvent motion) {
//         not activate on the title bar and media control bar
        return !(motion.getY() <= mTitleBar.getHeight()) && !(motion.getY() >= (mScreenHeight - ScreenUtils.dpToPxInt(mContext,60)));
    }

    private boolean expandSeekBarTouch(MotionEvent event) {
         // handle by volume bar
//        if (mVolumeBar.getVisibility() == View.VISIBLE) {
//            Rect volumeBarRect = new Rect();
//            mVolumeBar.getHitRect(volumeBarRect);
//            if (event.getX() >= (volumeBarRect.left - volumeBarRect.width())
//                    && event.getX() <= (volumeBarRect.right + volumeBarRect.width())
//                    && event.getY() >= volumeBarRect.top
//                    && event.getY() <= volumeBarRect.bottom) {
//                MotionEvent newEvent = MotionEvent.obtain(event.getDownTime(), event.getEventTime(),
//                        event.getAction(), volumeBarRect.left + volumeBarRect.width() / 2,
//                        event.getY(), event.getMetaState());
//                mVolumeBar.onTouchEvent(newEvent);
//                return true;
//            }
//        }
        return false;
    }

    private void handleNetworkChange() {
        // pause video first, when the network connection changes to mobile
        pauseVideo();
        // popup a window to prompt user
        BrowserDialog dialog = new BrowserDialog(mContext) {
            @Override
            public void onPositiveButtonClick() {
                super.onPositiveButtonClick();
                playVideo();
            }

            @Override
            public void onNegativeButtonClick() {
                super.onNegativeButtonClick();
            }
        };
        dialog.setBrowserMessage(R.string.video_mobile_permission);
        dialog.setBrowserPositiveButton(R.string.video_mobile_play);
        dialog.setBrowserNegativeButton(R.string.video_mobile_pause);
        dialog.show();
    }

    private void setButtonEnable(boolean enabled) {
        mBack.setEnabled(enabled);
        mShare.setEnabled(enabled);
//        mMediaControlPlayBtn.setEnabled(enabled);
//        mMediaControlVolumeBtn.setEnabled(enabled);
    }

    abstract class NoDoubleClickListener implements View.OnClickListener {
        protected  static final int MIN_CLICK_TIME = 200;
        private long lastClickTime = 0;

        abstract void onNoDoubleClick(View view);

        @Override
        public void onClick(View view) {
            long current = SystemClock.uptimeMillis();
            if (current - lastClickTime > MIN_CLICK_TIME) {
                lastClickTime = current;
                onNoDoubleClick(view);
            }
        }
    }
}
