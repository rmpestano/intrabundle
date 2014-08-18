package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.locator.BundleProjectLocator;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.util.BeanManagerController;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.shell.util.OSUtils;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;

import static org.junit.Assert.assertNotNull;

public class BundlePluginTest extends BaseBundleTest {


    @Inject
    MessageProvider provider;

    @Deployment
    public static JavaArchive getDeployment() {
        JavaArchive jar = SingletonAbstractShellTest.getDeployment()
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.facet")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.model")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.i18n")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.event")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.util")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.jdt")
                .addClass(ProjectUtils.class)
                .addClass(BundlePlugin.class).
                addClass(LocalePlugin.class).
                addClass(BundleProjectLocator.class)
                ;
        //System.out.println(jar.toString(true));
        return jar;
    }

    @Test
    public void shouldUseDeclarativeServices() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle usesDeclarativeServices");
        Assert.assertTrue(getOutput().contains(provider.getMessage("yes")));
    }

    @Test
    public void shouldPublishInterfaces() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle publishesInterfaces");
        Assert.assertTrue(getOutput().contains(provider.getMessage("yes")));
    }

    @Test
    public void shouldDeclarePermissions() throws Exception{
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle declaresPermissions");
        Assert.assertTrue(getOutput().contains(provider.getMessage("yes")));
    }

    @Test
    public void shouldNotUseBlueprint() throws Exception{
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle usesBlueprint");
        Assert.assertTrue(getOutput().contains(provider.getMessage("no")));
    }

    @Test
    public void shouldFindActivator() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle activator");
        String activatorPath = "/module/src/br/ufrgs/rmpestano/activator/Activator.java";
        if(OSUtils.isWindows()){
            activatorPath = activatorPath.replaceAll("/", File.separator + File.separator);
        }
        Assert.assertTrue(getOutput().contains(activatorPath));
    }

    @Test
    public void shouldGetBundleVersion() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle version");
        Assert.assertTrue(getOutput().startsWith("1.0.0.qualifier"));
    }

    @Test
    public void shouldGetExportedPackages() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle exportedPackages");
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.api"));
    }

    @Test
    public void shouldGetImportedPackages() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle importedPackages");
        Assert.assertTrue(getOutput().contains("org.osgi.framework"));
    }

    @Test
    public void shouldCountLinesOfCode() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle loc");
        Assert.assertTrue(getOutput().startsWith("2"));
    }

    @Test
    public void shouldFindRequiredBundle() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle requiredBundles");
        Assert.assertTrue(getOutput().contains("org.apache.tuscany.sdo.spec;visibility:=reexport"));
    }

    /**
     * lines of test code test
     */
    @Test
    public void shouldCountLinesOfTest() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle lot");
        Assert.assertTrue(getOutput().startsWith("1"));
    }

    @Test
    public void shouldContainStaleReferences() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().clear();
        getShell().execute("bundle staleReferences");
        Assert.assertTrue(getOutput().contains("HelloStaleManager.java"));
    }

    @Test
    public void shouldCountNumberOfClasses() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle nc");
        Assert.assertTrue(getOutput().startsWith("3"));
    }

    @Test
    public void shouldCountNumberOfAbstractClasses() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle na");
        Assert.assertTrue(getOutput().startsWith("1"));
    }

    @Test
    public void shouldCountNumberOfInterfaces() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle ni");
        Assert.assertTrue(getOutput().startsWith("2"));
    }

    @Test
    public void shouldCountNumberOfPackages() throws Exception {
        initializeOSGiProject();
        resetOutput();
        getShell().execute("bundle np");
        Assert.assertTrue(getOutput().startsWith("4"));
    }


    /**
     * test find activator command in eclipse bnd tools based OSGi project
     */
    @Test
    public void shouldFindActivatorInBndProject() throws Exception {
        resetOutput();
        initializeOSGiBNDProject();
        resetOutput();
        getShell().execute("bundle activator");
        String expected = OSUtils.isWindows() ? "\\module1\\src\\br\\ufrgs\\rmpestano\\activator\\Activator.java" : "/module1/src/br/ufrgs/rmpestano/activator/Activator.java";
        Assert.assertTrue(getOutput().contains(expected));
    }

    /**
     * test find activator command in maven bnd based OSGi bundle
     */
    //@Test
    public void shouldFindActivatorInMavenBndBundle() throws Exception {
        initializeMavenOSGiBNDProject();
        resetOutput();
        getShell().execute("bundle activator");
        String expected = OSUtils.isWindows() ? "\\module1\\src\\br\\ufrgs\\rmpestano\\activator\\Activator.java" : "/module1/src/br/ufrgs/rmpestano/activator/Activator.java";
        Assert.assertTrue(getOutput().contains(expected));
    }

    /**
     * test find activator command in eclipse bnd tools based OSGi project with bnd file in resources folder
     */
    @Test
    public void shouldFindActivatorInBndProjectWithResources() throws Exception {
        initializeOSGiBNDProjectWithBndInResources();
        resetOutput();
        getShell().execute("bundle activator");
        String expected = OSUtils.isWindows() ? "\\module1\\src\\br\\ufrgs\\rmpestano\\activator\\Activator.java" : "/module1/src/br/ufrgs/rmpestano/activator/Activator.java";
        Assert.assertTrue(getOutput().contains(expected));
        OSGiModule osGiModule = BeanManagerController.getBeanByType(getBeanManager(), OSGiModule.class);
        assertNotNull(osGiModule);
        Assert.assertTrue(osGiModule.getActivator().getFullyQualifiedName().equals(expected));
    }

    @Test
    public void shouldUseIpojo() throws Exception {
        resetOutput();
        initializeOSGiProjectWithIpojoComponent();
        resetOutput();
        getShell().execute("bundle usesIpojo");
        Assert.assertTrue(getOutput().startsWith(provider.getMessage("yes")));
    }

    @Test
    public void shouldHasOneIpojoComponent() throws Exception {
        resetOutput();
        initializeOSGiProjectWithIpojoComponent();
        resetOutput();
        getShell().execute("bundle numberOfIpojoComponents");
        Assert.assertTrue(getOutput().startsWith("1"));
    }

}
