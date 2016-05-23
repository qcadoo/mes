package com.qcadoo.mes.materialFlowResources;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DateDeserializer extends JsonDeserializer<Date> {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        format.setLenient(false);
        String date = jsonParser.getText();
        if (StringUtils.isEmpty(date)) {
            return null;
        }
        try {
            return format.parse(date);
        } catch (Exception e) {
            throw new RuntimeException("documentGrid.validate.field.error.invalidDateFormat");
        }
    }

}
