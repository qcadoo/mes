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
package com.qcadoo.mes.basic.util;

import com.google.common.base.Optional;
import com.qcadoo.mes.basic.constants.StaffFields;
import com.qcadoo.model.api.Entity;

public final class StaffNameExtractor {

    private StaffNameExtractor() {
    }

    public static Optional<String> extractNameAndSurname(final Entity staff) {
        return extract(staff, StaffFields.NAME, StaffFields.SURNAME);
    }

    public static Optional<String> extractSurnameAndName(final Entity staff) {
        return extract(staff, StaffFields.SURNAME, StaffFields.NAME);
    }

    private static Optional<String> extract(final Entity staff, final String firstFieldName, final String secondFieldName) {
        if (staff == null) {
            return Optional.absent();
        }
        String firstFieldValue = staff.getStringField(firstFieldName);
        String secondFieldValue = staff.getStringField(secondFieldName);
        return Optional.of(String.format("%s %s", firstFieldValue, secondFieldValue));
    }

}
