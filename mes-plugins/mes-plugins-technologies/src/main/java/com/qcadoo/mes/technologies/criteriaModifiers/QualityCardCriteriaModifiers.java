package com.qcadoo.mes.technologies.criteriaModifiers;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.basic.constants.QualityCardFields;
import com.qcadoo.model.api.search.JoinType;
import com.qcadoo.model.api.search.SearchCriteriaBuilder;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.view.api.components.lookup.FilterValueHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QualityCardCriteriaModifiers {

    public static final String L_PRODUCT_ID = "productId";
    public static final String L_PRODUCTS_LIST = "productsList";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void showQualityCardsForProduct(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        scb.add(SearchRestrictions.eq(QualityCardFields.STATE, "02accepted"));
        if (filterValue.has(L_PRODUCT_ID)) {
            scb.createAlias(QualityCardFields.PRODUCTS, QualityCardFields.PRODUCTS, JoinType.INNER)
                    .add(SearchRestrictions.eq(QualityCardFields.PRODUCTS + ".id", filterValue.getLong(L_PRODUCT_ID)));
        } else {
            scb.add(SearchRestrictions.idEq(-1L));
        }
    }

    public void showQualityCardsForProducts(final SearchCriteriaBuilder scb, final FilterValueHolder filterValue) {
        scb.add(SearchRestrictions.eq(QualityCardFields.STATE, "02accepted"));

        if(filterValue.has(L_PRODUCTS_LIST) && !Strings.isNullOrEmpty(filterValue.getString(L_PRODUCTS_LIST))) {
            String products = filterValue.getString(L_PRODUCTS_LIST);

            List<Long> qualityCardIds = findQualityCardsForProducts(products);
            if(!qualityCardIds.isEmpty()) {
                scb.add(SearchRestrictions.in("id", qualityCardIds));
            } else {
                scb.add(SearchRestrictions.idEq(-1L));
            }
        } else {
            scb.add(SearchRestrictions.idEq(-1L));
        }
    }

    private List<Long> findQualityCardsForProducts(String products) {
        Set<Long> ids = Arrays.stream(
                        products.replaceAll("[\\[\\]]", "").split(","))
                .map(Long::valueOf).collect(Collectors.toSet());
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("products", ids);

        StringBuilder query = new StringBuilder();
        query.append("SELECT qd.id ");
        query.append("FROM basic_qualitycard qd ");
        query.append("WHERE CAST(array(SELECT p.product_id FROM jointable_product_qualitycard p where p.qualitycard_id = qd.id) as bigint[]) @> CAST(ARRAY[ :products ] as bigint[]) ");

        return jdbcTemplate.queryForList(query.toString(), parameters, Long.class);
    }

}
