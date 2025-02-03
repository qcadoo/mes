package com.qcadoo.mes.deliveries.listeners;

import com.google.common.base.Strings;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

@Service
public class DeliveriesListListeners {

    @Autowired
    private ParameterService parameterService;

    public JavaMailSender getMailSender(Entity parameter) {
        JavaMailSenderImpl ms = new JavaMailSenderImpl();
        ms.setDefaultEncoding("utf-8");
        ms.setHost(parameter.getStringField(ParameterFields.EMAIL_HOST));
        ms.setPort(parameter.getIntegerField(ParameterFields.EMAIL_PORT) != null ? parameter.getIntegerField(ParameterFields.EMAIL_PORT) : -1);
        ms.setUsername(parameter.getStringField(ParameterFieldsD.EMAIL_USERNAME));
        ms.setPassword(parameter.getStringField(ParameterFieldsD.EMAIL_PASSWORD));
        Properties mailProperties = new Properties();
        mailProperties.put("mail.transport.protocol", "smtp");
        mailProperties.put("mail.smtp.auth", true);
        mailProperties.put("mail.smtp.starttls.enable", true);
        mailProperties.put("mail.smtp.ssl.enable", false);
        mailProperties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        mailProperties.put("mail.smtp.debug", false);
        ms.setJavaMailProperties(mailProperties);
        return ms;
    }

    public void sendEmail(final ViewDefinitionState view, final ComponentState state,
                          final String[] args) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        if (gridComponent.getSelectedEntitiesIds().size() > 20) {
            state.addMessage(
                    "deliveries.delivery.info.tooManyRowsToSendEmail",
                    ComponentState.MessageType.INFO);
            return;
        }

        Entity parameter = parameterService.getParameter();
        JavaMailSender mailSender = getMailSender(parameter);
        String username = parameter.getStringField(ParameterFieldsD.EMAIL_USERNAME);
        String subject = parameter.getStringField(ParameterFieldsD.DELIVERY_EMAIL_SUBJECT);
        String body = parameter.getStringField(ParameterFieldsD.DELIVERY_EMAIL_BODY);

        Date date = new Date();
        for (Entity delivery : gridComponent.getSelectedEntities()) {
            String supplierEmail = delivery.getBelongsToField(DeliveryFields.SUPPLIER).getStringField(CompanyFields.EMAIL);
            if (!Strings.isNullOrEmpty(supplierEmail)) {
                try {
                    sendHtmlTextEmail(mailSender, username, supplierEmail, subject + " " + delivery.getStringField(DeliveryFields.NUMBER), body);
                } catch (MailSendException | MailAuthenticationException e) {
                    state.addMessage(
                            "deliveries.delivery.error.sendEmailError",
                            ComponentState.MessageType.FAILURE);
                    return;
                }
                delivery.setField(DeliveryFields.DATE_OF_SENDING_EMAIL, date);
                delivery.getDataDefinition().save(delivery);
            } else {
                state.addMessage(
                        "deliveries.delivery.error.supplierWithoutEmail",
                        ComponentState.MessageType.FAILURE, delivery.getStringField(DeliveryFields.NUMBER));
            }
        }
    }

    private void sendHtmlTextEmail(JavaMailSender mailSender, String username, String supplierEmail, String subject, String body) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        try {
            mimeMessageHelper.setFrom(username);
            mimeMessageHelper.setTo(supplierEmail);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
        } catch (MessagingException e) {
            throw new MailPreparationException(e);
        }
        mailSender.send(mimeMessage);
    }

    public void releaseForPayment(final ViewDefinitionState view, final ComponentState state,
                                  final String[] args) {
        GridComponent gridComponent = (GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID);

        for (Entity delivery : gridComponent.getSelectedEntities()) {
            delivery.setField(DeliveryFields.RELEASED_FOR_PAYMENT, true);
            delivery.getDataDefinition().save(delivery);
        }

        state.addMessage(
                "deliveries.delivery.info.releasedForPayment",
                ComponentState.MessageType.SUCCESS);
    }
}
