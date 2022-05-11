package com.qcadoo.mes.basic.logging.aspects;

import com.beust.jcommander.internal.Lists;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Configurable
public class AccessFailureLoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HttpServletRequest request;

    @Pointcut("execution(org.springframework.web.servlet.ModelAndView "
            + "com.qcadoo.view.internal.controllers.ErrorController.getAccessDeniedPageView(..)) "
            + "&& args(code, exception, predefinedExceptionMessageHeader, predefinedExceptionMessageExplanation, locale)")
    public void getAccessDeniedPageViewExecution(final int code, final Exception exception, final String predefinedExceptionMessageHeader,
                                                 final String predefinedExceptionMessageExplanation, final Locale locale) {
    }

    @AfterReturning(pointcut = "getAccessDeniedPageViewExecution(code, exception, predefinedExceptionMessageHeader, predefinedExceptionMessageExplanation, locale)", returning = "modelAndView")
    public void afterReturningGetAccessDeniedPageViewExecution(final int code, final Exception exception,
                                                               final String predefinedExceptionMessageHeader, final String predefinedExceptionMessageExplanation, final Locale locale,
                                                               final ModelAndView modelAndView) {
        if (logger.isInfoEnabled()) {
            List<String> messageStatements = Lists.newArrayList();

            messageStatements.add("ERROR CODE:" + code + ".");

            Optional.ofNullable(modelAndView.getModel().get("errorHeader"))
                    .ifPresent(header -> messageStatements.add("(" + header + ")"));

            String errorUri = (String) request.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE);

            messageStatements.add("REQUEST URI:" + (StringUtils.isBlank(errorUri) ? request.getRequestURI() : errorUri));

            messageStatements.add("AUTHENTICATION:" + SecurityContextHolder.getContext().getAuthentication());

            logger.info(String.join(" ", messageStatements));

            if (Objects.nonNull(exception)) {
                Optional.ofNullable(modelAndView.getModel().get("rootException"))
                        .ifPresent(rootException -> logger.error(rootException.toString()));
                Optional.ofNullable(modelAndView.getModel().get("stackTrace"))
                        .ifPresent(stackTrace -> logger.error(StringUtils.replace((String) stackTrace, "&nbsp;", " ")));
            }
        }
    }

}
