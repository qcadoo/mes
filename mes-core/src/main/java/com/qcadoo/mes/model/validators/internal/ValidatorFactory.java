package com.qcadoo.mes.model.validators.internal;

import com.qcadoo.mes.model.HookDefinition;
import com.qcadoo.mes.model.validators.EntityValidator;
import com.qcadoo.mes.model.validators.FieldValidator;

/**
 * @apiviz.uses com.qcadoo.mes.core.data.definition.FieldValidator
 */

public interface ValidatorFactory {

    FieldValidator required();

    FieldValidator requiredOnCreate();

    FieldValidator unique();

    FieldValidator length(Integer min, Integer is, Integer max);

    FieldValidator scale(Integer min, Integer is, Integer max);

    FieldValidator precision(Integer min, Integer is, Integer max);

    FieldValidator range(Object from, Object to, boolean inclusive);

    FieldValidator custom(HookDefinition validateHook);

    EntityValidator customEntity(HookDefinition entityValidateHook);

}
