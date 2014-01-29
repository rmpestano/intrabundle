package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.annotation.Current;
import br.ufrgs.rmpestano.intrabundle.i18n.ResourceBundle;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.shell.util.OSUtils;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;

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

    @Inject
    @Current
    ResourceBundle resourceBundle;

    @Test
    public void testDefaultCommand() throws Exception {
        resetOutput();
        getShell().execute("osgi");
        Assert.assertTrue(getOutput().contains(resourceBundle.getString("osgi.defaultCommand")));
    }

    @Test
    public void testCount() throws Exception {
        resetOutput();
        getShell().execute("osgi countBundles");
        Assert.assertTrue(getOutput().contains("Total number of bundles:3"));

    }

    @Test
    public void testCountDifferentProject() throws Exception {
        initializeOSGiProject2();
        resetOutput();
        getShell().execute("osgi countBundles");
        Assert.assertTrue(getOutput().contains("Total number of bundles:4"));
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

    @Test
    public void testDeclarativeServices() throws Exception {
        resetOutput();
        getShell().execute("osgi usesDeclarativeServices");
        Assert.assertTrue(getOutput().contains(resourceBundle.getString("osgi.declarativeServices")+"\nmodule3"));
    }

    @Test
    public void activatorTest() throws Exception {
        resetOutput();
        getShell().execute("osgi listActivators");
        String activatorPath = "/main/module1/src/br/ufrgs/rmpestano/activator/Activator.java";
        if(OSUtils.isWindows()){
           activatorPath = activatorPath.replaceAll("/", File.separator + File.separator);
        }
        Assert.assertTrue(getOutput().contains(activatorPath));
    }

    //@Test
    public void testPrompt() throws Exception {
        //queueInputLines("y");
        //getShell().execute("os prompt foo bar");
    }
}
