package com.qcadoo.mes.deliveries;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.CalculationQuantityService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.*;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.model.api.*;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.plugin.api.PluginUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    public static final String OFFER = "offer";

    public static final String OPERATION = "operation";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private CalculationQuantityService calculationQuantityService;

    public Entity createDefaultReservationsForDeliveredProduct(final Entity deliveredProduct) {
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        if (Objects.nonNull(product) && !deliveredProduct.getBooleanField(DeliveredProductFields.IS_WASTE)) {
            List<Entity> deliveredProductReservations = Lists.newArrayList();

            Entity orderedProductForProduct = findOrderedProductForProduct(deliveredProduct);
            List<Entity> presentDeliveredProductForProductReservations = findPresentDeliveredProductForProductReservations(
                    deliveredProduct);

            if (Objects.nonNull(orderedProductForProduct)) {
                EntityList reservationsFromOrderedProduct = orderedProductForProduct
                        .getHasManyField(OrderedProductFields.RESERVATIONS);

                BigDecimal damagedQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DAMAGED_QUANTITY);
                damagedQuantity = Objects.isNull(damagedQuantity) ? BigDecimal.ZERO : damagedQuantity;

                BigDecimal availableQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
                availableQuantity = Objects.isNull(availableQuantity) ? BigDecimal.ZERO : availableQuantity;
                availableQuantity = availableQuantity.subtract(damagedQuantity);

                for (Entity reservationFromOrderedProduct : reservationsFromOrderedProduct) {
                    Optional<Entity> maybeDeliveredProductReservation = createDeliveredProductReservation(availableQuantity,
                            deliveredProduct, reservationFromOrderedProduct, presentDeliveredProductForProductReservations);

                    if (maybeDeliveredProductReservation.isPresent()) {
                        Entity deliveredProductReservation = maybeDeliveredProductReservation.get();

                        deliveredProductReservations.add(deliveredProductReservation);
                        presentDeliveredProductForProductReservations.add(deliveredProductReservation);

                        availableQuantity = availableQuantity.subtract(deliveredProductReservation
                                .getDecimalField(DeliveredProductReservationFields.DELIVERED_QUANTITY));
                    }
                }
            }

            deliveredProduct.setField(DeliveredProductFields.RESERVATIONS, deliveredProductReservations);
        }

        return deliveredProduct;
    }

    private Optional<Entity> createDeliveredProductReservation(final BigDecimal availableQuantity, final Entity deliveredProduct,
            final Entity reservationFromOrderedProduct, final List<Entity> presentDeliveredProductForProductReservations) {
        Entity location = reservationFromOrderedProduct.getBelongsToField(OrderedProductReservationFields.LOCATION);
        List<Entity> reservationsFromLocation = filterReservationsByLocation(presentDeliveredProductForProductReservations,
                location);

        BigDecimal orderedQuantity = reservationFromOrderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
        BigDecimal deliveredQuantity = sumQuantityFromReservations(reservationsFromLocation,
                DeliveredProductReservationFields.DELIVERED_QUANTITY);
        BigDecimal requestQuantity = orderedQuantity.subtract(deliveredQuantity);
        BigDecimal currentDeliveredQuantity = requestQuantity.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO
                : requestQuantity.min(availableQuantity);

        BigDecimal conversion = deliveredProduct.getDecimalField(DeliveredProductFields.CONVERSION);

        if (Objects.isNull(conversion)) {
            return Optional.empty();
        }

        BigDecimal currentDeliveredAdditionalQuantity = calculationQuantityService.calculateAdditionalQuantity(
                currentDeliveredQuantity, conversion,
                deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT).getStringField(ProductFields.ADDITIONAL_UNIT));

        if (currentDeliveredQuantity.compareTo(BigDecimal.ZERO) <= 0
                || currentDeliveredAdditionalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        Entity deliveredProductReservation = getDeliveredProductReservationDD().create();

        deliveredProductReservation.setField(DeliveredProductReservationFields.ADDITIONAL_QUANTITY,
                currentDeliveredAdditionalQuantity);
        deliveredProductReservation.setField(DeliveredProductReservationFields.ADDITIONAL_QUANTITY_UNIT,
                reservationFromOrderedProduct.getStringField(OrderedProductReservationFields.ADDITIONAL_QUANTITY_UNIT));
        deliveredProductReservation.setField(DeliveredProductReservationFields.DELIVERED_PRODUCT, deliveredProduct);
        deliveredProductReservation.setField(DeliveredProductReservationFields.DELIVERED_QUANTITY, currentDeliveredQuantity);
        deliveredProductReservation.setField(DeliveredProductReservationFields.DELIVERED_QUANTITY_UNIT,
                reservationFromOrderedProduct.getStringField(OrderedProductReservationFields.ORDERED_QUANTITY_UNIT));
        deliveredProductReservation.setField(DeliveredProductReservationFields.LOCATION, location);

        return Optional.of(deliveredProductReservation);
    }

    public void recalculateReservationsForDelivery(final Long deliveryId) {
        SearchCriterion criterion = SearchRestrictions.eq(DeliveredProductFields.DELIVERY + ".id", deliveryId);
        DataDefinition deliveredProductDD = getDeliveredProductDD();

        List<Entity> deliveredProducts = deliveredProductDD.find().add(criterion).list().getEntities();

        deletePreviousReservations(deliveredProducts);

        for (Entity deliveredProduct : deliveredProducts) {
            if (!deliveredProduct.getBooleanField(DeliveredProductFields.IS_WASTE)) {
                deliveredProduct = createDefaultReservationsForDeliveredProduct(deliveredProduct);
                deliveredProductDD.save(deliveredProduct);
            }
        }
    }

    private boolean deletePreviousReservations(final List<Entity> orderedOrDeliveredProducts) {
        int countReservations = 0;

        for (Entity p : orderedOrDeliveredProducts) {
            EntityList reservations = p.getHasManyField(DeliveredProductFields.RESERVATIONS);

            countReservations += reservations.size();

            reservations.stream().forEach(reservation -> reservation.getDataDefinition().delete(reservation.getId()));
        }

        return countReservations > 0;
    }

    private Entity findOrderedProductForProduct(final Entity deliveredProduct) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        Entity batch = deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);

        SearchCriteriaBuilder findOrderedProduct = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS).find();

        findOrderedProduct.add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT, product));

        if (Objects.isNull(batch)) {
            findOrderedProduct.add(SearchRestrictions.isNull(OrderedProductFields.BATCH));
        } else {
            findOrderedProduct.add(SearchRestrictions.belongsTo(OrderedProductFields.BATCH, batch));
        }
        if (PluginUtils.isEnabled("supplyNegotiations")) {
            findOrderedProduct.add(SearchRestrictions.belongsTo(OFFER, deliveredProduct.getBelongsToField(OFFER)));
        }
        if (PluginUtils.isEnabled("techSubcontrForDeliveries")) {
            findOrderedProduct.add(SearchRestrictions.belongsTo(OPERATION, deliveredProduct.getBelongsToField(OPERATION)));
        }

        return findOrderedProduct.setMaxResults(1).uniqueResult();
    }

    private List<Entity> findPresentDeliveredProductForProductReservations(final Entity deliveredProduct) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity batch = deliveredProduct.getBelongsToField(DeliveredProductFields.BATCH);

        SearchCriteriaBuilder findDeliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS).find();

        findDeliveredProducts.add(SearchRestrictions.belongsTo(DeliveredProductFields.PRODUCT,
                deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)));

        if (Objects.isNull(batch)) {
            findDeliveredProducts.add(SearchRestrictions.isNull(DeliveredProductFields.BATCH));
        } else {
            findDeliveredProducts.add(SearchRestrictions.belongsTo(DeliveredProductFields.BATCH, batch));
        }
        if (PluginUtils.isEnabled("supplyNegotiations")) {
            findDeliveredProducts.add(SearchRestrictions.belongsTo(OFFER, deliveredProduct.getBelongsToField(OFFER)));
        }
        List<Entity> deliveredProducts = findDeliveredProducts.list().getEntities();

        List<Entity> allReservationsFromDeliveredProducts = deliveredProducts.stream()
                .flatMap(p -> p.getHasManyField(DeliveredProductFields.RESERVATIONS).stream()).collect(Collectors.toList());

        return allReservationsFromDeliveredProducts;
    }

    private List<Entity> filterReservationsByLocation(final List<Entity> presentDeliveredProductForProductReservations,
            Entity location) {
        List<Entity> reservationsFromLocation = presentDeliveredProductForProductReservations.stream().filter(r -> {
            Entity reservationLocation = r.getBelongsToField(OrderedProductReservationFields.LOCATION);
            return location.getId().equals(reservationLocation.getId());
        }).collect(Collectors.toList());

        return reservationsFromLocation;
    }

    private BigDecimal sumQuantityFromReservations(final List<Entity> reservationsFromLocation, final String fieldName) {
        BigDecimal quantity = reservationsFromLocation.stream().map(r -> r.getDecimalField(fieldName)).reduce(BigDecimal.ZERO,
                (r1, r2) -> r1.add(r2));

        return quantity;
    }

    public void deleteReservationsForOrderedProductIfChanged(final Entity orderedProduct) {
        if (Objects.nonNull(orderedProduct.getId())) {
            Entity orderedProductFromDB = orderedProduct.getDataDefinition().get(orderedProduct.getId());

            List<String> fieldNames = Lists.newArrayList(OrderedProductFields.ORDERED_QUANTITY, OrderedProductFields.PRODUCT);

            for (String fieldName : fieldNames) {
                if (notEquals(orderedProduct.getField(fieldName), orderedProductFromDB.getField(fieldName))) {
                    if (deletePreviousReservations(Arrays.asList(orderedProductFromDB))) {
                        orderedProduct.addGlobalMessage("deliveries.delivery.message.reservationsDeletedFromOrderedProduct");
                    }

                    break;
                }
            }
        }
    }

    public void deleteReservationsForDeliveredProductIfChanged(Entity deliveredProduct) {
        if (Objects.nonNull(deliveredProduct.getId())) {
            Entity deliveredProductFromDB = deliveredProduct.getDataDefinition().get(deliveredProduct.getId());

            List<String> fieldNames = Arrays.asList(DeliveredProductFields.DELIVERED_QUANTITY,
                    DeliveredProductFields.DAMAGED_QUANTITY, DeliveredProductFields.PRODUCT, DeliveredProductFields.IS_WASTE);

            for (String fieldName : fieldNames) {
                if (notEquals(deliveredProduct.getField(fieldName), deliveredProductFromDB.getField(fieldName))) {
                    if (deletePreviousReservations(Arrays.asList(deliveredProductFromDB))) {
                        deliveredProduct.addGlobalMessage("deliveries.delivery.message.reservationsDeletedFromDeliveredProduct");
                    }

                    break;
                }
            }
        }
    }

    private boolean notEquals(final Object a, final Object b) {
        if (Objects.nonNull(a) && Objects.nonNull(b)) {
            if (a instanceof BigDecimal && b instanceof BigDecimal) {
                return ((BigDecimal) a).compareTo((BigDecimal) b) != 0;
            }

            return !a.equals(b);
        } else {
            return a != b;
        }
    }

    public boolean validateDeliveryAgainstReservations(final Entity delivery) {
        return validateDeliveryOrderedProductsAgainstReservations(delivery)
                && validateDeliveryDeliveredProductsAgainstReservations(delivery);
    }

    private boolean validateDeliveryOrderedProductsAgainstReservations(final Entity delivery) {
        Entity deliveryLocation = delivery.getBelongsToField(DeliveryFields.LOCATION);
        EntityList orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);

        if (Objects.nonNull(orderedProducts) && Objects.nonNull(deliveryLocation)) {
            for (Entity orderedProduct : orderedProducts) {
                EntityList reservations = orderedProduct.getHasManyField(OrderedProductFields.RESERVATIONS);

                if (Objects.nonNull(reservations)) {
                    for (Entity reservation : reservations) {
                        Entity reservationLocation = reservation.getBelongsToField(OrderedProductReservationFields.LOCATION);

                        if (deliveryLocation.getId().equals(reservationLocation.getId())) {
                            FieldDefinition locationField = delivery.getDataDefinition().getField(DeliveryFields.LOCATION);
                            delivery.addError(locationField, "deliveries.delivery.error.locationNotUniqueToDelivery",
                                    deliveryLocation.getStringField(LocationFields.NUMBER));

                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean validateDeliveryDeliveredProductsAgainstReservations(final Entity delivery) {
        Entity deliveryLocation = delivery.getBelongsToField(DeliveryFields.LOCATION);
        EntityList deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        if (Objects.nonNull(deliveredProducts) && Objects.nonNull(deliveryLocation)) {
            for (Entity deliveredProduct : deliveredProducts) {
                EntityList reservations = deliveredProduct.getHasManyField(DeliveredProductFields.RESERVATIONS);

                if (Objects.nonNull(reservations)) {
                    for (Entity reservation : reservations) {
                        Entity reservationLocation = reservation.getBelongsToField(DeliveredProductReservationFields.LOCATION);

                        if (deliveryLocation.getId().equals(reservationLocation.getId())) {
                            FieldDefinition locationField = delivery.getDataDefinition().getField(DeliveryFields.LOCATION);
                            delivery.addError(locationField, "deliveries.delivery.error.locationNotUniqueToDelivery",
                                    deliveryLocation.getStringField(LocationFields.NUMBER));

                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private DataDefinition getDeliveredProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
    }

    private DataDefinition getDeliveredProductReservationDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT_RESERVATION);
    }

}
