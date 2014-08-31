package br.ufrgs.rmpestano.intrabundle.metric;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.model.Metric;
import br.ufrgs.rmpestano.intrabundle.model.MetricPoints;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricName;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 8/17/14.
 */
@Singleton
public class MetricsCalculator implements Metrics {

    @Inject
    Instance<OSGiProject> currentOSGiProject;

    @Inject
    MessageProvider provider;

    private final int numberOfMetrics = 8;

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
        if (bundle.getUsesDeclarativeServices() || bundle.getUsesBlueprint() || bundle.getUsesIpojo()) {
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
        } else if (nroOfStaleReferences >= (bundle.getNumberOfClasses() / 25)) {
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
        }
        return null;
    }

    public MetricPoints calculateBundleMetric(OSGiModule bundle) {
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

}
