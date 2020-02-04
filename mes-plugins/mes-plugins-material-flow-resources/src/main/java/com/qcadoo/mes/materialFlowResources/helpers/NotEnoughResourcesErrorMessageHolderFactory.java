package com.qcadoo.mes.materialFlowResources.helpers;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.model.api.NumberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotEnoughResourcesErrorMessageHolderFactory {

    @Autowired
    private NumberService numberService;

    @Autowired
    private TranslationService translationService;

    public NotEnoughResourcesErrorMessageHolder create() {
        return new NotEnoughResourcesErrorMessageHolder(numberService, translationService);
    }

}
