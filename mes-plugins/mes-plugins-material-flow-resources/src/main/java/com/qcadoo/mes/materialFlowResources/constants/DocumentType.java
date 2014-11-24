/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.materialFlowResources.constants;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.qcadoo.model.api.Entity;

public enum DocumentType {

    RECEIPT("01receipt"), INTERNAL_INBOUND("02internalInbound"), INTERNAL_OUTBOUND("03internalOutbound"), RELEASE("04release"), TRANSFER(
            "05transfer");

    private final String value;

    private DocumentType(final String value) {
        this.value = value;
    }

    public String getStringValue() {
        return this.value;
    }

    public static DocumentType of(final Entity document) {
        Preconditions.checkArgument(document != null, "Passed entity have not to be null.");
        return parseString(document.getStringField(DocumentFields.TYPE));
    }

    public static DocumentType parseString(final String type) {
        for (DocumentType documentType : values()) {
            if (StringUtils.equalsIgnoreCase(type, documentType.getStringValue())) {
                return documentType;
            }
        }
        throw new IllegalArgumentException("Couldn't parse DocumentType from string '" + type + "'");
    }
}
