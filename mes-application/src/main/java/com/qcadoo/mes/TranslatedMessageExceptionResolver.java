package com.qcadoo.mes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.qcadoo.mes.api.TranslationService;

public final class TranslatedMessageExceptionResolver extends SimpleMappingExceptionResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TranslatedMessageExceptionResolver.class);

    public static final String DEFAULT_EXCEPTION_MESSAGE_ATTRIBUTE = "exceptionMessage";

    private static final String OBJECT_IN_USE = "Trying delete entity in use";

    private final String exceptionMessageAttribute = DEFAULT_EXCEPTION_MESSAGE_ATTRIBUTE;

    @Autowired
    private TranslationService translationService;

    @Override
    protected ModelAndView doResolveException(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception ex) {
        ModelAndView mv = super.doResolveException(request, response, handler, ex);
        if (mv != null) {
            String exceptionMessage = ex.getMessage();
            if (OBJECT_IN_USE.equals(ex.getMessage())) {
                exceptionMessage = translationService.translate("core.exception.illegalStateException.objectInUse",
                        request.getLocale());
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding exception message to view: " + exceptionMessage);
            }
            mv.addObject(this.exceptionMessageAttribute, exceptionMessage);
        }
        return mv;
    }
}
