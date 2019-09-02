package com.qcadoo.mes.technologies.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TechnologiesWithUsingProductListListeners {

    private static final String L_GRID = "grid";

    public static final String L_FORM = "form";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void goToModifyTechnology(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        FormComponent form = (FormComponent) view.getComponentByReference(L_FORM);
        GridComponent grid = (GridComponent) view.getComponentByReference(L_GRID);
        Set<Long> selected = grid.getSelectedEntitiesIds();

               Entity modifyTechnology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODIFY_TECHNOLOGY_HELPER).create();

        modifyTechnology.setField("product", form.getEntityId());
        modifyTechnology.setField("selectedEntities", selected.stream().map(e -> e.toString()).collect( Collectors.joining( "," )));

        modifyTechnology = modifyTechnology.getDataDefinition().save(modifyTechnology);

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.id", modifyTechnology.getId());
        String url = "../page/technologies/modifyTechnology.html";
        view.openModal(url, parameters);
    }
}
