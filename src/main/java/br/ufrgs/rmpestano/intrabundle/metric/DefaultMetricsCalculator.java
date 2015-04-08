package br.ufrgs.rmpestano.intrabundle.metric;

import br.ufrgs.rmpestano.intrabundle.event.MetricsConfigEvent;
import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.model.*;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricName;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import org.jboss.solder.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rmpestano on 8/17/14.
 */
@Singleton
//TODO move everything to OSGiProjectImple and deprecate MetricsCalculator(a project knows how to calculates its quality)
public class DefaultMetricsCalculator implements MetricsCalculation {

    @Inject
    Instance<OSGiProject> currentOSGiProject;

    @Inject
    MessageProvider provider;

    Map<OSGiProject, List<MetricPoints>> projectBundleQualities = new HashMap<OSGiProject,List<MetricPoints>>();//stores each project bundle quality

    @Inject
    Map<MetricName, Map<MetricScore, Double>> metricsLimit;

    /**
     * zero cycles = STATE_OF_THE_ART
     * zero > bundles with cycle <= 10% = VERY_GOOD
     * 10% > bundles with cycle <= 20% = GOOD
     * 20% > bundles with cycle <= 30% = REGULAR
     * > 30% bundles with cycle = ANTI_PATERN
     * @param bundle
     * @return
     */
    public Metric getCycleMetric(OSGiModule bundle) {
        MetricName name = MetricName.CYCLE;
        List<ModuleCycle> moduleCycles = currentOSGiProject.get().getModuleCyclicDependenciesMap().get(bundle);
        int numberOfCycles =  moduleCycles.size();

        if (numberOfCycles <= metricsLimit.get(MetricName.CYCLE).get(MetricScore.STATE_OF_ART)) {
            return new Metric(name, MetricScore.STATE_OF_ART);
        } else if (numberOfCycles <= metricsLimit.get(MetricName.CYCLE).get(MetricScore.VERY_GOOD )) {
            return new Metric(name, MetricScore.VERY_GOOD);
        } else if (numberOfCycles <= metricsLimit.get(MetricName.CYCLE).get(MetricScore.GOOD)) {
            return new Metric(name, MetricScore.GOOD);
        } else if (numberOfCycles <= metricsLimit.get(MetricName.CYCLE).get(MetricScore.REGULAR)) {
            return new Metric(name, MetricScore.REGULAR);
        }
        else if (numberOfCycles >=  metricsLimit.get(MetricName.CYCLE).get(MetricScore.ANTI_PATTERN)) {
            return new Metric(name, MetricScore.ANTI_PATTERN);
        }
        return null;
    }

    public Metric getBundleDependencyMetric(OSGiModule osGiModule) {
        MetricName name = MetricName.BUNDLE_DEPENDENCIES;
        if (getCurrentOSGiProject() == null) {
            return null;
        }
        int numberOfDependencies = getCurrentOSGiProject().getModulesDependencies().get(osGiModule).size();
        numberOfDependencies += osGiModule.getRequiredBundles().size();


        if (numberOfDependencies <= metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.STATE_OF_ART)) {
            return new Metric(name, MetricScore.STATE_OF_ART);
        }

        if (numberOfDependencies <= metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.VERY_GOOD)) {
            return new Metric(name, MetricScore.VERY_GOOD);
        }

        if (numberOfDependencies <= metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.GOOD)) {
            return new Metric(name, MetricScore.GOOD);
        }

        if (numberOfDependencies <= metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.REGULAR)) {
            return new Metric(name, MetricScore.REGULAR);
        }

        if (numberOfDependencies >= metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES).get(MetricScore.ANTI_PATTERN)) {
            return new Metric(name, MetricScore.ANTI_PATTERN);
        }
        //no limmit found
        return null;
    }

    public Metric getPublishesInterfaceMetric(OSGiModule bundle) {
        MetricName name = MetricName.PUBLISHES_INTERFACES;
        if (bundle.getPublishesInterfaces()) {
            return new Metric(name, MetricScore.STATE_OF_ART);
        } else {
            return new Metric(name, MetricScore.REGULAR);
        }
    }

    public Metric usesFrameworkToManageServicesMetric(OSGiModule bundle) {
        if (bundle.usesFramework()) {
            return new Metric(MetricName.USES_FRAMEWORK, MetricScore.STATE_OF_ART);
        } else {
            return new Metric(MetricName.USES_FRAMEWORK, MetricScore.REGULAR);
        }
    }

    public Metric hasStaleReferencesMetric(OSGiModule bundle) {
        MetricName name = MetricName.STALE_REFERENCES;
        int nroOfStaleReferences = bundle.getStaleReferences().size();
        if (nroOfStaleReferences <= (bundle.getNumberOfClasses() * metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.STATE_OF_ART))) {
            return new Metric(name, MetricScore.STATE_OF_ART);
        } else if (nroOfStaleReferences <= (bundle.getNumberOfClasses() * metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.VERY_GOOD))) {
            return new Metric(name, MetricScore.VERY_GOOD);
        }  else if (nroOfStaleReferences <= (bundle.getNumberOfClasses() * metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.GOOD))) {
            return new Metric(name, MetricScore.GOOD);
        } else if (nroOfStaleReferences <= (bundle.getNumberOfClasses() * metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.REGULAR))) {
            return new Metric(name, MetricScore.REGULAR);
        } else if (nroOfStaleReferences >= (bundle.getNumberOfClasses() * metricsLimit.get(MetricName.STALE_REFERENCES).get(MetricScore.ANTI_PATTERN))) {
            return new Metric(name, MetricScore.ANTI_PATTERN);
        }

        //no limmits found
        return null;
    }

    public Metric getDeclaresPermissionMetric(OSGiModule bundle) {
        MetricName name = MetricName.DECLARES_PERMISSION;
        if (bundle.getDeclaresPermissions()) {
            return new Metric(name, MetricScore.STATE_OF_ART);
        } else {
            return new Metric(name, MetricScore.REGULAR);
        }
    }

    public OSGiProject getCurrentOSGiProject() {
        try {
            return currentOSGiProject.get();
        } catch (Exception e) {
            Logger.getLogger(getClass().getCanonicalName()).error(e);
        }
        return null;
    }


    public MetricPoints calculateBundleQuality(OSGiModule bundle) {
        List<Metric> bundleMetrics = new ArrayList<Metric>();
        if (getCurrentOSGiProject() != null) {
            bundleMetrics.add(getBundleDependencyMetric(bundle));
            bundleMetrics.add(getCycleMetric(bundle));
        }
        bundleMetrics.add(getDeclaresPermissionMetric(bundle));
        bundleMetrics.add(usesFrameworkToManageServicesMetric(bundle));
        bundleMetrics.add(getPublishesInterfaceMetric(bundle));
        bundleMetrics.add(hasStaleReferencesMetric(bundle));
        // maximum points a bundle can get
        MetricPoints metricPoints = new MetricPoints(bundleMetrics);
        return metricPoints;

    }

    public MetricPoints calculateMetricQuality(MetricName metric){
        List<Metric> metrics = new ArrayList<Metric>();
        List<OSGiModule> modules = currentOSGiProject.get().getModules();
            switch (metric){
                case CYCLE:{
                    for (OSGiModule osGiModule : modules) {
                        metrics.add(getCycleMetric(osGiModule));
                    }
                    break;
                }
                case STALE_REFERENCES:{
                    for (OSGiModule osGiModule : modules) {
                        metrics.add(hasStaleReferencesMetric(osGiModule));
                    }
                    break;
                }
                case PUBLISHES_INTERFACES:{
                    for (OSGiModule osGiModule : modules) {
                        metrics.add(getPublishesInterfaceMetric(osGiModule));
                    }
                    break;
                }
                case USES_FRAMEWORK:{
                    for (OSGiModule osGiModule : modules) {
                        metrics.add(usesFrameworkToManageServicesMetric(osGiModule));
                    }
                    break;
                }
                case BUNDLE_DEPENDENCIES:{
                    for (OSGiModule osGiModule : modules) {
                        metrics.add(getBundleDependencyMetric(osGiModule));
                    }
                    break;
                }
                case DECLARES_PERMISSION:{
                    for (OSGiModule osGiModule : modules) {
                        metrics.add(getDeclaresPermissionMetric(osGiModule));
                    }
                    break;
                }
            }

        return new MetricPoints(metrics);
    }

    public MetricScore calculateProjectModeQuality() {
        Map<MetricScore, Integer> metricScores = new HashMap<MetricScore, Integer>();//counts metricScore of each bundle where:
        metricScores.put(MetricScore.ANTI_PATTERN, 0);
        metricScores.put(MetricScore.REGULAR, 0);
        metricScores.put(MetricScore.GOOD, 0);
        metricScores.put(MetricScore.VERY_GOOD, 0);
        metricScores.put(MetricScore.STATE_OF_ART, 0);
        for (MetricPoints points : getBundleQualityList()) {

            switch (points.getFinalScore()) {
                case ANTI_PATTERN: {
                    Integer count = metricScores.get(MetricScore.ANTI_PATTERN);
                    metricScores.put(MetricScore.ANTI_PATTERN, ++count);
                    break;
                }
                case REGULAR: {
                    Integer count = metricScores.get(MetricScore.REGULAR);
                    metricScores.put(MetricScore.REGULAR, ++count);
                    break;
                }
                case GOOD: {
                    Integer count = metricScores.get(MetricScore.GOOD);
                    metricScores.put(MetricScore.GOOD, ++count);
                    break;
                }
                case VERY_GOOD: {
                    Integer count = metricScores.get(MetricScore.VERY_GOOD);
                    metricScores.put(MetricScore.VERY_GOOD, ++count);
                    break;
                }
                case STATE_OF_ART: {
                    Integer count = metricScores.get(MetricScore.STATE_OF_ART);
                    metricScores.put(MetricScore.STATE_OF_ART, ++count);
                    break;
                }
            }
        }
        MetricScore mostFrequent = MetricScore.ANTI_PATTERN;
        for (MetricScore metricScore : metricScores.keySet()) {
            if (metricScores.get(metricScore) > metricScores.get(mostFrequent)) {
                mostFrequent = metricScore;
                //let the higher score to prevail if most frequent scores are tie
            } else if (metricScores.get(metricScore).equals(metricScores.get(mostFrequent))) {
                if (metricScore.getValue() > mostFrequent.getValue()) {
                    mostFrequent = metricScore;
                }
            }
        }
        return mostFrequent;
    }

    public List<OSGiModule> getModulesByQuality(MetricScore quality) {
        List<OSGiModule> result = new ArrayList<OSGiModule>(getCurrentOSGiProject().getModules().size());
        for (OSGiModule osGiModule : getCurrentOSGiProject().getModules()) {
            MetricPoints points = calculateBundleQuality(osGiModule);
            if (points.getFinalScore().equals(quality)) {
                result.add(osGiModule);
            }
        }
        return result;
    }

    public int getProjectQualityPonts() {
        int total = 0;
        for (MetricPoints points : getBundleQualityList()) {
            total += points.getFinalScore().getValue();
        }
        return total;
    }

    @Override
    public double getProjectQualityPointsPercentage() {
        BigDecimal bd = new BigDecimal(getProjectQualityPonts()/(double)getCurrentOSGiProject().getMaxPoints());
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.doubleValue()*100;
    }


    @Override
    public MetricScore calculateProjectAbsoluteQuality() {
        int maxPoints = getCurrentOSGiProject().getMaxPoints();

        int projectPoints = getProjectQualityPonts();

        if (projectPoints >= maxPoints * 0.9) {
            return MetricScore.STATE_OF_ART;
        } else if (projectPoints >= maxPoints * 0.75) {
            return MetricScore.VERY_GOOD;
        } else if (projectPoints >= maxPoints * 0.60) {
            return MetricScore.GOOD;
        } else if (projectPoints >= maxPoints * 0.4) {
            return MetricScore.REGULAR;
        } else {
            return MetricScore.ANTI_PATTERN;
        }


    }


    private List<MetricPoints> calculateBundlePoints(OSGiProject osGiProject){
        List<MetricPoints> bundleQualities = new ArrayList<MetricPoints>();
        for (OSGiModule osGiModule : osGiProject.getModules()) {
            bundleQualities.add(calculateBundleQuality(osGiModule));
        }
        return bundleQualities;
    }

    public List<MetricPoints> getBundleQualityList() {
        if(projectBundleQualities.size() == 5){//only cache 10 projects bundles qualities
            projectBundleQualities.clear();
        }
        if(!projectBundleQualities.containsKey(getCurrentOSGiProject())) {
            projectBundleQualities.put(getCurrentOSGiProject(),calculateBundlePoints(getCurrentOSGiProject()));
        }
        return projectBundleQualities.get(getCurrentOSGiProject());
    }

    public void listenMetricsConfigEvent(@Observes MetricsConfigEvent metricsConfigEvent){
        this.metricsLimit = metricsConfigEvent.getMetricsLimit();
    }
}
