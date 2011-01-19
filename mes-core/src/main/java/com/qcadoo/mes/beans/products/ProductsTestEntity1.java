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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "products_test_entity_1")
public class ProductsTestEntity1 {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 40)
    private String number;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String typeOfMaterial;

    private String ean;

    private String category;

    private String unit;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsTestEntity3 testEntity3;

    @OneToMany(mappedBy = "testEntity1", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<ProductsTestEntity2> testEntity2;

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

    public String getTypeOfMaterial() {
        return typeOfMaterial;
    }

    public void setTypeOfMaterial(String typeOfMaterial) {
        this.typeOfMaterial = typeOfMaterial;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<ProductsTestEntity2> getTestEntity2() {
        return testEntity2;
    }

    public void setTestEntity2(List<ProductsTestEntity2> testEntity2) {
        this.testEntity2 = testEntity2;
    }

    public ProductsTestEntity3 getTestEntity3() {
        return testEntity3;
    }

    public void setTestEntity3(ProductsTestEntity3 testEntity3) {
        this.testEntity3 = testEntity3;
    }

}
