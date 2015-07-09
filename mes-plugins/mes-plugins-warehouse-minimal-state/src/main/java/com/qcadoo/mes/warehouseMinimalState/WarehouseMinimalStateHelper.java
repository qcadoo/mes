package com.qcadoo.mes.warehouseMinimalState;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.DeliveriesService;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

@Service
public class WarehouseMinimalStateHelper {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private DeliveriesService deliveriesService;

    public boolean checkIfLowerThanMinimum(long productId, BigDecimal quantity, BigDecimal minimumState) {
        return quantity.compareTo(minimumState) == -1 || quantity.compareTo(minimumState) == 0;
    }

    public BigDecimal getOrderedQuantityForProductAndLocation(final Long warehouse, final Long product) {
        String query = "select COALESCE(sum(op.orderedQuantity),0) as  orderedQuantity from #deliveries_orderedProduct op, "
                + "#deliveries_delivery del where op.delivery.id=del.id and op.product.id=:product and del.location.id = :warehouseId "
                + "and del.state in ('01draft', '02prepared', '03duringCorrection', '05approved') and del.active=true";
        return getWarehouseStockDD().find(query).setParameter("warehouseId", warehouse).setParameter("product", product)
                .setMaxResults(1).uniqueResult().getDecimalField("orderedQuantity");
    }

    public List<Entity> getWarehouseStockWithTooSmallMinState(final Entity warehouse) {

        String query = "select stock from #materialFlowResources_warehouseStock as stock where stock.minimumState > 0"
                + " and stock.location.id = :warehouseId";
        return getWarehouseStockDD().find(query).setParameter("warehouseId", warehouse.getId()).list().getEntities();
    }

    private DataDefinition getWarehouseStockDD() {
        return dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, "warehouseStock");
    }

    public Entity getDefaultSupplier(Long productId) {
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(productId);

        if (product != null) {
            if (ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue().equals(
                    product.getStringField(ProductFields.ENTITY_TYPE))) {
                Entity defaultSupplier = getDefaultSupplierForProductsFamily(productId);
                if (defaultSupplier != null) {
                    return defaultSupplier;
                } else {
                    return getDefaultSupplierForParticularProduct(productId);
                }
            }
        }
        return null;
    }

    private Entity getDefaultSupplierForProductsFamily(Long productId) {
        String query = "select company from #deliveries_companyProductsFamily company, #basic_product product where product.parent.id = company.product.id and product.id = :id"
                + " and company.isDefault = true";
        return deliveriesService.getCompanyProductDD().find(query).setParameter("id", productId).setMaxResults(1).uniqueResult();
    }

    private Entity getDefaultSupplierForParticularProduct(Long productId) {
        String query = "select company from #deliveries_companyProduct company where company.product.id = :id"
                + " and company.isDefault = true";
        return deliveriesService.getCompanyProductDD().find(query).setParameter("id", productId).setMaxResults(1).uniqueResult();
    }
}
