package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.annotation.Current;
import br.ufrgs.rmpestano.intrabundle.i18n.ResourceBundle;
import br.ufrgs.rmpestano.intrabundle.locator.BundleProjectLocator;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.shell.util.OSUtils;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;

public class BundlePluginTest extends BaseBundleTest {


    @Inject
    @Current
    ResourceBundle resourceBundle;

    @Deployment
    public static JavaArchive getDeployment() {
        JavaArchive jar = SingletonAbstractShellTest.getDeployment()
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.facet")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.model")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.i18n")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.event")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.annotation")
                .addPackages(true, BundlePlugin.class.getPackage()).addClass(BundleProjectLocator.class);

        System.out.println(jar.toString(true));
        return jar;
    }

    @Test
    public void testDeclarativeServices() throws Exception {
        resetOutput();
        getShell().execute("bundle usesDeclarativeServices");
        Assert.assertTrue(getOutput().contains("true"));
    }

    @Test
    public void testPublishesInterfaces() throws Exception {
        resetOutput();
        getShell().execute("bundle publishesInterfaces");
        Assert.assertTrue(getOutput().contains("true"));
    }

    @Test
    public void activatorTest() throws Exception {
        resetOutput();
        getShell().execute("bundle activator");
        String activatorPath = "/module/src/br/ufrgs/rmpestano/activator/Activator.java";
        if(OSUtils.isWindows()){
            activatorPath = activatorPath.replaceAll("/", File.separator + File.separator);
        }
        Assert.assertTrue(getOutput().contains(activatorPath));
    }

    @Test
    public void exportedPackagesTest() throws Exception {
        resetOutput();
        getShell().execute("bundle exportedPackages");
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.api"));
    }

    @Test
    public void importedPackagesTest() throws Exception {
        resetOutput();
        getShell().execute("bundle importedPackages");
        Assert.assertTrue(getOutput().contains("org.osgi.framework"));
    }
}
