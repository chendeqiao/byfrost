package com.intelligence.browser.ui.media;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.downloads.BrowserNetworkStateNotifier;
import com.intelligence.browser.ui.webview.WebviewVideoPlayerLayer;
import com.intelligence.browser.utils.DeviceControlUtils;
import com.intelligence.browser.ui.widget.BrowserDialog;
import com.intelligence.commonlib.tools.DisplayUtil;
import com.intelligence.commonlib.tools.NetworkUtils;
import com.intelligence.commonlib.tools.ScreenUtils;

import java.util.Calendar;

public class FullWebviewVideoToolLayer implements View.OnTouchListener
        , WebviewVideoPlayerLayer
        , WebviewVideoPlayerLayer.MediaInfoListener
        , BrowserNetworkStateNotifier.NetworkStateChangedListener {
    // static variable
    private static final int DELAY_TIME = 4000;
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

    private RelativeLayout mToolLayer; // The layout over the fullscreen webview
    private ImageButton mPlayButton;

    private ImageView mVolumeImage;
    private ImageView mBrightnessImage;
    private ProgressBar mProgressBar;
    private LinearLayout mQuickController;

    private ImageView mFastImage;
    private ImageView mRewindImage;
    private TextView mSeekTime;
    private RelativeLayout mPlayController;

    // volume bar, a idiotic design
//    private SeekBar mVolumeBar;

    //TitleBar
    private RelativeLayout mTitleBar;
    private ImageView mBack;
    private TextView  mTitle;
    private TextView  mSysTime;
    private RelativeLayout mMediaControls;
    private ImageView mMediaControlPlayBtn;
    private TextView mMediaControlCurrentDuration;
    private SeekBar mMediaControlProgressBar;
    private TextView mMediaControlTotalDuration;
    private ImageView mMediaControlVolumeBtn;


    private GestureDetector mGestureDetector;
    private Activity mContext;

    // Device control
    private AudioManager mAudioManager;

    private WebviewVideoPlayerLayer.Listener mListener;

    private int mScreenWidth;
    private int mScreenHeight;
//    private boolean mLockState = false;
    private boolean mThreadState = false;
    private boolean mScrollState = false;
    private boolean mCanControlVideo = false;
    private TOUCH_OPERATION_TYPE mTouchType = TOUCH_OPERATION_TYPE.NONE;
    private int mMaxVolume;
    private int mVolumeProgress;
    private int mBrightnessProgress;
    private int mDuration;
    private int mCurrentPlayTime;
    private int mSeconds;
    private float mSavedBrightness;
    private int mMediaControlProgress;

    private Runnable mThread;
    private Runnable mHideVolumeThread;
    private Runnable mOnTouchUpThread;
    private Handler mHandler;

    private boolean mInMultiWindow;

    private int mTopHeight;
    private String mVideTitle;
    private BrowserVideoView mVideoView;
    private VideoPlayListener mVideoPlayListener;
    public FullWebviewVideoToolLayer(Activity context, BrowserVideoView videoView, String title, VideoPlayListener videoPlayListener) {
        mVideTitle = title;
        mVideoView = videoView;
        mContext = context;
        mVideoPlayListener = videoPlayListener;
        mTopHeight = (int)ScreenUtils.dpToPx(mContext, 60);
        initDeviceControl(mContext);
        initOverlayer(mContext);
        initEventHandle(mContext);
        removeWebMediaControls();
    }

    private void initEventHandle(Context context) {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_PLAY_BUTTON:
                        if ((boolean) msg.obj) {
                            mMediaControlPlayBtn.setImageResource(R.drawable.browser_video_pause);
                        } else {
                            mMediaControlPlayBtn.setImageResource(R.drawable.browser_video_play_icon);
                        }
                        break;
                    case UPDATE_TOTAL_DURATION:
                        mMediaControlTotalDuration.setText((String) msg.obj);
                        break;
                    case UPDATE_CURRENT_DURATION:
                        mMediaControlCurrentDuration.setText((String) msg.obj);
                        if (mDuration > 0) {
                            mMediaControlProgressBar.setProgress(100 * mCurrentPlayTime / mDuration);
                        }

                        break;
                    case INIT_MEDIA_CONTROLS:
                        break;
                    default:
                        break;
                }
            }
        };
        mThread = new TimerThread();
        mOnTouchUpThread = new OnTouchUpThread();
        mHideVolumeThread = new OnTouchUpThread();
        mGestureDetector = new GestureDetector(context, new FullScreenGestureListener(this));
        startThread(DELAY_TIME);
    }

    private void initDeviceControl(Activity context) {
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideoView.start();
                if(mVideoPlayListener != null){
                    mVideoPlayListener.onStartPlay();
                }
                beginFullScreen();
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO: 2024/10/14 全屏广告
                if(mVideoPlayListener != null){
                    mVideoPlayListener.onPlayFinish();
                    mPlayButton.setVisibility(View.VISIBLE);
                    displayControl(true);
                }
            }
        });
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSavedBrightness = DisplayUtil.getScreenBrightness(mContext);
        mMaxVolume = DeviceControlUtils.getMaxVolume(mAudioManager);
    }

    private void initOverlayer(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.browser_fullvideo_overlayer, null);

        // Get all views
        mToolLayer = (RelativeLayout) view.findViewById(R.id.video_overlay);
        mToolLayer.setOnTouchListener(this);

        mTitleBar = (RelativeLayout) mToolLayer.findViewById(R.id.video_title_bar);
        mTitleBar.setBackgroundColor(context.getResources().getColor(R.color.video_title_bar_background));
        mTitleBar.getBackground().setAlpha(TITLE_BAR_ALPHA);
        mBack = (ImageView) mToolLayer.findViewById(R.id.video_back);
        mBack.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (mBack.getVisibility() == View.VISIBLE) {
                    if(mVideoPlayListener != null){
                        mVideoPlayListener.onBack();
                    }
                }
            }
        });

        mTitle = (TextView) mToolLayer.findViewById(R.id.video_title);
        mTitle.setText(getVideoTitle());
        mSysTime = (TextView) mToolLayer.findViewById(R.id.video_sys_time);
        mSysTime.setText(getTextSystime());

        mPlayButton = (ImageButton) mToolLayer.findViewById(R.id.video_play_button);
        mPlayButton.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (isPaused()) {
                    playVideo();
                    sendMessage(UPDATE_PLAY_BUTTON, true);
                    mPlayButton.setVisibility(View.GONE);
                } else {
                    pauseVideo();
                    sendMessage(UPDATE_PLAY_BUTTON, false);
                    mPlayButton.setVisibility(View.VISIBLE);
                }
                updateMediaControls();
            }
        });

        mQuickController = (LinearLayout) mToolLayer.findViewById(R.id.video_quick_controller);
        mVolumeImage = (ImageView) mToolLayer.findViewById(R.id.video_volume);
        mBrightnessImage = (ImageView) mToolLayer.findViewById(R.id.video_brightness);
        mProgressBar = (ProgressBar) mToolLayer.findViewById(R.id.common_progressbar);
        mProgressBar.setMax(PROGRESS_MAX);

        mPlayController = (RelativeLayout) mToolLayer.findViewById(R.id.video_play_controller);
        mFastImage = (ImageView) mToolLayer.findViewById(R.id.video_fast_forward);
        mRewindImage = (ImageView) mToolLayer.findViewById(R.id.video_rewind);
        mSeekTime = (TextView) mToolLayer.findViewById(R.id.seek_time);

        mMediaControls = (RelativeLayout) mToolLayer.findViewById(R.id.media_controls);
        mMediaControlPlayBtn = (ImageView) mMediaControls.findViewById(R.id.media_controls_play_state);

        mMediaControlPlayBtn.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {
                if (isPaused()) {
                    playVideo();
                    mPlayButton.setVisibility(View.GONE);
                    sendMessage(UPDATE_PLAY_BUTTON, true);
                } else {
                    pauseVideo();
                    mPlayButton.setVisibility(View.VISIBLE);
                    sendMessage(UPDATE_PLAY_BUTTON, false);
                }
                updateMediaControls();
            }
        });
        mMediaControlCurrentDuration = (TextView) mMediaControls.findViewById(R.id.media_controls_current_duration);
        mMediaControlProgressBar = (SeekBar) mMediaControls.findViewById(R.id.media_controls_seek_bar);
        mMediaControlProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mMediaControlProgress = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopThread();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startThread(DELAY_TIME);
                mMediaControlProgressBar.setProgress(mMediaControlProgress);
                int seconds = mMediaControlProgress * mDuration / 100;
                mMediaControlCurrentDuration.setText(convertToText(seconds));
                seekToTime(seconds);

            }
        });

        mMediaControlTotalDuration = (TextView) mMediaControls.findViewById(R.id.media_controls_total_duration);
        mMediaControlVolumeBtn = (ImageView) mMediaControls.findViewById(R.id.media_controls_volume);
        mMediaControlVolumeBtn.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View view) {

            }
        });

        mBrowserFunction = mToolLayer.findViewById(R.id.browser_full_function);
        mChangeScreenView = mToolLayer.findViewById(R.id.full_change_screen);
        mChangeScreenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(mVideoPlayListener != null){
                   mVideoPlayListener.setRequestedOrientation();
               }
            }
        });

        setAlpha(ALPHA_TRANSPARENT);
    }

    private ImageView mChangeScreenView;
    private ImageView mAddBookmarks;
    private ViewGroup mBrowserFunction;


    boolean isForbidGesture = false;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // not response this region
//        if (mScreenWidth == 0) {

            mScreenWidth = DisplayUtil.getScreenWidth(mContext);
//        }
//        if (mScreenHeight == 0) {
            mScreenHeight = DisplayUtil.getScreenHeight(mContext);
//        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if(event.getY()<mTopHeight) {
                isForbidGesture = true;
            }
        }
        if (!validTouchRegion(event)) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mVolumeProgress = getCurrentVolumeProgress();
            mBrightnessProgress = getCurrentBrightnessProgress();

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            isForbidGesture = false;
            hideProgressBar();
            if ((mTouchType == TOUCH_OPERATION_TYPE.FAST_FORWARD
                    || mTouchType == TOUCH_OPERATION_TYPE.REWIND) && mSeconds != 0) {
                seekPlayTime(mSeconds);
            }
            mScrollState = false;
            mHandler.removeCallbacks(mOnTouchUpThread);
            mHandler.postDelayed(mOnTouchUpThread,DELAY_TIME);
            mSeconds = 0;
        }

        if(!isForbidGesture) {
            mGestureDetector.onTouchEvent(event);
        }
        return true;
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

    public interface VideoPlayListener{
        void onStartPlay();
        void onPlayFinish();
        void setRequestedOrientation();
        void onBack();
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
    }

    private void hideProgressBar() {
        mQuickController.setVisibility(View.GONE);
        mPlayController.setVisibility(View.GONE);
        mBrightnessImage.setVisibility(View.GONE);
    }

    private synchronized void activateProgressControl(MotionEvent e1, MotionEvent e2, TOUCH_OPERATION_TYPE type) {
        int progress;
        switch (type) {
            case VOLUME:
                // change volume
                handleVolume((int)((e1.getY() - e2.getY()) / PROGRESS_STEP));
                break;
            case BRIGHTNESS:
                // change brightness
                handleBrightness((int) ((e1.getY() - e2.getY())/ PROGRESS_STEP));
                break;

            case FAST_FORWARD:
                mPlayController.setVisibility(View.VISIBLE);
                mFastImage.setVisibility(View.VISIBLE);
                mRewindImage.setVisibility(View.GONE);
                mSeconds = (int) ((e2.getX() - e1.getX()) / PROGRESS_STEP);
                getCurrentPlayTime();
                mSeekTime.setText(convertToText(mCurrentPlayTime + mSeconds) + "/" + convertToText(mDuration));
                break;
            case REWIND:
                mPlayController.setVisibility(View.VISIBLE);
                mRewindImage.setVisibility(View.VISIBLE);
                mFastImage.setVisibility(View.GONE);
                mSeconds = (int) ((e2.getX() - e1.getX()) / PROGRESS_STEP);
                getCurrentPlayTime();
                mSeekTime.setText(convertToText(mCurrentPlayTime + mSeconds) + "/" + convertToText(mDuration));
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
        DeviceControlUtils.setBrightness(mContext, progress * BRIGHTNESS_MAX / (float) PROGRESS_MAX);
    }

    private int getCurrentBrightnessProgress() {
        float brightness = DeviceControlUtils.getCurrentBrightness(mContext);
        return (int) (brightness * PROGRESS_MAX / BRIGHTNESS_MAX);
    }

    private void seekPlayTime(int seconds) {
        mVideoView.seekTo((seconds * 1000) + mVideoView.getCurrentPosition());
        mVideoView.start();
        mPlayButton.setVisibility(View.GONE);
    }

    private void seekToTime(int seconds) {
        mVideoView.seekTo(seconds * 1000);
        mVideoView.start();
    }

    private int getCurrentPlayTime() {
        getMediaDuration();
        mCurrentPlayTime = mVideoView.getCurrentPosition()/1000;
        return mCurrentPlayTime;
    }

    private int getMediaDuration() {
        mDuration =  mVideoView.getDuration()/1000;
        return mDuration;
    }

    private void checkVideoObject() {
    }

    private void pauseVideo() {
        mVideoView.pause();
    }

    private void playVideo() {
        mVideoView.start();
    }

    private boolean isPaused() {
         return  !mVideoView.isPlaying();
    }

    // hide the web media controls
    private void removeWebMediaControls() {
    }

    // recover the web media controls
    private void resetWebMediaControls() {
    }
    // Invoke the javascript function

    // WebviewVideoPlayerLayer.MediaInfoListener
    @Override
    public void getMediaDuration(String duration) {
        mDuration = (int) Float.parseFloat(duration);
        sendMessage(UPDATE_TOTAL_DURATION, convertToText(mDuration));
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
        sendMessage(UPDATE_PLAY_BUTTON, !isPaused);
    }

    @Override
    public void initMediaControlsProgressBar() {
        sendMessage(INIT_MEDIA_CONTROLS, null);
    }

    @Override
    public void played() {
        sendMessage(UPDATE_PLAY_BUTTON, true);
        mPlayButton.setVisibility(View.GONE);
    }

    @Override
    public void paused() {
        sendMessage(UPDATE_PLAY_BUTTON, false);
        mPlayButton.setVisibility(View.VISIBLE);
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
//        if (!time.equals("00")) {
            time = time + String.valueOf(hours) + ":";
//        }else {
//            time = "";
//        }

        int minutes = (sec % HOUR_SECOND_SCALE) / TIME_SCALE;
        if (minutes < DECIMAL) {
            time = time + "0";
        }

        time = time + String.valueOf(minutes) + ":";
        int second = sec % TIME_SCALE;
        if (second < DECIMAL) {
            time = time + "0";
        }

        return time + second;
    }

    public void setListener (WebviewVideoPlayerLayer.Listener listener) {
        mListener = listener;
    }

    private void setLockState (boolean state) {
//        if (mListener != null) {
//            mListener.setLockerState(state);
//        }
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
            return TOUCH_OPERATION_TYPE.NONE;
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
                mTouchType = TOUCH_OPERATION_TYPE.VOLUME;
                return mTouchType;
            }
            mTouchType = TOUCH_OPERATION_TYPE.BRIGHTNESS;
            return mTouchType;
        } else {
            mScrollState = true;
            if (x2 > x1) {
                mTouchType = TOUCH_OPERATION_TYPE.FAST_FORWARD;
                return TOUCH_OPERATION_TYPE.FAST_FORWARD;
            }
            mTouchType = TOUCH_OPERATION_TYPE.REWIND;
            return TOUCH_OPERATION_TYPE.REWIND;
        }
    }

    private String getVideoTitle() {
        return mVideTitle;
    }

    private String getTextSystime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        String time = "";
        if (hour < DECIMAL) {
            time = "0";
        }
        time += String.valueOf(hour) + ":";
        if (minute < DECIMAL) {
            time += "0";
        }
        time += String.valueOf(minute);
        return time;
    }

    private boolean isFullScreen() {
        if (mTitleBar.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    private void displayControl(boolean display) {
        if (display) {
            lightenNavigationBar(true);
            setButtonEnable(true);
            mTitle.setText(getVideoTitle());
            mSysTime.setText(getTextSystime());
//                mBattery.setValue(DeviceControlUtils.getBatteryVoltagePercent(mContext));
            mTitleBar.setVisibility(View.VISIBLE);
            mMediaControls.setVisibility(View.VISIBLE);
            mBrowserFunction.setVisibility(View.VISIBLE);
            updateMediaControls();
        } else {
            setButtonEnable(false);
            mTitleBar.setVisibility(View.GONE);
            mMediaControls.setVisibility(View.GONE);
            mBrowserFunction.setVisibility(View.GONE);
            lightenNavigationBar(false);
        }
    }

    public void lightenNavigationBar(boolean lighten) {
        if (mContext == null) {
            return;
        }
        int systemUi = mContext.getWindow().getDecorView().getSystemUiVisibility();
        if (lighten) {
            mContext.getWindow().getDecorView().setSystemUiVisibility(systemUi & ~View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } else {
            mContext.getWindow().getDecorView().setSystemUiVisibility(systemUi | View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

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
        checkVideoObject();
        isPaused();
        updateMediaControls();
        mHandler.post(mThread);
        mHandler.removeCallbacks(mOnTouchUpThread);
        mHandler.postDelayed(mOnTouchUpThread,DELAY_TIME);
    }

    @Override
    public void endFullScreen() {
        if (mContext != null) {
            DisplayUtil.setScreenBrightness(mContext, mSavedBrightness);
        }

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (mGestureDetector != null) {
            mGestureDetector = null;
        }

        pauseVideo();
        resetWebMediaControls();
    }

    @Override
    public void multiWindow() {
        if (isMultiWindow(mContext)) {
            // only show title and share button in the title bar
            mSysTime.setVisibility(View.GONE);
            mTitleBar.setVerticalGravity(Gravity.BOTTOM);
        }
    }

    class FullScreenGestureListener extends GestureDetector.SimpleOnGestureListener {
        FullWebviewVideoToolLayer mOutClass;
        public FullScreenGestureListener(FullWebviewVideoToolLayer outClass) {
            mOutClass = outClass;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            stopThread();
//            displayControl(true);
            mOutClass.activateProgressControl(e1, e2, getOperationOfTouch(e1, e2));
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            getMediaDuration();
//            if (mLockLayout.getVisibility() == View.VISIBLE
//                    || mTitleBar.getVisibility() == View.VISIBLE) {
//                startThread(TIME_PROMPT);
//            } else {

            if(System.currentTimeMillis() - lastClickTime>500) {
                displayControl(!isFullScreen());
            }
            lastClickTime = System.currentTimeMillis();
//                startThread(DELAY_TIME);
//            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Do not do anything
            super.onLongPress(e);
        }
    }

    private long lastClickTime = 0;
    class TimerThread implements Runnable {
        @Override
        public void run() {
//            if (mTitleBar.getVisibility() == View.VISIBLE) {
                updateMediaControls();
                isPaused(isPaused());
                mHandler.postDelayed(mThread,1000);
//            }
        }
    }

    class OnTouchUpThread implements Runnable {
        @Override
        public void run() {
            if (mTitleBar.getVisibility() == View.VISIBLE && !isPaused()) {
                displayControl(false);
//                mPlayButton.setVisibility();
            }
        }
    }

    protected boolean isMultiWindow(Context context) {
        // This is a approximate method to judge if it is in multiple window state
        // when the version of build tools is less than 24
        DisplayMetrics display = context.getResources().getDisplayMetrics();
        float height = display.heightPixels;
        float width = (float) (1.2 * display.widthPixels);
        mInMultiWindow = (height < width) ? true : false;
        return mInMultiWindow;
    }

    protected void updateMediaControls() {
        if (getCurrentVolumeProgress() == 0) {
            mMediaControlVolumeBtn.setImageResource(R.drawable.browser_video_voice_close);
        } else {
            mMediaControlVolumeBtn.setImageResource(R.drawable.browser_video_voice);
        }

        getMediaDuration();
        getCurrentPlayTime();
        mMediaControlCurrentDuration.setText(convertToText(mCurrentPlayTime));
        mMediaControlTotalDuration.setText(convertToText(mDuration));
        if (mDuration != 0) {
            mMediaControlProgressBar.setProgress(100 * mCurrentPlayTime / mDuration);
        }
    }

    private void sendMessage(int what, Object data) {
        Message msg = Message.obtain(mHandler, what, data);
        msg.sendToTarget();
    }

    //judge the touch region valid or not
    private boolean validTouchRegion(MotionEvent motion) {
        // not activate on the title bar and media control bar
        if (motion.getY() <= mTitleBar.getHeight() || motion.getY() >= (mScreenHeight - mMediaControls.getHeight())) {
            return false;
        }

        return true;
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
//        mShare.setEnabled(enabled);
        mMediaControlPlayBtn.setEnabled(enabled);
        mMediaControlVolumeBtn.setEnabled(enabled);
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

    public void hideShade(){
        if(mHandler != null) {
            mHandler.removeCallbacks(mOnTouchUpThread);
            mHandler.postDelayed(mOnTouchUpThread, 0);
        }
    }
    @Override
    public void getVideoUrl(String url,String src) {

    }

    @Override
    public void getVideoErrorInfo(String info) {

    }
}
