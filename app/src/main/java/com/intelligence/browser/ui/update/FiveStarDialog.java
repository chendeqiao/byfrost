package com.intelligence.browser.ui.update;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelligence.browser.R;
import com.intelligence.browser.settings.BrowserSettingActivity;
import com.intelligence.browser.utils.DeviceInfoUtils;
import com.intelligence.browser.ui.widget.BrowserDialog;
import com.intelligence.browser.utils.ImageUtils;

public class FiveStarDialog extends BrowserDialog {
    private RatingStarItemView mRatingStarItemFirstView;
    private RatingStarItemView mRatingStarItemSecoudView;
    private RatingStarItemView mRatingStarItemThirdView;
    private RatingStarItemView mRatingStarItemFourthView;
    private RatingStarItemView mRatingStarItemFifthView;

    private int mScore;

    public FiveStarDialog(Context context) {

        super(context, R.style.FiveStarDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private Button mPositiveBotton;
    private Button mNegativeeBotton;
    private ImageView mColseIcon;
    @SuppressLint("MissingInflatedId")
    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.browser_five_star_dialog, null);
        setBrowserContentView(view);

        ((TextView) (view.findViewById(R.id.browser_five_star_title))).setText(getContext().getString(
                R.string.five_stars_titile, getContext().getString(R.string.application_name)));
//        (view.findViewById(R.id.star_negative)).setOnClickListener(this);
//        (view.findViewById(R.id.star_positive)).setOnClickListener(this);

        mPositiveBotton = view.findViewById(R.id.star_positive);
        mNegativeeBotton = view.findViewById(R.id.star_negative);
        mNegativeeBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mPositiveBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                DeviceInfoUtils.openAppMark(getContext());
            }
        });
        mRatingStarItemFirstView = view.findViewById(R.id.article_rating_star_item_first);
        mRatingStarItemSecoudView = view.findViewById(R.id.article_rating_star_item_secound);
        mRatingStarItemThirdView = view.findViewById(R.id.article_rating_star_item_third);
        mRatingStarItemFourthView = view.findViewById(R.id.article_rating_star_item_fourth);
        mRatingStarItemFifthView = view.findViewById(R.id.article_rating_star_item_fifth);
        mColseIcon = view.findViewById(R.id.five_start_close);
        mColseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ImageUtils.tinyIconColor(mColseIcon,R.color.setting_item_second);

    }

    @Override
    public void onClick(View v) {
        if (mScore > 0) {
            return;
        }
        int score = 0;
        switch (v.getId()) {
            case R.id.article_rating_star_item_first:
                score = 1;
                break;
            case R.id.article_rating_star_item_secound:
                score = 2;
                break;
            case R.id.article_rating_star_item_third:
                score = 3;
                break;
            case R.id.article_rating_star_item_fourth:
                score = 4;
                break;
            case R.id.article_rating_star_item_fifth:
                score = 5;
                break;
        }
        resetStarState();
        mScore = score;
        try {
            setRatingStarScore(score, false);
//            startScoreAnimation(score);
        }catch (Exception e){
            //lottie anim maybe is fail.
        }
    }

    public void resetStarState() {
        mScore = 0;
        mRatingStarItemFirstView.setShowStyle(RatingStarItemView.STAR_STYLE_DEFAULT);
        mRatingStarItemSecoudView.setShowStyle(RatingStarItemView.STAR_STYLE_DEFAULT);
        mRatingStarItemThirdView.setShowStyle(RatingStarItemView.STAR_STYLE_DEFAULT);
        mRatingStarItemFourthView.setShowStyle(RatingStarItemView.STAR_STYLE_DEFAULT);
        mRatingStarItemFifthView.setShowStyle(RatingStarItemView.STAR_STYLE_DEFAULT);
    }

    private void setPositiveClick(){
        mPositiveBotton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                BrowserSettingActivity.startPreferenceFragmentExtra(getContext(), "com.intelligence.browser.preferences.BrowserFeedbackFragment");
            }
        });
    }
    private void setRatingStarScore(int score,boolean isDefault) {
        switch (score) {
            case 1:
                setPositiveClick();
                mPositiveBotton.setText(getContext().getResources().getString(R.string.five_stars_comment));
                mRatingStarItemFirstView.setShowStyle(RatingStarItemView.STAR_STYLE_HIGH);
                break;
            case 2:
                setPositiveClick();
                mPositiveBotton.setText(getContext().getResources().getString(R.string.five_stars_comment));
                mRatingStarItemFirstView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemSecoudView.setShowStyle(RatingStarItemView.STAR_STYLE_HIGH);
                break;
            case 3:
                mPositiveBotton.setText(getContext().getResources().getString(R.string.five_stars));
                mRatingStarItemFirstView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemSecoudView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemThirdView.setShowStyle(RatingStarItemView.STAR_STYLE_RED);
                break;
            case 4:
                mPositiveBotton.setText(getContext().getResources().getString(R.string.five_stars));
                mRatingStarItemFirstView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemSecoudView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemThirdView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemFourthView.setShowStyle(RatingStarItemView.STAR_STYLE_RED);
                break;
            case 5:
                mPositiveBotton.setText(getContext().getResources().getString(R.string.five_stars));
                mRatingStarItemFirstView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemSecoudView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemThirdView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemFourthView.setShowStyle(RatingStarItemView.STAR_STYLE_LOW);
                mRatingStarItemFifthView.setShowStyle(RatingStarItemView.STAR_STYLE_RED);
                break;
            default:
                break;
        }
    }


    @Override
    public void show() {
        setAttributes();
        super.show();
    }

    public void onConfigurationChanged() {
        setAttributes();
    }

    private void setAttributes() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER);
//        dialogWindow.setAttributes(lp);
    }
}
