package com.qcadoo.mes.cmmsMachineParts.notification;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.cmmsMachineParts.MaintenanceEventService;
import com.qcadoo.security.api.SecurityService;
import com.qcadoo.view.api.notifications.Notification;
import com.qcadoo.view.api.notifications.NotificationDataComponent;
import com.qcadoo.view.api.notifications.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MaintenanceEnentsNotification implements NotificationDataComponent {

    @Autowired
    private MaintenanceEventService maintenanceEventService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private SecurityService securityService;

    @Override
    public Optional<Notification> registerNotification() {
        if (securityService.hasCurrentUserRole("ROLE_EVENTS_NOTIFICATION")
                && maintenanceEventService.existsNewEventsToNotification(securityService.getCurrentUserId())) {
            Notification notification = new Notification(NotificationType.information, translationService.translate(
                    "cmmsMachineParts.maintenanceEvent.notification.newEventNotification", LocaleContextHolder.getLocale()),
                    true, true);
            return Optional.ofNullable(notification);
        }
        return Optional.empty();
    }
}
