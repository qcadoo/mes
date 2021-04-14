package com.qcadoo.mes.basic.controllers.dataProvider.dto;

import java.util.Objects;

public class FactoryDto {

    private Long id;

    private String number;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FactoryDto))
            return false;
        FactoryDto that = (FactoryDto) o;
        return Objects.equals(id, that.id) && Objects.equals(number, that.number) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number, name);
    }
}
