package com.qcadoo.mes.security.internal;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class SessionExpirationFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            return;
        }
        if (!(response instanceof HttpServletResponse)) {
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (httpRequest.getSession(false) == null && httpRequest.getRequestedSessionId() != null
                && !httpRequest.isRequestedSessionIdValid()) {
            redirectToLoginPage(httpRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private void redirectToLoginPage(final HttpServletRequest httpRequest, final ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("true".equals(httpRequest.getParameter("iframe"))) {
            String targetUrl = httpRequest.getContextPath() + "/login.html?iframe=true";
            httpResponse.sendRedirect(httpResponse.encodeRedirectURL(targetUrl));
        } else if ("XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"))) {
            httpResponse.getOutputStream().println("sessionExpired");
        } else {
            String targetUrl = httpRequest.getContextPath() + "/login.html";
            httpResponse.sendRedirect(httpResponse.encodeRedirectURL(targetUrl));
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

}
