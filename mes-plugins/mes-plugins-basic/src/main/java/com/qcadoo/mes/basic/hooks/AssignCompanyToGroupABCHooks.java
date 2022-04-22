package com.qcadoo.mes.basic.hooks;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignCompanyToGroupABCHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) {

    }

    public void assignToGroupABC(final ViewDefinitionState view, final ComponentState state, final String[] args) throws JSONException {
        String idsStr =  view.getJsonContext().getString("window.mainTab.assignToGroupABCForm.companiesIds");
        List<Long> ids = Lists.newArrayList(idsStr.split(",")).stream().map(Long::valueOf)
                .collect(Collectors.toList());

        String abcValue = (String) view.getComponentByReference(CompanyFields.ABC_ANALYSIS).getFieldValue();
        ids.forEach(id -> {
            Entity company = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_COMPANY).get(id);
            company.setField(CompanyFields.ABC_ANALYSIS, abcValue);
            company.getDataDefinition().save(company);
        });

        view.addMessage("basic.assignCompanyToGroupABC.assign.success", ComponentState.MessageType.SUCCESS);
    }
}