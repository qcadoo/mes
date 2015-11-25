package com.qcadoo.mes.materialFlowResources.mappers;

import com.qcadoo.mes.materialFlowResources.DocumentPositionVO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DocumentPositionMapper implements RowMapper {

    @Override public DocumentPositionVO mapRow(ResultSet resultSet, int i) throws SQLException {
        DocumentPositionVO positionVO = new DocumentPositionVO();
        
        positionVO.setId(resultSet.getLong("id"));
        positionVO.setProduct_id(resultSet.getLong("product_id"));
        
        return positionVO;
    }
}
