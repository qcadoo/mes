package com.qcadoo.mes.masterOrders.hooks;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.masterOrders.constants.SalesPlanProductFields;
import com.qcadoo.mes.orders.criteriaModifiers.TechnologyCriteriaModifiersO;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.LookupComponent;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class SalesPlanUseOtherTechnologyHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void onBeforeRender(final ViewDefinitionState view) throws JSONException {
        String oldTechnologyId = view.getJsonContext().get("window.mainTab.salesPlanProduct.gridLayout.oldTechnologyId").toString();
        Entity technology = dataDefinitionService
                .get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY)
                .get(Long.parseLong(oldTechnologyId));
        Entity product = technology.getBelongsToField(TechnologyFields.PRODUCT);

        FieldComponent oldTechnologyField = (FieldComponent) view.getComponentByReference("oldTechnology");
        oldTechnologyField.setFieldValue(technology.getStringField(TechnologyFields.NUMBER));
        LookupComponent technologyLookup = (LookupComponent) view.getComponentByReference(SalesPlanProductFields.TECHNOLOGY);
        FilterValueHolder technologyFilterValueHolder = technologyLookup.getFilterValue();
        technologyFilterValueHolder.put(TechnologyCriteriaModifiersO.PRODUCT_PARAMETER, product.getId());
        technologyLookup.setFilterValue(technologyFilterValueHolder);
        technologyLookup.setRequired(true);
    }
}
