/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.2
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
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

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.qualityControls.constants.QualityControlsConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchRestrictions;

@Controller
public class QualityControlsController {

    private static final String QUALITY_CONTROL_FOR = "qualityControlFor";
    private static final String DATE_TO = "dateTo";
    private static final String DATE_FROM = "dateFrom";
    private static final String TYPE = "type";
    @Autowired
    private DataDefinitionService dataDefinitionService;

    @RequestMapping(value = "qualityControl/qualityControlByDates.pdf", method = RequestMethod.GET)
    public final ModelAndView qualityControlByDatesPdf(@RequestParam(TYPE) final String type,
            @RequestParam(DATE_FROM) final Object dateFrom, @RequestParam(DATE_TO) final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(QUALITY_CONTROL_FOR + StringUtils.capitalize(type) + "PdfView");
        mav.addObject(DATE_FROM, dateFrom);
        mav.addObject(DATE_TO, dateTo);
        mav.addObject("company", dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult());
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlByDates.xls", method = RequestMethod.GET)
    public final ModelAndView qualityControlByDatesXls(@RequestParam(TYPE) final String type,
            @RequestParam(DATE_FROM) final Object dateFrom, @RequestParam(DATE_TO) final Object dateTo) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(QUALITY_CONTROL_FOR + StringUtils.capitalize(type) + "XlsView");
        mav.addObject(DATE_FROM, dateFrom);
        mav.addObject(DATE_TO, dateTo);
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlReport.pdf", method = RequestMethod.GET)
    public final ModelAndView qualityControlReportPdf(@RequestParam(TYPE) final String type,
            @RequestParam("id") final Long[] entities) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(QUALITY_CONTROL_FOR + StringUtils.capitalize(type) + "PdfView");
        mav.addObject("entities", getQualityControlEntities(entities));
        mav.addObject("company", dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult());
        return mav;
    }

    @RequestMapping(value = "qualityControl/qualityControlReport.xls", method = RequestMethod.GET)
    public final ModelAndView qualityControlReportXls(@RequestParam(TYPE) final String type,
            @RequestParam("id") final Long[] entities) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(QUALITY_CONTROL_FOR + StringUtils.capitalize(type) + "XlsView");
        mav.addObject("entities", getQualityControlEntities(entities));
        return mav;
    }

    private List<Entity> getQualityControlEntities(final Long[] ids) {
        DataDefinition qualityControlDataDefinition = dataDefinitionService.get(QualityControlsConstants.PLUGIN_IDENTIFIER,
                QualityControlsConstants.MODEL_QUALITY_CONTROL);
        List<Entity> result = new LinkedList<Entity>();
        for (Long entityId : ids) {
            result.add(qualityControlDataDefinition.get(entityId));
        }
        return result;
    }

}
