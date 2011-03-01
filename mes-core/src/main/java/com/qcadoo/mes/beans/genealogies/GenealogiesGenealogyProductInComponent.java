/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

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
