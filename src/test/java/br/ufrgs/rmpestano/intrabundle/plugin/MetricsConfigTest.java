package br.ufrgs.rmpestano.intrabundle.plugin;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MetricsConfigTest extends BaseMetricsTest {


    @Test
    public void shouldChangeBundleDependenciesConfig() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        queueInputLines("1");
        queueInputLines("2.0");
        queueInputLines("2.0");
        queueInputLines("2.0");
        queueInputLines("2.0");
        queueInputLines("2.0");
        getShell().execute("metric-config");
        assertTrue(getOutput().contains("GOOD: 2.0"));
        assertTrue(getOutput().contains("STATE_OF_ART: 2.0"));
        assertTrue(getOutput().contains("REGULAR: 2.0"));
        assertTrue(getOutput().contains("VERY_GOOD: 2.0"));
        assertTrue(getOutput().contains("ANTI_PATTERN: 2.0"));
    }



}
