package com.qcadoo.mes.core.data.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.internal.validators.BeanMethodValidator;
import com.qcadoo.mes.core.data.internal.validators.MaxLenghtValidator;
import com.qcadoo.mes.core.data.internal.validators.RangeValidator;
import com.qcadoo.mes.core.data.internal.validators.RequiredValidator;
import com.qcadoo.mes.core.data.internal.validators.UniqueValidator;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;

@Service
public final class FieldValidatorFactoryImpl implements FieldValidatorFactory {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public FieldValidator required() {
        return new RequiredValidator();
    }

    @Override
    public FieldValidator unique() {
        return new UniqueValidator();
    }

    @Override
    public FieldValidator maxLength(final int maxLenght) {
        return new MaxLenghtValidator(maxLenght);
    }

    @Override
    public FieldValidator range(final Object from, final Object to) {
        return new RangeValidator(from, to);
    }

    @Override
    public FieldValidator beanMethod(final String beanName, final String staticValidateMethodName) {
        return new BeanMethodValidator(applicationContext.getBean(beanName), staticValidateMethodName);
    }

}
