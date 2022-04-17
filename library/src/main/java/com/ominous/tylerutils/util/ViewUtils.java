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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.ominous.tylerutils.R;

import java.lang.reflect.Field;
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
                .make(view, text, duration)
                .setTextColor(ContextCompat.getColor(context, R.color.color_white_regular))
                .setActionTextColor(ContextCompat.getColor(context, R.color.color_white))
                .setBackgroundTint(ContextCompat.getColor(context, R.color.snackbar_background));
    }

    public static Snackbar makeSnackbar(View view, @StringRes int textResId, int duration) {
        return makeSnackbar(view, view.getContext().getString(textResId), duration);
    }

    //Setting DrawableStart in XML does not work for Vectors
    public static void setDrawable(TextView textview, @DrawableRes int drawableRes, @ColorInt int color, int flagSide) {
        Drawable drawableStart,
                drawableEnd,
                drawableTop,
                drawableBottom,
                drawable = ContextCompat.getDrawable(textview.getContext(), drawableRes);

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
                textview.setCompoundDrawables(drawableEnd, drawableTop, drawableStart, drawableBottom);
            } else {
                textview.setCompoundDrawables(drawableStart, drawableTop, drawableEnd, drawableBottom);
            }
        }
    }

    public static void toggleKeyboardState(View v, boolean open) {
        InputMethodManager inputMethodManager = ContextCompat.getSystemService(v.getContext(), InputMethodManager.class);

        if (inputMethodManager != null) {
            if (open) {
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            } else {
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    public static String editTextToString(EditText editText) {
        Editable text = editText.getText();

        return text == null ? "" : text.toString();
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("DiscouragedPrivateApi")
    public static void setEditTextCursorColor(EditText editText, int cursorColor) {
        if (Build.VERSION.SDK_INT >= 29) {
            Drawable cursor = editText.getTextCursorDrawable();
            Drawable cursorHandle = editText.getTextSelectHandle();
            Drawable cursorHandleLeft = editText.getTextSelectHandleLeft();
            Drawable cursorHandleRight = editText.getTextSelectHandleRight();

            PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(cursorColor, PorterDuff.Mode.SRC_IN);

            cursor.setColorFilter(colorFilter);
            cursorHandle.setColorFilter(colorFilter);
            cursorHandleLeft.setColorFilter(colorFilter);
            cursorHandleRight.setColorFilter(colorFilter);

            editText.setTextCursorDrawable(cursor);
            editText.setTextSelectHandle(cursorHandle);
            editText.setTextSelectHandleLeft(cursorHandleLeft);
            editText.setTextSelectHandleRight(cursorHandleRight);
        } else {
            try {
                //TODO: Create Generic methods for reflection
                Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                fCursorDrawableRes.setAccessible(true);
                int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);

                // Left
                Field cursorDrawableLeftResField = TextView.class.getDeclaredField("mTextSelectHandleLeftRes");
                cursorDrawableLeftResField.setAccessible(true);
                int mCursorDrawableLeftRes = cursorDrawableLeftResField.getInt(editText);

                // Right
                Field cursorDrawableRightResField = TextView.class.getDeclaredField("mTextSelectHandleRightRes");
                cursorDrawableRightResField.setAccessible(true);
                int mCursorDrawableRightRes = cursorDrawableRightResField.getInt(editText);

                Field editorField = TextView.class.getDeclaredField("mEditor");
                editorField.setAccessible(true);
                Object editor = editorField.get(editText);
                Field cursorDrawableField = editor.getClass().getDeclaredField("mCursorDrawable");
                cursorDrawableField.setAccessible(true);

                Drawable[] drawables = new Drawable[3];
                Resources res = editText.getContext().getResources();
                drawables[0] = res.getDrawable(mCursorDrawableRes);
                drawables[1] = res.getDrawable(mCursorDrawableLeftRes);
                drawables[2] = res.getDrawable(mCursorDrawableRightRes);
                drawables[0].setColorFilter(cursorColor, PorterDuff.Mode.SRC_IN);
                drawables[1].setColorFilter(cursorColor, PorterDuff.Mode.SRC_IN);
                drawables[2].setColorFilter(cursorColor, PorterDuff.Mode.SRC_IN);
                cursorDrawableField.set(editor, drawables);
            } catch (IllegalAccessException e) {
                //
            } catch (NoSuchFieldException e) {
                //
            }
        }
    }

}
