package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.mes.advancedGenealogy.criteriaModifier.BatchCriteriaModifier;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.ResourceCorrectionFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ResourceCorrectionDetailsHooks {

    @Autowired
    private BatchCriteriaModifier batchCriteriaModifier;

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void onBeforeRender(final ViewDefinitionState view) {
        FormComponent resourceCorrectionForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity resourceCorrection = resourceCorrectionForm.getPersistedEntityWithIncludedFormValues();

        materialFlowResourcesService.fillUnitFieldValues(view);

        setBatchLookupsProductFilterValue(view, resourceCorrection);
    }

    private void setBatchLookupsProductFilterValue(final ViewDefinitionState view, final Entity resourceCorrection) {
        LookupComponent oldBatchLookup = (LookupComponent) view.getComponentByReference(ResourceCorrectionFields.OLD_BATCH);
        LookupComponent newBatchLookup = (LookupComponent) view.getComponentByReference(ResourceCorrectionFields.NEW_BATCH);

        Entity product = resourceCorrection.getBelongsToField(ResourceCorrectionFields.PRODUCT);

        if (Objects.nonNull(product)) {
            batchCriteriaModifier.putProductFilterValue(oldBatchLookup, product);
            batchCriteriaModifier.putProductFilterValue(newBatchLookup, product);
        }
    }

}
