package com.qcadoo.mes.basic.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFamilyElementType;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.basic.constants.SizeFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FormComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductFamilySizesListeners {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void addSizes(final ViewDefinitionState view, final ComponentState state, final String[] args) {
        FormComponent productForm = (FormComponent) view.getComponentByReference(QcadooViewConstants.L_FORM);
        String url = "/basic/addProductFamilySizes.html";
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("form.productId", productForm.getEntityId());
        view.openModal(url, parameters);
    }

    public final void generateProducts(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        JSONObject obj = view.getJsonContext();
        if (obj.has("window.mainTab.product.productId")) {
            GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
            Long productId = obj.getLong("window.mainTab.product.productId");
            DataDefinition productDataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_PRODUCT);
            DataDefinition sizeDataDefinition = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                    BasicConstants.MODEL_SIZE);
            Entity productFamily = productDataDefinition.get(productId);
            for (Long sizeId : grid.getSelectedEntitiesIds()) {
                Entity product = productDataDefinition.create();
                product.setField(ProductFields.PARENT, productFamily);
                Entity size = sizeDataDefinition.get(sizeId);
                product.setField(ProductFields.SIZE, size);
                product.setField(ProductFields.ASSORTMENT, productFamily.getBelongsToField(ProductFields.ASSORTMENT));
                product.setField(ProductFields.MODEL, productFamily.getBelongsToField(ProductFields.MODEL));
                product.setField(ProductFields.CATEGORY, productFamily.getStringField(ProductFields.CATEGORY));
                product.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
                product.setField(ProductFields.GLOBAL_TYPE_OF_MATERIAL,
                        productFamily.getStringField(ProductFields.GLOBAL_TYPE_OF_MATERIAL));
                product.setField(ProductFields.NAME, productFamily.getStringField(ProductFields.NAME));
                product.setField(ProductFields.NUMBER,
                        productFamily.getStringField(ProductFields.NUMBER) + "-" + size.getStringField(SizeFields.NUMBER));
                product.getDataDefinition().save(product);
            }
            view.addMessage("basic.addProductFamilySizes.generateProducts.success", ComponentState.MessageType.SUCCESS,
                    grid.getSelectedEntitiesIds().size() + "");
        }
    }
}
