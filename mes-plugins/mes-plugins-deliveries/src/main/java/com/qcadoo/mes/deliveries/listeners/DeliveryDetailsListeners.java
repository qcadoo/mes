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
package com.qcadoo.mes.deliveries.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.UnitConversionItemFieldsB;
import com.qcadoo.mes.deliveries.DeliveredProductMultiPositionService;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.deliveries.ReservationService;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.deliveries.hooks.DeliveredProductDetailsHooks;
import com.qcadoo.mes.deliveries.hooks.DeliveryDetailsHooks;
import com.qcadoo.mes.deliveries.print.DeliveryReportPdf;
import com.qcadoo.mes.deliveries.print.OrderReportPdf;
import com.qcadoo.mes.deliveries.states.constants.DeliveryStateStringValues;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.units.PossibleUnitConversions;
import com.qcadoo.model.api.units.UnitConversionService;
import com.qcadoo.report.api.pdf.PdfHelper;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ComponentState.MessageType;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.api.utils.NumberGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class DeliveryDetailsListeners {

    private static final Integer REPORT_WIDTH_A4 = 515;

    private static final String L_FORM = "form";

    private static final String L_WINDOW_ACTIVE_MENU = "window.activeMenu";

    private static final String L_PRODUCT = "product";

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private DeliveryDetailsHooks deliveryDetailsHooks;

    @Autowired
    private DeliveredProductDetailsHooks deliveredProductDetailsHooks;

    @Autowired
    private NumberService numberService;

    @Autowired
    private NumberGeneratorService numberGeneratorService;

    @Autowired
    private OrderReportPdf orderReportPdf;

    @Autowired
    private PdfHelper pdfHelper;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DeliveryReportPdf deliveryReportPdf;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private DeliveredProductMultiPositionService deliveredProductMultiPositionService;

    @Autowired
    private UnitConversionService unitConversionService;

    public void fillCompanyFieldsForSupplier(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveryDetailsHooks.fillCompanyFieldsForSupplier(view);
    }

    public final void printDeliveryReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(view, "save", args);

            if (!state.isHasError()) {
                view.redirectTo("/deliveries/deliveryReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
            }
        } else {
            state.addMessage("deliveries.delivery.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void printOrderReport(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        if (state instanceof FormComponent) {
            state.performEvent(view, "save", args);

            if (!state.isHasError()) {
                view.redirectTo("/deliveries/orderReport." + args[0] + "?id=" + state.getFieldValue(), true, false);
            }
        } else {
            state.addMessage("deliveries.order.report.componentFormError", MessageType.FAILURE);
        }
    }

    public final void copyProductsWithoutQuantityAndPrice(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        copyOrderedProductToDelivered(view, false);
    }

    public final void copyProductsWithQuantityAndPrice(final ViewDefinitionState view, final ComponentState state,
            final String[] args) {
        copyOrderedProductToDelivered(view, true);
    }

    public final void recalculateReservations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Long deliveryId = form.getEntityId();
        reservationService.recalculateReservationsForDelivery(deliveryId);
        view.addMessage("deliveries.delivery.recalculateReservations", MessageType.SUCCESS);
    }

    public final void assignStorageLocations(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deleteOldEntries();
        FormComponent form = (FormComponent) view.getComponentByReference("form");
        Entity delivery = form.getPersistedEntityWithIncludedFormValues();
        Entity deliveredProductMultiEntity = createDeliveredProductMultiEntity(delivery, view);
        deliveredProductMultiEntity.getDataDefinition().save(deliveredProductMultiEntity);
        String url = "../page/deliveries/deliveredProductAddMulti.html?context={\"form.id\":\""
                + deliveredProductMultiEntity.getId() + "\"}";
        view.openModal(url);
    }

    private void deleteOldEntries() {
        DataDefinition deliveredProductMulti = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT_MULTI);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        List<Entity> oldEntries = deliveredProductMulti.find().add(SearchRestrictions.lt("updateDate", cal.getTime())).list()
                .getEntities();
        oldEntries.stream().forEach(e -> e.getDataDefinition().delete(e.getId()));
    }

    private Entity createDeliveredProductMultiEntity(Entity delivery, ViewDefinitionState view) {
        Entity deliveredProductMulti = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT_MULTI).create();
        deliveredProductMulti.setField("delivery", delivery);
        List<Entity> orderedProducts = getSelectedProducts(view);
        List<Entity> deliveredProducts =delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
        List<Entity> deliveredProductMultiPositions = Lists.newArrayList();
        DataDefinition deliveredProductMultiPositionDD = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                "deliveredProductMultiPosition");
        for (Entity orderedProduct : orderedProducts) {
            Entity deliveredProductMultiPosition = createDeliveredProductMultiPosition(orderedProduct,
                    deliveredProductMultiPositionDD, deliveredProducts);
            deliveredProductMultiPosition.setField("deliveredProductMulti", deliveredProductMulti);
            deliveredProductMultiPosition = deliveredProductMultiPositionDD.save(deliveredProductMultiPosition);
            deliveredProductMultiPositions.add(deliveredProductMultiPosition);
        }
        deliveredProductMulti.setField("deliveredProductMultiPositions", deliveredProductMultiPositions);
        return deliveredProductMulti.getDataDefinition().save(deliveredProductMulti);
    }

    private List<Entity> getSelectedProducts(ViewDefinitionState view) {
        GridComponent orderdProductGrid = (GridComponent) view.getComponentByReference("orderedProducts");
        List<Entity> result = Lists.newArrayList();
        Set<Long> ids = orderdProductGrid.getSelectedEntitiesIds();
        if (ids != null && !ids.isEmpty()) {
            final SearchCriteriaBuilder searchCriteria = deliveriesService.getOrderedProductDD().find();
            searchCriteria.add(SearchRestrictions.in("id", ids));
            result = searchCriteria.list().getEntities();
        }
        return result;
    }

    private List<Entity> getSelectedDeliveredProducts(ViewDefinitionState view) {
        GridComponent orderdProductGrid = (GridComponent) view.getComponentByReference("deliveredProducts");
        List<Entity> result = Lists.newArrayList();
        Set<Long> ids = orderdProductGrid.getSelectedEntitiesIds();
        if (ids != null && !ids.isEmpty()) {
            final SearchCriteriaBuilder searchCriteria = deliveriesService.getDeliveredProductDD().find();
            searchCriteria.add(SearchRestrictions.in("id", ids));
            result = searchCriteria.list().getEntities();
        }
        return result;
    }

    private Entity createDeliveredProductMultiPosition(Entity orderedProduct, DataDefinition deliveredProductMultiPositionDD,
            List<Entity> deliveredProducts) {
        Entity deliveredProductMuliPosition = deliveredProductMultiPositionDD.create();
        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
        String unit = product.getStringField(ProductFields.UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
        if (additionalUnit == null) {
            additionalUnit = unit;
        }
        Entity additionalCode = orderedProduct.getBelongsToField(OrderedProductFields.ADDITIONAL_CODE);
        BigDecimal alreadyAssignedQuantity = deliveredProductMultiPositionService.countAlreadyAssignedQuantity(orderedProduct,
                additionalCode, deliveredProducts);
        BigDecimal quantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY).subtract(
                alreadyAssignedQuantity);
        if (BigDecimal.ZERO.compareTo(quantity) >= 0) {
            quantity = BigDecimal.ZERO;
        }
        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);
        BigDecimal additionalQuantity = conversion.multiply(quantity);

        deliveredProductMuliPosition.setField(DeliveredProductMultiPositionFields.PRODUCT, product);
        deliveredProductMuliPosition.setField(DeliveredProductMultiPositionFields.UNIT, unit);
        deliveredProductMuliPosition.setField(DeliveredProductMultiPositionFields.ADDITIONAL_UNIT, additionalUnit);
        deliveredProductMuliPosition.setField(DeliveredProductMultiPositionFields.QUANTITY, quantity);
        deliveredProductMuliPosition.setField(DeliveredProductMultiPositionFields.ADDITIONAL_QUANTITY, additionalQuantity);
        deliveredProductMuliPosition.setField(DeliveredProductMultiPositionFields.ADDITIONAL_CODE,
                orderedProduct.getField(OrderedProductFields.ADDITIONAL_CODE));
        deliveredProductMuliPosition.setField(DeliveredProductMultiPositionFields.CONVERSION, conversion);
        return deliveredProductMuliPosition;
    }

    private void copyOrderedProductToDelivered(final ViewDefinitionState view, boolean copyQuantityAndPrice) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long deliveryId = deliveryForm.getEntityId();

        if (deliveryId == null) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        copyOrderedProductToDelivered(delivery, copyQuantityAndPrice);
    }

    private void copyOrderedProductToDelivered(final Entity delivery, final boolean copyQuantityAndPrice) {
        // ALBR deliveredProduct has a validation so we have to delete all
        // entities before save HM field in delivery
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS, Lists.newArrayList());
        delivery.getDataDefinition().save(delivery);
        delivery.setField(DeliveryFields.DELIVERED_PRODUCTS,
                createDeliveredProducts(delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS), copyQuantityAndPrice));

        delivery.getDataDefinition().save(delivery);
    }

    private List<Entity> createDeliveredProducts(final List<Entity> orderedProducts, final boolean copyQuantityAndPrice) {
        List<Entity> deliveredProducts = Lists.newArrayList();

        for (Entity orderedProduct : orderedProducts) {
            deliveredProducts.add(createDeliveredProduct(orderedProduct, copyQuantityAndPrice));
        }

        return deliveredProducts;
    }

    private Entity createDeliveredProduct(final Entity orderedProduct, final boolean copyQuantityAndPrice) {
        Entity deliveredProduct = deliveriesService.getDeliveredProductDD().create();
        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);

        deliveredProduct.setField(DeliveredProductFields.PRODUCT, product);
        deliveredProduct.setField(DeliveredProductFields.ADDITIONAL_CODE,
                orderedProduct.getBelongsToField(OrderedProductFields.ADDITIONAL_CODE));
        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);
        deliveredProduct.setField(DeliveredProductFields.CONVERSION, conversion);
        deliveredProduct.setField(DeliveredProductFields.IS_WASTE, false);

        if (copyQuantityAndPrice) {
            BigDecimal deliverdQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
            deliveredProduct.setField(DeliveredProductFields.DELIVERED_QUANTITY, numberService.setScale(deliverdQuantity));
            deliveredProduct.setField(DeliveredProductFields.ADDITIONAL_QUANTITY,
                    numberService.setScale(deliverdQuantity.multiply(conversion)));
            deliveredProduct.setField(DeliveredProductFields.PRICE_PER_UNIT,
                    numberService.setScale(orderedProduct.getDecimalField(OrderedProductFields.PRICE_PER_UNIT)));
            deliveredProduct.setField(DeliveredProductFields.TOTAL_PRICE,
                    numberService.setScale(orderedProduct.getDecimalField(OrderedProductFields.TOTAL_PRICE)));
        }

        return deliveredProduct;
    }

    public final void createRelatedDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long deliveryId = deliveryForm.getEntityId();

        if (deliveryId == null) {
            return;
        }

        Entity delivery = deliveriesService.getDelivery(deliveryId);

        if (DeliveryStateStringValues.RECEIVED.equals(delivery.getStringField(DeliveryFields.STATE))
                || DeliveryStateStringValues.RECEIVE_CONFIRM_WAITING.equals(delivery.getStringField(DeliveryFields.STATE))) {
            Entity relatedDelivery = createRelatedDelivery(delivery);

            if (relatedDelivery == null) {
                deliveryForm.addMessage("deliveries.delivery.relatedDelivery.thereAreNoLacksToCover", MessageType.INFO);

                return;
            }

            Long relatedDeliveryId = relatedDelivery.getId();

            if (relatedDeliveryId == null) {
                deliveryForm.addMessage("deliveries.delivery.relatedDelivery.invalidDelivery", MessageType.FAILURE);
                return;
            }
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("form.id", relatedDeliveryId);

            parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.deliveries");

            String url = "../page/deliveries/deliveryDetails.html";
            view.redirectTo(url, false, true, parameters);
        }
    }

    private Entity createRelatedDelivery(final Entity delivery) {
        Entity relatedDelivery = null;

        List<Entity> orderedProducts = createOrderedProducts(delivery);

        if (!orderedProducts.isEmpty()) {
            relatedDelivery = deliveriesService.getDeliveryDD().create();

            relatedDelivery.setField(DeliveryFields.NUMBER, numberGeneratorService.generateNumber(
                    DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERY));
            relatedDelivery.setField(DeliveryFields.SUPPLIER, delivery.getBelongsToField(DeliveryFields.SUPPLIER));
            relatedDelivery.setField(DeliveryFields.DELIVERY_DATE, new Date());
            relatedDelivery.setField(DeliveryFields.RELATED_DELIVERY, delivery);
            relatedDelivery.setField(DeliveryFields.ORDERED_PRODUCTS, orderedProducts);
            relatedDelivery.setField(DeliveryFields.EXTERNAL_SYNCHRONIZED, true);
            relatedDelivery.setField(DeliveryFields.LOCATION, delivery.getBelongsToField(DeliveryFields.LOCATION));

            relatedDelivery = relatedDelivery.getDataDefinition().save(relatedDelivery);
        }

        return relatedDelivery;
    }

    private List<Entity> createOrderedProducts(final Entity delivery) {
        List<Entity> newOrderedProducts = Lists.newArrayList();

        List<Entity> orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);
        List<Entity> deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        for (Entity orderedProduct : orderedProducts) {
            Entity deliveredProduct = getDeliveredProduct(deliveredProducts, orderedProduct);

            if (deliveredProduct == null) {
                BigDecimal orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);

                newOrderedProducts.add(createOrderedProduct(orderedProduct, orderedQuantity, null));
            } else {
                BigDecimal orderedQuantity = getLackQuantity(orderedProduct, deliveredProduct);

                if (BigDecimal.ZERO.compareTo(orderedQuantity) < 0) {
                    newOrderedProducts.add(createOrderedProduct(orderedProduct, orderedQuantity, deliveredProduct));
                }
            }
        }

        return newOrderedProducts;
    }

    private Entity getDeliveredProduct(final List<Entity> deliveredProducts, final Entity orderedProduct) {
        for (Entity deliveredProduct : deliveredProducts) {
            if (checkIfProductsAreSame(orderedProduct, deliveredProduct)) {
                return deliveredProduct;
            }
        }

        return null;
    }

    private boolean checkIfProductsAreSame(final Entity orderedProduct, final Entity deliveredProduct) {
        Entity orderedAdditionalCode = orderedProduct.getBelongsToField(OrderedProductFields.ADDITIONAL_CODE);
        Entity deliveredAdditionalCode = deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE);
        boolean additionalCodesMatching = orderedAdditionalCode == null && deliveredAdditionalCode == null;
        if (orderedAdditionalCode != null && deliveredAdditionalCode != null) {
            additionalCodesMatching = orderedAdditionalCode.getId().equals(deliveredAdditionalCode.getId());
        }
        return (orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT).getId().equals(
                deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getId()))
                && additionalCodesMatching;
    }

    private Entity createOrderedProduct(final Entity orderedProduct, final BigDecimal orderedQuantity,
            final Entity deliveredProduct) {
        Entity newOrderedProduct = deliveriesService.getOrderedProductDD().create();

        Entity product = orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT);
        BigDecimal conversion = orderedProduct.getDecimalField(OrderedProductFields.CONVERSION);

        newOrderedProduct.setField(OrderedProductFields.PRODUCT, product);
        newOrderedProduct.setField(OrderedProductFields.ADDITIONAL_CODE,
                orderedProduct.getBelongsToField(OrderedProductFields.ADDITIONAL_CODE));
        newOrderedProduct.setField(OrderedProductFields.ORDERED_QUANTITY, numberService.setScale(orderedQuantity));
        newOrderedProduct.setField(OrderedProductFields.TOTAL_PRICE,
                orderedProduct.getDecimalField(OrderedProductFields.TOTAL_PRICE));
        newOrderedProduct.setField(OrderedProductFields.PRICE_PER_UNIT,
                orderedProduct.getDecimalField(OrderedProductFields.PRICE_PER_UNIT));
        newOrderedProduct.setField(OrderedProductFields.CONVERSION, conversion);
        newOrderedProduct.setField(OrderedProductFields.ADDITIONAL_QUANTITY, BigDecimalUtils.convertNullToZero(orderedQuantity)
                .multiply(conversion, numberService.getMathContext()));

        newOrderedProduct.setField(OrderedProductFields.RESERVATIONS,
                copyReservations(orderedProduct, newOrderedProduct, deliveredProduct));
        return newOrderedProduct;
    }

    private List<Entity> copyReservations(final Entity orderedProduct, final Entity newOrderedProduct,
            final Entity deliveredProduct) {
        List<Entity> newReservations = Lists.newArrayList();
        List<Entity> oldReservations = orderedProduct.getHasManyField(OrderedProductFields.RESERVATIONS);
        BigDecimal availableQuantity = newOrderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
        for (Entity oldReservation : oldReservations) {
            Entity location = oldReservation.getBelongsToField(OrderedProductReservationFields.LOCATION);
            BigDecimal deliveredReservedQuantity = getDeliveredReservedQuantity(deliveredProduct, location);

            BigDecimal quantity = BigDecimalUtils.convertNullToZero(
                    oldReservation.getDecimalField(OrderedProductReservationFields.ORDERED_QUANTITY));

            if (availableQuantity.compareTo(quantity) < 0) {
                quantity = availableQuantity;
            }
            quantity = quantity.subtract(deliveredReservedQuantity);
            if (quantity.compareTo(BigDecimal.ZERO) > 0) {

                Entity newReservation = dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                        DeliveriesConstants.MODEL_ORDERED_PRODUCT_RESERVATION).create();
                BigDecimal conversion = getConversion(orderedProduct.getBelongsToField(OrderedProductFields.PRODUCT));
                newReservation.setField(OrderedProductReservationFields.LOCATION,
                        oldReservation.getBelongsToField(OrderedProductReservationFields.LOCATION));
                newReservation.setField(OrderedProductReservationFields.ORDERED_PRODUCT,
                        oldReservation.getBelongsToField(OrderedProductReservationFields.ORDERED_PRODUCT));
                newReservation.setField(OrderedProductReservationFields.ORDERED_QUANTITY, quantity);
                newReservation.setField(OrderedProductReservationFields.ORDERED_QUANTITY_UNIT,
                        oldReservation.getStringField(OrderedProductReservationFields.ORDERED_QUANTITY_UNIT));
                newReservation.setField(OrderedProductReservationFields.ADDITIONAL_QUANTITY,
                        BigDecimalUtils.convertNullToZero(quantity).multiply(conversion, numberService.getMathContext()));
                newReservation.setField(OrderedProductReservationFields.ADDITIONAL_QUANTITY_UNIT,
                        oldReservation.getStringField(OrderedProductReservationFields.ADDITIONAL_QUANTITY_UNIT));
                newReservation.setField(OrderedProductReservationFields.ORDERED_PRODUCT, newOrderedProduct);
                newReservations.add(newReservation);
                availableQuantity = availableQuantity.subtract(quantity);
            }
        }
        return newReservations;
    }

    private BigDecimal getDeliveredReservedQuantity(final Entity deliveredProduct, final Entity location) {
        BigDecimal quantity = BigDecimal.ZERO;
        if (deliveredProduct != null) {
            List<Entity> reservations = deliveredProduct.getHasManyField(DeliveredProductFields.RESERVATIONS);
            quantity = reservations
                    .stream()
                    .filter(reservation -> reservation.getBelongsToField(DeliveredProductReservationFields.LOCATION).getId()
                            .equals(location.getId()))
                    .map(reservation -> reservation.getDecimalField(DeliveredProductReservationFields.DELIVERED_QUANTITY))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return quantity;
    }

    public BigDecimal getConversion(Entity product) {
        String unit = product.getStringField(ProductFields.UNIT);
        String additionalUnit = product.getStringField(ProductFields.ADDITIONAL_UNIT);
        if (additionalUnit == null) {
            return BigDecimal.ONE;
        }
        PossibleUnitConversions unitConversions = unitConversionService.getPossibleConversions(unit,
                searchCriteriaBuilder -> searchCriteriaBuilder.add(
                        SearchRestrictions.belongsTo(UnitConversionItemFieldsB.PRODUCT, product)));
        if (unitConversions.isDefinedFor(additionalUnit)) {
            return unitConversions.asUnitToConversionMap().get(additionalUnit);
        } else {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getLackQuantity(final Entity orderedProduct, final Entity deliveredProduct) {
        BigDecimal orderedQuantity = orderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
        BigDecimal deliveredQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
        BigDecimal damagedQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DAMAGED_QUANTITY);

        if (damagedQuantity == null) {
            return orderedQuantity.subtract(deliveredQuantity, numberService.getMathContext());
        } else {
            return orderedQuantity.subtract(deliveredQuantity, numberService.getMathContext()).add(damagedQuantity,
                    numberService.getMathContext());
        }
    }

    public final void showRelatedDeliveries(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent deliveryForm = (FormComponent) view.getComponentByReference(L_FORM);
        Long deliveryId = deliveryForm.getEntityId();

        if (deliveryId == null) {
            return;
        }

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("delivery.id", deliveryId);

        parameters.put(L_WINDOW_ACTIVE_MENU, "requirements.deliveries");

        String url = "../page/deliveries/deliveriesList.html";
        view.redirectTo(url, false, true, parameters);
    }

    public final void showProduct(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        GridComponent orderedProductGrid = (GridComponent) view.getComponentByReference(DeliveryFields.ORDERED_PRODUCTS);
        GridComponent deliveredProductsGrid = (GridComponent) view.getComponentByReference(DeliveryFields.DELIVERED_PRODUCTS);

        List<Entity> selectedEntities = orderedProductGrid.getSelectedEntities();
        if (selectedEntities.isEmpty()) {
            selectedEntities = deliveredProductsGrid.getSelectedEntities();
        }

        Entity selectedEntity = selectedEntities.iterator().next();
        selectedEntity = selectedEntity.getDataDefinition().getMasterModelEntity(selectedEntity.getId());
        Entity product = selectedEntity.getBelongsToField(L_PRODUCT);

        Map<String, Object> parameters = Maps.newHashMap();

        parameters.put("form.id", product.getId());

        parameters.put(L_WINDOW_ACTIVE_MENU, "basic.products");

        String url = "../page/basic/productDetails.html";
        view.redirectTo(url, false, true, parameters);
    }

    public void disableShowProductButton(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        deliveriesService.disableShowProductButton(view);
    }

    public void validateColumnsWidthForOrder(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long deliveryId = ((FormComponent) view.getComponentByReference("form")).getEntity().getId();
        Entity delivery = deliveriesService.getDelivery(deliveryId);
        List<String> columnNames = orderReportPdf.getUsedColumnsInOrderReport(delivery);
        if (!pdfHelper.validateReportColumnWidths(REPORT_WIDTH_A4, parameterService.getReportColumnWidths(), columnNames)) {
            state.addMessage("deliveries.delivery.printOrderReport.columnsWidthIsGreaterThenMax", MessageType.INFO, false);
        }
    }

    public void validateColumnsWidthForDelivery(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        Long deliveryId = ((FormComponent) view.getComponentByReference("form")).getEntity().getId();
        Entity delivery = deliveriesService.getDelivery(deliveryId);
        List<String> columnNames = deliveryReportPdf.getUsedColumnsInDeliveryReport(delivery);
        if (!pdfHelper.validateReportColumnWidths(REPORT_WIDTH_A4, parameterService.getReportColumnWidths(), columnNames)) {
            state.addMessage("deliveries.delivery.printOrderReport.columnsWidthIsGreaterThenMax", MessageType.INFO, false);
        }
    }

}
