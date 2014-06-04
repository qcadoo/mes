/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 1.3
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
package com.qcadoo.mes.states.messages.util;

import static com.qcadoo.mes.states.messages.constants.MessageFields.AUTO_CLOSE;
import static com.qcadoo.mes.states.messages.constants.MessageFields.CORRESPOND_FIELD_NAME;
import static com.qcadoo.mes.states.messages.constants.MessageFields.TRANSLATION_ARGS;
import static com.qcadoo.mes.states.messages.constants.MessageFields.TRANSLATION_KEY;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.StateMessageType;
import com.qcadoo.model.api.Entity;
import com.qcadoo.view.api.ComponentState.MessageType;

/**
 * Util class for {@link StateMessageType}
 * 
 * @since 1.1.7
 */
public final class MessagesUtil {

    /**
     * String using as a separator during join/split arguments.
     */
    public static final String ARGS_SEPARATOR = "@#@";

    private MessagesUtil() {
    }

    /**
     * Join given arguments using separator specified in {@link MessagesUtil#ARGS_SEPARATOR}.
     * 
     * @param splittedArgs
     *            array of translation arguments
     * @return joined string containing all splitterArgs elements, separated by {@link MessagesUtil#ARGS_SEPARATOR}
     */
    public static String joinArgs(final String[] splittedArgs) {
        if (ArrayUtils.isEmpty(splittedArgs)) {
            return null;
        }
        return StringUtils.join(splittedArgs, ARGS_SEPARATOR);
    }

    /**
     * Split given string using separator specified in {@link MessagesUtil#ARGS_SEPARATOR}
     * 
     * @param joinedArgs
     *            string which contains translation arguments separated by {@link MessagesUtil#ARGS_SEPARATOR}
     * @return array of strings representing translation arguments
     */
    public static String[] splitArgs(final String joinedArgs) {
        if (StringUtils.isBlank(joinedArgs)) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return joinedArgs.split(ARGS_SEPARATOR);
    }

    /**
     * Check if given list of message entity contains at least one message with type set to {@link StateMessageType#FAILURE}
     * 
     * @param messages
     *            list of message entities
     * @return true if given list contain at least one message with type set to {@link StateMessageType#FAILURE} or false
     *         otherwise.
     */
    public static boolean hasFailureMessages(final List<Entity> messages) {
        return hasMessagesOfType(messages, StateMessageType.FAILURE);
    }

    /**
     * Check if given list of message entity contains at least one message with type set to
     * {@link StateMessageType#VALIDATION_ERROR}
     * 
     * @param messages
     *            list of message entities
     * @return true if given list contain at least one message with type set to {@link StateMessageType#VALIDATION_ERROR} or false
     *         otherwise.
     */
    public static boolean hasValidationErrorMessages(final List<Entity> messages) {
        return hasMessagesOfType(messages, StateMessageType.VALIDATION_ERROR);
    }

    private static boolean hasMessagesOfType(final List<Entity> messages, final StateMessageType typeLookingFor) {
        if (messages == null) {
            return false;
        }
        for (Entity message : messages) {
            if (messageIsTypeOf(message, typeLookingFor)) {
                return true;
            }
        }
        return false;
    }

    public static boolean messageIsTypeOf(final Entity message, final StateMessageType typeLookingFor) {
        final String messageStringType = message.getStringField(MessageFields.TYPE);
        final StateMessageType messageType = StateMessageType.parseString(messageStringType);
        return messageType.equals(typeLookingFor);
    }

    public static boolean hasCorrespondField(final Entity message) {
        return StringUtils.isNotBlank(getCorrespondFieldName(message));
    }

    /**
     * Convert {@link StateMessageType} to appropriate {@link com.qcadoo.view.api.ComponentState.MessageType}
     * 
     * @param type
     *            {@link StateMessageType}
     * @return appropriate {@link com.qcadoo.view.api.ComponentState.MessageType}
     */
    public static MessageType convertViewMessageType(final StateMessageType type) {
        MessageType convertedType = null;
        switch (type) {
            case SUCCESS:
                convertedType = MessageType.SUCCESS;
                break;
            case FAILURE:
            case VALIDATION_ERROR:
                convertedType = MessageType.FAILURE;
                break;
            case INFO:
            default:
                convertedType = MessageType.INFO;
                break;
        }
        return convertedType;
    }

    public static String getKey(final Entity message) {
        return message.getStringField(TRANSLATION_KEY);
    }

    public static String[] getArgs(final Entity message) {
        return splitArgs(message.getStringField(TRANSLATION_ARGS));
    }

    public static String getCorrespondFieldName(final Entity message) {
        return message.getStringField(CORRESPOND_FIELD_NAME);
    }

    public static boolean isAutoClosed(final Entity message) {
        return message.getField(AUTO_CLOSE) == null || message.getBooleanField(AUTO_CLOSE);
    }

}
