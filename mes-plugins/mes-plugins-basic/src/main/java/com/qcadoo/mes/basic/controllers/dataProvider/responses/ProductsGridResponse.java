package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.ProductDTO;

import java.util.List;

public class ProductsGridResponse {

    private Integer total;

    private List<ProductDTO> rows = Lists.newArrayList();

    public ProductsGridResponse(Integer total, List<ProductDTO> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<ProductDTO> getRows() {
        return rows;
    }

    public void setRows(List<ProductDTO> rows) {
        this.rows = rows;
    }
}
