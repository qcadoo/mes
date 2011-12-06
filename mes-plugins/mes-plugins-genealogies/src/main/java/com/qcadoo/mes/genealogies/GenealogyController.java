/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.1.0
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
package com.qcadoo.mes.genealogies;

import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.genealogies.constants.GenealogiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.crud.CrudService;

@Controller
public class GenealogyController {

    private static final String REQUEST_PARAM_VALUE = "value";

    @Autowired
    private CrudService crudController;

    @Autowired
    private GenealogyAttributeService genealogyService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @RequestMapping(value = "genealogyAttribute", method = RequestMethod.GET)
    public ModelAndView getGenealogyAttributesPageView(final Locale locale) {

        JSONObject json = new JSONObject(ImmutableMap.of("form.id", genealogyService.getGenealogyAttributeId().toString()));

        Map<String, String> arguments = ImmutableMap.of("context", json.toString());
        return crudController.prepareView(GenealogiesConstants.PLUGIN_IDENTIFIER, GenealogiesConstants.VIEW_CURRENT_ATTRIBUTE,
                arguments, locale);
    }

    @RequestMapping(value = "genealogiesForComponents/genealogyForComponent.pdf", method = RequestMethod.GET)
    public ModelAndView genealogyForComponentPdf(@RequestParam(REQUEST_PARAM_VALUE) final String value) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("genealogyForComponentView");
        mav.addObject(REQUEST_PARAM_VALUE, value);
        mav.addObject("company", dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult());
        return mav;
    }

    @RequestMapping(value = "genealogies/genealogyForProduct.pdf", method = RequestMethod.GET)
    public ModelAndView genealogyForProductPdf(@RequestParam(REQUEST_PARAM_VALUE) final String value) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("genealogyForProductView");
        mav.addObject(REQUEST_PARAM_VALUE, value);
        mav.addObject("company", dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_COMPANY).find()
                .add(SearchRestrictions.eq("owner", true)).setMaxResults(1).uniqueResult());
        return mav;
    }
}
