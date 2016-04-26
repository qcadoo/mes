package com.qcadoo.mes.basic.controllers.dataProvider.responses;

import java.util.List;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;

public class DataResponse {

    private List<AbstractDTO> entities;

    private int numberOfResults;

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    public List<AbstractDTO> getEntities() {
        return entities;
    }

    public void setEntities(List<AbstractDTO> entities) {
        this.entities = entities;
    }

    public DataResponse(List<AbstractDTO> entities, int numberOfResults) {
        this.entities = entities;
        this.numberOfResults = numberOfResults;
    }
}
