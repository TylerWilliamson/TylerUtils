/*
 * Copyright 2020 - 2023 Tyler Williamson
 *
 * This file is part of TylerUtils.
 *
 * TylerUtils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TylerUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ominous.tylerutils.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

public class OpenCloseHandler {
    private OpenCloseState state = OpenCloseState.CLOSED;

    private final Animator openAnimator;
    private final Animator closeAnimator;

    public OpenCloseHandler(Animator openAnimator, Animator closeAnimator) {
        this.openAnimator = openAnimator;
        this.closeAnimator = closeAnimator;

        this.openAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setState(OpenCloseState.OPENING);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setState(OpenCloseState.OPEN);
            }
        });

        this.closeAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setState(OpenCloseState.CLOSING);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setState(OpenCloseState.CLOSED);
            }
        });
    }

    public void open() {
        switch (state) {
            case OPEN:
            case OPENING:
                break;
            case NULL:
            case CLOSED:
                openAnimator.start();
                break;
            case CLOSING:
                closeAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        openAnimator.start();
                        closeAnimator.removeListener(this);
                    }
                });
                closeAnimator.end();
        }
    }

    public void close() {
        switch (state) {
            case CLOSED:
            case CLOSING:
                break;
            case NULL:
            case OPEN:
                closeAnimator.start();
                break;
            case OPENING:
                openAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        closeAnimator.start();
                        openAnimator.removeListener(this);
                    }
                });
                openAnimator.end();
        }
    }

    public OpenCloseState getState() {
        return state;
    }
    public void setState(OpenCloseState state) {
        this.state = state;
    }
}
