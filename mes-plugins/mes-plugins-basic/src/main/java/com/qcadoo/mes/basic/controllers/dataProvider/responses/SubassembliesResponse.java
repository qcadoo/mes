package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import com.google.common.collect.Lists;
import com.qcadoo.mes.basic.controllers.dataProvider.dto.SubassemblyDto;

import java.util.List;

public class SubassembliesResponse {

    private List<SubassemblyDto> subassemblies = Lists.newArrayList();

    public SubassembliesResponse(List<SubassemblyDto> subassemblies) {
        this.subassemblies = subassemblies;
    }

    public List<SubassemblyDto> getSubassemblies() {
        return subassemblies;
    }

    public void setSubassemblies(List<SubassemblyDto> subassemblies) {
        this.subassemblies = subassemblies;
    }
}
