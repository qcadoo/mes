package com.qcadoo.mes.masterOrders.listeners;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.localization.api.utils.DateUtils;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementProductFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import com.qcadoo.view.constants.QcadooViewConstants;

@Service
public class SalesPlanMaterialRequirementDetailsListeners {

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DeliveriesService deliveriesService;

    public void generateSalesPlanMaterialRequirement(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        FormComponent salesPlanMaterialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        CheckBoxComponent generatedCheckBox = (CheckBoxComponent) view
                .getComponentByReference(SalesPlanMaterialRequirementFields.GENERATED);
        LookupComponent salesPlanLookup = (LookupComponent) view
                .getComponentByReference(SalesPlanMaterialRequirementFields.SALES_PLAN);
        FieldComponent workerField = (FieldComponent) view.getComponentByReference(SalesPlanMaterialRequirementFields.WORKER);
        FieldComponent dateField = (FieldComponent) view.getComponentByReference(SalesPlanMaterialRequirementFields.DATE);

        Entity salesPlan = salesPlanLookup.getEntity();

        if (validateSalesPlanMaterialRequirement(salesPlanMaterialRequirementForm, salesPlanLookup, salesPlan)) {
            workerField.setFieldValue(securityService.getCurrentUserName());
            dateField.setFieldValue(DateUtils.toDateTimeString(new Date()));
            generatedCheckBox.setChecked(true);

            Entity salesPlanMaterialRequirement = salesPlanMaterialRequirementForm.getEntity();

            List<Entity> salesPlanMaterialRequirementProducts = generateSalesPlanMaterialRequirementProducts(
                    salesPlanMaterialRequirement);

            salesPlanMaterialRequirement.setField(SalesPlanMaterialRequirementFields.SALES_PLAN_MATERIAL_REQUIREMENT_PRODUCTS,
                    salesPlanMaterialRequirementProducts);

            salesPlanMaterialRequirement = salesPlanMaterialRequirement.getDataDefinition().save(salesPlanMaterialRequirement);

            salesPlanMaterialRequirementForm.setEntity(salesPlanMaterialRequirement);

            view.addMessage("masterOrders.salesPlanMaterialRequirement.generate.success", ComponentState.MessageType.SUCCESS);
        } else {
            view.addMessage("masterOrders.salesPlanMaterialRequirement.generate.failure", ComponentState.MessageType.FAILURE);
        }
    }

    private boolean validateSalesPlanMaterialRequirement(final FormComponent salesPlanMaterialRequirementForm,
            final LookupComponent salesPlanLookup, final Entity salesPlan) {
        boolean isValid = true;

        if (Objects.isNull(salesPlan)) {
            salesPlanLookup.addMessage("qcadooView.validate.field.error.missing", ComponentState.MessageType.FAILURE);

            isValid = false;
        }

        return isValid;
    }

    private List<Entity> generateSalesPlanMaterialRequirementProducts(final Entity salesPlanMaterialRequirement) {
        return Lists.newArrayList();
    }

    public void createDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent salesPlanMaterialRequirementForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        GridComponent salesPlanMaterialRequirementProductsGrid = (GridComponent) view
                .getComponentByReference(QcadooViewConstants.L_GRID);

        Long salesPlanMaterialRequirementId = salesPlanMaterialRequirementForm.getEntityId();
        Set<Long> salesPlanMaterialRequirementProductIds = salesPlanMaterialRequirementProductsGrid.getSelectedEntitiesIds();

        if (Objects.nonNull(salesPlanMaterialRequirementId) && !salesPlanMaterialRequirementProductIds.isEmpty()) {
            Entity salesPlanMaterialRequirement = getSalesPlanMaterialRequirement(salesPlanMaterialRequirementId);
            List<Entity> salesPlanMaterialRequirementProducts = getSalesPlanMaterialRequirementProducts(
                    salesPlanMaterialRequirementProductIds);

            Entity delivery = createDelivery(salesPlanMaterialRequirement, salesPlanMaterialRequirementProducts);

            Long deliveryId = delivery.getId();

            if (Objects.nonNull(deliveryId)) {
                Map<String, Object> parameters = Maps.newHashMap();
                parameters.put("form.id", deliveryId);

                parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.deliveries");

                String url = "../page/deliveries/deliveryDetails.html";
                view.redirectTo(url, false, true, parameters);
            }
        }
    }

    private Entity createDelivery(final Entity salesPlanMaterialRequirement,
            final List<Entity> salesPlanMaterialRequirementProducts) {
        Entity delivery = deliveriesService.getDeliveryDD().create();

        Optional<Entity> mayBeSupplier = getSupplier(salesPlanMaterialRequirementProducts);
        List<Entity> orderedProducts = createOrderedProducts(salesPlanMaterialRequirementProducts);

        delivery.setField(DeliveryFields.NUMBER,
                numberGeneratorService.generateNumber(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY));

        if (mayBeSupplier.isPresent()) {
            delivery.setField(DeliveryFields.SUPPLIER, mayBeSupplier.get());
        }

        delivery.setField(DeliveryFields.ORDERED_PRODUCTS, orderedProducts);
        delivery.setField(DeliveryFields.EXTERNAL_SYNCHRONIZED, true);

        delivery = delivery.getDataDefinition().save(delivery);

        return delivery;
    }

    private List<Entity> createOrderedProducts(final List<Entity> salesPlanMaterialRequirementProducts) {
        List<Entity> orderedProducts = Lists.newArrayList();

        salesPlanMaterialRequirementProducts.forEach(salesPlanMaterialRequirementProduct -> {
            orderedProducts.add(createOrderedProduct(salesPlanMaterialRequirementProduct));

            salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.IS_DELIVERY_CREATED, true);

            salesPlanMaterialRequirementProduct.getDataDefinition().save(salesPlanMaterialRequirementProduct);
        });

        return orderedProducts;
    }

    private Entity createOrderedProduct(final Entity salesPlanMaterialRequirementProduct) {
        Entity product = salesPlanMaterialRequirementProduct.getBelongsToField(SalesPlanMaterialRequirementProductFields.PRODUCT);
        BigDecimal neededQuantity = BigDecimalUtils.convertNullToZero(
                salesPlanMaterialRequirementProduct.getDecimalField(SalesPlanMaterialRequirementProductFields.NEEDED_QUANTITY));
        BigDecimal minimumOrderQuantity = BigDecimalUtils.convertNullToZero(salesPlanMaterialRequirementProduct
                .getDecimalField(SalesPlanMaterialRequirementProductFields.MINIMUM_ORDER_QUANTITY));
        BigDecimal orderedQuantity;

        if (minimumOrderQuantity.compareTo(neededQuantity) > 0) {
            orderedQuantity = minimumOrderQuantity;
        } else {
            orderedQuantity = neededQuantity;
        }

        BigDecimal conversion = getConversion(product);

        BigDecimal additionalQuantity = orderedQuantity.multiply(conversion, numberService.getMathContext());

        Entity orderedProduct = deliveriesService.getOrderedProductDD().create();

        orderedProduct.setField(OrderedProductFields.PRODUCT, product);
        orderedProduct.setField(OrderedProductFields.ORDERED_QUANTITY, orderedQuantity);
        orderedProduct.setField(OrderedProductFields.CONVERSION, conversion);
        orderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY, additionalQuantity);

        return orderedProduct;
    }

    private Optional<Entity> getSupplier(final List<Entity> salesPlanMaterialRequirementProducts) {
        return salesPlanMaterialRequirementProducts.stream().filter(salesPlanMaterialRequirementProduct -> Objects.nonNull(
                salesPlanMaterialRequirementProduct.getBelongsToField(SalesPlanMaterialRequirementProductFields.SUPPLIER)))
                .findFirst();
    }

    private BigDecimal getConversion(final Entity product) {
        String unit = product.getStringField(ProductFields.UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);

        if (Objects.isNull(additionalUnit)) {
            return BigDecimal.ONE;
        }

        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder
                        .add(SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));

        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    private Entity getSalesPlanMaterialRequirement(final Long salesPlanMaterialRequirementId) {
        return getSalesPlanMaterialRequirementDD().get(salesPlanMaterialRequirementId);
    }

    private List<Entity> getSalesPlanMaterialRequirementProducts(final Set<Long> salesPlanMaterialRequirementProductIds) {
        return getSalesPlanMaterialRequirementProductDD().find()
                .add(SearchRestrictions.in("id", salesPlanMaterialRequirementProductIds)).list().getEntities();
    }

    private DataDefinition getSalesPlanMaterialRequirementDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_MATERIAL_REQUIREMENT);
    }

    private DataDefinition getSalesPlanMaterialRequirementProductDD() {
        return dataDefinitionService.get(MasterOrdersConstants.PLUGIN_IDENTIFIER,
                MasterOrdersConstants.MODEL_SALES_PLAN_MATERIAL_REQUIREMENT_PRODUCT);
    }

}
