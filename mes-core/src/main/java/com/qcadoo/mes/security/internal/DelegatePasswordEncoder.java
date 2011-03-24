package com.qcadoo.mes.security.internal;

import com.qcadoo.model.api.security.PasswordEncoder;

public class DelegatePasswordEncoder implements PasswordEncoder {

    private final org.springframework.security.authentication.encoding.PasswordEncoder passwordEncoder;

    public DelegatePasswordEncoder(final org.springframework.security.authentication.encoding.PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String encodePassword(final String rawPass, final Object salt) {
        return passwordEncoder.encodePassword(rawPass, salt);
    }

    @Override
    public boolean isPasswordValid(final String encPass, final String rawPass, final Object salt) {
        return passwordEncoder.isPasswordValid(encPass, rawPass, salt);
    }

}
