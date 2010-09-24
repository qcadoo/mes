package com.qcadoo.mes.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.core.api.TranslationService;

@Controller
public final class LoginController {

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ModelAndView getLoginPageView(@RequestParam(required = false) final String loginError,
            @RequestParam(required = false, defaultValue = "false") final Boolean iframe,
            @RequestParam(required = false, defaultValue = "false") final Boolean logout,
            @RequestParam(required = false, defaultValue = "false") final Boolean timeout, final Locale locale) {

        ModelAndView mav = new ModelAndView();
        mav.setViewName("login");
        mav.addObject("translation", translationService.getLoginTranslations(locale));
        mav.addObject("currentLanguage", locale.getLanguage());
        mav.addObject("iframe", iframe);

        if (logout) {
            mav.addObject("successMessage", "login.message.logout");
        } else if (timeout) {
            mav.addObject("errorMessage", "login.message.timeout");
        } else if (loginError != null) {
            mav.addObject("errorMessage", "login.message.error");
        } else if (iframe) {
            mav.addObject("errorMessage", "login.message.timeout");
        }

        return mav;
    }

    @RequestMapping(value = "accessDenied", method = RequestMethod.GET)
    public ModelAndView getAccessDeniedPageView(final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("accessDenied");

        mav.addObject("translation", translationService.getLoginTranslations(locale));

        return mav;
    }
}
