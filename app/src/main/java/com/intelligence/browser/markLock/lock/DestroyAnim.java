package com.intelligence.browser.markLock.lock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.renderscript.Int2;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DestroyAnim {


    private final Activity activity;
    private final int bgColor;


    private FrameLayout getDecorView(){
        return (FrameLayout) activity.getWindow().getDecorView();
    }

    public DestroyAnim(Activity activity, int bgColor) {
        this.activity = activity;
        this.bgColor = bgColor;
    }


    private final String TAG = "yzx_destroy_view";
    private void remoteFrontView(){
        FrameLayout container = (FrameLayout) activity.getWindow().getDecorView();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if(TAG.equals(child.getTag())){
                container.removeView(child);
                break;
            }
        }
    }


    public void start(final Runnable onComplete){
        if(isActivityFinish())
            return ;
        remoteFrontView();
        getDecorView().post(new Runnable() {
            @Override
            public void run() {
                if(isActivityFinish())
                    return ;
                int width = getDecorView().getWidth();
                int height = getDecorView().getHeight();
                if(width == 0 || height==0)
                    return ;
                DestroyView destroyView = new DestroyView(activity);
                destroyView.setClickable(true);
                destroyView.setTag(TAG);
                destroyView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
                destroyView.setDelegateView(getDecorView(), bgColor, width, height);
                getDecorView().addView(destroyView);
                destroyView.start(onComplete);
            }
        });
    }



    private boolean isActivityFinish(){
        if(activity==null)
            return true;
        try{
            if(activity.isFinishing())
                return true;
            else if(Build.VERSION.SDK_INT>=17 && activity.isDestroyed())
                return true;
            return false;
        }catch (Exception e){
            return true;
        }
    }


    /* ====================================================================== */




    private static class DestroyView extends View{
        public DestroyView(Context context) {
            super(context);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        public DestroyView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
        public DestroyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }




        private Bitmap bitmap;
        private View delegateView;
        private int bgColor;

        public void setDelegateView(View view, int bgColor, final int width, final int height){
            this.delegateView = view;
            this.bgColor = bgColor;
            if(bitmap != null && !bitmap.isRecycled())
                bitmap.recycle();
            delegateView.post(new Runnable() {
                public void run() {
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(bitmap);
                    delegateView.draw(canvas);
                }
            });

        }





        private boolean hasStarted = false;
        public void start(final Runnable onComplete){
            if(delegateView==null)
                return ;
            if(hasStarted)
                return ;
            hasStarted = true;
            delegateView.post(new Runnable() {
                public void run() {

                    Context context = delegateView.getContext();
                    if(context instanceof Activity){
                        Activity a = (Activity) context;
                        if(a.isFinishing())
                            return ;
                    }

                    initCenterPointsInfo();

                    ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener(){
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            invalidate();
                        }};
                    for (Map.Entry<Point, Animator> entry : animInfo.entrySet()) {
                        ValueAnimator anim = (ValueAnimator) entry.getValue();
                        anim.setInterpolator(new LinearInterpolator());
                        anim.setDuration(getRealDuration(indexInfo.get(entry.getKey()).x, indexInfo.get(entry.getKey()).y));
                        anim.setStartDelay(getRealDelay(indexInfo.get(entry.getKey()).x, indexInfo.get(entry.getKey()).y));
                        anim.addUpdateListener(listener);
                    }

                    AnimatorSet anim =  new AnimatorSet();
                    anim.playTogether(animInfo.values());
                    anim.addListener(new AnimatorListenerAdapter(){
                        public void onAnimationEnd(Animator animation) {
                            if(onComplete != null)
                                onComplete.run();
                        }});
                    anim.start();

                }
            });
        }


        private int getRealDelay(int x, int y){
            return 25 * y;
        }
        private int getRealDuration(int x, int y){
            return 140;
        }


        private void initCenterPointsInfo(){
            if(!centerPointList.isEmpty())
                return ;

            int viewWith = getWidth();
            if(viewWith==0)
                viewWith = getResources().getDisplayMetrics().widthPixels;
            block_width =viewWith/getHorizontalCount();
            block_height = block_width/5*3;
            int vertical_count = getHeight()/block_height+(((getHeight()%block_height)==0)? 0 : 1);

            for (int i = 0; i < vertical_count; i++) {
                for (int j = 0; j < getHorizontalCount(); j++) {
                    Point point = new Point();
                    int x = j * block_width + block_width/2;
                    int y = i * block_height + block_height/2;
                    point.set(x, y);
                    centerPointList.add(point);
                    indexInfo.put(point, new Int2(j, i));
                    animInfo.put(point, ValueAnimator.ofFloat(0f, 1f));
                }
            }
        }




        private RectF getClipRectByPointByProgress(Point point){
            RectF rect = new RectF();

            float progress = (float)(((ValueAnimator)animInfo.get(point)).getAnimatedValue());
            Int2 index = indexInfo.get(point);
            int left , right, top, bottom;

           if(isFirst(index)){
               left = 0;
               right = (int) (block_width * ((1-progress)));
               bottom = point.y+block_height/2;
               top = (int) (bottom - (block_height * (1-progress)));
           }else if(isLast(index)){
               right = delegateView.getWidth();
               left = (int) (right - (block_width * ((1-progress))));
               bottom = point.y+block_height/2;
               top = (int) (bottom - (block_height * (1-progress)));
           }else if(isCenter(index)){
               left = (int) (point.x-block_width/2+block_width/2*progress);
               right = 2 * (point.x - left) + left;
               bottom = point.y+block_height/2;
               top = (int) (bottom - (block_height * (1-progress)));
           }else{
               left = (int) (point.x-block_width/2+block_width/2*progress);
               right = 2 * (point.x - left) + left;
               bottom = point.y+block_height/2;
               top = (int) (bottom - (block_height * (1-progress)));
           }

            rect.set(left, top, right, bottom);
            return rect;
        }


        private int horizontal_count;
        private int getHorizontalCount(){
            if(horizontal_count!=0)
                return horizontal_count;
            int w = getResources().getDisplayMetrics().widthPixels;
            int h = getResources().getDisplayMetrics().heightPixels;
            return horizontal_count = (w > h ? 6 : 5);
        }
        private boolean isFirst(Int2 p){
            return p.x == 0;
        }
        private boolean isLast(Int2 p){
            return p.x == getHorizontalCount()-1;
        }
        private boolean isCenter(Int2 p){
            int hc = getHorizontalCount();
            if(hc % 2 == 0){
                return p.x == hc/2 || p.x == hc/2+1;
            }else
                return p.x==hc/2+1;
        }




        private final LinkedHashMap<Point, Int2> indexInfo = new LinkedHashMap<>();
        private final LinkedHashMap<Point, Animator> animInfo = new LinkedHashMap<>();
        private final ArrayList<Point> centerPointList = new ArrayList<>();

        private int block_height;
        private int block_width;
        private Path rectPath = new Path();


        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if(bitmap==null || !hasStarted || centerPointList.isEmpty())
                return ;

            canvas.drawColor(bgColor);

            rectPath.reset();
            for (Point point : centerPointList) {
                RectF rect = getClipRectByPointByProgress(point);
                rectPath.addRect(rect, Path.Direction.CW);
            }
            try{
                canvas.clipPath(rectPath);
            }catch (Exception e){/**/}



            canvas.drawBitmap(bitmap, 0, 0, null);

        }

    }


}
