package com.qcadoo.mes.beans.products;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "products_genealogy_other_feature")
public class ProductsGenealogyOtherFeature {

    @Id
    @GeneratedValue
    private Long id;

    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsGenealogy genealogy;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public ProductsGenealogy getGenealogy() {
        return genealogy;
    }

    public void setGenealogy(final ProductsGenealogy genealogy) {
        this.genealogy = genealogy;
    }
}
