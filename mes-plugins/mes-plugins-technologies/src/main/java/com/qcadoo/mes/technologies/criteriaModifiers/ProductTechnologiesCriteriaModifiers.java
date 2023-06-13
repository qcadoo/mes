package com.qcadoo.mes.technologies.criteriaModifiers;

import com.google.common.collect.Maps;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProductTechnologiesCriteriaModifiers {

    public static final String L_PRODUCT_ID = "productId";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void showForContextProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        if (filterValue.has(L_PRODUCT_ID)) {
            List<Long> technologiesIds = findTechnologiesIds(filterValue.getLong(L_PRODUCT_ID));
            if(!technologiesIds.isEmpty()) {
                scb.add(SearchRestrictions.in("id", technologiesIds));
            } else {
                scb.add(SearchRestrictions.idEq(-1L));
            }
        } else {
            scb.add(SearchRestrictions.idEq(-1L));
        }
    }

    private List<Long> findTechnologiesIds(Long productId) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("productId", productId);

        StringBuilder query = new StringBuilder();
        query.append("SELECT toc.technology_id FROM technologies_technologyoperationcomponent toc ");
        query.append("LEFT JOIN technologies_operationproductoutcomponent opoc ON opoc.operationcomponent_id = toc.id ");
        query.append("WHERE toc.parent_id is null AND opoc.waste = false AND opoc.product_id = :productId ");

        return jdbcTemplate.queryForList(query.toString(), parameters, Long.class);
    }
}