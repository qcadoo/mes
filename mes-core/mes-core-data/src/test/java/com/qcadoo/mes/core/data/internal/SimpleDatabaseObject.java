package com.qcadoo.mes.core.data.internal;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public final class SimpleDatabaseObject {

    private Long id;

    private String name;

    private Integer age;

    private BigDecimal money;

    private Boolean retired;

    private Date birthDate;

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Boolean getRetired() {
        return retired;
    }

    public void setRetired(Boolean retired) {
        this.retired = retired;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    private boolean deleted;

    private ParentDatabaseObject belongsTo;

    public SimpleDatabaseObject() {
    }

    public SimpleDatabaseObject(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public ParentDatabaseObject getBelongsTo() {
        return belongsTo;
    }

    public void setBelongsTo(ParentDatabaseObject belongsTo) {
        this.belongsTo = belongsTo;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).append(age).append(id).append(deleted).append(belongsTo).append(money)
                .append(retired).append(birthDate).toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleDatabaseObject)) {
            return false;
        }
        SimpleDatabaseObject other = (SimpleDatabaseObject) obj;
        return new EqualsBuilder().append(name, other.name).append(age, other.age).append(id, other.id)
                .append(deleted, other.deleted).append(belongsTo, other.belongsTo).append(money, other.money)
                .append(retired, other.retired).append(birthDate, other.birthDate).isEquals();
    }

}
