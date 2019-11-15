package com.qcadoo.mes.basic.imports.attribute;

import java.util.Objects;

public class AttributePosition {

    private int position;

    private String number;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AttributePosition))
            return false;
        AttributePosition that = (AttributePosition) o;
        return position == that.position && Objects.equals(number, that.number);
    }

    @Override public int hashCode() {
        return Objects.hash(position, number);
    }
}
