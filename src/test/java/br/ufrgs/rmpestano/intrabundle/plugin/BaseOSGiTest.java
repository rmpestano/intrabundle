package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.jasper.JasperManager;
import br.ufrgs.rmpestano.intrabundle.util.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import java.io.IOException;

/**
 * Created by rmpestano on 1/24/14.
 */
public abstract class BaseOSGiTest extends SingletonAbstractShellTest {


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

    /**
     * creates an eclipse based OSGi project with a main folder
     * and 3 sub folders inside which are OSGi bundles
     */
    public Project initializeOSGiProject() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        TestUtils.addBundle(main, "module1");
        TestUtils.addHelloManager((DirectoryResource) main.getChild("module1"));
        TestUtils.addPermissions((DirectoryResource) main.getChild("module1"));
        TestUtils.addBundle(main, "module2");
        TestUtils.addBundle(main, "module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    /**
     * creates an OSGi maven based project where we have a master module
     * that aggregates sub modules(also maven projects) that are OSGi bundles
     */
    public Project initializeOSGiMavenProject() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        TestUtils.addPom(main);
        TestUtils.addMavenBundle(main, "module1");
        TestUtils.addMavenBundle(main, "module2");
        TestUtils.addMavenBundle(main, "module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    /**
     * creates an OSGi maven based project where we have a master module
     * that aggregates sub modules(also maven projects) that are OSGi bundles
     * with manifest in moduleRoot/META-INF instead of moduleRoot/src/main/resources/META-INF
     */
    public Project initializeOSGiMavenProjectWithManifestInRoot() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        TestUtils.addPom(main);
        TestUtils.addMavenBundleWithManifestInRoot(main, "module1");
        TestUtils.addMavenBundleWithManifestInRoot(main, "module2");
        TestUtils.addMavenBundleWithManifestInRoot(main, "module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    /**
     * create an OSGi project where bundles are separared by packages.
     * A real example of that is the Jitsi project:https://github.com/jitsi/jitsi
     */
    public Project initializeOSGiProjectWithBundlesInSourceCode() throws IOException {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        DirectoryResource basePackage = main.getOrCreateChildDirectory("src");
        DirectoryResource mod1 = basePackage.getOrCreateChildDirectory("module1");
        TestUtils.addManifest(mod1, "/MANIFEST-in-mod1-source.MF");
        TestUtils.addActivatorInSource(mod1.getOrCreateChildDirectory("br").getOrCreateChildDirectory("ufrgs").getOrCreateChildDirectory("rmpestano").getOrCreateChildDirectory("activator"));

        DirectoryResource mod2 = basePackage.getOrCreateChildDirectory("module2");
        TestUtils.addManifest(mod2, "/MANIFEST.MF");
        TestUtils.addActivator(mod2);

        DirectoryResource mod3 = basePackage.getOrCreateChildDirectory("module3");
        TestUtils.addManifest(mod3, "/MANIFEST.MF");
        TestUtils.addActivator(mod3);
        getShell().setCurrentResource(main);
        return getProject();
    }

    /**
     * OSGi project with 2 folder levels and inside 3 OSGi modules
     */
    protected void initializeOSGiProjectWithTwoFolderLevels() throws IOException {
        DirectoryResource root = createTempFolder();
        DirectoryResource level1 = root.getOrCreateChildDirectory("level1");
        DirectoryResource level2 = level1.getOrCreateChildDirectory("level2");
        TestUtils.addBundle(level2, "module1");
        TestUtils.addBundle(level2, "module2");
        TestUtils.addBundle(level2, "module3");
        getShell().setCurrentResource(level1);
    }

    /**
     * creates an eclipse OSGi project based on BND tools
     * which has 3 sub folders inside which are OSGi bundles
     */
    public Project initializeOSGiBNDProject() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        TestUtils.addBndBundle(main, "module1");
        TestUtils.addBndBundle(main, "module2");
        TestUtils.addBndBundle(main, "module3");
        getShell().setCurrentResource(main);
        return getProject();
    }

    /**
     * creates a maven OSGi project based on BND tools(maven bundle plugin)
     * which has 3 sub folders inside which are OSGi bundles
     */
    public Project initializeMavenOSGiBNDProject() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        TestUtils.addMavenBndBundle(main, "module2");
        TestUtils.addMavenBndBundle(main, "module1");
        TestUtils.addMavenBndBundle(main, "module3");
        getShell().setCurrentResource(main);
        return getProject();
    }


    public Project initializeOSGiProjectWithTestBundles() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addTestBundle(main, "module1");
        addTestBundle(main, "module2");
        getShell().setCurrentResource(main);
        return getProject();
    }

    public Project initializeOSGiProjectWithTestCodeInsideBundles() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addTestInsideBundle(main, "module1");
        addTestInsideBundle(main, "module2");
        getShell().setCurrentResource(main);
        return getProject();
    }



    /**
     * test code in dedicated test folder
     *
     * @param dir
     * @param module
     */
    protected void addTestBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        TestUtils.addMetaInfWithManifest(bundle, "/MANIFEST-" + module + ".MF");
        TestUtils.addActivator(bundle);
        DirectoryResource testFolder = dir.getChildDirectory("test");
        TestUtils.addMetaInfWithManifest(testFolder, "/MANIFEST-" + module + ".MF");
        DirectoryResource aPackage = testFolder.getChildDirectory("package");
        FileResource<?> test1 = (FileResource<?>) aPackage.getChild("TestClass1.java");
        test1.setContents("import junit.framework.Assert; public class TestClass {@Test public void aTest{}}");

        FileResource<?> test2 = (FileResource<?>) aPackage.getChild("TestClass2.java");
        test2.setContents("import org.testng.Assert; public class TestClass {@Test public void aTest{}}");
    }

    /**
     * test code inside bundle
     *
     * @param dir
     * @param module
     */
    protected void addTestInsideBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        TestUtils.addMetaInfWithManifest(bundle, "/MANIFEST-" + module + ".MF");
        TestUtils.addActivator(bundle);
        DirectoryResource testFolder = bundle.getChildDirectory("test");
        DirectoryResource aPackage = testFolder.getChildDirectory("package");
        FileResource<?> test1 = (FileResource<?>) aPackage.getChild("TestClass1.java");
        test1.setContents("import junit.framework.Assert; public class TestClass {@Test public void aTest{}}");

        FileResource<?> test2 = (FileResource<?>) aPackage.getChild("TestClass2.java");
        test2.setContents("import org.testng.Assert; public class TestClass {@Test public void aTest{}}");
    }




}
