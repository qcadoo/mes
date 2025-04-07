package com.qcadoo.mes.materialFlowResources.hooks;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.materialFlowResources.constants.PalletStorageStateDtoFields;
import com.qcadoo.view.api.ViewDefinitionState;
import com.qcadoo.view.api.components.FieldComponent;
import com.qcadoo.view.api.components.GridComponent;
import com.qcadoo.view.constants.QcadooViewConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.ArrayUtils.contains;
import static org.apache.commons.lang3.ArrayUtils.indexOf;

@Service
public class PalletStorageStateDetailsHooks {

    @Autowired
    private TranslationService translationService;

    public void setupHeaderLabel(final ViewDefinitionState view) {
        String[] descriminatorFiltersFields = new String[] { PalletStorageStateDtoFields.PALLET_NUMBER,
                PalletStorageStateDtoFields.TYPE_OF_LOAD_UNIT, PalletStorageStateDtoFields.LOCATION_NUMBER,
                PalletStorageStateDtoFields.STORAGE_LOCATION_NUMBER };

        Map<String, String> filters = ((GridComponent) view.getComponentByReference(QcadooViewConstants.L_GRID)).getFilters();

        String headerText = filters.entrySet().stream()
                .filter(fe -> contains(descriminatorFiltersFields, fe.getKey()) && !fe.getValue().equals("ISNULL"))
                .sorted(comparing((fe) -> indexOf(descriminatorFiltersFields, fe.getKey())))
                .map(fe -> fe.getValue().replaceAll("[\\[\\]]", "")).collect(Collectors.joining(", "));

        FieldComponent headerLabel = (FieldComponent) view.getComponentByReference("palletStorageDetailsHeaderLabel");
        String headerLabelText = translationService.translate(
                "materialFlowResources.palletStorageStateDetails.window.mainTab.headerLabel", LocaleContextHolder.getLocale());
        headerLabel.setFieldValue(headerLabelText + " " + headerText);
        headerLabel.requestComponentUpdateState();
    }

}
