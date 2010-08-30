package com.qcadoo.mes.plugins.products.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.qcadoo.mes.core.data.api.DataAccessService;
import com.qcadoo.mes.core.data.api.DataDefinitionService;
import com.qcadoo.mes.plugins.products.validation.ValidationService;

@Controller
@RequestMapping(value = "/substitutes")
public class SubstituteController extends CrudTemplate {

    public SubstituteController(DataDefinitionService dataDefinitionService, DataAccessService dataAccessService,
            ValidationService validationUtils) {
        super("products.substitute", dataDefinitionService, dataAccessService, validationUtils);
    }

}
