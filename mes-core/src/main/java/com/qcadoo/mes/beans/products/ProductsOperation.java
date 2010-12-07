package com.qcadoo.mes.beans.products;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "products_operation")
public class ProductsOperation {

    @Id
    @GeneratedValue
    private Long id;

    private String number;

    private String name;

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
