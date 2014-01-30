package br.ufrgs.rmpestano.intrabundle.plugin;

import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.junit.Before;

/**
 * Created by rmpestano on 1/24/14.
 */
public abstract class BaseOSGiTest extends SingletonAbstractShellTest {



    public Project initializeOSGiProject() throws Exception
    {
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("main");
        addBundle(main,"module1");
        addBundle(main,"module2");
        addBundle(main,"module3");
        getShell().setCurrentResource(main);
        return getProject();
    }


    protected void addBundle(DirectoryResource dir, String module) {
        DirectoryResource bundle = dir.getOrCreateChildDirectory(module);
        addMetaInf(bundle);
        addActivator(bundle);
        if(module.equals("module3")){
            addDeclarativeServices(bundle);
        }
    }

    private void addDeclarativeServices(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("OSGI-INF");
        resource.createNewFile();
        FileResource<?> services = (FileResource<?>) resource.getChild("service.xml");
        services.setContents("services content");
    }

    private void addMetaInf(DirectoryResource root) {
        DirectoryResource metaInf = root.getOrCreateChildDirectory("META-INF");
        FileResource<?> fileResource = (FileResource<?>) metaInf.getChild("MANIFEST.MF");
        if(!fileResource.exists()){
            fileResource.setContents(getClass().getResourceAsStream("/MANIFEST.MF"));

        }
    }

    private void addActivator(DirectoryResource root){
        DirectoryResource resource = root.getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("br").
                getOrCreateChildDirectory("ufrgs").
                getOrCreateChildDirectory("rmpestano").
                getOrCreateChildDirectory("activator");
        FileResource<?> activator = (FileResource<?>) resource.getChild("Activator.java");
        activator.setContents("activator content");
    }


    @Before
    public void initProject(){
        try {
            initializeOSGiProject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
