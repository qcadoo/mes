package com.qcadoo.mes.deliveries.listeners;

import com.google.common.base.Strings;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.ParameterService;
import com.qcadoo.mes.basic.constants.CompanyFields;
import com.qcadoo.mes.basic.constants.ParameterFields;
import com.qcadoo.mes.deliveries.constants.DeliveryFields;
import com.qcadoo.mes.deliveries.constants.ParameterFieldsD;
import com.qcadoo.mes.deliveries.print.OrderReportPdf;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.report.api.ReportService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class DeliveriesListListeners {

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private OrderReportPdf orderReportPdf;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private FileService fileService;

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
        Set<String> suppliersWithoutEmail = new HashSet<>();
        for (Entity delivery : gridComponent.getSelectedEntities()) {
            String supplierEmail = delivery.getBelongsToField(DeliveryFields.SUPPLIER).getStringField(CompanyFields.EMAIL);
            if (!Strings.isNullOrEmpty(supplierEmail)) {
                try {
                    sendHtmlTextEmail(mailSender, username, supplierEmail, subject + " " + delivery.getStringField(DeliveryFields.NUMBER), body, delivery);
                } catch (MailSendException | MailAuthenticationException e) {
                    state.addMessage(
                            "deliveries.delivery.error.sendEmailError",
                            ComponentState.MessageType.FAILURE);
                    return;
                }
                delivery.setField(DeliveryFields.DATE_OF_SENDING_EMAIL, date);
                delivery.getDataDefinition().save(delivery);
            } else {
                suppliersWithoutEmail.add(delivery.getBelongsToField(DeliveryFields.SUPPLIER).getStringField(CompanyFields.NUMBER));
            }
        }
        if (suppliersWithoutEmail.isEmpty()) {
            state.addMessage(
                    "deliveries.delivery.info.sendEmail",
                    ComponentState.MessageType.SUCCESS);
        } else {
            state.addMessage(
                    "deliveries.delivery.error.suppliersWithoutEmail",
                    ComponentState.MessageType.FAILURE, String.join(", ", suppliersWithoutEmail));
        }
    }

    private void sendHtmlTextEmail(JavaMailSender mailSender, String username, String supplierEmail, String subject, String body, Entity delivery) {
        File reportFile = getReportFile(delivery);
        Map<String, Object> model = new HashMap<>();
        model.put("id", delivery.getId());
        orderReportPdf.buildPdfDocumentToFile(model, reportFile);
        byte[] content = getFileContent(reportFile);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(username);
            mimeMessageHelper.setTo(supplierEmail);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
            mimeMessageHelper.addAttachment(reportFile.getName(), new ByteArrayResource(content));
        } catch (MessagingException e) {
            throw new MailPreparationException(e);
        }
        mailSender.send(mimeMessage);
        fileService.remove(reportFile.getPath());
    }

    private byte[] getFileContent(File reportFile) {
        byte[] content;
        try {
            content = Files.readAllBytes(reportFile.toPath());
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return content;
    }

    private File getReportFile(Entity delivery) {
        String fileName = translationService.translate("deliveries.order.report.fileName", LocaleContextHolder.getLocale(),
                delivery.getStringField(DeliveryFields.NUMBER), orderReportPdf.getStringFromDate(delivery.getDateField("updateDate")));
        File reportFile;
        try {
            reportFile = fileService.createReportFile(fileName + "."
                    + ReportService.ReportType.PDF.getExtension());
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return reportFile;
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
