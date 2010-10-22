package com.qcadoo.mes.beans.products;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "products_instruction")
public class ProductsInstruction {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false)
    private String name;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFrom;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTo;

    @ManyToOne(fetch = FetchType.EAGER)
    private ProductsProduct product;

    @Column(nullable = false)
    private String typeOfMaterial;

    private String description;

    private Boolean master;

    @OneToMany(mappedBy = "instruction", fetch = FetchType.LAZY)
    private List<ProductsInstructionBomComponent> bomComponents;

    private boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(final Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(final Date dateTo) {
        this.dateTo = dateTo;
    }

    public ProductsProduct getProduct() {
        return product;
    }

    public void setProduct(final ProductsProduct product) {
        this.product = product;
    }

    public String getTypeOfMaterial() {
        return typeOfMaterial;
    }

    public void setTypeOfMaterial(final String typeOfMaterial) {
        this.typeOfMaterial = typeOfMaterial;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Boolean getMaster() {
        return master;
    }

    public void setMaster(final Boolean master) {
        this.master = master;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public List<ProductsInstructionBomComponent> getBomComponents() {
        return bomComponents;
    }

    public void setBomComponents(List<ProductsInstructionBomComponent> bomComponents) {
        this.bomComponents = bomComponents;
    }

}
