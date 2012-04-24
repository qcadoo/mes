package com.qcadoo.mes.genealogies;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterServiceGeneal {

    public void addFieldsForParameter(final DataDefinition dataDefinition, final Entity entity) {
        entity.setField("batchForDoneOrder", "01none");
    }
}
