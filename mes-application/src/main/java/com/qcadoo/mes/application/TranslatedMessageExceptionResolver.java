/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.application;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
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

    private final Map<String, String> translations = new HashMap<String, String>();

    private final String exceptionMessageAttribute = DEFAULT_EXCEPTION_MESSAGE_ATTRIBUTE;

    @Autowired
    private TranslationService translationService;

    @PostConstruct
    public void init() {
        translations.put("Trying delete entity in use", "core.exception.illegalStateException.objectInUse");
        translations.put("Entity.+ cannot be found", "core.exception.illegalStateException.entityNotFound");
    }

    @Override
    protected ModelAndView doResolveException(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception ex) {
        ModelAndView mv = super.doResolveException(request, response, handler, ex);
        if (mv != null) {
            String exceptionMessage = ex.getMessage();

            for (Map.Entry<String, String> translation : translations.entrySet()) {
                if (exceptionMessage.matches(translation.getKey())) {
                    exceptionMessage = translationService.translate(translation.getValue(), request.getLocale());
                    break;
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding exception message to view: " + exceptionMessage);
            }
            mv.addObject(this.exceptionMessageAttribute, exceptionMessage);
        }
        return mv;
    }
}
