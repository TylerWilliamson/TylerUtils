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

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static String linkify(CharSequence input, Pattern pattern, String defaultScheme) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer(input.length());
        String scheme, url, link;

        while (matcher.find()) {
            scheme = matcher.group(1);
            url = matcher.group(2);
            link = scheme == null || scheme.isEmpty() ? defaultScheme + "://" + url : matcher.group();

            matcher.appendReplacement(sb, String.format("<a href='%1$s'>%2$s</a>", link, matcher.group()));
        }

        matcher.appendTail(sb);

        return sb.toString();
    }

    public static String capitalizeEachWord(String text) {
        String[] words = text.split(" ");

        StringBuilder result = new StringBuilder();

        for (int i = 0, l = words.length; i < l; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1).toLowerCase());
        }

        return result.toString();
    }

    public static Spanned fromHtml(String text) {
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(text, 0);
        } else {
            //noinspection deprecation
            return Html.fromHtml(text);
        }
    }
}
