package com.qcadoo.mes.states.messages.util;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.Entity;

/**
 * Util class for {@link MessageType}
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
     * Check if given list of message entity contains at least one message with type set to {@link MessageType#FAILURE}
     * 
     * @param messages
     *            list of message entities
     * @return true if given list contain at least one message with type set to {@link MessageType#FAILURE} or false otherwise.
     */
    public static boolean hasFailureMessages(final List<Entity> messages) {
        return hasMessagesOfType(messages, MessageType.FAILURE);
    }

    private static boolean hasMessagesOfType(final List<Entity> messages, final MessageType typeLookingFor) {
        if (messages == null) {
            return false;
        }
        for (Entity message : messages) {
            String messageStringType = message.getStringField(MessageFields.TYPE);
            MessageType messageType = MessageType.parseString(messageStringType);
            if (messageType.equals(typeLookingFor)) {
                return true;
            }
        }
        return false;
    }
}
