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

package com.qcadoo.mes.beans.products;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.qcadoo.mes.beans.genealogies.GenealogiesGenealogyProductInComponent;

@Entity
@Table(name = "products_operation_product_in_component")
public class ProductsOperationProductInComponent {

    @Id
    @GeneratedValue
    private Long id;

    private Boolean batchRequired;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsTechnologyOperationComponent operationComponent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsProduct product;

    @Column(scale = 3, precision = 10)
    private BigDecimal quantity;

    @OneToMany(mappedBy = "productInComponent", fetch = FetchType.LAZY)
    private List<GenealogiesGenealogyProductInComponent> genealogyProductInComponents;

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ProductsTechnologyOperationComponent getOperationComponent() {
        return operationComponent;
    }

    public ProductsProduct getProduct() {
        return product;
    }

    public void setOperationComponent(final ProductsTechnologyOperationComponent operationComponent) {
        this.operationComponent = operationComponent;
    }

    public void setProduct(final ProductsProduct product) {
        this.product = product;
    }

    public Boolean getBatchRequired() {
        return batchRequired;
    }

    public void setBatchRequired(final Boolean batchRequired) {
        this.batchRequired = batchRequired;
    }

    public List<GenealogiesGenealogyProductInComponent> getGenealogyProductInComponents() {
        return genealogyProductInComponents;
    }

    public void setGenealogyProductInComponents(final List<GenealogiesGenealogyProductInComponent> genealogyProductInComponents) {
        this.genealogyProductInComponents = genealogyProductInComponents;
    }

}
