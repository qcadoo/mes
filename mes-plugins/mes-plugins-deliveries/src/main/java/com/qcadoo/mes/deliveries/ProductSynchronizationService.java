package com.qcadoo.mes.deliveries;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.OrderedProductFields;
import com.qcadoo.mes.deliveries.constants.ProductFieldsD;
import com.qcadoo.mes.materialFlow.constants.LocationFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;

@Service
public class ProductSynchronizationService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean shouldSynchronize(final StateChangeContext stateChangeContext) {
        Entity owner = stateChangeContext.getOwner();
        Entity location = owner.getBelongsToField(DeliveryFields.LOCATION);
        if (null != location) {
            location = location.getDataDefinition().get(location.getId()); // fetch
            return isNotBlank(location.getStringField(LocationFields.EXTERNAL_NUMBER));
        }
        return false;
    }

    public void synchronizeProducts(final StateChangeContext stateChangeContext, boolean synchronizeOrderedProducts) {
        Entity owner = stateChangeContext.getOwner();

        Set<String> productNamesToSynchronize = new HashSet<>();

        if (synchronizeOrderedProducts) {
            productNamesToSynchronize.addAll(makeAssociatedProductEntitiesSynchronized(owner, DeliveryFields.ORDERED_PRODUCTS,
                    OrderedProductFields.PRODUCT));
        }

        productNamesToSynchronize.addAll(makeAssociatedProductEntitiesSynchronized(owner, DeliveryFields.DELIVERED_PRODUCTS,
                DeliveredProductFields.PRODUCT));

        if (!productNamesToSynchronize.isEmpty()) {
            stateChangeContext.addMessage("deliveries.deliveredProducts.willSynchronize", StateMessageType.INFO, false,
                    String.join(", ", productNamesToSynchronize));
        }

    }

    private Set<String> makeAssociatedProductEntitiesSynchronized(Entity owner, final String productsHolderKey,
            final String productKey) {
        Set<String> result = new HashSet<>();
        for (Entity productContainingEntity : owner.getHasManyField(productsHolderKey)) {
            Entity product = productContainingEntity.getBelongsToField(productKey);
            if (!product.getBooleanField(ProductFieldsD.SYNCHRONIZE)
                    && isBlank(product.getStringField(ProductFields.EXTERNAL_NUMBER))) {
                result.add(product.getStringField(ProductFields.NUMBER));
                product.setField(ProductFieldsD.SYNCHRONIZE, Boolean.TRUE);
                getProductDataDefinition().save(product);
            }
        }
        return result;
    }

    private DataDefinition getProductDataDefinition() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT);
    }

}
