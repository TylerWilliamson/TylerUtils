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

package com.ominous.tylerutils.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.webkit.WebView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

public class ColorUtils {
    public static boolean isNightModeActive(Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void setNightMode(Context context) {
        setNightMode(context, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setNightMode(Context context, int mode) {
        //using a dummy WebView to avoid an Android bug regarding Dark Mode
        new WebView(context);

        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static int blendColors(int low, int high, double percent) {
        return Color.argb(
                255,
                (int) ((Color.red(low) * (100 - percent) / 100) + (Color.red(high) * percent / 100)),
                (int) ((Color.green(low) * (100 - percent) / 100) + (Color.green(high) * percent / 100)),
                (int) ((Color.blue(low) * (100 - percent) / 100) + (Color.blue(high) * percent / 100)));
    }

    public static int getDarkenedColor(int color) {
        return adjustBrightness(color, 0.75);
    }

    public static boolean isColorBright(int color) {
        return RGBtoHSP(color).perceivedBrightness > 0.5;
    }

    public static int adjustBrightness(int color, double amount) {
        HSPColor hsp = RGBtoHSP(color);
        hsp.perceivedBrightness *= amount;
        return hsp.toRGB();
    }

    public static int getColorForText(String text) {
        //TODO: Pastelify?
        return text == null ? 0xFFFFFFFF : text.hashCode() | 0xFF000000;
    }

    public static int getColorForBitmap(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);

        canvas.drawBitmap(bitmap, null, new Rect(0, 0, 1, 1), null);

        return newBitmap.getPixel(0, 0);
    }

    @ColorRes
    private static int getSystemAccentColorRes() {
        return Build.VERSION.SDK_INT >= 31 ? android.R.color.system_accent1_200 : 0;
    }

    @ColorInt
    private static int getAppAccentColor(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            TypedArray typedArray = context.getTheme()
                    .obtainStyledAttributes(new int[]{android.R.attr.colorAccent});

            int accentColor = typedArray.getColor(0, 0);
            typedArray.recycle();

            return accentColor;
        } else {
            return 0;
        }
    }

    @ColorInt
    public static int getAccentColor(Context context) {
        return getAccentColor(context, 0);
    }

    @ColorInt
    public static int getAccentColor(Context context, @ColorInt int defaultColor) {
        int systemAccentColorRes = getSystemAccentColorRes();
        int systemAccentColor = systemAccentColorRes != 0 ? ContextCompat.getColor(context, systemAccentColorRes) : 0;
        int appAccentColor = getAppAccentColor(context);

        return systemAccentColor != 0 ? systemAccentColor : appAccentColor != 0 ? appAccentColor : defaultColor;
    }

    @ColorRes
    public static int getAccentColorRes(@ColorRes int defaultColorRes) {
        int systemAccentColor = getSystemAccentColorRes();

        return systemAccentColor != 0 ? systemAccentColor : defaultColorRes;
    }

    //Based on public domain function by Darel Rex Finley, 2006
    //http://alienryderflex.com/hsp.html
    public static HSPColor RGBtoHSP(int color) {
        double red = Color.red(color) / 255.0;
        double green = Color.green(color) / 255.0;
        double blue = Color.blue(color) / 255.0;
        double alpha = Color.alpha(color) / 255.0;

        double h, s, p = Math.sqrt(HSPColor.pR * red * red + HSPColor.pG * green * green + HSPColor.pB * blue * blue);

        if (Math.abs(red - green) < 0.00001 && Math.abs(red - blue) < 0.00001) {
            return new HSPColor(0, 0, p, alpha);
        }
        if (red >= green && red >= blue) {   //  red is largest
            if (blue >= green) {
                h = 6. / 6. - 1. / 6. * (blue - green) / (red - green);
                s = 1. - green / red;
            } else {
                h = 0. / 6. + 1. / 6. * (green - blue) / (red - blue);
                s = 1. - blue / red;
            }
        } else if (green >= red && green >= blue) {   //  green is largest
            if (red >= blue) {
                h = 2. / 6. - 1. / 6. * (red - blue) / (green - blue);
                s = 1. - blue / green;
            } else {
                h = 2. / 6. + 1. / 6. * (blue - red) / (green - red);
                s = 1. - red / green;
            }
        } else {   //  blue is largest
            if (green >= red) {
                h = 4. / 6. - 1. / 6. * (green - red) / (blue - red);
                s = 1. - red / blue;
            } else {
                h = 4. / 6. + 1. / 6. * (red - green) / (blue - green);
                s = 1. - green / blue;
            }
        }

        return new HSPColor(h, s, p, alpha);
    }

    public static class HSPColor {
        private final static double pR = .299, pG = .587, pB = .114;
        private final double saturation, alpha, hue;
        private double perceivedBrightness;

        public HSPColor(double hue, double saturation, double perceivedBrightness, double alpha) {
            this.hue = hue;
            this.saturation = saturation;
            this.perceivedBrightness = perceivedBrightness;
            this.alpha = alpha;
        }

        //Based on public domain function by Darel Rex Finley, 2006
        //http://alienryderflex.com/hsp.html
        public int toRGB() {
            double minOverMax = 1 - saturation;
            double r, g, b, hue = this.hue;
            if (minOverMax > 0) {
                double part;
                if (hue < 0.166666666666667D) { //R>G>B
                    hue = 6 * hue;
                    part = 1 + hue * (1 / minOverMax - 1);
                    b = perceivedBrightness / Math.sqrt(pR / minOverMax / minOverMax + pG * part * part + pB);
                    r = b / minOverMax;
                    g = b + hue * (r - b);
                } else if (hue < 0.333333333333333D) { //G>R>B
                    hue = 6 * (-hue + 0.333333333333333D);
                    part = 1 + hue * (1 / minOverMax - 1);
                    b = perceivedBrightness / Math.sqrt(pG / minOverMax / minOverMax + pR * part * part + pB);
                    g = b / minOverMax;
                    r = b + hue * (g - b);
                } else if (hue < 0.5D) {   //  G>B>R
                    hue = 6 * (hue - 0.333333333333333D);
                    part = 1 + hue * (1 / minOverMax - 1);
                    r = perceivedBrightness / Math.sqrt(pG / minOverMax / minOverMax + pB * part * part + pR);
                    g = r / minOverMax;
                    b = r + hue * (g - r);
                } else if (hue < 0.666666666666667D) { //B>G>R
                    hue = 6 * (-hue + 0.666666666666667D);
                    part = 1 + hue * (1 / minOverMax - 1);
                    r = perceivedBrightness / Math.sqrt(pB / minOverMax / minOverMax + pG * part * part + pR);
                    b = r / minOverMax;
                    g = r + hue * (b - r);
                } else if (hue < 0.833333333333333D) { //B>R>G
                    hue = 6 * (hue - 0.666666666666667D);
                    part = 1 + hue * (1 / minOverMax - 1);
                    g = perceivedBrightness / Math.sqrt(pB / minOverMax / minOverMax + pR * part * part + pG);
                    b = g / minOverMax;
                    r = g + hue * (b - g);
                } else { //R>B>G
                    hue = 6 * (-hue + 1D);
                    part = 1 + hue * (1 / minOverMax - 1);
                    g = perceivedBrightness / Math.sqrt(pR / minOverMax / minOverMax + pB * part * part + pG);
                    r = g / minOverMax;
                    b = g + hue * (r - g);
                }
            } else {
                if (hue < 0.166666666666667D) { //R>G>B
                    hue = 6 * (hue - 0D);
                    r = Math.sqrt(perceivedBrightness * perceivedBrightness / (pR + pG * hue * hue));
                    g = r * hue;
                    b = 0;
                } else if (hue < 0.333333333333333D) { //G>R>B
                    hue = 6 * (-hue + 0.333333333333333D);
                    g = Math.sqrt(perceivedBrightness * perceivedBrightness / (pG + pR * hue * hue));
                    r = g * hue;
                    b = 0;
                } else if (hue < 0.5D) { //G>B>R
                    hue = 6 * (hue - 0.333333333333333D);
                    g = Math.sqrt(perceivedBrightness * perceivedBrightness / (pG + pB * hue * hue));
                    b = g * hue;
                    r = 0;
                } else if (hue < 0.666666666666667D) { //B>G>R
                    hue = 6 * (-hue + 0.666666666666667D);
                    b = Math.sqrt(perceivedBrightness * perceivedBrightness / (pB + pG * hue * hue));
                    g = b * hue;
                    r = 0;
                } else if (hue < 0.833333333333333D) { //B>R>G
                    hue = 6 * (hue - 0.666666666666667D);
                    b = Math.sqrt(perceivedBrightness * perceivedBrightness / (pB + pR * hue * hue));
                    r = b * hue;
                    g = 0;
                } else { //R>B>G
                    hue = 6 * (-hue + 1D);
                    r = Math.sqrt(perceivedBrightness * perceivedBrightness / (pR + pB * hue * hue));
                    b = r * hue;
                    g = 0;
                }
            }
            return Color.rgb((int) (r * 255), (int) (g * 255), (int) (b * 255)) | ((int) (alpha * 255) << 24);
        }
    }
}
