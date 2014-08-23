package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.metric.Metrics;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class MetricsTest extends BaseMetricsTest {

    @Inject
    Metrics metrics;

    @Test
    public void shouldCalculateMetricPointsOnMavenProject() throws Exception {
      initializeOSGiMavenProject();
      resetOutput();
      OSGiModule module1 = metrics.getCurrentOSGiProject().getModules().get(0);
      OSGiModule module2 = metrics.getCurrentOSGiProject().getModules().get(1);
      OSGiModule module3 = metrics.getCurrentOSGiProject().getModules().get(2);
      assertEquals(metrics.calculateBundleMetric(module1).getFinalScore(), MetricScore.REGULAR);
      assertEquals(metrics.calculateBundleMetric(module2).getFinalScore(), MetricScore.GOOD);
      assertEquals(metrics.calculateBundleMetric(module3).getFinalScore(), MetricScore.VERY_GOOD);
    }

}
