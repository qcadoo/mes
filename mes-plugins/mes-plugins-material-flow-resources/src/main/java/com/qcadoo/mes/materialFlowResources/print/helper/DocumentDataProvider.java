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

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.constants.AddressFields;
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
        Entity address = document.getBelongsToField(DocumentFields.ADDRESS);
        String result = company != null ? company.getStringField(CompanyFields.NAME) : StringUtils.EMPTY;
        if (address != null) {
            if (!result.isEmpty()) {
                result = result + "\n";
            }
            if (!Strings.isNullOrEmpty(address.getStringField(AddressFields.NUMBER))) {
                result = result + address.getStringField(AddressFields.NUMBER);
            }
            if (!Strings.isNullOrEmpty(address.getStringField(AddressFields.NAME))) {
                result = result + "\n" + address.getStringField(AddressFields.NAME);
            }
        }
        return result;
    }

    public static String state(final Entity document) {
        return document.getStringField(DocumentFields.STATE);
    }

    public static String description(final Entity document) {
        return document.getStringField(DocumentFields.DESCRIPTION) != null ? document.getStringField(DocumentFields.DESCRIPTION)
                : StringUtils.EMPTY;
    }

    public static String pzLocation(final Entity documentEntity) {
        Entity location = documentEntity.getBelongsToField(DocumentFields.LINKED_DOCUMENT_LOCATION);
        return location != null ? location.getStringField(LocationFields.NUMBER) + " - "
                + location.getStringField(LocationFields.NAME) : StringUtils.EMPTY;
    }

    public static String delivery(Entity documentEntity) {
        Entity delivery = documentEntity.getBelongsToField("delivery");
        return delivery != null ? delivery.getStringField("number") : StringUtils.EMPTY;
    }
}
