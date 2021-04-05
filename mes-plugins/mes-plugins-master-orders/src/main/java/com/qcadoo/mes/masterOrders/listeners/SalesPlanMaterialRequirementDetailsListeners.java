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
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrdersConstants;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementFields;
import com.qcadoo.mes.masterOrders.constants.SalesPlanMaterialRequirementProductFields;
import com.qcadoo.mes.masterOrders.helpers.SalesPlanMaterialRequirementHelper;
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
    private NumberService numberService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private UnitConversionService unitConversionService;

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private SalesPlanMaterialRequirementHelper salesPlanMaterialRequirementHelper;

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

            List<Entity> salesPlanMaterialRequirementProducts = salesPlanMaterialRequirementHelper
                    .generateSalesPlanMaterialRequirementProducts(salesPlanMaterialRequirement);

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
            } else {
                view.addMessage("masterOrders.salesPlanMaterialRequirement.createDelivery.info", ComponentState.MessageType.INFO);
            }
        }
    }

    private Entity createDelivery(final Entity salesPlanMaterialRequirement, final List<Entity> salesPlanMaterialRequirementProducts) {
        Entity delivery = deliveriesService.getDeliveryDD().create();

        Optional<Entity> mayBeSupplier = getSupplier(salesPlanMaterialRequirementProducts);
        List<Entity> orderedProducts = createOrderedProducts(salesPlanMaterialRequirementProducts);

        if (!orderedProducts.isEmpty()) {
            delivery.setField(DeliveryFields.NUMBER, numberGeneratorService.generateNumber(DeliveriesConstants.PLUGIN_IDENTIFIER,
                    DeliveriesConstants.MODEL_DELIVERY));

            if (mayBeSupplier.isPresent()) {
                delivery.setField(DeliveryFields.SUPPLIER, mayBeSupplier.get());
            }

            delivery.setField(DeliveryFields.ORDERED_PRODUCTS, orderedProducts);
            delivery.setField(DeliveryFields.EXTERNAL_SYNCHRONIZED, true);

            delivery = delivery.getDataDefinition().save(delivery);
        }

        return delivery;
    }

    private List<Entity> createOrderedProducts(final List<Entity> salesPlanMaterialRequirementProducts) {
        List<Entity> orderedProducts = Lists.newArrayList();

        salesPlanMaterialRequirementProducts.forEach(salesPlanMaterialRequirementProduct -> createOrderedProduct(orderedProducts,
                salesPlanMaterialRequirementProduct));

        return orderedProducts;
    }

    private Entity createOrderedProduct(final List<Entity> orderedProducts, final Entity salesPlanMaterialRequirementProduct) {
        Entity product = salesPlanMaterialRequirementProduct.getBelongsToField(SalesPlanMaterialRequirementProductFields.PRODUCT);
        BigDecimal quantity = BigDecimalUtils.convertNullToZero(
                salesPlanMaterialRequirementProduct.getDecimalField(SalesPlanMaterialRequirementProductFields.QUANTITY));
        BigDecimal currentStock = BigDecimalUtils.convertNullToZero(
                salesPlanMaterialRequirementProduct.getDecimalField(SalesPlanMaterialRequirementProductFields.CURRENT_STOCK));
        BigDecimal neededQuantity = BigDecimalUtils.convertNullToZero(
                salesPlanMaterialRequirementProduct.getDecimalField(SalesPlanMaterialRequirementProductFields.NEEDED_QUANTITY));
        BigDecimal minimumOrderQuantity = BigDecimalUtils.convertNullToZero(salesPlanMaterialRequirementProduct
                .getDecimalField(SalesPlanMaterialRequirementProductFields.MINIMUM_ORDER_QUANTITY));

        BigDecimal conversion = getConversion(product);
        BigDecimal orderedQuantity = getOrderedQuantity(quantity, currentStock, neededQuantity, minimumOrderQuantity);
        BigDecimal additionalQuantity;

        Optional<Entity> mayBeOrderedProduct = orderedProducts.stream()
                .filter(orderedProduct -> filterByProduct(orderedProduct, salesPlanMaterialRequirementProduct)).findFirst();

        Entity orderedProduct;

        if (mayBeOrderedProduct.isPresent()) {
            orderedProduct = mayBeOrderedProduct.get();

            orderedQuantity = orderedQuantity.add(orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY),
                    numberService.getMathContext());
            additionalQuantity = orderedQuantity.multiply(conversion, numberService.getMathContext());

            orderedProduct.setField(OrderedProductFields.ORDERED_QUANTITY, orderedQuantity);
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY, additionalQuantity);
        } else {
            additionalQuantity = orderedQuantity.multiply(conversion, numberService.getMathContext());

            orderedProduct = deliveriesService.getOrderedProductDD().create();

            orderedProduct.setField(OrderedProductFields.PRODUCT, product);
            orderedProduct.setField(OrderedProductFields.CONVERSION, conversion);
            orderedProduct.setField(OrderedProductFields.ORDERED_QUANTITY, orderedQuantity);
            orderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY, additionalQuantity);

            if (BigDecimal.ZERO.compareTo(orderedQuantity) <= 0) {
                orderedProducts.add(orderedProduct);

                salesPlanMaterialRequirementProduct.setField(SalesPlanMaterialRequirementProductFields.IS_DELIVERY_CREATED, true);

                salesPlanMaterialRequirementProduct.getDataDefinition().save(salesPlanMaterialRequirementProduct);
            }
        }

        return orderedProduct;
    }

    private BigDecimal getOrderedQuantity(final BigDecimal quantity, final BigDecimal currentStock,
            final BigDecimal neededQuantity, final BigDecimal minimumOrderQuantity) {
        BigDecimal orderedQuantity;

        if (BigDecimal.ZERO.compareTo(quantity) == 0) {
            orderedQuantity = quantity;
        } else {
            orderedQuantity = quantity.subtract(currentStock, numberService.getMathContext()).add(neededQuantity,
                    numberService.getMathContext());

            if (BigDecimal.ZERO.compareTo(orderedQuantity) < 0) {
                if (orderedQuantity.compareTo(minimumOrderQuantity) < 0) {
                    orderedQuantity = minimumOrderQuantity;
                }
            }
        }

        return orderedQuantity;
    }

    private boolean filterByProduct(final Entity orderedProduct, final Entity salesPlanMaterialRequirementProduct) {
        Entity orderedProductProduct = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
        Entity salesPlanMaterialRequirementProductProduct = salesPlanMaterialRequirementProduct
                .getBelongsToField(SalesPlanMaterialRequirementProductFields.PRODUCT);

        return Objects.nonNull(orderedProductProduct) && Objects.nonNull(salesPlanMaterialRequirementProductProduct)
                && orderedProductProduct.getId().equals(salesPlanMaterialRequirementProductProduct.getId());
    }

    private Optional<Entity> getSupplier(final List<Entity> salesPlanMaterialRequirementProducts) {
        return salesPlanMaterialRequirementProducts.stream()
                .filter(salesPlanMaterialRequirementProduct -> Objects.nonNull(salesPlanMaterialRequirementProduct
                        .getBelongsToField(SalesPlanMaterialRequirementProductFields.SUPPLIER)))
                .map(salesPlanMaterialRequirementProduct -> salesPlanMaterialRequirementProduct
                        .getBelongsToField(SalesPlanMaterialRequirementProductFields.SUPPLIER))
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
