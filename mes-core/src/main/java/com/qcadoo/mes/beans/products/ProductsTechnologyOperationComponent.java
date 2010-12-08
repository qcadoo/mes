/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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
@Table(name = "products_technology_operation_component")
public class ProductsTechnologyOperationComponent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsTechnology technology;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsOperation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductsTechnologyOperationComponent parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<ProductsTechnologyOperationComponent> children;

    @OneToMany(mappedBy = "operationComponent", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<ProductsOperationProductComponent> operationProductComponents;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public ProductsTechnology getTechnology() {
        return technology;
    }

    public void setTechnology(final ProductsTechnology technology) {
        this.technology = technology;
    }

    public ProductsTechnologyOperationComponent getParent() {
        return parent;
    }

    public void setParent(final ProductsTechnologyOperationComponent parent) {
        this.parent = parent;
    }

    public List<ProductsTechnologyOperationComponent> getChildren() {
        return children;
    }

    public void setChildren(final List<ProductsTechnologyOperationComponent> children) {
        this.children = children;
    }

    public ProductsOperation getOperation() {
        return operation;
    }

    public void setOperation(final ProductsOperation operation) {
        this.operation = operation;
    }

    public List<ProductsOperationProductComponent> getOperationProductComponents() {
        return operationProductComponents;
    }

    public void setOperationProductComponents(final List<ProductsOperationProductComponent> operationProductComponents) {
        this.operationProductComponents = operationProductComponents;
    }

}
