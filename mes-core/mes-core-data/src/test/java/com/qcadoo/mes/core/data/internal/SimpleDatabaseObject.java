package com.qcadoo.mes.core.data.internal;

public final class SimpleDatabaseObject {

    private Long id;

    private String name;

    private int age;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
