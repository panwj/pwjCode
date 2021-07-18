package com.mvcdemo.common.util;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;

/**
 * 彩蛋工具类
 */
public class ClickUtils {

    /**
     * @param handler           外界handler(为了减少handler的泛滥使用,最好全局传handler引用,如果没有就直接传 new Handler())
     * @param view     视图(任意控件)
     * @param delayMillis       长按时间,毫秒
     * @param onClickListener   点击回调的返回事件
     * @param longClickListener 长按回调的返回事件
     */

    public static void handleClickListener(final Handler handler, final View view, final long delayMillis, final View.OnClickListener onClickListener, final OnLongClickListener longClickListener) {

        final boolean[] mIsLongClicked = new boolean[1];

        view.setOnTouchListener(new OnTouchListener() {
            private int TOUCH_MAX = 50;
            private int mLastMotionX;
            private int mLastMotionY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(r);
                        if (!mIsLongClicked[0] && onClickListener != null) {
                            onClickListener.onClick(view);
                        }
                        mIsLongClicked[0] = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(mLastMotionX - x) > TOUCH_MAX
                                || Math.abs(mLastMotionY - y) > TOUCH_MAX) {
                            // 移动误差阈值
                            // xy方向判断
                            // 移动超过阈值，则表示移动了,就不是长按(看需求),移除 已有的Runnable回调
                            handler.removeCallbacks(r);
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        // 每次按下重新计时
                        // 按下前,先移除 已有的Runnable回调,防止用户多次单击导致多次回调长按事件的bug
                        handler.removeCallbacks(r);
                        mLastMotionX = x;
                        mLastMotionY = y;
                        // 按下时,开始计时
                        handler.postDelayed(r, delayMillis);
                        break;
                }
                return true;
            }

            private Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (longClickListener != null) {// 回调给用户,用户可能传null,需要判断null
                        mIsLongClicked[0] = true;
                        longClickListener.onLongClick(view);
                    }
                }
            };
        });
    }

}
