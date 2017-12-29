package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import java.util.List;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;

public class DataResponse {

    private final List<? extends AbstractDTO> entities;

    private int numberOfResults;

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    public List<? extends AbstractDTO> getEntities() {
        return entities;
    }

    public DataResponse(List<? extends AbstractDTO> entities, int numberOfResults) {
        this.entities = entities;
        this.numberOfResults = numberOfResults;
    }
}
