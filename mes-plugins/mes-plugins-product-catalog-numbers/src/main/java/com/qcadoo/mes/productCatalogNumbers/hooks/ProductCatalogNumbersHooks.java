package com.qcadoo.mes.productCatalogNumbers.hooks;

import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.CATALOG_NUMBER;
import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.COMPANY;
import static com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumberFields.PRODUCT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.productCatalogNumbers.constants.ProductCatalogNumbersConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;

@Service
public class ProductCatalogNumbersHooks {

    @Autowired
    private DataDefinitionService dataDefinitionService;

    public boolean checkIfExistsCatalogNumberWithProductAndCompany(final DataDefinition dataDefinition, final Entity entity) {
        SearchCriteriaBuilder criteria = dataDefinitionService
                .get(ProductCatalogNumbersConstants.PLUGIN_IDENTIFIER,
                        ProductCatalogNumbersConstants.MODEL_PRODUCT_CATALOG_NUMBERS).find()
                .add(SearchRestrictions.belongsTo(PRODUCT, entity.getBelongsToField(PRODUCT)))
                .add(SearchRestrictions.belongsTo(COMPANY, entity.getBelongsToField(COMPANY)));
        if (entity.getId() != null) {
            criteria.add(SearchRestrictions.ne("id", entity.getId()));
        }
        List<Entity> catalogsNumbers = criteria.list().getEntities();
        if (catalogsNumbers.isEmpty()) {
            return true;
        } else {
            entity.addGlobalError("productCatalogNumbers.productCatalogNumber.validationError.alreadyExistsProductForCompany");
            return false;
        }
    }

    public boolean checkIfExistsCatalogNumberWithNumberAndCompany(final DataDefinition dataDefinition, final Entity entity) {
        SearchCriteriaBuilder criteria = dataDefinitionService
                .get(ProductCatalogNumbersConstants.PLUGIN_IDENTIFIER,
                        ProductCatalogNumbersConstants.MODEL_PRODUCT_CATALOG_NUMBERS).find()
                .add(SearchRestrictions.eq(CATALOG_NUMBER, entity.getStringField(CATALOG_NUMBER)))
                .add(SearchRestrictions.belongsTo(COMPANY, entity.getBelongsToField(COMPANY)));
        if (entity.getId() != null) {
            criteria.add(SearchRestrictions.ne("id", entity.getId()));
        }
        List<Entity> catalogsNumbers = criteria.list().getEntities();
        if (catalogsNumbers.isEmpty()) {
            return true;
        } else {
            entity.addGlobalError("productCatalogNumbers.productCatalogNumber.validationError.alreadyExistsCatalogNumerForCompany");
            return false;
        }
    }
}
