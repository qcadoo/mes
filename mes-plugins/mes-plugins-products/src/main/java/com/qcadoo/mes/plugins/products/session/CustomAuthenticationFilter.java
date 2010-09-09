package com.qcadoo.mes.plugins.products.session;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult)
            throws IOException, ServletException {

        RedirectResponseWrapper redirectResponseWrapper = new RedirectResponseWrapper(response);

        super.successfulAuthentication(request, redirectResponseWrapper, authResult);

        response.getOutputStream().println("loginSuccessfull");

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {

        RedirectResponseWrapper redirectResponseWrapper = new RedirectResponseWrapper(response);

        super.unsuccessfulAuthentication(request, redirectResponseWrapper, failed);

        response.getOutputStream().println("loginUnsuccessfull:" + failed.getMessage());

    }

    private class RedirectResponseWrapper extends HttpServletResponseWrapper {

        public RedirectResponseWrapper(HttpServletResponse httpServletResponse) {
            super(httpServletResponse);
        }

        public void sendRedirect(String string) throws IOException {
        }

    }
}
