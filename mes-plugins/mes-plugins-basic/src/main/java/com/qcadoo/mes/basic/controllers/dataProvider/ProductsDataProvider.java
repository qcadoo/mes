package com.qcadoo.mes.basic.controllers.dataProvider;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsDataProvider {

    private static final String QUERY_PRODUCTS = "SELECT product.id as id, product.number as number, product.name as name "
                                                    + "FROM basic_product product WHERE product.active = true;";

    @Autowired private NamedParameterJdbcTemplate jdbcTemplate;

    public List<ProductDTO> getProducts(){
        List<ProductDTO> products = Lists.newArrayList();
        products = jdbcTemplate.query(QUERY_PRODUCTS, new BeanPropertyRowMapper(ProductDTO.class));
        return products;
    }
}
