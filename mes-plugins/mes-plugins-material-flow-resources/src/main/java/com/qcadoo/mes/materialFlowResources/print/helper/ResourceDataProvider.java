package com.qcadoo.mes.materialFlowResources.print.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResourceDataProvider {

    private static final String WASTE_MODE_YES = "02yes";

    private static final String WASTE_MODE_NO = "03no";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<ResourceDto> findResourcesByLocation(final Long locationId, List<Long> storageLocationIds, String category,
            String wasteMode, boolean appendOrderBy) {
        Map<String, Object> queryParameters = new HashMap<String, Object>();
        queryParameters.put("location", locationId);
        if (!storageLocationIds.isEmpty()) {
            queryParameters.put("ids", storageLocationIds);
        }
        if (StringUtils.isNoneBlank(category)) {
            queryParameters.put("category", category);
        }
        if (WASTE_MODE_NO.equals(wasteMode)) {
            queryParameters.put("wasteMode", false);

        } else if (WASTE_MODE_YES.equals(wasteMode)) {
            queryParameters.put("wasteMode", true);
        }
        return jdbcTemplate.query(buildQuery(!storageLocationIds.isEmpty(), category, wasteMode, appendOrderBy), queryParameters,
                BeanPropertyRowMapper.newInstance(ResourceDto.class));
    }

    private String buildQuery(boolean appendStorageLocationIds, String category, String wasteMode, boolean appendOrderBy) {
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
        query.append("WHERE resource.location_id = :location AND resource.storagelocation_id IS NOT NULL");
        if (appendStorageLocationIds) {
            query.append(" AND storagelocation.id IN (:ids)");
        }
        if (StringUtils.isNoneBlank(category)) {
            query.append(" AND product.category = :category");
        }
        if (WASTE_MODE_NO.equals(wasteMode) || WASTE_MODE_YES.equals(wasteMode)) {
            query.append(" AND resource.waste = :wasteMode");
        }
        if (appendOrderBy) {
            query.append(" ORDER BY storagelocation.number, palletnumber.number");

        }
        return query.toString();
    }
}
