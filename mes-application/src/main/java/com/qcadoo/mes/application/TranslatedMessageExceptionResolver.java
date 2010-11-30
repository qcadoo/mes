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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.qcadoo.mes.api.TranslationService;

public final class TranslatedMessageExceptionResolver extends SimpleMappingExceptionResolver {

    private static final Logger LOG = LoggerFactory.getLogger(TranslatedMessageExceptionResolver.class);

    public static final String DEFAULT_EXCEPTION_MESSAGE_ATTRIBUTE = "exceptionMessage";

    private final Map<String, String> messageTranslations = new HashMap<String, String>();

    private final Map<Exception, String> exceptionTranslations = new HashMap<Exception, String>();

    private final String exceptionMessageAttribute = DEFAULT_EXCEPTION_MESSAGE_ATTRIBUTE;

    @Autowired
    private TranslationService translationService;

    @PostConstruct
    public void init() {
        exceptionTranslations.put(new DataIntegrityViolationException(""),
                "core.exception.dataIntegrityViolationException.objectInUse");
        messageTranslations.put("Entity.+ cannot be found", "core.exception.illegalStateException.entityNotFound");
    }

    @Override
    protected ModelAndView doResolveException(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception ex) {
        ModelAndView mv = super.doResolveException(request, response, handler, ex);
        if (mv != null) {
            boolean found = false;
            String exceptionMessage = ex.getMessage();

            for (Map.Entry<Exception, String> translation : exceptionTranslations.entrySet()) {
                if (translation.getKey().getClass().isInstance(ex)) {
                    exceptionMessage = translationService.translate(translation.getValue(), request.getLocale());
                    found = true;
                    break;
                }
            }

            if (!found) {
                for (Map.Entry<String, String> translation : messageTranslations.entrySet()) {
                    if (exceptionMessage.matches(translation.getKey())) {
                        exceptionMessage = translationService.translate(translation.getValue(), request.getLocale());
                        break;
                    }
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
