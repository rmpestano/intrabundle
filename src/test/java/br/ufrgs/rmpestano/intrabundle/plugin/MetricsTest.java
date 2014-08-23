package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.metric.Metrics;
import org.junit.Test;

import javax.inject.Inject;

import static junit.framework.Assert.assertTrue;

public class MetricsTest extends BaseMetricsTest {




    @Inject
    Metrics metrics;

   
    @Test
    public void aTest(){
      assertTrue(true);
    }

}
