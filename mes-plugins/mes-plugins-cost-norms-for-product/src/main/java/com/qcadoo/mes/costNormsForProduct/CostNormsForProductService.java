package com.qcadoo.mes.costNormsForProduct;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchDisjunction;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchResult;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class CostNormsForProductService {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    /* ****** VIEW HOOKS ******* */

    public void fillCostTabUnit(final ViewDefinitionState viewDefinitionState) {
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference("form");
        FieldComponent costUnit = (FieldComponent) viewDefinitionState.getComponentByReference("costTabUnit");
        if (form == null || costUnit == null) {
            return;
        }
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                form.getEntityId());

        if (product == null) {
            return;
        }

        costUnit.setFieldValue(product.getStringField("unit"));
        costUnit.requestComponentUpdateState();
        costUnit.setEnabled(false);
    }

    public void fillCostTabCurrency(final ViewDefinitionState viewDefinitionState) {
        for (String componentReference : Arrays.asList("nominalCostCurrency", "lastPurchaseCostCurrency", "averageCostCurrency")) {
            FieldComponent field = (FieldComponent) viewDefinitionState.getComponentByReference(componentReference);
            field.setEnabled(true);
            // temporary
            field.setFieldValue("PLN");
            field.setEnabled(false);
            field.requestComponentUpdateState();
        }
    }

    public void fillInProductsGrid(final ViewDefinitionState viewDefinitionState) {
        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference("inProductsGrid");
        Long technologyId = ((FormComponent) viewDefinitionState.getComponentByReference("form")).getEntityId();
        if (technologyId == null || grid == null) {
            return;
        }
        Entity technology = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY).get(technologyId);
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);

        SearchDisjunction disjunction = SearchRestrictions.disjunction();
        for (Entity operationComponent : technology.getTreeField("operationComponents")) {
            disjunction.add(SearchRestrictions.belongsTo("operationComponent", operationComponent));
        }

        SearchResult searchResult = technologyDD.find().add(disjunction).createAlias("product", "product")
                .addOrder(SearchOrders.asc("product.name")).list();
        grid.setEntities(searchResult.getEntities());
    }

    /* ****** CUSTOM EVENT LISTENER ****** */

    /* ****** VALIDATORS ****** */

}
