package br.ufrgs.rmpestano.intrabundle.plugin;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import br.ufrgs.rmpestano.intrabundle.jasper.JasperManager;
import br.ufrgs.rmpestano.intrabundle.metric.Metrics;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class MetricsTest extends BaseOSGiTest {

    @Deployment
    public static JavaArchive getDeployment() {
        JavaArchive jar = SingletonAbstractShellTest.getDeployment()
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.facet")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.locator")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.model")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.i18n")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.event")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.util")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.jdt")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.metric")
                .addClass(OsgiPlugin.class)
                .addClass(OSGiScanPlugin.class)
                .addClass(JasperManager.class)
                .addClass(LocalePlugin.class);
        //System.out.println(jar.toString(true));
        return jar;

    }


    @Inject
    Metrics metrics;

   
    @Test
    public void aTest(){
      assertTrue(true);
    }

}
