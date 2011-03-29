/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.3.0
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */

package com.qcadoo.mes.application;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.qcadoo.view.internal.resource.ResourceService;

public final class StaticResourcesFilter implements Filter {

    private boolean useJarStaticResources;

    private static final List<String> NOT_STATIC_EXTENSIONS = Arrays.asList(new String[] { "html", "pdf", "xls", "/" });

    @Autowired
    private ResourceService resourceService;

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String[] arr = httpRequest.getRequestURI().split("\\.");
        String ext = arr[arr.length - 1];

        if (!NOT_STATIC_EXTENSIONS.contains(ext) && useJarStaticResources) {
            resourceService.serveResource(httpRequest, (HttpServletResponse) response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // empty
    }

    @Override
    public void destroy() {
        // empty
    }

    public void setUseJarStaticResources(final boolean useJarStaticResources) {
        this.useJarStaticResources = useJarStaticResources;
    }

}
