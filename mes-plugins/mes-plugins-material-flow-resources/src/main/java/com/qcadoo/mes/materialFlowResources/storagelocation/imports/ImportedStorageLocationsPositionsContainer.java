package com.qcadoo.mes.materialFlowResources.storagelocation.imports;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

public class ImportedStorageLocationsPositionsContainer {

    private boolean importedPositions = true;

    private final List<ImportedStorageLocationPosition> positions = Lists.newArrayList();

    public void addPosition(Row row) {
        Cell codeCell = row.getCell(StorageLocationsPositionsXlsFile.A.getColumnNumber());
        if (codeCell == null) {
            return;
        }

        DataFormatter formatter = new DataFormatter();
        String codeCellValue = formatter.formatCellValue(codeCell);
        if (StringUtils.isEmpty(codeCellValue)) {
            return;
        }

        ImportedStorageLocationPosition position = new ImportedStorageLocationPosition();

        position = position.withStorageLocation(codeCellValue);

        Cell productCell = row.getCell(StorageLocationsPositionsXlsFile.B.getColumnNumber());
        String productCellValue = formatter.formatCellValue(productCell);
        
        position = position.withProduct(productCellValue);

        positions.add(position);
    }

    public List<ImportedStorageLocationPosition> addPosition(ImportedStorageLocationPosition position) {
        positions.add(position);

        return positions;
    }

    public List<ImportedStorageLocationPosition> getPositions() {
        return positions;
    }

    public boolean isImportedPositions() {
        return importedPositions;
    }

    public void setImportedPositions(boolean importedPositions) {
        this.importedPositions = importedPositions;
    }

}
