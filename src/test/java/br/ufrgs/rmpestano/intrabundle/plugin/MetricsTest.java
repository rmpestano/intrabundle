package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.metric.Metrics;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import br.ufrgs.rmpestano.intrabundle.util.TestUtils;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
      assertEquals(metrics.calculateBundleMetric(module1).getFinalScore().name(), MetricScore.GOOD.name());
      assertEquals(metrics.calculateBundleMetric(module2).getFinalScore().name(), MetricScore.VERY_GOOD.name());
      assertEquals(metrics.calculateBundleMetric(module3).getFinalScore().name(), MetricScore.VERY_GOOD.name());
    }

    @Test
    public void shouldExecuteBundleMetricsCommandOnMavenProject() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        queueInputLines("n");
        queueInputLines("1");
        getShell().execute("osgi bundleMetrics");
        assertTrue(getOutput().contains("Points obtained: 85 of 120. Average score: GOOD"));
        assertTrue(getOutput().contains("Lines of code:STATE_OF_ART"));
        assertTrue(getOutput().contains("Bundle dependencies:VERY_GOOD"));
        assertTrue(getOutput().contains("Declares Permission:REGULAR"));
        assertTrue(getOutput().contains("Uses framework to manage services:REGULAR"));
        assertTrue(getOutput().contains("Publishes interfaces:STATE_OF_ART"));
        assertTrue(getOutput().contains("Stale references:STATE_OF_ART"));
    }

    @Test
    public void shouldExecuteBundleMetricsCommandOnAllBundles() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        queueInputLines("y");
        getShell().execute("osgi bundleMetrics");
        getOutput().contains("Metrics of module1:" + TestUtils.getNewLine() +
                "Points obtained: 85 of 120. Average score: GOOD" + TestUtils.getNewLine() +
                "Lines of code:STATE_OF_ART" + TestUtils.getNewLine() +
                "Bundle dependencies:VERY_GOOD" + TestUtils.getNewLine() +
                "Declares Permission:REGULAR" + TestUtils.getNewLine() +
                "Uses framework to manage services:REGULAR" + TestUtils.getNewLine() +
                "Publishes interfaces:STATE_OF_ART" + TestUtils.getNewLine() +
                "Stale references:STATE_OF_ART" + TestUtils.getNewLine() +
                "Metrics of module2:" + TestUtils.getNewLine() +
                "Points obtained: 90 of 120. Average score: VERY_GOOD" + TestUtils.getNewLine() +
                "Lines of code:STATE_OF_ART" + TestUtils.getNewLine() +
                "Bundle dependencies:STATE_OF_ART" + TestUtils.getNewLine() +
                "Declares Permission:REGULAR" + TestUtils.getNewLine() +
                "Uses framework to manage services:REGULAR" + TestUtils.getNewLine() +
                "Publishes interfaces:STATE_OF_ART" + TestUtils.getNewLine() +
                "Stale references:STATE_OF_ART" + TestUtils.getNewLine() +
                "Metrics of module3:" + TestUtils.getNewLine() +
                "Points obtained: 105 of 120. Average score: VERY_GOOD"+ TestUtils.getNewLine() +
                "Lines of code:STATE_OF_ART" + TestUtils.getNewLine() +
                "Bundle dependencies:STATE_OF_ART" + TestUtils.getNewLine() +
                "Declares Permission:REGULAR" + TestUtils.getNewLine() +
                "Uses framework to manage services:STATE_OF_ART" + TestUtils.getNewLine() +
                "Publishes interfaces:STATE_OF_ART" + TestUtils.getNewLine() +
                "Stale references:STATE_OF_ART");
    }

    @Test
    public void shouldInvokeBundleMetricCommand() throws Exception {
        initializeMavenBundle();
        resetOutput();
        getShell().execute("bundle metrics");
        assertTrue(getOutput().contains("Lines of code:STATE_OF_ART"));
        assertTrue(getOutput().contains("Publishes interfaces:STATE_OF_ART"));
        assertTrue(getOutput().contains("Stale references:STATE_OF_ART"));
        assertTrue(getOutput().contains("Uses framework to manage services:REGULAR"));
        assertTrue(getOutput().contains("Declares Permission:REGULAR"));
        assertTrue(getOutput().contains("Points obtained: 70 of 100. Average score: GOOD"));
    }

    @Test
    public void shouldInvokeBundleMetricCommandOnBundleWithManyLoc() throws Exception {
        initializeOSGiBundleWithModuleWithManyLoc();
        resetOutput();
        getShell().execute("bundle metrics");
        assertTrue(getOutput().contains("Lines of code:ANTI_PATTERN"));
        assertTrue(getOutput().contains("Points obtained: 35 of 100. Average score: ANTI_PATTERN"));
    }

}
