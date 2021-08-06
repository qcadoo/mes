package com.qcadoo.mes.basic.listeners;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.CheckBoxComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;

import java.util.Objects;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompanyDefaultProductsListeners {

    private static final String L_PRODUCT = "product";

    private static final String L_COMPANY = "company";

    private static final String L_IS_DEFAULT = "isDefault";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public final void addMultipleDefaultProducts(final ViewDefinitionState view, final ComponentState state, final String[] args)
            throws JSONException {
        JSONObject obj = view.getJsonContext();
        if (obj.has("window.mainTab.product.companyId")) {
            CheckBoxComponent generated = (CheckBoxComponent) view.getComponentByReference("generated");

            GridComponent grid = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);
            Long companyId = obj.getLong("window.mainTab.product.companyId");
            try {
                tryCreatePositions(grid, companyId);
                view.addMessage("basic.companyDefaultProducts.info.generationSuccess", ComponentState.MessageType.SUCCESS);
                generated.setChecked(true);
            } catch (EntityRuntimeException ere) {
                generated.setChecked(false);
                view.addMessage("basic.companyDefaultProducts.error.defaultExists", ComponentState.MessageType.FAILURE, ere
                        .getEntity().getBelongsToField(L_PRODUCT).getStringField(ProductFields.NUMBER));
            }
        }
    }

    @Transactional
    public void tryCreatePositions(GridComponent grid, Long companyId) {
        for (Long productId : grid.getSelectedEntitiesIds()) {
            Entity defaultProductCompany = getCompanyProductDD()
                    .find()
                    .add(SearchRestrictions.belongsTo(L_PRODUCT, BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT,
                            productId)).setMaxResults(1).uniqueResult();

            Entity productCompany = null;
            if (Objects.nonNull(defaultProductCompany)) {
                if (defaultProductCompany.getBelongsToField(L_COMPANY).getId().equals(companyId)) {
                    productCompany = defaultProductCompany;
                } else if (defaultProductCompany.getBooleanField(L_IS_DEFAULT)) {
                    throw new EntityRuntimeException(defaultProductCompany);
                } else {
                    productCompany = getCompanyProductDD().create();
                }

            } else {
                productCompany = getCompanyProductDD().create();
            }

            productCompany.setField(L_COMPANY, companyId);
            productCompany.setField(L_PRODUCT, productId);
            productCompany.setField(L_IS_DEFAULT, Boolean.TRUE);
            productCompany = productCompany.getDataDefinition().save(productCompany);
            if (!productCompany.isValid()) {
                throw new EntityRuntimeException(productCompany);
            }
        }
    }

    private DataDefinition getCompanyProductDD() {
        return dataDefinitionService.get("deliveries", "companyProduct");
    }

}
