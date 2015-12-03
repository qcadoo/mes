package com.qcadoo.mes.materialFlowResources.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.qcadoo.mes.materialFlowResources.DocumentPositionDTO;


public class DocumentPositionMapper implements RowMapper {

    @Override
    public DocumentPositionDTO mapRow(ResultSet resultSet, int i) throws SQLException {
        DocumentPositionDTO positionVO = new DocumentPositionDTO();

        positionVO.setId(resultSet.getLong("id"));
        positionVO.setProduct(resultSet.getString("product_number"));
        positionVO.setUnit(resultSet.getString("product_unit"));
        // nowe pole
        positionVO.setAdditional_code(resultSet.getString("additionalcode_code"));
        positionVO.setQuantity(resultSet.getBigDecimal("quantity"));
        positionVO.setGivenquantity(resultSet.getBigDecimal("givenquantity"));
        positionVO.setGivenunit(resultSet.getString("givenunit"));
        // nowe pole
        positionVO.setConversion(resultSet.getBigDecimal("conversion"));
        positionVO.setExpirationdate(resultSet.getDate("expirationdate"));
        // nowe pole
        positionVO.setPallet(resultSet.getString("palletnumber_number"));
        // nowe pole
        positionVO.setType_of_pallet(resultSet.getString("typeofpallet"));
        positionVO.setStorage_location(resultSet.getString("storagelocation_number"));
        positionVO.setDocument(resultSet.getLong("document_id"));

        
        
//        <belongsTo name="document" model="document"/>
//        <decimal name="price" default="0">
//            <validatesRange from="0"/>
//        </decimal>
//        <string name="batch"/>
//        <string name="unit" persistent="false"/>
//
//        <date name="productionDate"/>
//        <priority name="number" scope="document"/>
//        <hasMany name="attributeValues" joinField="position" model="attributeValue" cascade="delete"/>
//
//
//        <enum name="type" values="01receipt,02internalInbound,03internalOutbound,04release,05transfer"/>
//        <enum name="state" values="01draft,02accepted" default="01draft"/>    
        return positionVO;
    }
}
