package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.facet.OSGiFacet;
import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.jasper.JasperManager;
import br.ufrgs.rmpestano.intrabundle.metric.MetricsCalculation;
import br.ufrgs.rmpestano.intrabundle.model.*;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricName;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import br.ufrgs.rmpestano.intrabundle.util.Constants;
import br.ufrgs.rmpestano.intrabundle.util.MetricUtils;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 *
 */
@Alias("osgi")
@RequiresFacet(OSGiFacet.class)
public class OsgiPlugin implements Plugin {

    @Inject
    Instance<OSGiProject> project;

    @Inject
    MessageProvider provider;

    @Inject
    ShellPrompt prompt;

    @Inject
    Shell shell;

    @Inject
    JasperManager jasperManager;

    @Inject
    MetricsCalculation metrics;

    @Inject
    MetricUtils metricUtils;


    private boolean sorted;



    @Command(value = "countBundles")
    public void countBundles(@PipeIn String in, PipeOut out) {
        out.println("Total number of bundles:" + getModules().size());
    }

    @Command(value = "listBundles")
    public void listBundles(@PipeIn String in, PipeOut out) {
        for (OSGiModule osGiModule : getModules()) {
            out.println(osGiModule.getName());
        }
    }

    @Command(value = "bundleLocations")
    public void bundleLocations(@PipeIn String in, PipeOut out) {
        for (int i=1;i<=getModules().size();i++) {
            out.println(i+" - "+((Project)getModules().get(i-1)).getProjectRoot().getFullyQualifiedName());
        }
    }

    @Command(value = "loc", help = "count lines of code of all bundles")
    public void loc(@PipeIn String in, PipeOut out) {
        long total = 0;
        for (int i = 0; i < getModules().size(); i++) {
            long loci = getModules().get(i).getLinesOfCode();
            out.println(getModules().get(i).getName() + ":" + loci);
            total += loci;
        }
        out.println(provider.getMessage("osgi.total-loc") + total);
    }

    @Command(value = "lot", help = "count lines of test code of all bundles")
    public void lot(@PipeIn String in, PipeOut out) {
        out.println(provider.getMessage("osgi.total-lot") + project.get().getLinesOfTestCode());
    }

    @Command(value = "usesDeclarativeServices", help = "list modules that use declarative services")
    public void usesDeclarativeServices(@PipeIn String in, PipeOut out) {
        out.println(ShellColor.YELLOW, provider.getMessage("osgi.declarativeServices"));
        for (OSGiModule module : getModules()) {
            if (module.getUsesDeclarativeServices()) {
                out.println(module.getName());
            }
        }
    }

    @Command(value = "activators", help = "list modules activator classes")
    public void listActivators(@PipeIn String in, PipeOut out) {
        out.println(ShellColor.YELLOW, provider.getMessage("osgi.listActivators"));
        for (OSGiModule module : getModules()) {
            out.println(module.getName() + ":" + (module.getActivator() != null ? module.getActivator().getFullyQualifiedName() : provider.getMessage("osgi.no-activator")));
        }
    }

    @Command(value = "exportedPackages")
    public void listExportedPackages(@PipeIn String in, PipeOut out) {
        if (!allModules(provider.getMessage("packages"))) {
            OSGiModule choice = choiceModule();
            listModuleExportedPackages(choice, out);
        }//execute command for all modules
        else {
            for (OSGiModule osGiModule : getModules()) {
                listModuleExportedPackages(osGiModule, out);
            }
        }

    }

    @Command(value = "importedPackages")
    public void listImportedPackages(@PipeIn String in, PipeOut out) {
        if (!allModules(provider.getMessage("packages"))) {
            OSGiModule choice = choiceModule();
            listModuleImportedPackages(choice, out);
        }//execute command for all modules
        else {
            for (OSGiModule osGiModule : getModules()) {
                listModuleImportedPackages(osGiModule, out);
            }
        }
    }

    @Command(value = "requiredBundles")
    public void listRequiredBundles(@PipeIn String in, PipeOut out) {
        if (!allModules(provider.getMessage("requireBundles"))) {
            OSGiModule choice = choiceModule();
            listModuleRequiredBundles(choice, out);
        }//execute command for all bundles
        else {
            for (OSGiModule osGiModule : getModules()) {
                listModuleRequiredBundles(osGiModule, out);
            }
        }
    }

    @Command("dependencies")
    public void moduleDependencies(@PipeIn String in, PipeOut out) {
        if (!this.allModules(provider.getMessage("dependencies"))) {
            OSGiModule choice = choiceModule();
            this.listModuleDependencies(choice, out);
        }//execute command for all modules
        else {
            for (OSGiModule osGiModule : getModules()) {
                listModuleDependencies(osGiModule, out);
            }
        }
    }

    @Command("cycles")
    public void moduleCycles(@PipeIn String in, PipeOut out) {
        if (!this.allModules(provider.getMessage("cycles"))) {
            OSGiModule choice = choiceModule();
            this.listModuleCycles(choice, out);
        }//execute command for all modules
        else {
            for (OSGiModule osGiModule : getModules()) {
                listModuleCycles(osGiModule, out);
            }
        }
    }


    @Command("staleReferences")
    public void moduleStaleReferences(@PipeIn String in, PipeOut out) {
        if (!this.allModules(provider.getMessage("staleReferences"))) {
            OSGiModule bundle = choiceModule();
            List<Resource<?>> staleReferences = bundle.getStaleReferences();
            if(!staleReferences.isEmpty()){
                out.println(provider.getMessage("bundle.listing-stale-references"));
                for (Resource<?> staleReference : staleReferences) {
                    out.println(staleReference.getFullyQualifiedName());
                }

            }
            else{
                out.println(provider.getMessage("bundle.noStaleReferences"));
            }
        }//execute command for all modules
        else {
            out.println(ShellColor.YELLOW, "===== " + provider.getMessage("module.staleReferences") + " =====");
            for (OSGiModule module: getModules()) {
               if(!module.getStaleReferences().isEmpty()){
                   out.println(module.getName() + ":"+module.getStaleReferences().size() + " "+ provider.getMessage("metrics.staleReferences").toLowerCase());
               }
               else{
                   out.println(module.getName() + ":"+provider.getMessage("bundle.noStaleReferences"));
               }
            }
        }
    }

    @Command("publishInterfaces")
    public void publishInterfaces(@PipeIn String in, PipeOut out) {
        out.println(ShellColor.YELLOW, provider.getMessage("osgi.publishInterfaces"));
        for (OSGiModule osGiModule : getModules()) {
            if (osGiModule.getPublishesInterfaces()) {
                out.println(osGiModule.getName());
            }
        }
    }

    @Command("declaresPermissions")
    public void declaresPermissions(@PipeIn String in, PipeOut out) {
        out.println(ShellColor.YELLOW, provider.getMessage("osgi.declaresPermissions"));
        for (OSGiModule osGiModule : getModules()) {
            if (osGiModule.getDeclaresPermissions()) {
                out.println(osGiModule.getName());
            }
        }
    }

    @Command(value = "bundleMetrics")
    public void bundleMetrics(PipeOut out) {
        if (!this.allModules(provider.getMessage("metrics"))) {
            OSGiModule bundle = choiceModule();
            MetricPoints metricPoints = metrics.calculateBundleQuality(bundle);
            out.println(metricUtils.metricQuality(metricPoints));
            out.println(provider.getMessage("bundle.listing-metrics"));
            for (Metric metric : metricPoints.getBundleMetrics()) {
                 out.println(provider.getMessage(metric.getName().getValue())+":"+metric.getScore().name());
            }

        }//execute command for all modules
        else {
            out.println(ShellColor.YELLOW, "===== " + provider.getMessage("osgi.listing-metrics") + " =====");
            for (OSGiModule module: getModules()) {
                out.println(ShellColor.YELLOW,provider.getMessage("module.metrics",module.getName()));
                MetricPoints metricPoints = metrics.calculateBundleQuality(module);
                out.println(metricUtils.metricQuality(metricPoints));
                for (Metric metric : metricPoints.getBundleMetrics()) {
                    out.println(provider.getMessage(metric.getName().getValue())+":"+metric.getScore().name());
                }
            }
        }
    }

    @Command(value = "projectMetric", help = "returns OSGi project mode and absolute metric score based on each bundle score")
    public void projectMetric(PipeOut out) {
        out.println(provider.getMessage("osgi.metric",metrics.calculateProjectModeQuality().name(), metrics.calculateProjectAbsoluteQuality().name()));
    }

    @Command(value = "projectPoints", help = "returns OSGi project mode and absolute metric score based on each bundle score")
    public void projecPoints(PipeOut out) {
        int maxPoints = project.get().getMaxPoints();
        int projectPoints = metrics.getProjectQualityPonts();
        double percentage = metrics.getProjectQualityPointsPercentage();
        out.println(provider.getMessage("osgi.project-points",projectPoints,maxPoints,percentage));
    }

    @Command(value = "metricQuality", help = "returns the quality of a given metric based on the project modules")
    public void metricQuality(PipeOut out) {
        MetricName metric = choiceMetric();
        MetricPoints metricPoints = metrics.calculateMetricQuality(metric);
        out.println(metricUtils.metricQuality(metricPoints));
    }

    @Command(help = "list bundles with the given quality", value = "findBundlesByQuality")
    public void findModulesByQuality(PipeOut out) {
        MetricScore quality = choiceQuality();
        List<OSGiModule> modules = metrics.getModulesByQuality(quality);
        if(!modules.isEmpty()){
            for (OSGiModule osGiModule : modules) {
                out.println(osGiModule.getName());
            }
        } else{
            out.println(provider.getMessage("osgi.scan.noBundlesFound"));
        }

    }

    private void listModuleImportedPackages(OSGiModule module, PipeOut out) {
        out.println(ShellColor.YELLOW, "===== " + provider.getMessage("module.listImported", module) + " =====");
        if (module.getImportedPackages().isEmpty()) {
            out.println(provider.getMessage("module.noImportedPackages"));
        } else {
            for (String s : module.getImportedPackages()) {
                out.println(s);
            }
        }

    }

    private void listModuleExportedPackages(OSGiModule module, PipeOut out) {
        out.println(ShellColor.YELLOW, "===== " + provider.getMessage("module.listExported", module) + " =====");
        if (module.getExportedPackages().isEmpty()) {
            out.println(provider.getMessage("module.noExportedPackages"));
        } else {
            for (String s : module.getExportedPackages()) {
                out.println(s);
            }
        }

    }

    private void listModuleRequiredBundles(OSGiModule module, PipeOut out) {
        out.println(ShellColor.YELLOW, "===== " + provider.getMessage("module.listRequiredBundles", module) + " =====");
        if (module.getRequiredBundles().isEmpty()) {
            out.println(provider.getMessage("module.noRequiredBundles"));
        } else {
            for (String s : module.getRequiredBundles()) {
                out.println(s);
            }
        }

    }

    private void listModuleDependencies(OSGiModule choice, PipeOut out) {
        out.println(ShellColor.YELLOW, "===== " + provider.getMessage("module.dependencies", choice) + " =====");
        if (project.get().getModulesDependencies().get(choice).isEmpty()) {
            out.println(provider.getMessage("module.noDependency"));
        } else {
            for (OSGiModule m : project.get().getModulesDependencies().get(choice)) {
                out.println(m.getName());
            }
        }
    }

    private void listModuleCycles(OSGiModule choice, PipeOut out) {
        out.println(ShellColor.YELLOW, "===== " + provider.getMessage("module.cycles", choice) + " =====");
        if (project.get().getModuleCyclicDependenciesMap().get(choice).isEmpty()) {
            out.println(provider.getMessage("module.noDependency"));
        } else {
            for (ModuleCycle m : project.get().getModuleCyclicDependenciesMap().get(choice)) {
                out.println(m.toString());
            }
        }
    }

    private OSGiModule choiceModule() {
        return prompt.promptChoiceTyped(provider.getMessage("module.choice"), getModules());
    }

    private MetricScore choiceQuality() {
        return prompt.promptChoiceTyped(provider.getMessage("quality.choice"), Arrays.asList(MetricScore.values()));
    }
    private MetricName choiceMetric() {
        return prompt.promptChoiceTyped(provider.getMessage("metrics.choice"), Arrays.asList(MetricName.values()));
    }


    private boolean allModules(String allWhat) {
        return prompt.promptBoolean(provider.getMessage("module.all", allWhat));
    }

    public List<OSGiModule> getModules() {
        if(!sorted){
            Collections.sort(project.get().getModules());
            sorted = true;
        }
        return project.get().getModules();
    }


    @Command(help = "Generate a report containing information about all bundles of the project")
    public void report() {
        jasperManager.reportFromProject(project.get(),Constants.REPORT.GENERAL);
    }

    @Command(value = "metricReport", help = "Generate a report containing bundle metric information of all bundles of the project")
    public void metricReport() {
        jasperManager.reportFromProject(project.get(), Constants.REPORT.METRICS);
    }


}
