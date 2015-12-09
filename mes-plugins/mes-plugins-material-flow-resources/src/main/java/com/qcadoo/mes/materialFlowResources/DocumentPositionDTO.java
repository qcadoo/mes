package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class DocumentPositionDTO {

    private Long id;
    private Long document;
    private String product;
    private String additional_code;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal givenquantity;
    private String givenunit;
    private BigDecimal conversion;
    private Date expirationdate;
    private Date productiondate;
    private String pallet;
    private String type_of_pallet;
    private String storage_location;
    private BigDecimal price;
    private String batch;
    // TODO
    private Long resource;
}
