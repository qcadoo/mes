package com.qcadoo.mes.beans.products;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.qcadoo.mes.beans.basic.BasicMachine;
import com.qcadoo.mes.beans.basic.BasicStaff;

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
    private List<ProductsTechnologyOperationComponent> operationComponents;

    @ManyToOne(fetch = FetchType.LAZY)
    private BasicStaff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    private BasicMachine machine;

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

    public List<ProductsTechnologyOperationComponent> getOperationComponents() {
        return operationComponents;
    }

    public void setOperationComponents(final List<ProductsTechnologyOperationComponent> technologyOperationComponents) {
        this.operationComponents = technologyOperationComponents;
    }

    public BasicStaff getStaff() {
        return staff;
    }

    public void setStaff(BasicStaff staff) {
        this.staff = staff;
    }

    public BasicMachine getMachine() {
        return machine;
    }

    public void setMachine(BasicMachine machine) {
        this.machine = machine;
    }

}
