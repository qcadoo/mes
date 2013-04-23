package com.qcadoo.mes.workPlans.hooks;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.qcadoo.mes.workPlans.constants.WorkPlansConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.FieldDefinition;

public class ValidatorServiceImplTest {

    private ValidatorServiceImpl validatorService;

    @Mock
    private Entity entity;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private FieldDefinition attachmentFieldDef;

    @Before
    public final void init() {
        MockitoAnnotations.initMocks(this);
        validatorService = new ValidatorServiceImpl();
    }

    @Test
    public final void shouldMarkExtensionAsValidForNullAttachmentPathValue() {
        // given
        final String oldValue = "valid.png";
        final String newValue = null;

        // when
        final boolean isValid = validatorService.checkAttachmentExtension(dataDefinition, attachmentFieldDef, entity, oldValue,
                newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidForEmptyAttachmentPathValue() {
        // given
        final String oldValue = "valid.png";
        final String newValue = "";

        // when
        final boolean isValid = validatorService.checkAttachmentExtension(dataDefinition, attachmentFieldDef, entity, oldValue,
                newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidForBlankAttachmentPathValue() {
        // given
        final String oldValue = "valid.png";
        final String newValue = "   ";

        // when
        final boolean isValid = validatorService.checkAttachmentExtension(dataDefinition, attachmentFieldDef, entity, oldValue,
                newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidIfNewValueIsEqualToOldOne() {
        // given
        final String oldValue = "valid.png";
        final String newValue = oldValue;

        // when
        final boolean isValid = validatorService.checkAttachmentExtension(dataDefinition, attachmentFieldDef, entity, oldValue,
                newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidIfNewValueIsEqualToOldOneEvenIfBothValuesAreInvalid() {
        // given
        final String oldValue = "invalid.mp3";
        final String newValue = oldValue;

        // when
        final boolean isValid = validatorService.checkAttachmentExtension(dataDefinition, attachmentFieldDef, entity, oldValue,
                newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidIfNewValueIsValidAndOldValueIsInvalid() {
        // given
        final String oldValue = "invalid.mp3";
        final String newValue = "valid.png";

        // when
        final boolean isValid = validatorService.checkAttachmentExtension(dataDefinition, attachmentFieldDef, entity, oldValue,
                newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkExtensionAsValidEvenIfNewValueHasManyDots() {
        // given
        final String oldValue = null;
        final String newValue = "valid.wav.mp3.rmvb.pdf.sh.png";

        // when
        final boolean isValid = validatorService.checkAttachmentExtension(dataDefinition, attachmentFieldDef, entity, oldValue,
                newValue);

        // then
        Assert.assertTrue(isValid);
    }

    @Test
    public final void shouldMarkAllowedExtensionAsValid() {
        // given
        final String oldValue = null;
        final String newValuePrefix = "someFile.";

        // when
        for (String allowedFileExtension : WorkPlansConstants.FILE_EXTENSIONS) {
            boolean isValid = validatorService.checkAttachmentExtension(dataDefinition, attachmentFieldDef, entity, oldValue,
                    newValuePrefix + allowedFileExtension);
            Assert.assertTrue(isValid);
        }
    }

    @Test
    public final void shouldMarkDisallowedExtensionAsInvalid() {
        // given
        final String oldValue = null;
        final String newValuePrefix = "someFile.";
        final String disallowedExtension = ".mp3";

        // when
        for (String allowedFileExtension : WorkPlansConstants.FILE_EXTENSIONS) {
            boolean isValid = validatorService.checkAttachmentExtension(dataDefinition, attachmentFieldDef, entity, oldValue,
                    newValuePrefix + allowedFileExtension + disallowedExtension);
            Assert.assertFalse(isValid);
        }
    }

}
