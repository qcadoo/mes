package com.qcadoo.mes.materialFlowResources.mappers;

import com.qcadoo.mes.materialFlowResources.DocumentPositionVO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DocumentPositionMapper implements RowMapper {

    @Override
    public DocumentPositionVO mapRow(ResultSet resultSet, int i) throws SQLException {
        DocumentPositionVO positionVO = new DocumentPositionVO();

        positionVO.setId(resultSet.getLong("id"));
        positionVO.setProduct_id(resultSet.getLong("product_id"));
        // nowe pole
        positionVO.setAdditional_code_id(resultSet.getLong("additionalcode_id"));
        positionVO.setQuantity(resultSet.getBigDecimal("quantity"));
        positionVO.setGivenquantity(resultSet.getBigDecimal("givenquantity"));
        positionVO.setGivenunit(resultSet.getString("givenunit"));
        // nowe pole
        positionVO.setConversion(resultSet.getBigDecimal("conversion"));
        positionVO.setExpirationdate(resultSet.getDate("expirationdate"));
        // nowe pole
        positionVO.setPallet_id(resultSet.getLong("palletnumber_id"));
        // nowe pole
        positionVO.setType_of_pallet(resultSet.getString("typeofpallet"));
        positionVO.setStorage_location_id(resultSet.getLong("storagelocation_id"));

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

    public Map<String, Object> mapVoToParams(DocumentPositionVO vo) {
        Map<String, Object> params = new HashMap<>();

        params.put("id", vo.getId());
        params.put("product_id", vo.getProduct_id());
        params.put("additionalcode_id", vo.getAdditional_code_id());
        params.put("quantity", vo.getQuantity());
        params.put("givenquantity", vo.getGivenquantity());
        params.put("givenunit", vo.getGivenunit());
        params.put("conversion", vo.getConversion());
        params.put("expirationdate", vo.getExpirationdate());
        params.put("palletnumber_id", vo.getPallet_id());
        params.put("typeofpallet", vo.getType_of_pallet());
        params.put("storagelocation_id", vo.getStorage_location_id());

        return params;
    }
}
