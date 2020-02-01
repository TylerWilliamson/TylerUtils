package com.ominous.tylerutils.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatDelegate;

public class ColorUtils {

    public static boolean isNightModeActive(Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void setNightMode(Context context) {
        setNightMode(context,AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
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
        return Color.argb(255, (int) (Color.red(color) * 0.75), (int) (Color.green(color) * 0.75), (int) (Color.blue(color) * 0.75));
    }

    public static boolean isColorBright(int color) {
        //Luminosity method via https://stackoverflow.com/a/41335343
        return (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 > 0.5;
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
}
