package com.qcadoo.mes.technologies.export;

import com.google.common.collect.Lists;
import com.qcadoo.localization.api.TranslationService;
import com.qcadoo.mes.basic.constants.ProductFields;
import com.qcadoo.mes.technologies.ProductQuantitiesWithComponentsService;
import com.qcadoo.mes.technologies.TechnologyService;
import com.qcadoo.mes.technologies.constants.MrpAlgorithm;
import com.qcadoo.mes.technologies.constants.TechnologiesConstants;
import com.qcadoo.mes.technologies.constants.TechnologyFields;
import com.qcadoo.mes.technologies.dto.OperationProductComponentHolder;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateChangeFields;
import com.qcadoo.mes.technologies.states.constants.TechnologyStateStringValues;
import com.qcadoo.mes.technologies.tree.ProductStructureTreeService;
import com.qcadoo.model.api.DataDefinition;
import com.qcadoo.model.api.DataDefinitionService;
import com.qcadoo.model.api.Entity;
import com.qcadoo.model.api.NumberService;
import com.qcadoo.model.api.file.FileService;
import com.qcadoo.model.api.search.SearchOrders;
import com.qcadoo.model.api.search.SearchRestrictions;
import com.qcadoo.tenant.api.MultiTenantCallback;
import com.qcadoo.tenant.api.MultiTenantService;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TechnologyExportService {

    private static final Logger LOG = LoggerFactory.getLogger(TechnologyExportService.class);

    private static final String BACKSLASH = "\"";

    private static final String NEWLINE = "\n";

    private static final String SEMICOLON = ";";

    private static final String EMPTY = "";

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
    private MultiTenantService multiTenantService;

    @Autowired
    private DataDefinitionService dataDefinitionService;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private NumberService numberService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ProductStructureTreeService productStructureTreeService;

    @Autowired
    private ProductQuantitiesWithComponentsService productQuantitiesWithComponentsService;

    @Autowired
    private TechnologyService technologyService;

    public void exportTechnologiesTrigger() {
        multiTenantService.doInMultiTenantContext(() -> {
            if (StringUtils.hasText(server)) {
                exportTechnologies();
            }
        });
    }

    public void exportTechnologies() {
        Date exportDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss", LocaleContextHolder.getLocale());
        String dateWithTime = dateFormat.format(exportDate);
        String date = DateFormat.getDateInstance().format(exportDate);

        LOG.info("---------------------------------- START ACCEPTED ---------------------------------------------");
        String acceptedTechnologiesFileName = "technologie_aktualne_" + dateWithTime + ".csv";
        List<Entity> acceptedTechnologies = findAcceptedTechnologies();
        List<TechnologyExportEntry> acceptedTechnologyEntries = prepareEntriesForTechnologies(acceptedTechnologies);

        exportTechnologiesToFile(date, acceptedTechnologiesFileName, acceptedTechnologyEntries);
        LOG.info("---------------------------------- FINISH ACCEPTED ---------------------------------------------");

        LOG.info("------------------------------------ START ALL -------------------------------------------------");

        String allTechnologiesFileName = "technologie_pelna_kopia_" + dateWithTime + ".csv";
        List<Entity> notAcceptedTechnologies = findNotAcceptedTechnologies();
        List<TechnologyExportEntry> allTechnologiesEntries = prepareEntriesForTechnologies(notAcceptedTechnologies);
        allTechnologiesEntries.addAll(acceptedTechnologyEntries);
        allTechnologiesEntries.sort(Comparator.comparing(TechnologyExportEntry::getTechnologyNumber));
        exportTechnologiesToFile(date, allTechnologiesFileName, allTechnologiesEntries);
        LOG.info("-------------------------------------- FINISH ALL ----------------------------------------------");

    }

    private void exportTechnologiesToFile(final String exportDate, final String fileName, final  List<TechnologyExportEntry> entries) {
        File exportFile = fileService.createExportFile(fileName);

        exportToFile(exportFile, entries, exportDate);
        LOG.info("File created : " + exportFile.getPath());
        sendFileToFtp(fileName, exportFile);
        LOG.info("File sent : " + exportFile.getPath());

        fileService.remove(exportFile.getPath());
    }

    private List<TechnologyExportEntry> prepareEntriesForTechnologies(final List<Entity> technologies) {
        List<TechnologyExportEntry> entries = Lists.newArrayList();

        for (Entity technology : technologies) {

            String technologyNumber = normalizeString(technology.getStringField(TechnologyFields.NUMBER));
            String technologyName = normalizeString(technology.getStringField(TechnologyFields.NAME));
            String technologyState = translationService.translate(
                    "technologies.technology.state.value." + technology.getStringField(TechnologyFields.STATE),
                    LocaleContextHolder.getLocale());
            String isDefaultTechnology = defaultTechnologyToString(technology.getBooleanField(TechnologyFields.MASTER));
            String standardPerformance = numberService
                    .format(technologyService.getStandardPerformance(technology).orElse(null));
            String technologyStateChange = DateFormat.getDateInstance().format(productStructureTreeService
                    .getLastTechnologyStateChange(technology).getDateField(TechnologyStateChangeFields.DATE_AND_TIME));
            String technologyAcceptStateChange = getTechnologyAcceptStateChange(technology);
            String technologyOutdatedStateChange = getTechnologyOutdatedStateChange(technology);
            String technologyProduct = normalizeString(
                    technology.getBelongsToField(TechnologyFields.PRODUCT).getStringField(ProductFields.NUMBER));

            Map<OperationProductComponentHolder, BigDecimal> materialQuantitiesByOPC = productQuantitiesWithComponentsService
                    .getNeededProductQuantitiesByOPC(technology, BigDecimal.ONE, MrpAlgorithm.ONLY_MATERIALS);

            for (Map.Entry<OperationProductComponentHolder, BigDecimal> neededProductQuantity : materialQuantitiesByOPC
                    .entrySet()) {
                Entity material = neededProductQuantity.getKey().getProduct();

                TechnologyExportEntry entry = new TechnologyExportEntry();
                entry.setTechnologyNumber(technologyNumber);
                entry.setTechnologyName(technologyName);
                entry.setTechnologyState(technologyState);
                entry.setIsDefaultTechnology(isDefaultTechnology);
                entry.setStandardPerformance(standardPerformance);
                entry.setTechnologyStateChange(technologyStateChange);
                entry.setTechnologyAcceptStateChange(technologyAcceptStateChange);
                entry.setTechnologyOutdatedStateChange(technologyOutdatedStateChange);
                entry.setTechnologyProduct(technologyProduct);
                entry.setMaterialNumber(normalizeString(material.getStringField(ProductFields.NUMBER)));
                entry.setMaterialName(normalizeString(material.getStringField(ProductFields.NAME)));
                entry.setMaterialNeededQuantity(numberService.format(neededProductQuantity.getValue()));
                entry.setMaterialUnit(normalizeString(material.getStringField(ProductFields.UNIT)));

                entries.add(entry);
            }
        }


        return entries;
    }

    private void exportToFile(final File file, List<TechnologyExportEntry> entries, final String exportDate) {
        LOG.info("Start export file: " + file.getName());

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(239);
            fileOutputStream.write(187);
            fileOutputStream.write(191);

            try (BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8))) {
                createHeader(bufferedWriter);

                createRows(entries, exportDate, bufferedWriter);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        LOG.info("The file exported successfully.");
    }

    private void createRows(List<TechnologyExportEntry> entries, final String exportDate, final BufferedWriter bufferedWriter)
            throws IOException {
        for (TechnologyExportEntry entry : entries) {
                bufferedWriter.append(BACKSLASH).append(entry.getTechnologyNumber()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getTechnologyName()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getTechnologyState()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getIsDefaultTechnology()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getStandardPerformance()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getTechnologyStateChange()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getTechnologyAcceptStateChange()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getTechnologyOutdatedStateChange()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getTechnologyProduct()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getMaterialNumber())
                        .append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getMaterialName())
                        .append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getMaterialNeededQuantity()).append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(entry.getMaterialUnit())
                        .append(BACKSLASH);
                bufferedWriter.append(exportedCsvSeparator);
                bufferedWriter.append(BACKSLASH).append(exportDate).append(BACKSLASH);

                bufferedWriter.append(NEWLINE);
        }
    }

    private String getTechnologyOutdatedStateChange(final Entity technology) {
        Entity stateChange = productStructureTreeService.getTechnologyOutdatedStateChange(technology);

        if (Objects.isNull(stateChange)) {
            return EMPTY;
        }

        return DateFormat.getDateInstance().format(stateChange.getDateField(TechnologyStateChangeFields.DATE_AND_TIME));
    }

    private String getTechnologyAcceptStateChange(final Entity technology) {
        Entity stateChange = productStructureTreeService.getTechnologyAcceptStateChange(technology);

        if (Objects.isNull(stateChange)) {
            return EMPTY;
        }

        return DateFormat.getDateInstance().format(stateChange.getDateField(TechnologyStateChangeFields.DATE_AND_TIME));
    }

    private String defaultTechnologyToString(final Boolean master) {
        if (master) {
            return translationService.translate("technologies.technology.master.yes", LocaleContextHolder.getLocale());
        } else {
            return translationService.translate("technologies.technology.master.no", LocaleContextHolder.getLocale());
        }
    }

    private void createHeader(final BufferedWriter bufferedWriter) throws IOException {
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.technology.number", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.technology.name", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.technology.state", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.technology.default", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter.append(BACKSLASH)
                .append(normalizeString(translationService.translate(
                        "technologies.exportTechnologies.csv.technology.standardPerformance", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter.append(BACKSLASH)
                .append(normalizeString(translationService.translate(
                        "technologies.exportTechnologies.csv.technology.lastStateChangeDate", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter.append(BACKSLASH)
                .append(normalizeString(translationService.translate(
                        "technologies.exportTechnologies.csv.technology.technologyAcceptDate", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter.append(BACKSLASH)
                .append(normalizeString(translationService.translate(
                        "technologies.exportTechnologies.csv.technology.technologyOutdateDate", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.technology.product", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.product.number", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.product.name", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.product.quantity", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.product.unit", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);
        bufferedWriter.append(exportedCsvSeparator);
        bufferedWriter
                .append(BACKSLASH).append(normalizeString(translationService
                        .translate("technologies.exportTechnologies.csv.export.date", LocaleContextHolder.getLocale())))
                .append(BACKSLASH);

        bufferedWriter.append(NEWLINE);
    }

    private void sendFileToFtp(final String remoteFileName, final File localFile) {
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

    private List<Entity> findAcceptedTechnologies() {
        return getTechnologyDD().find()
                .add(SearchRestrictions.eq(TechnologyFields.STATE, TechnologyStateStringValues.ACCEPTED))
                .add(SearchRestrictions.eq(TechnologyFields.ACTIVE, true))
                .addOrder(SearchOrders.asc(TechnologyFields.NUMBER))
                .list().getEntities();
    }

    private List<Entity> findNotAcceptedTechnologies() {
        return getTechnologyDD().find()
                .add(SearchRestrictions.ne(TechnologyFields.STATE, TechnologyStateStringValues.ACCEPTED))
                .add(SearchRestrictions.eq(TechnologyFields.ACTIVE, true))
                .addOrder(SearchOrders.asc(TechnologyFields.NUMBER))
                .list().getEntities();
    }

    private DataDefinition getTechnologyDD() {
        return dataDefinitionService.get(TechnologiesConstants.PLUGIN_IDENTIFIER, TechnologiesConstants.MODEL_TECHNOLOGY);
    }

}
