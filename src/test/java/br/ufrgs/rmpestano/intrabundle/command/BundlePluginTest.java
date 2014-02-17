package br.ufrgs.rmpestano.intrabundle.command;

import junit.framework.Assert;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.junit.Test;

import java.io.File;

public class BundlePluginTest extends BaseBundleTest {

 


    @Test
    public void shouldUseDeclarativeServices() throws Exception {
        resetOutput();
        shellTest.execute("bundle usesDeclarativeServices");
        Assert.assertTrue(getOutput().contains("yes"));
    }

    @Test
    public void shouldPublishInterfaces() throws Exception {
        resetOutput();
        shellTest.execute("bundle publishesInterfaces");
        Assert.assertTrue(getOutput().contains("yes"));
    }

    @Test
    public void shouldDeclarePermissions() throws Exception{
        resetOutput();
        shellTest.execute("bundle declaresPermissions");
        Assert.assertTrue(getOutput().contains("yes"));
    }

    @Test
    public void shouldNotUseBlueprint() throws Exception{
        resetOutput();
        shellTest.execute("bundle usesBlueprint");
        Assert.assertTrue(getOutput().contains("no"));
    }

    @Test
    public void shouldFindActivator() throws Exception {
        resetOutput();
        shellTest.execute("bundle activator");
        String activatorPath = "/module/src/br/ufrgs/rmpestano/activator/Activator.java";
        if(OperatingSystemUtils.isWindows()){
            activatorPath = activatorPath.replaceAll("/", File.separator + File.separator);
        }
        Assert.assertTrue(getOutput().contains(activatorPath));
    }

    @Test
    public void shouldGetExportedPackages() throws Exception {
        resetOutput();
        shellTest.execute("bundle exportedPackages");
        Assert.assertTrue(getOutput().contains("br.ufrgs.rmpestano.api"));
    }

    @Test
    public void shouldGetImportedPackages() throws Exception {
        resetOutput();
        shellTest.execute("bundle importedPackages");
        Assert.assertTrue(getOutput().contains("org.osgi.framework"));
    }

    @Test
    public void shouldCountLinesOfCode() throws Exception {
        resetOutput();
        shellTest.execute("bundle loc");
        Assert.assertTrue(getOutput().startsWith("2"));
    }

    @Test
    public void shouldFindRequiredBundle() throws Exception {
        resetOutput();
        shellTest.execute("bundle requiredBundles");
        Assert.assertTrue(getOutput().contains("org.apache.tuscany.sdo.spec;visibility:=reexport"));
    }

    /**
     * lines of test code test
     */
    @Test
    public void shouldCountLinesOfTest() throws Exception {
        resetOutput();
        shellTest.execute("bundle lot");
        Assert.assertTrue(getOutput().startsWith("1"));
    }

    @Test
    public void shouldContainStaleReferences() throws Exception {
        resetOutput();
        shellTest.execute("bundle staleReferences");
        Assert.assertTrue(getOutput().contains("HelloStaleManager.java"));
    }

}
