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
package com.qcadoo.mes.productionCounting.hooks;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.SetTechnologyInComponentsService;
import com.qcadoo.mes.productionCounting.SetTrackingOperationProductsComponentsService;
import com.qcadoo.mes.productionCounting.constants.OrderFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ParameterFieldsPC;
import com.qcadoo.mes.productionCounting.constants.ProductionCountingConstants;
import com.qcadoo.mes.productionCounting.constants.ProductionTrackingFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.hooks.helpers.OperationProductsExtractor;
import com.qcadoo.mes.productionCounting.states.ProductionTrackingStatesHelper;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.view.api.utils.NumberGeneratorService;

@Service
public class ProductionTrackingHooks {

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private ProductionTrackingStatesHelper productionTrackingStatesHelper;

    @Autowired
    private OperationProductsExtractor operationProductsExtractor;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private SetTrackingOperationProductsComponentsService setTrackingOperationProductsComponents;

    @Autowired
    private SetTechnologyInComponentsService setTechnologyInComponentsService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ProductionTrackingService productionTrackingService;

    public void onCreate(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        setInitialState(productionTracking);
    }

    public void onCopy(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        setInitialState(productionTracking);
        productionTracking.setField(ProductionTrackingFields.IS_CORRECTION, false);
    }

    public void onSave(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        generateNumberIfNeeded(productionTracking);
        setTimesToZeroIfEmpty(productionTracking);
        copyProducts(productionTracking);
        generateSetTrackingOperationProductsComponents(productionTracking);
        generateSetTechnologyInComponents(productionTracking);
    }

    public void onDelete(final DataDefinition productionTrackingDD, final Entity productionTracking) {
        productionTrackingService.unCorrect(productionTracking);
    }

    private void copyProducts(final Entity productionTracking) {
        Entity order = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER);

        final boolean registerQuantityInProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_IN_PRODUCT);
        final boolean registerQuantityOutProduct = order.getBooleanField(OrderFieldsPC.REGISTER_QUANTITY_OUT_PRODUCT);

        if (!(registerQuantityInProduct || registerQuantityOutProduct)
                || StringUtils.isEmpty(order.getStringField(OrderFieldsPC.TYPE_OF_PRODUCTION_RECORDING))
                || !shouldCopyProducts(productionTracking)) {
            return;
        }

        OperationProductsExtractor.TrackingOperationProducts operationProducts = operationProductsExtractor
                .getProductsByModelName(productionTracking);

        List<Entity> inputs = Collections.emptyList();
        if (registerQuantityInProduct) {
            inputs = operationProducts.getInputComponents();
        }

        List<Entity> outputs = Collections.emptyList();
        if (registerQuantityOutProduct) {
            outputs = operationProducts.getOutputComponents();
        }

        if (registerQuantityInProduct) {
            productionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS, inputs);
        }
        if (registerQuantityOutProduct) {
            productionTracking.setField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS, outputs);
        }
    }

    private boolean shouldCopyProducts(final Entity productionTracking) {
        if (productionTracking.getId() == null) {
            List<Entity> inputProduct = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
            List<Entity> outputProduct = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
            return inputProduct.isEmpty() && outputProduct.isEmpty();
        }

        Entity existingProductionTracking = productionTracking.getDataDefinition().get(productionTracking.getId());

        Object oldTocValue = existingProductionTracking.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);
        Object newTocValue = productionTracking.getField(ProductionTrackingFields.TECHNOLOGY_OPERATION_COMPONENT);

        Object oldOrderValue = existingProductionTracking.getField(ProductionTrackingFields.ORDER);
        Object newOrderValue = productionTracking.getField(ProductionTrackingFields.ORDER);

        return !ObjectUtils.equals(oldOrderValue, newOrderValue) || !ObjectUtils.equals(oldTocValue, newTocValue);
    }

    private void setTimesToZeroIfEmpty(final Entity productionTracking) {
        setTimeToZeroIfNull(productionTracking, ProductionTrackingFields.LABOR_TIME);
        setTimeToZeroIfNull(productionTracking, ProductionTrackingFields.MACHINE_TIME);
    }

    private void setTimeToZeroIfNull(final Entity productionTracking, final String timeFieldName) {
        Integer time = productionTracking.getIntegerField(timeFieldName);
        productionTracking.setField(timeFieldName, ObjectUtils.defaultIfNull(time, 0));
    }

    private void setInitialState(final Entity productionTracking) {
        productionTracking.setField(ProductionTrackingFields.IS_EXTERNAL_SYNCHRONIZED, true);
        productionTracking.setField(ProductionTrackingFields.CORRECTION, null);

        if (productionTracking.getField(ProductionTrackingFields.LAST_TRACKING) == null) {
            productionTracking.setField(ProductionTrackingFields.LAST_TRACKING, false);
        }

        productionTrackingStatesHelper.setInitialState(productionTracking);
    }

    private void generateNumberIfNeeded(final Entity productionTracking) {
        if (productionTracking.getField(ProductionTrackingFields.NUMBER) == null) {
            Entity parameter = parameterService.getParameter();
            if (parameter.getBooleanField(ParameterFieldsPC.GENERATE_PRODUCTION_RECORD_NUMBER_FROM_ORDER_NUMBER)) {
                String[] orderNumberSplited = productionTracking.getBelongsToField(ProductionTrackingFields.ORDER)
                        .getStringField(OrderFields.NUMBER).split("-");
                if (orderNumberSplited.length > 1) {
                    String productionRecordNumber = getProductionRecordNumber(orderNumberSplited);
                    productionTracking.setField(ProductionTrackingFields.NUMBER, productionRecordNumber);
                } else {
                    productionTracking.setField(ProductionTrackingFields.NUMBER, numberGeneratorService.generateNumber(
                            ProductionCountingConstants.PLUGIN_IDENTIFIER, productionTracking.getDataDefinition().getName()));
                }
            } else {
                productionTracking.setField(ProductionTrackingFields.NUMBER, numberGeneratorService.generateNumber(
                        ProductionCountingConstants.PLUGIN_IDENTIFIER, productionTracking.getDataDefinition().getName()));
            }
        }
    }

    private String getProductionRecordNumber(final String[] orderNumberSplited) {
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < orderNumberSplited.length; i++) {
            if (i > 0) {
                number.append(orderNumberSplited[i]);
                if (i != orderNumberSplited.length - 1) {
                    number.append("-");
                }
            }
        }
        return StringUtils.strip(number.toString());
    }

    private void generateSetTrackingOperationProductsComponents(Entity productionTracking) {
        if (mustRebuildSetTrackingOperationProductsComponents(productionTracking)) {
            EntityList trackingOperationProductOutComponents = productionTracking.getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_OUT_COMPONENTS);
            for (Entity trackingOperationProductOutComponent : trackingOperationProductOutComponents) {
                BigDecimal usedQuantity = trackingOperationProductOutComponent.getDecimalField(TrackingOperationProductOutComponentFields.GIVEN_QUANTITY);

                List<Entity> setTrackingOperationProductsInComponents = trackingOperationProductOutComponent.getHasManyField(TrackingOperationProductOutComponentFields.SET_TRACKING_OPERATION_PRODUCTS_IN_COMPONENTS);
                List<Long> ids = setTrackingOperationProductsInComponents.stream().map(entity -> entity.getId()).collect(Collectors.toList());
                if (!ids.isEmpty()) {
                    Map<String, Object> parameters = new HashMap<String, Object>() {
                        {
                            put("ids", ids);
                        }
                    };
                    jdbcTemplate.update("DELETE FROM productioncounting_settrackingoperationproductincomponents WHERE id IN (:ids)", new MapSqlParameterSource(parameters));
                }
                trackingOperationProductOutComponent = setTrackingOperationProductsComponents.fillTrackingOperationProductOutComponent(productionTracking, trackingOperationProductOutComponent, usedQuantity);

                setTrackingOperationProductsInComponents = trackingOperationProductOutComponent.getHasManyField(TrackingOperationProductOutComponentFields.SET_TRACKING_OPERATION_PRODUCTS_IN_COMPONENTS);
                setTrackingOperationProductsInComponents.stream().forEach(entity -> {
                    entity.getDataDefinition().save(entity);
                });
            }
        }
    }

    private void generateSetTechnologyInComponents(Entity productionTracking) {
        if (mustRebuildSetTrackingOperationProductsComponents(productionTracking)) {
            EntityList trackingOperationProductInComponents = productionTracking
                    .getHasManyField(ProductionTrackingFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENTS);
            for (Entity trackingOperationProductInComponent : trackingOperationProductInComponents) {
                if (setTechnologyInComponentsService.isSet(trackingOperationProductInComponent)) {
                    BigDecimal usedQuantity = trackingOperationProductInComponent
                            .getDecimalField(TrackingOperationProductInComponentFields.GIVEN_QUANTITY);

                    List<Entity> setTechnologyInComponents = trackingOperationProductInComponent
                            .getHasManyField(TrackingOperationProductInComponentFields.SET_TECHNOLOGY_IN_COMPONENTS);
                    List<Long> ids = setTechnologyInComponents.stream().map(entity -> entity.getId())
                            .collect(Collectors.toList());
                    if (!ids.isEmpty()) {
                        Map<String, Object> parameters = new HashMap<String, Object>() {

                            {
                                put("ids", ids);
                            }
                        };
                        jdbcTemplate.update("DELETE FROM productioncounting_settechnologyincomponents WHERE id IN (:ids)",
                                new MapSqlParameterSource(parameters));
                    }

                    trackingOperationProductInComponent = setTechnologyInComponentsService
                            .fillTrackingOperationProductOutComponent(trackingOperationProductInComponent, productionTracking,
                                    usedQuantity);

                    setTechnologyInComponents = trackingOperationProductInComponent
                            .getHasManyField(TrackingOperationProductInComponentFields.SET_TECHNOLOGY_IN_COMPONENTS);
                    setTechnologyInComponents.stream().forEach(entity -> {
                        entity.getDataDefinition().save(entity);
                    });
                }
            }
        }
    }

    private boolean mustRebuildSetTrackingOperationProductsComponents(Entity productionTracking) {
        if (productionTracking.getId() == null) {
            return true;
        }

        Long previousOrderId = productionTracking.getDataDefinition().get(productionTracking.getId()).getBelongsToField(ProductionTrackingFields.ORDER).getId();

        return !previousOrderId.equals(productionTracking.getBelongsToField(ProductionTrackingFields.ORDER).getId());
    }
}
