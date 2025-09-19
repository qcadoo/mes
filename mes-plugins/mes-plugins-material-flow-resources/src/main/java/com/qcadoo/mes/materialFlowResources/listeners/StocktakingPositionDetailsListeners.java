package com.qcadoo.mes.materialFlowResources.listeners;

import com.qcadoo.mes.basic.constants.PalletNumberFields;
import com.qcadoo.mes.materialFlowResources.MaterialFlowResourcesService;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingFields;
import com.qcadoo.mes.materialFlowResources.constants.StocktakingPositionFields;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class StocktakingPositionDetailsListeners {

    @Autowired
    private MaterialFlowResourcesService materialFlowResourcesService;

    public void fillTypeOfLoadUnitField(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent palletNumberLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.PALLET_NUMBER);
        LookupComponent typeOfLoadUnitLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.TYPE_OF_LOAD_UNIT);
        FormComponent form = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);

        Entity location = form.getEntity().getBelongsToField(StocktakingPositionFields.STOCKTAKING).getBelongsToField(StocktakingFields.LOCATION);
        Entity palletNumber = palletNumberLookup.getEntity();
        Long typeOfLoadUnit = null;

        if (Objects.nonNull(palletNumber)) {
            typeOfLoadUnit = materialFlowResourcesService.getTypeOfLoadUnitByPalletNumber(location.getId(), palletNumber.getStringField(PalletNumberFields.NUMBER));
        }

        typeOfLoadUnitLookup.setFieldValue(typeOfLoadUnit);
        typeOfLoadUnitLookup.requestComponentUpdateState();
    }

    public void onProductChange(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        LookupComponent batchLookup = (LookupComponent) view.getComponentByReference(StocktakingPositionFields.BATCH);
        batchLookup.setFieldValue(null);
        batchLookup.requestComponentUpdateState();
    }

}
