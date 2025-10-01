package com.qcadoo.mes.materialFlowResources.states;

import com.qcadoo.mes.materialFlowResources.states.constants.StocktakingStateChangeDescriber;
import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StocktakingStateService extends BasicStateService implements StocktakingServiceMarker {

    @Autowired
    private StocktakingStateChangeDescriber stocktakingStateChangeDescriber;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return stocktakingStateChangeDescriber;
    }
}
