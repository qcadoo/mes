package com.qcadoo.mes.masterOrders.validators;

import static com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields.MASTER_ORDER;
import static com.qcadoo.mes.masterOrders.constants.MasterOrderProductFields.PRODUCT;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class MasterOrderProductValidators {

    public boolean checkIfEntityAlreadyExistsForProductAndMasterOrder(final DataDefinition masterOrderProductDD,
            final Entity masterOrderProduct) {
        List<Entity> masterOrderProductList = masterOrderProductDD.find()
                .add(SearchRestrictions.belongsTo(MASTER_ORDER, masterOrderProduct.getBelongsToField(MASTER_ORDER)))
                .add(SearchRestrictions.belongsTo(PRODUCT, masterOrderProduct.getBelongsToField(PRODUCT))).list().getEntities();
        if (masterOrderProductList.isEmpty()) {
            return true;
        } else {
            masterOrderProduct.addError(masterOrderProductDD.getField(PRODUCT),
                    "masterOrders.masterOrderProduct.alreadyExistsForProductAndMasterOrder");
            return false;
        }
    }

}
