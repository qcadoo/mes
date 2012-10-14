package com.qcadoo.mes.deliveries.states;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.deliveries.constants.DeliveryFields.DELIVERY_DATE;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveredProductFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.states.StateChangeContext;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;

@Service
public class DeliveryStateValidationService {

    private static final String ENTITY_IS_NULL = "entity is null";

    public void validationOnApproved(final StateChangeContext stateChangeContext) {
        final List<String> references = Arrays.asList(DELIVERY_DATE);
        checkRequired(references, stateChangeContext);
    }

    public void validationOnReceived(final StateChangeContext stateChangeContext) {
        checkDeliveredQuantity(stateChangeContext);
    }

    public void checkRequired(final List<String> fieldNames, final StateChangeContext stateChangeContext) {
        checkArgument(stateChangeContext != null, ENTITY_IS_NULL);
        final Entity stateChangeEntity = stateChangeContext.getOwner();
        for (String fieldName : fieldNames) {
            if (stateChangeEntity.getField(fieldName) == null) {
                stateChangeContext.addFieldValidationError(fieldName, "deliveries.delivery.deliveryStates.fieldRequired");
            }
        }
    }

    public void checkDeliveredQuantity(final StateChangeContext stateChangeContext) {
        checkArgument(stateChangeContext != null, ENTITY_IS_NULL);
        final Entity stateChangeEntity = stateChangeContext.getOwner();
        List<Entity> deliveredProducts = stateChangeEntity.getHasManyField(DeliveryFields.DELIVERED_PRODUCTS);
        boolean deliveredProductHasNull = false;
        StringBuffer listOfProductNumber = new StringBuffer();
        for (Entity delivProd : deliveredProducts) {
            if (delivProd.getDecimalField(DeliveredProductFields.DELIVERED_QUANTITY) == null) {
                deliveredProductHasNull = true;
                listOfProductNumber.append(delivProd.getBelongsToField(DeliveredProductFields.PRODUCT).getStringField(
                        ProductFields.NUMBER));
                listOfProductNumber.append(", ");
            }
        }
        if (deliveredProductHasNull) {
            stateChangeContext.addMessage("deliveries.deliveredProducts.deliveredQuantity.isRequired", StateMessageType.FAILURE,
                    false, listOfProductNumber.toString());
        }
    }

}
