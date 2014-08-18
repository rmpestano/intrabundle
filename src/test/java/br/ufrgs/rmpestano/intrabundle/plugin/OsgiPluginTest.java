package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.jasper.JasperManager;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.shell.util.OSUtils;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;

import static org.junit.Assert.assertTrue;

public class OsgiPluginTest extends BaseOSGiTest {

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
                .addClass(OsgiPlugin.class)
                .addClass(OSGiScanPlugin.class)
                .addClass(JasperManager.class)
                .addClass(LocalePlugin.class);
        //System.out.println(jar.toString(true));
        return jar;

    }

    @Inject
    MessageProvider provider;

    @Test
    public void shouldExecuteOSGiScan() throws Exception {
        resetOutput();
        initializeOSGiProjectWithTwoFolderLevels();
        queueInputLines("2");
        queueInputLines("n");
        getShell().execute("osgi-scan");
        Assert.assertTrue(getOutput().contains(provider.getMessage("osgi.scan.bundlesFound", 3)));
    }

    @Test
    public void shouldCountBundles() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("osgi countBundles");
        Assert.assertTrue(getOutput().startsWith("Total number of bundles:3"));
    }

    @Test
    public void shouldListBundles() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1" + getNewLine() + "module2" + getNewLine() + "module3"));
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
        Assert.assertTrue(getOutput().startsWith("module1" + getNewLine() + "module2" + getNewLine() + "module3"));
    }

    /**
     * test listActivator command in project where bundles
     * are inside src instead of being separated projects as usual
     */
    @Test
    public void shouldListActivatorsInSourceCode() throws Exception {
        initializeOSGiProjectWithBundlesInSourceCode();
        resetOutput();
        getShell().execute("osgi activators");
        String activatorPath = "/main/src/module1/br/ufrgs/rmpestano/activator/Activator.java";
        if(OSUtils.isWindows()){
            activatorPath = activatorPath.replaceAll("/", File.separator + File.separator);
        }
        Assert.assertTrue(getOutput().contains(activatorPath));
    }

    @Test
    public void shouldListBundlesInMavenProject() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1" + getNewLine() + "module2" + getNewLine()+ "module3"));
    }

    @Test
    public void shouldListBundlesInMavenProjectWithManifestInRoot() throws Exception {
        initializeOSGiMavenProjectWithManifestInRoot();
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1" + getNewLine() + "module2" + getNewLine()+ "module3"));
    }


    @Test
    public void module3ShouldUseDeclarativeServices() throws Exception {
        initializeOSGiProject();
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
        initializeOSGiProject();
        resetOutput();
        getShell().execute("osgi activators");
        String activatorPath = "/main/module1/src/br/ufrgs/rmpestano/activator/Activator.java";
        if(OSUtils.isWindows()){
           activatorPath = activatorPath.replaceAll("/", File.separator + File.separator);
        }
        Assert.assertTrue(getOutput().contains(activatorPath));
    }

    @Test
    public void shouldListExportedPackagesInModule1() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("n");
        queueInputLines("1");
        getShell().execute("osgi exportedPackages");
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.module1"));
        Assert.assertTrue(!getOutput().contains("br.ufrgs.rmpestano.module3"));
    }

    @Test
    public void shouldListExportedPackagesInMavenBndProject() throws Exception {
        initializeMavenOSGiBNDProject();
        resetOutput();
        queueInputLines("n");
        queueInputLines("1");
        getShell().execute("osgi exportedPackages");
        Assert.assertTrue(getOutput().contains("org.apache.karaf.scheduler"));
    }

    @Test
    public void shouldListExportedPackagesInAllModules() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("y");
        queueInputLines("1");
        getShell().execute("osgi exportedPackages");
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.package1"));
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.package2"));
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.module3"));
    }

    @Test
    public void shouldListImportedPackagesInAllModules() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("y");
        queueInputLines("1");
        getShell().execute("osgi importedPackages");
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.module3"));
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.package3"));
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.package4"));
    }

    @Test
    public void shouldListImportedPackagesInModule1() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("n");
        queueInputLines("1");
        getShell().execute("osgi importedPackages");
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.module3"));
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.package3"));
        Assert.assertFalse(getOutput().contains("br.ufrgs.rmpestano.package4"));
    }

    @Test
    public void shouldListImportedPackagesInMavenBndProject() throws Exception {
        initializeMavenOSGiBNDProject();
        resetOutput();
        queueInputLines("n");
        queueInputLines("1");
        getShell().execute("osgi importedPackages");
        Assert.assertTrue(getOutput().contains("weblogic.*"));
    }

    @Test
    public void shouldListModuleDependencies() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("y");
        getShell().execute("osgi dependencies");
        Assert.assertTrue(getOutput().contains("module3"));
    }

    @Test
    public void shouldListModuleDependenciesInModule1() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("n");
        queueInputLines("1");
        getShell().execute("osgi dependencies");
        Assert.assertTrue(getOutput().contains("module3"));
    }

    @Test
    public void shouldListRequiredBundles() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("y");
        getShell().execute("osgi requiredBundles");
        Assert.assertTrue(getOutput().contains("org.eclipse.core.runtime"));
        Assert.assertTrue(getOutput().contains("org.eclipse.osee.vaadin.widgets"));
    }

    @Test
    public void shouldListRequiredBundlesInMavebBndProject() throws Exception {
        System.out.println("shouldListRequiredBundlesInMavebBndProject");
        resetOutput();
        initializeMavenOSGiBNDProject();
        queueInputLines("y");
        getShell().execute("osgi requiredBundles");
        Assert.assertTrue(getOutput().contains("com.eclipse.bundle"));
    }

    @Test
    public void shouldListRequiredBundlesInModule2() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("n");
        queueInputLines("2");
        getShell().execute("osgi requiredBundles");
        Assert.assertTrue(getOutput().contains("org.eclipse.osee.vaadin.widgets"));
    }

    @Test
    public void shouldListStaleReferences() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("y");
        getShell().execute("osgi staleReferences");
        assertTrue(getOutput().contains("module1"));
        assertTrue(getOutput().contains("module2"));
    }

    @Test
    public void shouldListStaleReferencesInModule1() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("n");
        queueInputLines("1");
        getShell().execute("osgi staleReferences");
        assertTrue(getOutput().contains("HelloStaleManager.java"));
    }

    @Test
    public void shouldListModulesThatPublishesOnlyInterfaces() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("y");
        getShell().execute("osgi publishInterfaces");
        assertTrue(getOutput().contains("module1"));
        assertTrue(getOutput().contains("module2"));
        assertTrue(getOutput().contains("module3"));
    }

    @Test
    public void shouldListModulesThatDeclaresPermission() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("y");
        getShell().execute("osgi declaresPermissions");
        assertTrue(getOutput().startsWith("Listing modules that declares permissions:" + getNewLine()+"module1"));
    }

    /**
     * test listBundles command in eclipse bnd tools based OSGi project
     */
    @Test
    public void shouldListBundlesInBndProject() throws Exception {
        initializeOSGiBNDProject();
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1" + getNewLine() + "module2" + getNewLine() + "module3"));
    }

    /**
     * test listBundles command in maven bnd tools(maven bundle plugin) based OSGi project
     */
    @Test
    public void shouldListBundlesInMavenBndProject() throws Exception {
        initializeMavenOSGiBNDProject();
        resetOutput();
        getShell().execute("osgi countBundles");
        Assert.assertTrue(getOutput().startsWith("Total number of bundles:3"));
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1" + getNewLine() + "module2" + getNewLine() + "module3"));
    }

    @Test
    public void shouldCountBundlesInMavebBndProject() throws Exception {
        resetOutput();
        initializeMavenOSGiBNDProject();
        resetOutput();
        getShell().execute("osgi countBundles");
        Assert.assertTrue(getOutput().startsWith("Total number of bundles:3"));
    }

    @Test
    public void shouldCountLinesOfCode() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("osgi loc");
        assertTrue(getOutput().startsWith("module1:214"));
    }

    /**
     * count lines of test code inside each bundle in test source
     * @throws Exception
     */
    @Test
    public void shouldCountLinesOfTestCode() throws Exception {
        initializeOSGiProjectWithTestCodeInsideBundles();
        resetOutput();
        getShell().execute("osgi lot");
        assertTrue(getOutput().contains("Total lines of test code:4"));
    }

    /**
     * count lines of test code inside test folder(separated folder/project for tests)
     * @throws Exception
     */
    @Test
    public void shouldCountLinesOfTestCodeInsideTestBundles() throws Exception {
        initializeOSGiProjectWithTestBundles();
        resetOutput();
        getShell().execute("osgi lot");
        assertTrue(getOutput().contains("Total lines of test code:2"));
    }

}
