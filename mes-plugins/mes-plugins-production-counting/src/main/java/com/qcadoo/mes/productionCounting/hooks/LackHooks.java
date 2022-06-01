package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.UsedBatchFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LackHooks {

    public static final String L_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT = "trackingOperationProductOutComponent";
    public static final String L_LACK_QUANTITY = "lackQuantity";


    public boolean validatesWith(final DataDefinition lackDD, final Entity lack) {
        boolean valid = true;
        if (lack.getHasManyField("lackReasons").isEmpty()) {
            valid = false;
            lack.addGlobalError("productionCounting.lack.error.reasonsNotDefined");
        }
        return valid;
    }

    public void onSave(final DataDefinition lackDD, final Entity lack) {
        Entity trackingOperationProductOutComponent = lack
                .getBelongsToField(L_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT);


        List<Entity> lacks = trackingOperationProductOutComponent
                .getHasManyField(TrackingOperationProductOutComponentFields.LACKS);

        if (Objects.nonNull(lack.getId())) {
            lacks = lacks.stream().filter(entity -> !entity.getId().equals(lack.getId()))
                    .collect(Collectors.toList());
        }

        BigDecimal sumOfLacks = lacks.stream().map(ub -> ub.getDecimalField(L_LACK_QUANTITY))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sumOfLacks = sumOfLacks.add(lack.getDecimalField(L_LACK_QUANTITY));

        trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY,
                sumOfLacks);
        trackingOperationProductOutComponent.getDataDefinition().save(trackingOperationProductOutComponent);
    }
}
