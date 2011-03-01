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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "genealogies_current_attribute")
public class GenealogiesCurrentAttribute {

    @Id
    @GeneratedValue
    private Long id;

    private String shift;

    private String post;

    private String other;

    private String lastUsedShift;

    private String lastUsedPost;

    private String lastUsedOther;

    private Boolean shiftReq = false;

    private Boolean postReq = false;

    private Boolean otherReq = false;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(final String shift) {
        this.shift = shift;
    }

    public String getPost() {
        return post;
    }

    public void setPost(final String post) {
        this.post = post;
    }

    public String getOther() {
        return other;
    }

    public void setOther(final String other) {
        this.other = other;
    }

    public String getLastUsedShift() {
        return lastUsedShift;
    }

    public void setLastUsedShift(final String lastUsedShift) {
        this.lastUsedShift = lastUsedShift;
    }

    public String getLastUsedPost() {
        return lastUsedPost;
    }

    public void setLastUsedPost(final String lastUsedPost) {
        this.lastUsedPost = lastUsedPost;
    }

    public String getLastUsedOther() {
        return lastUsedOther;
    }

    public void setLastUsedOther(final String lastUsedOther) {
        this.lastUsedOther = lastUsedOther;
    }

    public Boolean getShiftReq() {
        return shiftReq;
    }

    public void setShiftReq(Boolean shiftReq) {
        this.shiftReq = shiftReq;
    }

    public Boolean getPostReq() {
        return postReq;
    }

    public void setPostReq(Boolean postReq) {
        this.postReq = postReq;
    }

    public Boolean getOtherReq() {
        return otherReq;
    }

    public void setOtherReq(Boolean otherReq) {
        this.otherReq = otherReq;
    }
}
