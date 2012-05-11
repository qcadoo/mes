package com.qcadoo.mes.samples.hook;

import static com.qcadoo.mes.samples.constants.ParameterFieldsSamples.SAMPLES_WERE_LOADED;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class ParameterModelHooksSamples {

    public void setSamplesField(final DataDefinition dataDefinition, final Entity parameter) {
        if (parameter.getField(SAMPLES_WERE_LOADED) == null) {
            parameter.setField(SAMPLES_WERE_LOADED, false);
        }
    }
}
