package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.locator.OSGiProjectLocator;
import br.ufrgs.rmpestano.intrabundle.util.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Created by rmpestano on 8/23/14.
 */
public abstract class BaseMetricsTest extends SingletonAbstractShellTest{

    @Deployment
    public static JavaArchive getDeployment() {
        JavaArchive jar = TestUtils.getBaseDeployment();
        jar.addPackages(true, OSGiProjectLocator.class.getPackage()) ;
        //System.out.println(jar.toString(true));
        return jar;

    }

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

    public Project initializeOSGiBundleWithStaleReferences() throws Exception {
        DirectoryResource root = createTempFolder();
        DirectoryResource mod1 = TestUtils.addMavenBundle(root,"module1");
        FileResource<?> aClass = (FileResource<?>) mod1.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("main").
                getOrCreateChildDirectory("java").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").getChild("AClass.java");
         if(!aClass.exists()){
             aClass.setContents(TestUtils.class.getResourceAsStream("/ClassWithLotsOfLines.java"));
         }
        getShell().setCurrentResource(mod1);
        return getProject();
    }


    public Project initializeMavenBundle() throws Exception {
        DirectoryResource root = createTempFolder();
        getShell().setCurrentResource(TestUtils.addMavenBundle(root, "module1"));
        return getProject();
    }
}
