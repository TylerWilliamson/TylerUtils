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

package com.ominous.tylerutils.util;

import android.content.Context;
import android.provider.Settings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LocaleUtils {
    private static final SimpleDateFormat sdfHour24h = new SimpleDateFormat("H", Locale.US);
    private static final SimpleDateFormat sdfHour12h = new SimpleDateFormat("ha", Locale.US);

    public static String formatHour(Context context, Locale locale, Date date, TimeZone timeZone) {
        if (is24HourFormat(context, locale)) {
            sdfHour24h.setTimeZone(timeZone);
            return sdfHour24h.format(date);
        } else {
            sdfHour12h.setTimeZone(timeZone);
            return sdfHour12h.format(date).replaceAll("[mM. ]", "");
        }
    }

    public static String formatDateTime(Context context, Locale locale, Date date, TimeZone timeZone) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, locale);

        if (df instanceof SimpleDateFormat) {
            String pattern = ((SimpleDateFormat) df).toPattern().replaceAll(":ss", "");

            if (is24HourFormat(context,locale)) {
                pattern = pattern.replaceAll(" a","").replaceAll("h","H");
            }

            SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);

            sdf.setTimeZone(timeZone);
            return sdf.format(date);
        } else {
            df.setTimeZone(timeZone);
            return df.format(date);
        }
    }

    private static boolean is24HourFormat(Context context, Locale locale) {
        String overrideLocale24Hour = Settings.System.getString(context.getContentResolver(),Settings.System.TIME_12_24);

        if (overrideLocale24Hour != null) {
            return overrideLocale24Hour.equals("24");
        }

        DateFormat natural = DateFormat.getTimeInstance(DateFormat.LONG, locale);
        return !(natural instanceof SimpleDateFormat) || ((SimpleDateFormat) natural).toPattern().indexOf('H') >= 0;
    }

    public static String getPercentageString(Locale locale, double percentage) {
        switch (locale.getLanguage()) {
            case "cs":
            case "sk":
            case "fi":
            case "fr":
            case "es":
            case "sv":
            case "de":
                return getDecimalString(locale,percentage * 100, 0) + " %";
            case "he":
            case "tr":
                return "%" + getDecimalString(locale,percentage * 100, 0);
            default:
                return getDecimalString(locale,percentage * 100, 0) + "%";
        }
    }

    public static String getDecimalString(Locale locale, double value, int decimalPlaces) {
        return decimalPlaces == 0 ? Integer.toString((int) value) : String.format(locale, "%." + decimalPlaces + "f", value);
    }
}
