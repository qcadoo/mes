package com.qcadoo.mes.productFlowThruDivision.warehouseIssue;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.productFlowThruDivision.constants.ProductFlowThruDivisionConstants;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.ProductsToIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.constans.WarehouseIssueFields;
import com.qcadoo.mes.productFlowThruDivision.warehouseIssue.states.constants.WarehouseIssueState;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchRestrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsToIssueService {

    private static final String WAREHOUSE_ISSUE_ALIAS = "wi";

    private static final String PRODUCT_ALIAS = "p";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Entity> getProductsToIssue() {
        return getProductToIssueDD()
                .find()
                .createAlias(ProductsToIssueFields.PRODUCT, PRODUCT_ALIAS, JoinType.LEFT)
                .createAlias(ProductsToIssueFields.WAREHOUSE_ISSUE, WAREHOUSE_ISSUE_ALIAS, JoinType.LEFT)
                .add(SearchRestrictions.in(
                        getStateField(),
                        Lists.newArrayList(WarehouseIssueState.DRAFT.getStringValue(),
                                WarehouseIssueState.IN_PROGRESS.getStringValue())))
                .add(SearchRestrictions.isNotNull(getExternalNumberField())).list().getEntities();
    }

    private String getStateField() {
        return WAREHOUSE_ISSUE_ALIAS + "." + WarehouseIssueFields.STATE;
    }

    private String getExternalNumberField() {
        return PRODUCT_ALIAS + "." + ProductFields.EXTERNAL_NUMBER;
    }

    private DataDefinition getProductToIssueDD() {
        return dataDefinitionService.get(ProductFlowThruDivisionConstants.PLUGIN_IDENTIFIER,
                ProductFlowThruDivisionConstants.MODEL_PRODUCTS_TO_ISSUE);
    }

}
