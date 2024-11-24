package com.intelligence.commonlib.baseUi;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.intelligence.browser.R;
import com.intelligence.commonlib.tools.DisplayUtil;
public class CircleImageView extends AppCompatImageView {
    private Context context;

    private boolean isCircle; // 是否显示为圆形，如果为圆形则设置的corner无效
    private boolean isCoverSrc; // border、inner_border是否覆盖图片
    private int borderWidth; // 边框宽度
    private int borderColor = Color.WHITE; // 边框颜色
    private int innerBorderWidth; // 内层边框宽度
    private int innerBorderColor = Color.WHITE; // 内层边框充色

    private int cornerRadius; // 统一设置圆角半径，优先级高于单独设置每个角的半径
    private int cornerTopLeftRadius; // 左上角圆角半径
    private int cornerTopRightRadius; // 右上角圆角半径
    private int cornerBottomLeftRadius; // 左下角圆角半径
    private int cornerBottomRightRadius; // 右下角圆角半径

    private int maskColor; // 遮罩颜色

    private int width;
    private int height;
    private float radius;

    private float[] borderRadii;
    private float[] srcRadii;

    private RectF srcRectF; // 图片占的矩形区域
    private RectF borderRectF; // 边框的矩形区域

    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, 0, 0);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
//            if (attr == R.styleable.CircleImageView_is_cover_src) {
//                isCoverSrc = ta.getBoolean(attr, isCoverSrc);
//            } else if (attr == R.styleable.CircleImageView_is_circle) {
//                isCircle = ta.getBoolean(attr, isCircle);
//            } else if (attr == R.styleable.CircleImageView_border_width) {
//                borderWidth = ta.getDimensionPixelSize(attr, borderWidth);
//            } else if (attr == R.styleable.CircleImageView_border_color) {
//                borderColor = ta.getColor(attr, borderColor);
//            } else if (attr == R.styleable.CircleImageView_inner_border_width) {
//                innerBorderWidth = ta.getDimensionPixelSize(attr, innerBorderWidth);
//            } else if (attr == R.styleable.CircleImageView_inner_border_color) {
//                innerBorderColor = ta.getColor(attr, innerBorderColor);
//            } else if (attr == R.styleable.CircleImageView_corner_radius) {
//                cornerRadius = ta.getDimensionPixelSize(attr, cornerRadius);
//            } else if (attr == R.styleable.CircleImageView_corner_top_left_radius) {
//                cornerTopLeftRadius = ta.getDimensionPixelSize(attr, cornerTopLeftRadius);
//            } else if (attr == R.styleable.CircleImageView_corner_top_right_radius) {
//                cornerTopRightRadius = ta.getDimensionPixelSize(attr, cornerTopRightRadius);
//            } else if (attr == R.styleable.CircleImageView_corner_bottom_left_radius) {
//                cornerBottomLeftRadius = ta.getDimensionPixelSize(attr, cornerBottomLeftRadius);
//            } else if (attr == R.styleable.CircleImageView_corner_bottom_right_radius) {
//                cornerBottomRightRadius = ta.getDimensionPixelSize(attr, cornerBottomRightRadius);
//            } else if (attr == R.styleable.CircleImageView_mask_color) {
//                maskColor = ta.getColor(attr, maskColor);
//            }
        }
        ta.recycle();

        borderRadii = new float[8];
        srcRadii = new float[8];

        borderRectF = new RectF();
        srcRectF = new RectF();
        calculateRadii();
        clearInnerBorderWidth();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        initBorderRectF();
        initSrcRectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        int angle  = cornerRadius;
        path.moveTo(angle, 0);
        path.lineTo(width - angle, 0);
        path.quadTo(width, 0, width, angle);//第一个角
        path.lineTo(width, height - angle);
        path.quadTo(width, height, width - angle, height);//2
        path.lineTo(angle, height);
        path.quadTo(0, height, 0, height - angle);//3
        path.lineTo(0, angle);
        path.quadTo(0, 0, angle, 0);//4
        canvas.clipPath(path);
        super.onDraw(canvas);

    }


    /**
     * 计算外边框的RectF
     */
    private void initBorderRectF() {
        if (!isCircle) {
            borderRectF.set(borderWidth / 2.0f, borderWidth / 2.0f, width - borderWidth / 2.0f, height - borderWidth / 2.0f);
        }
    }

    /**
     * 计算图片原始区域的RectF
     */
    private void initSrcRectF() {
        if (isCircle) {
            radius = Math.min(width, height) / 2.0f;
            srcRectF.set(width / 2.0f - radius, height / 2.0f - radius, width / 2.0f + radius, height / 2.0f + radius);
        } else {
            srcRectF.set(0, 0, width, height);
            if (isCoverSrc) {
                srcRectF = borderRectF;
            }
        }
    }

    /**
     * 计算RectF的圆角半径
     */
    private void calculateRadii() {
        if (isCircle) {
            return;
        }
        if (cornerRadius > 0) {
            for (int i = 0; i < borderRadii.length; i++) {
                borderRadii[i] = cornerRadius;
                srcRadii[i] = cornerRadius - borderWidth / 2.0f;
            }
        } else {
            borderRadii[0] = borderRadii[1] = cornerTopLeftRadius;
            borderRadii[2] = borderRadii[3] = cornerTopRightRadius;
            borderRadii[4] = borderRadii[5] = cornerBottomRightRadius;
            borderRadii[6] = borderRadii[7] = cornerBottomLeftRadius;

            srcRadii[0] = srcRadii[1] = cornerTopLeftRadius - borderWidth / 2.0f;
            srcRadii[2] = srcRadii[3] = cornerTopRightRadius - borderWidth / 2.0f;
            srcRadii[4] = srcRadii[5] = cornerBottomRightRadius - borderWidth / 2.0f;
            srcRadii[6] = srcRadii[7] = cornerBottomLeftRadius - borderWidth / 2.0f;
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
    }

    private void calculateRadiiAndRectF(boolean reset) {
        if (reset) {
            cornerRadius = 0;
        }
        calculateRadii();
        initBorderRectF();
        invalidate();
    }

    /**
     * 目前圆角矩形情况下不支持inner_border，需要将其置0
     */
    private void clearInnerBorderWidth() {
        if (!isCircle) {
            this.innerBorderWidth = 0;
        }
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = DisplayUtil.dp2px(context, cornerRadius);
        calculateRadiiAndRectF(false);
    }
}
