package com.qcadoo.mes.technologies.export;

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
import java.util.Objects;

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

@Service
public class TechnologyExportService {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyExportService.class);

    private static final String BACKSLASH = "\"";

    private static final String NEWLINE = "\n";

    private static final String SEMICOLON = ";";

    public static final String EMPTY = "";

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

    public void exportTechnologies() {
        Date exportDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss", LocaleContextHolder.getLocale());
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

            createHeader(bufferedWriter);

            createRows(technologies, exportDate, bufferedWriter);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
        LOG.info("The file exported successfully.");
    }

    private void createRows(List<Entity> technologies, String exportDate, BufferedWriter bufferedWriter) throws IOException {
        for (Entity technology : technologies) {
            EntityTree productStructureTree = productStructureTreeService.generateProductStructureTree(null, technology);
            String technologyNumber = normalizeString(technology.getStringField(TechnologyFields.NUMBER));
            String technologyName = normalizeString(technology.getStringField(TechnologyFields.NAME));
            String technologyState  = translationService.translate("technologies.technology.state.value."+technology.getStringField(TechnologyFields.STATE),
                    LocaleContextHolder.getLocale());
            String isDefaultTechnology = defaultTechnologyToString(technology.getBooleanField(TechnologyFields.MASTER));
            String technologyStandardPerformance = numberService.format(technology
                    .getDecimalField(TechnologyFields.STANDARD_PERFORMANCE_TECHNOLOGY));
            String technologyStateChange = DateFormat.getDateInstance().format(
                    productStructureTreeService.getLastTechnologyStateChange(technology).getDateField(
                            TechnologyStateChangeFields.DATE_AND_TIME));
            String technologyAcceptStateChange = getTechnologyAcceptStateChange(technology);
            String technologyOutdatedStateChange = getTechnologyOutdatedStateChange(technology);
            String technologyProduct = normalizeString(technology.getBelongsToField(TechnologyFields.PRODUCT).getStringField(
                    ProductFields.NUMBER));
            for (Entity productNode : productStructureTree) {
                if (ProductStructureTreeService.L_MATERIAL.equals(productNode
                        .getStringField(ProductStructureTreeNodeFields.ENTITY_TYPE))) {
                    bufferedWriter.append(BACKSLASH).append(technologyNumber).append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(technologyName).append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(technologyState).append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(isDefaultTechnology).append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(technologyStandardPerformance).append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(technologyStateChange).append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(technologyAcceptStateChange).append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(technologyOutdatedStateChange).append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(technologyProduct).append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    Entity material = productNode.getBelongsToField(ProductStructureTreeNodeFields.PRODUCT);
                    bufferedWriter.append(BACKSLASH).append(normalizeString(material.getStringField(ProductFields.NUMBER)))
                            .append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(normalizeString(material.getStringField(ProductFields.NAME)))
                            .append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH)
                            .append(numberService.format(productNode.getDecimalField(ProductStructureTreeNodeFields.QUANTITY)))
                            .append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(normalizeString(material.getStringField(ProductFields.UNIT)))
                            .append(BACKSLASH);
                    bufferedWriter.append(exportedCsvSeparator);
                    bufferedWriter.append(BACKSLASH).append(exportDate).append(BACKSLASH);

                    bufferedWriter.append(NEWLINE);
                }
            }
        }
    }

    private String getTechnologyOutdatedStateChange(Entity technology) {
        Entity stateChange = productStructureTreeService.getTechnologyOutdatedStateChange(technology);
        if(Objects.isNull(stateChange)) {
            return EMPTY;
        }
        return DateFormat.getDateInstance().format(stateChange.getDateField(TechnologyStateChangeFields.DATE_AND_TIME));
    }

    private String getTechnologyAcceptStateChange(Entity technology) {
        Entity stateChange = productStructureTreeService.getTechnologyAcceptStateChange(technology);
        if(Objects.isNull(stateChange)) {
            return EMPTY;
        }
        return DateFormat.getDateInstance().format(stateChange.getDateField(TechnologyStateChangeFields.DATE_AND_TIME));
    }

    private String defaultTechnologyToString(Boolean master) {
        if(master) {
            return translationService.translate("technologies.technology.master.yes",
                    LocaleContextHolder.getLocale());
        } else {
            return translationService.translate("technologies.technology.master.no",
                    LocaleContextHolder.getLocale());
        }
    }

    private void createHeader(BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.technology.number",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.technology.name",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.technology.state",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.technology.default",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate(
                        "technologies.exportTechnologies.csv.technology.standardPerformance", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate(
                        "technologies.exportTechnologies.csv.technology.lastStateChangeDate", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate(
                        "technologies.exportTechnologies.csv.technology.technologyAcceptDate", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate(
                        "technologies.exportTechnologies.csv.technology.technologyOutdateDate", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.technology.product",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.product.number",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.product.name",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.product.quantity",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.product.unit",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH)
                .append(normalizeString(translationService.translate("technologies.exportTechnologies.csv.export.date",
                        LocaleContextHolder.getLocale()))).append(BACKSLASH);

        bufferedWriter.append(NEWLINE);
    }

    private void sendFileToFtp(String remoteFileName, File localFile) {
        FTPClient ftpClient = new FTPClient();
        try (InputStream inputStream = new FileInputStream(localFile)) {
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean done = ftpClient.changeWorkingDirectory(workingDir);
            if (done) {
                LOG.info("Successfully changed working directory.");

                LOG.info("Start uploading file: " + remoteFileName);
                done = ftpClient.storeFile(remoteFileName, inputStream);
                LOG.info("FTP reply code: " + ftpClient.getReplyCode());
                LOG.info("FTP reply message: " + ftpClient.getReplyString());
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
            return string.replaceAll(BACKSLASH, "\\\"").replaceAll(NEWLINE, " ").replace(SEMICOLON, " ");
        } else {
            return "";
        }
    }
}
