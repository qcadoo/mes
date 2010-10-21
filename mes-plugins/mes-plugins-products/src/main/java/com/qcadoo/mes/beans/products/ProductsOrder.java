package com.qcadoo.mes.beans.products;

import java.math.BigDecimal;
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
@Table(name = "products_order")
public class ProductsOrder {

    @Id
    @GeneratedValue
    private Long id;

    private String number;

    private String name;

    private String state;

    private boolean deleted;

    @Temporal(TemporalType.DATE)
    private Date effectiveDateFrom;

    @Temporal(TemporalType.DATE)
    private Date effectiveDateTo;

    @Temporal(TemporalType.DATE)
    private Date dateFrom;

    @Temporal(TemporalType.DATE)
    private Date dateTo;

    private String machine;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsProduct product;

    @Column(scale = 3, precision = 10)
    private BigDecimal plannedQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsInstruction defaultInstruction;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsInstruction instruction;

    @Column(scale = 3, precision = 10)
    private BigDecimal doneQuantity;

    private String startWorker;

    private String endWorker;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<ProductsMaterialRequirementComponent> materialRequirements;

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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public Date getEffectiveDateFrom() {
        return effectiveDateFrom;
    }

    public void setEffectiveDateFrom(final Date effectiveDateFrom) {
        this.effectiveDateFrom = effectiveDateFrom;
    }

    public Date getEffectiveDateTo() {
        return effectiveDateTo;
    }

    public void setEffectiveDateTo(final Date effectiveDateTo) {
        this.effectiveDateTo = effectiveDateTo;
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

    public String getMachine() {
        return machine;
    }

    public void setMachine(final String machine) {
        this.machine = machine;
    }

    public ProductsProduct getProduct() {
        return product;
    }

    public void setProduct(final ProductsProduct product) {
        this.product = product;
    }

    public BigDecimal getPlannedQuantity() {
        return plannedQuantity;
    }

    public void setPlannedQuantity(final BigDecimal plannedQuantity) {
        this.plannedQuantity = plannedQuantity;
    }

    public ProductsInstruction getDefaultInstruction() {
        return defaultInstruction;
    }

    public void setDefaultInstruction(final ProductsInstruction defaultInstruction) {
        this.defaultInstruction = defaultInstruction;
    }

    public ProductsInstruction getInstruction() {
        return instruction;
    }

    public void setInstruction(final ProductsInstruction instruction) {
        this.instruction = instruction;
    }

    public BigDecimal getDoneQuantity() {
        return doneQuantity;
    }

    public void setDoneQuantity(final BigDecimal doneQuantity) {
        this.doneQuantity = doneQuantity;
    }

    public String getStartWorker() {
        return startWorker;
    }

    public void setStartWorker(final String startWorker) {
        this.startWorker = startWorker;
    }

    public String getEndWorker() {
        return endWorker;
    }

    public void setEndWorker(final String endWorker) {
        this.endWorker = endWorker;
    }

    public List<ProductsMaterialRequirementComponent> getMaterialRequirements() {
        return materialRequirements;
    }

    public void setMaterialRequirements(final List<ProductsMaterialRequirementComponent> materialRequirements) {
        this.materialRequirements = materialRequirements;
    }

}
