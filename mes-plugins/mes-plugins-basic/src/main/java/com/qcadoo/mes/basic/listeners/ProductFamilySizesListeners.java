package com.qcadoo.mes.basic.listeners;

import com.google.common.collect.Maps;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.*;
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

    private static final String L_NOMINAL_COST = "nominalCost";

    private static final String L_NOMINAL_COST_CURRENCY = "nominalCostCurrency";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private ParameterService parameterService;

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

            DataDefinition productDataDefinition = getProductDD();
            DataDefinition sizeDataDefinition = getSizeDD();

            Entity productFamily = productDataDefinition.get(productId);

            int errors = 0;

            for (Long sizeId : grid.getSelectedEntitiesIds()) {
                Entity product = productDataDefinition.create();
                Entity size = sizeDataDefinition.get(sizeId);

                product.setField(ProductFields.PARENT, productFamily);
                product.setField(ProductFields.SIZE, size);
                product.setField(ProductFields.ASSORTMENT, productFamily.getBelongsToField(ProductFields.ASSORTMENT));
                product.setField(ProductFields.MODEL, productFamily.getBelongsToField(ProductFields.MODEL));
                product.setField(ProductFields.CATEGORY, productFamily.getStringField(ProductFields.CATEGORY));
                product.setField(ProductFields.ENTITY_TYPE, ProductFamilyElementType.PARTICULAR_PRODUCT.getStringValue());
                product.setField(ProductFields.GLOBAL_TYPE_OF_MATERIAL,
                        productFamily.getStringField(ProductFields.GLOBAL_TYPE_OF_MATERIAL));
                product.setField(ProductFields.UNIT, productFamily.getStringField(ProductFields.UNIT));
                product.setField(ProductFields.NAME, productFamily.getStringField(ProductFields.NAME));
                product.setField(ProductFields.NUMBER,
                        productFamily.getStringField(ProductFields.NUMBER) + "-" + size.getStringField(SizeFields.NUMBER));

                if (parameterService.getParameter().getBooleanField(ParameterFields.COPY_NOMINAL_COST_FAMILY_OF_PRODUCTS_SIZES)) {
                    product.setField(L_NOMINAL_COST, productFamily.getDecimalField(L_NOMINAL_COST));
                    product.setField(L_NOMINAL_COST_CURRENCY, productFamily.getBelongsToField(L_NOMINAL_COST_CURRENCY));
                }

                product = product.getDataDefinition().save(product);

                if (product.isValid()) {
                    if (parameterService.getParameter().getBooleanField(ParameterFields.COPY_ATTRIBUTES_TO_SIZE_PRODUCTS)) {
                        DataDefinition attributeValueDD = getAttributeValueDD();

                        for (Entity productFamilyAttributeValue : productFamily
                                .getHasManyField(ProductFields.PRODUCT_ATTRIBUTE_VALUES)) {
                            Entity productAttributeValue = attributeValueDD.create();

                            productAttributeValue.setField(ProductAttributeValueFields.PRODUCT, product.getId());
                            productAttributeValue.setField(ProductAttributeValueFields.VALUE,
                                    productFamilyAttributeValue.getStringField(ProductAttributeValueFields.VALUE));
                            productAttributeValue.setField(ProductAttributeValueFields.ATTRIBUTE,
                                    productFamilyAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE));
                            productAttributeValue.setField(ProductAttributeValueFields.ATTRIBUTE_VALUE,
                                    productFamilyAttributeValue.getBelongsToField(ProductAttributeValueFields.ATTRIBUTE_VALUE));

                            productAttributeValue.getDataDefinition().save(productAttributeValue);
                        }
                    }
                } else {
                    errors = errors + 1;

                    view.addMessage("basic.addProductFamilySizes.generateProducts.failure", ComponentState.MessageType.FAILURE,
                            product.getStringField(ProductFields.NUMBER));
                }
            }

            int entitiesWithoutErrors = grid.getSelectedEntitiesIds().size() - errors;

            if (entitiesWithoutErrors > 0) {
                view.addMessage("basic.addProductFamilySizes.generateProducts.success", ComponentState.MessageType.SUCCESS,
                        "" + (entitiesWithoutErrors));
            }
        }
    }

    private DataDefinition getProductDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_PRODUCT);
    }

    private DataDefinition getSizeDD() {
        return dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER,
                BasicConstants.MODEL_SIZE);
    }

    private DataDefinition getAttributeValueDD() {
        return dataDefinitionService
                .get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT_ATTRIBUTE_VALUE);
    }

}
