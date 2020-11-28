package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.CountryDto;

import java.util.List;

public class CountriesGridResponse {

    private Integer total;

    private List<CountryDto> rows = Lists.newArrayList();

    public CountriesGridResponse(Integer total, List<CountryDto> rows) {
        this.total = total;
        this.rows = rows;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<CountryDto> getRows() {
        return rows;
    }

    public void setRows(List<CountryDto> rows) {
        this.rows = rows;
    }

}
