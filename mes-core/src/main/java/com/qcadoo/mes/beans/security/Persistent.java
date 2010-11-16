/**
 * ********************************************************************
 * Code developed by amazing QCADOO developers team.
 * Copyright (c) Qcadoo Limited sp. z o.o. (2010)
 * ********************************************************************
 */

package com.qcadoo.mes.beans.security;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "persistent_logins")
public class Persistent {

    @Column(nullable = false)
    private String username;

    @Id
    private String series;

    @Column(nullable = false)
    private String token;

    @Column(name = "last_used", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUsed;

    public final String getUsername() {
        return username;
    }

    public final void setUsername(final String username) {
        this.username = username;
    }

    public final String getSeries() {
        return series;
    }

    public final void setSeries(final String series) {
        this.series = series;
    }

    public final String getToken() {
        return token;
    }

    public final void setToken(final String token) {
        this.token = token;
    }

    public final Date getLastUsed() {
        return lastUsed;
    }

    public final void setLastUsed(final Date lastUsed) {
        this.lastUsed = lastUsed;
    }

}
