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
package com.qcadoo.mes.emailNotifications.notifications.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qcadoo.mes.emailNotifications.notifications.constants.SendingStatus;
import com.qcadoo.mes.emailNotifications.notifications.mandrill.ContentItem;
import com.qcadoo.mes.emailNotifications.notifications.mandrill.Recipient;
import com.qcadoo.mes.emailNotifications.notifications.mandrill.TemplateEmail;
import com.qcadoo.mes.emailNotifications.notifications.service.MailingService;
import com.qcadoo.mes.emailNotifications.notifications.service.MandrillService;
import com.qcadoo.mes.emailNotifications.notifications.service.SendinblueService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sibModel.SendEmail;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MailingServiceImpl implements MailingService {

    @Value("${deliveryTemplateEmail}")
    private String deliveryTemplateEmail;

    @Autowired
    private MandrillService mandrillService;

    @Autowired
    private SendinblueService sendinblueService;

    @Override
    public SendingStatus sendTemplateDeliveryInfoEmailsByMandrill(final List<String> emails, final List<String> deliveries) {
        TemplateEmail email = new TemplateEmail();

        email.setContentItems(Lists.newArrayList(prepareDeliveriesList(deliveries)));
        email.setTemplateName(getRandomTemplate(deliveryTemplateEmail));

        List<Recipient> recipientsList = emails.stream().map(Recipient::new).collect(Collectors.toList());

        email.getMessage().setRecipients(recipientsList);

        return mandrillService.sendTemplateEmail(email);
    }

    @Override
    public SendingStatus sendTemplateDeliveryInfoEmailsBySendinblue(final List<String> emails, final List<String> deliveries) {
        SendEmail sendEmail = new SendEmail();

        Map<String, String> attributes = Maps.newHashMap();
        attributes.put("DELIVERIES", prepareDeliveriesList(deliveries).getContent());

        sendEmail.emailTo(emails);
        sendEmail.attributes(attributes);

        return sendinblueService.sendTemplateEmail(Long.valueOf(getRandomTemplate(deliveryTemplateEmail)), sendEmail);
    }

    private ContentItem prepareDeliveriesList(final List<String> deliveries) {
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
