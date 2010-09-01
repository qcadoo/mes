package com.qcadoo.mes.core.data.internal.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.validation.EntityValidator;
import com.qcadoo.mes.core.data.validation.FieldValidator;
import com.qcadoo.mes.core.data.validation.FieldValidatorFactory;

@Service
public final class FieldValidatorFactoryImpl implements FieldValidatorFactory {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataAccessService dataAccessService;

    @Override
    public FieldValidator required() {
        return new RequiredValidator();
    }

    @Override
    public FieldValidator unique() {
        return new UniqueValidator(dataAccessService);
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
    public FieldValidator custom(final String beanName, final String staticValidateMethodName) {
        return new CustomValidator(applicationContext.getBean(beanName), staticValidateMethodName);
    }

    @Override
    public EntityValidator customEntity(final String beanName, final String staticValidateMethodName) {
        return new CustomEntityValidator(applicationContext.getBean(beanName), staticValidateMethodName);
    }
}
