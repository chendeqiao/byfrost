package com.intelligence.browser.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.intelligence.commonlib.tools.ScreenUtils;

public class SimpleRoundedImageView extends ImageView {


    private float cornerRadius = 12.0f;  // 默认圆角半径
    private Path path;
    private Paint paint;

    public SimpleRoundedImageView(Context context) {
        super(context);
        init();
    }

    public SimpleRoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleRoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        cornerRadius = ScreenUtils.dpToPx(getContext(),4);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (path != null) {
            path.reset();
            RectF rect = new RectF(0, 0, w, h);
            path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
            path.close();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            // 绘制圆角背景
            Drawable background = getBackground();
            if (background instanceof ColorDrawable) {
                paint.setColor(((ColorDrawable) background).getColor());
                canvas.save();
                canvas.clipPath(path);
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
                canvas.restore();
            } else if (background != null) {
                // 其他类型的 Drawable 直接绘制
                canvas.save();
                canvas.clipPath(path);
                background.setBounds(0, 0, getWidth(), getHeight());
                background.draw(canvas);
                canvas.restore();
            }

            // 绘制圆角图片
            canvas.save();
            canvas.clipPath(path);
            super.onDraw(canvas);
            canvas.restore();
        } catch (Exception e) {
            super.onDraw(canvas);
        }
    }

    // 提供设置圆角半径的方法
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        path.reset();
        RectF rect = new RectF(0, 0, getWidth(), getHeight());
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
        path.close();
        invalidate();  // 重新绘制
    }
}