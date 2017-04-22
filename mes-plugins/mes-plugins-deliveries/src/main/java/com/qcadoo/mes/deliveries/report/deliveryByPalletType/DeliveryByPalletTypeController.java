package com.qcadoo.mes.deliveries.report.deliveryByPalletType;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

@Controller
class DeliveryByPalletTypeController {

    @RequestMapping(value = "/deliveries/deliveryByPalletType.xlsx", method = RequestMethod.GET)
    public ModelAndView generatePlannedEventsReport(@RequestParam("from") final Long from, @RequestParam("to") final Long to) {
        HashMap<String, Long> params = new HashMap<String, Long>();
        params.put("from", from);
        params.put("to", to);
        return new ModelAndView("deliveryByPalletTypeXlsView", "params", params);
    }
}
