package com.qcadoo.mes.technologies.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.constants.ProductStructureTreeNodeFields;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.EntityTree;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.tenant.api.MultiTenantCallback;
import com.qcadoo.tenant.api.MultiTenantService;
import com.qcadoo.view.api.ComponentState;
import com.qcadoo.view.api.ViewDefinitionState;

@Service
public class TechnologyExportService {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyExportService.class);

    private static final String backslash = "\"";

    private static final String newline = "\n";

    @Autowired
    private FileService fileService;

    @Value("${exportedCsvSeparator:','}")
    private String exportedCsvSeparator;

    @Value("${ftp.server}")
    private String server;

    @Value("#{'${ftp.port:21}' == '' ? 21 : '${ftp.port:21}'}")
    private int port;

    @Value("${ftp.user}")
    private String user;

    @Value("${ftp.pass}")
    private String pass;

    private String workingDir = "/qcadoo";

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private MultiTenantService multiTenantService;

    public void exportTechnologiesTrigger() {
        multiTenantService.doInMultiTenantContext(new MultiTenantCallback() {
            @Override
            public void invoke() {
                if (StringUtils.hasText(server)) {
                    exportTechnologies();
                }
            }
        });
    }

    public void exportTechnologies(final ViewDefinitionState view, final ComponentState componentState, final String[] args) {
        LOG.info("Port: " + port);
    }

    private void exportTechnologies() {
        Date exportDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", LocaleContextHolder.getLocale());
        String dateWithTime = dateFormat.format(exportDate);
        String date = DateFormat.getDateInstance().format(exportDate);

        String acceptedTechnologiesFileName = "technologie_aktualne_" + dateWithTime + ".csv";
        List<Entity> acceptedTechnologies = findAcceptedTechnologies();
        exportTechnologiesToFile(date, acceptedTechnologiesFileName, acceptedTechnologies);

        String allTechnologiesFileName = "technologie_pelna_kopia_" + dateWithTime + ".csv";
        List<Entity> allTechnologies = findAllTechnologies();
        exportTechnologiesToFile(date, allTechnologiesFileName, allTechnologies);
    }

    private void exportTechnologiesToFile(String exportDate, String fileName, List<Entity> technologies) {
        File exportFile = fileService.createExportFile(fileName);
        exportToFile(exportFile, technologies, exportDate);
        sendFileToFtp(fileName, exportFile);
        fileService.remove(exportFile.getPath());
    }

    private List<Entity> findAcceptedTechnologies() {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        return technologyDD.find().add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE))
                .add(SearchRestrictions.eq(TechnologyFields.STATE, TechnologyStateStringValues.ACCEPTED))
                .add(SearchRestrictions.eq(TechnologyFields.ACTIVE, true)).addOrder(SearchOrders.asc(TechnologyFields.NUMBER))
                .list().getEntities();
    }

    private List<Entity> findAllTechnologies() {
        DataDefinition technologyDD = dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER,
                TechnologiesConstants.MODEL_TECHNOLOGY);
        return technologyDD.find().add(SearchRestrictions.isNull(TechnologyFields.TECHNOLOGY_TYPE))
                .add(SearchRestrictions.eq(TechnologyFields.ACTIVE, true)).addOrder(SearchOrders.asc(TechnologyFields.NUMBER))
                .list().getEntities();
    }

    private void exportToFile(File file, List<Entity> technologies, String exportDate) {
        LOG.info("Start export file: " + file.getName());
        BufferedWriter bufferedWriter = null;

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            fileOutputStream.write(239);
            fileOutputStream.write(187);
            fileOutputStream.write(191);

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8")));

            bufferedWriter
                    .append(backslash).append(normalizeString(translationService
                            .translate("technologies.exportTechnologies.csv.technology.number", LocaleContextHolder.getLocale())))
                    .append(backslash);
            bufferedWriter.append(exportedCsvSeparator);
            bufferedWriter
                    .append(backslash).append(normalizeString(translationService
                            .translate("technologies.exportTechnologies.csv.technology.name", LocaleContextHolder.getLocale())))
                    .append(backslash);
            bufferedWriter.append(exportedCsvSeparator);
            bufferedWriter.append(backslash)
                    .append(normalizeString(
                            translationService.translate("technologies.exportTechnologies.csv.technology.standardPerformance",
                                    LocaleContextHolder.getLocale())))
                    .append(backslash);
            bufferedWriter.append(exportedCsvSeparator);
            bufferedWriter.append(backslash)
                    .append(normalizeString(
                            translationService.translate("technologies.exportTechnologies.csv.technology.lastStateChangeDate",
                                    LocaleContextHolder.getLocale())))
                    .append(backslash);
            bufferedWriter.append(exportedCsvSeparator);
            bufferedWriter.append(backslash).append(normalizeString(translationService
                    .translate("technologies.exportTechnologies.csv.technology.product", LocaleContextHolder.getLocale())))
                    .append(backslash);
            bufferedWriter.append(exportedCsvSeparator);
            bufferedWriter
                    .append(backslash).append(normalizeString(translationService
                            .translate("technologies.exportTechnologies.csv.product.number", LocaleContextHolder.getLocale())))
                    .append(backslash);
            bufferedWriter.append(exportedCsvSeparator);
            bufferedWriter
                    .append(backslash).append(normalizeString(translationService
                            .translate("technologies.exportTechnologies.csv.product.name", LocaleContextHolder.getLocale())))
                    .append(backslash);
            bufferedWriter.append(exportedCsvSeparator);
            bufferedWriter
                    .append(backslash).append(normalizeString(translationService
                            .translate("technologies.exportTechnologies.csv.product.quantity", LocaleContextHolder.getLocale())))
                    .append(backslash);
            bufferedWriter.append(exportedCsvSeparator);
            bufferedWriter
                    .append(backslash).append(normalizeString(translationService
                            .translate("technologies.exportTechnologies.csv.product.unit", LocaleContextHolder.getLocale())))
                    .append(backslash);
            bufferedWriter.append(exportedCsvSeparator);
            bufferedWriter
                    .append(backslash).append(normalizeString(translationService
                            .translate("technologies.exportTechnologies.csv.export.date", LocaleContextHolder.getLocale())))
                    .append(backslash);

            bufferedWriter.append(newline);

            for (Entity technology : technologies) {
                EntityTree productStructureTree = productStructureTreeService.generateProductStructureTree(null, technology);
                String technologyNumber = normalizeString(technology.getStringField(TechnologyFields.NUMBER));
                String technologyName = normalizeString(technology.getStringField(TechnologyFields.NAME));
                String technologyStandardPerformance = numberService
                        .format(technology.getDecimalField(TechnologyFields.STANDARD_PERFORMANCE_TECHNOLOGY));
                String technologyStateChange = DateFormat.getDateInstance().format(productStructureTreeService
                        .getLastTechnologyStateChange(technology).getDateField(TechnologyStateChangeFields.DATE_AND_TIME));
                String technologyProduct = normalizeString(
                        technology.getBelongsToField(TechnologyFields.PRODUCT).getStringField(ProductFields.NUMBER));
                for (Entity productNode : productStructureTree) {
                    if (ProductStructureTreeService.L_MATERIAL
                            .equals(productNode.getStringField(ProductStructureTreeNodeFields.ENTITY_TYPE))) {
                        bufferedWriter.append(backslash).append(technologyNumber).append(backslash);
                        bufferedWriter.append(exportedCsvSeparator);
                        bufferedWriter.append(backslash).append(technologyName).append(backslash);
                        bufferedWriter.append(exportedCsvSeparator);
                        bufferedWriter.append(backslash).append(technologyStandardPerformance).append(backslash);
                        bufferedWriter.append(exportedCsvSeparator);
                        bufferedWriter.append(backslash).append(technologyStateChange).append(backslash);
                        bufferedWriter.append(exportedCsvSeparator);
                        bufferedWriter.append(backslash).append(technologyProduct).append(backslash);
                        bufferedWriter.append(exportedCsvSeparator);
                        Entity material = productNode.getBelongsToField(ProductStructureTreeNodeFields.PRODUCT);
                        bufferedWriter.append(backslash).append(normalizeString(material.getStringField(ProductFields.NUMBER)))
                                .append(backslash);
                        bufferedWriter.append(exportedCsvSeparator);
                        bufferedWriter.append(backslash).append(normalizeString(material.getStringField(ProductFields.NAME)))
                                .append(backslash);
                        bufferedWriter.append(exportedCsvSeparator);
                        bufferedWriter.append(backslash)
                                .append(numberService
                                        .format(productNode.getDecimalField(ProductStructureTreeNodeFields.QUANTITY)))
                                .append(backslash);
                        bufferedWriter.append(exportedCsvSeparator);
                        bufferedWriter.append(backslash).append(normalizeString(material.getStringField(ProductFields.UNIT)))
                                .append(backslash);
                        bufferedWriter.append(exportedCsvSeparator);
                        bufferedWriter.append(backslash).append(exportDate).append(backslash);

                        bufferedWriter.append(newline);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
        LOG.info("The file exported successfully.");
    }

    private void sendFileToFtp(String remoteFileName, File localFile) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean done = ftpClient.changeWorkingDirectory(workingDir);
            if (done) {
                LOG.info("Successfully changed working directory.");
                InputStream inputStream = new FileInputStream(localFile);

                LOG.info("Start uploading file: " + remoteFileName);
                done = ftpClient.storeFile(remoteFileName, inputStream);
                inputStream.close();
                if (done) {
                    LOG.info("The file is uploaded successfully.");
                }
            } else {
                LOG.warn("Failed to change working directory.");
            }
        } catch (IOException ex) {
            LOG.error("Error: " + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                LOG.error("Error: " + ex.getMessage());
            }
        }
    }

    private String normalizeString(final String string) {
        if (StringUtils.hasText(string)) {
            return string.replaceAll(backslash, "\\\"").replaceAll(newline, " ");
        } else {
            return "";
        }
    }
}
