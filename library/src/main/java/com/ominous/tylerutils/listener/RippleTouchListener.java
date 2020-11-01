package com.ominous.tylerutils.listener;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.ominous.tylerutils.card.BaseCardView;

@RequiresApi(api = 21)
public class RippleTouchListener implements View.OnTouchListener {
    private final static int[]
            STATE_PRESSED = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled},
            STATE_DEFAULT = new int[]{};

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Drawable background = v.getBackground(),
                foreground = v instanceof BaseCardView ? ((BaseCardView) v).getForeground() : null;

        RippleDrawable rippleDrawable = (RippleDrawable) (background instanceof RippleDrawable ? background : foreground instanceof RippleDrawable ? foreground : null);

        if (rippleDrawable != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    rippleDrawable.setHotspot(event.getX(), event.getY());
                    rippleDrawable.setState(STATE_PRESSED);
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                case MotionEvent.ACTION_CANCEL:
                    rippleDrawable.setState(STATE_DEFAULT);
                default:
                    break;
            }
        }
        return true;
    }
}
