package com.ominous.tylerutils.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class LocaleUtils {
    private static SimpleDateFormat sdfHour24h = new SimpleDateFormat("H", Locale.US);
    private static SimpleDateFormat sdfHour12h = new SimpleDateFormat("ha", Locale.US);

    public static String formatHour(Locale locale, Date date, TimeZone timeZone) {
        if (is24HourFormat(locale)) {
            sdfHour24h.setTimeZone(timeZone);
            return sdfHour24h.format(date);
        } else {
            sdfHour12h.setTimeZone(timeZone);
            return sdfHour12h.format(date).replaceAll("[mM. ]", "");
        }
    }

    public static String formatDateTime(Locale locale, Date date, TimeZone timeZone) {
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, locale);

        if (df instanceof SimpleDateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat(((SimpleDateFormat) df).toPattern().replaceAll(":ss", ""), locale);

            sdf.setTimeZone(timeZone);
            return sdf.format(date);
        } else {
            df.setTimeZone(timeZone);
            return df.format(date);
        }
    }

    private static boolean is24HourFormat(Locale locale) {
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
