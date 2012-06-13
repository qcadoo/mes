package com.qcadoo.mes.states;

import static com.qcadoo.mes.states.messages.util.MessagesUtil.joinArgs;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;

import org.mockito.Mock;

import com.qcadoo.mes.states.constants.StateChangeStatus;
import com.qcadoo.mes.states.messages.constants.MessageFields;
import com.qcadoo.mes.states.messages.constants.MessageType;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityList;

public abstract class StateChangeTest {

    @Mock
    protected Entity stateChangeEntity;

    @Mock
    protected DataDefinition stateChangeDD;

    protected static final StateChangeEntityDescriber DESCRIBER = new MockStateChangeDescriber();

    protected void stubStateChangeEntity(final StateChangeEntityDescriber describer) {
        given(stateChangeEntity.getDataDefinition()).willReturn(stateChangeDD);
        final EntityList emptyEntityList = mockEntityList(Collections.<Entity> emptyList());
        given(stateChangeEntity.getHasManyField(describer.getMessagesFieldName())).willReturn(emptyEntityList);
        stubEntityField(stateChangeEntity, describer.getStatusFieldName(), StateChangeStatus.IN_PROGRESS.getStringValue());
    }

    protected EntityList mockEntityList(final List<Entity> entities) {
        EntityList entityList = mock(EntityList.class);
        given(entityList.iterator()).willReturn(entities.iterator());
        given(entityList.isEmpty()).willReturn(entities.isEmpty());
        return entityList;
    }

    protected Entity mockMessage(final MessageType type, final String translationKey, final String... translationArgs) {
        Entity message = mock(Entity.class);
        stubEntityField(message, MessageFields.TYPE, type);
        stubEntityField(message, MessageFields.TRANSLATION_KEY, translationKey);
        stubEntityField(message, MessageFields.TRANSLATION_ARGS, joinArgs(translationArgs));
        return message;
    }

    protected static void mockStateChangeStatus(final Entity entity, final StateChangeStatus status) {
        stubEntityField(entity, DESCRIBER.getStatusFieldName(), status.getStringValue());
    }

    protected static void stubEntityField(final Entity entity, final String fieldName, final Object fieldValue) {
        given(entity.getField(fieldName)).willReturn(fieldValue);
        given(entity.getStringField(fieldName)).willReturn(fieldValue == null ? null : fieldValue.toString());
    }

}
