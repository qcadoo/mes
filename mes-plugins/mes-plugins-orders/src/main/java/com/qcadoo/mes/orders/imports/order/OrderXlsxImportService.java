/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
 * <p>
 * This file is part of Qcadoo.
 * <p>
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.orders.imports.order;

import com.google.common.collect.Sets;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.util.AdditionalUnitService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

@Service
public class OrderXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_DIVISION = "division";

    private static final String L_RANGE = "range";

    private static final String L_ONE_DIVISION = "01oneDivision";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    private static final String L_REGISTER_QUANTITY_IN_PRODUCT = "registerQuantityInProduct";

    private static final String L_REGISTER_QUANTITY_OUT_PRODUCT = "registerQuantityOutProduct";

    private static final String L_REGISTER_PRODUCTION_TIME = "registerProductionTime";

    private static final Set<String> L_PRODUCTION_TRACKING_FIELDS = Sets.newHashSet(L_TYPE_OF_PRODUCTION_RECORDING,
            L_REGISTER_QUANTITY_IN_PRODUCT, L_REGISTER_QUANTITY_OUT_PRODUCT, L_REGISTER_PRODUCTION_TIME);

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Autowired
    private AdditionalUnitService additionalUnitService;

    @Override
    public Entity createEntity(final String pluginIdentifier, final String modelName) {
        Entity order = getDataDefinition(pluginIdentifier, modelName).create();

        setRequiredFields(order);

        return order;
    }

    private void setRequiredFields(final Entity order) {
        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

    @Override
    public void validateEntity(final Entity order, final DataDefinition orderDD) {
        validatePlannedQuantity(order, orderDD);
        validateNumber(order, orderDD);
        validateTechnology(order, orderDD);
        validateName(order, orderDD);
        validateDescription(order, orderDD);
        validateDates(order, orderDD);
        validateDivision(order, orderDD);
        validateProductionLine(order, orderDD);
        validateProductionTrackingFields(order, orderDD);
    }

    private void validatePlannedQuantity(final Entity order, final DataDefinition orderDD) {
        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        BigDecimal plannedQuantityForAdditionalUnit = plannedQuantity;

        if (Objects.nonNull(plannedQuantity) && Objects.nonNull(product)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = additionalUnitService.getAdditionalUnit(product);

            plannedQuantityForAdditionalUnit = additionalUnitService.getQuantityAfterConversion(order, additionalUnit,
                    plannedQuantity, unit);
        }

        order.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT, plannedQuantityForAdditionalUnit);
    }

    private void validateNumber(final Entity order, final DataDefinition orderDD) {
        String number = order.getStringField(OrderFields.NUMBER);

        if (Objects.isNull(number)) {
            number = numberGeneratorService.generateNumber(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);

            order.setField(OrderFields.NUMBER, number);
        }
    }

    private void validateTechnology(final Entity order, final DataDefinition orderDD) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        if (Objects.nonNull(product)) {
            if (Objects.isNull(technology)) {
                technology = technologyServiceO.getDefaultTechnology(product);

                order.setField(OrderFields.TECHNOLOGY, technology);
            } else {
                if (!validateTechnologyStateAndProduct(technology, product)) {
                    order.addError(orderDD.getField(OrderFields.TECHNOLOGY), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
                }
            }
        }
    }

    private boolean validateTechnologyStateAndProduct(final Entity technology, final Entity product) {
        String technologyState = technology.getStringField(TechnologyFields.STATE);
        Entity technologyProduct = technology.getBelongsToField(TechnologyFields.PRODUCT);
        String technologyProductEntityType = technologyProduct.getStringField(ProductFields.ENTITY_TYPE);

        return TechnologyStateStringValues.ACCEPTED.equals(technologyState)
                && (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(technologyProductEntityType) &&
                technologyProduct.getId().equals(product.getId()))
                || (ProductFamilyElementType.PRODUCTS_FAMILY.getStringValue().equals(technologyProductEntityType) &&
                technologyProduct.getHasManyField(ProductFields.CHILDREN).stream().anyMatch(child -> child.getId().equals(product.getId())));
    }

    private void validateName(final Entity order, final DataDefinition orderDD) {
        String name = order.getStringField(OrderFields.NAME);
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(name) && Objects.nonNull(product)) {
            name = orderService.makeDefaultName(product, technology, LocaleContextHolder.getLocale());

            order.setField(OrderFields.NAME, name);
        }
    }

    private void validateDescription(final Entity order, final DataDefinition orderDD) {
        String description = order.getStringField(OrderFields.DESCRIPTION);
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);

        Entity parameter = parameterService.getParameter();

        boolean fillOrderDescriptionBasedOnTechnology = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);

        boolean fillOrderDescriptionBasedOnProductDescription = parameter
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_PRODUCT_DESCRIPTION);

        if (Objects.isNull(description)) {
            StringBuilder descriptionBuilder = new StringBuilder();

            if (fillOrderDescriptionBasedOnTechnology && Objects.nonNull(technology)
                    && StringUtils.isNoneBlank(technology.getStringField(TechnologyFields.DESCRIPTION))) {
                descriptionBuilder.append(technology.getStringField(TechnologyFields.DESCRIPTION));
            }

            if (fillOrderDescriptionBasedOnProductDescription && Objects.nonNull(product)) {
                String productDescription = product.getStringField(ProductFields.DESCRIPTION);

                if (StringUtils.isNoneBlank(productDescription)) {
                    if (StringUtils.isNoneBlank(descriptionBuilder.toString())) {
                        descriptionBuilder.append("\n");
                    }
                    
                    descriptionBuilder.append(productDescription);
                }
            }

            order.setField(OrderFields.DESCRIPTION, descriptionBuilder.toString());
        }
    }

    private void validateDates(final Entity order, final DataDefinition orderDD) {
        Date startDate = order.getDateField(OrderFields.START_DATE);
        Date finishDate = order.getDateField(OrderFields.FINISH_DATE);

        if (Objects.isNull(startDate) || Objects.isNull(finishDate)) {
            if (Objects.isNull(startDate)) {
                order.setField(OrderFields.DATE_FROM, startDate);
            }

            if (Objects.isNull(finishDate)) {
                order.setField(OrderFields.DATE_TO, finishDate);
            }
        } else {
            if (startDate.after(finishDate) || startDate.equals(finishDate)) {
                order.addError(orderDD.getField(OrderFields.FINISH_DATE), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
            }
        }
    }

    private void validateDivision(final Entity order, final DataDefinition orderDD) {
        Entity division = order.getBelongsToField(OrderFields.DIVISION);
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(division) && Objects.nonNull(technology)) {
            String technologyRange = technology.getStringField(L_RANGE);

            if (L_ONE_DIVISION.equals(technologyRange)) {
                division = technology.getBelongsToField(L_DIVISION);

                order.setField(OrderFields.DIVISION, division);
            }
        }
    }

    private void validateProductionLine(final Entity order, final DataDefinition orderDD) {
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        if (Objects.isNull(productionLine)) {
            productionLine = orderService.getProductionLine(order.getBelongsToField(OrderFields.TECHNOLOGY));

            order.setField(OrderFields.PRODUCTION_LINE, productionLine);
        }
    }

    private void validateProductionTrackingFields(final Entity order, final DataDefinition orderDD) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(technology)) {
            setProductionTrackingFields(order, parameterService.getParameter());
        } else {
            setProductionTrackingFields(order, technology);
        }
    }

    private void setProductionTrackingFields(final Entity order, final Entity technologyOrParameter) {
        L_PRODUCTION_TRACKING_FIELDS.forEach(fieldName -> order.setField(fieldName, technologyOrParameter.getField(fieldName)));
    }

}
