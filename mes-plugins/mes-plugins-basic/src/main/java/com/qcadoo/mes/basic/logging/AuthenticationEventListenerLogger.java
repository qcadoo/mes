package com.qcadoo.mes.basic.logging;

import com.beust.jcommander.internal.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
final class AuthenticationEventListenerLogger implements ApplicationListener<AbstractAuthenticationEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(final AbstractAuthenticationEvent event) {
        if (logger.isInfoEnabled()) {
            List<String> messageStatements = Lists.newArrayList();

            if (event instanceof AbstractAuthenticationFailureEvent) {
                messageStatements.add("FAILED TO LOG IN.");
            } else if (event instanceof AuthenticationSuccessEvent) {
                messageStatements.add("SUCCESSFULLY LOGGED IN.");
            } else {
                return;
            }

            messageStatements.add("Event details: " + event.toString());

            logger.info(String.join(" ", messageStatements));
        }
    }

}
