package com.qcadoo.mes.materialFlowResources.print.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceDataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<ResourceDto> findResourcesByLocation(final Long locationId) {
        return jdbcTemplate.query(buildQuery(), new MapSqlParameterSource("location", locationId),
                BeanPropertyRowMapper.newInstance(ResourceDto.class));
    }

    private String buildQuery() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT resource.id, resource.number, ");
        query.append("resource.quantity, resource.price, ");
        query.append("resource.productiondate, resource.expirationdate, ");
        query.append("resource.quantityinadditionalunit,resource.conversion, ");
        query.append("resource.givenunit, ");
        query.append("resource.availablequantity, ");
        query.append("resource.reservedquantity, ");
        query.append("storagelocation.id AS storagelocationid, ");
        query.append("storagelocation.number AS storagelocationnumber, ");
        query.append("additionalcode.id AS additionalcodeid, ");
        query.append("additionalcode.code AS additionalcodecode, ");
        query.append("product.id AS productid, ");
        query.append("product.number AS productnumber, ");
        query.append("product.name AS productname, ");
        query.append("palletnumber.id AS palletnumberid, ");
        query.append("palletnumber.number AS palletnumbernumber ");
        query.append("FROM materialflowresources_resource resource ");
        query.append("LEFT JOIN materialflowresources_storagelocation storagelocation ON storagelocation.id = resource.storagelocation_id ");
        query.append("LEFT JOIN basic_additionalcode additionalcode ON additionalcode.id = resource.additionalcode_id ");
        query.append("LEFT JOIN basic_product product ON product.id = resource.product_id ");
        query.append("LEFT JOIN basic_palletnumber palletnumber ON palletnumber.id = resource.palletnumber_id ");
        query.append("WHERE resource.location_id = :location");
        return query.toString();
    }
}
