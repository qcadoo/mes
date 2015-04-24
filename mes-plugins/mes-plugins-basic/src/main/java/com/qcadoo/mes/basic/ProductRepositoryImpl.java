package com.qcadoo.mes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;

/**
 * Created by kamilsiwonia on 23.04.2015.
 */
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> findAll() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM basic_product");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(query.toString(), Collections.EMPTY_MAP);
        return list;

    }

    @Override
    public void delete(Long id) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("DELETE FROM basic_product WHERE id = :id ");
        jdbcTemplate.update(queryBuilder.toString(), Collections.singletonMap("id", id));
    }

    @Override public void create(ProductVO productVO) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("number", productVO.getNumber());
        params.put("name", productVO.getName());
        params.put("ean", productVO.getEan());
        params.put("active", productVO.isActive());

        StringBuilder queryBuilder = new StringBuilder("INSERT INTO basic_product( ");
        queryBuilder.append("number, name, ean, active) ");
        queryBuilder.append("VALUES (:number, :name, :ean, :active) RETURNING id ");
        jdbcTemplate.queryForObject(queryBuilder.toString(), params, Long.class);
    }

    @Override public void update(Long id, ProductVO productVO) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("number", productVO.getNumber());
        params.put("id", productVO.getId());
        params.put("name", productVO.getName());
        params.put("ean", productVO.getEan());
        params.put("active", productVO.isActive());
        jdbcTemplate
                .update("UPDATE basic_product SET number = :number, name = :name, ean = :ean, active = :active WHERE id = :id ",
                        params);
    }

}
