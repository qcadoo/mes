package com.qcadoo.mes.cmmsMachineParts.reports;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PlannedEventsReportController {

    @RequestMapping(value = "/cmmsMachineParts/plannedEvents.xlsx", method = RequestMethod.GET)
    public ModelAndView generatePlannedEventsReport(@RequestParam("context") final String context) {
        HashMap<String, String> filtersMap = new HashMap<String, String>();
        try {
            JSONObject jObject = new JSONObject(context);
            Iterator<?> keys = jObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = jObject.getString(key);
                filtersMap.put(key, value);
            }
        } catch (JSONException e) {
            filtersMap = new HashMap<String, String>();
        }
        return new ModelAndView("plannedEventsXlsView", "filtersMap", filtersMap);
    }

}
