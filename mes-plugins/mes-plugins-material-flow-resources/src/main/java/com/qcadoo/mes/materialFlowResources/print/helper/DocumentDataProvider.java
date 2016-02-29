/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.materialFlowResources.print.helper;

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;

import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.materialFlowResources.constants.DocumentFields;
import com.qcadoo.model.api.Entity;

public final class DocumentDataProvider {

    private static final String L_LONG_DATE = "yyyy-MM-dd HH:mm:ss";

    private DocumentDataProvider() {
    }

    public static String name(final Entity document) {
        return document.getStringField(DocumentFields.NAME);
    }

    public static String number(final Entity document) {
        return document.getStringField(DocumentFields.NUMBER);
    }

    public static String time(final Entity document) {
        return new SimpleDateFormat(L_LONG_DATE).format(document.getDateField(DocumentFields.TIME));
    }

    public static String locationFrom(final Entity document) {
        Entity locationFrom = document.getBelongsToField(DocumentFields.LOCATION_FROM);
        return locationFrom != null
                ? locationFrom.getStringField(LocationFields.NUMBER) + " - " + locationFrom.getStringField(LocationFields.NAME)
                : StringUtils.EMPTY;
    }

    public static String locationTo(final Entity document) {
        Entity locationTo = document.getBelongsToField(DocumentFields.LOCATION_TO);
        return locationTo != null
                ? locationTo.getStringField(LocationFields.NUMBER) + " - " + locationTo.getStringField(LocationFields.NAME)
                : StringUtils.EMPTY;
    }

    public static String company(final Entity document) {
        Entity company = document.getBelongsToField(DocumentFields.COMPANY);
        return company != null ? company.getStringField(CompanyFields.NAME) : StringUtils.EMPTY;
    }

    public static String state(final Entity document) {
        return document.getStringField(DocumentFields.STATE);
    }

    public static String description(final Entity document) {
        return document.getStringField(DocumentFields.DESCRIPTION) != null ? document.getStringField(DocumentFields.DESCRIPTION)
                : StringUtils.EMPTY;
    }

}
