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

import android.content.Context;
import android.icu.text.DateFormat;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.os.Build;
import android.provider.Settings;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LocaleUtils {
    private static boolean shouldOverride24HourFormat(Context context) {
        String overrideLocale24Hour = Settings.System.getString(context.getContentResolver(), Settings.System.TIME_12_24);

        return overrideLocale24Hour != null && overrideLocale24Hour.equals("24");
    }

    public static String formatHour(Context context, Locale locale, Date date, java.util.TimeZone timeZone) {
        if (shouldOverride24HourFormat(context)) {
            return formatTime(context, locale, date, timeZone);
        } else {
            if (Build.VERSION.SDK_INT >= 24) {
                TimeZone icuTimeZone = TimeZone.getTimeZone(timeZone.getID());

                DateFormat df = DateFormat.getInstanceForSkeleton("j", locale);
                df.setTimeZone(icuTimeZone);

                return df.format(date);
            } else {
                java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("h", locale);
                df.setTimeZone(timeZone);

                return df.format(date);
            }
        }
    }

    public static String formatTime(Context context, Locale locale, Date date, java.util.TimeZone timeZone) {
        if (Build.VERSION.SDK_INT >= 24) {
            TimeZone icuTimeZone = TimeZone.getTimeZone(timeZone.getID());

            DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, locale);

            if (shouldOverride24HourFormat(context) && df instanceof SimpleDateFormat) {
                DateFormat df2 = DateFormat.getInstanceForSkeleton(
                        ((SimpleDateFormat) df).toPattern()
                                .replaceAll("[hj]", "H"), locale);

                df2.setTimeZone(icuTimeZone);
                return df2.format(date);
            } else {
                df.setTimeZone(icuTimeZone);
                return df.format(date);
            }
        } else {
            java.text.DateFormat df = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, locale);

            if (shouldOverride24HourFormat(context) && df instanceof java.text.SimpleDateFormat) {
                java.text.SimpleDateFormat df2 = new java.text.SimpleDateFormat(
                        ((java.text.SimpleDateFormat) df).toPattern()
                                .replaceAll("[hj]", "H"), locale);

                df2.setTimeZone(timeZone);
                return df2.format(date);
            } else {
                df.setTimeZone(timeZone);
                return df.format(date);
            }
        }
    }

    public static String formatDateTime(Context context, Locale locale, Date date, java.util.TimeZone timeZone) {
        if (Build.VERSION.SDK_INT >= 24) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, locale);
            TimeZone icuTimeZone = TimeZone.getTimeZone(timeZone.getID());

            if (df instanceof SimpleDateFormat) {
                String pattern = ((SimpleDateFormat) df).toPattern().replaceAll(":s+", "");

                if (shouldOverride24HourFormat(context)) {
                    pattern = pattern
                            .replaceAll("[hj]", "H");
                }

                SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);

                sdf.setTimeZone(icuTimeZone);
                return sdf.format(date);
            } else {
                df.setTimeZone(icuTimeZone);
                return df.format(date);
            }
        } else {
            java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.LONG, locale);

            if (df instanceof java.text.SimpleDateFormat) {
                String pattern = ((java.text.SimpleDateFormat) df).toPattern().replaceAll(":s+", "");

                if (shouldOverride24HourFormat(context)) {
                    pattern = pattern
                            .replaceAll("[hj]", "H");
                }

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern, locale);

                sdf.setTimeZone(timeZone);
                return sdf.format(date);
            } else {
                df.setTimeZone(timeZone);
                return df.format(date);
            }
        }
    }

    /**
     * @Deprecated Use formatTime instead
     */
    public static String formatHourLong(Context context, Locale locale, Date date, java.util.TimeZone timeZone) {
        return formatTime(context, locale, date, timeZone);
    }

    public static long getStartOfDay(Date date, java.util.TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
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
                return getDecimalString(locale, percentage * 100, 0) + " %";
            case "he":
            case "tr":
                return "%" + getDecimalString(locale, percentage * 100, 0);
            default:
                return getDecimalString(locale, percentage * 100, 0) + "%";
        }
    }

    public static String getDecimalString(Locale locale, double value, int decimalPlaces) {
        return decimalPlaces == 0 ? Integer.toString((int) value) : String.format(locale, "%." + decimalPlaces + "f", value);
    }

    public static double parseDouble(Locale locale, String doubleString) {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                return NumberFormat.getInstance(locale)
                        .parse(doubleString == null ? "0" : doubleString)
                        .doubleValue();
            } else {
                Number number = java.text.NumberFormat.getInstance(locale)
                        .parse(doubleString == null ? "0" : doubleString);
                return number == null ? 0 : number.doubleValue();
            }
        } catch (ParseException e) {
            return 0;
        }
    }
}
