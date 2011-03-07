package com.qcadoo.model.api.security;

import org.springframework.dao.DataAccessException;

public interface PasswordEncoder {

    String encodePassword(String rawPass, Object salt) throws DataAccessException;

    boolean isPasswordValid(String encPass, String rawPass, Object salt) throws DataAccessException;

}
