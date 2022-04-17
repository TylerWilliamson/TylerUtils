/*
 *     Copyright 2020 - 2022 Tyler Williamson
 *
 *     This file is part of TylerUtils.
 *
 *     TylerUtils is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     TylerUtils is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ominous.tylerutils.card;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ominous.tylerutils.R;
import com.ominous.tylerutils.util.ColorUtils;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public abstract class BaseCardView extends CardView implements View.OnClickListener {
    private final ValueAnimator pressedAnimation;
    private OnTouchListener onTouchListener = null;

    public BaseCardView(Context context) {
        this(context, null, 0);
    }

    public BaseCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.style.AppTheme_Card);

        this.setOnClickListener(this);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        this.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background));
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
                                ContextCompat.getColor(getContext(), R.color.card_background),
                                ContextCompat.getColor(getContext(), R.color.card_background_pressed),
                                animation.getAnimatedFraction() * 100));
            }
        });

        //The work is purely visual
        //noinspection "ClickableViewAccessibility"
        super.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressedAnimation.start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    pressedAnimation.reverse();
            }

            return onTouchListener != null && onTouchListener.onTouch(v, event);
        });
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        onTouchListener = l;
    }
}
