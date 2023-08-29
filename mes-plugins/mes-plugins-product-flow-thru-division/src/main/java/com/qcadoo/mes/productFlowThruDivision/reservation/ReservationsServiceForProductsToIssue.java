package com.qcadoo.mes.productFlowThruDivision.reservation;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.mes.materialFlowResources.constants.ReservationFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.IssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ReservationsServiceForProductsToIssue {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

    public boolean reservationsEnabledForProductsToIssue() {
        Entity parameter = parameterService.getParameter();
        return parameter.getBooleanField("warehouseIssuesReserveStates");
    }

    private Entity getReservationForProductToIssue(final Entity productToIssue) {
        if (productToIssue == null || productToIssue.getId() == null) {
            return null;
        }
        SearchCriteriaBuilder find = getReservationDD().find();
        find.add(SearchRestrictions.belongsTo(ReservationFields.PRODUCTS_TO_ISSUE, productToIssue)).setMaxResults(1);
        return find.uniqueResult();
    }

    private Entity getReservationForIssue(final Entity issue) {
        Entity productsToIssue = getProductToIssueForIssue(issue);

        return getReservationForProductToIssue(productsToIssue);
    }

    public void updateReservationFromProductToIssue(Entity productToIssue) {
        if (!reservationsEnabledForProductsToIssue()) {
            return;
        }

        Entity existingReservation = getReservationForProductToIssue(productToIssue);
        if (existingReservation != null) {
            Entity location = productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE).getBelongsToField(
                    WarehouseIssueFields.PLACE_OF_ISSUE);
            BigDecimal quantity = calculateQuantityForReservation(productToIssue);
            if (quantity.compareTo(BigDecimal.ZERO) == 0) {
                existingReservation.getDataDefinition().delete(existingReservation.getId());
            } else {
                existingReservation.setField(ReservationFields.QUANTITY, quantity);
                existingReservation.setField(ReservationFields.PRODUCT,
                        productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT));
                existingReservation.setField(ReservationFields.LOCATION, location);
                existingReservation.getDataDefinition().save(existingReservation);
            }

        } else {
            BigDecimal neededQuantity = productToIssue.getDecimalField(ProductsToIssueFields.DEMAND_QUANTITY);
            BigDecimal issuedQuantity = Optional.ofNullable(productToIssue.getDecimalField(ProductsToIssueFields.ISSUE_QUANTITY))
                    .orElse(BigDecimal.ZERO);
            if (neededQuantity.compareTo(issuedQuantity) >= 0) {
                createReservationFromProductToIssue(productToIssue, neededQuantity.subtract(issuedQuantity));
            }
        }
    }

    private Entity createReservationFromProductToIssue(Entity productToIssue, BigDecimal quantity) {
        Entity reservation = getReservationDD().create();

        Entity location = productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE)
                .getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE);
        reservation.setField(ReservationFields.LOCATION, location);
        reservation.setField(ReservationFields.PRODUCTS_TO_ISSUE, productToIssue);
        reservation.setField(ReservationFields.PRODUCT, productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT));
        reservation.setField(ReservationFields.QUANTITY, quantity);

        productToIssue.setField(ProductsToIssueFields.RESERVATIONS, Lists.newArrayList(reservation));

        return reservation;
    }

    private DataDefinition getReservationDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_RESERVATION);
    }

    private DataDefinition getProductsToIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE);
    }

    private DataDefinition getIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_ISSUE);
    }

    public void deleteReservationFromProductToIssue(Entity productToIssue) {
        if (!reservationsEnabledForProductsToIssue()) {
            return;
        }

        Entity reservation = getReservationForProductToIssue(productToIssue);
        if (reservation != null) {
            BigDecimal qauntity = productToIssue.getDecimalField(ProductsToIssueFields.DEMAND_QUANTITY);
            reduceReservationFor(reservation, qauntity);
        }
    }

    public void onIssue(Entity issue) {
        if (!reservationsEnabledForProductsToIssue()) {
            return;
        }

        Entity reservation = getReservationForIssue(issue);
        if (reservation != null) {
            BigDecimal issueQuantity = issue.getDecimalField(IssueFields.ISSUE_QUANTITY);
            reduceReservationFor(reservation, issueQuantity);
        }
    }

    public void onIssueCompensation(Entity issue) {
        if (!reservationsEnabledForProductsToIssue()) {
            return;
        }

        updateReservationFromProductToIssue(getProductToIssueForIssue(issue));
    }

    private void reduceReservationFor(Entity reservation, BigDecimal forQuantity) {
        BigDecimal newReservationQuantity = reservation.getDecimalField(ReservationFields.QUANTITY).subtract(forQuantity);
        if (newReservationQuantity.compareTo(BigDecimal.ZERO) > 0) {
            reservation.setField(ReservationFields.QUANTITY, newReservationQuantity);
            reservation.getDataDefinition().save(reservation);
        } else {
            reservation.setField(ReservationFields.QUANTITY, BigDecimal.ZERO);
            reservation.getDataDefinition().save(reservation);
        }
    }

    private BigDecimal calculateQuantityForReservation(Entity productToIssue) {
        BigDecimal demandQuantity = productToIssue.getDecimalField(ProductsToIssueFields.DEMAND_QUANTITY);
        BigDecimal correctionQuantity = BigDecimalUtils.convertNullToZero(productToIssue
                .getDecimalField(ProductsToIssueFields.CORRECTION));
        BigDecimal newQuantityForReservation = demandQuantity.subtract(calculateIssuedQuantityForProduct(productToIssue))
                .subtract(correctionQuantity);

        if (newQuantityForReservation.compareTo(BigDecimal.ZERO) < 0) {
            newQuantityForReservation = BigDecimal.ZERO;
        }

        return newQuantityForReservation;
    }

    private BigDecimal calculateIssuedQuantityForProduct(Entity productToIssue) {
        Entity warehouseIssue = productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE);
        Entity product = productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT);
        Entity location = productToIssue.getBelongsToField(ProductsToIssueFields.LOCATION);
        BigDecimal conversion = productToIssue.getDecimalField(ProductsToIssueFields.CONVERSION);

        SearchCriteriaBuilder findIssue = getIssueDD().find();
        findIssue.add(SearchRestrictions.belongsTo(IssueFields.WAREHOUSE_ISSUE, warehouseIssue));
        findIssue.add(SearchRestrictions.belongsTo(IssueFields.PRODUCT, product));
        findIssue.add(SearchRestrictions.belongsTo(IssueFields.LOCATION, location));
        findIssue.add(SearchRestrictions.eq(IssueFields.CONVERSION, conversion));
        findIssue.add(SearchRestrictions.eq(IssueFields.ISSUED, Boolean.TRUE));

        return findIssue.list().getEntities().stream().map(issue -> issue.getDecimalField(IssueFields.ISSUE_QUANTITY))
                .reduce(BigDecimal.ZERO, (a, b) -> (a.add(b)));
    }

    private Entity getProductToIssueForIssue(Entity issue) {
        if (issue.getId() == null) {
            return null;
        }
        SearchCriteriaBuilder find = getProductsToIssueDD().find();
        find.add(SearchRestrictions.belongsTo(ProductsToIssueFields.WAREHOUSE_ISSUE,
                issue.getBelongsToField(IssueFields.WAREHOUSE_ISSUE)));
        find.add(SearchRestrictions.belongsTo(ProductsToIssueFields.PRODUCT, issue.getBelongsToField(IssueFields.PRODUCT)));
        find.add(SearchRestrictions.belongsTo(ProductsToIssueFields.LOCATION, issue.getBelongsToField(IssueFields.LOCATION)));
        find.add(SearchRestrictions.eq(ProductsToIssueFields.CONVERSION, issue.getField(IssueFields.CONVERSION)));
        find.setMaxResults(1);

        Entity productsToIssue = find.uniqueResult();

        return productsToIssue;
    }
}
