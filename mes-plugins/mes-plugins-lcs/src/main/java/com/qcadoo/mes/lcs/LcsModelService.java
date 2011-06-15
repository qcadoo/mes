package com.qcadoo.mes.lcs;

import org.springframework.stereotype.Service;

import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.Entity;

@Service
public class LcsModelService {

    public void updateLcsNumbers(final DataDefinition dataDefinition, final Entity entity) {
        int translationsNumber = entity.getHasManyField("translations").size();
        int workstandsNumber = entity.getHasManyField("workstands").size();
        entity.setField("numberOfTranslations", translationsNumber);
        entity.setField("numberOfWorkstands", workstandsNumber);
    }

    public boolean validatorWorkstandNameUniqe(final DataDefinition dataDefinition, final Entity entity) {
        String name = entity.getStringField("name");
        for (Entity existingWorkstand : entity.getBelongsToField("lcs").getHasManyField("workstands")) {
            if (!existingWorkstand.getId().equals(entity.getId()) && existingWorkstand.getStringField("name").equals(name)) {
                entity.addError(dataDefinition.getField("name"), "lcs.workstands.nameExists");
                return false;
            }
        }
        return true;
    }

    public boolean validatorTranslationUniqe(final DataDefinition dataDefinition, final Entity entity) {
        Long translationId = entity.getBelongsToField("translation").getId();
        for (Entity existingTranslation : entity.getBelongsToField("lcs").getHasManyField("translations")) {
            if (!existingTranslation.getId().equals(entity.getId())
                    && existingTranslation.getBelongsToField("translation").getId().equals(translationId)) {
                entity.addError(dataDefinition.getField("translation"), "lcs.lcsTranslation.translationExists");
                return false;
            }
        }
        return true;
    }
}
