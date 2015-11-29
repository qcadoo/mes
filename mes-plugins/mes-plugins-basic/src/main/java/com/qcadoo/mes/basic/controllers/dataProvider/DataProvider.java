package com.qcadoo.mes.basic.controllers.dataProvider;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.AdditionalCodeDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.PalletNumberDTO;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataProvider {

    @Autowired private NamedParameterJdbcTemplate jdbcTemplate;

    public List<ProductDTO> getProductsByQuery(String query) {
        String _query = "SELECT product.id as id, product.number as code, product.number as number, product.name as name "
                + "FROM basic_product product WHERE product.active = true and product.number ilike '%"+query+"%' LIMIT 15;";
        List<ProductDTO> products = Lists.newArrayList();
        products = jdbcTemplate.query(_query, new BeanPropertyRowMapper(ProductDTO.class));
        return products;
    }

    public List<AdditionalCodeDTO> getAdditionalCodesByQuery(String query) {
        String _query = "SELECT additionalcode.id as id, additionalcode.code as code "
                + "FROM basic_additionalcode additionalcode WHERE additionalcode.code ilike '%"+query+"%' LIMIT 15;";
        List<AdditionalCodeDTO> codes = Lists.newArrayList();
        codes = jdbcTemplate.query(_query, new BeanPropertyRowMapper(AdditionalCodeDTO.class));
        return codes;
    }

    public List<PalletNumberDTO> getPalletNumbersByQuery(String query) {
        String _query = "SELECT palletnumber.id as id, palletnumber.number as code, palletnumber.number as number "
                + "FROM basic_palletnumber palletnumber WHERE palletnumber.active = true and palletnumber.number ilike '%"+query+"%' LIMIT 15;";
        List<PalletNumberDTO> pallets = Lists.newArrayList();
        pallets = jdbcTemplate.query(_query, new BeanPropertyRowMapper(PalletNumberDTO.class));
        return pallets;
    }

}
