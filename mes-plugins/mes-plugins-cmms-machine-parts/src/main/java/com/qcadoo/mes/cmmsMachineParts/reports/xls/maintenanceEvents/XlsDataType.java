package com.qcadoo.mes.cmmsMachineParts.reports.xls.maintenanceEvents;

import com.google.common.base.Strings;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class XlsDataType {

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
            SimpleDateFormat df = new SimpleDateFormat(DateUtils.L_DATE_FORMAT);
            String date = df.format(value);
            return date;
        } else {
            return DateUtils.toDateTimeString(value);
        }
    }

    public static String getValue(Integer value) {
        if (value == null) {
            return "";
        }
        int hours = value / 3600;
        int minutes = (value % 3600) / 60;
        int seconds = value % 60;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return timeString;
    }

    public static String getValue(TranslationService translationService, Locale locale, String value) {
        return Strings.nullToEmpty(translationService.translate(value, locale));
    }

}
