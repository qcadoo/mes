package com.qcadoo.mes.materialFlowResources;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    private final NumberDeserializers.BigDecimalDeserializer delegate = NumberDeserializers.BigDecimalDeserializer.instance;

    @Override
    public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            return delegate.deserialize(jsonParser, deserializationContext);
        } catch (Exception e) {
            throw new RuntimeException("documentGrid.validate.field.error.invalidNumericFormat");
        }
    }

}
