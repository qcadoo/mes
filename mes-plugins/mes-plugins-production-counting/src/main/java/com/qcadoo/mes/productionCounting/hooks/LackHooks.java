package com.qcadoo.mes.productionCounting.hooks;

import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductOutComponentFields;
import com.qcadoo.mes.productionCounting.constants.UsedBatchFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LackHooks {

    public static final String L_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT = "trackingOperationProductOutComponent";
    public static final String L_LACK_QUANTITY = "lackQuantity";
    public static final String L_LACK_REASONS = "lackReasons";
    public static final String L_CAUSE_OF_WASTES = "causeOfWastes";

    @Autowired
    private NumberService numberService;

    public boolean validatesWith(final DataDefinition lackDD, final Entity lack) {
        boolean valid = true;
        if (lack.getHasManyField(L_LACK_REASONS).isEmpty()) {
            valid = false;
            lack.addGlobalError("productionCounting.lack.error.reasonsNotDefined");
        }

        List<Entity> duplicates = lack.getHasManyField(L_LACK_REASONS).stream()
                .collect(Collectors.groupingBy(e -> e.getStringField(L_CAUSE_OF_WASTES)))
                .entrySet().stream()
                .filter(e->e.getValue().size() > 1)
                .flatMap(e->e.getValue().stream())
                .collect(Collectors.toList());

        if(!duplicates.isEmpty()) {
            valid = false;
            lack.addGlobalError("productionCounting.lack.error.reasonsIsDuplicated");
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
        trackingOperationProductOutComponent.getDataDefinition().fastSave(trackingOperationProductOutComponent);
    }

    public void onDelete(final DataDefinition lackDD, final Entity lack) {
        Entity trackingOperationProductOutComponent = lack
                .getBelongsToField(L_TRACKING_OPERATION_PRODUCT_OUT_COMPONENT);

        BigDecimal sumLacksQuantity = trackingOperationProductOutComponent
                .getDecimalField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY);

        if(Objects.isNull(sumLacksQuantity)) {
            return;
        }

        BigDecimal newSumLacksQuantity = sumLacksQuantity.subtract(lack.getDecimalField(L_LACK_QUANTITY),
                numberService.getMathContext());
        trackingOperationProductOutComponent.setField(TrackingOperationProductOutComponentFields.WASTES_QUANTITY,
                newSumLacksQuantity);

        trackingOperationProductOutComponent.getDataDefinition().fastSave(trackingOperationProductOutComponent);
    }
}
