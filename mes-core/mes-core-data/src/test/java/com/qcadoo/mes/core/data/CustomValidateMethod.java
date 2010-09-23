package com.qcadoo.mes.core.data;

import com.qcadoo.mes.core.data.model.DataDefinition;

public class CustomValidateMethod {

    /**
     * 
     */
    private final ValidatorTest CustomValidateMethod;

    /**
     * @param validatorTest
     */
    CustomValidateMethod(ValidatorTest validatorTest) {
        CustomValidateMethod = validatorTest;
    }

    public boolean isEqualToQwerty(final DataDefinition dataDefinition, final Object object) {
        return String.valueOf(object).equals("qwerty");
    }

}