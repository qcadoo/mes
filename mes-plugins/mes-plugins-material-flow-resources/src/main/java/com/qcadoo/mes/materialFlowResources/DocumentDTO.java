package com.qcadoo.mes.materialFlowResources;

import java.util.Objects;

public class DocumentDTO {

    private Long id;
    private String state;
    private String type;
    private Long locationTo_id;
    private Long locationFrom_id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getLocationTo_id() {
        return locationTo_id;
    }

    public void setLocationTo_id(Long locationTo_id) {
        this.locationTo_id = locationTo_id;
    }

    public Long getLocationFrom_id() {
        return locationFrom_id;
    }

    public void setLocationFrom_id(Long locationFrom_id) {
        this.locationFrom_id = locationFrom_id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.id);
        hash = 83 * hash + Objects.hashCode(this.state);
        hash = 83 * hash + Objects.hashCode(this.type);
        hash = 83 * hash + Objects.hashCode(this.locationTo_id);
        hash = 83 * hash + Objects.hashCode(this.locationFrom_id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocumentDTO other = (DocumentDTO) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.state, other.state)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.locationTo_id, other.locationTo_id)) {
            return false;
        }
        if (!Objects.equals(this.locationFrom_id, other.locationFrom_id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DocumentDTO{" + "id=" + id + ", state=" + state + ", type=" + type + ", locationTo_id=" + locationTo_id + ", locationFrom_id=" + locationFrom_id + '}';
    }


}
