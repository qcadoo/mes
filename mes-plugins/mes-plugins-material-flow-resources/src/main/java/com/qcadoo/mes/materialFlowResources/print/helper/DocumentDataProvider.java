package com.qcadoo.mes.materialFlowResources.print.helper;

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;

import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.model.api.Entity;

public class DocumentDataProvider {

    private static final String L_LONG_DATE = "yyyy-MM-dd HH:mm:ss";

    private DocumentDataProvider() {
    }

    public static String number(final Entity document) {
        return document.getStringField(DocumentFields.NUMBER);
    }

    public static String time(final Entity document) {
        return new SimpleDateFormat(L_LONG_DATE).format(document.getDateField(DocumentFields.TIME));
    }

    public static String locationFrom(final Entity document) {
        Entity locationFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        return locationFrom != null ? locationFrom.getStringField(LocationFields.NUMBER) + " - "
                + locationFrom.getStringField(LocationFields.NAME) : StringUtils.EMPTY;
    }

    public static String locationTo(final Entity document) {
        Entity locationTo = document.getBelongsToField(DocumentFields.LOCATION_TO);
        return locationTo != null ? locationTo.getStringField(LocationFields.NUMBER) + " - "
                + locationTo.getStringField(LocationFields.NAME) : StringUtils.EMPTY;
    }

    public static String company(final Entity document) {
        Entity company = document.getBelongsToField(DocumentFields.COMPANY);
        return company != null ? company.getStringField(CompanyFields.NAME) : StringUtils.EMPTY;
    }

    public static String state(final Entity document) {
        return document.getStringField(DocumentFields.STATE);
    }

    public static String description(final Entity document) {
        return document.getStringField(DocumentFields.DESCRIPTION);
    }

}
