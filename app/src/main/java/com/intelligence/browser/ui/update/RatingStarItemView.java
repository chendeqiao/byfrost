package com.intelligence.browser.ui.update;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.intelligence.browser.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RatingStarItemView extends FrameLayout {
    public static final int STAR_STYLE_DEFAULT = 0;
    public static final int STAR_STYLE_LOW = 1;
    public static final int STAR_STYLE_HIGH = 2;
    public static final int STAR_STYLE_RED = 3;

    @IntDef({STAR_STYLE_DEFAULT, STAR_STYLE_LOW, STAR_STYLE_HIGH,STAR_STYLE_RED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RatingStarStyle {
    }

    private View mRootView;
    private ImageView mItemStar;
    private TextView mItemDes;
    private String mTextDes;

    public RatingStarItemView(@NonNull Context context) {
        super(context);
    }

    public RatingStarItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ratingstar);
        mTextDes = a.getString(R.styleable.ratingstar_ratingstar_text);
        a.recycle();
        initView(context);
    }

    public RatingStarItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    private ImageView mItemSelect;

    private void initView(Context context) {
        View view = View.inflate(getContext(), R.layout.browser_rating_start_item_view, this);
        mRootView = view.findViewById(R.id.articel_rating_item_layout);
        mItemStar = view.findViewById(R.id.articel_rating_item_star);
//        mItemSelect = view.findViewById(R.id.articel_rating_item_star_select);
        mItemDes = view.findViewById(R.id.articel_rating_item_des);
        mItemDes.setText(mTextDes);
        mItemStar.setBackground(getResources().getDrawable(R.drawable.browser_five_start_default));
        mItemStar.setBackground(getResources().getDrawable(R.drawable.common_gp_rate_star));
    }

    public void setShowStyle(@RatingStarStyle int ratingStarStyle) {

//        switch (ratingStarStyle) {
//            case STAR_STYLE_DEFAULT:
//                mItemStar.setBackgroundResource(R.drawable.browser_five_start_default);
//                mItemDes.setTextColor(getResources().getColor(R.color.browser_tips_color));
//                break;
//            case STAR_STYLE_LOW:

//                mItemDes.setTextColor(getResources().getColor(R.color.browser_tips_color));
//                break;
//            case STAR_STYLE_HIGH:
//                mItemStar.setBackground(getResources().getDrawable(R.drawable.common_gp_rate_star));
//                mItemDes.setTextColor(getResources().getColor(R.color.browser_tips_color));
//                break;
//            case STAR_STYLE_RED:
//                mItemStar.setBackground(getResources().getDrawable(R.drawable.common_gp_rate_star));
//                mItemDes.setTextColor(getResources().getColor(R.color.five_star_btn_color_high));
//                break;
        }
//    }

}
