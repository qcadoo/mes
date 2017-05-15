package com.qcadoo.mes.materialFlowResources.notification;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.service.DraftDocumentsNotificationService;
import com.qcadoo.view.api.notifications.Notification;
import com.qcadoo.view.api.notifications.NotificationDataComponent;
import com.qcadoo.view.api.notifications.NotificationType;

@Component
public class DraftDocumentsNotification implements NotificationDataComponent {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private DraftDocumentsNotificationService draftDocumentsNotificationService;

    @Override
    public Optional<Notification> registerNotification() {
        if (draftDocumentsNotificationService.shouldNotifyCurrentUser()) {
            String code = "materialFlowResources.notification.document.draftNotification";
            return Optional.of(new Notification(NotificationType.information,
                    translationService.translate(code, LocaleContextHolder.getLocale()), true, true));
        }
        return Optional.empty();
    }

}
