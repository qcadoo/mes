package com.qcadoo.mes.view.internal;

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
import org.springframework.http.converter.HttpMessageNotWritableException;

public class JsonHttpMessageConverter extends AbstractHttpMessageConverter<JSONObject> {

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
            throws IOException, HttpMessageNotReadableException {
        String body = IOUtils.toString(inputMessage.getBody(), CHARSET.name());
        try {
            return new JSONObject(body);
        } catch (JSONException e) {
            throw new HttpMessageNotReadableException(e.getMessage(), e);
        }
    }

    @Override
    protected void writeInternal(final JSONObject json, final HttpOutputMessage outputMessage) throws IOException,
            HttpMessageNotWritableException {
        Writer writer = new OutputStreamWriter(outputMessage.getBody(), CHARSET);
        writer.write(json.toString());
        writer.flush();
    }

}
