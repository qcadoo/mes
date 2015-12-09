package com.qcadoo.mes.materialFlowResources;

import lombok.Data;

@Data
public class DocumentDTO {

    private Long id;
    private String state;
    private String type;
    private Long locationTo_id;
    private Long locationFrom_id;
}
