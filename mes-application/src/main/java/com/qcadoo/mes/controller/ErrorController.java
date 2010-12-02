package com.qcadoo.mes.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.TranslationService;

@Controller
public class ErrorController {

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "error", method = RequestMethod.GET)
    public ModelAndView getAccessDeniedPageView(@RequestParam final int code, final Locale locale) {
        return getAccessDeniedPageView(code, null, null, locale);
    }

    public ModelAndView getAccessDeniedPageView(@RequestParam final int code, final Exception exception,
            final String predefinedExceptionMessage, final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("core/error");

        mav.addObject("code", code);

        if (predefinedExceptionMessage != null) {

            mav.addObject("errorHeader",
                    translationService.translate("core.errorPage.error." + predefinedExceptionMessage + ".header", locale));
            mav.addObject("errorExplanation",
                    translationService.translate("core.errorPage.error." + predefinedExceptionMessage + ".explanation", locale));

        } else {

            String errorHeader = null;
            String errorExplanation = null;
            switch (code) {
                case 400: // Bad request
                    errorHeader = "core.errorPage.error.badRequest.header";
                    errorExplanation = "core.errorPage.error.badRequest.explanation";
                    break;
                case 403: // Forbidden
                    errorHeader = "core.errorPage.error.forbidden.header";
                    errorExplanation = "core.errorPage.error.forbidden.explanation";
                    break;
                case 404: // Not found
                    errorHeader = "core.errorPage.error.notFound.header";
                    errorExplanation = "core.errorPage.error.notFound.explanation";
                    break;
                case 500: // Internal Error
                    errorHeader = "core.errorPage.error.internalError.header";
                    errorExplanation = "core.errorPage.error.internalError.explanation";
                    break;
                case 503: // Gateway timeout
                    errorHeader = "core.errorPage.error.gatewayTimeout.header";
                    errorExplanation = "core.errorPage.error.gatewayTimeout.explanation";
                    break;
                default:
                    errorHeader = "core.errorPage.error.defaultError.header";
                    errorExplanation = "core.errorPage.error.defaultError.explanation";
                    break;
            }
            mav.addObject("errorHeader", translationService.translate(errorHeader, locale));
            mav.addObject("errorExplanation", translationService.translate(errorExplanation, locale));

        }

        mav.addObject("showDetailsText", translationService.translate("core.errorPage.showDetails", locale));
        mav.addObject("hideDetailsText", translationService.translate("core.errorPage.hideDetails", locale));

        if (exception != null) {
            mav.addObject("showDetails", true);

            mav.addObject("exceptionHeader", exception.getMessage());
            mav.addObject("exceptionClass", exception.getClass().getCanonicalName());

            mav.addObject("exceptionMessageText", translationService.translate("core.errorPage.details.messageText", locale));
            mav.addObject("exceptionClassText", translationService.translate("core.errorPage.details.classText", locale));
        } else {
            mav.addObject("showDetails", false);
        }

        return mav;
    }
}
