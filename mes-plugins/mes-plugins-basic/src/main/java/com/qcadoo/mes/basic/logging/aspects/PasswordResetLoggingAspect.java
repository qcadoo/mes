package com.qcadoo.mes.basic.logging.aspects;

import com.beust.jcommander.internal.Lists;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Aspect
public class PasswordResetLoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Pointcut("execution(java.lang.String com.qcadoo.view.internal.controllers.PasswordResetController.processForgotPasswordFormView(..)) && args(login)")
    public void processForgotPasswordFormViewExecution(final String login) {
    }

    @AfterReturning(pointcut = "processForgotPasswordFormViewExecution(login)", returning = "statusCode")
    public void afterReturningGetAccessDeniedPageViewExecution(final String login, final String statusCode) {
        if (logger.isInfoEnabled()) {
            List<String> messageStatements = Lists.newArrayList();

            messageStatements.add("REQUESTED PASSWORD CHANGE");
            messageStatements.add("FOR USER:" + login);
            messageStatements.add("STATUS:" + statusCode);
            messageStatements.add("AUTHENTICATION:" + SecurityContextHolder.getContext().getAuthentication());

            logger.info(String.join(" ", messageStatements));
        }
    }

}
