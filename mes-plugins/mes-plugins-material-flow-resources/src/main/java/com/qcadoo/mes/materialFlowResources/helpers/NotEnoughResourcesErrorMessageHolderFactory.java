package com.qcadoo.mes.materialFlowResources.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qcadoo.model.api.NumberService;

@Component
public class NotEnoughResourcesErrorMessageHolderFactory {

    @Autowired
    private NumberService numberService;

    public NotEnoughResourcesErrorMessageHolder create() {
        return new NotEnoughResourcesErrorMessageHolder(numberService);
    }

}
