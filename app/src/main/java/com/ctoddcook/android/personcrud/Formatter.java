package com.ctoddcook.android.personcrud;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is used to provide consistent formatting for birth dates and zip codes throughout the
 * app, and to avoid repeating formatting code in multiple places.
 * <p>
 * Created by C. Todd Cook on 9/15/2016.<br>
 * ctodd@ctoddcook.com
 */
class Formatter {
    private static final DateFormat mDateForm =
        SimpleDateFormat.getDateInstance(DateFormat.MEDIUM);
    private static final DateFormat mMLDateTimeForm =
        SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG);
    private static final NumberFormat mZipForm = new DecimalFormat("##00000");

    /**
     * Returns a String with a date formatted in MEDIUM DATE FORM.
     *
     * @param date The date to be formatted
     * @return A String with the formatted date
     */
    static String formatBirthDate(Date date) {
        return mDateForm.format(date);
    }

    /**
     * Returns a String with a date formatted in MEDIUM DATE FORM.
     *
     * @param date The date to be formatted, as a long
     * @return A String with the formatted date
     */
    static String formatBirthDate(long date) {
        return formatBirthDate(new Date(date));
    }

    /**
     * Returns a String with an integer formatted in 5 digits, with leading zeroes if needed. For
     * example, the integer 892 would be returned as "00892".
     *
     * @param zc The integer (presumably a zip code) to be formatted
     * @return A String with the formatted zip code
     */
    static String formatZipCode(int zc) {
        return mZipForm.format(zc);
    }

    /**
     * Returns a String with a date formatted into a MEDIUM date and long time. For example, a
     * returned value might be "Jan 12, 1952 3:30:16pm".
     *
     * @param date The date value to be formatted
     * @return A String with the formatted datetime
     */
    private static String formatMLDateTime(Date date) {
        return mMLDateTimeForm.format(date);
    }

    /**
     * Returns a String with a date formatted into a MEDIUM date and long time. For example, a
     * returned value might be "Jan 12, 1952 3:30:16pm".
     *
     * @param date The date value to be formatted, as a long.
     * @return A String with the formatted datetime
     */
    static String formatMLDateTime(long date) {
        return formatMLDateTime(new Date(date));
    }
}
