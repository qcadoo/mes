package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents;

import com.google.common.base.Strings;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class XlsDataType {

    /**
     * Date format.
     */
    public static final String L_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Date-time format.
     */
    public static final String L_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private XlsDataType(){}
    
    public static String getValue(String value) {
        return Strings.nullToEmpty(value);
    }

    public static String getValue(NumberService numberService, BigDecimal value) {
        if (value == null) {
            return "";
        }
        return numberService.formatWithMinimumFractionDigits(value, 0);
    }

    public static String getValue(Date value, boolean dateOnly) {
        if (value == null) {
            return "";
        }
        if (dateOnly) {
            SimpleDateFormat df = new SimpleDateFormat(L_DATE_FORMAT);
            return df.format(value);
        } else {
            SimpleDateFormat df = new SimpleDateFormat(L_DATE_TIME_FORMAT);
            return df.format(value);
        }
    }

    public static String getValue(Integer value) {
        if (value == null) {
            return "";
        }
        int hours = value / 3600;
        int minutes = (value % 3600) / 60;
        int seconds = value % 60;

        String timeString = String.format("%d:%02d:%02d", hours, minutes, seconds);

        return timeString;
    }

    public static String getValue(TranslationService translationService, Locale locale, String value) {
        return Strings.nullToEmpty(translationService.translate(value, locale));
    }

}
