package com.intelligence.componentlib.badge;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.TextView;

import androidx.annotation.ColorRes;

import com.intelligence.commonlib.tools.ScreenUtils;
import com.intelligence.commonlib.tools.StringUtil;

public class BadgeView extends ViewGroup {
    /**
     * 标记类型--无
     */
    public static final int BADGE_TYPE_NONE = -1;
    /**
     * 标记类型--文本
     */
    public static final int BADGE_TYPE_TEXT = 0;

    /**
     * 标记类型--圆点
     */
    public static final int BADGE_TYPE_CIRCLE_DOT = 1;

    /**
     * 标记类型--文本(文本背景左下角带箭头样式)
     */
    public static final int BADGE_TYPE_ARROW_TEXT = 3;

    /**
     * 文本类型标记与targetview重叠部分水平方向大小
     */
    private final float BADGE_TEXT_OVERLAY_IN_TARGET_HORIZONTAL = 10f;
    /**
     * 文本类型标记与targetview重叠部分垂直方向大小
     */
    private final float BADGE_TEXT_OVERLAY_IN_TARGET_VERTICAL = 10f;
    /**
     * 文本类型标记默认字体大小
     */
    private final float DEFAULT_BADGE_TEXT_FONT_SIZE = 8.0f;
    /**
     * 文本类型标记的默认高度
     */
    private final float DEFAULT_BADGE_TEXT_HEIGHT = 15f;
    /**
     * 圆点类型标记的默认宽高
     */
    private final int DEFAULT_BADGE_DOT_SIZE = 8;
    /**
     * 标记默认背景颜色--红色
     */
    private static final int DEFAULT_BADGE_BACKGROUND_COLOR = Color.parseColor("#ff6600");
    /**
     * 文本类型标记默认文字颜色--白色
     */
    private static final int DEFAULT_BADGE_TEXT_COLOR = Color.WHITE;
    /**
     * 文本类型描边默认宽度
     */
    private final float DEFAULT_BADGE_TEXT_STROKE_WIDTH = 1;
    /**
     * 文本类型默认圆角半径
     */
    private final float DEFAULT_BADGE_RADIUS = 5;
    /**
     * 目标View是否水平居中显示
     */
    private boolean isTargetShowInCenterHorizontal = false;
    /**
     * 目标View是否垂直居中显示
     */
    private boolean isTargetShowInCenterVertical = false;
    /**
     * 圆点badge显示的宽高大小
     */
    public static float mCircleDotBadgeSize;
    /**
     * 文本badge的高度
     */
    private float mTextBadgeHeight;

    /**
     * 文本badge的最大宽度
     */
    private int mTextBadgeMaxWidth;
    /**
     * badge描边宽度
     */
    private int mTextBadgeStrokeWidth;
    /**
     * 带圆角文本badge圆角半径
     */
    private float mTextBadgeRadius;
    /**
     * badge类型，目前支持文本和圆点两种
     */
    private int mBadgeType;
    /**
     * 记录badge的可见性
     */
    private boolean mBadgeVisible;
    /**
     * 设置红点应用的目标是否是文本，用于控制红点offset值
     */
    private boolean mCircleDotStyleForTextTarget;
    /**
     * 文本类型标记文字颜色
     */
    private int mBadgeTextColor;
    /**
     * 文本类型标记文字颜色
     */
    private float mBadgeTextSize;
    /**
     * 标记背景颜色
     */
    private int mBadgeBackgroundColor;

    /**
     * badge从target右上角向左偏移位置
     */
    private float mHorizontalOffset = -1;
    /**
     * badge从target右上角向下偏移位置
     */
    private float mVerticalOffset = -1;

    /**
     * 文本类型的view
     */
    private TextView mTextBadgeView;
    /**
     * 圆点类型的view
     */
    private View mCircleDotBadgeView;

    /**
     * 指定child整体布局位置
     */
    private int mGravity;

    private int mWidthSpecMode;
    private int mHeightSpecMode;

    private boolean isWithSharpCorner = false;
    private String mBadgeContent;
    private AnimatorSet mAnimatorSet;
    private float[] mAnimParams = new float[]{1.0F, 0.8F, 1.1F, 0.9F, 1.0F};

    public BadgeView(Context context) {
        super(context);
        init();
    }

    public BadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public BadgeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mCircleDotBadgeSize = ScreenUtils.dpToPx(getContext(), DEFAULT_BADGE_DOT_SIZE);
        mTextBadgeHeight = ScreenUtils.dpToPx(getContext(), DEFAULT_BADGE_TEXT_HEIGHT);
        mTextBadgeStrokeWidth = ScreenUtils.dpToPxInt(getContext(), DEFAULT_BADGE_TEXT_STROKE_WIDTH);
        mTextBadgeRadius = ScreenUtils.dpToPxInt(getContext(), DEFAULT_BADGE_RADIUS);
        mBadgeType = BADGE_TYPE_NONE;
        mBadgeTextColor = DEFAULT_BADGE_TEXT_COLOR;
        mBadgeBackgroundColor = DEFAULT_BADGE_BACKGROUND_COLOR;
        mBadgeTextSize = DEFAULT_BADGE_TEXT_FONT_SIZE;
        mTextBadgeMaxWidth = -1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //内部不存在子View或者超过两个，则认为设置无效
        if (getChildCount() == 0 || getChildCount() > 2) {
            setMeasuredDimension(0, 0);
            return;
        }

        if (getChildCount() == 1) {//只显示标识或者targetview的情况
            measureOneView(widthMeasureSpec, heightMeasureSpec);
        } else if (getChildCount() == 2) {//显示targetview和标识的情况
            measureTwoView(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void measureOneView(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        View childView = getChildAt(0);
        measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        MarginLayoutParams targetParams = (MarginLayoutParams) childView.getLayoutParams();
        //add targetview width
//        int width = childView.getMeasuredWidth() + targetParams.leftMargin + targetParams.rightMargin;
//        int height = childView.getMeasuredHeight() + targetParams.topMargin + targetParams.bottomMargin;
        int width = childView.getMeasuredWidth() + targetParams.leftMargin + targetParams.rightMargin + getPaddingLeft() + getPaddingRight();
        int height = childView.getMeasuredHeight() + targetParams.topMargin + targetParams.bottomMargin + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width, heightMode == MeasureSpec.EXACTLY ? heightSize : height);
    }

    private void measureTwoView(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        mWidthSpecMode = widthMode;
        mHeightSpecMode = heightMode;

        View targetChild = getChildAt(0);
        measureChild(targetChild, widthMeasureSpec, heightMeasureSpec);
        MarginLayoutParams targetParams = (MarginLayoutParams) targetChild.getLayoutParams();

        View badgeChild = getChildAt(1);
        measureChild(badgeChild, widthMeasureSpec, heightMeasureSpec);

        float horizontalOffset = 0;//badge在target中向左偏移位置
        float verticalOffset = 0;//badge在target中向下偏移位置
        switch (mBadgeType) {
            case BADGE_TYPE_CIRCLE_DOT:
                verticalOffset = mCircleDotBadgeSize / 2;
                if (mCircleDotStyleForTextTarget) {
                    horizontalOffset = 0;
                } else {
                    horizontalOffset = mCircleDotBadgeSize;
                    verticalOffset = mCircleDotBadgeSize;
                }
                break;
            case BADGE_TYPE_TEXT:
                horizontalOffset = ScreenUtils.dpToPx(getContext(), BADGE_TEXT_OVERLAY_IN_TARGET_HORIZONTAL);
                verticalOffset = ScreenUtils.dpToPx(getContext(), BADGE_TEXT_OVERLAY_IN_TARGET_VERTICAL);
                break;
            default:
                break;
        }

        horizontalOffset = mHorizontalOffset != -1 ? mHorizontalOffset : horizontalOffset;
        int finalHorizontalOffset = (int) Math.min(horizontalOffset, targetChild.getMeasuredWidth());
        //目标View的宽度
//        int width = targetChild.getMeasuredWidth() + targetParams.leftMargin + targetParams.rightMargin;
        int width = targetChild.getMeasuredWidth() + targetParams.leftMargin + targetParams.rightMargin + getPaddingLeft() + getPaddingRight();
        //根据badge和水平offset调整宽度
        width += Math.max(badgeChild.getMeasuredWidth() - finalHorizontalOffset, 0);

        //如果想保持目标View在父布局中水平居中，则添加badge超出目标View右边部分的同等大小，使目标View的左右间距相同
        if (isTargetShowInCenterHorizontal) {
            width += Math.max(badgeChild.getMeasuredWidth() - finalHorizontalOffset, 0);
        }

        verticalOffset = mVerticalOffset != -1 ? mVerticalOffset : verticalOffset;
        int finalVerticalOffset = (int) Math.min(verticalOffset, targetChild.getMeasuredHeight());
//        int height = targetChild.getMeasuredHeight() + targetParams.topMargin + targetParams.bottomMargin;
        int height = targetChild.getMeasuredHeight() + targetParams.topMargin + targetParams.bottomMargin + getPaddingTop() + getPaddingBottom();
        height += Math.max(badgeChild.getMeasuredHeight() - finalVerticalOffset, 0);

        if (mGravity != Gravity.BOTTOM) {
            //如果想保持目标View在父布局中垂直居中，则添加badge超出目标View上边部分的同等大小，使目标View的上下间距相同
            if (isTargetShowInCenterVertical) {
                height += Math.max(badgeChild.getMeasuredHeight() - finalVerticalOffset, 0);
            } else {
                height -= Math.max(badgeChild.getMeasuredHeight() - finalVerticalOffset, 0);
            }
        }

        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : width, heightMode == MeasureSpec.EXACTLY ? heightSize : height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (getChildCount() == 0 || getChildCount() > 2) {
            return;
        }

        if (getChildCount() == 1) {
            layoutOneView();
        } else if (getChildCount() == 2) {
            layoutTwoView();
        }

    }

    /**
     * 只显示目标View或者Badge
     */
    private void layoutOneView() {
        View childView = getChildAt(0);
        MarginLayoutParams childParams = (MarginLayoutParams) childView.getLayoutParams();
//        int left = childParams.leftMargin;
//        int top = childParams.topMargin;
        int left = childParams.leftMargin + getPaddingLeft();
        int top = childParams.topMargin + getPaddingTop();
        int right = left + childView.getMeasuredWidth();
        int bottom = top + childView.getMeasuredHeight();
        childView.layout(left, top, right, bottom);
    }

    /**
     * 同时显示目标View和Badge
     */
    private void layoutTwoView() {
        View targetChild = getChildAt(0);
        View badgeChild = getChildAt(1);
        //add badgeview width
        float horizontalOffset = 0;//badge在target中向左偏移位置
        float verticalOffset = 0;//badge在target中向下偏移位置
        switch (mBadgeType) {
            case BADGE_TYPE_CIRCLE_DOT:
                verticalOffset = mCircleDotBadgeSize / 2;
                if (mCircleDotStyleForTextTarget) {
                    horizontalOffset = 0;
                } else {
                    horizontalOffset = mCircleDotBadgeSize;
                    verticalOffset = mCircleDotBadgeSize;
                }
                break;
            case BADGE_TYPE_TEXT:
                horizontalOffset = ScreenUtils.dpToPxInt(getContext(), BADGE_TEXT_OVERLAY_IN_TARGET_HORIZONTAL);
                verticalOffset = ScreenUtils.dpToPxInt(getContext(), BADGE_TEXT_OVERLAY_IN_TARGET_VERTICAL);
                break;
            default:
                break;
        }

        horizontalOffset = mHorizontalOffset != -1 ? mHorizontalOffset : horizontalOffset;
        int finalHorizontalOffset = (int) Math.min(horizontalOffset, targetChild.getMeasuredWidth());
        verticalOffset = mVerticalOffset != -1 ? mVerticalOffset : verticalOffset;
        int finalVerticalOffset = (int) Math.min(verticalOffset, targetChild.getMeasuredHeight());

        //layout targetView
        MarginLayoutParams targetParams = (MarginLayoutParams) targetChild.getLayoutParams();
//        int targetLeft = targetParams.leftMargin;
        int targetLeft = targetParams.leftMargin + getPaddingLeft();
        if (isTargetShowInCenterHorizontal) {
            if (mWidthSpecMode == MeasureSpec.EXACTLY) {
                targetLeft = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 - targetChild.getMeasuredWidth() / 2 + getPaddingLeft();
            } else {
                targetLeft += Math.max(badgeChild.getMeasuredWidth() - finalHorizontalOffset, 0);
            }
        }

//        int targetTop = targetParams.topMargin + Math.max(badgeChild.getMeasuredHeight() - finalVerticalOffset, 0);
        int targetTop = targetParams.topMargin + Math.max(badgeChild.getMeasuredHeight() - finalVerticalOffset, 0) + getPaddingTop();
        if (isTargetShowInCenterVertical) {
            if (mHeightSpecMode == MeasureSpec.EXACTLY) {
                targetTop = (getHeight() - getPaddingTop() - getPaddingBottom()) / 2 - targetChild.getMeasuredHeight() / 2 + getPaddingTop();
            }
        } else {
            targetTop -= Math.max(badgeChild.getMeasuredHeight() - finalVerticalOffset, 0);
        }

        int targetRight = targetLeft + targetChild.getMeasuredWidth();
        int targetBottom = targetTop + targetChild.getMeasuredHeight();
        targetChild.layout(targetLeft, targetTop, targetRight, targetBottom);

        //layout badge
//        float leftOffset;
//        if(finalHorizontalOffset > badgeChild.getMeasuredWidth()){
//            leftOffset = finalHorizontalOffset;
//        }else{
//            leftOffset = badgeChild.getMeasuredWidth();
//        }

//        int topOffset;
//        if(finalVerticalOffset > badgeChild.getMeasuredHeight()){
//            topOffset = finalVerticalOffset - badgeChild.getMeasuredHeight();
//        }else{
//            topOffset = 0;
//        }
//        int badgeLeft = (int) Math.max(getWidth() - leftOffset, 0);
//        int badgeLeft = (int) Math.max(getWidth() - leftOffset - getPaddingRight(), 0);
        int badgeLeft = targetRight - finalHorizontalOffset;
        int badgeBottom = targetTop + finalVerticalOffset;
//        int badgeTop = Math.min(topOffset, getHeight() - badgeChild.getMeasuredHeight());
//        int badgeTop = Math.min(topOffset, getHeight() - badgeChild.getMeasuredHeight()) + getPaddingTop();
        int badgeTop = badgeBottom - badgeChild.getMeasuredHeight();
        int badgeRight = Math.min(badgeLeft + badgeChild.getMeasuredWidth(), getWidth() - getPaddingRight());
//        int badgeBottom = badgeTop + badgeChild.getMeasuredHeight();
        badgeChild.layout(badgeLeft, badgeTop, badgeRight, badgeBottom);
    }

    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }

    /**
     * 显示文本Badge提示
     * <ul>
     * 注意：所有参数必须在此方法之前设置才能生效，切记
     * </ul>
     *
     * @param badgeContent 文本badge显示的内容
     */
    public void showTextBadge(String badgeContent) {
        showTextBadge(badgeContent, false);
    }

    /**
     * 获取气泡文本宽度
     */
    public int getBadgeWidth() {
        int width = 0;
        if (mTextBadgeView != null) {
            int textWidth = (int) Layout.getDesiredWidth(mBadgeContent, mTextBadgeView.getPaint());
            int paddingLeft = mTextBadgeView.getPaddingLeft();
            int paddingRight = mTextBadgeView.getPaddingRight();
            width = textWidth + paddingLeft + paddingRight;
        }
        return width;
    }

    /**
     * 显示文本Badge提示
     * <ul>
     * 注意：所有参数必须在此方法之前设置才能生效，切记
     * </ul>
     *
     * @param badgeContent    文本badge显示的内容
     * @param withSharpCorner 是否带尖角样式
     */
    public void showTextBadge(String badgeContent, boolean withSharpCorner) {
        this.isWithSharpCorner = withSharpCorner;
        this.mBadgeContent = badgeContent;
        mBadgeVisible = true;
        setBadgeType(BADGE_TYPE_TEXT);
        if (mCircleDotBadgeView != null) {
            removeView(mCircleDotBadgeView);
        }

        int width = LayoutParams.WRAP_CONTENT;
        int height = (int) mTextBadgeHeight;
        if (TextUtils.isEmpty(badgeContent) || badgeContent.length() == 1) {
            width = height;
        }
        if(!TextUtils.isEmpty(badgeContent) && badgeContent.length() == 1 && StringUtil.isChinese(badgeContent.charAt(0))) {
            width += 5.5f;
        }
        if (mTextBadgeView == null) {
            mTextBadgeView = new TextView(getContext());
            mTextBadgeView.setTextColor(mBadgeTextColor);
            mTextBadgeView.setIncludeFontPadding(false);
            mTextBadgeView.setGravity(Gravity.CENTER);
            mTextBadgeView.setMaxLines(1);
            this.addView(mTextBadgeView, new MarginLayoutParams(width, height));
        } else {
            int badgeIndex = indexOfChild(mTextBadgeView);
            if (badgeIndex != -1) {
                MarginLayoutParams marginLayoutParams = (MarginLayoutParams) mTextBadgeView.getLayoutParams();
                marginLayoutParams.width = width;
                marginLayoutParams.height = height;
                mTextBadgeView.setLayoutParams(marginLayoutParams);
                mTextBadgeView.setVisibility(VISIBLE);
            } else {
                this.addView(mTextBadgeView, new MarginLayoutParams(width, height));
                mTextBadgeView.setVisibility(VISIBLE);
            }
        }
        mTextBadgeView.setPadding(ScreenUtils.dpToPxInt(getContext(), 4.5f), 0, ScreenUtils.dpToPxInt(getContext(), 4.5f), 0);
        mTextBadgeView.setBackgroundDrawable(withSharpCorner ? getTextBadgeSharpCornerBg() : getTextBadgeBackground());
        mTextBadgeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mBadgeTextSize);
        if (mTextBadgeMaxWidth > 0) {
            mTextBadgeView.setMaxWidth(mTextBadgeMaxWidth);
        }
        mTextBadgeView.setText(badgeContent);
//        mTextBadgeView.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/DIN Alternate.ttf"));
    }

    /**
     * 显示圆点Badge提示
     * <ul>
     * 注意：所有参数必须在此方法之前设置才能生效，切记
     * </ul>
     */
    public void showCircleDotBadge() {
        mBadgeVisible = true;
        setBadgeType(BADGE_TYPE_CIRCLE_DOT);
        if (mTextBadgeView != null) {
            removeView(mTextBadgeView);
        }

        this.mBadgeContent = "";
        this.isWithSharpCorner = false;

        int badgeCircleDotWidth = (int) mCircleDotBadgeSize;
        int badgeCircleDotHeight = badgeCircleDotWidth;
        if (mCircleDotBadgeView == null) {
            mCircleDotBadgeView = new View(getContext());
            mCircleDotBadgeView.setBackgroundDrawable(getCircleBadgeBackground());
            this.addView(mCircleDotBadgeView, new MarginLayoutParams(badgeCircleDotWidth, badgeCircleDotHeight));
        } else {
            int badgeIndex = indexOfChild(mCircleDotBadgeView);
            if (badgeIndex == -1) {
                this.addView(mCircleDotBadgeView, new MarginLayoutParams(badgeCircleDotWidth, badgeCircleDotHeight));
                mCircleDotBadgeView.setVisibility(VISIBLE);
            }
        }
    }

    /**
     * 隐藏红点(将红点remove掉)
     */
    public void hideBadge() {
        mBadgeType = BADGE_TYPE_NONE;
        mBadgeVisible = false;
        if (mTextBadgeView != null) {
            removeView(mTextBadgeView);
        }

        if (mCircleDotBadgeView != null) {
            removeView(mCircleDotBadgeView);
        }
    }

    /**
     * 设置Badge的可见性，并不从不居中移除
     */
    public void setBadgeVisibility(boolean visible) {
        if (mBadgeVisible == visible) {
            return;
        }

        if (mBadgeType == BADGE_TYPE_NONE) {
            return;
        }

        mBadgeVisible = visible;
        if (mTextBadgeView != null) {
            mTextBadgeView.setVisibility(mBadgeVisible ? VISIBLE : INVISIBLE);
        }

        if (mCircleDotBadgeView != null) {
            mCircleDotBadgeView.setVisibility(mBadgeVisible ? VISIBLE : INVISIBLE);
        }
    }

    /**
     * 判断当前badge的可见性
     *
     * @return
     * @see {@link #setBadgeVisibility(boolean)}
     */
    public boolean isBadgeVisible() {
        return mBadgeVisible;
    }

    /**
     * 是否已显示了Badge标记
     */
    public boolean isShowBadge() {
        return mBadgeType != BADGE_TYPE_NONE;
    }

    /**
     * 获取文本badge的背景
     *
     * @return
     */
    private Drawable getTextBadgeBackground() {

        float r = mTextBadgeHeight / 2;
        float[] outerR = new float[]{r, r, r, r, r, r, r, r};

//        RoundRectShape rr = new RoundRectShape(outerR, null, null);
//        ShapeDrawable drawable = new ShapeDrawable(rr);
//        drawable.getPaint().setColor(mBadgeBackgroundColor);

        GradientDrawable drawable = new GradientDrawable();//创建drawable
        drawable.setColor(mBadgeBackgroundColor);
        drawable.setCornerRadii(outerR);
        drawable.setStroke(mTextBadgeStrokeWidth, Color.WHITE);//白色内描边

        return drawable;
    }

    /**
     * 获取文本badge的背景
     *
     * @return
     */
    private Drawable getTextBadgeSharpCornerBg() {

//        float r = mTextBadgeHeight / 2;
//        float[] outerR = new float[]{r, r, r, r, r, r, 0, 0};
        float[] outerR = new float[]{mTextBadgeRadius, mTextBadgeRadius, 0, 0, mTextBadgeRadius, mTextBadgeRadius, 0, 0};

//        RoundRectShape rr = new RoundRectShape(outerR, null, null);
//        ShapeDrawable drawable = new ShapeDrawable(rr);
//        drawable.getPaint().setColor(mBadgeBackgroundColor);

        GradientDrawable drawable = new GradientDrawable();//创建drawable
        drawable.setColor(mBadgeBackgroundColor);
        drawable.setCornerRadii(outerR);
        drawable.setStroke(mTextBadgeStrokeWidth, Color.WHITE);//白色内描边

        return drawable;
    }

    /**
     * 获取圆点badge的背景
     *
     * @return
     */
    private Drawable getCircleBadgeBackground() {
//        ArcShape arcShape = new ArcShape(0, 360); //顺时针  开始角度0， 扫描的角度360 扇形
//        ShapeDrawable drawable = new ShapeDrawable(arcShape);
//        drawable.getPaint().setColor(mBadgeBackgroundColor);
//        drawable.getPaint().setStyle(Paint.Style.FILL);

        float r = mCircleDotBadgeSize / 2;
        float[] outerR = new float[] { r, r, r, r, r, r, r, r };
        GradientDrawable drawable = new GradientDrawable();//创建drawable
        drawable.setColor(mBadgeBackgroundColor);
        drawable.setCornerRadii(outerR);
        drawable.setStroke(mTextBadgeStrokeWidth, Color.WHITE);

        return drawable;
    }

    /**
     * 设置TargetView是否水平居中显示
     *
     * @param isCenterHorizontal
     */
    public void setTargetShowInCenterHorizontal(boolean isCenterHorizontal) {
        this.isTargetShowInCenterHorizontal = isCenterHorizontal;
    }

    /**
     * 设置TargetView是否垂直居中显示
     *
     * @param isCenterVertial
     */
    public void setTargetShowInCenterVertical(boolean isCenterVertial) {
        this.isTargetShowInCenterVertical = isCenterVertial;
    }

    /**
     * 设置描边的宽度
     */
    public void setBadgeStrokeWidth(int width){
        this.mTextBadgeStrokeWidth = width;
    }

    /**
     * 设置TargetView是否居中显示（水平和垂直同时生效）
     *
     * @param isCenter
     */
    public void setTargetShowInCenter(boolean isCenter) {
        this.isTargetShowInCenterHorizontal = isCenter;
        this.isTargetShowInCenterVertical = isCenter;
    }

    /**
     * 设置标记类型，目前支持两种类型，文本气泡和圆点 {@link #BADGE_TYPE_TEXT}，{@link #BADGE_TYPE_CIRCLE_DOT}
     *
     * @param type
     */
    private void setBadgeType(int type) {
        this.mBadgeType = type;
    }

    /**
     * 获取当前Badge类型
     *
     * @return 返回以下类型中的一个：
     * <ul>
     * <li>{@link #BADGE_TYPE_NONE}</li>
     * <li>{@link #BADGE_TYPE_TEXT}</li>
     * <li>{@link #BADGE_TYPE_CIRCLE_DOT}</li>
     * </ul>
     */
    public int getBadgeType() {
        return this.mBadgeType;
    }

    /**
     * 设置红点显示是否是针对文本内容的
     *
     * @param targetIsText
     */
    public void setCircleDotStyleForTextTarget(boolean targetIsText) {
        this.mCircleDotStyleForTextTarget = targetIsText;
    }

    /**
     * 设置气泡文本颜色
     *
     * @param color
     */
    public void setBadgeTextColor(@ColorRes int color) {
        this.mBadgeTextColor = color;
    }

    /**
     * 设置气泡文本字体大小，单位为sp
     *
     * @param textSize
     */
    public void setBadgeTextSize(float textSize) {
        this.mBadgeTextSize = (textSize >= 0 ? textSize : 0);
    }

    /**
     * 设置圆点或气泡背景颜色
     *
     * @param color
     */
    public void setBadgeBackgroundColor(@ColorRes int color) {
        this.mBadgeBackgroundColor = color;
    }

    /**
     * 设置badge从target右上角向左偏移位置
     *
     * @param offset
     */
    public void setHorizontalOffset(float offset) {
        this.mHorizontalOffset = offset;
    }

    /**
     * 设置badge从target右上角向下偏移位置
     *
     * @param offset
     */
    public void setVerticalOffset(float offset) {
        this.mVerticalOffset = offset;
    }

    /**
     * 设置圆点Bagde直径大小
     *
     * @param circleDotSize
     */
    public void setCircleDotBadgeSize(float circleDotSize) {
        mCircleDotBadgeSize = (circleDotSize >= 0 ? circleDotSize : 0);
    }

    /**
     * 获取圆点Bagde直径大小
     */
    public float getCircleDotBadgeSize(){
        return mCircleDotBadgeSize;
    }

    /**
     * 设置文本Bagde高度
     *
     * @param textBadgeHeight
     */
    public void setTextBadgeHeight(float textBadgeHeight) {
        this.mTextBadgeHeight = textBadgeHeight;
    }

    /**
     * 设置文本badge的最大宽度
     */
    public void setTextBadgeMaxWidth(int mTextBadgeMaxWidth) {
        this.mTextBadgeMaxWidth = mTextBadgeMaxWidth;
    }


    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    /**
     * 当前红点是否有角标样式
     *
     * @return
     */
    public boolean isWithSharpCorner() {
        return isWithSharpCorner;
    }

    /**
     * 获取当前红点的内容
     *
     * @return
     */
    public String getBadgeContent() {
        return mBadgeContent;
    }

    public void startBadgeViewAnimation() {
        View view = mBadgeType == BADGE_TYPE_CIRCLE_DOT ? mCircleDotBadgeView : mBadgeType == BADGE_TYPE_TEXT ? mTextBadgeView : null;
        if (mAnimatorSet == null && view != null) {
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", mAnimParams);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", mAnimParams);
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.setDuration(300L);
            mAnimatorSet.playTogether(scaleXAnimator, scaleYAnimator);
        }
        mAnimatorSet.setTarget(view);
        mAnimatorSet.start();
    }

    public void startShakeAndRotateAnimation() {
        // 旋转动画
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(this, "rotation", 0f, 7f, -7f, 0f);
        rotateAnimator.setDuration(500);
        rotateAnimator.setRepeatCount(3);
        rotateAnimator.setInterpolator(new CycleInterpolator(1));

        // 震动动画
        ObjectAnimator shakeAnimator = ObjectAnimator.ofFloat(this, "translationX", 0f, 1f, -1f, 0f);
        shakeAnimator.setDuration(500);
        shakeAnimator.setRepeatCount(3);
        shakeAnimator.setInterpolator(new CycleInterpolator(2));

        // 同时执行旋转和震动动画
        rotateAnimator.start();
        shakeAnimator.start();
    }
}
