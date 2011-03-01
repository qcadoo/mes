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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

public final class JsonHttpMessageConverter extends AbstractHttpMessageConverter<JSONObject> {

    public static final Charset CHARSET = Charset.forName("UTF-8");

    public static final MediaType MEDIA_TYPE = new MediaType("application", "json", CHARSET);

    public JsonHttpMessageConverter() {
        super(MEDIA_TYPE);
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return JSONObject.class.isAssignableFrom(clazz);
    }

    @Override
    protected JSONObject readInternal(final Class<? extends JSONObject> clazz, final HttpInputMessage inputMessage)
            throws IOException {
        String body = IOUtils.toString(inputMessage.getBody(), CHARSET.name());
        try {
            return new JSONObject(body);
        } catch (JSONException e) {
            throw new HttpMessageNotReadableException(e.getMessage(), e);
        }
    }

    @Override
    protected void writeInternal(final JSONObject json, final HttpOutputMessage outputMessage) throws IOException {
        Writer writer = null;

        try {
            writer = new OutputStreamWriter(outputMessage.getBody(), CHARSET);
            writer.write(json.toString());
            writer.flush();
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

}
