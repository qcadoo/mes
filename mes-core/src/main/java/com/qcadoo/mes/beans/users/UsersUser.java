package com.qcadoo.mes.beans.users;

public class UsersUser {

    private Long id;

    private String userName;

    private String email;

    private String firstName;

    private String lastName;

    private String description;

    private String password;

    private UsersGroup userGroup;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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
