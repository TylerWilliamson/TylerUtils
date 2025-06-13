/*
 * Copyright 2020 - 2025 Tyler Williamson
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
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Build;

public class OpenCloseHandler {
    private OpenCloseState state = OpenCloseState.CLOSED;

    private final Animator openAnimator;
    private final Animator closeAnimator;
    private final long openDuration;
    private final long closeDuration;

    public OpenCloseHandler(Animator openAnimator, Animator closeAnimator) {
        this.openAnimator = openAnimator;
        this.closeAnimator = closeAnimator;
        this.openDuration = openAnimator.getDuration();
        this.closeDuration = closeAnimator.getDuration();

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

    public void open(){
        open(false);
    }

    public void open(boolean now) {
        switch (state) {
            case OPEN:
            case OPENING:
                break;
            case NULL:
            case CLOSED:
            case OPENING_PARTIAL:
                openAnimator.setDuration(now ? 0 : openDuration);
                openAnimator.start();
                break;
            case CLOSING:
            case CLOSING_PARTIAL:
                openAnimator.setDuration(now ? 0 : openDuration);
                closeAnimator.setDuration(now ? 0 : closeDuration);
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

    public void open(Float progress) {
        if (Build.VERSION.SDK_INT >= 22) {
            switch (state) {
                case OPEN:
                case OPENING:
                    break;
                case CLOSING:
                    openAnimator.end();
                case NULL:
                case CLOSED:
                    if (openAnimator instanceof ValueAnimator) {
                        ((ValueAnimator) openAnimator).setCurrentFraction(progress);
                    }

                    break;
            }
        }
    }

    public void close(){
        close(false);
    }

    public void close(boolean now) {
        switch (state) {
            case CLOSED:
            case CLOSING:
                break;
            case NULL:
            case OPEN:
            case CLOSING_PARTIAL:
                closeAnimator.setDuration(now ? 0 : closeDuration);
                closeAnimator.start();
                break;
            case OPENING:
            case OPENING_PARTIAL:
                openAnimator.setDuration(now ? 0 : openDuration);
                closeAnimator.setDuration(now ? 0 : closeDuration);

                //todo use animatorset
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

    public void close(Float progress) {
        if (Build.VERSION.SDK_INT >= 22) {
            switch (state) {
                case CLOSED:
                case CLOSING:
                    break;
                case OPENING:
                    closeAnimator.end();
                case NULL:
                case OPEN:
                    if (closeAnimator instanceof ValueAnimator) {
                        ((ValueAnimator) closeAnimator).setCurrentFraction(progress);
                    }

                    break;
            }
        }
    }

    public OpenCloseState getState() {
        return state;
    }
    public void setState(OpenCloseState state) {
        this.state = state;
    }
}
