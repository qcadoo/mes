package com.qcadoo.mes.deliveries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductReservationFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductReservationFields;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.FieldDefinition;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ReservationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity createDefaultReservationsForDeliveredProduct(Entity deliveredProduct) {
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        if (product != null && !deliveredProduct.getBooleanField(DeliveredProductFields.IS_WASTE)) {
            List<Entity> deliveredProductReservations = new ArrayList<>();

            Entity orderedProductForProduct = findOrderedProductForProduct(deliveredProduct);
            List<Entity> presentDeliveredProductForProductReservations = findPresentDeliveredProductForProductReservations(
                    deliveredProduct);

            if (orderedProductForProduct != null) {
                EntityList reservationsFromOrderedProduct = orderedProductForProduct
                        .getHasManyField(OrderedProductFields.RESERVATIONS);

                BigDecimal damagedQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DAMAGED_QUANTITY);
                damagedQuantity = damagedQuantity == null ? BigDecimal.ZERO : damagedQuantity;

                BigDecimal availableQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
                availableQuantity = availableQuantity == null ? BigDecimal.ZERO : availableQuantity;
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

    private Optional<Entity> createDeliveredProductReservation(BigDecimal availableQuantity, final Entity deliveredProduct,
            final Entity reservationFromOrderedProduct, List<Entity> presentDeliveredProductForProductReservations) {
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
        if (conversion == null) {
            return Optional.empty();
        }
        BigDecimal currentDeliveredAdditionalQuantity = currentDeliveredQuantity.multiply(conversion);

        if (currentDeliveredQuantity.compareTo(BigDecimal.ZERO) <= 0
                || currentDeliveredAdditionalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        DataDefinition deliveredProductReservationDD = getDeliveredProductReservationDD();
        Entity deliveredProductReservation = deliveredProductReservationDD.create();
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

    public void recalculateReservationsForDelivery(Long deliveryId) {
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

    private DataDefinition getDeliveredProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
    }

    private DataDefinition getDeliveredProductReservationDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT_RESERVATION);
    }

    private boolean deletePreviousReservations(List<Entity> orderedOrDeliveredProducts) {
        int countReservations = 0;
        for (Entity p : orderedOrDeliveredProducts) {
            EntityList reservations = p.getHasManyField(DeliveredProductFields.RESERVATIONS);
            countReservations += reservations.size();
            reservations.stream().forEach(reservation -> {
                reservation.getDataDefinition().delete(reservation.getId());
            });
        }

        return countReservations > 0;
    }

    private Entity findOrderedProductForProduct(Entity deliveredProduct) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity additionalCode = deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE);
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);

        SearchCriteriaBuilder findOrderedProduct = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS).find();
        findOrderedProduct.add(SearchRestrictions.belongsTo(OrderedProductFields.PRODUCT, product));
        if (additionalCode == null) {
            findOrderedProduct.add(SearchRestrictions.isNull(OrderedProductFields.ADDITIONAL_CODE));
        } else {
            findOrderedProduct.add(SearchRestrictions.belongsTo(OrderedProductFields.ADDITIONAL_CODE, additionalCode));
        }
        Entity orderedProductForProduct = findOrderedProduct.uniqueResult();

        return orderedProductForProduct;
    }

    private List<Entity> findPresentDeliveredProductForProductReservations(Entity deliveredProduct) {
        Entity delivery = deliveredProduct.getBelongsToField(DeliveredProductFields.DELIVERY);
        Entity additionalCode = deliveredProduct.getBelongsToField(DeliveredProductFields.ADDITIONAL_CODE);

        SearchCriteriaBuilder findDeliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS).find();
        findDeliveredProducts.add(SearchRestrictions.belongsTo(DeliveredProductFields.PRODUCT,
                deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT)));
        if (additionalCode == null) {
            findDeliveredProducts.add(SearchRestrictions.isNull(DeliveredProductFields.ADDITIONAL_CODE));
        } else {
            findDeliveredProducts.add(SearchRestrictions.belongsTo(DeliveredProductFields.ADDITIONAL_CODE, additionalCode));
        }
        List<Entity> deliveredProducts = findDeliveredProducts.list().getEntities();

        List<Entity> allReservationsFromDeliveredProducts = deliveredProducts.stream().flatMap(p -> {
            return p.getHasManyField(DeliveredProductFields.RESERVATIONS).stream();
        }).collect(Collectors.toList());

        return allReservationsFromDeliveredProducts;
    }

    private List<Entity> filterReservationsByLocation(List<Entity> presentDeliveredProductForProductReservations,
            Entity location) {
        List<Entity> reservationsFromLocation = presentDeliveredProductForProductReservations.stream().filter(reservation -> {
            Entity reservationLocation = reservation.getBelongsToField(OrderedProductReservationFields.LOCATION);
            return location.getId().equals(reservationLocation.getId());
        }).collect(Collectors.toList());

        return reservationsFromLocation;
    }

    private BigDecimal sumQuantityFromReservations(List<Entity> reservationsFromLocation, String field) {
        BigDecimal quantity = reservationsFromLocation.stream().map(r -> {
            return r.getDecimalField(field);
        }).reduce(BigDecimal.ZERO, (r1, r2) -> r1.add(r2));

        return quantity;
    }

    public void deleteReservationsForOrderedProductIfChanged(Entity orderedProduct) {
        if (orderedProduct.getId() != null) {
            Entity orderedProductFromDB = orderedProduct.getDataDefinition().get(orderedProduct.getId());
            List<String> fields = Arrays.asList(OrderedProductFields.ORDERED_QUANTITY, OrderedProductFields.PRODUCT,
                    OrderedProductFields.ADDITIONAL_CODE);

            for (String field : fields) {
                if (notEquals(orderedProduct.getField(field), orderedProductFromDB.getField(field))) {
                    if (deletePreviousReservations(Arrays.asList(orderedProductFromDB))) {
                        orderedProduct.addGlobalMessage("deliveries.delivery.message.reservationsDeletedFromOrderedProduct");
                    }
                    break;
                }
            }
        }
    }

    public void deleteReservationsForDeliveredProductIfChanged(Entity deliveredProduct) {
        if (deliveredProduct.getId() != null) {
            Entity deliveredProductFromDB = deliveredProduct.getDataDefinition().get(deliveredProduct.getId());
            List<String> fields = Arrays.asList(DeliveredProductFields.DELIVERED_QUANTITY,
                    DeliveredProductFields.DAMAGED_QUANTITY, DeliveredProductFields.PRODUCT,
                    DeliveredProductFields.ADDITIONAL_CODE, DeliveredProductFields.IS_WASTE);

            for (String field : fields) {
                if (notEquals(deliveredProduct.getField(field), deliveredProductFromDB.getField(field))) {
                    if (deletePreviousReservations(Arrays.asList(deliveredProductFromDB))) {
                        deliveredProduct.addGlobalMessage("deliveries.delivery.message.reservationsDeletedFromDeliveredProduct");
                    }
                    break;
                }
            }
        }
    }

    private boolean notEquals(Object a, Object b) {
        if (a != null && b != null) {
            if (a instanceof BigDecimal && b instanceof BigDecimal) {
                return ((BigDecimal) a).compareTo((BigDecimal) b) != 0;
            }
            return !a.equals(b);
        } else {
            return a != b;
        }
    }

    public boolean validateDeliveryAgainstReservations(Entity delivery) {
        return validateDeliveryOrderedProductsAgainstReservations(delivery)
                && validateDeliveryDeliveredProductsAgainstReservations(delivery);
    }

    private boolean validateDeliveryOrderedProductsAgainstReservations(Entity delivery) {
        Entity deliveryLocation = delivery.getBelongsToField(DeliveryFields.LOCATION);
        EntityList orderedProducts = delivery.getHasManyField(DeliveryFields.ORDERED_PRODUCTS);
        if (orderedProducts != null && deliveryLocation != null) {
            for (Entity orderedProduct : orderedProducts) {
                EntityList reservations = orderedProduct.getHasManyField(OrderedProductFields.RESERVATIONS);
                if (reservations != null) {
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

    private boolean validateDeliveryDeliveredProductsAgainstReservations(Entity delivery) {
        Entity deliveryLocation = delivery.getBelongsToField(DeliveryFields.LOCATION);
        EntityList deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
        if (deliveredProducts != null && deliveryLocation != null) {
            for (Entity deliveredProduct : deliveredProducts) {
                EntityList reservations = deliveredProduct.getHasManyField(DeliveredProductFields.RESERVATIONS);
                if (reservations != null) {
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
}
