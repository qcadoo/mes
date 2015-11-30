package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class DocumentPositionVO {

    private Long id;
    private String product;
    private String additional_code;
    private BigDecimal quantity;
    private BigDecimal givenquantity;
    private String givenunit;
    private BigDecimal conversion;
    private Date expirationdate;
    private String pallet;
    private String type_of_pallet;
//    private Long storage_location_id;
//    private Long resource_id;
}
