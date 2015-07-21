package com.qcadoo.mes.technologies.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.util.UnitService;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class OPICDetailsHooks {

    @Autowired
    private UnitService unitService;

    public void fillUnitBeforeRender(final ViewDefinitionState view) {

        unitService.fillProductUnitBeforeRenderIfEmpty(view, OperationProductInComponentFields.UNIT);
        unitService.fillProductUnitBeforeRenderIfEmpty(view, OperationProductInComponentFields.GIVEN_UNIT);
    }
}
