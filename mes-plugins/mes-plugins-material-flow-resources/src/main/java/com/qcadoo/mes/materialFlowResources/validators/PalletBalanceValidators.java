package com.qcadoo.mes.materialFlowResources.validators;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.materialFlowResources.constants.PalletBalanceFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class PalletBalanceValidators {

    public boolean validate(final DataDefinition dataDefinition, final Entity palletBalance) {
        Date dateFrom = palletBalance.getDateField(PalletBalanceFields.DATE_FROM);
        Date dateTo = palletBalance.getDateField(PalletBalanceFields.DATE_TO);
        if (dateFrom != null && dateTo != null && dateTo.before(dateFrom)) {
            palletBalance.addError(dataDefinition.getField(PalletBalanceFields.DATE_FROM),
                    "materialFlowResource.palletBalance.validation.error.dateFrom");
            return false;
        }
        return true;
    }
}
