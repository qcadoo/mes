package com.qcadoo.mes.qualityControls;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterServiceQC {

    public void addFieldsForParameter(final DataDefinition dataDefinition, final Entity parameter) {
        parameter.setField("autoGenerateQualityControl", false);
        parameter.setField("checkDoneOrderForQuality", false);
    }
}
