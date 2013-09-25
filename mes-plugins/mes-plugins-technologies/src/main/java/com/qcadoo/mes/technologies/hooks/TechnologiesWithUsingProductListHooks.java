package com.qcadoo.mes.technologies.hooks;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.OperationProductInComponentFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchQueryBuilder;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;

@Service
public class TechnologiesWithUsingProductListHooks {

    private static final String L_PRODUCT = "product";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    private static final String L_GRID = "grid";

    private static final String L_FORM = "form";

    private static final String L_VIEW_DEFINITION_STATE_IS_NULL = "viewDefinitionState is null";

    public void fillTechnologiesWithUsingProduct(final ViewDefinitionState viewDefinitionState) {
        checkArgument(viewDefinitionState != null, L_VIEW_DEFINITION_STATE_IS_NULL);

        GridComponent grid = (GridComponent) viewDefinitionState.getComponentByReference(L_GRID);
        FormComponent product = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);

        if (product.getEntityId() == null) {
            return;
        }

        List<Entity> inputProducts = getTechnologyWithProductInComponent(product.getEntity());

        grid.setEntities(inputProducts);
    }

    private List<Entity> getTechnologyWithProductInComponent(final Entity product) {
        String query = String.format(
                "SELECT operationProductInComponent FROM #technologies_operationProductInComponent AS operationProductInComponent "
                        + "WHERE operationProductInComponent.%s.id = :product", OperationProductInComponentFields.PRODUCT);

        SearchQueryBuilder searchQueryBuilder = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).find(query);
        searchQueryBuilder.setEntity("product", product);

        return searchQueryBuilder.list().getEntities();
    }

    public void fillProductName(final ViewDefinitionState viewDefinitionState) {
        FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference(L_PRODUCT);
        FormComponent form = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        if (form.getEntityId() == null) {
            return;
        }
        Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT).get(
                form.getEntityId());
        if (product == null) {
            return;
        }
        productField.setFieldValue(String.format("%s - %s", product.getStringField(ProductFields.NUMBER),
                product.getStringField(ProductFields.NAME)));
        productField.requestComponentUpdateState();
    }
}
