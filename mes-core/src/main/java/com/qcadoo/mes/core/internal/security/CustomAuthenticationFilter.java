package com.qcadoo.mes.core.internal.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public final class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authResult) throws IOException, ServletException {

        RedirectResponseWrapper redirectResponseWrapper = new RedirectResponseWrapper(response);

        super.successfulAuthentication(request, redirectResponseWrapper, authResult);

        response.getOutputStream().println("loginSuccessfull");

    }

    @Override
    protected void unsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
            final AuthenticationException failed) throws IOException, ServletException {

        RedirectResponseWrapper redirectResponseWrapper = new RedirectResponseWrapper(response);

        super.unsuccessfulAuthentication(request, redirectResponseWrapper, failed);

        response.getOutputStream().println("loginUnsuccessfull:" + failed.getMessage());

    }

    private static final class RedirectResponseWrapper extends HttpServletResponseWrapper {

        public RedirectResponseWrapper(final HttpServletResponse httpServletResponse) {
            super(httpServletResponse);
        }

        @Override
        public void sendRedirect(final String string) throws IOException {
            // this method should be empty to prevent setting redirect by parent
        }

    }
}
