/**
 * ***************************************************************************
 * Copyright (c) 2010 Qcadoo Limited
 * Project: Qcadoo MES
 * Version: 0.2.0
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

package com.qcadoo.mes.products.print.xls;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qcadoo.mes.api.Entity;
import com.qcadoo.mes.products.print.DocumentService;
import com.qcadoo.mes.utils.xls.XlsUtil;

@Service
public abstract class XlsDocumentService extends DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(XlsDocumentService.class);

    @Override
    public final void generateDocument(final Entity entity, final Locale locale) throws IOException {
        setDecimalFormat((DecimalFormat) DecimalFormat.getInstance(locale));
        getDecimalFormat().setMaximumFractionDigits(3);
        getDecimalFormat().setMinimumFractionDigits(3);
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(getReportTitle(locale));
        addHeader(sheet, locale);
        addSeries(sheet, entity);
        sheet.setZoom(4, 3);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream((String) entity.getField("fileName") + getSuffix() + XlsUtil.XLS_EXTENSION);
            workbook.write(outputStream);
        } catch (IOException e) {
            LOG.error("Problem with generating document - " + e.getMessage());
            if (outputStream != null) {
                outputStream.close();
            }
            throw e;
        }
        outputStream.close();
    }

    protected abstract void addHeader(final HSSFSheet sheet, final Locale locale);

    protected abstract void addSeries(final HSSFSheet sheet, final Entity entity);

}
