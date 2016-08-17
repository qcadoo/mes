package com.qcadoo.mes.materialFlowResources;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;

public class BooleanDeserializer extends JsonDeserializer<Boolean> {

    private final NumberDeserializers.BooleanDeserializer delegate = new NumberDeserializers.BooleanDeserializer(Boolean.class,
            false);

    @Override
    public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonToken t = jsonParser.getCurrentToken();
        if (t == JsonToken.VALUE_STRING) {
            String text = jsonParser.getText().trim();
            if ("1".equals(text)) {
                return Boolean.TRUE;
            }
            if ("0".equals(text)) {
                return Boolean.FALSE;
            }
        }
        throw new RuntimeException("documentGrid.validate.field.error.invalidNumericFormat");
    }

}
