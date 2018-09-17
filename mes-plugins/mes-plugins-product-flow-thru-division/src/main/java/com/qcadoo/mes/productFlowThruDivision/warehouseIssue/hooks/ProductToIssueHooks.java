package com.qcadoo.mes.productFlowThruDivision.warehouseIssue.hooks;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.productFlowThruDivision.reservation.ReservationsServiceForProductsToIssue;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.WarehouseIssueParameterService;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.validators.ProductToIssueValidators;
import com.qcadoo.model.api.BigDecimalUtils;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ProductToIssueHooks {

    @Autowired
    private WarehouseIssueParameterService warehouseIssueParameterService;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    @Autowired
    private ReservationsServiceForProductsToIssue reservationsServiceForProductsToIssue;

    @Autowired
    private ProductToIssueValidators productToIssueValidators;

    public void onSave(final DataDefinition productToIssueDD, final Entity productToIssue) {
        if (!warehouseIssueParameterService.issueForOrder()) {
            Entity wi = productToIssue.getBelongsToField(ProductsToIssueFields.WAREHOUSE_ISSUE);

            Entity product = productToIssue.getBelongsToField(ProductsToIssueFields.PRODUCT);
            Map<Long, BigDecimal> quantitiesForProductsLocationsQuantity = materialFlowResourcesService
                    .getQuantitiesForProductsAndLocation(Lists.newArrayList(product),
                            wi.getBelongsToField(WarehouseIssueFields.PLACE_OF_ISSUE));
            Map<Long, BigDecimal> quantitiesForProducts = materialFlowResourcesService.getQuantitiesForProductsAndLocation(
                    Lists.newArrayList(product), productToIssue.getBelongsToField(ProductsToIssueFields.LOCATION));
            productToIssue.setField("locationsQuantity", quantitiesForProductsLocationsQuantity.get(product.getId()));
            productToIssue.setField("placeOfIssueQuantity", quantitiesForProducts.get(product.getId()));
        }
        productToIssue.setField(ProductsToIssueFields.ISSUE_QUANTITY,
                BigDecimalUtils.convertNullToZero(productToIssue.getDecimalField(ProductsToIssueFields.ISSUE_QUANTITY)));

        reservationsServiceForProductsToIssue.updateReservationFromProductToIssue(productToIssue);
    }

    public void onDelete(final DataDefinition productToIssueDD, final Entity productToIssue) {
        reservationsServiceForProductsToIssue.deleteReservationFromProductToIssue(productToIssue);
    }

}
