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
package com.qcadoo.mes.orders;

import com.google.common.collect.Sets;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.orders.constants.ParameterFieldsO;
import com.qcadoo.mes.orders.states.constants.OrderState;
import com.qcadoo.mes.productionLines.constants.ParameterFieldsPL;
import com.qcadoo.mes.productionLines.constants.ProductionLineFields;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.constants.TechnologyOperationComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologyProductionLineFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.plugin.api.PluginUtils;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    private static final String L_EMPTY_NUMBER = "";

    private static final String L_DIVISION = "division";

    private static final Set<String> L_ORDER_STARTED_STATES = Collections
            .unmodifiableSet(Sets.newHashSet(OrderState.IN_PROGRESS.getStringValue(), OrderState.COMPLETED.getStringValue(),
                    OrderState.INTERRUPTED.getStringValue()));

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private TechnologyService technologyService;

    @Autowired
    private TechnologyServiceO technologyServiceO;

    @Override
    public Entity getOrder(final Long orderId) {
        return getOrderDD().get(orderId);
    }

    @Override
    public DataDefinition getOrderDD() {
        return dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER);
    }

    @Override
    public boolean isOrderStarted(final String state) {
        return L_ORDER_STARTED_STATES.contains(state);
    }

    @Override
    public String makeDefaultName(final Entity product, Entity technology, final Locale locale) {
        if (Objects.isNull(technology)) {
            technology = technologyServiceO.getDefaultTechnology(product);
        }

        String technologyNumber = L_EMPTY_NUMBER;

        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(new Date());

        if (Objects.nonNull(technology)) {
            technologyNumber = "tech. " + technology.getStringField(TechnologyFields.NUMBER);

            return translationService.translate("orders.order.name.default", locale, product.getStringField(OrderFields.NAME),
                    product.getStringField(ProductFields.NUMBER), technologyNumber,
                    cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH));
        } else {
            return translationService.translate("orders.order.name.defaultFromProduct", locale,
                    product.getStringField(OrderFields.NAME), product.getStringField(ProductFields.NUMBER),
                    cal.get(Calendar.YEAR) + "." + (cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.DAY_OF_MONTH));
        }
    }

    @Override
    public Entity getProductionLine(final Entity technology) {
        Entity parameter = parameterService.getParameter();

        Entity defaultProductionLine = parameter.getBelongsToField(ParameterFieldsPL.DEFAULT_PRODUCTION_LINE);
        Entity productionLine = null;

        if (Objects.isNull(technology)) {
            return null;
        }

        if (Objects.nonNull(defaultProductionLine)) {
            List<Entity> lines = technology.getHasManyField(TechnologyFields.PRODUCTION_LINES);

            boolean anyMatch = lines.stream().anyMatch(l -> l.getBelongsToField(TechnologyProductionLineFields.PRODUCTION_LINE).getId().equals(defaultProductionLine.getId()));

            if (anyMatch) {
                productionLine = defaultProductionLine;
            }

            if (lines.isEmpty()) {
                productionLine = defaultProductionLine;
            }
        }

        if (Objects.nonNull(technology) && parameter.getBooleanField(ParameterFieldsO.PROMPT_DEFAULT_LINE_FROM_TECHNOLOGY)
                && PluginUtils.isEnabled(L_PRODUCT_FLOW_THRU_DIVISION)) {
            Optional<Entity> maybeProductionLine = technologyService.getProductionLine(technology);

            if (maybeProductionLine.isPresent()) {
                productionLine = maybeProductionLine.get();
            }
        }

        return productionLine;
    }

    @Override
    public Entity getDivision(final Entity technology) {
        Entity division = null;

        if (Objects.nonNull(technology)) {
            division = technology.getBelongsToField(L_DIVISION);
        }

        if (Objects.isNull(division)) {
            Entity productionLine = getProductionLine(technology);

            if (Objects.nonNull(productionLine)) {
                List<Entity> divisions = productionLine.getManyToManyField(ProductionLineFields.DIVISIONS);

                if (divisions.size() == 1) {
                    division = divisions.get(0);
                }
            }
        }

        return division;
    }

    // FIXME - this method doesn't have anything in common with production orders..
    @Override
    public void changeFieldState(final ViewDefinitionState view, final String booleanFieldComponentName,
                                 final String fieldComponentName) {
        CheckBoxComponent booleanCheckBox = (CheckBoxComponent) view.getComponentByReference(booleanFieldComponentName);

        FieldComponent fieldComponent = (FieldComponent) view.getComponentByReference(fieldComponentName);

        if (booleanCheckBox.isChecked()) {
            fieldComponent.setEnabled(true);
            fieldComponent.requestComponentUpdateState();
        } else {
            fieldComponent.setEnabled(false);
            fieldComponent.requestComponentUpdateState();
        }
    }

    @Override
    public boolean checkComponentOrderHasTechnology(final DataDefinition dataDefinition, final Entity entity) {
        Entity order;

        if (OrdersConstants.MODEL_ORDER.equals(entity.getDataDefinition().getName())) {
            order = entity;
        } else {
            order = entity.getBelongsToField(OrdersConstants.MODEL_ORDER);
        }

        if (Objects.isNull(order)) {
            return true;
        }

        if (Objects.isNull(order.getField(OrderFields.TECHNOLOGY))) {
            entity.addError(dataDefinition.getField(OrdersConstants.MODEL_ORDER),
                    "orders.validate.global.error.orderMustHaveTechnology");

            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean checkRequiredBatch(final Entity order) {
        Entity technology = order.getBelongsToField(OrderFields.TECHNOLOGY);

        if (Objects.nonNull(technology)) {
            if (order.getHasManyField("genealogies").isEmpty()) {
                if (technology.getBooleanField("batchRequired")) {
                    return false;
                }
                if (technology.getBooleanField("shiftFeatureRequired")) {
                    return false;
                }
                if (technology.getBooleanField("postFeatureRequired")) {
                    return false;
                }
                if (technology.getBooleanField("otherFeatureRequired")) {
                    return false;
                }
                for (Entity operationComponent : technology.getTreeField(TechnologyFields.OPERATION_COMPONENTS)) {
                    for (Entity operationProductComponent : operationComponent
                            .getHasManyField(TechnologyOperationComponentFields.OPERATION_PRODUCT_IN_COMPONENTS)) {
                        if (operationProductComponent.getBooleanField("batchRequired")) {
                            return false;
                        }
                    }
                }
            }
            for (Entity genealogy : order.getHasManyField("genealogies")) {
                if (technology.getBooleanField("batchRequired") && Objects.isNull(genealogy.getField("batch"))) {
                    return false;
                }
                if (technology.getBooleanField("shiftFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("shiftFeatures");

                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                if (technology.getBooleanField("postFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("postFeatures");

                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                if (technology.getBooleanField("otherFeatureRequired")) {
                    List<Entity> entityList = genealogy.getHasManyField("otherFeatures");

                    if (entityList.isEmpty()) {
                        return false;
                    }
                }
                for (Entity genealogyProductIn : genealogy.getHasManyField("productInComponents")) {
                    if (genealogyProductIn.getBelongsToField("productInComponent").getBooleanField("batchRequired")) {
                        List<Entity> entityList = genealogyProductIn.getHasManyField("batch");

                        if (entityList.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public String buildOrderDescription(final Entity masterOrder, final Entity technology, final Entity product,
                                        final boolean fillOrderDescriptionBasedOnTechnology, final boolean fillOrderDescriptionBasedOnProductDescription) {
        StringBuilder builder = new StringBuilder();

        if (Objects.nonNull(masterOrder)) {
            String poNumber = "";
            String direction = "";

            if (Objects.nonNull(masterOrder.getStringField("poNumber"))) {
                poNumber = masterOrder.getStringField("poNumber");
            }
            if (Objects.nonNull(masterOrder.getStringField("direction"))) {
                direction = masterOrder.getStringField("direction");
            }

            if (!poNumber.isEmpty()) {
                builder.append("PO");
                builder.append(poNumber);
                if (direction.isEmpty()) {
                    builder.append("\n");
                }
            }

            if (!direction.isEmpty()) {
                builder.append("-");
                builder.append(direction);
                builder.append("\n");
            }

            buildMOTechnologyDescription(masterOrder, technology, fillOrderDescriptionBasedOnTechnology, builder);
            buildMOProductDescription(masterOrder, product, fillOrderDescriptionBasedOnProductDescription, builder);
        }

        buildTechnologyDescription(technology, fillOrderDescriptionBasedOnTechnology, builder);
        buildProductDescription(product, fillOrderDescriptionBasedOnProductDescription, builder);

        return builder.toString();
    }

    private void buildMOTechnologyDescription(final Entity masterOrder, final Entity technology,
                                              final boolean fillOrderDescriptionBasedOnTechnology, final StringBuilder builder) {
        if (fillOrderDescriptionBasedOnTechnology && Objects.isNull(technology)
                && Objects.nonNull(masterOrder.getBelongsToField("technology"))) {
            String technologyDescription = masterOrder.getBelongsToField("technology")
                    .getStringField(TechnologyFields.DESCRIPTION);

            if (Objects.nonNull(technologyDescription)) {
                builder.append(technologyDescription);
            }
        }
    }

    private void buildMOProductDescription(final Entity masterOrder, final Entity product,
                                           final boolean fillOrderDescriptionBasedOnProductDescription, final StringBuilder builder) {
        if (fillOrderDescriptionBasedOnProductDescription && Objects.isNull(product)
                && Objects.nonNull(masterOrder.getBelongsToField("product"))) {
            String productDescription = masterOrder.getBelongsToField("product").getStringField(ProductFields.DESCRIPTION);

            if (Objects.nonNull(productDescription)) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }

                builder.append(productDescription);
            }
        }
    }

    private void buildTechnologyDescription(final Entity technology, final boolean fillOrderDescriptionBasedOnTechnology,
                                            final StringBuilder builder) {
        if (fillOrderDescriptionBasedOnTechnology && Objects.nonNull(technology)) {
            String technologyDescription = technology.getStringField(TechnologyFields.DESCRIPTION);

            if (Objects.nonNull(technologyDescription)) {
                builder.append(technologyDescription);
            }
        }
    }

    private void buildProductDescription(final Entity product, final boolean fillOrderDescriptionBasedOnProductDescription,
                                         final StringBuilder builder) {
        if (fillOrderDescriptionBasedOnProductDescription && Objects.nonNull(product)) {
            String productDescription = product.getStringField(ProductFields.DESCRIPTION);

            if (Objects.nonNull(productDescription)) {
                if (builder.length() > 0) {
                    builder.append("\n");
                }

                builder.append(productDescription);
            }
        }
    }

    @Override
    public Optional<Entity> findLastOrder(final Entity order) {
        Entity productionLine = order.getBelongsToField(OrderFields.PRODUCTION_LINE);

        Entity lastOrder = getOrderDD().find()
                .add(SearchRestrictions.isNotNull(OrderFields.FINISH_DATE))
                .add(SearchRestrictions.belongsTo(OrderFields.PRODUCTION_LINE, productionLine))
                .add(SearchRestrictions.ne(OrderFields.STATE, OrderState.ABANDONED.getStringValue()))
                .addOrder(SearchOrders.desc(OrderFields.FINISH_DATE)).setMaxResults(1).uniqueResult();

        return Optional.ofNullable(lastOrder);
    }

    @Override
    public void setPlannedQuantityForAdditionalUnit(final Entity order) {
        Entity product = order.getBelongsToField(OrderFields.PRODUCT);
        BigDecimal plannedQuantity = order.getDecimalField(OrderFields.PLANNED_QUANTITY);

        if (Objects.nonNull(product)) {
            String unit = product.getStringField(ProductFields.UNIT);
            String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

            if (!StringUtils.isEmpty(additionalUnit)) {
                PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                        searchCriteriaBuilder -> searchCriteriaBuilder
                                .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

                if (unitConversions.isDefinedFor(additionalUnit)) {
                    BigDecimal plannedQuantityForAdditionalUnit = unitConversions.convertTo(plannedQuantity, additionalUnit);

                    order.setField(OrderFields.PLANNED_QUANTITY_FOR_ADDITIONAL_UNIT, plannedQuantityForAdditionalUnit);
                }
            }
        }
    }

}
