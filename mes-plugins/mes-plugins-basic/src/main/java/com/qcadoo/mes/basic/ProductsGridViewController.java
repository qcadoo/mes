package com.qcadoo.mes.basic;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.security.api.SecurityService;

@Controller
public class ProductsGridViewController {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Value("${useCompressedStaticResources}")
    private boolean useCompressedStaticResources;

    @RequestMapping(value = "productsGrid", method = RequestMethod.GET)
    public ModelAndView getDashboardView(@RequestParam final Map<String, String> arguments, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("userLogin", securityService.getCurrentUserName());

        mav.addObject("translationsMap", translationService.getMessagesGroup("productsGrid", locale));

        mav.setViewName("basic/productsGrid");
        mav.addObject("useCompressedStaticResources", useCompressedStaticResources);
        return mav;
    }
}
