package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.Utils;
import org.jboss.forge.project.BaseProject;
import org.jboss.forge.project.Facet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.FacetNotFoundException;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

import javax.enterprise.inject.Typed;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rmpestano on 1/22/14.
 */
@Typed()
public class OSGiModuleImpl extends BaseProject implements OSGiModule,Project {
    private DirectoryResource projectRoot = null;
    private final ProjectFactory factory;
    private Long totalLoc;
    private Boolean usesDeclarativeServices;
    private FileResource<?> activator;
    private FileResource<?> manifest;
    private List<String> exportedPackages;
    private List<String> importedPackages;
    private Boolean publishesInterfaces;


    public OSGiModuleImpl() {
        factory = null;
    }

    public OSGiModuleImpl(final ProjectFactory factory, final DirectoryResource dir) {
        this.factory = factory;
        this.projectRoot = dir;
    }

    @Override
    public <F extends Facet> F getFacet(final Class<F> type) {
        try {
            return super.getFacet(type);
        } catch (FacetNotFoundException e) {
            factory.registerSingleFacet(this, type);
            return super.getFacet(type);
        }
    }

    @Override
    public DirectoryResource getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(DirectoryResource projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public boolean exists() {
        return (projectRoot != null) && projectRoot.exists();
    }

    @Override
    public String toString() {
        return getProjectRoot().toString();
    }

    private FileResource<?> findActivator() throws IOException {
        RandomAccessFile randomAccessFile;
        File f = new File(getManifest().getFullyQualifiedName());
        randomAccessFile = new RandomAccessFile(f, "r");

        String line;
        while((line = randomAccessFile.readLine()) != null){
            if (line.contains("Bundle-Activator:")) {
               break;
            }
        }
        if(line == null){
            return null;//no activator
        }
        String actvatorPath = line.trim().substring(line.indexOf("Bundle-Activator:") + 17);
        actvatorPath = actvatorPath.trim().replaceAll("\\.","/");
        if(!actvatorPath.startsWith("/")){
            actvatorPath = "/" +actvatorPath;
        }
        Resource<?> activator = Utils.getProjectSourcePath(projectRoot).getChild(actvatorPath.concat(".java"));
        if(activator == null || !activator.exists()){
            throw new RuntimeException("Could not find activator class at "+getProjectRoot() + actvatorPath);
        }

        return (FileResource<?>) activator;

    }

    private Long countModuleLines(DirectoryResource projectRoot) {
        for (Resource<?> resource : projectRoot.listResources()) {
            if (resource instanceof FileResource<?> && resource.getName().endsWith(".java")) {
                try {
                    this.totalLoc += countFileLines((FileResource<?>) resource);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resource instanceof DirectoryResource) {
                this.totalLoc = countModuleLines((DirectoryResource) resource);
            }
        }
        return totalLoc;
    }

    private Long countFileLines(FileResource<?> resource) throws IOException {
        RandomAccessFile file = new RandomAccessFile(new File(resource.getFullyQualifiedName()), "r");
        Long total = new Long(0);
        String line;
        while ((line = file.readLine()) != null) {
            total++;
        }
        return total;
    }

    private boolean usesDeclarativeServices() {
        Resource<?> OSGiInf = Utils.getProjectResourcesPath(projectRoot).getChild("OSGI-INF");
        return OSGiInf.exists() && OSGiInf.getChild("service.xml").exists();
    }

    private FileResource<?> findManifest() {
        Resource<?> metaInf = Utils.getProjectMetaInfPath(projectRoot);
        if (metaInf == null || !metaInf.exists()) {
            throw new RuntimeException("OSGi project(" + getProjectRoot().getFullyQualifiedName() + ") without META-INF directory cannot be analysed by intrabundle");
        }
        Resource<?> manifest = metaInf.getChild("MANIFEST.MF");
        if (manifest == null || !manifest.exists()) {
            throw new RuntimeException("OSGi project(" + getProjectRoot().getFullyQualifiedName() + ") without MANIFEST.MF file cannot be analysed by intrabundle");
        }
        return (FileResource<?>) manifest;
    }

    private List<String> findExportedPackages() {
        exportedPackages = new ArrayList<String>();
        try {
            RandomAccessFile file = new RandomAccessFile(new File(getManifest().getFullyQualifiedName()), "r");
            String line;
            while ((line = file.readLine())!= null){
                  if(line.contains("Export-Package")){
                      line = line.substring(line.lastIndexOf(":")+1).trim();
                      if(!"".equals(line)){
                          exportedPackages.addAll(Arrays.asList(line.split(",")));
                      }
                      //try to get packages from next lines
                      String nextLine;
                      while((nextLine  = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")){
                          exportedPackages.addAll(Arrays.asList(nextLine.trim().split(",")));
                      }
                      break;

                  }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        }
        finally {
            return exportedPackages;
        }
    }

    private List<String> findImportedPackages() {
        importedPackages = new ArrayList<String>();
        try {
            RandomAccessFile file = new RandomAccessFile(new File(getManifest().getFullyQualifiedName()), "r");
            String line;
            while ((line = file.readLine())!= null){
                if(line.contains("Import-Package")){
                    line = line.substring(line.lastIndexOf(":")+1).trim();
                    if(!"".equals(line)){
                        importedPackages.addAll(Arrays.asList(line.split(",")));
                    }
                    //try to get packages from next lines
                    String nextLine;
                    while((nextLine  = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")){
                        importedPackages.addAll(Arrays.asList(nextLine.trim().split(",")));
                    }
                    break;

                }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        }
        finally {
            return importedPackages;
        }
    }


    //getters

    public Boolean getUsesDeclarativeServices() {
        if (usesDeclarativeServices == null) {
            usesDeclarativeServices = usesDeclarativeServices();
        }
        return usesDeclarativeServices;
    }

    @Override
    public FileResource<?> getActivator() {
        if (activator == null) {
            try {
                activator = findActivator();
            } catch (IOException e) {
                throw new RuntimeException("Could not find activator class");
            }
        }
        return activator;
    }

    @Override
    public FileResource<?> getManifest() {
        if (manifest == null) {
            manifest = findManifest();
        }
        return manifest;
    }


    public Long getLinesOfCode() {
        if (totalLoc == null) {
            totalLoc = new Long(0L);
            totalLoc = countModuleLines(getProjectRoot());
        }
        return totalLoc;
    }

    public List<String> getExportedPackages() {
        if(exportedPackages == null){
            exportedPackages = findExportedPackages();
        }
        return exportedPackages;
    }


    public List<String> getImportedPackages() {
        if(importedPackages == null){
            importedPackages = findImportedPackages();
        }
        return importedPackages;
    }

    public Boolean getPublishesInterfaces() {
        if(publishesInterfaces == null){
            publishesInterfaces = publishedInterfaces();
        }
        return publishesInterfaces;
    }

    /**
     * verifies if exported packages by the bundle are only exporting interfaces
     */
    private Boolean publishedInterfaces() {
        if(getExportedPackages().isEmpty()){
            return false;
        }

        for (String exportedPackage : getExportedPackages()) {
            String exportedPackagePath = exportedPackage.trim().replaceAll("\\.","/");
            if(!exportedPackagePath.startsWith("/")){
                exportedPackagePath = "/" +exportedPackagePath;
            }
            List<Resource<?>> resources =  Utils.getProjectSourcePath(projectRoot).getChild(exportedPackagePath).listResources(new ResourceFilter() {
                @Override
                public boolean accept(Resource<?> resource) {
                    return resource.getName().endsWith(".java");
                }
            });

            if(!resources.isEmpty()){
                for (Resource<?> resource : resources) {
                    if(!Utils.isInterface((FileResource<?>) resource)){
                        return false;
                    }
                }

            }

        }
        //all exported packages contains only interfaces
        return true;

    }
}
