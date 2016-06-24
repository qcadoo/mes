package com.qcadoo.mes.deliveries;

import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductReservationFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductReservationFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchCriterion;
import com.qcadoo.model.api.search.SearchRestrictions;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public Entity createDefaultReservationsForDeliveredProduct(Entity deliveredProduct) {
        Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
        if (product != null) {
            List<Entity> deliveredProductReservations = new ArrayList<>();

            Entity orderedProductForProduct = findOrderedProductForProduct(deliveredProduct);
            List<Entity> presentDeliveredProductForProductReservations = findPresentDeliveredProductForProductReservations(deliveredProduct);

            if (orderedProductForProduct != null) {
                EntityList reservationsFromOrderedProduct = orderedProductForProduct.getHasManyField(OrderedProductFields.RESERVATIONS);

                BigDecimal availableQuantity = deliveredProduct.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY);
                availableQuantity = availableQuantity == null ? BigDecimal.ZERO : availableQuantity;
                for (Entity reservationFromOrderedProduct : reservationsFromOrderedProduct) {
                    Optional<Entity> maybeDeliveredProductReservation = createDeliveredProductReservation(availableQuantity, deliveredProduct, reservationFromOrderedProduct, presentDeliveredProductForProductReservations);
                    if (maybeDeliveredProductReservation.isPresent()) {
                        Entity deliveredProductReservation = maybeDeliveredProductReservation.get();
                        deliveredProductReservations.add(deliveredProductReservation);
                        presentDeliveredProductForProductReservations.add(deliveredProductReservation);
                        availableQuantity = availableQuantity.subtract(deliveredProductReservation.getDecimalField(DeliveredProductReservationFields.DELIVERED_QUANTITY));
                    }
                }
            }
            deliveredProduct.setField(DeliveredProductFields.RESERVATIONS, deliveredProductReservations);
        }

        return deliveredProduct;
    }

    private Optional<Entity> createDeliveredProductReservation(BigDecimal availableQuantity, final Entity deliveredProduct, final Entity reservationFromOrderedProduct, List<Entity> presentDeliveredProductForProductReservations) {
        Entity location = reservationFromOrderedProduct.getBelongsToField(OrderedProductReservationFields.LOCATION);
        List<Entity> reservationsFromLocation = filterReservationsByLocation(presentDeliveredProductForProductReservations, location);

        BigDecimal orderedQuantity = reservationFromOrderedProduct.getDecimalField(OrderedProductFields.ORDERED_QUANTITY);
        BigDecimal deliveredQuantity = sumQuantityFromReservations(reservationsFromLocation, DeliveredProductReservationFields.DELIVERED_QUANTITY);
        BigDecimal requestQuantity = orderedQuantity.subtract(deliveredQuantity);
        BigDecimal currentDeliveredQuantity = requestQuantity.compareTo(BigDecimal.ZERO) <= 0 ? BigDecimal.ZERO : requestQuantity.min(availableQuantity);

        BigDecimal conversion = deliveredProduct.getDecimalField(DeliveredProductFields.CONVERSION);
        if (conversion == null) {
            return Optional.empty();
        }
        BigDecimal currentDeliveredAdditionalQuantity = currentDeliveredQuantity.multiply(conversion);

        if (currentDeliveredQuantity.compareTo(BigDecimal.ZERO) <= 0 || currentDeliveredAdditionalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        DataDefinition deliveredProductReservationDD = getDeliveredProductReservationDD();
        Entity deliveredProductReservation = deliveredProductReservationDD.create();
        deliveredProductReservation.setField(DeliveredProductReservationFields.ADDITIONAL_QUANTITY, currentDeliveredAdditionalQuantity);
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
            deliveredProduct = createDefaultReservationsForDeliveredProduct(deliveredProduct);
            deliveredProductDD.save(deliveredProduct);
        }
    }

    private DataDefinition getDeliveredProductDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT);
    }

    private DataDefinition getDeliveredProductReservationDD() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER, DeliveriesConstants.MODEL_DELIVERED_PRODUCT_RESERVATION);
    }

    private void deletePreviousReservations(List<Entity> deliveredProducts) {
        for (Entity deliveredProduct : deliveredProducts) {
            deliveredProduct.getHasManyField(DeliveredProductFields.RESERVATIONS).stream().forEach(reservation -> {
                reservation.getDataDefinition().delete(reservation.getId());
            });
        }
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
        EntityList deliveredProducts = delivery.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);

        List<Entity> allReservationsFromDeliveredProducts = deliveredProducts.stream().flatMap(p -> {
            return p.getHasManyField(DeliveredProductFields.RESERVATIONS).stream();
        }).collect(Collectors.toList());

        return allReservationsFromDeliveredProducts;
    }

    private List<Entity> filterReservationsByLocation(List<Entity> presentDeliveredProductForProductReservations, Entity location) {
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
}
