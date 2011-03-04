package com.qcadoo.model.integration;

import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
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
