package com.qcadoo.mes.productFlowThruDivision.deliveries.states;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductReservationFields;
import com.qcadoo.mes.deliveries.constants.DeliveriesConstants;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.materialFlow.constants.MaterialFlowConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.StorageLocationFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.deliveries.helpers.CreationWarehouseIssueState;
import com.qcadoo.mes.productFlowThruDivision.deliveries.helpers.DeliveredProductReservationKeyObject;
import com.qcadoo.mes.productFlowThruDivision.deliveries.helpers.DeliveredProductReservationObject;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueGenerator;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.CollectionProducts;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants.WarehouseIssueState;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.validators.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DeliveryStatePFTDService {

    private static final String L_DELIVERY_ID = "deliveryId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    @Autowired
    private WarehouseIssueGenerator warehouseIssueGenerator;

    public void tryCreateIssuesForDeliveriesReservations(final StateChangeContext context) {
        if (warehouseIssueParameterService.generateWarehouseIssuesToDeliveries()) {
            Entity delivery = context.getOwner();

            List<Entity> reservationsForDelivery = findReservationsForDelivery(delivery);

            if (reservationsForDelivery.isEmpty()) {
                return;
            }

            Multimap<Long, Entity> reservationsMapGroupedByWarehouse = ArrayListMultimap.create();

            for (Entity res : reservationsForDelivery) {
                reservationsMapGroupedByWarehouse.put(res.getBelongsToField(DeliveredProductReservationFields.LOCATION).getId(),
                        res);
            }

            CreationWarehouseIssueState creationWarehouseIssueState = new CreationWarehouseIssueState();

            createWarehouseIssues(creationWarehouseIssueState, delivery, delivery.getBelongsToField(DeliveryFields.LOCATION),
                    reservationsMapGroupedByWarehouse);

            context.addMessage("productFlowThruDivision.deliveries.warehouseIssue.issuesCreated.success",
                    StateMessageType.SUCCESS);
        }
    }

    private void createWarehouseIssues(final CreationWarehouseIssueState creationWarehouseIssueState, final Entity delivery,
            final Entity placeOfIssue, final Multimap reservationsMapGroupedByWarehouse) {
        Map<Long, List<Entity>> simpleReservationsMap = reservationsMapGroupedByWarehouse.asMap();

        simpleReservationsMap.forEach((k, v) -> createWarehouseIssue(k, v, creationWarehouseIssueState, delivery, placeOfIssue));
    }

    private void createWarehouseIssue(final Long warehouse, final List<Entity> reservations,
            final CreationWarehouseIssueState creationWarehouseIssueState, final Entity delivery, final Entity placeOfIssue) {
        Multimap<DeliveredProductReservationKeyObject, DeliveredProductReservationObject> flatReservations = flatReservations(
                reservations);
        Map<DeliveredProductReservationKeyObject, Collection<DeliveredProductReservationObject>> simpleFlatReservations = flatReservations
                .asMap();

        Entity warehouseIssue = createNewWarehouseIssue(placeOfIssue, delivery);

        if (!warehouseIssue.isValid()) {
            for (Map.Entry<String, ErrorMessage> entry : warehouseIssue.getErrors().entrySet()) {
                creationWarehouseIssueState.getErrors().add(entry.getValue());
            }

            throw new IllegalStateException(
                    "Undone creation warehouse issue for delivery : " + delivery.getStringField(DeliveryFields.NUMBER));
        }

        Entity location = getLocationDD().get(warehouse);

        List<Entity> createdProductsToIssue = Lists.newArrayList();

        for (Map.Entry<DeliveredProductReservationKeyObject, Collection<DeliveredProductReservationObject>> entry : simpleFlatReservations
                .entrySet()) {
            DeliveredProductReservationKeyObject key = entry.getKey();
            Collection<DeliveredProductReservationObject> value = entry.getValue();
            Entity product = key.getProduct();

            Entity productToIssue = getProductsToIssueDD().create();

            productToIssue.setField(ProductsToIssueFields.PRODUCT, product);
            productToIssue.setField(ProductsToIssueFields.CONVERSION, key.getConversion());
            productToIssue.setField(ProductsToIssueFields.LOCATION, location);

            if (productAlreadyCreated(createdProductsToIssue, product.getId(), location.getId(),
                    key.getConversion())) {
                warehouseIssue = createNewWarehouseIssue(placeOfIssue, delivery);

                if (!warehouseIssue.isValid()) {
                    for (Map.Entry<String, ErrorMessage> e : warehouseIssue.getErrors().entrySet()) {
                        creationWarehouseIssueState.getErrors().add(e.getValue());
                    }

                    throw new IllegalStateException(
                            "Undone creation warehouse issue for delivery : " + delivery.getStringField(DeliveryFields.NUMBER));
                }

                createdProductsToIssue = Lists.newArrayList();
            }

            productToIssue.setField(ProductsToIssueFields.WAREHOUSE_ISSUE, warehouseIssue);

            Optional<Entity> storageLocation = findStorageLocationForProduct(product, location);

            if (storageLocation.isPresent()) {
                productToIssue.setField(ProductsToIssueFields.STORAGE_LOCATION, storageLocation.get());
            }

            BigDecimal dQuantity = value.stream().filter(Objects::nonNull)
                    .map(val -> val.getReservationForDeliveredProduct()
                            .getDecimalField(DeliveredProductReservationFields.DELIVERED_QUANTITY))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal aQuantity = value.stream().filter(Objects::nonNull)
                    .map(val -> val.getReservationForDeliveredProduct()
                            .getDecimalField(DeliveredProductReservationFields.ADDITIONAL_QUANTITY))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            productToIssue.setField(ProductsToIssueFields.DEMAND_QUANTITY, dQuantity);
            productToIssue.setField(ProductsToIssueFields.ADDITIONAL_DEMAND_QUANTITY, aQuantity);
            productToIssue = productToIssue.getDataDefinition().save(productToIssue);

            createdProductsToIssue.add(productToIssue);

            if (!productToIssue.isValid()) {
                for (Map.Entry<String, ErrorMessage> e : productToIssue.getErrors().entrySet()) {
                    creationWarehouseIssueState.getErrors().add(e.getValue());
                }

                throw new IllegalStateException(
                        "Undone creation warehouse issue for delivery : " + delivery.getStringField(DeliveryFields.NUMBER));
            }
        }
    }

    private boolean productAlreadyCreated(final List<Entity> createdProductsToIssue, final Long productId, final Long locationId, final BigDecimal conversion) {
        return createdProductsToIssue.stream().anyMatch(createdProduct -> Objects
                .equals(createdProduct.getBelongsToField(ProductsToIssueFields.PRODUCT).getId(), productId)
                && Objects.equals(createdProduct.getBelongsToField(ProductsToIssueFields.LOCATION).getId(), locationId)
                && conversion.compareTo(createdProduct.getDecimalField(ProductsToIssueFields.CONVERSION)) != 0);
    }

    private Entity createNewWarehouseIssue(final Entity placeOfIssue, final Entity delivery) {
        Entity warehouseIssue = getWarehouseIssueDD().create();

        warehouseIssue.setField(WarehouseIssueFields.COLLECTION_PRODUCTS, CollectionProducts.ON_ORDER.getStringValue());
        warehouseIssue.setField(WarehouseIssueFields.NUMBER, warehouseIssueGenerator.setNumberFromSequence());
        warehouseIssue.setField(WarehouseIssueFields.PLACE_OF_ISSUE, placeOfIssue);
        warehouseIssue.setField(WarehouseIssueFields.STATE, WarehouseIssueState.DRAFT.getStringValue());
        warehouseIssue.setField(WarehouseIssueFields.PRODUCTS_TO_ISSUE_MODE,
                warehouseIssueParameterService.getProductsToIssue().getStrValue());
        warehouseIssue.setField(WarehouseIssueFields.DESCRIPTION, buildDescription(delivery));
        warehouseIssue = warehouseIssue.getDataDefinition().save(warehouseIssue);

        return warehouseIssue;
    }

    private String buildDescription(final Entity delivery) {
        return translationService.translate("productFlowThruDivision.deliveries.warehouseIssue.for",
                LocaleContextHolder.getLocale(), delivery.getStringField(DeliveryFields.NUMBER));
    }

    private Multimap<DeliveredProductReservationKeyObject, DeliveredProductReservationObject> flatReservations(
            final List<Entity> reservations) {
        Multimap<DeliveredProductReservationKeyObject, DeliveredProductReservationObject> reservationsMapGroupedByWarehouse = ArrayListMultimap
                .create();

        reservations.forEach(res -> {
            Entity deliveredProduct = res.getBelongsToField(DeliveredProductReservationFields.DELIVERED_PRODUCT);
            Entity product = deliveredProduct.getBelongsToField(DeliveredProductFields.PRODUCT);
            BigDecimal conversion = deliveredProduct.getDecimalField(DeliveredProductFields.CONVERSION);

            DeliveredProductReservationKeyObject key = new DeliveredProductReservationKeyObject(product,
                    conversion);
            DeliveredProductReservationObject value = new DeliveredProductReservationObject(deliveredProduct, res);

            reservationsMapGroupedByWarehouse.put(key, value);
        });

        return reservationsMapGroupedByWarehouse;
    }

    private List<Entity> findReservationsForDelivery(final Entity delivery) {
        String query = buildQueryForDeliveredProductReservations();

        return getDeliveredProductReservation().find(query).setParameter(L_DELIVERY_ID, delivery.getId()).list().getEntities();
    }

    private String buildQueryForDeliveredProductReservations() {
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT productReservation FROM #deliveries_deliveredProductReservation productReservation ");
        builder.append("LEFT JOIN productReservation.deliveredProduct as deliveredProduct ");
        builder.append("LEFT JOIN deliveredProduct.delivery as delivery ");
        builder.append("WHERE delivery.id = :");
        builder.append(L_DELIVERY_ID);

        return builder.toString();
    }

    public Optional<Entity> findStorageLocationForProduct(final Entity product, final Entity location) {
        SearchCriteriaBuilder scb = getStorageLocationDD().find();

        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.PRODUCT, product));
        scb.add(SearchRestrictions.belongsTo(StorageLocationFields.LOCATION, location));

        return Optional.ofNullable(scb.setMaxResults(1).uniqueResult());
    }

    public void validate(StateChangeContext stateChangeContext) {
        if (warehouseIssueParameterService.generateWarehouseIssuesToDeliveries()) {
            Entity delivery = stateChangeContext.getOwner();

            if (Objects.isNull(delivery.getBelongsToField(DeliveryFields.LOCATION))) {
                stateChangeContext.addFieldValidationError(DeliveryFields.LOCATION, "qcadooView.validate.field.error.missing");
            }
        }
    }

    private DataDefinition getLocationDD() {
        return dataDefinitionService.get(MaterialFlowConstants.PLUGIN_IDENTIFIER, MaterialFlowConstants.MODEL_LOCATION);
    }

    private DataDefinition getStorageLocationDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_STORAGE_LOCATION);
    }

    private DataDefinition getDeliveredProductReservation() {
        return dataDefinitionService.get(DeliveriesConstants.PLUGIN_IDENTIFIER,
                DeliveriesConstants.MODEL_DELIVERED_PRODUCT_RESERVATION);
    }

    private DataDefinition getWarehouseIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_WAREHOUSE_ISSUE);
    }

    private DataDefinition getProductsToIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE);
    }

}
