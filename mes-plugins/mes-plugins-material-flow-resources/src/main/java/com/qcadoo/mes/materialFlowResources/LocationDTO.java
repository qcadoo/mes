package com.qcadoo.mes.materialFlowResources;

import lombok.Data;

@Data
public class LocationDTO {

    private Long id;
    private boolean requirePrice;
    private boolean requirebatch;
    private boolean requirEproductionDate;
    private boolean requirEexpirationDate;
    private String algorithm;
}
