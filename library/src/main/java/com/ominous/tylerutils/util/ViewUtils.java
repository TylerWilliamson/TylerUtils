package com.ominous.tylerutils.util;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.ominous.tylerutils.R;

import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

public class ViewUtils {
    public static final int FLAG_START = 1, FLAG_END = 2, FLAG_TOP = 4, FLAG_BOTTOM = 8;

    public static Snackbar makeSnackbar(View view, String text, int duration) {
        Context context = view.getContext();

        return Snackbar
                .make(view,text,duration)
                .setTextColor(ContextCompat.getColor(context,R.color.color_white_regular))
                .setActionTextColor(ContextCompat.getColor(context,R.color.color_white))
                .setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_background));
    }

    public static Snackbar makeSnackbar(View view, @StringRes int textResId, int duration) {
        return makeSnackbar(view,view.getContext().getString(textResId),duration);
    }

    //Setting DrawableStart in XML does not work for Vectors
    public static void setDrawable(TextView textview, @DrawableRes int drawableRes, @ColorInt int color, int flagSide) {
        Drawable drawableStart,
                drawableEnd,
                drawableTop,
                drawableBottom,
                drawable = ContextCompat.getDrawable(textview.getContext(),drawableRes);

        if (drawable != null) {
            int size = (int) textview.getTextSize();
            drawable.setBounds(0, 0, size, size);

            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            Drawable[] currentDrawables = textview.getCompoundDrawables();

            drawableStart = ((flagSide & FLAG_START) > 0) ? drawable : currentDrawables[0];
            drawableTop = ((flagSide & FLAG_TOP) > 0) ? drawable : currentDrawables[1];
            drawableEnd = ((flagSide & FLAG_END) > 0) ? drawable : currentDrawables[2];
            drawableBottom = ((flagSide & FLAG_BOTTOM) > 0) ? drawable : currentDrawables[3];

            if (Build.VERSION.SDK_INT > 17 && TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
                textview.setCompoundDrawables(drawableEnd,drawableTop,drawableStart,drawableBottom);
            } else {
                textview.setCompoundDrawables(drawableStart,drawableTop,drawableEnd,drawableBottom);
            }
        }
    }

    public static void toggleKeyboardState(View v, boolean open) {
        InputMethodManager inputMethodManager = ContextCompat.getSystemService(v.getContext(),InputMethodManager.class);

        if (inputMethodManager != null) {
            if (open) {
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            } else {
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }
}
