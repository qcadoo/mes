/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.4
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
package com.qcadoo.mes.materialFlowResources.controllers;

import com.google.common.collect.ImmutableMap;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.crud.CrudService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class MaterialFlowResourcesController {

    @Autowired
    private CrudService crudService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @RequestMapping(value = MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER + "/document.pdf", method = RequestMethod.GET)
    public final ModelAndView documentPdf(@RequestParam("id") final String id) {
        ModelAndView mav = new ModelAndView();

        mav.setViewName("documentPdf");
        mav.addObject("id", id);

        return mav;
    }

    @RequestMapping(value = "materialFlowResourcesParameters", method = RequestMethod.GET)
    public ModelAndView getMaterialFlowResourcesParametersPageView(final Locale locale) {
        DataDefinition parameterDD = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PARAMETER);
        DataDefinition documentPositionParametersDD = dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT_POSITION_PARAMETERS);

        Entity parameter = parameterService.getParameter();
        Entity documentPositionParameters = parameter.getBelongsToField("documentPositionParameters");

        if (documentPositionParameters == null) {
            List<Entity> entities = documentPositionParametersDD.find().setMaxResults(1).list().getEntities();
            if (!entities.isEmpty()) {
                documentPositionParameters = entities.get(0);
            }
            if (documentPositionParameters == null) {
                documentPositionParameters = documentPositionParametersDD.create();
                documentPositionParameters = documentPositionParametersDD.save(documentPositionParameters);
            }

            parameter.setField("documentPositionParameters", documentPositionParameters);
            parameterDD.save(parameter);
        }

        JSONObject json = new JSONObject(ImmutableMap.of("form.id", documentPositionParameters.getId()));

        Map<String, String> arguments = ImmutableMap.of("context", json.toString());

        return crudService.prepareView(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER, "materialFlowResourcesParameters",
                arguments, locale);
    }
}
