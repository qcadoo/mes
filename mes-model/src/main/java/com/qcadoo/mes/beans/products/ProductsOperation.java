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

    @Column(nullable = false, length = 255, unique = true)
    private String number;

    @Column(nullable = false, length = 2048, unique = true)
    private String name;

    @OneToMany(mappedBy = "operation", fetch = FetchType.LAZY)
    private List<ProductsTechnologyOperationComponent> operationComponents;

    @ManyToOne(fetch = FetchType.LAZY)
    private BasicStaff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    private BasicMachine machine;

    @Column(length = 2048)
    private String comment;

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

    public void setStaff(final BasicStaff staff) {
        this.staff = staff;
    }

    public BasicMachine getMachine() {
        return machine;
    }

    public void setMachine(final BasicMachine machine) {
        this.machine = machine;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

}
