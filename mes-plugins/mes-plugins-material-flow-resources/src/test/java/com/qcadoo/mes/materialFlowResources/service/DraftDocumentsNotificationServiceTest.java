package com.qcadoo.mes.materialFlowResources.service;

import static com.qcadoo.mes.materialFlowResources.service.DraftDocumentsNotificationService.ROLE_DOCUMENTS_NOTIFICATION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.security.api.SecurityService;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(SearchRestrictions.class)
public class DraftDocumentsNotificationServiceTest {

    private static final long CURRENT_USER_ID = 1L;

    private DraftDocumentsNotificationService draftDocumentsNotificationService;

    @Mock
    private DataDefinitionService dataDefinitionService;

    @Mock
    private SecurityService securityService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        draftDocumentsNotificationService = new DraftDocumentsNotificationService(securityService, dataDefinitionService);
    }

    @Test
    public void shouldReturnFalseWhenUserNotLoggedIn() {
        // given
        given(securityService.getCurrentUserId()).willReturn(null);

        // when
        boolean result = draftDocumentsNotificationService.shouldNotifyCurrentUser();

        // then
        verify(securityService).getCurrentUserId();
        verify(securityService, never()).hasCurrentUserRole(ROLE_DOCUMENTS_NOTIFICATION);
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenUserHasNoRole() {
        // given
        given(securityService.getCurrentUserId()).willReturn(CURRENT_USER_ID);
        given(securityService.hasCurrentUserRole(ROLE_DOCUMENTS_NOTIFICATION)).willReturn(Boolean.FALSE);

        // when
        boolean result = draftDocumentsNotificationService.shouldNotifyCurrentUser();

        // then
        verify(securityService).getCurrentUserId();
        verify(securityService).hasCurrentUserRole(ROLE_DOCUMENTS_NOTIFICATION);
        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenThereAreNoDraftDocuments() {
        // given
        DraftDocumentsNotificationService spy = spy(draftDocumentsNotificationService);
        given(securityService.getCurrentUserId()).willReturn(CURRENT_USER_ID);
        given(securityService.hasCurrentUserRole(ROLE_DOCUMENTS_NOTIFICATION)).willReturn(Boolean.TRUE);
        doReturn(0).when(spy).countDraftDocumentsForUser(CURRENT_USER_ID);

        // when
        boolean result = spy.shouldNotifyCurrentUser();

        // then
        verify(securityService).getCurrentUserId();
        verify(securityService).hasCurrentUserRole(ROLE_DOCUMENTS_NOTIFICATION);
        verify(spy).countDraftDocumentsForUser(CURRENT_USER_ID);
        assertFalse(result);
    }

    @Test
    public void shouldReturnTrueWhenThereAreDraftDocuments() {
        // given
        DraftDocumentsNotificationService spy = spy(draftDocumentsNotificationService);
        given(securityService.getCurrentUserId()).willReturn(CURRENT_USER_ID);
        given(securityService.hasCurrentUserRole(ROLE_DOCUMENTS_NOTIFICATION)).willReturn(Boolean.TRUE);
        doReturn(1).when(spy).countDraftDocumentsForUser(CURRENT_USER_ID);

        // when
        boolean result = spy.shouldNotifyCurrentUser();

        // then
        verify(securityService).getCurrentUserId();
        verify(securityService).hasCurrentUserRole(ROLE_DOCUMENTS_NOTIFICATION);
        verify(spy).countDraftDocumentsForUser(CURRENT_USER_ID);
        assertTrue(result);
    }

}
