package com.qcadoo.mes.basic.logging.aspects;

import com.beust.jcommander.internal.Lists;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@Aspect
public class RegexFailedValidationLoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Pointcut("execution(boolean com.qcadoo.model.internal.validators.RegexValidator.call(..)) && target(target) && args(entity, oldValue, newValue)")
    public void callExecution(final Object target, final Entity entity, final Object oldValue, final Object newValue) {
    }

    @AfterReturning(pointcut = "callExecution(target, entity, oldValue, newValue)", returning = "result")
    public void afterReturningCallExecution(final Object target, final Entity entity, final Object oldValue, final Object newValue, final boolean result) {
        if (!result && logger.isInfoEnabled()) {
            try {
                List<String> messageStatements = Lists.newArrayList();

                FieldDefinition fieldDefinition = (FieldDefinition) FieldUtils.readField(target, "fieldDefinition", true);
                DataDefinition dataDefinition = fieldDefinition.getDataDefinition();

                messageStatements.add("INPUT REJECTED FOR ENTITY: " + dataDefinition.getPluginIdentifier() + "." + dataDefinition.getName());
                messageStatements.add("AND FIELD: " + fieldDefinition.getName());
                messageStatements.add("OLD VALUE: " + Optional.ofNullable(oldValue).map(Object::toString).orElse(StringUtils.EMPTY));
                messageStatements.add("NEW VALUE: " + Optional.ofNullable(newValue).map(Object::toString).orElse(StringUtils.EMPTY));
                messageStatements.add("AUTHENTICATION:" + SecurityContextHolder.getContext().getAuthentication());

                logger.info(String.join(" ", messageStatements));
            } catch (IllegalAccessException iae) {
                // ignore
            }
        }
    }

}
