package com.qcadoo.mes.qualityControls;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.model.DataDefinition;

@Controller
public class QualityControlsController {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @RequestMapping(value = "qualityControl/qualityControlByDates.pdf", method = RequestMethod.GET)
    public ModelAndView qualityControlByDatesPdf(@RequestParam("type") final String type,
            @RequestParam("dateFrom") final Object dateFrom, @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlFor" + StringUtils.capitalize(type) + "PdfView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlByDates.xls", method = RequestMethod.GET)
    public ModelAndView qualityControlByDatesXls(@RequestParam("type") final String type,
            @RequestParam("dateFrom") final Object dateFrom, @RequestParam("dateTo") final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlFor" + StringUtils.capitalize(type) + "XlsView");
        mav.addObject("dateFrom", dateFrom);
        mav.addObject("dateTo", dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlReport.pdf", method = RequestMethod.GET)
    public ModelAndView qualityControlReportPdf(@RequestParam("type") final String type, @RequestParam("id") final Long[] entities) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlFor" + StringUtils.capitalize(type) + "PdfView");
        mav.addObject("entities", getQualityControlEntities(entities));
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlReport.xls", method = RequestMethod.GET)
    public ModelAndView qualityControlReportXls(@RequestParam("type") final String type, @RequestParam("id") final Long[] entities) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("qualityControlFor" + StringUtils.capitalize(type) + "XlsView");
        mav.addObject("entities", getQualityControlEntities(entities));
        return mav;
    }

    private List<Entity> getQualityControlEntities(final Long[] ids) {
        DataDefinition qualityControlDataDefinition = dataDefinitionService.get("qualityControls", "qualityControl");
        List<Entity> result = new LinkedList<Entity>();
        for (Long entityId : ids) {
            result.add(qualityControlDataDefinition.get(entityId));
        }
        return result;
    }

}
