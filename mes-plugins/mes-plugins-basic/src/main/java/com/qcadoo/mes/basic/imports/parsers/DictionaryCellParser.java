package com.qcadoo.mes.basic.imports.parsers;

import static com.qcadoo.model.api.search.SearchRestrictions.and;
import static com.qcadoo.model.api.search.SearchRestrictions.belongsTo;
import static com.qcadoo.model.api.search.SearchRestrictions.eq;

import java.util.Objects;
import java.util.function.Consumer;

import com.qcadoo.mes.basic.imports.helpers.CellErrorsAccessor;
import com.qcadoo.mes.basic.imports.helpers.CellParser;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.constants.DictionaryFields;
import com.qcadoo.model.constants.DictionaryItemFields;
import com.qcadoo.model.constants.QcadooModelConstants;

public class DictionaryCellParser implements CellParser {

    private static final String L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_DICTIONARY_ITEM = "qcadooView.validate.field.error.invalidDictionaryItem";

    private static final String L_BASIC_IMPORT_ERROR_FIELD_INACTIVE_DICTIONARY_ITEM = "basic.import.error.field.inactiveDictionaryItem";

    private String dictionaryName;

    private DataDefinitionService dataDefinitionService;

    private DictionaryCellParser() {
        this.dataDefinitionService = null;
        this.dictionaryName = null;
    }

    public DictionaryCellParser(final DataDefinitionService dataDefinitionService, final String dictionaryName) {
        this.dataDefinitionService = dataDefinitionService;
        this.dictionaryName = dictionaryName;
    }

    @Override
    public void parse(final String cellValue, final String dependentCellValue, final CellErrorsAccessor errorsAccessor,
            final Consumer<Object> valueConsumer) {
        Entity dictionaryItem = getDictionaryItemByName(cellValue);

        if (Objects.isNull(dictionaryItem)) {
            errorsAccessor.addError(L_QCADOO_VIEW_VALIDATE_FIELD_ERROR_INVALID_DICTIONARY_ITEM);
        } else if (!dictionaryItem.isActive()) {
            errorsAccessor.addError(L_BASIC_IMPORT_ERROR_FIELD_INACTIVE_DICTIONARY_ITEM);
        } else {
            valueConsumer.accept(cellValue);
        }
    }

    private Entity getDictionaryItemByName(final String name) {
        return getDictionaryItemDD().find()
                .add(and(eq(DictionaryItemFields.NAME, name), belongsTo(DictionaryItemFields.DICTIONARY, getDictionaryByName())))
                .setMaxResults(1).uniqueResult();
    }

    private Entity getDictionaryByName() {
        return getDictionaryDD().find().add(eq(DictionaryFields.NAME, dictionaryName)).setMaxResults(1).uniqueResult();
    }

    private DataDefinition getDictionaryDD() {
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY);
    }

    private DataDefinition getDictionaryItemDD() {
        return dataDefinitionService.get(QcadooModelConstants.PLUGIN_IDENTIFIER, QcadooModelConstants.MODEL_DICTIONARY_ITEM);
    }

}