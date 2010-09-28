package com.qcadoo.mes.beans.users;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "users_user")
public final class UsersUser {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String userName;

    @ManyToOne(fetch = FetchType.EAGER)
    private UsersGroup userGroup;

    private String email;

    private String firstName;

    private String lastName;

    private String description;

    @Column(nullable = false)
    private String password;

    private boolean deleted;

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public UsersGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(final UsersGroup userGroup) {
        this.userGroup = userGroup;
    }

}
