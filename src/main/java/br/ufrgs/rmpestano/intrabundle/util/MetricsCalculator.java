package br.ufrgs.rmpestano.intrabundle.util;

import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import br.ufrgs.rmpestano.intrabundle.model.Metrics;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by rmpestano on 8/17/14.
 */
@Singleton
public class MetricsCalculator implements Metrics {

  @Inject
  Instance<OSGiProject> currentOSGiProject;

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

  public OSGiProject getCurrentOSGiProject() {
    return currentOSGiProject.get();
  }


}
