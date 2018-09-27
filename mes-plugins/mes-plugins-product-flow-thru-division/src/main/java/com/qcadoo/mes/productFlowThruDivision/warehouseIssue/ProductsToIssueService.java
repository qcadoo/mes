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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Transactional
    public void fillStorageLocations() {
        StringBuilder query = geBaseUpdateStorageLocationsQuery();

        Map<String, Object> parameters = new HashMap<String, Object>();
        jdbcTemplate.update(query.toString(), parameters);
    }

    private StringBuilder geBaseUpdateStorageLocationsQuery() {
        StringBuilder query = new StringBuilder();
        query.append("UPDATE productflowthrudivision_productstoissue ptis ");
        query.append("SET storagelocation_id= (SELECT sl.id FROM materialflowresources_storagelocation sl WHERE sl.location_id =ptis.location_id AND sl.product_id=ptis.product_id AND active = true LIMIT 1) ");
        query.append("FROM productflowthrudivision_warehouseissue issue ");
        query.append("WHERE ptis.warehouseissue_id = issue.id ");
        query.append("AND issue.state::text = ANY (ARRAY['01draft'::character varying::text, '02inProgress'::character varying::text]) ");
        return query;
    }

    @Transactional
    public void fillStorageLocations(List<Long> ids) {
        StringBuilder query = geBaseUpdateStorageLocationsQuery();
        query.append("AND ptis.id IN (:ids)");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ids", ids);
        jdbcTemplate.update(query.toString(), parameters);
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
