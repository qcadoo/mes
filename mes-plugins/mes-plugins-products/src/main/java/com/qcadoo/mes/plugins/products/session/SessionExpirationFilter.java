package com.qcadoo.mes.plugins.products.session;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.InitializingBean;

public class SessionExpirationFilter implements Filter, InitializingBean {

    public void afterPropertiesSet() throws Exception {
    }

    public void destroy() {
    }

    @SuppressWarnings("cast")
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        // checkArgument(request instanceof HttpServletRequest, "must be HttpServletRequest");
        // checkArgument(response instanceof HttpServletResponse, "must be HttpServletResponse");
        if (!(request instanceof HttpServletRequest)) {
            return;
        }
        if (!(response instanceof HttpServletResponse)) {
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        if (session == null && httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid()) {

            if ("true".equals(httpRequest.getParameter("iframe"))) {
                String targetUrl = httpRequest.getContextPath() + "/login.html?iframe=true";
                httpResponse.sendRedirect(httpResponse.encodeRedirectURL(targetUrl));
            } else if ("XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"))) {
                httpResponse.getOutputStream().println("sessionExpired");
            } else {
                String targetUrl = httpRequest.getContextPath() + "/login.html";
                httpResponse.sendRedirect(httpResponse.encodeRedirectURL(targetUrl));
            }
            return;
        }

        chain.doFilter(request, response);
    }

    public void init(FilterConfig arg0) throws ServletException {
    }

}