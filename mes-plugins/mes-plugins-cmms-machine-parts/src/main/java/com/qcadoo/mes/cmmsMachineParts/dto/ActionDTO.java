package com.qcadoo.mes.cmmsMachineParts.dto;

import com.qcadoo.mes.basic.controllers.dataProvider.dto.AbstractDTO;

public class ActionDTO implements AbstractDTO {

    private Long id;

    private String code;

    private Long plannedEventId;

    public ActionDTO() {
    }

    public ActionDTO(Long id, String code, Long plannedEventId) {
        this.id = id;
        this.code = code;
        this.plannedEventId = plannedEventId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ActionDTO actionDTO = (ActionDTO) o;

        return code != null ? code.equals(actionDTO.code) : actionDTO.code == null;

    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlannedEventId() {
        return plannedEventId;
    }

    public void setPlannedEventId(Long plannedEventId) {
        this.plannedEventId = plannedEventId;
    }
}
