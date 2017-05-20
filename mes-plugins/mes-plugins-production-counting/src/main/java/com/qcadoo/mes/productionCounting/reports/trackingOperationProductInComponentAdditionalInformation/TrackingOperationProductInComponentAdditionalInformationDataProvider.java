package com.qcadoo.mes.productionCounting.reports.trackingOperationProductInComponentAdditionalInformation;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;

@Repository
final class TrackingOperationProductInComponentAdditionalInformationDataProvider {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    List<TrackingOperationProductInComponentAdditionalInformationReportDto> getAdditionalInformationReportData(Date fromDate,
            Date toDate) {

        String query = "SELECT" +
                "  productiontracking.number AS productiontrackingnumber," +
                "  ordersorder.number        AS ordernumber," +
                "  product.number            AS productnumber," +
                "  product.name              AS productname," +
                "  trackingoperationproductincomponent.additionalinformation " +
                "FROM productioncounting_productiontracking productiontracking" +
                "  JOIN orders_order ordersorder" +
                "    ON (ordersorder.id = productiontracking.order_id)" +
                "  JOIN productioncounting_trackingoperationproductincomponent trackingoperationproductincomponent" +
                "    ON (trackingoperationproductincomponent.productiontracking_id = productiontracking.id)" +
                "  JOIN basic_product product" +
                "    ON (product.id = trackingoperationproductincomponent.product_id)" +
                "WHERE trackingoperationproductincomponent.additionalinformation <> ''" +
                "      AND productiontracking.createdate BETWEEN :fromDate AND (:toDate::TIMESTAMP + '1 DAY'::INTERVAL) " +
                "ORDER BY productiontracking.id";

        return jdbcTemplate.query(query, ImmutableMap.of("fromDate", fromDate, "toDate", toDate),
                BeanPropertyRowMapper.newInstance(TrackingOperationProductInComponentAdditionalInformationReportDto.class));
    }

}