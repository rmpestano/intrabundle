package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.util.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Created by rmpestano on 1/24/14.
 */
public abstract class BaseBundleTest extends SingletonAbstractShellTest {

    @Deployment
    public static JavaArchive getDeployment() {
        JavaArchive jar = TestUtils.getBaseDeployment();
        //System.out.println(jar.toString(true));
        return jar;
    }


    public Project initializeOSGiProject() throws Exception{
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("module");
        TestUtils.addMetaInfWithManifest(main, "/MANIFEST.MF");
        TestUtils.addActivator(main);
        TestUtils.addTestClass(main);
        TestUtils.addAbstractClass(main);
        TestUtils.addInterface(main);
        TestUtils.addPermissions(main);
        TestUtils.addExportedPackage(main);
        TestUtils.addHelloManager(main);
        getShell().setCurrentResource(main);
        return getProject();
    }

    public Project initializeOSGiProjectWithIpojoComponent() throws Exception{
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("module");
        TestUtils.addMetaInfWithManifest(main, "/MANIFEST.MF");
        TestUtils.addActivator(main);
        TestUtils.addTestClass(main);
        TestUtils.addPermissions(main);
        TestUtils.addExportedPackage(main);
        TestUtils.addHelloManager(main);
        TestUtils.addIpojoComponent(main);
        getShell().setCurrentResource(main);
        return getProject();
    }


    /**
     * creates an maven OSGi project based on BND tools(maven bundle plugin)
     */
    public Project initializeMavenOSGiBNDProject() throws Exception
    {
        DirectoryResource root = createTempFolder();
        getShell().setCurrentResource(addMavenBndBundle(root, "module1"));
        return getProject();

    }

    /**
     * creates an eclipse OSGi project based on BND tools
     */
    public Project initializeOSGiBNDProject() throws Exception
    {
        DirectoryResource root = createTempFolder();
        getShell().setCurrentResource(addBndBundle(root, "module1"));
        return getProject();
    }

    /**
     * creates an eclipse OSGi project based on BND tools where bnd file is in resources folder
     */
    public Project initializeOSGiBNDProjectWithBndInResources() throws Exception
    {
        DirectoryResource root = createTempFolder();
        getShell().setCurrentResource(addBndBundleInResources(root, "module1"));
        return getProject();
    }

    protected DirectoryResource addMavenBndBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        TestUtils.addMavenActivator(bundle);
        TestUtils.addBndPom(bundle);
        return bundle;
    }

    protected DirectoryResource addBndBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        TestUtils.addActivator(bundle);
        TestUtils.addBnd(bundle);
        return bundle;
    }

    protected DirectoryResource addBndBundleInResources(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        DirectoryResource resources = bundle.getOrCreateChildDirectory("resources");
        TestUtils.addActivator(bundle);
        TestUtils.addBnd(resources);
        return bundle;
    }


    

   


}
