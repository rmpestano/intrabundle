package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.jasper.JasperManager;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
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
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.event")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.jdt")
                .addClass(OsgiPlugin.class)
                .addClass(JasperManager.class)
                .addClass(LocalePlugin.class)
                .addClass(ProjectUtils.class);
        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    MessageProvider provider;

    @Test
    public void shouldExecuteDefaultCommand() throws Exception {
        resetOutput();
        getShell().execute("osgi");
        Assert.assertTrue(getOutput().contains(provider.getMessage("osgi.defaultCommand")));
    }

    @Test
    public void shouldCountBundles() throws Exception {
        resetOutput();
        getShell().execute("osgi countBundles");
        Assert.assertTrue(getOutput().contains("Total number of bundles:3"));
    }

    @Test
    public void shouldListBundles() throws Exception {
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1\nmodule2\nmodule3"));
    }

    /**
     * test listBundles command in project where bundles
     * are inside src instead of being separated projects as usual
     */
    @Test
    public void shouldListBundlesInSourceCode() throws Exception {
        initializeOSGiProjectWithBundlesInSourceCode();
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1\nmodule2\nmodule3"));

    }

    @Test
    public void shouldListBundlesInMavenProject() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1\nmodule2\nmodule3"));
    }


    @Test
    public void module3ShouldUseDeclarativeServices() throws Exception {
        resetOutput();
        getShell().execute("osgi usesDeclarativeServices");
        String assertion = "\nmodule3";
        if(OSUtils.isWindows()){
            assertion = "\r"+assertion;
        }
        Assert.assertTrue(getOutput().contains(provider.getMessage("osgi.declarativeServices") + assertion));
    }

    @Test
    public void shouldFindActivatorClass() throws Exception {
        resetOutput();
        getShell().execute("osgi activators");
        String activatorPath = "/main/module1/src/br/ufrgs/rmpestano/activator/Activator.java";
        if(OSUtils.isWindows()){
           activatorPath = activatorPath.replaceAll("/", File.separator + File.separator);
        }
        Assert.assertTrue(getOutput().contains(activatorPath));
    }

    @Test
    public void shouldListExportedPackages() throws Exception {
        resetOutput();
        queueInputLines("y");
        queueInputLines("1");
        getShell().execute("osgi exportedPackages");
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.package2"));
    }

    @Test
    public void shouldListImportedPackages() throws Exception {
        resetOutput();
        queueInputLines("y");
        queueInputLines("1");
        getShell().execute("osgi importedPackages");
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.package3"));
    }

    @Test
    public void shouldListModuleDependencies() throws Exception {
        resetOutput();
        queueInputLines("1");
        getShell().execute("osgi moduleDependencies");
        Assert.assertTrue(getOutput().contains("module3"));
    }


}