package br.ufrgs.rmpestano.intrabundle.metric;

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

  private final int     numberOfMetrics = 8;

  public MetricScore getLocMetric(OSGiModule bundle) {
    long linesOfCode = bundle.getLinesOfCode();

    if (linesOfCode <= 300) {
      return MetricScore.STATE_OF_ART;
    } else if (linesOfCode <= 500) {
      return MetricScore.VERY_GOOD;
    } else if (linesOfCode <= 750) {
      return MetricScore.GOOD;
    } else if (linesOfCode <= 1000) {
      return MetricScore.REGULAR;
    }
    // lines of code greater than 1000
    return MetricScore.ANTI_PATTERN;
  }

  public MetricScore getBundleDependencyMetric(OSGiModule osGiModule) {
    if(getCurrentOSGiProject() == null){
        return MetricScore.REGULAR;
    }
    int numberOfDependencies = getCurrentOSGiProject().getModulesDependencies().get(osGiModule).size();

    // not depending on others modules is state of art
    if (numberOfDependencies == 0) {
      return MetricScore.STATE_OF_ART;
    }

    if (numberOfDependencies <= 3) {
      return MetricScore.VERY_GOOD;
    }

    if (numberOfDependencies <= 5) {
      return MetricScore.GOOD;
    }

    if (numberOfDependencies <= 9) {
      return MetricScore.REGULAR;
    }

    // depending on 10 or more bundles is considered anti pattern(high coupled)
    return MetricScore.ANTI_PATTERN;
  }

  public MetricScore getPublishesInterfaceMetric(OSGiModule bundle) {
    if (bundle.getPublishesInterfaces()) {
      return MetricScore.STATE_OF_ART;
    } else {
      return MetricScore.REGULAR;
    }
  }

  public MetricScore usesFrameworkToManageServicesMetric(OSGiModule bundle) {
    if (bundle.getUsesDeclarativeServices() || bundle.getUsesBlueprint() || bundle.getUsesIpojo()) {
      return MetricScore.STATE_OF_ART;
    } else {
      return MetricScore.REGULAR;
    }
  }

  public MetricScore hasStaleReferencesMetric(OSGiModule bundle) {
    int nroOfStaleReferences = bundle.getStaleReferences().size();
    if (nroOfStaleReferences == 0) {
      return MetricScore.STATE_OF_ART;
    } else if (nroOfStaleReferences >= (bundle.getNumberOfClasses() / 25)) {
      // 20% of classes has stale references
      return MetricScore.REGULAR;
    }
    // more than 25% of classes contains staleReferences
    return MetricScore.ANTI_PATTERN;
  }

  public Metric getDeclaresPermissionMetric(OSGiModule bundle) {
    MetricName name = MetricName.DECLARES_PERMISSION;
    if (bundle.getDeclaresPermissions()) {
      return new Metric(name,MetricScore.STATE_OF_ART);
    } else {
      return new Metric(name,MetricScore.REGULAR);
    }
  }

  public OSGiProject getCurrentOSGiProject() {
      try {
          return currentOSGiProject.get();
      }catch (Exception e){}
    return null;
  }

  public MetricPoints calculateBundleMetric(OSGiModule bundle) {
    List<Metric> bundleMetrics = new ArrayList<Metric>();
    bundleScore += getLocMetric(bundle).getValue();
    numMetrics++;
    if(getCurrentOSGiProject() != null){
        bundleScore += getBundleDependencyMetric(bundle).getValue();
        numMetrics++;
    }
    bundleScore += getDeclaresPermissionMetric(bundle).getValue();
    numMetrics++;
    bundleScore += usesFrameworkToManageServicesMetric(bundle).getValue();
    numMetrics++;
    bundleScore += getPublishesInterfaceMetric(bundle).getValue();
    numMetrics++;
    bundleScore += hasStaleReferencesMetric(bundle).getValue();
    numMetrics++;
    // maximum points a bundle can get
    int maxPoints = numMetrics * MetricScore.STATE_OF_ART.getValue();

    MetricPoints metricPoints = new MetricPoints(bundleMetrics);
    return metricPoints;

  }

  public MetricScore getProjectMetric() {
    //todo
    return null;
  }


}
