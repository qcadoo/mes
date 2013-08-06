package com.qcadoo.mes.technologies.hooks;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        String QUERY = String.format(" select prodin from #technologies_operationProductInComponent as prodin "
                + "where prodin.%s.id = :product", OperationProductInComponentFields.PRODUCT);

        SearchQueryBuilder sqb = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_OPERATION_PRODUCT_IN_COMPONENT).find(QUERY);
        sqb.setEntity(OperationProductInComponentFields.PRODUCT, product);
        return sqb.list().getEntities();
    }

    public void fillProductName(final ViewDefinitionState viewDefinitionState) {
        FormComponent productInComponent = (FormComponent) viewDefinitionState.getComponentByReference(L_FORM);
        FieldComponent productField = (FieldComponent) viewDefinitionState.getComponentByReference(L_PRODUCT);
        Long productInComponentId = productInComponent.getEntityId();
        if (productInComponentId == null) {
            productField.setFieldValue(null);
        } else {
            Entity productEntity = productInComponent.getEntity().getDataDefinition().get(productInComponentId)
                    .getBelongsToField(OperationProductInComponentFields.PRODUCT);
            productField.setFieldValue(String.format("%s - %s", productEntity.getStringField(ProductFields.NUMBER),
                    productEntity.getStringField(ProductFields.NAME)));
        }
        productField.requestComponentUpdateState();
    }
}
