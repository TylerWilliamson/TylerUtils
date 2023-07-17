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

package com.ominous.tylerutils.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class BitmapUtils {
    public static Bitmap generateDefaultImage(String text, int imageSize) {
        int textSize = (int) (imageSize * 0.6);

        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        String letter = text == null || text.length() == 0 ? " " : text.toUpperCase().replaceAll("[^A-Z0-9]", "").substring(0, 1);
        int bitmapColor = ColorUtils.getColorForText(text);

        Rect textBounds = new Rect();
        Paint textPaint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(bitmapColor);

        textPaint.setColor(ColorUtils.isColorBright(bitmapColor) ? 0xFF000000 : 0xFFFFFFFF);
        textPaint.setTextSize(textSize);
        textPaint.getTextBounds(letter, 0, 1, textBounds);
        canvas.drawText(letter, 0, 1, imageSize / 2f + (textBounds.left - textBounds.right) / 2f, imageSize / 2f
                + (textBounds.bottom - textBounds.top) / 2f, textPaint);

        return bitmap;
    }

    public static Bitmap squarify(Bitmap bitmap, int color) {
        int dim = Math.max(bitmap.getWidth(), bitmap.getHeight());
        Bitmap newBitmap = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(color);
        canvas.drawBitmap(bitmap, (dim - bitmap.getWidth()) / 2f, (dim - bitmap.getHeight()) / 2f, null);

        return newBitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable, int color) {
        Bitmap bitmap = null;

        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }

        return bitmap;
    }
}
