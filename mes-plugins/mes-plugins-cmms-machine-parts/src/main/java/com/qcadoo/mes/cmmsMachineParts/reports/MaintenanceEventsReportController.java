package com.qcadoo.mes.cmmsMachineParts.reports;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Iterator;

@Controller
public class MaintenanceEventsReportController {

    @RequestMapping(value = "/cmmsMachineParts/maintenanceEvents.xlsx", method = RequestMethod.GET)
    public ModelAndView generateMaintenanceEventsReport(@RequestParam("filters") final String filters) {
        HashMap<String, String> filtersMap = new HashMap<String, String>();
        try {
            JSONObject jObject = new JSONObject(filters);
            Iterator<?> keys = jObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = jObject.getString(key);
                filtersMap.put(key, value);
            }
        } catch (JSONException e) {
            filtersMap = new HashMap<String, String>();
        }
        return new ModelAndView("maintenanceEventsXlsView", "filtersMap", filtersMap);

    }

}
