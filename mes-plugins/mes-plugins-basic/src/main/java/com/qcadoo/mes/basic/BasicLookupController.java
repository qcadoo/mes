package com.qcadoo.mes.basic;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.security.api.SecurityService;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

public abstract class BasicLookupController {

    @Autowired
    protected TranslationService translationService;

    @Autowired
    protected SecurityService securityService;

    @Value("${useCompressedStaticResources}")
    protected boolean useCompressedStaticResources;

    protected ModelAndView getModelAndView(final String name, final Locale locale) {
        ModelAndView mav = new ModelAndView();

        mav.addObject("userLogin", securityService.getCurrentUserName());

        mav.addObject("translationsMap", translationService.getMessagesGroup(name, locale));

        mav.setViewName("basic/" + name);
        mav.addObject("useCompressedStaticResources", useCompressedStaticResources);
        return mav;
    }

    public abstract ModelAndView getLookupView(@RequestParam final Map<String, String> arguments, final Locale locale);

    public abstract List getRecords(@RequestParam String sidx, @RequestParam String sord);

}
