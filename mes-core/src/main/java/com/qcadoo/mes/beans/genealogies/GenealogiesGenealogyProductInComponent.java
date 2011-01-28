package com.qcadoo.mes.beans.genealogies;

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

import com.qcadoo.mes.beans.products.ProductsOperationProductInComponent;

@Entity
@Table(name = "genealogies_genealogy_product_in_component")
public class GenealogiesGenealogyProductInComponent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private GenealogiesGenealogy genealogy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsOperationProductInComponent productInComponent;

    @OneToMany(mappedBy = "productInComponent", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<GenealogiesProductInBatch> batch;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public GenealogiesGenealogy getGenealogy() {
        return genealogy;
    }

    public void setGenealogy(final GenealogiesGenealogy genealogy) {
        this.genealogy = genealogy;
    }

    public ProductsOperationProductInComponent getProductInComponent() {
        return productInComponent;
    }

    public void setProductInComponent(final ProductsOperationProductInComponent productInComponent) {
        this.productInComponent = productInComponent;
    }

    public List<GenealogiesProductInBatch> getBatch() {
        return batch;
    }

    public void setBatch(final List<GenealogiesProductInBatch> batch) {
        this.batch = batch;
    }

}
