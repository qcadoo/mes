package com.qcadoo.mes.core.validation;

import com.qcadoo.mes.core.model.HookDefinition;

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

    FieldValidator range(Object from, Object to);

    FieldValidator custom(HookDefinition validateHook);

    EntityValidator customEntity(HookDefinition entityValidateHook);

}
