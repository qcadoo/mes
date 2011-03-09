package com.qcadoo.model.api.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public final class DateUtils {

    private DateUtils() {
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String REPORT_DATE_TIME_FORMAT = "yyyy_MM_dd_HH_mm_ss";

    public static Date parseDate(final String dateExpression, final boolean roundToUp) throws ParseException {
        String[] dateExpressionParts = dateExpression.split("-");

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2000);
        cal.set(Calendar.MONTH, 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        boolean dayDefined = false;

        try {
            int year = Integer.parseInt(dateExpressionParts[0]);
            if (year > 2500) {
                throw new ParseException("wrong date", 1);
            }
            if (year < 1500) {
                return null;
            }
            cal.set(Calendar.YEAR, year);

            if (dateExpressionParts.length > 1) {
                int month = Integer.parseInt(dateExpressionParts[1]);
                if (month > 12 || month < 1) {
                    throw new ParseException("wrong date", 1);
                }
                cal.set(Calendar.MONTH, month - 1);
            } else {
                if (roundToUp) {
                    cal.set(Calendar.MONTH, cal.getActualMaximum(Calendar.MONTH));
                } else {
                    cal.set(Calendar.MONTH, cal.getActualMinimum(Calendar.MONTH));
                }
            }

            if (dateExpressionParts.length > 2) {
                int day = Integer.parseInt(dateExpressionParts[2]);
                if (day > 0) {
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    dayDefined = true;
                }
            }
            if (!dayDefined) {
                if (roundToUp) {
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                } else {
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
                }
            }
            return cal.getTime();
        } catch (NumberFormatException e) {
            throw new ParseException("wrong date", 1);
        }
    }

}
