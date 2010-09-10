package com.qcadoo.mes.plugins.products.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.plugins.products.translation.TranslationService;

@Controller
public class LoginController {

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ModelAndView getLoginPageView(@RequestParam(required = false) String login_error,
            @RequestParam(required = false) Boolean iframe, final Locale locale) {

        if (iframe == null) {
            iframe = false;
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName("login");

        mav.addObject("translation", translationService.getLoginTranslations(locale));
        mav.addObject("currentLanguage", locale.getLanguage());

        mav.addObject("iframe", iframe);

        if (login_error != null) {
            mav.addObject("errorMessage", "login.message.error");
        } else if (iframe) {
            mav.addObject("errorMessage", "login.message.timeout");
        }

        return mav;
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public ModelAndView getLogoutPageView(final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("login");

        mav.addObject("translation", translationService.getLoginTranslations(locale));
        mav.addObject("currentLanguage", locale.getLanguage());

        mav.addObject("successMessage", "login.message.logout");

        return mav;
    }

    @RequestMapping(value = "timeout", method = RequestMethod.GET)
    public ModelAndView getTimeoutPageView(final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("login");

        mav.addObject("translation", translationService.getLoginTranslations(locale));
        mav.addObject("currentLanguage", locale.getLanguage());

        mav.addObject("errorMessage", "login.message.timeout");

        return mav;
    }

}
