package com.qcadoo.mes.materialFlowResources.service;

import com.qcadoo.mes.materialFlowResources.constants.MaterialFlowResourcesConstants;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.exception.EntityRuntimeException;
import com.qcadoo.security.api.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TransactionAspectSupport.class)
public class DocumentBuilderTest {

    private DocumentBuilder documentBuilder;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private ResourceManagementService resourceManagementService;

    @Mock
    private UserService userService;

    @Mock
    private DataDefinition dataDefinition;

    @Mock
    private Entity entity;

    @Mock
    private Entity currentUser;

    @Mock
    private ReceiptDocumentForReleaseHelper receiptDocumentForReleaseHelper;

    @Mock
    private DocumentStateChangeService documentStateChangeService;

    @Mock
    private TransactionStatus transactionStatus;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(TransactionAspectSupport.class);

        given(TransactionAspectSupport.currentTransactionStatus()).willReturn(transactionStatus);

        given(dataDefinitionService.get(MaterialFlowResourcesConstants.PLUGIN_IDENTIFIER,
                MaterialFlowResourcesConstants.MODEL_DOCUMENT)).willReturn(dataDefinition);

        given(dataDefinition.create()).willReturn(entity);

        given(dataDefinition.save(anyObject())).willReturn(entity);

        given(entity.isValid()).willReturn(Boolean.FALSE);

        given(userService.getCurrentUserEntity()).willReturn(currentUser);

        given(currentUser.getId()).willReturn(1L);

        documentBuilder = new DocumentBuilder(dataDefinitionService, resourceManagementService, receiptDocumentForReleaseHelper,
                documentStateChangeService, userService.getCurrentUserEntity());
    }

    @Test
    public void shouldRollbackOnErrorWhenBuild() {

        // given
        // common logic is inside init()

        // when
        Entity result = documentBuilder.build();

        // then
        Assert.assertFalse(result.isValid());
        verify(transactionStatus).setRollbackOnly();
    }

    @Test(expected = EntityRuntimeException.class)
    public void shouldThrowOnErrorWhenBuildWithEntityRuntimeException() {

        // given
        // common logic is inside init()

        // when
        documentBuilder.buildWithEntityRuntimeException();

        // then
        // expect EntityRuntimeException
    }

}
