package com.qcadoo.mes.beans.products;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "products_operation")
public class ProductsOperation {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 255)
    private String number;

    @Column(nullable = false, length = 2048)
    private String name;

    @OneToMany(mappedBy = "operation", fetch = FetchType.LAZY)
    private List<ProductsTechnologyOperationComponent> technologyOperationComponents;

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

    public List<ProductsTechnologyOperationComponent> getTechnologyOperationComponents() {
        return technologyOperationComponents;
    }

    public void setTechnologyOperationComponents(final List<ProductsTechnologyOperationComponent> technologyOperationComponents) {
        this.technologyOperationComponents = technologyOperationComponents;
    }

}
