package com.qcadoo.mes.materialFlowResources;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class DocumentPositionVO {

    private Long id;

    private BigDecimal quantity;

    private BigDecimal givenquantity;

    private Date expirationdate;

    private String type;

    private Long product_id;
}
