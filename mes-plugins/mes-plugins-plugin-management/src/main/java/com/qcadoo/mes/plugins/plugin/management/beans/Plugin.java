package com.qcadoo.mes.plugins.plugin.management.beans;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public final class Plugin {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String publisher;

    private String version;

    private String description;

    private boolean deleted;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean active;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean base;

    private String codeId;

    private String packageName;

    public boolean isBase() {
        return base;
    }

    public void setBase(final boolean base) {
        this.base = base;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(final String publisher) {
        this.publisher = publisher;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getCodeId() {
        return codeId;
    }

    public void setCodeId(final String codeId) {
        this.codeId = codeId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

}
