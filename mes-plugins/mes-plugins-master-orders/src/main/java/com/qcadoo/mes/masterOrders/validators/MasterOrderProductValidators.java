package com.qcadoo.mes.masterOrders.validators;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderFields.PRODUCT;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields.MASTER_ORDER;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields;
import com.qcadoo.mes.masterOrders.constants.MasterOrderType;
import com.qcadoo.mes.orders.constants.OrderFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MasterOrderProductValidators {

    public boolean checkIfEntityAlreadyExistsForProductAndMasterOrder(final DataDefinition masterOrderProductDD,
            final Entity masterOrderProduct) {
        SearchCriteriaBuilder searchCriteriaBuilder = masterOrderProductDD.find()
                .add(SearchRestrictions.belongsTo(MASTER_ORDER, masterOrderProduct.getBelongsToField(MASTER_ORDER)))
                .add(SearchRestrictions.belongsTo(PRODUCT, masterOrderProduct.getBelongsToField(PRODUCT)));

        Long masterOrderId = masterOrderProduct.getId();
        if (masterOrderId != null) {
            searchCriteriaBuilder.add(SearchRestrictions.ne("id", masterOrderId));
        }
        List<Entity> masterOrderProductList = searchCriteriaBuilder.list().getEntities();

        if (masterOrderProductList.isEmpty()) {
            return true;
        } else {
            masterOrderProduct.addError(masterOrderProductDD.getField(PRODUCT),
                    "masterOrders.masterOrderProduct.alreadyExistsForProductAndMasterOrder");

            return false;
        }
    }

    public boolean checkIfCanChangedTechnology(final DataDefinition masterProductOrderDD, final Entity masterProductOrder) {
        if (masterProductOrder.getId() == null) {
            return true;
        }
        Entity masterOrderProductFromDB = masterProductOrderDD.get(masterProductOrder.getId());
        Entity masterOrder = masterProductOrder.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        Entity technologyFromDB = masterOrderProductFromDB.getBelongsToField(MasterOrderFields.TECHNOLOGY);
        Entity productFromDB = masterProductOrder.getBelongsToField(MasterOrderFields.PRODUCT);
        Entity technology = masterProductOrder.getBelongsToField(MasterOrderFields.TECHNOLOGY);
        if (technology == null || (technologyFromDB != null && technology.getId().equals(technologyFromDB.getId()))) {
            return true;
        }
        if (!masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE).equals(
                MasterOrderType.MANY_PRODUCTS.getStringValue())) {
            return true;
        }

        List<Entity> orders = masterOrder.getHasManyField(MasterOrderFields.ORDERS).find()
                .add(SearchRestrictions.belongsTo(MasterOrderProductFields.PRODUCT, productFromDB)).list().getEntities();
        boolean isValid = true;
        StringBuilder orderNumberListWitkWrongNumer = new StringBuilder();
        for (Entity order : orders) {
            Entity technologyFromOrder = order.getBelongsToField(OrderFields.TECHNOLOGY);
            if ((technologyFromOrder == null) || !technologyFromOrder.getId().equals(technology.getId())) {
                isValid = false;
                orderNumberListWitkWrongNumer.append(order.getStringField(OrderFields.NUMBER));
                orderNumberListWitkWrongNumer.append(", ");
            }
        }
        if (!isValid) {
            masterProductOrder.addError(masterProductOrderDD.getField(MasterOrderFields.TECHNOLOGY),
                    "masterOrders.masterOrder.technology.wrongTechnology", orderNumberListWitkWrongNumer.toString());
        }
        return isValid;
    }

    public boolean checkIfCanChangedProduct(final DataDefinition masterOrderProductDD, final Entity masterProductOrder) {
        if (masterProductOrder.getId() == null) {
            return true;
        }
        Entity masterOrderProductFromDB = masterOrderProductDD.get(masterProductOrder.getId());
        Entity productFromDB = masterOrderProductFromDB.getBelongsToField(MasterOrderFields.PRODUCT);
        Entity product = masterProductOrder.getBelongsToField(MasterOrderFields.PRODUCT);
        Entity masterOrder = masterProductOrder.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        if (product == null || (productFromDB != null && product.getId().equals(productFromDB.getId()))) {
            return true;
        }
        if (!masterOrder.getStringField(MasterOrderFields.MASTER_ORDER_TYPE).equals(
                MasterOrderType.MANY_PRODUCTS.getStringValue())) {
            return true;
        }

        List<Entity> orders = masterOrder.getHasManyField(MasterOrderFields.ORDERS).find()
                .add(SearchRestrictions.belongsTo(MasterOrderProductFields.PRODUCT, productFromDB)).list().getEntities();
        boolean isValid = true;
        StringBuilder orderNumberListWitkWrongNumer = new StringBuilder();
        for (Entity order : orders) {
            isValid = false;
            orderNumberListWitkWrongNumer.append(order.getStringField(OrderFields.NUMBER));
            orderNumberListWitkWrongNumer.append(", ");
        }
        if (!isValid) {
            masterProductOrder.addError(masterOrderProductDD.getField(MasterOrderFields.PRODUCT),
                    "masterOrders.masterOrder.product.wrongProduct", orderNumberListWitkWrongNumer.toString());
        }
        return isValid;
    }

    public boolean checkIfExistsOrderWithSelectedProduct(final DataDefinition masterOrderProductDD,
            final Entity masterProductOrder) {
        Entity masterOrder = masterProductOrder.getBelongsToField(MasterOrderProductFields.MASTER_ORDER);
        Entity product = masterProductOrder.getBelongsToField(PRODUCT);
        List<Entity> orders = masterOrder.getHasManyField(MasterOrderFields.ORDERS).find()
                .add(SearchRestrictions.not(SearchRestrictions.belongsTo(PRODUCT, product))).list().getEntities();
        List<Entity> ordersWithProduct = masterOrder.getHasManyField(MasterOrderFields.ORDERS).find()
                .add(SearchRestrictions.belongsTo(MasterOrderProductFields.PRODUCT, product)).list().getEntities();
        boolean isValid = true;
        if (orders.isEmpty()) {
            return isValid;
        }
        if (!orders.isEmpty() && ordersWithProduct.isEmpty()) {
            StringBuilder orderNumberListWitkWrongNumer = new StringBuilder();
            for (Entity order : orders) {
                isValid = false;
                orderNumberListWitkWrongNumer.append(order.getStringField(OrderFields.NUMBER));
                orderNumberListWitkWrongNumer.append(" [");
                orderNumberListWitkWrongNumer.append(order.getBelongsToField(OrderFields.PRODUCT).getStringField(
                        ProductFields.NUMBER));
                orderNumberListWitkWrongNumer.append("], ");
            }
            if (!isValid) {
                masterProductOrder.addError(masterOrderProductDD.getField(PRODUCT),
                        "masterOrders.masterOrderProduct.ordersForSelectedProductDoesNotExists",
                        orderNumberListWitkWrongNumer.toString());
            }
        }
        return isValid;
    }
}
