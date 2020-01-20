package com.ominous.tylerutils.util;

import android.content.Context;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.ominous.tylerutils.R;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

public class ViewUtils {

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
}
