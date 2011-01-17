package com.qcadoo.mes.beans.products;

import java.util.List;

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
@Table(name = "products_genealogy_product_in_component")
public class ProductsGenealogyProductInComponent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsGenealogy genealogy;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsOperationProductInComponent productInComponent;

    @OneToMany(mappedBy = "productInComponent", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<ProductsGenealogyProductInBatch> batch;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ProductsGenealogy getGenealogy() {
        return genealogy;
    }

    public void setGenealogy(final ProductsGenealogy genealogy) {
        this.genealogy = genealogy;
    }

    public ProductsOperationProductInComponent getProductInComponent() {
        return productInComponent;
    }

    public void setProductInComponent(final ProductsOperationProductInComponent productInComponent) {
        this.productInComponent = productInComponent;
    }

    public List<ProductsGenealogyProductInBatch> getBatch() {
        return batch;
    }

    public void setBatch(final List<ProductsGenealogyProductInBatch> batch) {
        this.batch = batch;
    }

}
