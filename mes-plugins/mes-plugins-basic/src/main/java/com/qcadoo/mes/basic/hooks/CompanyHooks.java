package com.qcadoo.mes.basic.hooks;

import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class CompanyHooks {

    public void onCopy(final DataDefinition companyDD, final Entity company) {
        company.setField(CompanyFields.EXTERNAL_NUMBER, null);
    }
}
