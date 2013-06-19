package org.ekkoproject.android.player.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class ChildViewPager extends ViewPager {
    private static final int INVALID_POINTER = -1;

    private boolean focusDetermined = false;
    private int touchSlop = 0;
    private int pointerId = 0;
    private float initialX = 0;
    private float initialY = 0;

    public ChildViewPager(final Context context) {
        super(context);
        init();
    }

    public ChildViewPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        this.touchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
            // disable parents & reset tracking data on initial event
            getParent().requestDisallowInterceptTouchEvent(true);
            this.focusDetermined = false;
            this.pointerId = ev.getPointerId(0);
            this.initialX = MotionEventCompat.getX(ev, 0);
            this.initialY = MotionEventCompat.getY(ev, 0);
            break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        // attempt to determine focus
        if (!this.focusDetermined) {
            switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // check for an actual scroll
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, this.pointerId);
                if (pointerIndex != INVALID_POINTER) {
                    final float dx = MotionEventCompat.getX(ev, pointerIndex) - this.initialX;
                    final float dy = MotionEventCompat.getY(ev, pointerIndex) - this.initialY;

                    // we are scrolling horizontally
                    if (Math.abs(dx) > this.touchSlop && Math.abs(dx * 0.5f) > Math.abs(dy)) {
                        // only block parent focus when we aren't scrolling past
                        // a bound
                        final PagerAdapter adapter = this.getAdapter();
                        final int curItem = this.getCurrentItem();
                        final int count = adapter != null ? adapter.getCount() : 0;
                        if ((dx < 0.0f && curItem < count - 1) || (dx > 0.0f && curItem > 0)) {
                            // prevent parent focus
                            getParent().requestDisallowInterceptTouchEvent(true);
                        } else if ((dx > 0.0f && curItem <= 0) || (dx < 0.0f && curItem >= count - 1)) {
                            // allow parent focus
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }

                        // we have determined the focus, quit checking
                        this.focusDetermined = true;
                    }
                    // vertical scroll, ignore it
                    else if (Math.abs(dy) > this.touchSlop) {
                        // allow parent focus
                        getParent().requestDisallowInterceptTouchEvent(false);
                        this.focusDetermined = true;
                    }
                }
                break;
            }
        }

        // trigger actual touch event
        return super.onTouchEvent(ev);
    }
}
