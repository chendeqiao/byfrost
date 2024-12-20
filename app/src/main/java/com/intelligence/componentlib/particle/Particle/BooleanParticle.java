package com.intelligence.componentlib.particle.Particle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.intelligence.componentlib.particle.Factory.BooleanFactory;

import java.util.Random;

public class BooleanParticle extends Particle{
    static Random random = new Random();
    float radius = BooleanFactory.PART_WH;
    float alpha;
    Rect mBound;

    public BooleanParticle(int color, float x, float y, Rect bound) {
        super(color, x, y);
        mBound = bound;
    }


    @Override
    protected void draw(Canvas canvas, Paint paint) {
        paint.setColor(color);
        paint.setAlpha((int) (Color.alpha(color) * alpha)); //这样透明颜色就不是黑色了
        canvas.drawCircle(cx, cy, radius, paint);
    }

    @Override
    protected void caculate(float factor) {
        cx = cx + factor * random.nextInt(mBound.width()) * (random.nextFloat() - 0.5f);
        cy = cy + factor * random.nextInt(mBound.height()) * (random.nextFloat() - 0.5f);

        radius = radius - factor * random.nextInt(2);

        alpha = (1f - factor) * (1 + random.nextFloat());
    }
}
