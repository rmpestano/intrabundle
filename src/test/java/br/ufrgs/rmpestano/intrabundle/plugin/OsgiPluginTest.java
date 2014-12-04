package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.util.TestUtils;
import junit.framework.Assert;
import org.jboss.forge.shell.util.OSUtils;
import org.junit.Test;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.File;
import java.util.Set;

import static org.junit.Assert.*;

public class OsgiPluginTest extends BaseOSGiTest {



    @Inject
    MessageProvider provider;


    @Inject
    Instance<OSGiProject> currentOsgiProject;

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
        Assert.assertTrue(getOutput().startsWith("module1" + TestUtils.getNewLine() + "module2" + TestUtils.getNewLine() + "module3"));
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
        Assert.assertTrue(getOutput().startsWith("module1" + TestUtils.getNewLine() + "module2" + TestUtils.getNewLine() + "module3"));
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
        String mod1Expected = "/main/src/module1/br/ufrgs/rmpestano/activator/Activator.java";
        String mod2Expected = "/main/src/module2/br/ufrgs/rmpestano/activator/Activator.java";
        String mod3Expected = "/main/src/module3/src/br/ufrgs/rmpestano/activator/Activator.java";
        if(OSUtils.isWindows()){
            mod1Expected = mod1Expected.replaceAll("/", File.separator + File.separator);
            mod2Expected = mod2Expected.replaceAll("/", File.separator + File.separator);
            mod3Expected = mod3Expected.replaceAll("/", File.separator + File.separator);
        }
        getOutput().contains(mod1Expected);
        getOutput().contains(mod2Expected);
        getOutput().contains(mod3Expected);


    }

    @Test
    public void shouldListBundlesInMavenProject() throws Exception {
        initializeOSGiMavenProject();
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1" + TestUtils.getNewLine() + "module2" + TestUtils.getNewLine()+ "module3"));
    }

    @Test
    public void shouldListBundlesInMavenProjectWithManifestInRoot() throws Exception {
        initializeOSGiMavenProjectWithManifestInRoot();
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1" + TestUtils.getNewLine() + "module2" + TestUtils.getNewLine()+ "module3"));
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
        Set<OSGiModule> dependencies = currentOsgiProject.get().getModulesDependencies().keySet();
        for (OSGiModule dependency : dependencies) {
            if(dependency.getName().equals("module1")){
                assertEquals(currentOsgiProject.get().getModulesDependencies().get(dependency).size(),1);
                assertTrue(currentOsgiProject.get().getModulesDependencies().get(dependency).get(0).getName().equals("module3"));
            }
        }
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
        assertTrue(currentOsgiProject.get().getModules().get(1).getRequiredBundles().contains("org.eclipse.osee.vaadin.widgets"));
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
        assertTrue(currentOsgiProject.get().getModules().get(0).getStaleReferences().get(0).getName().equals("HelloStaleManager.java"));

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
        assertTrue(currentOsgiProject.get().getModules().get(0).getDeclaresPermissions());
    }

    @Test
    public void shouldListModulesThatDeclaresPermission() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("y");
        getShell().execute("osgi declaresPermissions");
        assertTrue(getOutput().startsWith("Listing modules that declares permissions:" + TestUtils.getNewLine()+"module1"));
    }

    /**
     * test listBundles command in eclipse bnd tools based OSGi project
     */
    @Test
    public void shouldListBundlesInBndProject() throws Exception {
        initializeOSGiBNDProject();
        resetOutput();
        getShell().execute("osgi listBundles");
        Assert.assertTrue(getOutput().startsWith("module1" + TestUtils.getNewLine() + "module2" + TestUtils.getNewLine() + "module3"));
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
        Assert.assertTrue(getOutput().startsWith("module1" + TestUtils.getNewLine() + "module2" + TestUtils.getNewLine() + "module3"));
    }

    @Test
    public void shouldCountBundlesInMavebBndProject() throws Exception {
        resetOutput();
        initializeMavenOSGiBNDProject();
        resetOutput();
        getShell().execute("osgi countBundles");
        Assert.assertTrue(getOutput().startsWith("Total number of bundles:3"));
        assertEquals(currentOsgiProject.get().getModules().size(),3);
    }


    @Test
    public void shouldCountLinesOfCode() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("osgi loc");
        assertTrue(getOutput().startsWith("module1:255"));
        assertNotNull(currentOsgiProject.get());
        assertTrue(currentOsgiProject.get().getLinesOfCode().equals(339L));
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
        assertTrue(currentOsgiProject.get().getLinesOfTestCode().equals(2L));
        assertTrue(getOutput().contains("Total lines of test code:2"));
    }

    @Test
    public void shouldListModulesByQuality() throws Exception {
        initializeOSGiProject();
        resetOutput();
        queueInputLines("1");
        getShell().execute("osgi findBundlesByQuality");
        assertTrue(getOutput().contains(provider.getMessage("osgi.scan.noBundlesFound")));
        resetOutput();
        queueInputLines("2");
        getShell().execute("osgi findBundlesByQuality");
        assertTrue(getOutput().contains(provider.getMessage("osgi.scan.noBundlesFound")));
        resetOutput();
        queueInputLines("3");
        getShell().execute("osgi findBundlesByQuality");
        assertTrue(getOutput().trim().endsWith(TestUtils.getNewLine()+"module1"));
        assertFalse(getOutput().contains(provider.getMessage("osgi.scan.noBundlesFound")));
        resetOutput();
        queueInputLines("4");
        getShell().execute("osgi findBundlesByQuality");
        assertTrue(getOutput().trim().endsWith("module2"));
        assertFalse(getOutput().contains(provider.getMessage("osgi.scan.noBundlesFound")));
        resetOutput();
        queueInputLines("5");
        getShell().execute("osgi findBundlesByQuality");
        assertTrue(getOutput().contains(TestUtils.getNewLine()+"module3"));
    }

}
