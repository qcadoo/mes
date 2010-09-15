package com.qcadoo.mes.crud.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.crud.translation.TranslationService;

@Controller
public class LoginController {

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ModelAndView getLoginPageView(@RequestParam(required = false) String login_error,
            @RequestParam(required = false) Boolean iframe, @RequestParam(required = false) Boolean logout,
            @RequestParam(required = false) Boolean timeout, final Locale locale) {

        if (iframe == null) {
            iframe = false;
        }
        if (logout == null) {
            logout = false;
        }
        if (timeout == null) {
            timeout = false;
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName("login");

        mav.addObject("translation", translationService.getLoginTranslations(locale));
        mav.addObject("currentLanguage", locale.getLanguage());

        mav.addObject("iframe", iframe);

        if (logout) {
            mav.addObject("successMessage", "login.message.logout");
        } else if (timeout) {
            mav.addObject("errorMessage", "login.message.timeout");
        } else if (login_error != null) {
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
