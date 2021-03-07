package com.qcadoo.mes.basic.criteriaModifiers;

import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductFamilySizesCriteriaModifiers {

    public static final String L_PRODUCT_ID = "productId";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public void filter(final SearchCriteriaBuilder scb, final FilterValueHolder filter) {
        if (filter.has(L_PRODUCT_ID)) {
            Long productId = filter.getLong(L_PRODUCT_ID);
            Entity product = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)
                    .get(productId);
            scb.add(SearchRestrictions.belongsTo(ProductFields.PARENT, product));
            scb.add(SearchRestrictions.isNotNull(ProductFields.SIZE));
        }
    }

    public void addFilter(final SearchCriteriaBuilder scb, final FilterValueHolder filter) {
        if (filter.has(L_PRODUCT_ID)) {
            Long productId = filter.getLong(L_PRODUCT_ID);
            Entity productFamily = dataDefinitionService.get(BasicConstants.PLUGIN_IDENTIFIER, BasicConstants.MODEL_PRODUCT)
                    .get(productId);

            List<Entity> products = productFamily.getHasManyField(ProductFields.CHILDREN);

            for (Entity product : products) {
                Entity size = product.getBelongsToField(ProductFields.SIZE);
                if(size != null){
                    scb.add(SearchRestrictions.idNe(size.getId()));
                }
            }
        }
    }
}
