package com.qcadoo.mes.productionCounting.internal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialRequirements.api.MaterialRequirementReportDataService;
import com.qcadoo.mes.orders.constants.OrdersConstants;
import com.qcadoo.mes.productionCounting.internal.constants.ProductionCountingConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;

@Service
public class ProductionCountingService {

    @Autowired
    DataDefinitionService dataDefinitionService;

    @Autowired
    MaterialRequirementReportDataService materialRequirementReportDataService;

    public void setParametersDefaultValue(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        Entity parameter = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER).get(
                form.getEntityId());

        for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                "registerProductionTime")) {
            FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            if (parameter == null || parameter.getField(componentReference) == null) {
                component.setFieldValue(true);
                component.requestComponentUpdateState();
            }
        }
    }

    public void setOrderDefaultValue(final ViewDefinitionState viewDefinitionState) {
        FieldComponent typeOfProductionRecording = (FieldComponent) viewDefinitionState
                .getComponentByReference("typeOfProductionRecording");

        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        if (form.getEntityId() != null) {
            Entity order = dataDefinitionService.get(OrdersConstants.PLUGIN_IDENTIFIER, OrdersConstants.MODEL_ORDER).get(
                    (Long) form.getEntityId());
            if (order == null || "".equals(order.getField("typeOfProductionRecording"))) {
                typeOfProductionRecording.setFieldValue("01none");
            }
            for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime")) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
                if (order == null || order.getField(componentReference) == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
        } else {
            typeOfProductionRecording.setFieldValue("01none");
            for (String componentReference : Arrays.asList("registerQuantityInProduct", "registerQuantityOutProduct",
                    "registerProductionTime")) {
                FieldComponent component = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
                if (component.getFieldValue() == null) {
                    component.setFieldValue(true);
                    component.requestComponentUpdateState();
                }
            }
        }
    }

    public void setProductBelongsToOperation(final ViewDefinitionState viewDefinitionState, final ComponentState state,
            final String[] args) {

        // TODO
    }

    public void updateUsedProductsForOrder(final Entity order) {

        List<Entity> orderList = Arrays.asList(order);

        Map<Entity, BigDecimal> products = materialRequirementReportDataService.getQuantitiesForOrdersTechnologyProducts(
                orderList, true);

        List<Entity> usedproducts = dataDefinitionService
                .get(ProductionCountingConstants.PLUGIN_IDENTIFIER, ProductionCountingConstants.MODEL_PRODUCTION_RECORD).find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();

        for (Entry<Entity, BigDecimal> product : products.entrySet()) {
            Entity found = null;

            for (Entity usedproduct : usedproducts) {
                if (usedproduct.getBelongsToField("product").equals(product.getKey())) {
                    found = usedproduct;
                    break;
                }
            }

            if (found == null) {
                Entity newEntry = dataDefinitionService.get("usedProducts", "usedProducts").create();
                newEntry.setField("order", order);
                newEntry.setField("product", product.getKey());
                newEntry.setField("plannedQuantity", product.getValue());
                dataDefinitionService.get("usedProducts", "usedProducts").save(newEntry);
            } else {
                BigDecimal currentPlannedQuantity = (BigDecimal) found.getField("plannedQuantity");
                if (currentPlannedQuantity != product.getValue()) {
                    found.setField("plannedQuantity", product.getValue());
                    dataDefinitionService.get("usedProducts", "usedProducts").save(found);
                }
            }
        }

        usedproducts = dataDefinitionService.get("usedProducts", "usedProducts").find()
                .add(SearchRestrictions.belongsTo("order", order)).list().getEntities();
        for (Entity usedproduct : usedproducts) {
            boolean found = false;
            for (Entry<Entity, BigDecimal> product : products.entrySet()) {
                if (usedproduct.getBelongsToField("product").equals(product.getKey())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                dataDefinitionService.get("usedProducts", "usedProducts").delete(usedproduct.getId());
            }
        }

    }
}
