package com.qcadoo.mes.basic;

import java.util.Map;

import java.util.List;

/**
 * Created by kamilsiwonia on 23.04.2015.
 */
public interface ProductRepository {

   List<Map<String, Object>> findAll(String sidx, String sord);

    void delete(Long id);

    void create(ProductVO productVO);

    void update(Long id, ProductVO productVO);
}
