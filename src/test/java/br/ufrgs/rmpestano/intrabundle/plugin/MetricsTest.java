package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.metric.MetricsCalculation;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import br.ufrgs.rmpestano.intrabundle.util.TestUtils;
import org.junit.Test;

import javax.inject.Inject;

import java.text.NumberFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricsTest extends BaseMetricsTest {

    @Inject
    MetricsCalculation metrics;

    @Test
    public void shouldCalculateMetricPointsOnMavenProject() throws Exception {
      initializeOSGiMavenProject();
      resetOutput();
      OSGiModule module1 = metrics.getCurrentOSGiProject().getModules().get(0);
      OSGiModule module2 = metrics.getCurrentOSGiProject().getModules().get(1);
      OSGiModule module3 = metrics.getCurrentOSGiProject().getModules().get(2);
      assertEquals(metrics.calculateBundleQuality(module1).getFinalScore().name(), MetricScore.VERY_GOOD.name());
      assertEquals(metrics.calculateBundleQuality(module2).getFinalScore().name(), MetricScore.VERY_GOOD.name());
      assertEquals(metrics.calculateBundleQuality(module3).getFinalScore().name(), MetricScore.STATE_OF_ART.name());
    }

    @Test
    public void shouldExecuteBundleMetricsCommandOnMavenProject() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        queueInputLines("n");
        queueInputLines("1");
        getShell().execute("osgi bundleMetrics");

        assertTrue(getOutput().contains("Points obtained: 23 of 30 - " +NumberFormat.getInstance().format(76.7) +
            "% of compliance"));
        assertTrue(getOutput().contains("Cyclic dependencies:STATE_OF_ART"));
        assertTrue(getOutput().contains("Bundle dependencies:VERY_GOOD"));
        assertTrue(getOutput().contains("Declares Permission:REGULAR"));
        assertTrue(getOutput().contains("Uses framework to manage services:REGULAR"));
        assertTrue(getOutput().contains("Publishes interfaces:STATE_OF_ART"));
        assertTrue(getOutput().contains("Stale references:STATE_OF_ART"));
    }

    @Test
    public void shouldExecuteOSGiProjectMetricCommand() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        getShell().execute("osgi projectMetric");
        assertTrue(getOutput().startsWith("Project Frequent metric: VERY_GOOD, Final metric: VERY_GOOD"));
    }

    @Test
    public void shouldCalculateMetricOfOSGiProject() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        MetricScore projectScore = metrics.calculateProjectModeQuality();
        MetricScore projectFinalScore = metrics.calculateProjectAbsoluteQuality();
        assertEquals(projectScore, MetricScore.VERY_GOOD);
        assertEquals(projectFinalScore, MetricScore.VERY_GOOD);
    }


    @Test
    public void shouldExecuteBundleMetricsCommandOnAllBundles() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        queueInputLines("y");
        getShell().execute("osgi bundleMetrics");


        getOutput().contains("Metrics of module1:" + TestUtils.getNewLine() +
                "Points obtained: 23 of 30 - " + NumberFormat.getInstance().format(76.7) +
                "% of compliance" + TestUtils.getNewLine() +
                "Cyclic dependencies:STATE_OF_ART" + TestUtils.getNewLine() +
                "Bundle dependencies:VERY_GOOD" + TestUtils.getNewLine() +
                "Declares Permission:REGULAR" + TestUtils.getNewLine() +
                "Uses framework to manage services:REGULAR" + TestUtils.getNewLine() +
                "Publishes interfaces:STATE_OF_ART" + TestUtils.getNewLine() +
                "Stale references:STATE_OF_ART" + TestUtils.getNewLine() +
                "Metrics of module2:" + TestUtils.getNewLine() +
                "Points obtained: 23 of 30 - " +
                NumberFormat.getInstance().format(76.7) +
                "% of compliance" + TestUtils.getNewLine() +
                "Cyclic dependencies:STATE_OF_ART" + TestUtils.getNewLine() +
                "Bundle dependencies:VERY_GOOD" + TestUtils.getNewLine() +
                "Declares Permission:REGULAR" + TestUtils.getNewLine() +
                "Uses framework to manage services:REGULAR" + TestUtils.getNewLine() +
                "Publishes interfaces:STATE_OF_ART" + TestUtils.getNewLine() +
                "Stale references:STATE_OF_ART" + TestUtils.getNewLine() +
                "Metrics of module3:" + TestUtils.getNewLine() +
                "Points obtained: 27 of 30 - 90% of compliance"+ TestUtils.getNewLine() +
                "Cyclic dependencies:STATE_OF_ART" + TestUtils.getNewLine() +
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
        assertTrue(getOutput().contains("Publishes interfaces:STATE_OF_ART"));
        assertTrue(getOutput().contains("Stale references:STATE_OF_ART"));
        assertTrue(getOutput().contains("Uses framework to manage services:REGULAR"));
        assertTrue(getOutput().contains("Declares Permission:REGULAR"));
        assertTrue(getOutput().contains("Points obtained: 14 of 20 - 70% of compliance"));
    }

    @Test
    public void shouldInvokeBundleMetricCommandOnBundleWithStaleReferences() throws Exception {
        initializeOSGiBundleWithStaleReferences();
        resetOutput();
        getShell().execute("bundle metrics");
        assertTrue(getOutput().trim().contains("Stale references:ANTI_PATTERN"));
        assertTrue(getOutput().trim().contains("Publishes interfaces:STATE_OF_ART"));
        assertTrue(getOutput().contains("Points obtained: 10 of 20 - 50% of compliance"));
    }

    @Test
    public void shouldCalculateMetricsQuality() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        queueInputLines("1");
        getShell().execute("osgi metricQuality");
        assertTrue(getOutput().trim().contains("Points obtained: 15 of 15 - 100% of compliance."));
        resetOutput();
        queueInputLines("2");
        getShell().execute("osgi metricQuality");
        assertTrue(getOutput().trim().endsWith("Points obtained: 15 of 15 - 100% of compliance."));
        resetOutput();
        queueInputLines("3");
        getShell().execute("osgi metricQuality");
        assertTrue(getOutput().trim().endsWith("13 of 15 - " +
        NumberFormat.getInstance().format(86.7) +  "% of compliance."));
        resetOutput();
        queueInputLines("4");
        getShell().execute("osgi metricQuality");
        assertTrue(getOutput().trim().endsWith("Points obtained: 9 of 15 - 60% of compliance."));
        resetOutput();
        queueInputLines("5");
        getShell().execute("osgi metricQuality");
        assertTrue(getOutput().trim().endsWith("Points obtained: 6 of 15 - 40% of compliance."));
        resetOutput();
        queueInputLines("6");
        getShell().execute("osgi metricQuality");
        assertTrue(getOutput().trim().endsWith("Points obtained: 15 of 15 - 100% of compliance."));
    }

}
