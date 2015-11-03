package com.qcadoo.mes.materialFlowResources;

import com.qcadoo.mes.basic.ProductVO;

import java.util.List;
import java.util.Map;

public interface DocumentPositionRepository {

   List<Map<String, Object>> findAll(String sidx, String sord);

    void delete(Long id);

    void create(DocumentPositionVO documentPositionVO);

    void update(Long id, DocumentPositionVO documentPositionVO);

    List<Map<String,String>> getTypes();
}
