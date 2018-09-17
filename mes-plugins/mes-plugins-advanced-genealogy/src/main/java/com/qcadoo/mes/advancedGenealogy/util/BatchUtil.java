/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
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
package com.qcadoo.mes.advancedGenealogy.util;

import com.qcadoo.mes.advancedGenealogy.constants.AdvancedGenealogyConstants;
import com.qcadoo.mes.advancedGenealogy.constants.BatchFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.types.BelongsToType;

/**
 * Util for common batch operations, like extracting batch number from belongsTo batch field.
 */
public final class BatchUtil {

    private BatchUtil() {
    }

    /**
     * Tries to extract the batch's number.
     * 
     * @param entity
     *            entity from which you want to extract batch's number
     * @param batchFieldName
     *            name of the belongsTo field pointing to the batch entity
     * @return batch number as a String or null if given entity is null or batch field is null.
     * @throws IllegalArgumentException
     *             if given field name doesn't correspond to belongs to field pointing to the batch model.
     */
    public static String extractNumberFrom(final Entity entity, final String batchFieldName) {
        return getBatchField(entity, batchFieldName, BatchFields.NUMBER);
    }

    /**
     * Tries to extract the batch's external number.
     * 
     * @param entity
     *            entity from which you want to extract batch's external number
     * @param batchFieldName
     *            name of the belongsTo field pointing to the batch entity
     * @return batch external number as a String or null if given entity is null or batch field is null.
     * @throws IllegalArgumentException
     *             if given field name doesn't correspond to belongs to field pointing to the batch model.
     */
    public static String extractExternalNumberFrom(final Entity entity, final String batchFieldName) {
        return getBatchField(entity, batchFieldName, BatchFields.EXTERNAL_NUMBER);
    }

    private static <T> T getBatchField(final Entity entity, final String batchFieldName, final String batchProperty) {
        Entity batchOrNull = getBatchFrom(entity, batchFieldName);
        if (batchOrNull == null) {
            return null;
        }
        return (T) batchOrNull.getField(batchProperty);
    }

    private static Entity getBatchFrom(final Entity entity, final String batchFieldName) {
        if (entity == null) {
            return null;
        }
        checkField(entity.getDataDefinition(), batchFieldName);
        return entity.getBelongsToField(batchFieldName);
    }

    private static void checkField(final DataDefinition dataDef, final String fieldName) {
        FieldDefinition fd = dataDef.getField(fieldName);
        if (fd.getType() instanceof BelongsToType) {
            BelongsToType btType = (BelongsToType) fd.getType();
            DataDefinition dd = btType.getDataDefinition();
            if (AdvancedGenealogyConstants.PLUGIN_IDENTIFIER.equals(dd.getPluginIdentifier())
                    && AdvancedGenealogyConstants.MODEL_BATCH.equals(dd.getName())) {
                return;
            }
        }
        throw new IllegalArgumentException(String.format(
                "Field '%s' should be of type belongsTo, pointing to the advanced genealogy's batch entity!", fieldName));
    }

}
