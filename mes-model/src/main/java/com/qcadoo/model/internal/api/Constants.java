package com.qcadoo.model.internal.api;

import org.springframework.core.io.support.ResourcePatternResolver;

public interface Constants {

    String XSL = "com/qcadoo/model/model-1.0.xsl";

    String XSD = "com/qcadoo/model/model-1.0.xsd";

    String RESOURCE_PATTERN = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "model/*.xml";

    String VALIDATION_MESSAGE_REQUIRED = "required";

    String VALIDATION_MESSAGE_BELOW_RANGE = "below range";

    String VALIDATION_MESSAGE_ABOVE_RANGE = "above range";

    String VALIDATION_MESSAGE_INVALID_LENGTH = "invalid length";

    String VALIDATION_MESSAGE_BELOW_MIN_LENGTH = "below min length";

    String VALIDATION_MESSAGE_ABOVE_MAX_LENGTH = "above max length";

    String VALIDATION_MESSAGE_INVALID_PRECISION = "invalid precision";

    String VALIDATION_MESSAGE_BELOW_MIN_PRECISION = "below min presicion";

    String VALIDATION_MESSAGE_ABOVE_MAX_PRECISION = "above max precision";

    String VALIDATION_MESSAGE_INVALID_SCALE = "invalid scale";

    String VALIDATION_MESSAGE_BELOW_MIN_SCALE = "below min scale";

    String VALIDATION_MESSAGE_ABOVE_MAX_SCALE = "above max scale";

}
