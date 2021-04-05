package com.qcadoo.mes.productFlowThruDivision.criteriaModifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productFlowThruDivision.constants.ModelCardProductFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchProjections;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.model.api.search.SearchSubqueries;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

@Service
public class ProductCriteriaModifiersPFTD {

    public static final String L_MODEL_CARD_ID = "modelCardId";

    public static final String L_PRODUCT_ID = "productId";

    private static final String L_THIS_ID = "this.id";

    private static final String L_DOT = ".";

    private static final String L_ID = "id";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void showNotAssignedProducts(final SearchCriteriaBuilder searchCriteriaBuilder, final FilterValueHolder filterValue) {
        if (filterValue.has(L_MODEL_CARD_ID)) {
            long modelCardId = filterValue.getLong(L_MODEL_CARD_ID);

            SearchCriteriaBuilder subCriteria = dataDefinitionService
                    .get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                            ProductFlowThruDivisionConstants.MODEL_MODEL_CARD_PRODUCT)
                    .findWithAlias(ProductFlowThruDivisionConstants.MODEL_MODEL_CARD_PRODUCT)
                    .createAlias(ModelCardProductFields.PRODUCT, ModelCardProductFields.PRODUCT, JoinType.INNER)
                    .add(SearchRestrictions.eqField(ModelCardProductFields.PRODUCT + L_DOT + L_ID, L_THIS_ID))
                    .add(SearchRestrictions.belongsTo(ModelCardProductFields.MODEL_CARD,
                            ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER, ModelCardProductFields.MODEL_CARD, modelCardId))
                    .setProjection(SearchProjections.id());
            if (filterValue.has(L_PRODUCT_ID)) {
                searchCriteriaBuilder.add(SearchRestrictions.or(SearchSubqueries.notExists(subCriteria),
                        SearchRestrictions.idEq(filterValue.getLong(L_PRODUCT_ID))));
            } else {
                searchCriteriaBuilder.add(SearchSubqueries.notExists(subCriteria));
            }
        }

    }
}
