package br.ufrgs.rmpestano.intrabundle.metric;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.model.Metric;
import br.ufrgs.rmpestano.intrabundle.model.MetricPoints;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricName;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import org.jboss.solder.logging.Logger;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
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

    Map<OSGiProject, List<MetricScore>> projectBundleQualities;//stores each project bundle quality

    public Metric getLocMetric(OSGiModule bundle) {
        MetricName name = MetricName.LOC;
        long linesOfCode = bundle.getLinesOfCode();

        if (linesOfCode <= 500) {
            return new Metric(name, MetricScore.STATE_OF_ART);
        } else if (linesOfCode <= 750) {
            return new Metric(name, MetricScore.VERY_GOOD);
        } else if (linesOfCode <= 1000) {
            return new Metric(name, MetricScore.GOOD);
        } else if (linesOfCode <= 1500) {
            return new Metric(name, MetricScore.REGULAR);
        }
        // lines of code greater than 1500
        return new Metric(name, MetricScore.ANTI_PATTERN);
    }

    public Metric getBundleDependencyMetric(OSGiModule osGiModule) {
        MetricName name = MetricName.BUNDLE_DEPENDENCIES;
        if (getCurrentOSGiProject() == null) {
            return null;
        }
        int numberOfDependencies = getCurrentOSGiProject().getModulesDependencies().get(osGiModule).size();
        numberOfDependencies += osGiModule.getRequiredBundles().size();

        // not depending on others modules is state of art
        if (numberOfDependencies == 0) {
            return new Metric(name, MetricScore.STATE_OF_ART);
        }

        if (numberOfDependencies <= 3) {
            return new Metric(name, MetricScore.VERY_GOOD);
        }

        if (numberOfDependencies <= 5) {
            return new Metric(name, MetricScore.GOOD);
        }

        if (numberOfDependencies <= 9) {
            return new Metric(name, MetricScore.REGULAR);
        }

        // depending on 10 or more bundles is considered anti pattern(high coupled)
        return new Metric(name, MetricScore.ANTI_PATTERN);
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
        if (nroOfStaleReferences == 0) {
            return new Metric(name, MetricScore.STATE_OF_ART);
        } else if (nroOfStaleReferences <= (bundle.getNumberOfClasses() * 0.10)) {
            // 10% of classes has stale references
            return new Metric(name, MetricScore.VERY_GOOD);
        }  else if (nroOfStaleReferences <= (bundle.getNumberOfClasses() * 0.25)) {
            // 25% of classes has stale references
            return new Metric(name, MetricScore.GOOD);
        } else if (nroOfStaleReferences <= (bundle.getNumberOfClasses() * 0.50)) {
            // 25% of classes has stale references
            return new Metric(name, MetricScore.REGULAR);
        }

        // more than 25% of classes contains staleReferences
        return new Metric(name, MetricScore.ANTI_PATTERN);
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
        bundleMetrics.add(getLocMetric(bundle));
        if (getCurrentOSGiProject() != null) {
            bundleMetrics.add(getBundleDependencyMetric(bundle));
        }
        bundleMetrics.add(getDeclaresPermissionMetric(bundle));
        bundleMetrics.add(usesFrameworkToManageServicesMetric(bundle));
        bundleMetrics.add(getPublishesInterfaceMetric(bundle));
        bundleMetrics.add(hasStaleReferencesMetric(bundle));
        // maximum points a bundle can get
        MetricPoints metricPoints = new MetricPoints(bundleMetrics);
        return metricPoints;

    }

    public MetricScore calculateProjectModeQuality() {
        Map<MetricScore, Integer> metricScores = new HashMap<MetricScore, Integer>();//counts metricScore of each bundle where:
        metricScores.put(MetricScore.ANTI_PATTERN, 0);
        metricScores.put(MetricScore.REGULAR, 0);
        metricScores.put(MetricScore.GOOD, 0);
        metricScores.put(MetricScore.VERY_GOOD, 0);
        metricScores.put(MetricScore.STATE_OF_ART, 0);
        for (MetricScore bundleScore : getBundleQualityList()) {

            switch (bundleScore) {
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
        for (MetricScore metricScore : getBundleQualityList()) {
            total += metricScore.getValue();
        }
        return total;
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


    private Map<OSGiProject,List<MetricScore>> calculateBundleQualities(OSGiProject osGiProject){
        Map<OSGiProject, List<MetricScore>> result = new HashMap<OSGiProject, List<MetricScore>>();
        result.put(osGiProject, new ArrayList<MetricScore>());
        for (OSGiModule osGiModule : osGiProject.getModules()) {
            result.get(osGiProject).add(calculateBundleQuality(osGiModule).getFinalScore());
        }
        return result;
    }

    public List<MetricScore> getBundleQualityList() {
        if(projectBundleQualities == null || !projectBundleQualities.containsKey(getCurrentOSGiProject()) || projectBundleQualities.size() > 2) {//only cache two projects bundles quality at a time
            projectBundleQualities = calculateBundleQualities(getCurrentOSGiProject());
        }
        return projectBundleQualities.get(getCurrentOSGiProject());
    }
}
