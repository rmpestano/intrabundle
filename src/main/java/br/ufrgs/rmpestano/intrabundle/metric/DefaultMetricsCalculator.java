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
public class DefaultMetricsCalculator implements MetricsCalculation {

    @Inject
    Instance<OSGiProject> currentOSGiProject;

    @Inject
    MessageProvider provider;

    public Metric getLocMetric(OSGiModule bundle) {
        MetricName name = MetricName.LOC;
        long linesOfCode = bundle.getLinesOfCode();

        if (linesOfCode <= 300) {
            return new Metric(name, MetricScore.STATE_OF_ART);
        } else if (linesOfCode <= 500) {
            return new Metric(name, MetricScore.VERY_GOOD);
        } else if (linesOfCode <= 750) {
            return new Metric(name, MetricScore.GOOD);
        } else if (linesOfCode <= 1000) {
            return new Metric(name, MetricScore.REGULAR);
        }
        // lines of code greater than 1000
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
        } else if (nroOfStaleReferences <= (bundle.getNumberOfClasses() * 0.25)) {
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

    public MetricScore calculateProjectModeQuality(OSGiProject osGiProject) {
        Map<MetricScore, Integer> metricScores = new HashMap<MetricScore, Integer>();//counts metricScore of each bundle where:
        metricScores.put(MetricScore.ANTI_PATTERN, 0);
        metricScores.put(MetricScore.REGULAR, 0);
        metricScores.put(MetricScore.GOOD, 0);
        metricScores.put(MetricScore.VERY_GOOD, 0);
        metricScores.put(MetricScore.STATE_OF_ART, 0);
        for (OSGiModule osGiModule : osGiProject.getModules()) {

            MetricScore bundleScore = this.calculateBundleQuality(osGiModule).getFinalScore();
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

    public MetricScore calculateProjectModeQuality() {
        return calculateProjectModeQuality(getCurrentOSGiProject());
    }

    @Override
    public MetricScore calculateProjectAbsoluteQuality(OSGiProject osGiProject) {
        int maxPoints = osGiProject.getModules().size() * MetricScore.STATE_OF_ART.getValue();
        int projectPoints = 0;
        for (OSGiModule osGiModule : osGiProject.getModules()) {
            projectPoints += calculateBundleQuality(osGiModule).getBundlePoints();
        }

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

    @Override
    public MetricScore calculateProjectAbsoluteQuality() {
        return calculateProjectAbsoluteQuality(getCurrentOSGiProject());
    }
}
