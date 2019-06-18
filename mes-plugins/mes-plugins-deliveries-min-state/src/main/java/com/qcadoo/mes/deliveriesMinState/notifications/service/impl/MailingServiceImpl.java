/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo Framework
 * Version: 1.4
 *
 * This file is part of Qcadoo.
 *
 * Qcadoo is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * ***************************************************************************
 */
package com.qcadoo.mes.deliveriesMinState.notifications.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.deliveriesMinState.notifications.constants.SendingStatus;
import com.qcadoo.mes.deliveriesMinState.notifications.mandrill.ContentItem;
import com.qcadoo.mes.deliveriesMinState.notifications.mandrill.Recipient;
import com.qcadoo.mes.deliveriesMinState.notifications.mandrill.SendResponse;
import com.qcadoo.mes.deliveriesMinState.notifications.mandrill.TemplateEmail;
import com.qcadoo.mes.emailNotifications.sendinblue.Mailin;
import com.qcadoo.mes.deliveriesMinState.notifications.service.MailingService;
import com.qcadoo.mes.deliveriesMinState.notifications.service.MandrillService;

@Service
public class MailingServiceImpl implements MailingService {

    @Value("${deliveryTemplateEmail}")
    private String deliveryTemplateEmail;

    @Value("${mandrillApiKey}")
    private String apiKey;

    @Autowired
    private MandrillService mandrillService;

    @Override
    public SendingStatus sendTemplateEmailByMandrill(TemplateEmail email) {
        SendingStatus result = SendingStatus.sent;
        SendResponse[] responses = mandrillService.sendTemplateEmail(email);

        List<String> invalidEmails = Lists.newArrayList();
        List<String> rejectedEmails = Lists.newArrayList();
        for (SendResponse resp : responses) {
            if (resp.getStatus() == SendingStatus.invalid) {
                invalidEmails.add(resp.getEmailAddress());
            } else if (resp.getStatus() == SendingStatus.rejected) {
                rejectedEmails.add(resp.getEmailAddress() + " - " + resp.getRejectReason());
            }
        }

        StringBuilder errorMessage = new StringBuilder();
        if (!invalidEmails.isEmpty()) {
            errorMessage.append("invalid email addresses: ");
            errorMessage.append(invalidEmails.stream().collect(Collectors.joining(", ")));
        }
        if (!rejectedEmails.isEmpty()) {
            errorMessage.append("rejected emails: ");
            errorMessage.append(rejectedEmails.stream().collect(Collectors.joining(", ")));
        }
        if (errorMessage.length() > 0) {
            errorMessage.insert(0, "Errors during sending emails occurerd: ");
            throw new IllegalStateException(errorMessage.toString());
        }
        return result;
    }

    @Override
    public SendingStatus sendTemplateDeliveryInfoEmailsByMandrill(List<String> emails, List<String> productNumbers) {
        ContentItem productsItem = prepareDeliveriesList(productNumbers);
        TemplateEmail email = new TemplateEmail();
        email.setApiKey(apiKey);
        email.setTemplateName(getRandomTemplate(deliveryTemplateEmail));
        email.setContentItems(Lists.newArrayList(productsItem));

        List<Recipient> recipientsList = emails.stream().map(Recipient::new).collect(Collectors.toList());
        email.getMessage().setRecipients(recipientsList);

        return sendTemplateEmailByMandrill(email);
    }

    @Override
    public SendingStatus sendTemplateDeliveryInfoEmailsBySendinblue(List<String> emails, List<String> deliveries) {
        Mailin http = new Mailin("https://api.sendinblue.com/v2.0", apiKey);
        Map<String, String> attr = Maps.newHashMap();
        attr.put("DELIVERIES", prepareDeliveriesList(deliveries).getContent());

        Map<String, Object> data = Maps.newHashMap();
        data.put("id", getRandomTemplate(deliveryTemplateEmail));
        data.put("to", emails.stream().collect(Collectors.joining("|")));
        data.put("attr", attr);
        http.send_transactional_template(data);

        return SendingStatus.sent;
    }

    private ContentItem prepareDeliveriesList(List<String> deliveries) {
        StringBuilder content = new StringBuilder();
        content.append("<ul>");
        content.append(deliveries.stream().map(delivery -> "<li>" + delivery + "</li>").collect(Collectors.joining()));
        content.append("</ul>");
        return new ContentItem("deliveries", content.toString());
    }

    private String getRandomTemplate(final String templates) {
        if (!StringUtils.isEmpty(templates)) {
            String[] splittedTemplates = templates.split(",");

            int randomTemplateIndex = (int) (Math.random() * splittedTemplates.length);
            return splittedTemplates[randomTemplateIndex];
        }
        return StringUtils.EMPTY;
    }
}
