/*
 *     Copyright 2020 - 2021 Tyler Williamson
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
