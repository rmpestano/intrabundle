package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.jasper.JasperManager;
import br.ufrgs.rmpestano.intrabundle.util.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Created by rmpestano on 8/23/14.
 */
public abstract class BaseMetricsTest extends SingletonAbstractShellTest{

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
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.metric")
                .addClass(OsgiPlugin.class)
                .addClass(JasperManager.class)
                .addClass(LocalePlugin.class);
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
}
