package com.qcadoo.mes.materialFlowResources.states;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.materialFlowResources.constants.ResourceFields;
import com.qcadoo.mes.materialFlowResources.exceptions.InvalidResourceException;
import com.qcadoo.mes.materialFlowResources.service.ResourceManagementService;
import com.qcadoo.mes.materialFlowResources.states.constants.RepackingStateChangeDescriber;
import com.qcadoo.mes.materialFlowResources.states.constants.RepackingStateStringValues;
import com.qcadoo.mes.newstates.BasicStateService;
import com.qcadoo.mes.states.StateChangeEntityDescriber;
import com.qcadoo.model.api.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RepackingStateService extends BasicStateService implements RepackingServiceMarker {

    private static final Logger LOG = LoggerFactory.getLogger(RepackingStateService.class);

    @Autowired
    private RepackingStateChangeDescriber repackingStateChangeDescriber;

    @Autowired
    private ResourceManagementService resourceManagementService;


    @Autowired
    private TranslationService translationService;

    @Override
    public StateChangeEntityDescriber getChangeEntityDescriber() {
        return repackingStateChangeDescriber;
    }

    @Override
    public Entity onAfterSave(Entity entity, String sourceState, String targetState, Entity stateChangeEntity,
                              StateChangeEntityDescriber describer) {
        switch (targetState) {
            case RepackingStateStringValues.ACCEPTED:
                try {
                    resourceManagementService.repackageResources(entity);
                } catch (InvalidResourceException ire) {
                    entity.setNotValid();

                    String productNumber = ire.getEntity().getBelongsToField(ResourceFields.PRODUCT)
                            .getStringField(ProductFields.NUMBER);
                    if ("materialFlow.error.position.batch.required"
                            .equals(ire.getEntity().getError(ResourceFields.BATCH).getMessage())) {
                        LOG.error(translationService.translate(
                                "materialFlow.document.validate.global.error.invalidResource.batchRequired",
                                LocaleContextHolder.getLocale(), productNumber));
                    } else {
                        String resourceNumber = ire.getEntity().getStringField(ResourceFields.NUMBER);

                        LOG.error(translationService.translate("materialFlow.document.validate.global.error.invalidResource",
                                LocaleContextHolder.getLocale(), resourceNumber, productNumber));
                    }
                }
                break;
        }

        return entity;
    }

}
