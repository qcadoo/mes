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
package com.qcadoo.mes.basic.imports.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.constants.QcadooModelConstants;
import com.qcadoo.model.constants.UnitConversionItemFields;

@Service
public class ProductXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING = "qcadooView.validate.field.error.missing";

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_COST_FOR_NUMBER = "costForNumber";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Override
    public Entity createEntity(final String pluginIdentifier, final String modelName) {
        Entity product = getDataDefinition(pluginIdentifier, modelName).create();

        setRequiredFields(product);

        return product;
    }

    private void setRequiredFields(final Entity product) {
        product.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
        product.setField(L_COST_FOR_NUMBER, BigDecimal.ONE);
    }

    @Override
    public void validateEntity(final Entity product, final DataDefinition productDD) {
        validateAdditionalUnit(product, productDD);
        validateConversion(product, productDD);
    }

    private void validateAdditionalUnit(final Entity product, DataDefinition productDD) {
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
        String unit = product.getStringField(ProductFields.UNIT);

        if (Objects.nonNull(additionalUnit) && Objects.nonNull(unit)) {
            if (additionalUnit.equals(unit)) {
                product.addError(productDD.getField(ProductFields.ADDITIONAL_UNIT), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
            }
        }
    }

    private void validateConversion(final Entity product, final DataDefinition productDD) {
        BigDecimal conversion = product.getDecimalField(ProductFields.CONVERSION);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
        String unit = product.getStringField(ProductFields.UNIT);

        if (Objects.nonNull(additionalUnit) && Objects.nonNull(unit)) {
            if (Objects.isNull(conversion)) {
                product.addError(productDD.getField(ProductFields.CONVERSION), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_MISSING);
            } else {
                List<Entity> conversionItems = Lists.newArrayList();

                Entity conversionItem = createUnitConversionItem(product, conversion, additionalUnit, unit);

                conversionItems.add(conversionItem);

                product.setField(ProductFields.CONVERSION_ITEMS, conversionItems);
            }
        }
    }

    private Entity createUnitConversionItem(final Entity product, final BigDecimal conversion, final String additionalUnit, final String unit) {
        Entity unitConversionItem = getUnitConversionItemDD().create();

        unitConversionItem.setField(UnitConversionItemFieldsB.PRODUCT, product);
        unitConversionItem.setField(UnitConversionItemFields.UNIT_FROM, unit);
        unitConversionItem.setField(UnitConversionItemFields.QUANTITY_FROM, BigDecimal.ONE);
        unitConversionItem.setField(UnitConversionItemFields.UNIT_TO, additionalUnit);
        unitConversionItem.setField(UnitConversionItemFields.QUANTITY_TO, conversion);

        return unitConversionItem;
    }

    private DataDefinition getUnitConversionItemDD() {
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_UNIT_CONVERSION_ITEM);
    }

}
