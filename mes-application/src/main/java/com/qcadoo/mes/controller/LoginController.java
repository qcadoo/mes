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
public final class LoginController {

    @Autowired
    private TranslationService translationService;

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public ModelAndView getLoginPageView(@RequestParam(required = false) final String loginError,
            @RequestParam(required = false, defaultValue = "false") final Boolean iframe,
            @RequestParam(required = false, defaultValue = "false") final Boolean logout,
            @RequestParam(required = false, defaultValue = "false") final Boolean timeout, final Locale locale) {

        ModelAndView mav = new ModelAndView();
        mav.setViewName("core/login");
        mav.addObject("translation", translationService.getSecurityMessages(locale));
        mav.addObject("currentLanguage", locale.getLanguage());

        mav.addObject("iframe", iframe);

        if (logout) {
            mav.addObject("messageType", "success");
            mav.addObject("messageHeader", "security.message.logoutHeader");
            mav.addObject("messageContent", "security.message.logoutContent");
        } else if (timeout || iframe) {
            mav.addObject("messageType", "info");
            mav.addObject("messageHeader", "security.message.timeoutHeader");
            mav.addObject("messageContent", "security.message.timeoutContent");
        } else if (loginError != null) {
            mav.addObject("messageType", "error");
            mav.addObject("messageHeader", "security.message.errorHeader");
            mav.addObject("messageContent", "security.message.errorContent");
        }

        return mav;
    }

    @RequestMapping(value = "accessDenied", method = RequestMethod.GET)
    public ModelAndView getAccessDeniedPageView(final Locale locale) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("core/accessDenied");

        mav.addObject("translation", translationService.getSecurityMessages(locale));

        return mav;
    }
}
