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
