package com.intelligence.browser.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.intelligence.browser.settings.BrowserSettings;
import com.intelligence.browser.settings.PreferenceKeys;
import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.ScreenUtils;

public class TextSizeProgressBar extends FrameLayout implements SeekBar.OnSeekBarChangeListener {
    public static final int TYPE_TEXT_SIZE = 0;
    public static final int TYPE_BRIGHTNESS = 1;

    private static final int SEEKBAR_DIFFERENCE_FONTSIZE = 6;//与设置中设置字体大小的差值
    private static final int SEEKBAR_MAX_PROGRESS = 14;//设置中设置字体的长度是20,这里是14，用于主页的特殊情况
    private static final int SEEKBAR_MAX_BRIGHTNESS = 100;
    private static int TEXT_SIZE_MAX_VALUE = SEEKBAR_DIFFERENCE_FONTSIZE + SEEKBAR_MAX_PROGRESS;
    private int mRadius;
    private long mOldTime;
    private ImageView mProgressIcon;
    private SeekBar mSeekBar;
    private int mCurrentProgress = 0;
    private int mMax = SEEKBAR_MAX_PROGRESS;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            setVisibility(GONE);
        }
    };


    private Thread mThread = new Thread(new Runnable() {
        @Override
        public void run() {
            boolean isClose = false;
            while (!isClose) {
                long currentTime = System.currentTimeMillis();
                if (mOldTime == 0) {
                    mOldTime = currentTime;
                } else if (currentTime - mOldTime > 1000 * 3) {
                    isClose = true;
                    Message message = new Message();
                    mHandler.sendMessage(message);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    public TextSizeProgressBar(Context context) {
        super(context);
        initView();
    }

    public TextSizeProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }


    public void initView() {
        View mView = View.inflate(getContext(), R.layout.browser_progress_seekbar, this);

        mProgressIcon = (ImageView) mView.findViewById(
                R.id.progress_icon);

        mSeekBar = (SeekBar) mView.findViewById(
                R.id.seekbar);
        mMax = SEEKBAR_MAX_PROGRESS;
        mCurrentProgress = BrowserSettings.getInstance().getPreferences().getInt(PreferenceKeys.PREF_MIN_FONT_SIZE, 0);
        //用于主页的特殊情况（0代表10PT）
        if (mCurrentProgress > SEEKBAR_DIFFERENCE_FONTSIZE) {
            mCurrentProgress -= SEEKBAR_DIFFERENCE_FONTSIZE;
        } else {
            mCurrentProgress = 0;
        }
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mCurrentProgress);
        setTextSize(mCurrentProgress);
    }

    private void setTextSize(int progress){
        try {
            if (mProgressIcon != null) {
                int pading = ScreenUtils.dpToPxInt(getContext(), (int) ((TEXT_SIZE_MAX_VALUE - progress) / 2.5f));
                mProgressIcon.setPadding(pading, 0, pading, 0);
            }
        }catch (Exception e){
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mCurrentProgress = progress;
            //用于主页的特殊情况
            if (progress != 0) {
                progress += SEEKBAR_DIFFERENCE_FONTSIZE;
            }
            BrowserSettings.getInstance().getPreferences().edit().putInt(PreferenceKeys.PREF_MIN_FONT_SIZE, progress)
                    .apply();
            setTextSize(progress);
        }
        mOldTime = 0;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}
