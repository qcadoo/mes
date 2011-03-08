package com.qcadoo.model.integration;

import org.springframework.dao.DataAccessException;

import com.qcadoo.model.api.security.PasswordEncoder;

public class MockPasswordEncoder implements PasswordEncoder {

    @Override
    public String encodePassword(final String rawPass, final Object salt) throws DataAccessException {
        return rawPass;
    }

    @Override
    public boolean isPasswordValid(final String encPass, final String rawPass, final Object salt) throws DataAccessException {
        return true;
    }

}
