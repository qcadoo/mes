package com.qcadoo.model.api;

import java.util.List;
import java.util.Locale;

public interface ExpressionService {

    /**
     * Generates text to display in grid cell. If columnDefinition has expression - uses it, otherwise result is value of field
     * (or comma separated fields values when columDefinition has more than one field). Returns null when generated value is null.
     */
    String getValue(Entity entity, List<FieldDefinition> fieldDefinitions, Locale locale);

    /**
     * Evaluate expression value using entity fields values. Returns null when generated value is null.
     */
    String getValue(Entity entity, String expression, Locale locale);

}
