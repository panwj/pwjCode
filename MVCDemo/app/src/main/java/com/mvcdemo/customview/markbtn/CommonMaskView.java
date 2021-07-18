package com.mvcdemo.customview.markbtn;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.concurrent.atomic.AtomicBoolean;

public class CommonMaskView extends View {
    public Runnable mRunnable;
    public int alpha;
    public AtomicBoolean mAtomicBoolean;
    private int mCx;
    private int mCy;
    public int mEnd;
    public int mStart;
    private int mRadius;
    public Paint mPaint;
    public ValueAnimator mValueAnimator;
    private int mCount;
    public Handler mHandler;

    private class MyHandler extends Handler {
        private CommonMaskView mMaskView;

        MyHandler(CommonMaskView commonMaskView) {
            this.mMaskView = commonMaskView;
        }

        public final void handleMessage(Message message) {
            this.mMaskView.start();
        }
    }

    private class MyRunnable implements Runnable {
        private CommonMaskView mMaskView;

        MyRunnable(CommonMaskView commonMaskView) {
            this.mMaskView = commonMaskView;
        }

        public final void run() {
            this.mMaskView.mAtomicBoolean.set(true);
            this.mMaskView.start();
        }
    }

    private class MyAnimatorUpdateListener implements AnimatorUpdateListener {
        private CommonMaskView mMaskView;

        MyAnimatorUpdateListener(CommonMaskView commonMaskView) {
            this.mMaskView = commonMaskView;
        }

        public final void onAnimationUpdate(ValueAnimator valueAnimator) {
            float floatValue = (float) valueAnimator.getAnimatedValue();
            this.mMaskView.mRadius = (int) floatValue;
            this.mMaskView.alpha = (int) ((1.0f - ((floatValue - ((float) this.mMaskView.mStart)) / ((float) (this.mMaskView.mEnd - this.mMaskView.mStart)))) * 255.0f);
            this.mMaskView.mPaint.setAlpha(this.mMaskView.alpha);
            this.mMaskView.invalidate();
        }
    }

    private class MaAnimatorListerAdapter extends AnimatorListenerAdapter {
        private /* synthetic */ CommonMaskView mMaskView;

        MaAnimatorListerAdapter(CommonMaskView commonMaskView) {
            this.mMaskView = commonMaskView;
        }

        public final void onAnimationEnd(Animator animator) {
            this.mMaskView.mHandler.sendEmptyMessageDelayed(0, 300);
        }
    }

    public CommonMaskView(Context context) {
        this(context, null);
    }

    public CommonMaskView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CommonMaskView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAtomicBoolean = null;
        this.mCx = 0;
        this.mCy = 0;
        this.alpha = 0;
        this.mEnd = 0;
        this.mStart = 0;
        this.mRadius = 0;
        this.mHandler = new MyHandler(this);
        this.mRunnable = new MyRunnable(this);
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.FILL);
        this.mPaint.setColor(Color.argb(16, 31, 50, 220));
        this.mAtomicBoolean = new AtomicBoolean(false);
        this.mStart = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60.0f, getContext().getResources().getDisplayMetrics());
    }

    public final void showRipple() {
        postDelayed(this.mRunnable, 500);
    }

    public final void start() {
        this.mCount++;
        if (this.mCount <= 2) {
            this.mCx = (getRight() - getLeft()) / 2;
            this.mCy = (getBottom() - getTop()) / 2;
            this.mEnd = getWidth() / 2;
            if (this.mCx != 0 && this.mCy != 0 && this.mEnd != 0) {
                this.mValueAnimator = ValueAnimator.ofFloat(this.mStart, (float) this.mEnd);
                this.mValueAnimator.setDuration(600);
                this.mValueAnimator.addUpdateListener(new MyAnimatorUpdateListener(this));
                this.mValueAnimator.addListener(new MaAnimatorListerAdapter(this));
                this.mValueAnimator.start();
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mCx != 0 && this.mCy != 0 && this.mEnd != 0 && this.mAtomicBoolean.get()) {
            canvas.drawCircle((float) this.mCx, (float) this.mCy, (float) this.mRadius, this.mPaint);
        }
    }

    public void setRippleColor(int color) {
        this.mPaint.setColor(color);
        invalidate();
    }
}
