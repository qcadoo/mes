package com.qcadoo.mes.core.data.internal;

import com.qcadoo.mes.core.data.beans.Entity;
import com.qcadoo.mes.core.data.model.DataDefinition;

public class CustomEntityValidateMethod {

    /**
     * 
     */
    private final ValidatorTest CustomEntityValidateMethod;

    /**
     * @param validatorTest
     */
    CustomEntityValidateMethod(ValidatorTest validatorTest) {
        CustomEntityValidateMethod = validatorTest;
    }

    public boolean hasAge18AndNameMrT(final DataDefinition dataDefinition, final Entity entity) {
        return (entity.getField("age").equals(18) && entity.getField("name").equals("Mr T"));
    }

}