package br.ufrgs.rmpestano.intrabundle.plugin;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Facet;
import org.jboss.forge.project.Project;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class OsgiPluginTest extends BaseOSGiTest
{
   @Deployment
   public static JavaArchive getDeployment()
   {
      return SingletonAbstractShellTest.getDeployment()
            .addPackages(true, "br.ufrgs.rmpestano.intrabundle.facet")
            .addPackages(true, "br.ufrgs.rmpestano.intrabundle.locator")
            .addPackages(true, "br.ufrgs.rmpestano.intrabundle.model")
            .addPackages(true, OsgiPlugin.class.getPackage()).addAsResource("MANIFEST.MF");
   }

   @Test
   public void testDefaultCommand() throws Exception
   {
       Project p = initializeOSGiProject();
       for (Facet facet : p.getFacets()) {
           getShell().println(facet.getClass().getSimpleName());
       }
       getShell().execute("osgi");
   }

   //@Test
   public void testCommand() throws Exception
   {
      //getShell().execute("os command");
   }

   //@Test
   public void testPrompt() throws Exception
   {
      //queueInputLines("y");
      //getShell().execute("os prompt foo bar");
   }
}
