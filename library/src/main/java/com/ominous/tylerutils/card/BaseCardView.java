package com.ominous.tylerutils.card;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.ominous.tylerutils.R;
import com.ominous.tylerutils.util.ColorUtils;

public abstract class BaseCardView extends CardView implements View.OnClickListener {
    private ValueAnimator pressedAnimation;
    private OnTouchListener onTouchListener = null;

    public BaseCardView(Context context) {
        this(context, null, 0);
    }

    public BaseCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    //We call performClick, and the work is purely visual
    @SuppressLint("ClickableViewAccessibility")
    public BaseCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.style.AppTheme_Card);

        this.setOnClickListener(this);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        this.setCardBackgroundColor(ContextCompat.getColor(context,R.color.card_background));
        this.setRadius(getResources().getDimensionPixelSize(R.dimen.margin_quarter));
        this.setCardElevation(context.getResources().getDimensionPixelSize(R.dimen.card_elevation));

        this.pressedAnimation = ValueAnimator.ofInt(
                context.getResources().getDimensionPixelSize(R.dimen.card_elevation),
                context.getResources().getDimensionPixelSize(R.dimen.card_elevation_pressed))
                .setDuration(200);

        this.pressedAnimation.addUpdateListener(animation -> {
            BaseCardView.this.setCardElevation((Integer) animation.getAnimatedValue());

            if (ColorUtils.isNightModeActive(getContext())) {
                BaseCardView.this.setCardBackgroundColor(
                        ColorUtils.blendColors(
                                ContextCompat.getColor(getContext(),R.color.card_background),
                                ContextCompat.getColor(getContext(),R.color.card_background_pressed),
                                animation.getAnimatedFraction() * 100));
            }
        });

        super.setOnTouchListener((v, event) -> {
            if (pressedAnimation != null) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pressedAnimation.start();
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                    case MotionEvent.ACTION_CANCEL:
                        pressedAnimation.reverse();
                }
            }

            return onTouchListener != null && onTouchListener.onTouch(v, event);
        });
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        onTouchListener = l;
    }
}
