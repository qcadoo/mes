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

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.qcadoo.mes.beans.products.ProductsOrder;

@Entity
@Table(name = "genealogies_genealogy")
public class GenealogiesGenealogy {

    @Id
    @GeneratedValue
    private Long id;

    private String batch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ProductsOrder order;

    @OneToMany(mappedBy = "genealogy", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<GenealogiesGenealogyProductInComponent> productInComponents;

    @OneToMany(mappedBy = "genealogy", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<GenealogiesShiftFeature> shiftFeatures;

    @OneToMany(mappedBy = "genealogy", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<GenealogiesOtherFeature> otherFeatures;

    @OneToMany(mappedBy = "genealogy", fetch = FetchType.LAZY)
    @Cascade({ CascadeType.DELETE })
    private List<GenealogiesPostFeature> postFeatures;

    @Temporal(TemporalType.DATE)
    private Date date;

    private String worker;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(final String batch) {
        this.batch = batch;
    }

    public ProductsOrder getOrder() {
        return order;
    }

    public void setOrder(final ProductsOrder order) {
        this.order = order;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(final String worker) {
        this.worker = worker;
    }

    public List<GenealogiesGenealogyProductInComponent> getProductInComponents() {
        return productInComponents;
    }

    public void setProductInComponents(final List<GenealogiesGenealogyProductInComponent> productInComponents) {
        this.productInComponents = productInComponents;
    }

    public List<GenealogiesShiftFeature> getShiftFeatures() {
        return shiftFeatures;
    }

    public void setShiftFeatures(final List<GenealogiesShiftFeature> shiftFeatures) {
        this.shiftFeatures = shiftFeatures;

    }

    public List<GenealogiesOtherFeature> getOtherFeatures() {
        return otherFeatures;
    }

    public void setOtherFeatures(final List<GenealogiesOtherFeature> otherFeatures) {
        this.otherFeatures = otherFeatures;
    }

    public List<GenealogiesPostFeature> getPostFeatures() {
        return postFeatures;
    }

    public void setPostFeatures(final List<GenealogiesPostFeature> postFeatures) {
        this.postFeatures = postFeatures;
    }

}
