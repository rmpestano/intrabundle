package br.ufrgs.rmpestano.intrabundle.plugin;

import org.jboss.forge.project.Project;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.junit.Before;

/**
 * Created by rmpestano on 1/24/14.
 */
public abstract class BaseBundleTest extends SingletonAbstractShellTest {



    public Project initializeOSGiProject() throws Exception{
        DirectoryResource root = createTempFolder();
        DirectoryResource main = root.getOrCreateChildDirectory("module");
        addMetaInf(main,"/MANIFEST.MF");
        addActivator(main);
        addDeclarativeServices(main);
        addExportedPackage(main);
        getShell().setCurrentResource(main);
        return getProject();
    }


    private void addDeclarativeServices(DirectoryResource root) {
        DirectoryResource resource = root.getOrCreateChildDirectory("OSGI-INF");
        resource.createNewFile();
        FileResource<?> services = (FileResource<?>) resource.getChild("service.xml");
        services.setContents("services content");
    }

    private void addMetaInf(DirectoryResource root,String manifestName) {
        DirectoryResource metaInf = root.getOrCreateChildDirectory("META-INF");
        FileResource<?> fileResource = (FileResource<?>) metaInf.getChild("MANIFEST.MF");
        if(!fileResource.exists()){
            fileResource.setContents(getClass().getResourceAsStream(manifestName));

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

    private void addExportedPackage(DirectoryResource root){
     DirectoryResource resource =  root.getOrCreateChildDirectory("src").
               getOrCreateChildDirectory("br").
               getOrCreateChildDirectory("ufrgs").
               getOrCreateChildDirectory("rmpestano").
               getOrCreateChildDirectory("api");
        FileResource<?> exportedInterface = (FileResource<?>)resource.getChild("ExportedClass.java");
        exportedInterface.setContents("public interface Interface{}");
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
