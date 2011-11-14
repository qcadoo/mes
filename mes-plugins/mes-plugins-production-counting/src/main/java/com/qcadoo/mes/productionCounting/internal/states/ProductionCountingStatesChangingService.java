package com.qcadoo.mes.productionCounting.internal.states;

import static com.google.common.base.Preconditions.checkArgument;
import static com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates.ACCEPTED;
import static com.qcadoo.mes.productionCounting.internal.states.ProductionCountingStates.DRAFT;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.Entity;

@Service
public class ProductionCountingStatesChangingService {

    void performChangeState(final Entity newEntity, final Entity oldEntity) {
        checkArgument(newEntity != null, "New entity is null");
        checkArgument(oldEntity != null, "Old entity is null");

        ProductionCountingStates state = ProductionCountingStates.valueOf(newEntity.getStringField("state"));

        if (oldEntity == null && !state.equals(DRAFT)) {
            throw new IllegalStateException();
        }
        if (oldEntity != null && state.equals(ACCEPTED)
                && !ProductionCountingStates.valueOf(oldEntity.getStringField("state")).equals(DRAFT)) {
            throw new IllegalStateException();
        }
        switch (state) {
            case DRAFT:
                performAccepted();
                break;
            case ACCEPTED:
                performDeclined();
            default:
                throw new IllegalStateException("unknown product type");
        }

    }

    public void performAccepted() {

    }

    public void performDeclined() {
    }
}
