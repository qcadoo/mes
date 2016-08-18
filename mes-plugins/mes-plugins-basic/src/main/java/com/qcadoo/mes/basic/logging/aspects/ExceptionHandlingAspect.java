/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
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
package com.qcadoo.mes.basic.logging.aspects;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import com.qcadoo.mes.basic.BasicException;
import com.qcadoo.mes.basic.ErrorResponse;
import com.qcadoo.mes.basic.constants.BasicConstants;
import com.qcadoo.plugin.api.RunIfEnabled;

@Aspect
@Configurable
@RunIfEnabled(BasicConstants.PLUGIN_IDENTIFIER)
public class ExceptionHandlingAspect {

    public static final String JSON_CONTENT_TYPE = "application/json";

    @Pointcut("if() && execution(org.springframework.web.servlet.ModelAndView com.qcadoo.view.internal.exceptionresolver.DefaultExceptionResolver.doResolveException(..)) "
            + "&& args(request, response, handler, exception)")
    public static boolean resolveException(final HttpServletRequest request, final HttpServletResponse response,
            final Object handler, final Exception exception) {
        return isIntegrationRequest(request);
    }

    protected static boolean isIntegrationRequest(final HttpServletRequest request) {
        String servletPath = request.getServletPath();
        return StringUtils.isNotEmpty(servletPath) && servletPath.startsWith("/rest");
    }

    @Around("resolveException(request, response, handler, exception)")
    public ModelAndView handleException(final ProceedingJoinPoint pjp, final HttpServletRequest request,
            final HttpServletResponse response, final Object handler, final Exception exception) throws Throwable {
        ErrorResponse errorResponse = null;
        if (exception instanceof BasicException || exception.getCause() instanceof BasicException) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse = new ErrorResponse(exception);
            writeResponse(response, errorResponse);
            return null;
        } else {
            return (ModelAndView) pjp.proceed();
        }

    }

    private void writeResponse(final HttpServletResponse response, final ErrorResponse body) throws IOException {
        Writer writer = null;
        response.setContentType(JSON_CONTENT_TYPE);
        Charset charset = Charset.forName("UTF-8");
        response.setCharacterEncoding(charset.toString());
        try {
            writer = new OutputStreamWriter(response.getOutputStream(), charset);
            writer.append(body.toJsonString());
            writer.flush();
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

}
