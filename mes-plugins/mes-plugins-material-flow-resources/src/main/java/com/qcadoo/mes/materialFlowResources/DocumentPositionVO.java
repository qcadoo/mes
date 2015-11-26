package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class DocumentPositionVO {

    private Long id;
    private Long product_id;
    private Long additional_code_id;
    private BigDecimal quantity;
    private BigDecimal givenquantity;
    private String givenunit;
    private BigDecimal conversion;
    private Date expirationdate;
    private Long pallet_id;
    private String type_of_pallet;
    private Long storage_location_id;
    private Long resource_id;
}
