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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.qcadoo.mes.deliveriesMinState.notifications.mandrill.SendResponse;
import com.qcadoo.mes.deliveriesMinState.notifications.mandrill.TemplateEmail;
import com.qcadoo.mes.deliveriesMinState.notifications.service.MandrillService;

@Service
public class MandrillServiceImpl implements MandrillService {

    private static final String SEND_TEMPLATE = "messages/send-template.json";

    private static final String MANDRILL_ADDRESS = "https://mandrillapp.com/api/1.0/";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public SendResponse[] sendTemplateEmail(TemplateEmail templateEmail) {
        StringBuilder url = new StringBuilder(MANDRILL_ADDRESS).append(SEND_TEMPLATE);

        ResponseEntity<SendResponse[]> response = restTemplate.postForEntity(url.toString(), templateEmail, SendResponse[].class);
        if (response.getStatusCode().compareTo(HttpStatus.OK) == 0) {
            if (response.getBody().length > 0) {
                return response.getBody();
            } else {
                throw new IllegalStateException("Not sent any email.");
            }
        } else {
            throw new IllegalStateException("Unable to send email, status code: " + response.getStatusCode().toString());
        }
    }
}
