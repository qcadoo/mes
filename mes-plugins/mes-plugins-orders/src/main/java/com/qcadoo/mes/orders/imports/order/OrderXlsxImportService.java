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
package com.qcadoo.mes.orders.imports.order;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.imports.services.XlsxImportService;
import com.qcadoo.mes.orders.OrderService;
import com.qcadoo.mes.orders.TechnologyServiceO;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrderType;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.productionLines.constants.ParameterFieldsPL;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class OrderXlsxImportService extends XlsxImportService {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM = "qcadooView.validate.field.error.custom";

    private static final String L_DIVISION = "division";

    private static final String L_PRODUCTION_LINE = "productionLine";

    private static final String L_RANGE = "range";

    private static final String L_ONE_DIVISION = "01oneDivision";

    private static final String L_TYPE_OF_PRODUCTION_RECORDING = "typeOfProductionRecording";

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Override
    public Entity createEntity(final String pluginIdentifier, final String modelName) {
        Entity order = getDataDefinition(pluginIdentifier, modelName).create();

        setRequiredFields(order);

        return order;
    }

    private void setRequiredFields(final Entity order) {
        order.setField(OrderFields.ORDER_TYPE, OrderType.WITH_PATTERN_TECHNOLOGY.getStringValue());
        order.setField(OrderFields.EXTERNAL_SYNCHRONIZED, true);
    }

    @Override
    public void validateEntity(final Entity order, final DataDefinition orderDD) {
        validateNumber(order, orderDD);
        validateTechnology(order, orderDD);
        validateName(order, orderDD);
        validateDescription(order, orderDD);
        validateDivision(order, orderDD);
        validateProductionLine(order, orderDD);
        validateTypeOfProductionRecording(order, orderDD);
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
                String technologyState = technology.getStringField(TechnologyFields.STATE);
                Entity technologyProduct = technology.getBelongsToField(TechnologyFields.PRODUCT);

                if (!TechnologyStateStringValues.ACCEPTED.equals(technologyState)
                        && !technologyProduct.getId().equals(product.getId())) {
                    order.addError(orderDD.getField(OrderFields.TECHNOLOGY), L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_CUSTOM);
                }
            }
        }

        order.setField(OrderFields.TECHNOLOGY_PROTOTYPE, order.getBelongsToField(OrderFields.TECHNOLOGY));
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

        boolean fillOrderDescriptionBasedOnTechnology = parameterService.getParameter()
                .getBooleanField(ParameterFieldsO.FILL_ORDER_DESCRIPTION_BASED_ON_TECHNOLOGY_DESCRIPTION);

        if (Objects.isNull(description) && Objects.nonNull(technology) && fillOrderDescriptionBasedOnTechnology) {
            description = technology.getStringField(TechnologyFields.DESCRIPTION);

            order.setField(OrderFields.DESCRIPTION, description);
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
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(productionLine)) {
            if (Objects.isNull(technology)) {
                productionLine = parameterService.getParameter().getBelongsToField(ParameterFieldsPL.DEFAULT_PRODUCTION_LINE);
            } else {
                productionLine = technology.getBelongsToField(L_PRODUCTION_LINE);

                if (Objects.isNull(productionLine)) {
                    productionLine = parameterService.getParameter().getBelongsToField(ParameterFieldsPL.DEFAULT_PRODUCTION_LINE);
                }
            }

            order.setField(OrderFields.PRODUCTION_LINE, productionLine);
        }
    }

    private void validateTypeOfProductionRecording(final Entity order, final DataDefinition orderDD) {
        String typeOfProductionRecording;
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.isNull(technology)) {
            typeOfProductionRecording = parameterService.getParameter().getStringField(L_TYPE_OF_PRODUCTION_RECORDING);
        } else {
            typeOfProductionRecording = technology.getStringField(L_TYPE_OF_PRODUCTION_RECORDING);

            if (Objects.isNull(typeOfProductionRecording)) {
                typeOfProductionRecording = parameterService.getParameter().getStringField(L_TYPE_OF_PRODUCTION_RECORDING);
            }
        }

        order.setField(L_TYPE_OF_PRODUCTION_RECORDING, typeOfProductionRecording);
    }

}
