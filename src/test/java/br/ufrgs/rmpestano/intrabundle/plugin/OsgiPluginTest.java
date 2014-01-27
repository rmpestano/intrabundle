package br.ufrgs.rmpestano.intrabundle.plugin;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class OsgiPluginTest extends BaseOSGiTest {

    @Deployment
    public static JavaArchive getDeployment() {
        JavaArchive jar = SingletonAbstractShellTest.getDeployment()
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.facet")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.locator")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.model")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.i18n")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.event")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.annotation")
                .addPackages(true, OsgiPlugin.class.getPackage())
                .addAsResource("MANIFEST.MF");
        System.out.println(jar.toString(true));
        return jar;
    }

    @Test
    public void testDefaultCommand() throws Exception {
        getShell().execute("osgi");
    }

    @Test
    public void testCount() throws Exception {
        getShell().execute("osgi countBundles");
    }

    @Test
    public void testCountDifferentProject() throws Exception {
        initializeOSGiProject2();
        getShell().execute("osgi countBundles");
    }

    public Project initializeOSGiProject2() throws Exception
    {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addBundle(main,"module1");
        addBundle(main,"module2");
        addBundle(main,"module3");
        addBundle(main,"module4");
        getShell().setCurrentResource(main);
        return getProject();
    }

    //@Test
    public void testPrompt() throws Exception {
        //queueInputLines("y");
        //getShell().execute("os prompt foo bar");
    }
}
