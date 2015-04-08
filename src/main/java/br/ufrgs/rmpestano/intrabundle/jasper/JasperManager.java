package br.ufrgs.rmpestano.intrabundle.jasper;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.metric.MetricsCalculation;
import br.ufrgs.rmpestano.intrabundle.model.*;
import br.ufrgs.rmpestano.intrabundle.model.enums.FileType;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricName;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import br.ufrgs.rmpestano.intrabundle.util.MetricUtils;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.*;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellPrompt;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

@Singleton
public class JasperManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private String   fileName;

    private String   reportName;

    private List<?>  data;

    private Map      params;

    private FileType type;

    @Inject
    Shell shell;

    @Inject
    ShellPrompt prompt;

    @Inject
    MessageProvider provider;

    @Inject
    MetricsCalculation metrics;

    @Inject
    MetricUtils metricUtils;

    @Inject
    Map<MetricName, Map<MetricScore, Double>> metricsLimit;

    /**
     * @param filename name of the generated PDF file, if not provided reportName.pdf will be used
     * @return
     */
    public JasperManager filename(String filename) {
        this.fileName = filename;
        return this;
    }

    /**
     * @param reportName name of the .jasper file
     * @return
     */
    public JasperManager reportName(String reportName) {
        this.reportName = reportName;
        return this;
    }

    public JasperManager data(List<?> data) {
        this.data = data;
        return this;
    }

    public JasperManager type(FileType type) {
        this.type = type;
        return this;
    }

    public JasperManager params(Map params) {
        this.params = params;
        return this;
    }

    /**
     * build PDF file from .jasper(located on /resources/report/filename) on current directory
     */
    public void build() {
        if (reportName == null || data == null) {
            throw new RuntimeException("reportName and data is required to generate the report");
        }
        if (fileName == null) {
            fileName = reportName;
        }

        if (type == null) {
            type = FileType.PDF;
        }

        if (params == null) {
            params = new HashMap();
        }
        String path = shell.getCurrentDirectory().getFullyQualifiedName() + "/" + fileName + "." + type.getName();
        JasperPrint jp = getJasperPrint();
        JRExporter exporter = null;
        try {
            switch (type) {
                case PDF: {
                    JasperExportManager.exportReportToPdfFile(jp, path);
                    break;
                }
                case HTML: {
                    JasperExportManager.exportReportToHtmlFile(jp, path);
                    break;
                }
                case TXT: {
                    exporter = new JRTextExporter();
                    exporter.setParameter(JRTextExporterParameter.JASPER_PRINT, jp);
                    exporter.setParameter(JRTextExporterParameter.OUTPUT_FILE_NAME, path);
                    exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, 300);
                    exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, 40);
                    exporter.exportReport();
                    break;
                }
                case EXCEL: {
                    exporter = new JRXlsExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT,
                            jp);
                    exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,
                            path);
                    exporter.exportReport();
                    break;
                }

                case CSV: {
                    exporter = new JRCsvExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT,
                            jp);
                    exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,
                            path);
                    exporter.exportReport();
                    break;
                }
                case RTF: {
                    exporter = new JRRtfExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT,
                            jp);
                    exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,
                            path);
                    exporter.exportReport();
                    break;
                }

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        shell.println(provider.getMessage("report.generated", path));
    }

    /**
     * get JasperPrint from .jasper file
     */
    public JasperPrint getJasperPrint() {
        try {
            //reports are placed under /resources/reports
            String path = "/reports/" + reportName;
            if (!path.endsWith(".jasper")) {
                path = path.concat(".jasper");
            }
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                throw new RuntimeException("File " + path + " not found");
            }
            params.put("SUBREPORT_DIR", "/reports/");
            return JasperFillManager.fillReport(is, params, new JRBeanCollectionDataSource(data));
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
    }

    public void reportFromProject(OSGiProject project, String reportName) {
        removeZeroLinesOfCodeModules(project);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("INITIAL_TIME", new Date());
        params.put("project", new OSGiProjectReport(project));
        params.put("provider", provider);
        params.put("projectQuality", metrics.calculateProjectModeQuality().name());
        params.put("projectAbsoluteQuality", metrics.calculateProjectAbsoluteQuality().name());

        addMetricsQuality(params);

        int maxPoints = project.getMaxPoints();
        int projectPoints = metrics.getProjectQualityPonts();
        double percentage = metrics.getProjectQualityPointsPercentage();
        params.put("projectQualityPoints", provider.getMessage("osgi.project-points", projectPoints, maxPoints, percentage));
        FileType type = prompt.promptChoiceTyped(provider.getMessage("report.type"), FileType.getAll(), FileType.HTML);
        this.reportName(reportName).filename(project.toString() + "_" + reportName).type(type).data(getModulesToReport(project)).params(params).build();
    }


    public List<ModuleDTO> getModulesToReport(OSGiProject project) {
        List<ModuleDTO> modulesDTO = new ArrayList<ModuleDTO>();
        for (OSGiModule module : project.getModules()) {
            modulesDTO.add(new ModuleDTO(module, metrics));
        }
        return modulesDTO;
    }

    public void reportFromProject(OSGiProject project, String... reportsName) {
        removeZeroLinesOfCodeModules(project);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("INITIAL_TIME", new Date());
        params.put("project", new OSGiProjectReport(project));
        params.put("provider", provider);
        params.put("projectQuality", metrics.calculateProjectModeQuality().name());
        params.put("projectAbsoluteQuality", metrics.calculateProjectAbsoluteQuality().name());

        addMetricsQuality(params);

        int maxPoints = project.getMaxPoints();
        int projectPoints = metrics.getProjectQualityPonts();
        double percentage = metrics.getProjectQualityPointsPercentage();
        params.put("projectQualityPoints", provider.getMessage("osgi.project-points", projectPoints, maxPoints, percentage));
        FileType type = prompt.promptChoiceTyped(provider.getMessage("report.type"), FileType.getAll(), FileType.HTML);
        for (String s : reportsName) {
            this.reportName(s).filename(project.toString() + "_" + s).type(type).data(getModulesToReport(project)).params(params).build();
        }
    }

    private void removeZeroLinesOfCodeModules(OSGiProject projectFound) {
        if (projectFound != null || projectFound.getModules() != null) {
            Iterator<OSGiModule> i = projectFound.getModules().iterator();
            while (i.hasNext()) {
                OSGiModule module = i.next();
                if (!module.hasLinesOfCode()) {
                    i.remove();
                }
            }
        }
    }

    private void addMetricsQuality(Map<String, Object> params) {
        MetricPoints metricPoints = metrics.calculateMetricQuality(MetricName.CYCLE);

        params.put("cycles", metricUtils.metricQuality(metricPoints));
        params.put("cyclesStateOfArt", metricsLimit.get(MetricName.CYCLE).get(MetricScore.STATE_OF_ART));
        params.put("cyclesVeryGood", metricsLimit.get(MetricName.CYCLE).get(MetricScore.VERY_GOOD));
        params.put("cyclesGood", metricsLimit.get(MetricName.CYCLE).get(MetricScore.GOOD));
        params.put("cyclesRegular", metricsLimit.get(MetricName.CYCLE).get(MetricScore.REGULAR));
        params.put("cyclesAntiPattern", metricsLimit.get(MetricName.CYCLE).get(MetricScore.ANTI_PATTERN));

        metricPoints = metrics.calculateMetricQuality(MetricName.STALE_REFERENCES);
        params.put("staleReferences", metricUtils.metricQuality(metricPoints));
        params.put("staleReferencesStateOfArt", metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.STATE_OF_ART));
        params.put("staleReferencesVeryGood", metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.VERY_GOOD));
        params.put("staleReferencesGood", metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.GOOD));
        params.put("staleReferencesRegular", metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.REGULAR));
        params.put("staleReferencesAntiPattern", metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.ANTI_PATTERN));

        metricPoints = metrics.calculateMetricQuality(MetricName.USES_FRAMEWORK);
        params.put("usesFramework", metricUtils.metricQuality(metricPoints));

        metricPoints = metrics.calculateMetricQuality(MetricName.BUNDLE_DEPENDENCIES);
        params.put("bundleDependency", metricUtils.metricQuality(metricPoints));
        params.put("bundleDependencyStateOfArt", metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.STATE_OF_ART));
        params.put("bundleDependencyVeryGood", metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.VERY_GOOD));
        params.put("bundleDependencyGood", metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.GOOD));
        params.put("bundleDependencyRegular", metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.REGULAR));
        params.put("bundleDependencyAntiPattern", metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.ANTI_PATTERN));


        metricPoints = metrics.calculateMetricQuality(MetricName.DECLARES_PERMISSION);
        params.put("declaresPermission", metricUtils.metricQuality(metricPoints));

        metricPoints = metrics.calculateMetricQuality(MetricName.PUBLISHES_INTERFACES);
        params.put("publishesInterfaces", metricUtils.metricQuality(metricPoints));


    }
}
