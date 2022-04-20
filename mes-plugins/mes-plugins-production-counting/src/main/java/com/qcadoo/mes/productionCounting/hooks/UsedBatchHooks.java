package com.qcadoo.mes.productionCounting.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.productionCounting.ProductionTrackingService;
import com.qcadoo.mes.productionCounting.constants.TrackingOperationProductInComponentFields;
import com.qcadoo.mes.productionCounting.constants.UsedBatchFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsedBatchHooks {

    @Autowired
    private ProductionTrackingService productionTrackingService;

    @Autowired
    private NumberService numberService;

    public boolean validatesWith(final DataDefinition usedBatchDD, final Entity usedBatch) {
        Entity trackingOperationProductInComponent = usedBatch
                .getBelongsToField(UsedBatchFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENT);


        List<Entity> usedBathes = Lists.newArrayList(trackingOperationProductInComponent
                .getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES));
        if (Objects.nonNull(usedBatch.getId())) {
            usedBathes = usedBathes.stream().filter(op -> !op.getId().equals(usedBatch.getId()))
                    .collect(Collectors.toList());
        }

        boolean anyMatch = usedBathes.stream()
                .anyMatch(ub -> ub.getBelongsToField(UsedBatchFields.BATCH).getId()
                        .equals(usedBatch.getBelongsToField(UsedBatchFields.BATCH).getId()));

        if(anyMatch) {
            usedBatch.addError(usedBatchDD.getField(UsedBatchFields.BATCH),
                    "productionCounting.usedBatch.error.batchAlreadyUsed");
            return false;
        }

        return true;
    }

    public void onSave(final DataDefinition usedBatchDD, final Entity usedBatch) {

        Entity trackingOperationProductInComponent = usedBatch
                .getBelongsToField(UsedBatchFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENT);

        List<Entity> usedBathes = trackingOperationProductInComponent
                .getHasManyField(TrackingOperationProductInComponentFields.USED_BATCHES);

        if (Objects.nonNull(usedBatch.getId())) {
            usedBathes = usedBathes.stream().filter(entity -> !entity.getId().equals(usedBatch.getId()))
                    .collect(Collectors.toList());
        }
        BigDecimal sumUsedBatchesQuantity = usedBathes.stream().map(ub -> ub.getDecimalField(UsedBatchFields.QUANTITY))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sumUsedBatchesQuantity = sumUsedBatchesQuantity.add(usedBatch.getDecimalField(UsedBatchFields.QUANTITY));

        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY,
                sumUsedBatchesQuantity);

        Optional<BigDecimal> givenQuantity = productionTrackingService.calculateGivenQuantity(
                trackingOperationProductInComponent, sumUsedBatchesQuantity);
        givenQuantity.ifPresent(gq -> trackingOperationProductInComponent.setField(
                TrackingOperationProductInComponentFields.GIVEN_QUANTITY, gq));
        Entity trackingOperationProductInComponentDb = trackingOperationProductInComponent.getDataDefinition()
                .save(trackingOperationProductInComponent);

        if(!trackingOperationProductInComponentDb.isValid()) {
            usedBatch.addGlobalError("productionCounting.usedBatch.error.sumUsedBatchesQuantityToBig",false);
        }


    }

    public void onDelete(final DataDefinition usedBatchDD, final Entity usedBatch) {
        Entity trackingOperationProductInComponent = usedBatch
                .getBelongsToField(UsedBatchFields.TRACKING_OPERATION_PRODUCT_IN_COMPONENT);

        BigDecimal sumUsedBatchesQuantity = trackingOperationProductInComponent
                .getDecimalField(TrackingOperationProductInComponentFields.USED_QUANTITY);

        BigDecimal newUsedBatchesQuantity = sumUsedBatchesQuantity.subtract(usedBatch.getDecimalField(UsedBatchFields.QUANTITY),
                numberService.getMathContext());
        trackingOperationProductInComponent.setField(TrackingOperationProductInComponentFields.USED_QUANTITY,
                newUsedBatchesQuantity);

        Optional<BigDecimal> givenQuantity = productionTrackingService.calculateGivenQuantity(
                trackingOperationProductInComponent, newUsedBatchesQuantity);
        givenQuantity.ifPresent(gq -> trackingOperationProductInComponent.setField(
                TrackingOperationProductInComponentFields.GIVEN_QUANTITY, gq));
        trackingOperationProductInComponent.getDataDefinition().save(trackingOperationProductInComponent);
    }
}
