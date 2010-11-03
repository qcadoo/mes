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
import javax.servlet.http.HttpServletResponseWrapper;

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

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        RedirectResponseWrapper redirectResponseWrapper = new RedirectResponseWrapper(httpResponse);

        chain.doFilter(request, redirectResponseWrapper);

        if (redirectResponseWrapper.getRedirect() != null) {
            if (redirectResponseWrapper.getRedirect().contains("logout=true")) {
                httpResponse.sendRedirect(redirectResponseWrapper.getRedirect());
            } else {
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                redirectToLoginPage(httpRequest, httpResponse);
            }
        }
    }

    private void redirectToLoginPage(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if ("true".equals(request.getParameter("iframe"))) {
            String targetUrl = request.getContextPath() + "/login.html?iframe=true";
            response.sendRedirect(response.encodeRedirectURL(targetUrl));
        } else if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.getOutputStream().println("sessionExpired");
        } else {
            String targetUrl = request.getContextPath() + "/login.html?timeout=true";
            response.sendRedirect(response.encodeRedirectURL(targetUrl));
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    private static final class RedirectResponseWrapper extends HttpServletResponseWrapper {

        private String redirect = null;

        public RedirectResponseWrapper(final HttpServletResponse httpServletResponse) {
            super(httpServletResponse);
        }

        @Override
        public void sendRedirect(final String string) throws IOException {
            System.out.println(" ------------------------------------------------------------------- IS REDIRECT to " + string);
            redirect = string;
        }

        public String getRedirect() {
            return redirect;
        }
    }

}
